package com.example.pantallas.security;

public class SecurityUtil {

    private static String currentUser;
    private static String currentRole;
    private static int currentUserId;

    private SecurityUtil() {}

    public static void login(int id, String username, String role) {
        currentUserId = id;
        currentUser = username;
        currentRole = role;
    }

    public static void logout() {
        currentUserId = 0;
        currentUser = null;
        currentRole = null;
    }

    public static boolean isAuthenticated() {
        return currentUser != null;
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentRole() {
        return currentRole;
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }
    
    public static boolean hasRole(String role) {
        return currentRole != null && currentRole.equalsIgnoreCase(role);
    }
}
