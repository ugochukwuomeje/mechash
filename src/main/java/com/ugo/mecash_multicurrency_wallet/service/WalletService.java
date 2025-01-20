package com.ugo.mecash_multicurrency_wallet.service;

import com.ugo.mecash_multicurrency_wallet.dto.request.WalletRequest;
import com.ugo.mecash_multicurrency_wallet.dto.response.BalanceResDto;
import com.ugo.mecash_multicurrency_wallet.dto.response.WalletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface WalletService {
    WalletResponse depositMoney(WalletRequest walletRequest);
    WalletResponse withdrawMoney(WalletRequest walletRequest, Authentication authentication);
    WalletResponse transferMoney(WalletRequest walletRequest, Authentication authentication);
    BalanceResDto getBalance(WalletRequest walletRequest, Authentication authentication);
}

