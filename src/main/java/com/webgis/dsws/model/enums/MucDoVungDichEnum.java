package com.webgis.dsws.model.enums;

import lombok.Getter;

@Getter
public enum MucDoVungDichEnum {
    CAP_DO_1(1, "Thấp", "#624E88"),
    CAP_DO_2(2, "Trung bình", "#821131"),
    CAP_DO_3(3, "Cao", "#C7253E"),
    CAP_DO_4(4, "Rất cao", "#E85C0D");

    private final Integer mucDo;
    private final String moTa;
    private final String mauHienThi;

    MucDoVungDichEnum(Integer mucDo, String moTa, String mauHienThi) {
        this.mucDo = mucDo;
        this.moTa = moTa;
        this.mauHienThi = mauHienThi;
    }
}
