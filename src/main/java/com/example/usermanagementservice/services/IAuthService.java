package com.example.usermanagementservice.services;

import com.example.usermanagementservice.models.User;

public interface IAuthService {

    User signup(String name, String email, String password);

    User login(String email, String password);

}
