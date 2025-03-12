package com.cmb.efx.controller;

import com.cmb.efx.dto.ApproveVolumeRequest;
import com.cmb.efx.dto.CreateVolumeLimitRequest;
import com.cmb.efx.dto.Response;
import com.cmb.efx.service.VolumeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("volume")
@AllArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {})
public class VolumeController {

    private VolumeService volumeService;

    @PostMapping
    public ResponseEntity<Response> createVolumeLimit(@RequestBody CreateVolumeLimitRequest request,
                                                      @RequestHeader("Authorization") String token){
        Response response = volumeService.saveVolumeLimit(request, token);
        return ResponseEntity.ofNullable(response);
    }

    @GetMapping("/fetchAllVolumes")
    public ResponseEntity<Response> fetchVolumes(@RequestParam(required = false, defaultValue = "1") int pageNumber,
                                                 @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                 @RequestParam(required = false) String fromDate,
                                                 @RequestParam(required = false) String toDate,
                                                 @RequestParam(required = false) String currencyType,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(required = false) String searchQuery,
                                                 @RequestHeader("Authorization") String token) {

        Response response = volumeService.fetchVolume(pageNumber, pageSize, fromDate, toDate, currencyType, status, searchQuery, token);
        return ResponseEntity.ok(response);
    }
    @GetMapping("fetchCurrentVolumes")
    public ResponseEntity<Response> fetchCurrentVolume(@RequestHeader("Authorization") String token){
        Response response = volumeService.fetchCurrentVolumeLimit(token);
        return ResponseEntity.ofNullable(response);
    }

    @PostMapping("approveVolume")
    public ResponseEntity<Response> approveVolume(@RequestBody ApproveVolumeRequest request, @RequestHeader("Authorization") String token){
        Response response = volumeService.approveVolume(request, token);
        return ResponseEntity.ofNullable(response);
    }

    @PostMapping("rejectVolume")
    public ResponseEntity<Response> rejectVolume(@RequestBody ApproveVolumeRequest request, @RequestHeader("Authorization") String token){
        Response response = volumeService.rejectVolume(request, token);
        return ResponseEntity.ofNullable(response);
    }
}
