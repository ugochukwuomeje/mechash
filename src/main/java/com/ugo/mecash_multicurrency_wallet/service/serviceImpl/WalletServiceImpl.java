package com.ugo.mecash_multicurrency_wallet.service.serviceImpl;

import com.ugo.mecash_multicurrency_wallet.dto.request.WalletRequest;
import com.ugo.mecash_multicurrency_wallet.dto.response.WalletResponse;
import com.ugo.mecash_multicurrency_wallet.entity.Transaction;
import com.ugo.mecash_multicurrency_wallet.entity.User;
import com.ugo.mecash_multicurrency_wallet.entity.Wallet;
import com.ugo.mecash_multicurrency_wallet.enums.ResponseMessage;
import com.ugo.mecash_multicurrency_wallet.enums.TransactionType;
import com.ugo.mecash_multicurrency_wallet.repository.TransactionRepository;
import com.ugo.mecash_multicurrency_wallet.repository.UserRepository;
import com.ugo.mecash_multicurrency_wallet.repository.WalletRepository;
import com.ugo.mecash_multicurrency_wallet.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserRepository userRepository;
    WalletResponse walletResponse = new WalletResponse();
  //  @Override
//    public WalletResponse depositMoney(WalletRequest walletRequest) {
//        WalletResponse walletResponse = new WalletResponse();
//        try {
//            if (walletRequest.getUserId() == null || walletRequest.getCurrencyCode() == null ||
//                    walletRequest.getAmount() == null || walletRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
//                log.error("Invalid Input Parameter");
//                walletResponse.setResponseMessage(ResponseMessage.INVALID_INPUT_PARAMETER.getStatusCode());
//                return walletResponse;
//            }
//
//            Optional<Wallet> wallet = walletRepository.findByWalletId(walletRequest.getUserId());
//            if (wallet.isPresent()) {
//                ////////////////////////////////////////////// Process the wallet
//                Wallet existingWallet = wallet.get();
//                existingWallet.setBalance(existingWallet.getBalance().add(walletRequest.getAmount()));
//                walletRepository.save(existingWallet);
//
//                walletResponse.setResponseMessage(ResponseMessage.SUCCESS.getStatusCode());
//                walletResponse.setBalance(existingWallet.getBalance());
//            } else {
//                log.error("Wallet not found for userId: {}", walletRequest.getUserId());
//                walletResponse.setResponseMessage(ResponseMessage.WALLET_NOT_FOUND.getStatusCode());
//            }
//        } catch (Exception e) {
//            log.error("Error occurred while depositing money: ", e);
//            walletResponse.setResponseMessage(ResponseMessage.INTERNAL_ERROR.getStatusCode());
//        }
//        return walletResponse;
//    }
    @Override
    @Transactional
    public WalletResponse depositMoney(WalletRequest walletRequest, Authentication authentication) {
        WalletResponse walletResponse = new WalletResponse();

        try {
            ///////////////////////////////////////////// Validate input parameters
            if (walletRequest.getCurrencyCode() == null ||
                    walletRequest.getAmount() == null || walletRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Invalid input parameters");
                walletResponse.setResponseMessage(ResponseMessage.INVALID_INPUT_PARAMETER.getStatusCode());
                return walletResponse;
            }

            ///////////////////////////////////// Fetch the authenticated user from the Authentication object
            String userEmail = authentication.getName();
            log.info("*********************************Deposit method logged authenticated username/email*********************************" + userEmail);
            User user = userRepository.findByUserUAndUsername(userEmail)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            ///////////////////////////////////// Fetch the wallet with a pessimistic lock for the authenticated user
            Optional<Wallet> walletOptional = walletRepository.findByIdWithLock(user.getId(), walletRequest.getCurrencyCode());
            if (walletOptional.isEmpty()) {
                log.error("Wallet not found for userId: {}", user.getId());
                walletResponse.setResponseMessage(ResponseMessage.WALLET_NOT_FOUND.getStatusCode());
                return walletResponse;
            }

            Wallet wallet = walletOptional.get();

            ////////////////////////////////////////// Validate system constraints
            if (wallet.getMaxBalance() != null &&
                    wallet.getBalance().add(walletRequest.getAmount()).compareTo(wallet.getMaxBalance()) > 0) {
                log.error("Deposit exceeds maximum allowed balance for userId: {}", user.getId());
                walletResponse.setResponseMessage(ResponseMessage.MAX_BALANCE_EXCEEDED.getStatusCode());
                return walletResponse;
            }

            if (wallet.getMaxTransactionsPerDay() != null) {
                int dailyTransactionCount = walletRepository.countTransactionsForToday(wallet.getId()).intValue();
                if (dailyTransactionCount >= wallet.getMaxTransactionsPerDay()) {
                    log.error("Maximum transactions per day exceeded for userId: {}", user.getId());
                    walletResponse.setResponseMessage(ResponseMessage.MAX_TRANSACTIONS_EXCEEDED.getStatusCode());
                    return walletResponse;
                }
            }

            ///////////////////////////////// Generate transaction reference
            String transactionReference = UUID.randomUUID().toString();

            ///////////////////////////////// Check if the deposit is internal or external
            if (walletRequest.isExternalDeposit()) {
                // external wallet deposit
                wallet.setBalance(wallet.getBalance().add(walletRequest.getAmount()));
            } else {
                // Internal deposit
                wallet.setBalance(wallet.getBalance().add(walletRequest.getAmount()));
            }

            walletRepository.save(wallet);

            Transaction transaction = new Transaction();
            transaction.setTransactionReference(transactionReference);
            transaction.setWallet(wallet);
            transaction.setAmount(walletRequest.getAmount());
            transaction.setCurrencyCode(walletRequest.getCurrencyCode());
            transaction.setType("DEPOSIT");
            transaction.setTransactionDate(LocalDateTime.now());
            transactionRepository.save(transaction);

            walletResponse.setResponseMessage(ResponseMessage.SUCCESS.getStatusCode());
            walletResponse.setUserId(user.getId());
            walletResponse.setBalance(wallet.getBalance());
            walletResponse.setTransactionReference(transactionReference);
            walletResponse.setCurrencyCode(wallet.getCurrencyCode());
            walletResponse.setRecipientWalletId(walletRequest.getRecipientWalletId());
            walletResponse.setResponseMessage(ResponseMessage.SUCCESS.getStatusCode());
            walletResponse.setResponseCode(200);

        } catch (Exception e) {
            log.error("Error occurred while depositing money: ", e);
            walletResponse.setResponseMessage(ResponseMessage.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        return walletResponse;
    }





    @Override
    @Transactional
    public WalletResponse withdrawMoney(WalletRequest walletRequest, Authentication authentication) {
        WalletResponse walletResponse = new WalletResponse();

        try {
            ////////////////////////////////////////////////// Validate input parameters
            if (walletRequest.getCurrencyCode() == null ||
                    walletRequest.getAmount() == null || walletRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Invalid input parameters for withdrawal.");
                walletResponse.setResponseMessage(ResponseMessage.INVALID_INPUT_PARAMETER.getStatusCode());
                return walletResponse;
            }

            String userEmail = authentication.getName();
            log.info("*********************************Withdraw method logged authenticated username/email*********************************" + userEmail);
            User user = userRepository.findByUserUAndUsername(userEmail)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));


            //////////////////////////////////////////////////// Fetch the wallet with a pessimistic lock for the authenticated user
            Optional<Wallet> walletOptional = walletRepository.findByIdWithLock(user.getId(), walletRequest.getCurrencyCode());
            if (walletOptional.isEmpty()) {
                log.error("Wallet not found for userId: {}", user.getId());
                walletResponse.setResponseMessage(ResponseMessage.WALLET_NOT_FOUND.getStatusCode());
                return walletResponse;
            }

            Wallet wallet = walletOptional.get();

            ///////////////////////////////////////////////// Validate if the wallet is active
            if (!wallet.isActive()) {
                log.error("Wallet is not active for userId: {}", user.getId());
                walletResponse.setResponseMessage(ResponseMessage.WALLET_IS_NOT_ACTIVE.getStatusCode());
                return walletResponse;
            }

            /////////////////////////////////////// Validate if sufficient funds are available for the withdrawal
            if (wallet.getBalance().compareTo(walletRequest.getAmount()) < 0) {
                log.error("Insufficient funds in the wallet for userId: {}", user.getId());
                walletResponse.setResponseMessage(ResponseMessage.INSUFFICIENT_FUNDS.getStatusCode());
                return walletResponse;
            }

            ///////////////////////////////// Generate transaction reference
            String transactionReference = UUID.randomUUID().toString();

            //////////////////////////////// Perform the withdrawal by deducting the amount from the wallet balance
            wallet.setBalance(wallet.getBalance().subtract(walletRequest.getAmount()));

            /////////////////////////////// Create a new transaction for the withdrawal
            Transaction transaction = new Transaction();
            transaction.setTransactionReference(transactionReference);
            transaction.setWallet(wallet);
            transaction.setType(String.valueOf(TransactionType.WITHDRAWAL));
            transaction.setAmount(walletRequest.getAmount());
            transaction.setCurrencyCode(wallet.getCurrencyCode());
            transaction.setTransactionDate(LocalDateTime.now());

            ////////////////////////////////////// Save the transaction
            transactionRepository.save(transaction);

            ////////////////////////////////////// Save the updated wallet
            walletRepository.save(wallet);

            //////////////////////////////////////// Set successful response
            walletResponse.setResponseMessage(ResponseMessage.SUCCESS.getStatusCode());
            walletResponse.setBalance(wallet.getBalance());
            walletResponse.setTransactionReference(transactionReference);
            walletResponse.setUserId(user.getId());
            walletResponse.setCurrencyCode(walletRequest.getCurrencyCode());
            walletResponse.setResponseCode(200);

        } catch (Exception ex) {
            log.error("Error occurred while withdrawing money: ", ex);
            walletResponse.setResponseMessage(ResponseMessage.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        return walletResponse;
    }



    @Override
    @Transactional
    public WalletResponse transferMoney(WalletRequest walletRequest, Authentication authentication) {
        WalletResponse walletResponse = new WalletResponse();

        try {
            ////////////////////////////////// Validate input parameters
            if (walletRequest.getRecipientWalletId() == null ||
                    walletRequest.getCurrencyCode() == null || walletRequest.getAmount() == null ||
                    walletRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Invalid input parameters for transfer.");
                walletResponse.setResponseMessage(ResponseMessage.INVALID_INPUT_PARAMETER.getStatusCode());
                return walletResponse;
            }

            ////////////////////////////////// Fetch sender's user ID
            String senderEmail = authentication.getName();
            User sender = userRepository.findByUserUAndUsername(senderEmail)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Fetch sender's wallet with a pessimistic lock
            Optional<Wallet> senderWalletOptional = walletRepository.findByIdWithLock(sender.getId(), walletRequest.getCurrencyCode());
            if (senderWalletOptional.isEmpty()) {
                log.error("Sender's wallet not found for userId: {}", sender.getId());
                walletResponse.setResponseMessage(ResponseMessage.WALLET_NOT_FOUND.getStatusCode());
                return walletResponse;
            }

            Wallet senderWallet = senderWalletOptional.get();

            ///////////////////////////////////// Validate if sender's wallet is active
            if (!senderWallet.isActive()) {
                log.error("Sender's wallet is not active for userId: {}", sender.getId());
                walletResponse.setResponseMessage(ResponseMessage.WALLET_IS_NOT_ACTIVE.getStatusCode());
                return walletResponse;
            }

            //////////////////////////////////////// Validate if sufficient funds are available for the transfer + transaction fee
            BigDecimal transactionFee = new BigDecimal("0.02");
            BigDecimal totalAmountToDeduct = walletRequest.getAmount().add(walletRequest.getAmount().multiply(transactionFee));

            if (senderWallet.getBalance().compareTo(totalAmountToDeduct) < 0) {
                log.error("Insufficient funds in sender's wallet for userId: {}", sender.getId());
                walletResponse.setResponseMessage(ResponseMessage.INSUFFICIENT_FUNDS.getStatusCode());
                return walletResponse;
            }

            //////////////////////////////////////////// Fetch recipient's wallet with a pessimistic lock
            //////////////////////////////////////////// we can alternatively call an api with the recipient wallet details
            Optional<Wallet> recipientWalletOptional = walletRepository.findByIdWithLock(walletRequest.getRecipientWalletId(), walletRequest.getCurrencyCode());
            if (recipientWalletOptional.isEmpty()) {
                log.error("Recipient's wallet not found for walletId: {}", walletRequest.getRecipientWalletId());
                walletResponse.setResponseMessage(ResponseMessage.RECIPIENT_WALLET_NOT_FOUND.getStatusCode());
                return walletResponse;
            }

            Wallet recipientWallet = recipientWalletOptional.get();

            //////////////////////////////////////// Validate if recipient's wallet is active
            if (!recipientWallet.isActive()) {
                log.error("Recipient's wallet is not active for walletId: {}", walletRequest.getRecipientWalletId());
                walletResponse.setResponseMessage(ResponseMessage.WALLET_IS_NOT_ACTIVE.getStatusCode());
                return walletResponse;
            }

            /////////////////////////////////////// Generate transaction reference
            String transactionReference = UUID.randomUUID().toString();

            /////////////////////////////////////// Debit sender of transaction fee and sending amount
            senderWallet.setBalance(senderWallet.getBalance().subtract(totalAmountToDeduct));

            /////////////////////////////////////// Add transfer amount to recipient's wallet
            recipientWallet.setBalance(recipientWallet.getBalance().add(walletRequest.getAmount()));

            walletRepository.save(senderWallet);
            walletRepository.save(recipientWallet);

            Transaction transaction = new Transaction();
            transaction.setTransactionReference(transactionReference);
            transaction.setWallet(senderWallet);
            transaction.setRecipientWallet(recipientWallet);
            transaction.setAmount(walletRequest.getAmount());
            transaction.setCurrencyCode(walletRequest.getCurrencyCode());
            transaction.setType("TRANSFER");
            transaction.setTransactionDate(LocalDateTime.now());
            transactionRepository.save(transaction);

            walletResponse.setResponseMessage(ResponseMessage.SUCCESS.getStatusCode());
            walletResponse.setBalance(senderWallet.getBalance());
            walletResponse.setTransactionReference(transactionReference);
            walletResponse.setUserId(senderWallet.getId());
            walletResponse.setCurrencyCode(walletRequest.getCurrencyCode());
            walletResponse.setRecipientWalletId(recipientWallet.getId());
            walletResponse.setResponseCode(200);

        } catch (Exception ex) {
            log.error("Error occurred during money transfer: ", ex);
            walletResponse.setResponseMessage(ResponseMessage.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        return walletResponse;
    }




    @Override
    @Transactional
    public WalletResponse getBalance(WalletRequest walletRequest, Authentication authentication) {
        WalletResponse walletResponse = new WalletResponse();

        try {
            /////////////////////////////////// Validate input parameters
            if (walletRequest.getUserId() == null || walletRequest.getCurrencyCode() == null) {
                log.error("Invalid input parameters for balance retrieval.");
                walletResponse.setResponseMessage(ResponseMessage.INVALID_INPUT_PARAMETER.getStatusCode());
                return walletResponse;
            }

            ///////////////////////////////////////// Fetch user's wallet by userId and currency code
            Optional<Wallet> walletOptional = walletRepository.findByIdUserIdAndCurrencyCode(walletRequest.getUserId(), walletRequest.getCurrencyCode());
            if (walletOptional.isEmpty()) {
                log.error("Wallet not found for userId: {} and currencyCode: {}", walletRequest.getUserId(), walletRequest.getCurrencyCode());
                walletResponse.setResponseMessage(ResponseMessage.USER_WALLET_NOT_FOUND.getStatusCode());
                return walletResponse;
            }

            Wallet userWallet = walletOptional.get();

            //////////////////////////////////////// Validate if wallet is active
            if (!userWallet.isActive()) {
                log.error("Wallet is not active for userId: {} and currencyCode: {}", walletRequest.getUserId(), walletRequest.getCurrencyCode());
                walletResponse.setResponseMessage(ResponseMessage.WALLET_IS_NOT_ACTIVE.getStatusCode());
                return walletResponse;
            }

            /////////////////////////////////////// Validate ownership or authorization
            String currentUsername = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));

            if (!userWallet.getUser().getUserName().equals(currentUsername) && !isAdmin) {
                log.error("Access denied for user: {}. Attempted to access wallet for userId: {}", currentUsername, walletRequest.getUserId());
                walletResponse.setResponseMessage(ResponseMessage.ACCESS_DENIED.getStatusCode());
                return walletResponse;
            }

            /////////////////////////////////////// Generate transaction reference
            String transactionReference = UUID.randomUUID().toString();

            /////////////////////////////////////// Save the transaction
            Transaction transaction = new Transaction();
            transaction.setTransactionReference(transactionReference);
            transaction.setWallet(userWallet);
            transaction.setAmount(BigDecimal.ZERO);
            transaction.setCurrencyCode(userWallet.getCurrencyCode());
            transaction.setType("BALANCE_CHECK");
            transaction.setTransactionDate(LocalDateTime.now());
            transactionRepository.save(transaction);

            walletResponse.setResponseMessage(ResponseMessage.SUCCESS.getStatusCode());
            walletResponse.setBalance(userWallet.getBalance());
            walletResponse.setCurrencyCode(userWallet.getCurrencyCode());
            walletResponse.setUser(userWallet.getUser());
            walletResponse.setTransactionReference(transactionReference);

            log.info("Balance retrieved successfully for userId: {} with transaction reference: {}", walletRequest.getUserId(), transactionReference);

        } catch (Exception ex) {
            log.error("Error occurred while retrieving balance: ", ex);
            walletResponse.setResponseMessage(ResponseMessage.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        return walletResponse;
    }


}
