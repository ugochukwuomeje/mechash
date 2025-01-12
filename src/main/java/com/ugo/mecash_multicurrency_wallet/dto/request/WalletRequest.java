package com.ugo.mecash_multicurrency_wallet.dto.request;

import lombok.Data;

import java.math.BigDecimal;
@Data

public class WalletRequest {
    private Long userId;
    private String currencyCode;
    private BigDecimal amount;
    private Long recipientWalletId;
    private boolean externalDeposit;

}
