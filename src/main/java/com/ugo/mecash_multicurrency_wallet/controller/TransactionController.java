package com.ugo.mecash_multicurrency_wallet.controller;

import com.ugo.mecash_multicurrency_wallet.dto.request.WalletRequest;
import com.ugo.mecash_multicurrency_wallet.dto.response.TransactionResponse;
import com.ugo.mecash_multicurrency_wallet.entity.Transaction;
import com.ugo.mecash_multicurrency_wallet.enums.ResponseMessage;
import com.ugo.mecash_multicurrency_wallet.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;
    @GetMapping("/history")
    public ResponseEntity<Page<Transaction>> getTransactionHistory(
            @ModelAttribute String  email,
            @RequestParam int pageNumber,
            @RequestParam int pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Authentication authentication) {

        Page<Transaction> transactionResponses = transactionService.getTransactionHistory(
                 email, pageNumber, pageSize, startDate, endDate, authentication);

        return ResponseEntity.ok(transactionResponses);
    }

}

