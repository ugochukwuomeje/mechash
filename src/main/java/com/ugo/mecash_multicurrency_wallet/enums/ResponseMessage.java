package com.ugo.mecash_multicurrency_wallet.enums;

public enum ResponseMessage {

    ACCOUNT_NUMBER_CANNOT_BE_FOUND("404"),WALLET_ID_MUST_BE_PROVIDED(""), INVALID_INPUT_PARAMETER("404"), SUCCESS("200"), WALLET_NOT_FOUND("404"), INTERNAL_SERVER_ERROR("500"), MAX_BALANCE_EXCEEDED(""), MAX_TRANSACTIONS_EXCEEDED(""), WALLET_IS_NOT_ACTIVE(""), INSUFFICIENT_FUNDS(""), RECIPIENT_WALLET_NOT_FOUND(""), USER_WALLET_NOT_FOUND(""), ACCESS_DENIED(""), INVALID_PAGINATION_PARAMETERS(""), ERROR_FETCHING_TRANSACTION_HISTORY(""), PAGE_NUMBER_OR_PAGE_SIZE_CANNOT_BE_LESS_THAN_1(""), ERROR_ACCESSING_TOKEN(""), INCORRECT_USERNAME_OR_PASSWORD("");
    String statusCode;

        private ResponseMessage(String statusCode){
            this.statusCode = statusCode;
        }

        public String getStatusCode(){
            return statusCode;
        }

}
