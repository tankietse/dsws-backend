package com.webgis.dsws.model;

import lombok.Getter;

import java.util.*;

@Getter
public enum CapHanhChinh {
    CAP_TINH(Arrays.asList(4, 6), "Cấp tỉnh", Arrays.asList(
            PhanLoaiDonViHanhChinh.THANH_PHO_TRUNG_UONG,
            PhanLoaiDonViHanhChinh.TINH
    )),
    CAP_HUYEN(Arrays.asList(6, 8), "Cấp huyện", Arrays.asList(
            PhanLoaiDonViHanhChinh.QUAN,
            PhanLoaiDonViHanhChinh.HUYEN,
            PhanLoaiDonViHanhChinh.THI_XA,
            PhanLoaiDonViHanhChinh.THANH_PHO_THUOC_TINH,
            PhanLoaiDonViHanhChinh.THANH_PHO_THUOC_THANH_PHO
    )),
    CAP_XA(Arrays.asList(8, 10), "Cấp xã", Arrays.asList(
            PhanLoaiDonViHanhChinh.PHUONG,
            PhanLoaiDonViHanhChinh.XA,
            PhanLoaiDonViHanhChinh.THI_TRAN
    )),

    // Dành cho các đơn vị hành chính đặc biệt như TP. Hồ Chí Minh, Hà Nội
    DAC_BIET(List.of(6), "Đơn vị hành chính đặc biệt", Arrays.asList(
            PhanLoaiDonViHanhChinh.THANH_PHO_THUOC_THANH_PHO
    ));

    private final List<Integer> adminLevels;
    private final String moTa;
    private final List<PhanLoaiDonViHanhChinh> cacPhanLoai;

    CapHanhChinh(List<Integer> adminLevels, String moTa, List<PhanLoaiDonViHanhChinh> cacPhanLoai) {
        this.adminLevels = adminLevels;
        this.moTa = moTa;
        this.cacPhanLoai = cacPhanLoai;
    }

    // Hàm thực hiện chuyển đổi từ admin level sang cấp hành chính
    public static CapHanhChinh fromAdminLevel(Integer adminLevel, PhanLoaiDonViHanhChinh phanLoai) {
        // Duyệt qua tất cả các cấp hành chính
        for (CapHanhChinh cap : values()) {
            if (cap.getAdminLevels().contains(adminLevel) &&
                    cap.getCacPhanLoai().contains(phanLoai)) {
                return cap;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy cấp hành chính phù hợp");
    }
}
