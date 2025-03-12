package com.cmb.efx.controller;

import com.cmb.efx.service.CurrencyService;
import com.cmb.efx.dto.ConversionRequest;
import com.cmb.efx.dto.CurrencyRequest;
import com.cmb.efx.dto.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/currency")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CurrencyController {
    @Autowired
    private CurrencyService currencyService;



    @PostMapping
    public ResponseEntity<Response> addCurrency(@RequestBody CurrencyRequest request, @RequestHeader("Authorization") String token){
        if (token.isEmpty()){
            Response response = Response.builder().responseMessage("token cannot be null").build();
            return ResponseEntity.ofNullable(response);
        }
        Response response = currencyService.addCurrency(request, token);
        return ResponseEntity.ofNullable(response);
    }

    @GetMapping
    public ResponseEntity<Response> fetchAllCurrencies(@RequestHeader("Authorization") String token){
        if (token.isEmpty()){
            Response response = Response.builder().responseMessage("token cannot be null").build();
            return ResponseEntity.ofNullable(response);
        }
        Response response = currencyService.fetchAllCurrencies(token);
        return ResponseEntity.ofNullable(response);
    }

    @PostMapping("doConversion")
    public ResponseEntity<Response> doConversion(@RequestBody ConversionRequest request,
                                                 @RequestHeader("Authorization") String token) throws Exception {
        if (token.isEmpty()){
            Response response = Response.builder().responseMessage("token cannot be null").build();
            return ResponseEntity.ofNullable(response);
        }
        Response response = currencyService.doConversion(request, token);
        return ResponseEntity.ofNullable(response);
    }
}
