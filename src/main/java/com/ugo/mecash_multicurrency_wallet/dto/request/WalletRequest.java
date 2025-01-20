package com.ugo.mecash_multicurrency_wallet.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
@Data

public class WalletRequest {
    private Long userId;
    @NotBlank(message = "send currency cannot be blank")
    private String send_currency_code;
    @NotBlank(message = "send currency cannot be blank")
    private String receive_currency_code;
    @NotBlank(message = "send currency cannot be blank")
    private String send_country_code;
    @NotBlank(message = "receive currency cannot be blank")
    private String receive_country_code;
    @NotBlank(message = "send issuer code cannot be blank")
    private String send_issuer_code;
    @NotBlank(message = "receive issuer code cannot be blank")
    private String receive_issuer_code;
    @NotBlank(message = "send currency cannot be blank")
    @Size(min = 1, message = "Amount cannot be less than 1")
    private BigDecimal send_amount;
    @Size(min = 1, message = "exchange cannot be less than 1")
    private BigDecimal exchange_rate;
    @Size(min = 1, message = "receive Amount cannot be less than 1")
    @NotBlank(message = "receive amount cannot be blank")
    private BigDecimal receive_amount;
    @NotBlank(message = "recipient wallet cannot be blank")
    private Long recipientWalletId;
    @NotBlank(message = "send wallet cannot be blank")
    private Long sender_walletId;
    @Size(min = 4,max = 4, message = "invalid transaction pin")
    private String pin;
    private String narration;
    private String description;
}
