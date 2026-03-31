package com.hotel.util;

import lombok.Data;

@Data
public class UserContext {
    private static final ThreadLocal<UserInfo> USER_HOLDER = new ThreadLocal<>();

    public static void setUser(UserInfo user) {
        USER_HOLDER.set(user);
    }

    public static UserInfo getUser() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        UserInfo user = getUser();
        return user != null ? user.getUserId() : null;
    }

    public static String getUsername() {
        UserInfo user = getUser();
        return user != null ? user.getUsername() : null;
    }

    public static Integer getRole() {
        UserInfo user = getUser();
        return user != null ? user.getRole() : null;
    }

    public static boolean isAdmin() {
        return Integer.valueOf(1).equals(getRole());
    }

    public static void clear() {
        USER_HOLDER.remove();
    }

    @Data
    public static class UserInfo {
        private Long userId;
        private String username;
        private Integer role;
    }
}
