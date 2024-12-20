package com.webgis.dsws.domain.dto;

import com.webgis.dsws.domain.model.enums.MucDoVungDichEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Geometry;

import java.util.Set;

@Data
@NoArgsConstructor
public class VungDichMapDTO {
    private Long id;
    private String tenVung;
    private Point centerPoint;
    private float banKinh;
    private MucDoVungDichEnum mucDo;
    private String colorCode;
    private Set<TrangTraiSimpleDTO> trangTrais;
    private DonViHanhChinhSimpleDTO donViHanhChinh;

    @Data
    @NoArgsConstructor
    public static class TrangTraiSimpleDTO {
        private Long id;
        private String tenTrangTrai;
        private String tenChu;
        private String diaChi;
        private Point location;
        private Float khoangCach;
    }

    @Data
    @NoArgsConstructor
    public static class DonViHanhChinhSimpleDTO {
        private Integer id;
        private String ten;
        private String capHanhChinh;
        private Geometry boundary;
    }
}
