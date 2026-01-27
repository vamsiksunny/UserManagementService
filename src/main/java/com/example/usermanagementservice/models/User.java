package com.example.usermanagementservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.Setter;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class User extends BaseModel {
    private String email;
    private String password;
    private String name;
    @ManyToMany
    private List<Role> roles = new ArrayList<>();
}
