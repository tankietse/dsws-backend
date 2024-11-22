package com.webgis.dsws.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import com.webgis.dsws.model.VungDich;
import com.webgis.dsws.model.enums.MucDoVungDichEnum;
import com.webgis.dsws.repository.VungDichRepository;
import com.webgis.dsws.model.BienPhapPhongChong;

import jakarta.persistence.EntityNotFoundException;

@Service
@Validated
public class VungDichService {
    private final VungDichRepository vungDichRepository;
    private final GeometryFactory geometryFactory;

    @Autowired
    public VungDichService(VungDichRepository vungDichRepository) {
        this.vungDichRepository = vungDichRepository;
        this.geometryFactory = new GeometryFactory();
    }

    /**
     * Kiểm tra xem một tọa độ có nằm trong vùng dịch hay không.
     * 
     * @param vungDichId ID của vùng dịch.
     * @param coordinate Tọa độ cần kiểm tra.
     * @return true nếu tọa độ nằm trong vùng dịch, ngược lại false.
     */
    public boolean contains(Long vungDichId, Coordinate coordinate) {
        VungDich vungDich = vungDichRepository.findById(vungDichId).orElse(null);
        if (vungDich != null && vungDich.getGeom() != null) {
            Point point = geometryFactory.createPoint(coordinate);
            return vungDich.getGeom().contains(point);
        }
        return false;
    }

    /**
     * Lấy thông tin vùng dịch theo ID
     */
    public VungDich getVungDichById(Long id) {
        return vungDichRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vùng dịch không tồn tại với ID: " + id));
    }

    /**
     * Cập nhật thông tin vùng dịch
     */
    public VungDich updateVungDich(Long id, VungDich vungDichDetails) {
        VungDich vungDich = getVungDichById(id);
        vungDich.setTenVung(vungDichDetails.getTenVung());
        vungDich.setMucDo(vungDichDetails.getMucDo());
        vungDich.setGeom(vungDichDetails.getGeom());
        vungDich.setBanKinh(vungDichDetails.getBanKinh());
        return vungDichRepository.save(vungDich);
    }

    /**
     * Xóa vùng dịch
     */
    public void deleteVungDich(Long id) {
        VungDich vungDich = getVungDichById(id);
        vungDichRepository.delete(vungDich);
    }

    /**
     * Phương thức cảnh báo nếu vùng dịch đang ở mức nghiêm trọng.
     * 
     * @param vungDichId ID của vùng dịch.
     * @return Thông báo cảnh báo.
     */
    public String canhBaoMucDo(Long vungDichId) {
        VungDich vungDich = vungDichRepository.findById(vungDichId).orElse(null);
        if (vungDich != null) {
            if (vungDich.getMucDo() == MucDoVungDichEnum.CAP_DO_4) {
                return "Cảnh báo: Vùng dịch " + vungDich.getTenVung() + " đang ở mức nghiêm trọng.";
            }
            return "Vùng dịch " + vungDich.getTenVung() + " an toàn.";
        }
        return "Vùng dịch không tồn tại.";
    }

    /**
     * Thêm biện pháp phòng chống mới cho vùng dịch.
     * 
     * @param vungDichId ID của vùng dịch.
     * @param bienPhap   Biện pháp phòng chống cần thêm.
     */
    public void addBienPhapPhongChong(Long vungDichId, BienPhapPhongChong bienPhap) {
        VungDich vungDich = vungDichRepository.findById(vungDichId).orElse(null);
        if (vungDich != null) {
            vungDich.getBienPhapPhongChongs().add(bienPhap);
            vungDichRepository.save(vungDich);
        }
    }
}