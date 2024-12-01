package com.webgis.dsws.model.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum VaiTroEnum {
    ADMIN(),
    MANAGER(),
    USER();

    public final long value = 0;
}
