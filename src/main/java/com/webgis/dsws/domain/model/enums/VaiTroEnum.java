package com.webgis.dsws.domain.model.enums;

import lombok.Getter;

@Getter
public enum VaiTroEnum {
    ADMIN(1L, "ROLE_ADMIN"),
    MANAGER(2L, "ROLE_MANAGER"),
    USER(3L, "ROLE_USER");

    private final long value;
    private final String role;

    VaiTroEnum(long value, String role) {
        this.value = value;
        this.role = role;
    }
}