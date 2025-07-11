package com.he161696.kingbarber.model;

public class Clients {
    private int ClientId;
    private String FullName;
    private String Email;
    private String Password;

    public Clients(int clientId) {
        ClientId = clientId;
    }

    public Clients(int clientId, String fullName, String email, String password) {
        ClientId = clientId;
        FullName = fullName;
        Email = email;
        Password = password;
    }

    public int getClientId() {
        return ClientId;
    }

    public void setClientId(int clientId) {
        ClientId = clientId;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
