package com.shop.common.context;

import java.util.Collections;
import java.util.List;

public final class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> ROLES = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId, String username, List<String> roles) {
        USER_ID.set(userId);
        USERNAME.set(username);
        ROLES.set(roles == null ? Collections.emptyList() : roles);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static List<String> getRoles() {
        return ROLES.get() == null ? Collections.emptyList() : ROLES.get();
    }

    public static boolean isAdmin() {
        return getRoles().stream().anyMatch(r -> "SUPER_ADMIN".equals(r) || "ADMIN".equals(r));
    }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
        ROLES.remove();
    }
}

