package com.manager.auth_service.dto;

import lombok.Data;
import java.util.Set;

@Data
public class CreateUserRequest {

    private String username;

    private String password;

    private String email;

    private Set<String> roles;

}