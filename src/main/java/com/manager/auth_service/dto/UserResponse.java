package com.manager.auth_service.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private boolean enabled;
    private Set<String> roles;

}