package com.webgis.dsws.config.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.locationtech.jts.geom.*;
import java.io.IOException;

public class GeometrySerializer extends JsonSerializer<Geometry> {
    @Override
    public void serialize(Geometry value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        gen.writeStartObject();
        gen.writeStringField("type", value.getGeometryType());
        gen.writeFieldName("coordinates");
        writeCoordinates(value, gen);
        gen.writeEndObject();
    }

    private void writeCoordinates(Geometry geometry, JsonGenerator gen) throws IOException {
        if (geometry instanceof Point) {
            Point point = (Point) geometry;
            gen.writeStartArray();
            gen.writeNumber(point.getX());
            gen.writeNumber(point.getY());
            gen.writeEndArray();
        } else if (geometry instanceof Polygon) {
            writePolygonCoordinates((Polygon) geometry, gen);
        } else if (geometry instanceof MultiPolygon) {
            writeMultiPolygonCoordinates((MultiPolygon) geometry, gen);
        } else if (geometry instanceof LineString) {
            writeLineStringCoordinates((LineString) geometry, gen);
        } else if (geometry instanceof MultiLineString) {
            writeMultiLineStringCoordinates((MultiLineString) geometry, gen);
        }
    }

    private void writePolygonCoordinates(Polygon polygon, JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        writeLineStringCoordinates(polygon.getExteriorRing(), gen);
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            writeLineStringCoordinates(polygon.getInteriorRingN(i), gen);
        }
        gen.writeEndArray();
    }

    private void writeMultiPolygonCoordinates(MultiPolygon multiPolygon, JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            writePolygonCoordinates((Polygon) multiPolygon.getGeometryN(i), gen);
        }
        gen.writeEndArray();
    }

    private void writeLineStringCoordinates(LineString line, JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        for (Coordinate coord : line.getCoordinates()) {
            gen.writeStartArray();
            gen.writeNumber(coord.x);
            gen.writeNumber(coord.y);
            gen.writeEndArray();
        }
        gen.writeEndArray();
    }

    private void writeMultiLineStringCoordinates(MultiLineString multiLine, JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        for (int i = 0; i < multiLine.getNumGeometries(); i++) {
            writeLineStringCoordinates((LineString) multiLine.getGeometryN(i), gen);
        }
        gen.writeEndArray();
    }
}