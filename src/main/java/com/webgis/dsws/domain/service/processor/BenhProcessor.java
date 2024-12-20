package com.webgis.dsws.domain.service.processor;

import com.webgis.dsws.domain.model.Benh;
import com.webgis.dsws.domain.model.constant.BenhRegistry;
import com.webgis.dsws.domain.model.enums.MucDoBenhEnum;
import com.webgis.dsws.domain.model.enums.TrangThaiEnum;
import com.webgis.dsws.domain.repository.LoaiVatNuoiRepository;
import com.webgis.dsws.domain.model.TrangTraiVatNuoi;
import com.webgis.dsws.domain.model.CaBenh;
import com.webgis.dsws.domain.model.BenhVatNuoi;
import com.webgis.dsws.domain.model.DienBienCaBenh;
import com.webgis.dsws.domain.service.DienBienCaBenhService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.Random;
import java.util.Date;

/**
 * Lớp BenhProcessor chịu trách nhiệm xử lý thông tin bệnh.
 * Nó chuẩn hóa tên bệnh, cập nhật các danh mục bệnh, và cập nhật thông tin về
 * các loài vật nuôi bị ảnh hưởng.
 */
@Component
@RequiredArgsConstructor
public class BenhProcessor {
    private final LoaiVatNuoiRepository loaiVatNuoiRepository;
    private final DienBienCaBenhService dienBienCaBenhService;
    private final Random random = new Random();

    /**
     * Xử lý thông tin bệnh.
     * 
     * @param benh đối tượng Benh cần được xử lý.
     */
    public void processBenh(Benh benh) {
        // Lấy tên bệnh chuẩn hóa bằng tiếng Việt
        String stdName = BenhRegistry.getVietnameseName(benh.getTenBenh());
        benh.setTenBenh(stdName);

        // Lấy các danh mục bệnh từ registry
        BenhRegistry.DiseaseInfo info = BenhRegistry.getInfo(stdName);
        if (info != null) {
            Set<MucDoBenhEnum> categories = info.getCategories();
            benh.setMucDoBenhs(categories);

            // Cập nhật các cờ dựa trên danh mục bệnh
            benh.setCanCongBoDich(categories.contains(MucDoBenhEnum.BANG_A)
                    || categories.contains(MucDoBenhEnum.BANG_B));

            benh.setCanPhongBenhBatBuoc(categories.contains(MucDoBenhEnum.PHONG_BENH_BAT_BUOC));

            // Cập nhật các loài vật nuôi bị ảnh hưởng
            Set<String> affectedAnimals = info.getAffectedAnimals();
            benh.setLoaiVatNuoi(new HashSet<>(loaiVatNuoiRepository.findAllByTenLoaiIn(affectedAnimals)));
        }
    }

    /**
     * Tính toán số ca nhiễm ban đầu dựa trên tổng số vật nuôi
     * Cho phép tỷ lệ nhiễm từ 10-90% tổng đàn tùy theo mức độ nghiêm trọng
     * 
     * @param totalAnimals tổng số vật nuôi
     * @return số ca nhiễm ban đầu
     */
    private int calculateInitialInfections(int totalAnimals) {
        // Tỷ lệ nhiễm từ 10-90%
        double baseInfectionRate = 0.50 + random.nextDouble() * 0.80;

        // Tính số ca nhiễm dự kiến
        int estimatedCases = (int) Math.round(totalAnimals * baseInfectionRate);

        // Đảm bảo có ít nhất 1 ca nhiễm và không quá 90% tổng đàn
        return Math.max(1, Math.min(estimatedCases, (int) (totalAnimals * 0.9)));
    }

    /**
     * Tạo mới một ca bệnh ban đầu cho trang trại vật nuôi.
     *
     * @param benh             Thông tin bệnh.
     * @param trangTraiVatNuoi Thông tin trang trại vật nuôi.
     * @return Ca bệnh ban đầu.
     *
     *         Tính toán tỷ lệ tử vong từ thông tin bệnh:
     *         - Lấy thông tin bệnh vật nuôi tương ứng từ danh sách bệnh vật nuôi.
     *         - Tính số lượng vật nuôi ban đầu bị nhiễm bệnh.
     *         - Tính tỷ lệ tử vong dựa trên thông tin bệnh vật nuôi, nếu không có
     *         thông tin thì mặc định là 0.1 (10%).
     *         - Tính số lượng vật nuôi ban đầu tử vong dựa trên tỷ lệ tử vong.
     */
    @Transactional
    public CaBenh createInitialCaBenh(Benh benh, TrangTraiVatNuoi trangTraiVatNuoi) {
        // Calculate initial stats based on animals and disease severity
        BenhVatNuoi benhVatNuoi = benh.getBenhVatNuois().stream()
                .filter(bvn -> bvn.getLoaiVatNuoi().getId().equals(trangTraiVatNuoi.getLoaiVatNuoi().getId()))
                .findFirst()
                .orElse(null);

        int totalAnimals = trangTraiVatNuoi.getSoLuong();
        int initialInfections = calculateInitialInfections(totalAnimals);
        float deathRate = (benhVatNuoi != null) ? benhVatNuoi.getTiLeChet() : 0.1f;
        int initialDeaths = (int) Math.round(initialInfections * deathRate);

        CaBenh caBenh = new CaBenh();
        caBenh.setBenh(benh);
        caBenh.setTrangTrai(trangTraiVatNuoi.getTrangTrai());
        caBenh.setNgayPhatHien(new Date());
        caBenh.setSoCaNhiemBanDau(initialInfections);
        caBenh.setSoCaTuVongBanDau(initialDeaths);
        caBenh.setMoTaBanDau(generateInitialDescription(benh, initialInfections, initialDeaths));
        caBenh.setTrangThai(TrangThaiEnum.PENDING);
        caBenh.setDaKetThuc(false);
        caBenh.setNguyenNhanDuDoan(generateCauseDescription(benh));

        // Initialize dienBienCaBenhs collection
        caBenh.setDienBienCaBenhs(new HashSet<>());

        return caBenh;
    }

    private String generateInitialDescription(Benh benh, int infections, int deaths) {
        return String.format("Phát hiện ổ dịch %s với %d ca nhiễm, %d ca tử vong",
                benh.getTenBenh(), infections, deaths);
    }

    private String generateCauseDescription(Benh benh) {
        return String.format("Nghi ngờ do %s",
                benh.getTacNhanGayBenh() != null ? benh.getTacNhanGayBenh() : "chưa xác định");
    }

    private DienBienCaBenh createInitialDienBienCaBenh(CaBenh caBenh, int initialInfections, int initialDeaths) {
        DienBienCaBenh dienBien = new DienBienCaBenh();
        dienBien.setCaBenh(caBenh);
        dienBien.setNgayCapNhat(new Date());
        dienBien.setSoCaNhiemMoi(initialInfections);
        dienBien.setSoCaTuVong(initialDeaths);
        dienBien.setSoCaKhoi(0);
        dienBien.setBienPhapXuLy("Phát hiện và cách ly ban đầu kèm theo dõi sức khỏe");
        dienBien.setNguoiCapNhat(caBenh.getNguoiTao());
        dienBien.setKetQuaXuLy("Đang theo dõi");

        return dienBien;
    }
}