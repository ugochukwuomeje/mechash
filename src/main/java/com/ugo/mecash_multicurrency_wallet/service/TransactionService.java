package com.ugo.mecash_multicurrency_wallet.service;

import com.ugo.mecash_multicurrency_wallet.dto.request.WalletRequest;
import com.ugo.mecash_multicurrency_wallet.dto.response.TransactionResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TransactionService {
    List<TransactionResponse> getTransactionHistory(WalletRequest walletRequest, int pageNumber,
                                                    int pageSize, String startDate,
                                                    String endDate, Authentication authentication);
}

