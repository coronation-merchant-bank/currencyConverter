package com.cmb.efx.controller;

import com.cmb.efx.dto.ApproveRateRequest;
import com.cmb.efx.service.RateService;
import com.cmb.efx.dto.CreateRateRequest;
import com.cmb.efx.dto.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("rates")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RateController {

    @Autowired
    private RateService rateService;

    @PostMapping("createRate")
    public ResponseEntity<Response> createRate(@RequestBody CreateRateRequest request, @RequestHeader("Authorization") String token){
        Response response = rateService.createRate(request, token);
        return ResponseEntity.ofNullable(response);
    }



    @GetMapping("fetchRate/{id}")
    public ResponseEntity<Response> fetchById(@PathVariable Long id, @RequestHeader("Authorization") String token){
        Response response = rateService.fetchRateById(id, token);
        return ResponseEntity.ofNullable(response);
    }

    @PutMapping("updateRate/{id}")
    public ResponseEntity<Response> updateRate(@RequestBody CreateRateRequest request, @PathVariable Long id, @RequestHeader("Authorization") String token){
        Response response = rateService.updateRate(request, id, token);
        return ResponseEntity.ofNullable(response);
    }

    @GetMapping
    public ResponseEntity<Response> fetchRates(@RequestParam int pageNumber,
                                               @RequestParam int pageSize,
                                               @RequestParam(required = false) String fromDate,
                                               @RequestParam(required = false) String toDate,
                                               @RequestHeader("Authorization") String token){
        Response response = rateService.fetchRates(pageNumber, pageSize, fromDate, toDate, token);
        return ResponseEntity.ofNullable(response);
    }

    @GetMapping("currentRate")
    public ResponseEntity<Response> currentRate(@RequestHeader("Authorization") String token){
        Response response = rateService.currentRate(token);
        return ResponseEntity.ofNullable(response);
    }

    @PostMapping("approveRate")
    public ResponseEntity<Response> approveRate(@RequestBody ApproveRateRequest request, @RequestHeader("Authorization") String token){
        Response response = rateService.approveRate(request, token);
        return ResponseEntity.ofNullable(response);
    }

    @PostMapping("rejectRate")
    public ResponseEntity<Response> rejectRate(@RequestBody ApproveRateRequest request, @RequestHeader("Authorization") String token){
        Response response = rateService.rejectRate(request, token);
        return ResponseEntity.ofNullable(response);
    }

}
