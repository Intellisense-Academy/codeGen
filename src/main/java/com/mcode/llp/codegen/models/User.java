package com.mcode.llp.codegen.models;

import lombok.Data;

@Data
public class User {
    private String username;
    private String password;  // In real use cases, hash this password!
    private String role;
    private String tenant;
}
