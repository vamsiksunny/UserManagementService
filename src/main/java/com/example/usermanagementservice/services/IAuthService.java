package com.example.usermanagementservice.services;

import com.example.usermanagementservice.models.User;
import org.antlr.v4.runtime.misc.Pair;

public interface IAuthService {

    User signup(String name, String email, String password);

    Pair<User, String> login(String email, String password);

    void validateToken(String token);

}
