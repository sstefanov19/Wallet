package org.example.digitalwallet.dto;


import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DepositRequest {

    @Positive(message = "Value must be positive")
    @DecimalMax(value = "100000" ,message = "Deposit limit exceeded")
    @NotBlank(message = "Value cannot be blank")
    private BigDecimal depositAmount;
}
