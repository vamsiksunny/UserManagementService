package com.example.usermanagementservice.controllers;

import com.example.usermanagementservice.dto.*;
import com.example.usermanagementservice.models.Role;
import com.example.usermanagementservice.models.User;
import com.example.usermanagementservice.services.IAuthService;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    private IAuthService authService;

    //signup
    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody SignUpRequestDto signUpRequestDto) {
        User user = authService.signup(
                signUpRequestDto.getName(),
                signUpRequestDto.getEmail(),
                signUpRequestDto.getPassword()
        );

        UserDto userDto = from(user);
        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    //login
    // this is post api since we create jwt token in this api
    // we are using ResponseEntity because we need to pass jwt token in headers of response
    @PostMapping("login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        Pair<User, String> userTokenPair = authService.login(
                loginRequestDto.getEmail(),
                loginRequestDto.getPassword());
        String token = userTokenPair.b;
        UserDto userDto = from(userTokenPair.a);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.SET_COOKIE, token);
        return new ResponseEntity<>(userDto, headers, HttpStatus.OK);
    }

    @PostMapping("/validateToken")
    public void validToken(@RequestBody ValidateTokenDto validateTokenDto) {
        authService.validateToken(validateTokenDto.getToken());
    }

    private UserDto from(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());

        List<RoleDto> roleDtoList = new ArrayList<>();
        for (Role role : user.getRoles()) {
            RoleDto roleDto = new RoleDto();
            roleDto.setValue(role.getValue());
            roleDtoList.add(roleDto);
        }
        userDto.setRoles(roleDtoList);
        return userDto;
    }

}
