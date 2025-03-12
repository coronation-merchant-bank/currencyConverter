package com.cmb.efx.service;

import com.cmb.efx.dto.*;
import com.cmb.efx.entity.Transactions;
import com.cmb.efx.repository.TransactionRepository;
import com.cmb.efx.util.AppUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;


@Service
@Slf4j
@RequiredArgsConstructor
public class TransferService {

    private final TransactionRepository transactionRepository;

    private final TokenService tokenService;

    @Value("${intra-bank-transfer-url}")
    private String transferUrl;

    public TransferResponse intraBankTransfer(TransferRequest request){
        RestTemplate restTemplate = new RestTemplate();
        String url = transferUrl + "fiFundTransfer";
        HttpEntity<TransferRequest> entity = new HttpEntity<>(request);
        ResponseEntity<TransferResponse> responseEntity = restTemplate
                .exchange(url, HttpMethod.POST, entity, TransferResponse.class);
        log.info("transfer response: {}", responseEntity);
        return responseEntity.getBody();

    }



    public Response doTransfer(TransferRequest request, String token) throws Exception {
        if (tokenService.validateToken(token)){
            return Response.builder()
                    .responseMessage("Invalid Token")
                    .build();
        }
        String reqUuid = AppUtil.requestUUid();

        log.info("id: {}", reqUuid);
        TransferRequest transferRequest = TransferRequest.builder()
                .recs(request.getRecs())
                .reqUuid(reqUuid)
                .build();
        TransferResponse transferResponse = intraBankTransfer(transferRequest);

        try {

            if (transferResponse.getResponseDescription().equalsIgnoreCase("SUCCESS")){
                for (int i=0; i<transferRequest.getRecs().size(); i++){
                    String transactionType;
                    if (transferRequest.getRecs().get(i).getCreditDebitFlg().equalsIgnoreCase("D")){
                        transactionType = "DEBIT";
                    } else {
                        transactionType = "CREDIT";
                    }
                    Transactions transactions = Transactions.builder()
                            .accountNumber(transferRequest.getRecs().get(i).getAcctId().getAcctId())
                            .amount(Double.parseDouble(transferRequest.getRecs().get(i).getTrnAmt().getAmountValue()))
                            .currency(transferRequest.getRecs().get(i).getTrnAmt().getCurrencyCode())
                            .transactionType(transactionType)
                            .narration(transferRequest.getRecs().get(i).getTranRemarks())
                            .tranId(transferResponse.getTranId())
                            .build();

                    transactionRepository.save(transactions);



                }
                return Response.builder()
                        .responseMessage("Transfer Successful")
                        .transferResponse(transferResponse)
                        .build();

            } else {
                throw new Exception("API Error: " + transferResponse.getResponseDescription());
            }
        } catch (Exception e) {
            throw new Exception(transferResponse.getResponseDescription());
        }
    }

    public Response accountEnquiry(AccountEnquiryRequest request, String token){
        if (tokenService.validateToken(token)){
            return Response.builder()
                    .responseMessage("Invalid Token")
                    .build();
        }
        String url = transferUrl + "accountDetail";
        RestTemplate restTemplate = new RestTemplate();
        log.info("url: {}", url);
        HttpEntity<AccountEnquiryRequest> entity = new HttpEntity<>(request);
        ResponseEntity<AccountEnquiry> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, AccountEnquiry.class);
        if (!Objects.requireNonNull(responseEntity.getBody()).getResponseText().equalsIgnoreCase("success")){
            return Response.builder()
                    .responseMessage("Account does not exist")
                    .build();
        }

        return Response.builder()
                .responseMessage("Success")
                .accountDetails(responseEntity.getBody())
                .build();
    }

    public List<Transactions> transactionList(String token){

        return transactionRepository.findAll();
    }
}
