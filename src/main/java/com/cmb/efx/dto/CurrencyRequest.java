package com.cmb.efx.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrencyRequest {
    @NotNull(message = "Currency Name cannot be null")
    @Min(message = "Currency Name should be at least 3 characters", value = 3)
    private String currencyName;
    @Size(message = "Currency Symbol must be 3 characters", min = 3, max=3)
    private String currencySymbol;
    @NotNull(message = "Country should not be null")
    private String country;
}
