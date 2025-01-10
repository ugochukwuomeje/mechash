package com.ugo.mecash_multicurrency_wallet.controller;

import com.ugo.mecash_multicurrency_wallet.dto.request.WalletRequest;
import com.ugo.mecash_multicurrency_wallet.dto.response.WalletResponse;
import com.ugo.mecash_multicurrency_wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {
    @Autowired
    private WalletService walletService;
    @PostMapping("/deposit/{userId}/{currencyCode}")
    public ResponseEntity<WalletResponse> deposit(@RequestBody WalletRequest walletRequest) {
        WalletResponse walletResponse = walletService.depositMoney(walletRequest);
        return ResponseEntity.ok(walletResponse);
    }

    @PostMapping("/withdraw/{userId}/{currencyCode}")
    public ResponseEntity<WalletResponse> withdraw(@RequestBody WalletRequest walletRequest) {
        WalletResponse walletResponse = walletService.withdrawMoney(walletRequest);
        return ResponseEntity.ok(walletResponse);
    }

    @PostMapping("/transfer/{userId}/{currencyCode}")
    public ResponseEntity<WalletResponse> transfer(@RequestBody WalletRequest walletRequest) {
        WalletResponse walletResponse = walletService.transferMoney(walletRequest);
        return ResponseEntity.ok(walletResponse);
    }

    @GetMapping("/balance/{userId}/{currencyCode}")
    public ResponseEntity<WalletResponse> getBalance(
            @ModelAttribute WalletRequest walletRequest, Authentication authentication) {
        WalletResponse walletResponse = walletService.getBalance(walletRequest, authentication);
        return ResponseEntity.ok(walletResponse);
    }
}

