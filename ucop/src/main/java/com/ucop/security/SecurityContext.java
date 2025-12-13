package com.ucop.security;

import com.ucop.entity.Account;

/**
 * Đơn giản hóa việc truyền user hiện tại tới các lớp khác (audit, service).
 * Không phải là giải pháp bảo mật đầy đủ, chỉ dùng trong phạm vi desktop app này.
 */
public final class SecurityContext {

    private static final ThreadLocal<Account> CURRENT = new ThreadLocal<>();

    private SecurityContext() {
    }

    public static void setCurrentUser(Account account) {
        CURRENT.set(account);
    }

    public static Account getCurrentUser() {
        return CURRENT.get();
    }

    public static String getCurrentUsername() {
        Account acc = CURRENT.get();
        return acc != null ? acc.getUsername() : null;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
