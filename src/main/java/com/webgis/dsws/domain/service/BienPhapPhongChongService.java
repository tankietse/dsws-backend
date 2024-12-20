package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.model.BienPhapPhongChong;
import com.webgis.dsws.domain.model.enums.MucDoBienPhapEnum;
import com.webgis.dsws.domain.model.enums.MucDoVungDichEnum;
import com.webgis.dsws.domain.repository.BienPhapPhongChongRepository;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

/**
 * /**
 * Service xử lý các biện pháp phòng chống dịch bệnh.
 * Tuân thủ Single Responsibility Principle - chỉ tập trung vào logic biện pháp
 * phòng chống.
 */
@Service
public class BienPhapPhongChongService {

    @Autowired
    private BienPhapPhongChongRepository bienPhapPhongChongRepository;

    /**
     * Tạo danh sách biện pháp phòng chống mặc định dựa trên mức độ nghiêm trọng
     * 
     * @param severity Mức độ nghiêm trọng của vùng dịch
     * @return Tập hợp các biện pháp phòng chống
     */
    public Set<BienPhapPhongChong> getDefaultPreventiveMeasures(MucDoVungDichEnum severity) {
        Set<BienPhapPhongChong> measures = new HashSet<>();
        measures.addAll(getBasicMeasures());
        measures.addAll(MucDoBienPhapEnum.valueOf(severity.name()).getMeasures());
        return measures;
    }

    public Set<BienPhapPhongChong> saveAll(Set<BienPhapPhongChong> bienPhaps) {
        return new HashSet<>(bienPhapPhongChongRepository.saveAllAndFlush(bienPhaps));
    }

    private Set<BienPhapPhongChong> getBasicMeasures() {
        return Set.of(
                createMeasure(
                        "Giám sát dịch bệnh",
                        "Theo dõi và báo cáo tình hình dịch bệnh hàng ngày",
                        1));
    }

    private BienPhapPhongChong createMeasure(String ten, String moTa, int thuTuUuTien) {
        BienPhapPhongChong measure = new BienPhapPhongChong();
        measure.setTenBienPhap(ten);
        measure.setMoTa(moTa);
        measure.setThuTuUuTien(thuTuUuTien);
        return measure;
    }
}