package com.webgis.dsws.domain.service;

import org.locationtech.jts.geom.Point;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.stereotype.Service;

@Service
public class GeometryService {
    private final GeoJsonReader reader = new GeoJsonReader();
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public Polygon convertToPolygon(String geoJson) throws ParseException {
        Geometry geom = reader.read(geoJson);
        if (geom instanceof Polygon) {
            return (Polygon) geom;
        } else if (geom instanceof MultiPolygon) {
            return (Polygon) geom.getGeometryN(0);
        }
        return null;
    }

    public Point convertToPoint(String geoJson) throws ParseException {
        Geometry geom = reader.read(geoJson);
        if (geom instanceof Point) {
            return (Point) geom;
        }
        return null;
    }

    public Geometry convertToGeometry(String geoJson) throws ParseException {
        return reader.read(geoJson);
    }

    /**
     * Tính khoảng cách giữa hai đối tượng Geometry
     * 
     * @param geom1 Đối tượng Geometry thứ nhất
     * @param geom2 Đối tượng Geometry thứ hai
     * @return Khoảng cách giữa hai Geometry
     */
    public double calculateDistance(Geometry geom1, Geometry geom2) {
        return geom1.distance(geom2);
    }

    /**
     * Tính điểm trung tâm (centroid) của một tập hợp các điểm.
     * Sử dụng phương pháp trung bình cộng các tọa độ để xác định centroid.
     * 
     * @return Geometry đại diện cho điểm trung tâm
     * @param points Danh sách các điểm cần tính centroid
     * @throws IllegalArgumentException nếu danh sách điểm rỗng
     */
    public Geometry calculateCentroid(List<Point> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("Danh sách điểm không được rỗng");
        }

        // Tính tổng các tọa độ
        double sumX = 0;
        double sumY = 0;
        for (Point point : points) {
            sumX += point.getX();
            sumY += point.getY();
        }

        // Tính trung bình
        double centerX = sumX / points.size();
        double centerY = sumY / points.size();

        // Tạo điểm centroid
        return geometryFactory.createPoint(new Coordinate(centerX, centerY));
    }
}