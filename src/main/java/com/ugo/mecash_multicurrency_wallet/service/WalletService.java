package com.ugo.mecash_multicurrency_wallet.service;

import com.ugo.mecash_multicurrency_wallet.dto.request.WalletRequest;
import com.ugo.mecash_multicurrency_wallet.dto.response.WalletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

public interface WalletService {
    WalletResponse depositMoney(WalletRequest walletRequest);
    WalletResponse withdrawMoney(WalletRequest walletRequest);
    WalletResponse transferMoney(WalletRequest walletRequest);
    WalletResponse getBalance(WalletRequest walletRequest, Authentication authentication);
}

