package com.webgis.dsws.model;

public enum PhanLoaiDonViHanhChinh {
    THANH_PHO_TRUNG_UONG("Thành phố trực thuộc Trung ương", 4),
    TINH("Tỉnh", 4),
    THANH_PHO_THUOC_TINH("Thành phố thuộc tỉnh", 6),
    THANH_PHO_THUOC_THANH_PHO("Thành phố thuộc thành phố trực thuộc Trung ương", 6),
    QUAN("Quận", 8),
    HUYEN("Huyện", 8),
    THI_XA("Thị xã", 8),
    PHUONG("Phường", 10),
    XA("Xã", 10),
    THI_TRAN("Thị trấn", 10);

    private final String moTa;
    private final Integer defaultAdminLevel;

    PhanLoaiDonViHanhChinh(String moTa, Integer defaultAdminLevel) {
        this.moTa = moTa;
        this.defaultAdminLevel = defaultAdminLevel;
    }

    public String getMoTa() {
        return moTa;
    }

    public Integer getDefaultAdminLevel() {
        return defaultAdminLevel;
    }
}
