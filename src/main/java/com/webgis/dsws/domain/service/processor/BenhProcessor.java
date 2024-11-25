package com.webgis.dsws.domain.service.processor;

import com.webgis.dsws.domain.model.Benh;
import com.webgis.dsws.domain.model.constant.BenhRegistry;
import com.webgis.dsws.domain.model.enums.MucDoBenhEnum;
import com.webgis.dsws.domain.repository.LoaiVatNuoiRepository;
import com.webgis.dsws.domain.model.TrangTraiVatNuoi;
import com.webgis.dsws.domain.model.CaBenh;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
     * Tạo ca bệnh mới với số lượng nhiễm ngẫu nhiên
     * 
     * @param benh             thông tin bệnh
     * @param trangTraiVatNuoi thông tin vật nuôi trong trang trại
     * @return đối tượng ca bệnh mới
     */
    public CaBenh createInitialCaBenh(Benh benh, TrangTraiVatNuoi trangTraiVatNuoi) {
        int totalAnimals = trangTraiVatNuoi.getSoLuong();
        int initialInfections = calculateInitialInfections(totalAnimals);

        CaBenh caBenh = new CaBenh();
        caBenh.setBenh(benh);
        caBenh.setTrangTrai(trangTraiVatNuoi.getTrangTrai());
        caBenh.setNgayPhatHien(new Date());
        caBenh.setSoCaNhiemBanDau(initialInfections);
        // TODO: Gán diễn biến ca bệnh
        caBenh.setDaKetThuc(false);

        return caBenh;
    }
}