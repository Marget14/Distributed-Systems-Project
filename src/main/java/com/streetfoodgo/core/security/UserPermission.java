package com.streetfoodgo.core.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserPermission {
    ORDER_CREATE("order:create"),
    ORDER_READ("order:read"),
    ORDER_UPDATE_STATUS("order:update"),
    MENU_MANAGE("menu:manage"),
    USER_MANAGE("user:manage");
    private final String permission;
}