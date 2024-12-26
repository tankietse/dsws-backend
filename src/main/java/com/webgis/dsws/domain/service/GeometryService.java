package com.webgis.dsws.domain.service;

import org.locationtech.jts.geom.Point;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.stereotype.Service;

import com.webgis.dsws.exception.DataImportException;

import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.geom.LinearRing;

@Service
public class GeometryService {
    private final GeoJsonReader reader = new GeoJsonReader();
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final WKBReader wkbReader = new WKBReader();
    private final WKTReader wktReader = new WKTReader();

    public Point createPoint(Double longitude, Double latitude) {

        return geometryFactory.createPoint(new Coordinate(longitude, latitude));

    }

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

    /**
     * Chuyển đổi chuỗi geometry sang đối tượng Point
     * Hỗ trợ cả định dạng WKB và WKT
     * 
     * @param geomWKT Chuỗi geometry cần chuyển đổi
     * @return Đối tượng Point đã chuyển đổi
     * @throws DataImportException nếu không thể chuyển đổi
     */
    public Point convertGeometry(String geomWKT) {
        try {
            // First try WKB conversion
            byte[] wkbBytes = hexStringToByteArray(geomWKT);
            return (Point) wkbReader.read(wkbBytes);
        } catch (Exception e) {
            try {
                // If WKB fails, try WKT
                return (Point) wktReader.read(geomWKT);
            } catch (Exception ex) {
                throw new DataImportException("Không thể chuyển đổi geometry. Dữ liệu: " + geomWKT, ex);
            }
        }
    }

    /**
     * Chuyển đổi chuỗi hex thành mảng byte
     * 
     * @param s Chuỗi hex cần chuyển đổi
     * @return Mảng byte tương ứng
     */
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        // Chuyển đổi từng cặp ký tự hex thành byte
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public Object extractCoordinates(Geometry geometry) {
        if (geometry == null) {
            return null;
        }

        try {
            // Handle different geometry types
            String geoType = geometry.getGeometryType();
            switch (geoType) {
                case "Polygon":
                    return extractPolygonCoordinates((Polygon) geometry);
                case "MultiPolygon":
                    return extractMultiPolygonCoordinates((MultiPolygon) geometry);
                case "Point":
                    Coordinate coord = geometry.getCoordinate();
                    return Arrays.asList(coord.x, coord.y);
                default:
                    System.err.println("Unsupported geometry type: " + geoType);
                    return null;
            }
        } catch (Exception e) {
            System.err.println("Error extracting coordinates: " + e.getMessage());
            return null;
        }
    }

    private List<List<List<Double>>> extractPolygonCoordinates(Polygon polygon) {
        List<List<List<Double>>> polygonCoords = new ArrayList<>();

        try {
            // Extract exterior ring
            Coordinate[] exteriorCoords = polygon.getExteriorRing().getCoordinates();
            List<List<Double>> exteriorRing = new ArrayList<>();
            for (Coordinate coord : exteriorCoords) {
                if (!Double.isNaN(coord.x) && !Double.isNaN(coord.y)) {
                    exteriorRing.add(Arrays.asList(coord.x, coord.y));
                }
            }
            polygonCoords.add(exteriorRing);

            // Extract interior rings
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                List<List<Double>> interiorRing = new ArrayList<>();
                Coordinate[] interiorCoords = polygon.getInteriorRingN(i).getCoordinates();
                for (Coordinate coord : interiorCoords) {
                    if (!Double.isNaN(coord.x) && !Double.isNaN(coord.y)) {
                        interiorRing.add(Arrays.asList(coord.x, coord.y));
                    }
                }
                polygonCoords.add(interiorRing);
            }
        } catch (Exception e) {
            System.err.println("Error extracting polygon coordinates: " + e.getMessage());
            return null;
        }

        return polygonCoords;
    }

    private List<List<List<List<Double>>>> extractMultiPolygonCoordinates(MultiPolygon multiPolygon) {
        List<List<List<List<Double>>>> multiPolygonCoords = new ArrayList<>();

        try {
            int numGeometries = multiPolygon.getNumGeometries();
            for (int i = 0; i < numGeometries; i++) {
                Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
                List<List<List<Double>>> polygonCoords = extractPolygonCoordinates(polygon);
                if (polygonCoords != null) {
                    multiPolygonCoords.add(polygonCoords);
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting multipolygon coordinates: " + e.getMessage());
            return null;
        }

        return multiPolygonCoords;
    }

    /**
     * Tạo một hình tròn từ một điểm trung tâm và bán kính.
     * 
     * @param center Điểm trung tâm của hình tròn
     * @param radius Bán kính của hình tròn (đơn vị: mét)
     * @return Đối tượng Geometry đại diện cho hình tròn
     */
    public Geometry createCircle(Point center, double radius) {
        int numPoints = 64; // Số điểm để tạo hình tròn
        Coordinate[] coords = new Coordinate[numPoints + 1];
        for (int i = 0; i < numPoints; i++) {
            double angle = (i * 2 * Math.PI) / numPoints;
            double dx = radius * Math.cos(angle);
            double dy = radius * Math.sin(angle);
            coords[i] = new Coordinate(center.getX() + dx, center.getY() + dy);
        }
        coords[numPoints] = coords[0]; // Đóng vòng tròn

        LinearRing ring = geometryFactory.createLinearRing(coords);
        return geometryFactory.createPolygon(ring, null);
    }
}