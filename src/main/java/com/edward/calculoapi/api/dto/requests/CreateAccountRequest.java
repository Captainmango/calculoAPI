package com.edward.calculoapi.api.dto.requests;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

public class CreateAccountRequest {

    @NotBlank
    private String firstName;

    private String lastName;

    private Set<String> roles;

    @NotBlank
    @Size(min=6)
    private String password;

    public CreateAccountRequest(String firstName, String lastName, Set<String> roles, String password, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
        this.password = password;
        this.email = email;
    }

    @NotBlank
    @Email
    private String email;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> role) {
        this.roles = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
