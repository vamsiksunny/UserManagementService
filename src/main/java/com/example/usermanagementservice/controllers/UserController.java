package com.example.usermanagementservice.controllers;

import com.example.usermanagementservice.dto.RoleDto;
import com.example.usermanagementservice.dto.UserDto;
import com.example.usermanagementservice.models.Role;
import com.example.usermanagementservice.models.User;
import com.example.usermanagementservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/users")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public UserDto getUserDetails(@PathVariable Long id) {
        return from(userService.findUserById(id));
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
