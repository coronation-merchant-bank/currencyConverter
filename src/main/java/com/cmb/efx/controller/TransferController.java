package com.cmb.efx.controller;

import com.cmb.efx.dto.AccountEnquiryRequest;
import com.cmb.efx.dto.Response;
import com.cmb.efx.dto.TransferRequest;
import com.cmb.efx.entity.Transactions;
import com.cmb.efx.service.TransferService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("transfer")
@AllArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {})
public class TransferController {
    private TransferService transferService;

    @PostMapping
    public ResponseEntity<Response> doTransfer(@RequestBody TransferRequest transferRequest,
                                               @RequestHeader("Authorization") String token) throws Exception {
        Response response = transferService.doTransfer(transferRequest, token);
        return ResponseEntity.ofNullable(response);
    }

    @PostMapping("accountEnquiry")
    public ResponseEntity<Response> doAccountEnquiry(@RequestBody AccountEnquiryRequest request, @RequestHeader("Authorization") String token){
        Response response = transferService.accountEnquiry(request, token);
        return ResponseEntity.ofNullable(response);
    }

    @GetMapping("transactionList")
    public ResponseEntity<List<Transactions>> transactionList(@RequestHeader("Authorization") String token){
        return ResponseEntity.ok(transferService.transactionList(token));
    }
}
