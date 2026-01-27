package com.example.usermanagementservice.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequestDto {

    private String email;
    private String password;

}
