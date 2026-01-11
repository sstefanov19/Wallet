package org.example.digitalwallet.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DepositRequest {
    private BigDecimal depositAmount;
}
