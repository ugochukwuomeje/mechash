package com.ugo.mecash_multicurrency_wallet.dto.response;

import com.ugo.mecash_multicurrency_wallet.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private User user;
    private Long userId;
    private String currencyCode;
    private BigDecimal balance;
    private Long recipientWalletId;
    private String transactionReference;
    private String responseMessage;
    private int responseCode;
}

