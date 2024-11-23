
package com.webgis.dsws.domain.model.enums;

public enum MucDoBenhEnum {
    BANG_A("Bệnh thuộc danh mục bảng A của Luật Thú y thế giới"),
    BANG_B("Bệnh thuộc danh mục bảng B của Luật Thú y thế giới"),
    NGUY_HIEM("Bệnh nguy hiểm của động vật"),
    PHONG_BENH_BAT_BUOC("Bệnh phải áp dụng các biện pháp phòng bệnh bắt buộc"),
    THONG_THUONG("Bệnh thông thường");

    private final String description;

    MucDoBenhEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}