package com.ugo.mecash_multicurrency_wallet.service;

import com.ugo.mecash_multicurrency_wallet.dto.request.WalletRequest;
import com.ugo.mecash_multicurrency_wallet.dto.response.WalletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

public interface WalletService {
    WalletResponse depositMoney(WalletRequest walletRequest, Authentication authentication);
    WalletResponse withdrawMoney(WalletRequest walletRequest, Authentication authentication);
    WalletResponse transferMoney(WalletRequest walletRequest, Authentication authentication);
    WalletResponse getBalance(WalletRequest walletRequest, Authentication authentication);
}

