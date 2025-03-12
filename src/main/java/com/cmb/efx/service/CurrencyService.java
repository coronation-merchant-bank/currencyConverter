package com.cmb.efx.service;

import com.cmb.efx.dto.*;
import com.cmb.efx.entity.Currency;
import com.cmb.efx.entity.Rates;
import com.cmb.efx.entity.Transactions;
import com.cmb.efx.exception.CustomException;
import com.cmb.efx.exception.InvalidTokenException;
import com.cmb.efx.repository.CurrencyRepository;
import com.cmb.efx.repository.TransactionRepository;
import com.cmb.efx.util.AppUtil;
import com.cmb.efx.util.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.cmb.efx.util.Mapper.mapCurrencyToDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    private final TokenService tokenService;

    private final RateService rateService;

    private final TransferService transferService;

    private final TransactionRepository transactionRepository;

    private final VolumeService volumeService;

    @Value("${USD-GL}")
    private String usdGlAccount;

    @Value("${GBP-GL}")
    private String gbpGlAccount;

    @Value("${EUR-GL}")
    private String eurGlAccount;

    @Value("${NGN-GL}")
    private String ngnGlAccount;

private static final String INVALID_TOKEN = "Invalid token";


    public Response addCurrency(CurrencyRequest request, String token){
        if (tokenService.validateToken(token)){
            return Response.builder()
                    .responseMessage(INVALID_TOKEN)
                    .build();
        }
        Optional<Currency> currency = currencyRepository.findByCurrencyName(request.getCurrencyName());
        if (currency.isPresent()){
            return Response.builder()
                    .responseMessage("Attempt to save duplicate currency")
                    .build();
        }


        Currency newCurrency = new Currency();
        newCurrency.setCurrencyName(request.getCurrencyName());
        newCurrency.setCurrencySymbol(request.getCurrencySymbol());
        newCurrency.setCountry(request.getCountry());

        Currency savedCurrency = currencyRepository.save(newCurrency);
        return Response.builder()
                .responseMessage("Success")
                .currency(mapCurrencyToDto(savedCurrency))
                .build();
    }

    public Response fetchAllCurrencies(String token){
        if (tokenService.validateToken(token)){
            return Response.builder()
                    .responseMessage("Invalid Token")
                    .build();
        }
        List<CurrencyRequest> currencies = currencyRepository.findAll()
                .stream().map(Mapper::mapCurrencyToDto).toList();
        return Response.builder()
                .responseMessage("Success")
                .currencyList(currencies)
                .build();

    }

    public Response doConversion(ConversionRequest request, String token) throws Exception {
        if (tokenService.validateToken(token)){
            throw new InvalidTokenException(INVALID_TOKEN);
        }

        System.out.println("User attempting conversion is: " + tokenService.getUsername(token));
        System.out.println("Attempt started at: " + LocalDateTime.now());

        if(volumeService.isThereValidVolume(request.getFromCurrency())){
            log.info("available volume: {}", volumeService.isThereValidVolume(request.getFromCurrency()));
            throw new CustomException("There's no valid volume for this currency", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Response volumeResponse = volumeService.fetchCurrentVolumeLimit(token);

        VolumeLimitResponse volumeLimitResponse = volumeResponse.getVolumeLimitList().stream()
                .filter(limit -> limit.getCurrencyType().equalsIgnoreCase(request.getFromCurrency()))
                .findFirst() // Get the first (latest) approved record for that currency
                .orElse(null);

        if (volumeLimitResponse != null && Double.parseDouble(request.getAmountToConvert()) > volumeLimitResponse.getUnused()){
            throw new CustomException("Insufficient volume for transaction. Please contact Admin", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String glAccount = switch (request.getFromCurrency().toUpperCase()) {
            case "USD" -> usdGlAccount;
            case "GBP" -> gbpGlAccount;
            case "EUR" -> eurGlAccount;
            default -> null;
        };

        Rates rates = rateService.currentRate(token).getRate();
        if (rates == null){
            throw new CustomException("Rate is currently not set up. Please contact admin", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        //check balance of sending account
        double rate = switch (request.getFromCurrency().toUpperCase()){
            case "USD" -> rateService.currentRate(token).getRate().getUsdBid();
            case "GBP" -> rateService.currentRate(token).getRate().getGbpBid();
            case "EUR" -> rateService.currentRate(token).getRate().getEurBid();
            default -> 1;
        };

        //confirm accounts are coronation accounts,
        //check balance of sending account
        //credit dollar account and debit one
        Records debitingForeignAccount = Records.builder()
                .creditDebitFlg("D")
                .acctId(AccountId.builder()
                        .acctId(request.getFromAccount())
                        .build())
                .trnAmt(TrnAmt.builder()
                        .amountValue(request.getAmountToConvert())
                        .currencyCode(request.getFromCurrency())
                        .build())
                .serialNum(1)
                .trnParticulars("Local Transaction")
                .tranRemarks(request.getFromCurrency() + " to naira conversion")
                .build();
        Records creditingGlAccount = Records.builder()
                .creditDebitFlg("C")
                .acctId(AccountId.builder()
                        .acctId(glAccount)
                        .build())
                .trnAmt(TrnAmt.builder()
                        .amountValue(request.getAmountToConvert())
                        .currencyCode(request.getFromCurrency())
                        .build())
                .serialNum(2)
                .trnParticulars("Local Transaction")
                .tranRemarks(request.getFromCurrency() + " to naira conversion")

                .build();

        System.out.println("Transaction done by: " + tokenService.getUsername(token));
        System.out.println("Foreign currency debit done at" + LocalDateTime.now());

        List<Records> recs = new ArrayList<>();
        recs.add(debitingForeignAccount);
        recs.add(creditingGlAccount);
        String reqUuid = AppUtil.requestUUid();
        log.info("first reqUuid: {}", reqUuid);
        TransferRequest transferRequest = TransferRequest.builder()
                .recs(recs)
                .reqUuid(reqUuid)
                .build();
        TransferResponse response = transferService.intraBankTransfer(transferRequest);
        if (!response.getResponseDescription().equalsIgnoreCase("SUCCESS")){
            throw new CustomException("Foreign currency Transfer failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (response.getResponseDescription().equalsIgnoreCase("SUCCESS")){
            for (Records rec : recs) {
                transactionRepository.save(Transactions.builder()
                        .tranId(reqUuid)
                        .currency(request.getFromCurrency())
                        .narration(request.getFromCurrency() + " to naira conversion")
                        .transactionType(rec.getCreditDebitFlg())
                        .amount(Double.parseDouble(request.getAmountToConvert()))
                        .accountNumber(rec.getAcctId().getAcctId())
                        .build());
            }

        } else {
            return Response.builder()
                    .responseMessage("Conversion failed at step 1")
                    .build();
        }
         double convertedAmount = Double.parseDouble(request.getAmountToConvert()) * rate;


        //credit naira account;
        Records debitingNgnGlAccount = Records.builder()
                .creditDebitFlg("D")
                .acctId(AccountId.builder()
                        .acctId(ngnGlAccount)
                        .build())
                .trnAmt(TrnAmt.builder()
                        .amountValue(String.valueOf(convertedAmount))
                        .currencyCode("NGN")
                        .build())
                .serialNum(1)
                .trnParticulars("Local Transfer")
                .tranRemarks("testing")
                .build();
        Records creditingNgnAccount = Records.builder()
                .creditDebitFlg("C")
                .acctId(AccountId.builder()
                        .acctId(request.getToAccount())
                        .build())
                .trnAmt(TrnAmt.builder()
                        .amountValue(String.valueOf(convertedAmount))
                        .currencyCode("NGN")
                        .build())
                .serialNum(2)
                .trnParticulars("Local Transfer")
                .tranRemarks("testing")
                .build();

        List<Records> recs2 = new ArrayList<>();
        recs2.add(debitingNgnGlAccount);
        recs2.add(creditingNgnAccount);

        String reqNgnUuid = AppUtil.requestUUid();
        log.info("reqUuid: {}", reqNgnUuid);
        TransferRequest ngntransferRequest = TransferRequest.builder()
                .recs(recs2)
                .reqUuid(reqNgnUuid)
                .build();
        TransferResponse response2 = transferService.intraBankTransfer(ngntransferRequest);
        System.out.println("Naira conversion done at " + LocalDateTime.now());
        if (!Objects.equals(response2.getResponseDescription(), "SUCCESS")){
            throw new CustomException("Naira Transfer failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (response2.getResponseDescription().equalsIgnoreCase("SUCCESS")){
            for (Records rec : recs2) {
                transactionRepository.save(Transactions.builder()
                        .tranId(reqNgnUuid)
                        .currency(request.getFromCurrency() + " to NGN")
                        .narration(request.getFromCurrency() + " to naira conversion")
                        .transactionType(rec.getCreditDebitFlg())
                        .amount(Double.parseDouble(request.getAmountToConvert()))
                        .accountNumber(request.getFromAccount())

                        .build());
            }

        } else {
            return Response.builder()
                    .responseMessage("Conversion failed at step 2")
                    .build();
        }


        ConversionResponse conversionResponse = ConversionResponse.builder()
                .convertedFrom(request.getFromCurrency()+request.getFromAccount())
                .convertedTo("NGN"+request.getToAccount())
                .rate(rate)
                .amountConverted(request.getAmountToConvert())
                .conversionResult(String.valueOf(convertedAmount))
                .build();
        return Response.builder()
                .responseMessage("Success")
                .conversionResponse(conversionResponse)
                .build();
    }


}
