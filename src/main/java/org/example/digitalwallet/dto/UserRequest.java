package org.example.digitalwallet.dto;


import org.example.digitalwallet.model.MembershipStatus;

public class UserRequest {

    private String email;
    private String username;
    private String password;

    private MembershipStatus status = MembershipStatus.FREE;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public void setStatus(MembershipStatus status) {
        this.status = status;
    }

    public UserRequest() {
    }

    public UserRequest(String email, String username, String password, MembershipStatus status) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.status = status;
    }
}
