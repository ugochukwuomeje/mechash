package com.ugo.mecash_multicurrency_wallet.dto.response;

import com.ugo.mecash_multicurrency_wallet.entity.Wallet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private String type;
    private String currencyCode;
    private Double amount;
    private LocalDateTime transactionDate;
    private String ResponseMessage;
    private String TransactionReference;
    private Wallet wallet;

    public TransactionResponse(Wallet wallet, LocalDateTime transactionDate, BigDecimal amount, String type, String transactionReference, String currencyCode) {
    }
}

