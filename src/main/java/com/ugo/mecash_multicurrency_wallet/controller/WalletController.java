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
@RequestMapping("/api")
public class WalletController {
    @Autowired
    private WalletService walletService;
    @PostMapping("/wallets/deposit")
    public ResponseEntity<WalletResponse> deposit(@RequestBody WalletRequest walletRequest, Authentication authentication) {
        WalletResponse walletResponse = walletService.depositMoney(walletRequest, authentication);
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
    public ResponseEntity<WalletResponse> getBalance(
            @RequestBody WalletRequest walletRequest, Authentication authentication) {
        WalletResponse walletResponse = walletService.getBalance(walletRequest, authentication);
        return ResponseEntity.ok(walletResponse);
    }
}

