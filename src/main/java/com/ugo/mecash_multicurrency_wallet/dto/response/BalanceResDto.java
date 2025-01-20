package com.ugo.mecash_multicurrency_wallet.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BalanceResDto {

    private String responseMessage;
    private int responseCode;
    private List<Map<String, Object>> balanceList;
}
