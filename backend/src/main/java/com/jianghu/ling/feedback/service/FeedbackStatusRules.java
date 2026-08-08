package com.jianghu.ling.feedback.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 反馈状态流转（api.md §14.5.0 / §16.11.3）。
 */
public final class FeedbackStatusRules {

    public static final Set<String> TYPES = Set.of("BUG", "SUGGEST", "COMPLAINT", "OTHER");
    public static final Set<String> STATUSES = Set.of("NEW", "PROCESSING", "RESOLVED", "CLOSED");
    public static final Set<String> TERMINAL = Set.of("RESOLVED", "CLOSED");

    private FeedbackStatusRules() {
    }

    public static Map<String, Object> historyEntry(String fromStatus, String toStatus,
                                                   Long adminId, String adminName, String remark,
                                                   LocalDateTime at) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("fromStatus", fromStatus);
        e.put("toStatus", toStatus);
        e.put("adminId", adminId);
        e.put("adminName", adminName);
        e.put("remark", remark);
        e.put("at", at);
        return e;
    }

    public static boolean isValidType(String type) {
        return type != null && TYPES.contains(type);
    }

    public static boolean isValidStatus(String status) {
        return status != null && STATUSES.contains(status);
    }

    public static boolean isTerminal(String status) {
        return status != null && TERMINAL.contains(status);
    }

    /**
     * @return true 若允许从 from → to
     */
    public static boolean canTransit(String from, String to) {
        if (!isValidStatus(from) || !isValidStatus(to)) {
            return false;
        }
        if (isTerminal(from)) {
            return false;
        }
        if ("NEW".equals(from)) {
            return "PROCESSING".equals(to) || "RESOLVED".equals(to) || "CLOSED".equals(to);
        }
        if ("PROCESSING".equals(from)) {
            return "RESOLVED".equals(to) || "CLOSED".equals(to);
        }
        return false;
    }
}
