package com.webgis.dsws.mapper;

import com.webgis.dsws.util.StringUtils;
import com.webgis.dsws.domain.model.*;
import com.webgis.dsws.dto.TrangTraiImportDTO;
import com.webgis.dsws.exception.DataImportException;
import com.webgis.dsws.domain.repository.DonViHanhChinhRepository;
import com.webgis.dsws.domain.service.impl.BenhServiceImpl;
import com.webgis.dsws.domain.service.importer.AddressService;
import com.webgis.dsws.domain.service.importer.LoaiVatNuoiImportProcessor;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TrangTraiMapper {
    private final WKTReader wktReader;
    private final DonViHanhChinhRepository donViHanhChinhRepository;
    private final WKBReader wkbReader = new WKBReader();
    private final AddressService addressService;
    private final BenhServiceImpl benhService;
    private final LoaiVatNuoiImportProcessor loaiVatNuoiImportProcessor;

    @Transactional
    public TrangTrai toEntity(TrangTraiImportDTO dto) {
        TrangTrai trangTraiEntity = new TrangTrai();
        // TrangTraiBenh trangTraiBenhEntity = new TrangTraiBenh();
        // Benh benhEntity = new Benh();

        // ID được sinh tự động

        // TODO: Cập nhật thông tin từ DTO vào entity
        trangTraiEntity.setMaTrangTrai(dto.getMaSo());
        trangTraiEntity.setTenChu(dto.getChuCoSo());
        trangTraiEntity.setSoDienThoai(dto.getDienThoai());
        trangTraiEntity.setSoNha(dto.getSoNha());
        trangTraiEntity.setKhuPho(dto.getKhuPho());
        trangTraiEntity.setDiaChiDayDu(dto.getDiaChi());
        trangTraiEntity.setTongDan(dto.getSoLuong());
        // trangTraiEntity.setTenTrangTrai(dto.getTenTrangTrai());
        // trangTraiEntity.setEmail(dto.getEmail());
        trangTraiEntity.setTenDuong(dto.getTenDuong());
        // trangTraiEntity.setDienTich(dto.getDienTich());
        // trangTraiEntity.setPhuongThucChanNuoi(dto.getPhuongThucChanNuoi());
        // trangTraiEntity.setNguoiQuanLy(dto.getNguoiQuanLy());
        trangTraiEntity.setNgayTao(LocalDateTime.now());
        trangTraiEntity.setNgayCapNhat(LocalDateTime.now());
        trangTraiEntity.setTrangThaiHoatDong(true);

        // Handle geometry conversion
        Point point = convertGeometry(dto.getGeomWKB());
        trangTraiEntity.setPoint(point);

        // Find and set administrative unit
        DonViHanhChinh donViHanhChinh = findDonViHanhChinh(dto.getTenXaPhuong());
        trangTraiEntity.setDonViHanhChinh(donViHanhChinh);

        // Generate full address
        String fullAddress = addressService.generateFullAddress(
                dto.getSoNha(),
                dto.getTenDuong(),
                dto.getKhuPho(),
                donViHanhChinh);

        // Update address with point information if needed
        fullAddress = addressService.updateAddressWithPoint(fullAddress, point, donViHanhChinh);
        trangTraiEntity.setDiaChiDayDu(fullAddress);

        // Process LoaiVatNuoi
        Set<String> loaiVatNuoiNames = parseNames(dto.getChungLoai());
        Set<LoaiVatNuoi> loaiVatNuois = loaiVatNuoiImportProcessor.processAndSave(loaiVatNuoiNames);

        // Get total number of animals
        Integer totalAnimals = trangTraiEntity.getTongDan();
        int numAnimalTypes = loaiVatNuois.size();

        // Distribute animals equally if possible
        int averageSoLuong = (totalAnimals != null && numAnimalTypes > 0) ? totalAnimals / numAnimalTypes : 0;
        int remainder = (totalAnimals != null && numAnimalTypes > 0) ? totalAnimals % numAnimalTypes : 0;

        Set<TrangTraiVatNuoi> trangTraiVatNuois = new HashSet<>();
        int index = 0;
        for (LoaiVatNuoi loaiVatNuoi : loaiVatNuois) {
            TrangTraiVatNuoi trangTraiVatNuoi = new TrangTraiVatNuoi();
            trangTraiVatNuoi.setTrangTrai(trangTraiEntity);
            trangTraiVatNuoi.setLoaiVatNuoi(loaiVatNuoi);
            trangTraiVatNuoi.setSoLuong(averageSoLuong + (index < remainder ? 1 : 0));
            trangTraiVatNuois.add(trangTraiVatNuoi);
            index++;
        }
        trangTraiEntity.setTrangTraiVatNuois(trangTraiVatNuois);

        // Process Benh
        Set<CaBenh> danhSachCaBenh = benhService.processBenhList(dto.getLoaiBenh(), trangTraiEntity);
        trangTraiEntity.setCaBenhs(danhSachCaBenh);

        return trangTraiEntity;
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

    private Set<String> parseNames(String namesStr) {
        if (namesStr == null || namesStr.trim().isEmpty()) {
            return Collections.emptySet();
        }
        return Arrays.stream(namesStr.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toSet());
    }
}