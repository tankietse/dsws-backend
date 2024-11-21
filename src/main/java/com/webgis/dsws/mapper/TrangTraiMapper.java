package com.webgis.dsws.mapper;

import com.webgis.dsws.dto.TrangTraiImportDTO;
import com.webgis.dsws.exception.DataImportException;
import com.webgis.dsws.model.DonViHanhChinh;
import com.webgis.dsws.model.TrangTrai;
import com.webgis.dsws.model.TrangTraiBenh;
import com.webgis.dsws.repository.DonViHanhChinhRepository;
import com.webgis.dsws.service.AddressService;
import com.webgis.dsws.service.impl.BenhServiceImpl;
import com.webgis.dsws.util.StringUtils;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TrangTraiMapper {
    private final WKTReader wktReader;
    private final DonViHanhChinhRepository donViHanhChinhRepository;
    private final WKBReader wkbReader = new WKBReader();
    private final AddressService addressService;
    private final BenhServiceImpl benhService;

    public TrangTrai toEntity(TrangTraiImportDTO dto) {
        TrangTrai entity = new TrangTrai();
        // ID được sinh tự động
        entity.setTenChu(dto.getChuCoSo());
        entity.setSoDienThoai(dto.getDienThoai());
        entity.setSoNha(dto.getSoNha());
        entity.setKhuPho(dto.getKhuPho());
        entity.setDiaChiDayDu(dto.getDiaChi());
        entity.setTongDan(dto.getTongDan());

        // Handle geometry conversion
        Point point = convertGeometry(dto.getGeomWKB());
        entity.setPoint(point);

        // Find and set administrative unit
        DonViHanhChinh donViHanhChinh = findDonViHanhChinh(dto.getTenXaPhuong());
        entity.setDonViHanhChinh(donViHanhChinh);

        // Generate full address
        String fullAddress = addressService.generateFullAddress(
                dto.getSoNha(),
                dto.getTenDuong(),
                dto.getKhuPho(),
                donViHanhChinh);

        // Update address with point information if needed
        fullAddress = addressService.updateAddressWithPoint(fullAddress, point, donViHanhChinh);
        entity.setDiaChiDayDu(fullAddress);

        // Set<TrangTraiBenh> trangTraiBenhs =
        // benhService.processBenhList(dto.getLoaiBenh(), entity);
        // entity.setTrangTraiBenhs(trangTraiBenhs);

        return entity;
    }

    private Point convertGeometry(String geomWKT) {
        try {
            // First try WKB conversion
            byte[] wkbBytes = hexStringToByteArray(geomWKT);
            return (Point) wkbReader.read(wkbBytes);
        } catch (Exception e) {
            try {
                // If WKB fails, try WKT
                return (Point) wktReader.read(geomWKT);
            } catch (Exception ex) {
                throw new DataImportException("Không thể chuyển đổi geometry. Dữ liệu: " + geomWKT, ex);
            }
        }
    }

    // Chuyển chuỗi hex sang mảng byte
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        // Chuyển đổi từng cặp ký tự hex thành byte
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private DonViHanhChinh findDonViHanhChinh(String tenPhuong) {
        // Tìm theo tên chính xác
        DonViHanhChinh donViHanhChinh = donViHanhChinhRepository.findByTen(tenPhuong);

        if (donViHanhChinh == null) {
            // Thử tìm theo tên giống nhất
            List<DonViHanhChinh> matches = donViHanhChinhRepository
                    .findByTenContainingIgnoreCaseAndDiacritics(tenPhuong);

            if (!matches.isEmpty()) {
                // Lấy đơn vị hành chính có tên giống nhất (dựa trên khoảng cách Levenshtein)
                donViHanhChinh = matches.stream()
                        .min((a, b) -> {
                            String normalizedInput = StringUtils.normalize(tenPhuong);
                            String normalizedA = StringUtils.normalize(a.getTen());
                            String normalizedB = StringUtils.normalize(b.getTen());

                            int diffA = levenshteinDistance(normalizedInput, normalizedA);
                            int diffB = levenshteinDistance(normalizedInput, normalizedB);

                            return Integer.compare(diffA, diffB);
                        })
                        .orElse(null);
            }
        }

        return donViHanhChinh;
    }

    // Tính khoảng cách Levenshtein giữa 2 chuỗi
    private int levenshteinDistance(String s1, String s2) {
        // Tạo mảng 2 chiều để lưu kết quả
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                            dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }
}