package com.cmb.efx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Data {
    private Long id;
    private String email;
    private String fullname;
    private String department;
    private String phone;
    private String lastLoggedOn;
    private String role;
    private Object[] permissions;
}
