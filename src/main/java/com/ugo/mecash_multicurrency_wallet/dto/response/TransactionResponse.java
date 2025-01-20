package com.ugo.mecash_multicurrency_wallet.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ugo.mecash_multicurrency_wallet.entity.Wallet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {
    private String type;
    private String currencyCode;
    private Double amount;
    private LocalDateTime transactionDate;
    private String ResponseMessage;
    private String responseCode;
    private String TransactionReference;
    private Wallet wallet;

    public TransactionResponse(Wallet wallet, LocalDateTime transactionDate, BigDecimal amount, String type, String transactionReference, String currencyCode) {
    }
}

