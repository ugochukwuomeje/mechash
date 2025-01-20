package com.ugo.mecash_multicurrency_wallet.service;

import com.ugo.mecash_multicurrency_wallet.dto.request.WalletRequest;
import com.ugo.mecash_multicurrency_wallet.dto.response.TransactionResponse;
import com.ugo.mecash_multicurrency_wallet.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TransactionService {
    Page<Transaction> getTransactionHistory(String email, int pageNumber,
                                            int pageSize, String startDate,
                                            String endDate, Authentication authentication);
}

