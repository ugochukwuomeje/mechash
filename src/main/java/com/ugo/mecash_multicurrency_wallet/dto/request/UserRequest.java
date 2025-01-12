package com.ugo.mecash_multicurrency_wallet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    private String email;
    private String password;
  //  private String fullName;
    private String userName;
    private String firstName;
    private String lastName;
    private String role;

}

