package com.webgis.dsws.mapper;

import com.webgis.dsws.domain.repository.BenhVatNuoiRepository;
import com.webgis.dsws.util.StringUtils;
import com.webgis.dsws.domain.model.*;
import com.webgis.dsws.dto.TrangTraiImportDTO;
import com.webgis.dsws.domain.repository.DonViHanhChinhRepository;
import com.webgis.dsws.domain.service.impl.BenhServiceImpl;
import com.webgis.dsws.domain.service.importer.AddressService;
import com.webgis.dsws.domain.service.importer.LoaiVatNuoiImportProcessor;
import com.webgis.dsws.domain.service.GeometryService;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * Lớp chuyển đổi dữ liệu từ DTO sang Entity cho TrangTrai.
 * Xử lý việc import dữ liệu trang trại từ file, bao gồm:
 * - Chuyển đổi thông tin cơ bản của trang trại
 * - Xử lý dữ liệu địa lý (geometry)
 * - Xử lý thông tin đơn vị hành chính
 * - Phân bổ số lượng vật nuôi
 * - Xử lý thông tin bệnh và ca bệnh
 */
@Component
@RequiredArgsConstructor
public class TrangTraiMapper {
    private final DonViHanhChinhRepository donViHanhChinhRepository;
    private final AddressService addressService;
    private final BenhServiceImpl benhService;
    private final LoaiVatNuoiImportProcessor loaiVatNuoiImportProcessor;
    private final GeometryService geometryService;
    private final BenhVatNuoiRepository benhVatNuoiRepository;

    /**
     * Chuyển đổi từ DTO sang Entity TrangTrai
     * 
     * @param dto Đối tượng DTO chứa dữ liệu import
     * @return Đối tượng TrangTrai đã được chuyển đổi và xử lý
     */
    @Transactional
    public TrangTrai toEntity(TrangTraiImportDTO dto) {
        // Khởi tạo đối tượng TrangTrai mới
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

        // Xử lý chuyển đổi geometry
        Point point = geometryService.convertGeometry(dto.getGeomWKB());
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

        // Xử lý phân bổ số lượng vật nuôi
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

        // // Xử lý thông tin bệnh và tạo ca bệnh
        // if (dto.getLoaiBenh() != null && !dto.getLoaiBenh().trim().isEmpty()) {
        // // Remove duplicate disease names
        // Set<String> uniqueBenhNames = Arrays.stream(dto.getLoaiBenh().split(","))
        // .map(String::trim)
        // .filter(name -> !name.isEmpty())
        // .collect(Collectors.toSet());

        // // Find or create Benh in batch
        // Set<Benh> benhSet = benhService.findOrCreateBenhBatch(uniqueBenhNames);

        // // Create disease cases efficiently
        // Set<CaBenh> danhSachCaBenh = benhSet.stream()
        // .flatMap(benh -> trangTraiVatNuois.stream()
        // .filter(trangTraiVatNuoi -> benh.getLoaiVatNuoi()
        // .contains(trangTraiVatNuoi.getLoaiVatNuoi()))
        // .map(trangTraiVatNuoi -> benhProcessor.createInitialCaBenh(benh,
        // trangTraiVatNuoi)))
        // .filter(Objects::nonNull)
        // .collect(Collectors.toSet());

        // trangTraiEntity.setCaBenhs(danhSachCaBenh);

        // // Associate diseases with animal types in benh_vatnuoi
        // benhSet.forEach(benh -> {
        // benh.getLoaiVatNuoi().forEach(loaiVatNuoi -> {
        // BenhVatNuoi benhVatNuoi = new BenhVatNuoi();
        // benhVatNuoi.setBenh(benh);
        // benhVatNuoi.setLoaiVatNuoi(loaiVatNuoi);
        // benhVatNuoiRepository.save(benhVatNuoi);
        // });
        // });
        // }

        // Process Benh and BenhVatNuoi relationships

        String loaiBenh = dto.getLoaiBenh();
        Set<CaBenh> danhSachCaBenh = benhService.processBenhList(loaiBenh, trangTraiEntity);
        trangTraiEntity.setCaBenhs(danhSachCaBenh);

        // Process BenhVatNuoi relationships
        danhSachCaBenh.forEach(caBenh -> {
            Benh benh = caBenh.getBenh();
            TrangTraiVatNuoi trangTraiVatNuoi = caBenh.getTrangTrai().getTrangTraiVatNuois().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "No TrangTraiVatNuoi found for TrangTrai: " + caBenh.getTrangTrai().getId()));

            // Create BenhVatNuoi if it doesn't exist
            if (!benhVatNuoiRepository.existsByBenhAndLoaiVatNuoi(benh, trangTraiVatNuoi.getLoaiVatNuoi())) {
                BenhVatNuoi benhVatNuoi = new BenhVatNuoi();
                benhVatNuoi.setBenh(benh);
                benhVatNuoi.setLoaiVatNuoi(trangTraiVatNuoi.getLoaiVatNuoi());
                benhVatNuoiRepository.save(benhVatNuoi);
            }
        });

        return trangTraiEntity;
    }

    /**
     * Chuyển đổi chuỗi hex thành mảng byte
     * 
     * @param s Chuỗi hex cần chuyển đổi
     * @return Mảng byte tương ứng
     */
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

    /**
     * Tìm đơn vị hành chính dựa trên tên
     * Sử dụng thuật toán Levenshtein để tìm tên gần đúng nếu không tìm thấy tên
     * chính xác
     * 
     * @param tenPhuong Tên phường/xã cần tìm
     * @return Đơn vị hành chính tương ứng hoặc null nếu không tìm thấy
     */
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

    /**
     * Tính khoảng cách Levenshtein giữa hai chuỗi
     * Sử dụng để so sánh độ tương đồng của tên đơn vị hành chính
     * 
     * @param s1 Chuỗi thứ nhất
     * @param s2 Chuỗi thứ hai
     * @return Khoảng cách Levenshtein giữa hai chuỗi
     */
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

    /**
     * Phân tách chuỗi tên thành tập hợp các tên riêng biệt
     * Loại bỏ khoảng trắng thừa và các tên rỗng
     * 
     * @param namesStr Chuỗi tên cần phân tách, các tên cách nhau bởi dấu phẩy
     * @return Tập hợp các tên đã được xử lý
     */
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