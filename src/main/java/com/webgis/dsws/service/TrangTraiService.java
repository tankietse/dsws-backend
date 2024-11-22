package com.webgis.dsws.service;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import com.webgis.dsws.common.impl.ImportEntityProcessor;
import com.webgis.dsws.model.TrangTrai;
import com.webgis.dsws.model.VungDich;
import com.webgis.dsws.model.VungDichTrangTrai;
import com.webgis.dsws.repository.TrangTraiRepository;
import com.webgis.dsws.repository.VungDichTrangTraiRepository;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.persistence.EntityNotFoundException;
import com.google.common.base.Preconditions;

@Service
@Validated
public class TrangTraiService {
    private final TrangTraiRepository trangTraiRepository;
    private final GeometryService geometryService;
    private final ImportEntityProcessor<TrangTrai> trangTraiProcessor;
    private final VungDichTrangTraiRepository vungDichTrangTraiRepository;

    public TrangTraiService(TrangTraiRepository trangTraiRepository, GeometryService geometryService,
            ImportEntityProcessor<TrangTrai> trangTraiProcessor,
            VungDichTrangTraiRepository vungDichTrangTraiRepository) {
        this.trangTraiRepository = trangTraiRepository;
        this.geometryService = geometryService;
        this.trangTraiProcessor = trangTraiProcessor;
        this.vungDichTrangTraiRepository = vungDichTrangTraiRepository;
    }

    public TrangTrai findOrCreate(String name) {
        return trangTraiProcessor.findOrCreate(name);
    }

    public Set<TrangTrai> processAndSave(Set<String> names) {
        return trangTraiProcessor.processAndSave(names);
    }

    public TrangTrai save(TrangTrai trangTrai) {
        return trangTraiRepository.save(trangTrai);
    }

    public TrangTrai findById(Long id) {
        return trangTraiRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy trang trại với ID: " + id));
    }

    public List<TrangTrai> findAll() {
        return trangTraiRepository.findAll();
    }

    public void deleteById(Long id) {
        trangTraiRepository.deleteById(id);
    }

    public TrangTrai update(TrangTrai trangTrai) {
        return trangTraiRepository.save(trangTrai);
    }

    /**
     * Cập nhật thông tin trang trại
     */
    public TrangTrai updateTrangTrai(Long id, TrangTrai trangTraiDetails) {
        TrangTrai trangTrai = findById(id);
        
        // Chỉ cập nhật các trường không null
        if (trangTraiDetails.getTenTrangTrai() != null) {
            trangTrai.setTenTrangTrai(trangTraiDetails.getTenTrangTrai());
        }
        if (trangTraiDetails.getTenChu() != null) {
            trangTrai.setTenChu(trangTraiDetails.getTenChu());
        }
        if (trangTraiDetails.getSoDienThoai() != null) {
            trangTrai.setSoDienThoai(trangTraiDetails.getSoDienThoai());
        }
        if (trangTraiDetails.getEmail() != null) {
            trangTrai.setEmail(trangTraiDetails.getEmail());
        }
        if (trangTraiDetails.getPoint() != null) {
            trangTrai.setPoint(trangTraiDetails.getPoint());
        }
        if (trangTraiDetails.getDiaChiDayDu() != null) {
            trangTrai.setDiaChiDayDu(trangTraiDetails.getDiaChiDayDu());
        }
        
        return trangTraiRepository.save(trangTrai);
    }

    /**
     * Tìm các trang trại trong bán kính
     */
    public List<TrangTrai> findTrangTraiInRadius(Point center, double radius) {
        Preconditions.checkNotNull(center, "Điểm trung tâm không được null");
        Preconditions.checkArgument(radius > 0, "Bán kính phải lớn hơn 0");
        return trangTraiRepository.findFarmsWithinDistance(center, radius);
    }

    /**
     * Liên kết các trang trại bị ảnh hưởng với vùng dịch
     * 
     * @param vungDich Vùng dịch cần xử lý
     * @return Danh sách các VungDichTrangTrai đã được tạo
     */
    public List<VungDichTrangTrai> associateAffectedFarms(VungDich vungDich) {
        List<TrangTrai> affectedFarms = trangTraiRepository.findFarmsWithinDistance(vungDich.getGeom(),
                vungDich.getBanKinh());
        List<VungDichTrangTrai> vungDichTrangTrais = new ArrayList<>();
        for (TrangTrai trangTrai : affectedFarms) {
            double distance = geometryService.calculateDistance(vungDich.getGeom(), trangTrai.getPoint());
            VungDichTrangTrai vdt = new VungDichTrangTrai();
            vdt.setVungDich(vungDich);
            vdt.setTrangTrai(trangTrai);
            vdt.setKhoangCach((float) distance);
            // TODO: Xác định mức độ ảnh hưởng dựa trên khoảng cách
            // vdt.setMucDoAnhHuong(tínhMứcĐộẢnhHưởng(distance));
            vungDichTrangTraiRepository.save(vdt);
            vungDichTrangTrais.add(vdt);
        }
        return vungDichTrangTrais;
    }
}
