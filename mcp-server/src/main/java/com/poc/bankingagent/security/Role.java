package com.poc.bankingagent.security;

import java.util.EnumSet;
import java.util.Set;

public enum Role {
    USER(EnumSet.of(
            Permission.TOOL_ACCOUNT_READ,
            Permission.TOOL_CARD_TRANSACTION_READ,
            Permission.TOOL_ASYNC_JOB_READ,
            Permission.RESOURCE_SCHEMA_READ
    )),
    ADVISOR(union(USER.permissions, EnumSet.of(
            Permission.TOOL_LOAN_RECOMMEND,
            Permission.TOOL_TICKET_CREATE
    ))),
    ADMIN(EnumSet.allOf(Permission.class));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = Set.copyOf(permissions);
    }

    public boolean has(Permission permission) {
        return permissions.contains(permission);
    }

    private static Set<Permission> union(Set<Permission> base, Set<Permission> additional) {
        EnumSet<Permission> result = EnumSet.copyOf(base);
        result.addAll(additional);
        return result;
    }
}
