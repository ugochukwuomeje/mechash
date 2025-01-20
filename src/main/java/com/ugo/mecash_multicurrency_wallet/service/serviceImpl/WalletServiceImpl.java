package com.ugo.mecash_multicurrency_wallet.service.serviceImpl;

import com.ugo.mecash_multicurrency_wallet.dto.request.WalletRequest;
import com.ugo.mecash_multicurrency_wallet.dto.response.BalanceResDto;
import com.ugo.mecash_multicurrency_wallet.dto.response.WalletResponse;
import com.ugo.mecash_multicurrency_wallet.entity.Transaction;
import com.ugo.mecash_multicurrency_wallet.entity.User;
import com.ugo.mecash_multicurrency_wallet.entity.Wallet;
import com.ugo.mecash_multicurrency_wallet.enums.ResponseMessage;
import com.ugo.mecash_multicurrency_wallet.enums.Status;
import com.ugo.mecash_multicurrency_wallet.enums.TransactionType;
import com.ugo.mecash_multicurrency_wallet.enums.Type;
import com.ugo.mecash_multicurrency_wallet.repository.TransactionRepository;
import com.ugo.mecash_multicurrency_wallet.repository.UserRepository;
import com.ugo.mecash_multicurrency_wallet.repository.WalletRepository;
import com.ugo.mecash_multicurrency_wallet.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

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
    public WalletResponse depositMoney(WalletRequest walletRequest) {
        WalletResponse walletResponse = new WalletResponse();

        try {
            ///////////////////////////////////////////// Validate input parameters

            ///////////////////////////////////// Fetch the user from the wallet
            Long receivewallet = walletRequest.getRecipientWalletId();

            if(receivewallet == null ){
                walletResponse.setResponseMessage(ResponseMessage.RECIPIENT_WALLET_CANNOT_BE_NULL.toString());
                walletResponse.setResponseMessage(ResponseMessage.RECIPIENT_WALLET_CANNOT_BE_NULL.toString());
                return walletResponse;
            }

            log.info("*********************************Deposit method logged authenticated username/email*********************************" + walletRequest.getSender_walletId());
            Optional<Wallet> walletOptional = walletRepository.findByWallet(receivewallet);
            if (walletOptional.isEmpty()) {
                log.error("Wallet not found for userId: {}", walletRequest.getUserId());
                walletResponse.setResponseMessage(ResponseMessage.WALLET_NOT_FOUND.toString());
                walletResponse.setResponseCode(Integer.parseInt(ResponseMessage.WALLET_NOT_FOUND.getStatusCode()));
                return walletResponse;
            }
            ///////////////check if the account is active
            if (!walletOptional.get().getIsActive()) {
                log.error("Wallet not active for userId: {}", walletRequest.getUserId());
                walletResponse.setResponseMessage(ResponseMessage.WALLET_IS_NOT_ACTIVE.getStatusCode());
                walletResponse.setResponseCode(Integer.parseInt(ResponseMessage.WALLET_IS_NOT_ACTIVE.getStatusCode()));
                return walletResponse;
            }

            if (walletRequest.getReceive_amount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Deposit amount must be greater than zero.");
            }

            if (walletRequest.getSend_amount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Send amount must be greater than zero.");
            }

            Wallet wallet = walletOptional.get();

            ////////////////////////////////////////// Validate system constraints
            if (wallet.getMaxBalance() != null &&
                    wallet.getBalance().add(walletRequest.getReceive_amount()).compareTo(wallet.getMaxBalance()) > 0) {
                log.error("Deposit exceeds maximum allowed balance for userId: {}", wallet.getWallet());
                walletResponse.setResponseCode(Integer.parseInt(ResponseMessage.MAX_BALANCE_EXCEEDED.getStatusCode()));
                walletResponse.setResponseMessage(ResponseMessage.MAX_BALANCE_EXCEEDED.toString());
                return walletResponse;
            }

            if (wallet.getMaxTransactionsPerDay() != null) {
                int dailyTransactionCount = walletRepository.countTransactionsForToday(wallet.getId()).intValue();
                if (dailyTransactionCount >= wallet.getMaxTransactionsPerDay()) {
                    log.error("Maximum transactions per day exceeded for userId: {}", wallet.getWallet());
                    walletResponse.setResponseCode(Integer.parseInt(ResponseMessage.MAX_TRANSACTIONS_EXCEEDED.getStatusCode()));
                    walletResponse.setResponseMessage(ResponseMessage.MAX_TRANSACTIONS_EXCEEDED.toString());
                    return walletResponse;
                }
            }

            ///////////////////////////////// Generate transaction reference
            String transactionReference = UUID.randomUUID().toString();

            walletRepository.save(wallet);

            Transaction transaction = new Transaction();
            transaction.setTransactionReference(transactionReference);
            transaction.setWallet(wallet);
            transaction.setReceive_amount(walletRequest.getReceive_amount());
            transaction.setReceive_currency(walletRequest.getReceive_currency_code());
            transaction.setSend_amount(walletRequest.getSend_amount());
            transaction.setReceive_amount(walletRequest.getReceive_amount());
            transaction.setReceive_currency(walletRequest.getReceive_currency_code());
            transaction.setType(Type.DEPOSIT);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setExchange_rate(walletRequest.getExchange_rate());
            transaction.setAccount(walletRequest.getSender_walletId());
            transaction.setResponse_message(ResponseMessage.SUCCESS.toString());
            transaction.setNarration(walletRequest.getNarration());
            transaction.setPayment_status(Status.COMPLETED);
            transaction.setSend_country_code(walletRequest.getSend_country_code());
            transaction.setDest_country_code(walletRequest.getReceive_country_code());
            transaction.setSend_issuer_code(walletRequest.getSend_issuer_code());
            transaction.setDestination_issuer_code(walletRequest.getReceive_issuer_code());
            transactionRepository.save(transaction);

            /////////////update wallet
             walletRepository.updateWallet( wallet.getBalance().add(walletRequest.getReceive_amount()),  wallet.getWallet());

            walletResponse.setBalance(wallet.getBalance().add(walletRequest.getReceive_amount()));
            walletResponse.setTransactionReference(transactionReference);
            walletResponse.setCurrencyCode(wallet.getCurrencyCode());
            walletResponse.setRecipientWalletId(walletRequest.getRecipientWalletId());
            walletResponse.setResponseMessage(ResponseMessage.SUCCESS.toString());
            walletResponse.setResponseCode(Integer.parseInt(ResponseMessage.SUCCESS.getStatusCode()));

        } catch (Exception e) {
            log.error("Error occurred while depositing money: ", e);
            walletResponse.setResponseMessage(ResponseMessage.INTERNAL_SERVER_ERROR.toString());
            walletResponse.setResponseCode(Integer.parseInt(ResponseMessage.INTERNAL_SERVER_ERROR.getStatusCode()));
        }

        return walletResponse;
    }

    @Override
    @Transactional
    public WalletResponse withdrawMoney(WalletRequest walletRequest, Authentication authentication) {
        WalletResponse walletResponse = new WalletResponse();

        try {
            ////////////////////////////////////////////////// Validate input parameters

            String userEmail = authentication.getName();
            log.info("*********************************Withdraw method logged authenticated username/email*********************************" + userEmail);
            User user = userRepository.findByUserUAndUsername(userEmail)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            if(walletRequest.getPin() == null || walletRequest.getPin().isEmpty()){

                walletResponse.setResponseMessage(ResponseMessage.INCORRECT_PIN.toString());
                walletResponse.setResponseCode(Integer.parseInt(ResponseMessage.INCORRECT_PIN.getStatusCode()));

                return walletResponse;
            }

            ////////////////////validate the authenticated user is the owner of the wallet
            //////////////////////////////////////////////////// Fetch the wallet with a pessimistic lock for the authenticated user
            Optional<Wallet> walletOptional = walletRepository.findByIdWithLock(user.getId(), walletRequest.getSender_walletId(), walletRequest.getSend_currency_code());
            if (walletOptional.isEmpty()) {
                log.error("Wallet not found for userId: {}", user.getId());
                walletResponse.setResponseMessage(ResponseMessage.WALLET_NOT_FOUND.toString());
                walletResponse.setResponseCode(Integer.parseInt(ResponseMessage.WALLET_NOT_FOUND.getStatusCode()));
                return walletResponse;
            }

            log.info("==============wallet found");

            Wallet wallet = walletOptional.get();

            ///////////////validate pin
            if(!bCryptPasswordEncoder.matches(walletRequest.getPin(),wallet.getPin())){

                log.error("================incorrect pin: {}", user.getId());
                walletResponse.setResponseMessage(ResponseMessage.INCORRECT_PIN.toString());
                walletResponse.setResponseCode(Integer.parseInt(ResponseMessage.INCORRECT_PIN.getStatusCode()));
                int pin_attempt = getPinAttempt(walletRequest.getSender_walletId());
                if(pin_attempt>=4){

                    ////////////////block the wallet
                    walletRepository.blockAccount(false, wallet.getWallet(), user);
                    walletResponse.setResponseMessage(ResponseMessage.WALLET_IS_NOT_ACTIVE.toString());
                    walletResponse.setResponseCode(Integer.parseInt(ResponseMessage.WALLET_IS_NOT_ACTIVE.getStatusCode()));
                    return walletResponse;
                }else{
                    pin_attempt++;
                    walletRepository.incrementPinAttempt(pin_attempt, wallet.getWallet(), user);
                }
                //////////////////////
                return walletResponse;
            }

            if(wallet.getPin_attempt() > 0) {
                walletRepository.incrementPinAttempt(0, wallet.getWallet(), user);
            }

            ///////////////////////////////////////////////// Validate if the wallet is active
            if (!wallet.isActive()) {
                log.error("Wallet is not active for userId: {}", user.getId());
                walletResponse.setResponseMessage(ResponseMessage.WALLET_IS_NOT_ACTIVE.toString());
                walletResponse.setResponseCode(Integer.parseInt(ResponseMessage.WALLET_IS_NOT_ACTIVE.getStatusCode()));
                return walletResponse;
            }

            /////////////////////////////////////// Validate if sufficient funds are available for the withdrawal
            if (wallet.getBalance().compareTo(walletRequest.getSend_amount()) < 0) {
                log.error("Insufficient funds in the wallet for userId: {}", user.getId());
                walletResponse.setResponseMessage(ResponseMessage.INSUFFICIENT_FUNDS.getStatusCode());
                walletResponse.setResponseCode(Integer.parseInt(ResponseMessage.INSUFFICIENT_FUNDS.getStatusCode()));
                return walletResponse;
            }

            ////////////////////////

            ///////////////////////////////// Generate transaction reference
            String transactionReference = UUID.randomUUID().toString();

            log.info("===========wallet balance before subtracting is: "+wallet.getBalance());
            //////////////////////////////// Perform the withdrawal by deducting the amount from the wallet balance
            wallet.setBalance(wallet.getBalance().subtract(walletRequest.getSend_amount()));

            /////////////////////////////// Create a new transaction for the withdrawal
            Transaction transaction = new Transaction();
            transaction.setTransactionReference(transactionReference);
            transaction.setWallet(wallet);
            transaction.setType(Type.WITHDRAWAL);
            transaction.setSend_amount(walletRequest.getSend_amount());
            transaction.setSend_currency(walletRequest.getSend_currency_code());
            transaction.setDestination_issuer_code("N/A");
            transaction.setSend_issuer_code(walletRequest.getSend_issuer_code());
            transaction.setSend_country_code(walletRequest.getSend_country_code());
            transaction.setDest_country_code("N/A");
            transaction.setSend_currency(walletRequest.getSend_currency_code());
            transaction.setDest_country_code("N/A");
            transaction.setNarration(walletRequest.getNarration());
            transaction.setReceive_amount(walletRequest.getReceive_amount());
            transaction.setSend_currency(walletRequest.getSend_currency_code());
            transaction.setReceive_currency("N/A");
            transaction.setExchange_rate(new BigDecimal(1));
            transaction.setTransactionDate(LocalDateTime.now());

            ////////////////////////////////////// Save the transaction
            transactionRepository.save(transaction);

            ////////////////////////////////////// Save the updated wallet

            //walletRepository.updateWallet(wallet.getBalance().subtract(walletRequest.getSend_amount()), walletRequest.getSender_walletId());


            log.info("=========== wallet balance after subtracting "+walletRequest.getSend_amount()+" is "+wallet.getBalance());
            //////////////////////////////////////// Set successful response
            walletResponse.setResponseMessage(ResponseMessage.SUCCESS.getStatusCode());
            walletResponse.setBalance(wallet.getBalance());
            walletResponse.setTransactionReference(transactionReference);
            walletResponse.setUserId(user.getId());
            walletResponse.setCurrencyCode(walletRequest.getSend_currency_code());
            walletResponse.setResponseCode(Integer.parseInt(ResponseMessage.SUCCESS.getStatusCode()));

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

            ////////////////////////////////// Fetch sender's user ID
            String senderEmail = authentication.getName();
            User sender = userRepository.findByUserUAndUsername(senderEmail)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Fetch sender's wallet with a pessimistic lock
            Optional<Wallet> senderWalletOptional = walletRepository.findByIdWithLock(sender.getId(), walletRequest.getSender_walletId(), walletRequest.getSend_currency_code());
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


            if(!bCryptPasswordEncoder.matches(walletRequest.getPin(), senderWallet.getPin())){

                log.error("================incorrect pin: {}", sender.getId());
                walletResponse.setResponseMessage(ResponseMessage.INCORRECT_PIN.toString());
                int pin_attempt = getPinAttempt(walletRequest.getSender_walletId());
                if(pin_attempt>=4){

                    ////////////////block the wallet
                    walletRepository.blockAccount(false, senderWallet.getWallet(), sender);
                }else{
                    pin_attempt++;
                    walletRepository.incrementPinAttempt(pin_attempt, senderWallet.getWallet(), sender);
                }
                //////////////////////
                return walletResponse;
            }

            /////////////////////////////////////// Generate transaction reference
            String transactionReference = UUID.randomUUID().toString();

            //////////////////////////////////////// Validate if sufficient funds are available for the transfer + transaction fee
            BigDecimal transactionFee = new BigDecimal("0.02");
            BigDecimal totalAmountToDeduct = walletRequest.getSend_amount().add(walletRequest.getSend_amount().multiply(transactionFee));

            if (senderWallet.getBalance().compareTo(totalAmountToDeduct) < 0) {
                log.error("Insufficient funds in sender's wallet for userId: {}", sender.getId());
                walletResponse.setResponseMessage(ResponseMessage.INSUFFICIENT_FUNDS.getStatusCode());
                return walletResponse;
            }

            if(walletRequest.getSend_issuer_code().equalsIgnoreCase(walletRequest.getReceive_issuer_code())) {
                //////////////////////////////////////////// Fetch recipient's wallet with a pessimistic lock
                //////////////////////////////////////////// we can alternatively call an api with the recipient wallet details
                Optional<Wallet> recipientWalletOptional = walletRepository.findByIdWithLock(walletRequest.getRecipientWalletId(), walletRequest.getReceive_currency_code());
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


                /////////////////////////////////////// Debit sender of transaction fee and sending amount
                senderWallet.setBalance(senderWallet.getBalance().subtract(totalAmountToDeduct));

                /////////////////////////////////////// Add transfer amount to recipient's wallet
                recipientWallet.setBalance(recipientWallet.getBalance().add(walletRequest.getReceive_amount().multiply(walletRequest.getExchange_rate())));

                walletRepository.save(senderWallet);
                walletRepository.save(recipientWallet);
            }

            Transaction transaction = new Transaction();
            transaction.setTransactionReference(transactionReference);
            transaction.setWallet(senderWallet);
            transaction.setReceive_wallet(walletRequest.getRecipientWalletId());
            transaction.setSend_amount(walletRequest.getReceive_amount().multiply(walletRequest.getExchange_rate()));
            transaction.setSend_currency(walletRequest.getSend_currency_code());
            transaction.setReceive_currency(walletRequest.getReceive_currency_code());
            transaction.setSend_country_code(walletRequest.getSend_country_code());
            transaction.setDest_country_code(walletRequest.getReceive_country_code());
            transaction.setType(Type.TRANSFER);
            transaction.setPayment_status(Status.COMPLETED);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setSend_issuer_code(walletRequest.getReceive_issuer_code());

            transactionRepository.save(transaction);

            walletResponse.setResponseMessage(ResponseMessage.SUCCESS.getStatusCode());
            walletResponse.setBalance(senderWallet.getBalance());
            walletResponse.setTransactionReference(transactionReference);
            walletResponse.setUserId(senderWallet.getId());
            walletResponse.setResponseCode(200);

        } catch (Exception ex) {
            log.error("Error occurred during money transfer: ", ex);
            walletResponse.setResponseMessage(ResponseMessage.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        return walletResponse;
    }




    @Override
    @Transactional
    public BalanceResDto getBalance(WalletRequest walletRequest, Authentication authentication) {
        BalanceResDto balanceResDto = new BalanceResDto();
        List<Map<String, Object>> mapList = new ArrayList<>();

        try {
            ////////////////////////////////// Fetch sender's user ID
            String senderEmail = authentication.getName();
            User user = userRepository.findByUserUAndUsername(senderEmail)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Fetch sender's wallet with a pessimistic lock
            Optional<List<Map<String, Object>>> optionalMapList = walletRepository.findByIdWithLock(user);
            if (optionalMapList.isEmpty()) {
                log.error(" wallet not found for userId: {}", user.getId());
                balanceResDto.setResponseMessage(ResponseMessage.WALLET_NOT_FOUND.getStatusCode());
                return balanceResDto;
            }

            mapList = optionalMapList.get();

            String transactionReference = UUID.randomUUID().toString();

            balanceResDto.setResponseMessage(ResponseMessage.SUCCESS.getStatusCode());
            balanceResDto.setBalanceList(mapList);
           balanceResDto.setResponseCode(Integer.parseInt(ResponseMessage.SUCCESS.getStatusCode()));

            log.info("Balance retrieved successfully for userId: {} with transaction reference: {}", walletRequest.getUserId(), transactionReference);

        } catch (Exception ex) {
            log.error("Error occurred while retrieving balance: ", ex);
            balanceResDto.setResponseMessage(ResponseMessage.INTERNAL_SERVER_ERROR.toString());
            balanceResDto.setResponseCode(Integer.parseInt(ResponseMessage.INTERNAL_SERVER_ERROR.getStatusCode()));
        }

        return balanceResDto;
    }


    public int getPinAttempt(Long wallet){

        log.info("=================== get pin with the wallet: "+wallet);
        return walletRepository.findByWallet(wallet).get().getPin_attempt();

    }

}
