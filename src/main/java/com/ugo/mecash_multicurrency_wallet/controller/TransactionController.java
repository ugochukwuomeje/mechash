package com.ugo.mecash_multicurrency_wallet.controller;

import com.ugo.mecash_multicurrency_wallet.dto.request.WalletRequest;
import com.ugo.mecash_multicurrency_wallet.dto.response.TransactionResponse;
import com.ugo.mecash_multicurrency_wallet.enums.ResponseMessage;
import com.ugo.mecash_multicurrency_wallet.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;
    @GetMapping("/{userId}/{currencyCode}")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
            @ModelAttribute WalletRequest walletRequest,
            @RequestParam int pageNumber,
            @RequestParam int pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Authentication authentication) {

        List<TransactionResponse> transactionResponses = transactionService.getTransactionHistory(
                walletRequest, pageNumber, pageSize, startDate, endDate, authentication);

        if (transactionResponses.isEmpty() || (transactionResponses.size() == 1 &&
                ResponseMessage.PAGE_NUMBER_OR_PAGE_SIZE_CANNOT_BE_LESS_THAN_1.getStatusCode()
                        .equals(transactionResponses.get(0).getResponseMessage()))) {
            return ResponseEntity.badRequest().body(transactionResponses);
        }

        return ResponseEntity.ok(transactionResponses);
    }

}

