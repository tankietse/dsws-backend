
package com.webgis.dsws.model;

import lombok.Getter;

@Getter
public enum AdminLevel {
    QUOC_GIA(2),
    CAP_TINH(4),
    CAP_HUYEN(6),
    CAP_XA(8);

    private final int level;

    AdminLevel(int level) {
        this.level = level;
    }

    public static AdminLevel fromLevel(int level) {
        for (AdminLevel al : values()) {
            if (al.getLevel() == level) return al;
        }
        throw new IllegalArgumentException("Invalid admin level: " + level);
    }
}