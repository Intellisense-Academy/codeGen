package com.mcode.llp.codegen.controllers;

import com.mcode.llp.codegen.models.TenantRequest;
import com.mcode.llp.codegen.services.TenantService;
import com.mcode.llp.codegen.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class TenantController {
    private final TenantService tenantService;
    private final UserService userService;

    @Autowired
    public TenantController(TenantService tenantService,UserService userService){
        this.tenantService=tenantService;
        this.userService=userService;
    }


    @PostMapping("/add-tenant")
    public ResponseEntity<Object> addTenant(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @RequestBody TenantRequest request) {
        try{
            String[] credentials = userService.extractCredentials(authHeader);
            String email = credentials[0];
            String password = credentials[1];
            ResponseEntity<Object> userValidResponse = userService.isValidUser(email, password,"users","POST");
            if (userValidResponse.getStatusCode() == HttpStatus.OK) {
                tenantService.createTenant(request);
                return ResponseEntity.ok("Tenant created successfully!");
            }else{
                return userValidResponse;
            }
        }catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}

