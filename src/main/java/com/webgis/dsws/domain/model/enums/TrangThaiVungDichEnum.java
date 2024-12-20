package com.webgis.dsws.domain.model.enums;

import lombok.*;

@Getter
public enum TrangThaiVungDichEnum {
    DANG_BUNG_PHAT("1", "Đang bùng phát", "Dịch bệnh đang phát triển nhanh chóng, số ca nhiễm gia tăng.", "#FF0000"),
    DA_KIEM_SOAT_DUOC("2", "Đã kiểm soát được", "Dịch bệnh đang dần ổn định, số ca nhiễm giảm, lây lan đã được hạn chế.", "#FFFF00"),
    DANG_GIAM_SAT("3", "Đang giám sát", "Không có ca nhiễm mới, nhưng vẫn theo dõi để đảm bảo không tái phát.", "#00FF00"),
    DA_KET_THUC("4", "Đã kết thúc", "Vùng dịch không còn ca bệnh, các hoạt động đã trở lại bình thường.", "#0000FF");

    private final String maTrangThai;
    private final String tenTrangThai;
    private final String moTa;
    private final String mauHienThi;

    TrangThaiVungDichEnum(String maTrangThai, String tenTrangThai, String moTa, String mauHienThi) {
        this.maTrangThai = maTrangThai;
        this.tenTrangThai = tenTrangThai;
        this.moTa = moTa;
        this.mauHienThi = mauHienThi;
    }
}