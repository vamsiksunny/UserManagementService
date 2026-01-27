package com.example.usermanagementservice.services;

import com.example.usermanagementservice.exceptions.PasswordMismatchException;
import com.example.usermanagementservice.exceptions.UserAlreadyExistingException;
import com.example.usermanagementservice.exceptions.UserNotRegisteredException;
import com.example.usermanagementservice.models.Role;
import com.example.usermanagementservice.models.State;
import com.example.usermanagementservice.models.User;
import com.example.usermanagementservice.repo.RoleRepository;
import com.example.usermanagementservice.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class AuthService implements IAuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public User signup(String name, String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            throw new UserAlreadyExistingException("Please try a different email.");
        }

        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(password); // TODO : to be encoded
        user.setState(State.ACTIVE);
        user.setCreatedAt(new Date());

        Role role;

        Optional<Role> roleOptional = roleRepository.findByValue("NON_ADMIN");
        if (roleOptional.isEmpty()) {
            role = new Role();
            role.setValue("NON_ADMIN");
            role.setState(State.ACTIVE);
            role.setCreatedAt(new Date());
            roleRepository.save(role);
        } else {
            role = roleOptional.get();
        }

        user.getRoles().add(role);

        return userRepository.save(user);
    }

    @Override
    public User login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new UserNotRegisteredException("User ");
        }

        User user = userOptional.get();
        if (!password.equals(user.getPassword())) {
            throw new PasswordMismatchException("Please enter correct password");
        }
        // JWT token generation
        return user;
    }
}
