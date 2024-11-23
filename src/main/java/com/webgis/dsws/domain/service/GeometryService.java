package com.webgis.dsws.domain.service;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.stereotype.Service;

@Service
public class GeometryService {
    private final GeoJsonReader reader = new GeoJsonReader();

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
     * @param geom1 Đối tượng Geometry thứ nhất
     * @param geom2 Đối tượng Geometry thứ hai
     * @return Khoảng cách giữa hai Geometry
     */
    public double calculateDistance(Geometry geom1, Geometry geom2) {
        return geom1.distance(geom2);
    }
}