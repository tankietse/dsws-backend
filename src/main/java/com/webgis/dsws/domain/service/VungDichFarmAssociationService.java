package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.model.TrangTrai;
import com.webgis.dsws.domain.model.VungDich;
import com.webgis.dsws.domain.model.VungDichTrangTrai;
import com.webgis.dsws.domain.repository.TrangTraiRepository;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class VungDichFarmAssociationService {

    private final TrangTraiRepository trangTraiRepository;
    private final GeometryService geometryService;

    public VungDichFarmAssociationService(TrangTraiRepository trangTraiRepository, GeometryService geometryService) {
        this.trangTraiRepository = trangTraiRepository;
        this.geometryService = geometryService;
    }

    public void associateAffectedFarms(VungDich vungDich) {
        Set<VungDichTrangTrai> vungDichTrangTrais = new HashSet<>();

        List<TrangTrai> affectedFarms = trangTraiRepository.findFarmsWithinDistance(
                vungDich.getGeom(), vungDich.getBanKinh());

        for (TrangTrai trangTrai : affectedFarms) {
            VungDichTrangTrai vdt = new VungDichTrangTrai();
            vdt.setVungDich(vungDich);
            vdt.setTrangTrai(trangTrai);
            // Calculate distance and set khoangCach
            float distance = (float) geometryService.calculateDistance(
                    vungDich.getGeom(), trangTrai.getPoint());
            vdt.setKhoangCach(distance);
            vungDichTrangTrais.add(vdt);
        }

        vungDich.setTrangTrais(vungDichTrangTrais);
    }
}