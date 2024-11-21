
package com.webgis.dsws.model;

import lombok.Getter;

@Getter
public enum AdminLevelEnum {
    QUOC_GIA(2),
    CAP_TINH(4),
    CAP_HUYEN(6),
    CAP_XA(8);

    private final int level;

    AdminLevelEnum(int level) {
        this.level = level;
    }

    public static AdminLevelEnum fromLevel(int level) {
        for (AdminLevelEnum al : values()) {
            if (al.getLevel() == level)
                return al;
        }
        throw new IllegalArgumentException("Invalid admin level: " + level);
    }
}