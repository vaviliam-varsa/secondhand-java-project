package com.secondhand.frontend.session;

public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();

    private String token;
    private Long userId;
    private String username;
    private String role;

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
}