package com.evomanex.AppRH.domain.user;

public enum UserRole {
    ADMIN("admin"),
    USERRH("user_rh"),
    USER("user_candidato");

    private String role;

     UserRole(String role){
        this. role = role;
    }

    public String getRole(){
        return role;
    }

}
