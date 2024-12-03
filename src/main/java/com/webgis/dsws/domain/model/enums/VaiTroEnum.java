package com.webgis.dsws.domain.model.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum VaiTroEnum {
    ADMIN(),
    MANAGER(),
    USER();

    public final long value = 0;
}