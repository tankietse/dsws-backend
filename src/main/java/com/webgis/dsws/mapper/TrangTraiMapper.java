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
     * Chuyển đổi từ DTO sang Entity TrangTrai.
     * Xử lý toàn bộ quá trình chuyển đổi dữ liệu bao gồm:
     * - Tạo thông tin cơ bản trang trại
     * - Xử lý dữ liệu địa lý và hành chính
     * - Xử lý thông tin vật nuôi
     * - Xử lý thông tin bệnh
     */
    @Transactional
    public TrangTrai toEntity(TrangTraiImportDTO dto) {
        TrangTrai trangTrai = createBasicTrangTrai(dto);

        // Handle geographic and administrative data
        setupGeographicData(trangTrai, dto);

        // Process animal types and distributions
        processAnimalTypes(trangTrai, dto);

        // Process diseases
        processDiseases(trangTrai, dto);

        return trangTrai;
    }

    /**
     * Tạo đối tượng TrangTrai với các thông tin cơ bản.
     * Thiết lập các thuộc tính cơ bản như mã, tên chủ, địa chỉ, số điện thoại...
     */
    private TrangTrai createBasicTrangTrai(TrangTraiImportDTO dto) {
        TrangTrai trangTrai = new TrangTrai();

        trangTrai.setMaTrangTrai(dto.getMaSo());
        trangTrai.setTenChu(dto.getChuCoSo());
        trangTrai.setSoDienThoai(dto.getDienThoai());
        trangTrai.setSoNha(dto.getSoNha());
        trangTrai.setKhuPho(dto.getKhuPho());
        trangTrai.setDiaChiDayDu(dto.getDiaChi());
        trangTrai.setTongDan(dto.getSoLuong());
        trangTrai.setTenDuong(dto.getTenDuong());
        trangTrai.setNgayTao(LocalDateTime.now());
        trangTrai.setNgayCapNhat(LocalDateTime.now());
        trangTrai.setTrangThaiHoatDong(true);

        return trangTrai;
    }

    /**
     * Xử lý và thiết lập dữ liệu địa lý cho trang trại.
     * - Chuyển đổi tọa độ địa lý
     * - Thiết lập đơn vị hành chính
     * - Tạo địa chỉ đầy đủ
     */
    private void setupGeographicData(TrangTrai trangTrai, TrangTraiImportDTO dto) {
        // Convert and set geometry
        Point point = geometryService.convertGeometry(dto.getGeomWKB());
        trangTrai.setPoint(point);

        // Set administrative unit
        DonViHanhChinh donViHanhChinh = findDonViHanhChinh(dto.getTenXaPhuong());
        trangTrai.setDonViHanhChinh(donViHanhChinh);

        // Generate and update full address
        String fullAddress = addressService.generateFullAddress(
                dto.getSoNha(),
                dto.getTenDuong(),
                dto.getKhuPho(),
                donViHanhChinh);
        fullAddress = addressService.updateAddressWithPoint(fullAddress, point, donViHanhChinh);
        trangTrai.setDiaChiDayDu(fullAddress);
    }

    /**
     * Xử lý thông tin về các loại vật nuôi.
     * - Phân tích danh sách loại vật nuôi
     * - Tính toán phân bổ số lượng
     * - Tạo các bản ghi liên kết giữa trang trại và vật nuôi
     */
    private void processAnimalTypes(TrangTrai trangTrai, TrangTraiImportDTO dto) {
        Set<String> loaiVatNuoiNames = parseNames(dto.getChungLoai());
        Set<LoaiVatNuoi> loaiVatNuois = loaiVatNuoiImportProcessor.processAndSave(loaiVatNuoiNames);

        // Calculate distribution
        Integer totalAnimals = trangTrai.getTongDan();
        int numAnimalTypes = loaiVatNuois.size();
        int averageSoLuong = (totalAnimals != null && numAnimalTypes > 0) ? totalAnimals / numAnimalTypes : 0;
        int remainder = (totalAnimals != null && numAnimalTypes > 0) ? totalAnimals % numAnimalTypes : 0;

        // Create TrangTraiVatNuoi entries
        Set<TrangTraiVatNuoi> trangTraiVatNuois = distributeAnimals(trangTrai, loaiVatNuois, averageSoLuong, remainder);
        trangTrai.setTrangTraiVatNuois(trangTraiVatNuois);
    }

    /**
     * Phân bổ số lượng vật nuôi cho từng loại.
     * Tạo các đối tượng TrangTraiVatNuoi với số lượng được phân bổ đều
     */
    private Set<TrangTraiVatNuoi> distributeAnimals(TrangTrai trangTrai, Set<LoaiVatNuoi> loaiVatNuois,
            int averageSoLuong, int remainder) {
        Set<TrangTraiVatNuoi> trangTraiVatNuois = new HashSet<>();
        int index = 0;

        for (LoaiVatNuoi loaiVatNuoi : loaiVatNuois) {
            TrangTraiVatNuoi trangTraiVatNuoi = new TrangTraiVatNuoi();
            trangTraiVatNuoi.setTrangTrai(trangTrai);
            trangTraiVatNuoi.setLoaiVatNuoi(loaiVatNuoi);
            trangTraiVatNuoi.setSoLuong(averageSoLuong + (index < remainder ? 1 : 0));
            trangTraiVatNuois.add(trangTraiVatNuoi);
            index++;
        }

        return trangTraiVatNuois;
    }

    /**
     * Xử lý thông tin về các bệnh của vật nuôi.
     * - Tạo danh sách ca bệnh
     * - Thiết lập mối quan hệ giữa bệnh và vật nuôi
     */
    private void processDiseases(TrangTrai trangTrai, TrangTraiImportDTO dto) {
        Set<CaBenh> danhSachCaBenh = benhService.processBenhList(dto.getLoaiBenh(), trangTrai);
        trangTrai.setCaBenhs(danhSachCaBenh);

        processBenhVatNuoi(danhSachCaBenh);
    }

    /**
     * Xử lý và lưu trữ mối quan hệ giữa bệnh và loại vật nuôi.
     * Tạo các bản ghi BenhVatNuoi nếu chưa tồn tại
     */
    private void processBenhVatNuoi(Set<CaBenh> danhSachCaBenh) {
        danhSachCaBenh.forEach(caBenh -> {
            Benh benh = caBenh.getBenh();
            TrangTraiVatNuoi trangTraiVatNuoi = caBenh.getTrangTrai().getTrangTraiVatNuois().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "No TrangTraiVatNuoi found for TrangTrai: " + caBenh.getTrangTrai().getId()));

            if (!benhVatNuoiRepository.existsByBenhAndLoaiVatNuoi(benh, trangTraiVatNuoi.getLoaiVatNuoi())) {
                BenhVatNuoi benhVatNuoi = new BenhVatNuoi();
                benhVatNuoi.setBenh(benh);
                benhVatNuoi.setLoaiVatNuoi(trangTraiVatNuoi.getLoaiVatNuoi());
                benhVatNuoiRepository.save(benhVatNuoi);
            }
        });
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