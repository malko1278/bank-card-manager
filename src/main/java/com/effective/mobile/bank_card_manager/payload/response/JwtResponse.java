package com.effective.mobile.bank_card_manager.payload.response;

import java.util.Collection;

public class JwtResponse {
    private String token;
    private Long id;
    private String username;
    private Collection<?> authorities;

    public JwtResponse(String token, Long id, String username, Collection<?> authorities) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.authorities = authorities;
    }

    // Геттеры (сеттеры отсутствуют, так как это неизменяемый объект ответа)
    public String getToken() {
        return token;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Collection<?> getAuthorities() {
        return authorities;
    }
}