package com.ugo.mecash_multicurrency_wallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletDepositRequest {

    @NotBlank(message = "send currency cannot be blank")
    private String receive_currency_code;
    @NotBlank(message = "send currency cannot be blank")
    private String send_country_code;
}
