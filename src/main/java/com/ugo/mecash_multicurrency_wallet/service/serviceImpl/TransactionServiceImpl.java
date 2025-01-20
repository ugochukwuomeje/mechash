package com.ugo.mecash_multicurrency_wallet.service.serviceImpl;

import com.ugo.mecash_multicurrency_wallet.dto.request.WalletRequest;
import com.ugo.mecash_multicurrency_wallet.dto.response.TransactionResponse;
import com.ugo.mecash_multicurrency_wallet.dto.response.WalletResponse;
import com.ugo.mecash_multicurrency_wallet.entity.Transaction;
import com.ugo.mecash_multicurrency_wallet.entity.User;
import com.ugo.mecash_multicurrency_wallet.entity.Wallet;
import com.ugo.mecash_multicurrency_wallet.enums.ResponseMessage;
import com.ugo.mecash_multicurrency_wallet.repository.TransactionRepository;
import com.ugo.mecash_multicurrency_wallet.repository.UserRepository;
import com.ugo.mecash_multicurrency_wallet.repository.WalletRepository;
import com.ugo.mecash_multicurrency_wallet.service.TransactionService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    @Autowired
    private UserRepository userRepository;

    WalletResponse walletResponse = new WalletResponse();
    TransactionResponse transactionResponse = new TransactionResponse();

    @Override
    public Page<Transaction> getTransactionHistory(String userEmail, int pageNumber,
                                                           int pageSize, String startDate,
                                                           String endDate, Authentication authentication) {
        List<TransactionResponse> transactionResponses = new ArrayList<>();

        Page<Transaction> transactions = null;

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.DESC, "transactionDate"));

        String authenticationEmail = authentication.getName();
        User user = userRepository.findByUserUAndUsername(authenticationEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));


        //////////////////////////////////// Validate pagination parameters
        if (pageNumber < 1 || pageSize < 1) {
            log.error("Invalid pagination parameters: pageNumber = {}, pageSize = {}", pageNumber, pageSize);
            Transaction errorResponse = new Transaction();
            errorResponse.setResponse_message(ResponseMessage.PAGE_NUMBER_OR_PAGE_SIZE_CANNOT_BE_LESS_THAN_1.getStatusCode());
            return new PageImpl<>(List.of(errorResponse),pageable,0);
        }

        ////////////////////////////////// ensure that the authenticate user is the owner of the account
        if(!userEmail.equalsIgnoreCase(authenticationEmail)){

            Transaction transactionResponse1 = new Transaction();
            transactionResponse1.setResponse_message(ResponseMessage.ACCESS_DENIED.toString().toString());
            transactionResponse1.setResponse_code(Integer.parseInt(ResponseMessage.ACCESS_DENIED.getStatusCode()));

            return new PageImpl<>(List.of(transactionResponse1),pageable,0);
        }

        //////////////////////////////////////////////// Fetch and map transactions
        try {

            transactions = transactionRepository.findByWalletIdAndDateRange(user, startDate, endDate, pageable);

            log.info("Transaction history retrieved successfully for userId: {}.", user.getUserName());

        } catch (IllegalArgumentException | SecurityException ex) {
            log.error("Error occurred while fetching transaction history: {}", ex.getMessage());
            Transaction errorResponse = new Transaction();
            errorResponse.setResponse_message(ResponseMessage.ERROR_FETCHING_TRANSACTION_HISTORY.toString());
            errorResponse.setResponse_code(Integer.parseInt(ResponseMessage.ERROR_FETCHING_TRANSACTION_HISTORY.getStatusCode()));
            return new PageImpl<>(List.of(errorResponse),pageable,0);
        } catch (Exception ex) {
            log.error("Unexpected error occurred: ", ex);
            Transaction errorResponse = new Transaction();
            errorResponse.setResponse_message(ResponseMessage.INTERNAL_SERVER_ERROR.getStatusCode());
            errorResponse.setResponse_code(Integer.parseInt(ResponseMessage.INTERNAL_SERVER_ERROR.getStatusCode()));
            return new PageImpl<>(List.of(errorResponse),pageable,0);
        }

        return transactions;
    }

}
