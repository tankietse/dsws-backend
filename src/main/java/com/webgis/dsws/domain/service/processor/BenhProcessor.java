package com.webgis.dsws.domain.service.processor;

import com.webgis.dsws.domain.model.Benh;
import com.webgis.dsws.domain.model.LoaiVatNuoi;
import com.webgis.dsws.domain.model.constant.BenhRegistry;
import com.webgis.dsws.domain.model.enums.MucDoBenhEnum;
import com.webgis.dsws.domain.repository.LoaiVatNuoiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Lớp BenhProcessor chịu trách nhiệm xử lý thông tin bệnh.
 * Nó chuẩn hóa tên bệnh, cập nhật các danh mục bệnh, và cập nhật thông tin về các loài vật nuôi bị ảnh hưởng.
 */
@Component
@RequiredArgsConstructor
public class BenhProcessor {
    private final LoaiVatNuoiRepository loaiVatNuoiRepository;

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
}