package com.ugo.mecash_multicurrency_wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String userName;
    private String firstName;
    private String lastName;
    private String role;
    private List<WalletResponse> wallets;
    private String message;
}

