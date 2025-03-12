package com.cmb.efx.controller;

import com.cmb.efx.dto.*;
import com.cmb.efx.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@AllArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    private AuthService authService;

    @PostMapping("login")
    public ResponseEntity<Response> login(@RequestBody LdapLoginRequest request, @RequestHeader(required = false) String entity) throws Exception {

        Response response = authService.ldapLogin(request, entity);
        return ResponseEntity.ofNullable(response);
    }

    @PostMapping("getStaffRole")
    public ResponseEntity<RoleResponse> getStaffRole(@RequestBody RoleRequest request){
        RoleResponse response = authService.getStaffRole(request);
        return ResponseEntity.ofNullable(response);
    }
}
