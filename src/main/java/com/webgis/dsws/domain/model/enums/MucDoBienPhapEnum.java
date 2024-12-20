package com.webgis.dsws.domain.model.enums;

import com.webgis.dsws.domain.model.BienPhapPhongChong;
import java.util.Set;

public enum MucDoBienPhapEnum {
    CAP_DO_1(Set.of(
        createMeasure("Tăng cường giám sát", "Kiểm tra sức khỏe vật nuôi thường xuyên", 1),
        createMeasure("Vệ sinh chuồng trại", "Thực hiện vệ sinh, khử trùng chuồng trại định kỳ", 1)
    )),
    CAP_DO_2(Set.of(
        createMeasure("Cách ly vật nuôi", "Cách ly vật nuôi nghi nhiễm và tiếp xúc", 2),
        createMeasure("Khử trùng định kỳ", "Phun thuốc khử trùng khu vực nghi nhiễm, 1 lần/ngày", 2)
    )),
    CAP_DO_3(Set.of(
        createMeasure("Kiểm soát vận chuyển", "Hạn chế vận chuyển vật nuôi ra/vào vùng dịch", 3),
        createMeasure("Khử trùng tăng cường", "Phun thuốc khử trùng toàn bộ khu vực, 2 lần/ngày", 3)
    )),
    CAP_DO_4(Set.of(
        createMeasure("Phong tỏa hoàn toàn", "Cấm vận chuyển, mua bán vật nuôi trong vùng dịch", 4),
        createMeasure("Tiêu hủy bắt buộc", "Tiêu hủy toàn bộ vật nuôi nhiễm bệnh và có nguy cơ", 4)
    ));

    private final Set<BienPhapPhongChong> measures;

    MucDoBienPhapEnum(Set<BienPhapPhongChong> measures) {
        this.measures = measures;
    }

    public Set<BienPhapPhongChong> getMeasures() {
        return measures;
    }

    private static BienPhapPhongChong createMeasure(String ten, String moTa, int thuTuUuTien) {
        BienPhapPhongChong measure = new BienPhapPhongChong();
        measure.setTenBienPhap(ten);
        measure.setMoTa(moTa);
        measure.setThuTuUuTien(thuTuUuTien);
        return measure;
    }
}