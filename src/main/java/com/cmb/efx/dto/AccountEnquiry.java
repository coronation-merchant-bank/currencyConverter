package com.cmb.efx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountEnquiry {
    private String status;
    private double balance;
    private String restriction;
    private String cifId;
    private String accountSchmCode;
    private String misCode;
    private double effectiveBalance;
    private String phoneNumber;
    private String email;
    private String bvn;
    private String address;
    private String branchCode;
    private String bankId;
    private String systemOnlyAcctFlg;
    private String freezeCode;
    private String openDate;
    private String glSubHeadCode;
    private String schemeType;
    private String customerDateOfBirth;
    private String salutation;
    private String tier;
    private String loginId;
    private String tin;
    private String rcNumber;
    private String nin;
    private String riskRating;
    private String schemeTypeDesc;
    private String accountType;
    private String relationshipManager;
    private Double lienAmount;
    private String sbu;
    private String accountName;
    private String responseText;
    private String accountNumber;
    private String responseCode;
    private String accountCurrency;
    private String signatories;
}
