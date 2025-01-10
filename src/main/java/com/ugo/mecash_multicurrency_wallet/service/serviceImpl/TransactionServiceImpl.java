package com.ugo.mecash_multicurrency_wallet.service.serviceImpl;

import com.ugo.mecash_multicurrency_wallet.dto.request.WalletRequest;
import com.ugo.mecash_multicurrency_wallet.dto.response.TransactionResponse;
import com.ugo.mecash_multicurrency_wallet.dto.response.WalletResponse;
import com.ugo.mecash_multicurrency_wallet.entity.Transaction;
import com.ugo.mecash_multicurrency_wallet.entity.Wallet;
import com.ugo.mecash_multicurrency_wallet.enums.ResponseMessage;
import com.ugo.mecash_multicurrency_wallet.repository.TransactionRepository;
import com.ugo.mecash_multicurrency_wallet.repository.WalletRepository;
import com.ugo.mecash_multicurrency_wallet.service.TransactionService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    WalletResponse walletResponse = new WalletResponse();
    TransactionResponse transactionResponse = new TransactionResponse();

    @Override
    public List<TransactionResponse> getTransactionHistory(WalletRequest walletRequest, int pageNumber,
                                                           int pageSize, String startDate,
                                                           String endDate, Authentication authentication) {
        List<TransactionResponse> transactionResponses = new ArrayList<>();

        //////////////////////////////////// Validate pagination parameters
        if (pageNumber < 1 || pageSize < 1) {
            log.error("Invalid pagination parameters: pageNumber = {}, pageSize = {}", pageNumber, pageSize);
            TransactionResponse errorResponse = new TransactionResponse();
            errorResponse.setResponseMessage(ResponseMessage.PAGE_NUMBER_OR_PAGE_SIZE_CANNOT_BE_LESS_THAN_1.getStatusCode());
            transactionResponses.add(errorResponse);
            return transactionResponses;
        }

        //////////////////////////////////// Validate input parameters
        if (walletRequest.getUserId() == null || walletRequest.getCurrencyCode() == null) {
            log.error("Invalid input parameters: Wallet ID and Currency Code must be provided.");
            TransactionResponse errorResponse = new TransactionResponse();
            errorResponse.setResponseMessage(ResponseMessage.WALLET_ID_MUST_BE_PROVIDED.getStatusCode());
            transactionResponses.add(errorResponse);
            return transactionResponses;
        }

        /////////////////////////////////////////////// Fetch wallet
        Optional<Wallet> walletOptional = walletRepository.findByIdUserIdAndCurrencyCode(walletRequest.getUserId(), walletRequest.getCurrencyCode());
        if (walletOptional.isEmpty()) {
            log.error("Wallet not found for userId: {} and currencyCode: {}", walletRequest.getUserId(), walletRequest.getCurrencyCode());
            TransactionResponse errorResponse = new TransactionResponse();
            errorResponse.setResponseMessage(ResponseMessage.USER_WALLET_NOT_FOUND.getStatusCode());
            transactionResponses.add(errorResponse);
            return transactionResponses;
        }

        Wallet userWallet = walletOptional.get();

        //////////////////////////////////////////// Validate ownership or admin authorization
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));

        if (!userWallet.getUser().getUserName().equals(currentUsername) && !isAdmin) {
            log.error("Access denied: User {} attempted to view transactions for wallet userId: {}", currentUsername, walletRequest.getUserId());
            TransactionResponse errorResponse = new TransactionResponse();
            errorResponse.setResponseMessage(ResponseMessage.ACCESS_DENIED.getStatusCode());
            transactionResponses.add(errorResponse);
            return transactionResponses;
        }

        //////////////////////////////////////////////// Fetch and map transactions
        try {
            Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.DESC, "transactionDate"));
            Page<Transaction> transactions = transactionRepository.findByWalletIdAndDateRange(
                    walletRequest.getUserId(), startDate, endDate, pageable);

            transactionResponses = transactions.getContent().stream()
                    .map(transaction -> new TransactionResponse(
                            transaction.getWallet(),
                            transaction.getTransactionDate(),
                            transaction.getAmount(),
                            transaction.getType(),
                            transaction.getTransactionReference(),
                            transaction.getCurrencyCode()))
                    .collect(Collectors.toList());

            log.info("Transaction history retrieved successfully for userId: {}.", walletRequest.getUserId());

        } catch (IllegalArgumentException | SecurityException ex) {
            log.error("Error occurred while fetching transaction history: {}", ex.getMessage());
            TransactionResponse errorResponse = new TransactionResponse();
            errorResponse.setResponseMessage(ResponseMessage.ERROR_FETCHING_TRANSACTION_HISTORY.getStatusCode());
            transactionResponses.add(errorResponse);
        } catch (Exception ex) {
            log.error("Unexpected error occurred: ", ex);
            TransactionResponse errorResponse = new TransactionResponse();
            errorResponse.setResponseMessage(ResponseMessage.INTERNAL_SERVER_ERROR.getStatusCode());
            transactionResponses.add(errorResponse);
        }

        return transactionResponses;
    }

}
