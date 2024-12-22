package com.webgis.dsws.domain.model.enums;

public enum MucDoBenhEnum {
    BANG_A("Bệnh thuộc danh mục bảng A của Luật Thú y thế giới", 5, "#4B0082", 0.9f, 0.8f, 0.1f),
    BANG_B("Bệnh thuộc danh mục bảng B của Luật Thú y thế giới", 4, "#ae00ff", 0.7f, 0.6f, 0.3f),
    NGUY_HIEM("Bệnh nguy hiểm của động vật", 3, "#FF0000", 0.5f, 0.4f, 0.5f),
    PHONG_BENH_BAT_BUOC("Bệnh phải áp dụng các biện pháp phòng bệnh bắt buộc", 2, "#FFA500", 0.3f, 0.2f, 0.7f),
    THONG_THUONG("Bệnh thông thường", 1, "#008000", 0.1f, 0.1f, 0.8f);

    private final String description;
    private final int severityLevel;
    private final String colorCode;
    private final float tiLeLayNhiem;
    private final float tiLeChet;
    private final float tiLeHoiPhuc;

    MucDoBenhEnum(String description, int severityLevel, String colorCode, float tiLeLayNhiem, float tiLeChet,
            float tiLeHoiPhuc) {
        this.description = description;
        this.severityLevel = severityLevel;
        this.colorCode = colorCode;
        this.tiLeLayNhiem = tiLeLayNhiem;
        this.tiLeChet = tiLeChet;
        this.tiLeHoiPhuc = tiLeHoiPhuc;
    }

    public String getDescription() {
        return description;
    }

    public int getSeverityLevel() {
        return severityLevel;
    }

    public String getColorCode() {
        return colorCode;
    }

    public float getTiLeLayNhiem() {
        return tiLeLayNhiem;
    }

    public float getTiLeChet() {
        return tiLeChet;
    }

    public float getTiLeHoiPhuc() {
        return tiLeHoiPhuc;
    }
}