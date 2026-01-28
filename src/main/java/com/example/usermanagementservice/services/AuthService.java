package com.example.usermanagementservice.services;

import com.example.usermanagementservice.exceptions.PasswordMismatchException;
import com.example.usermanagementservice.exceptions.UserAlreadyExistingException;
import com.example.usermanagementservice.exceptions.UserNotRegisteredException;
import com.example.usermanagementservice.models.Role;
import com.example.usermanagementservice.models.State;
import com.example.usermanagementservice.models.User;
import com.example.usermanagementservice.repo.RoleRepository;
import com.example.usermanagementservice.repo.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AuthService implements IAuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public User signup(String name, String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            throw new UserAlreadyExistingException("Please try a different email.");
        }

        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(bCryptPasswordEncoder.encode(password));
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
    public Pair<User, String> login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new UserNotRegisteredException("User ");
        }

        User user = userOptional.get();
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new PasswordMismatchException("Please enter correct password");
        }
        // JWT token generation
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        List<String> roles = new ArrayList<>();
        for (Role role : user.getRoles()) {
            roles.add(role.getValue());
        }
        claims.put("access", roles);
        Long currentTime = System.currentTimeMillis();
        claims.put("iat", currentTime);
        claims.put("expiry", currentTime + 100000);
        claims.put("issuer", "facebook");

        MacAlgorithm algo = Jwts.SIG.HS256;
        SecretKey secretKey = algo.key().build();

        String token = Jwts.builder().claims(claims).signWith(secretKey).compact();

        return new Pair<>(user, token);
    }
}
