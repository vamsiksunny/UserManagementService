package com.example.usermanagementservice.services;

import com.example.usermanagementservice.exceptions.*;
import com.example.usermanagementservice.models.Role;
import com.example.usermanagementservice.models.Session;
import com.example.usermanagementservice.models.State;
import com.example.usermanagementservice.models.User;
import com.example.usermanagementservice.repo.RoleRepository;
import com.example.usermanagementservice.repo.SessionRepository;
import com.example.usermanagementservice.repo.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class AuthService implements IAuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SecretKey secretKey;

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

        String token = Jwts.builder().claims(claims).signWith(secretKey).compact();

        Session session = new Session();
        session.setUser(user);
        session.setToken(token);
        session.setState(State.ACTIVE);
        session.setCreatedAt(new Date());
        sessionRepository.save(session);

        return new Pair<>(user, token);
    }

    // check if token is expired or not --> JWT parser
    @Override
    public void validateToken(String token) {
        Optional<Session> sessionOptional = sessionRepository.findByToken(token);

        if (sessionOptional.isEmpty()) {
            throw new InvalidTokenException("Please login");
        }

        // check for expiry
        Session session = sessionOptional.get();
        JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();

        Claims claims = jwtParser.parseSignedClaims(token).getPayload();

        Long expiry = (Long)claims.get("expiry");
        long currentTime = System.currentTimeMillis();
        System.out.println("expiry " + expiry);
        System.out.println("current Time " + currentTime);

        if(currentTime > expiry) {
            session.setState(State.INACTIVE);
            session.setLastUpdatedAt(new Date());
            sessionRepository.save(session);
            throw new TokenExpiredException("Please login again, token has expired");
        }
    }

}
