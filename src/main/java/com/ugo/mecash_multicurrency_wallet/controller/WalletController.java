package com.ugo.mecash_multicurrency_wallet.controller;

import com.ugo.mecash_multicurrency_wallet.dto.request.WalletRequest;
import com.ugo.mecash_multicurrency_wallet.dto.response.BalanceResDto;
import com.ugo.mecash_multicurrency_wallet.dto.response.WalletResponse;
import com.ugo.mecash_multicurrency_wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
@Slf4j
public class WalletController {
    @Autowired
    private WalletService walletService;
    @PostMapping("/wallets/deposit")
    public ResponseEntity<WalletResponse> deposit( @RequestBody WalletRequest walletRequest, Authentication authentication) {
        log.info("======================calling deposit money controller method");
        WalletResponse walletResponse = walletService.depositMoney(walletRequest);
        return ResponseEntity.ok(walletResponse);
    }

    @PostMapping("/wallets/withdraw")
    public ResponseEntity<WalletResponse> withdraw(@RequestBody WalletRequest walletRequest, Authentication authentication) {
        WalletResponse walletResponse = walletService.withdrawMoney(walletRequest, authentication);
        return ResponseEntity.ok(walletResponse);
    }

    @PostMapping("/wallets/transfer")
    public ResponseEntity<WalletResponse> transfer(@RequestBody WalletRequest walletRequest, Authentication authentication) {
        WalletResponse walletResponse = walletService.transferMoney(walletRequest, authentication);
        return ResponseEntity.ok(walletResponse);
    }

    @PostMapping("/wallets/balance")
    public ResponseEntity<BalanceResDto> getBalance(
            @RequestBody WalletRequest walletRequest, Authentication authentication) {
        BalanceResDto balanceResDto = walletService.getBalance(walletRequest, authentication);
        return ResponseEntity.ok(balanceResDto);
    }
}

