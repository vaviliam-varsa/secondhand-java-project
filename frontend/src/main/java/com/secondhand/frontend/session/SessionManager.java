package com.secondhand.frontend.session;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();

    private String token;
    private Long userId;
    private String username;
    private String role;

    // Client-side convenience only: the backend contract has no "my ads" endpoint,
    // so we remember ids of ads created during this session to let the user find
    // and manage them (including PENDING ones not shown in the public list).
    private final List<Long> myAdIds = new ArrayList<>();

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void login(String token, Long userId, String username, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public void logout() {
        this.token = null;
        this.userId = null;
        this.username = null;
        this.role = null;
        this.myAdIds.clear();
    }

    public boolean isLoggedIn() {
        return token != null;
    }

    public boolean isAdmin() {
        return isLoggedIn() && "ADMIN".equals(role);
    }

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }

    public void rememberCreatedAd(Long adId) {
        if (adId != null && !myAdIds.contains(adId)) {
            myAdIds.add(adId);
        }
    }

    public List<Long> getMyAdIds() {
        return myAdIds;
    }
}