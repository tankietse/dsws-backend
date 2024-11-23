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

@Component
@RequiredArgsConstructor
public class BenhProcessor {
    private final LoaiVatNuoiRepository loaiVatNuoiRepository;

    public void processBenh(Benh benh) {
        // Get standardized Vietnamese name
        String stdName = BenhRegistry.getVietnameseName(benh.getTenBenh());
        benh.setTenBenh(stdName);

        // Get disease categories from registry
        BenhRegistry.DiseaseInfo info = BenhRegistry.getInfo(stdName);
        if (info != null) {
            Set<MucDoBenhEnum> categories = info.getCategories();
            benh.setMucDoBenhs(categories);
            
            // Update flags based on categories
            benh.setCanCongBoDich(categories.contains(MucDoBenhEnum.BANG_A) 
                || categories.contains(MucDoBenhEnum.BANG_B));
            
            benh.setCanPhongBenhBatBuoc(categories.contains(MucDoBenhEnum.PHONG_BENH_BAT_BUOC));

            // Update affected animals
            Set<String> affectedAnimals = info.getAffectedAnimals();
            benh.setLoaiVatNuoi(new HashSet<>(loaiVatNuoiRepository.findAllByTenLoaiIn(affectedAnimals)));
        }
    }
}