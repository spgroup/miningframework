package org.opentripplanner.common.geometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.linearref.LengthLocationMap;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opentripplanner.analyst.UnsupportedGeometryException;
import org.opentripplanner.common.model.P2;
import java.util.List;

public class GeometryUtils {

    private static CoordinateSequenceFactory csf = new Serializable2DPackedCoordinateSequenceFactory();

    private static GeometryFactory gf = new GeometryFactory(csf);

    public static final CoordinateReferenceSystem WGS84_XY;

    static {
        try {
            WGS84_XY = CRS.getAuthorityFactory(true).createCoordinateReferenceSystem("EPSG:4326");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Could not create longitude-first WGS84 coordinate reference system.");
        }
    }

    public static LineString makeLineString(double... coords) {
        GeometryFactory factory = getGeometryFactory();
        Coordinate[] coordinates = new Coordinate[coords.length / 2];
        for (int i = 0; i < coords.length; i += 2) {
            coordinates[i / 2] = new Coordinate(coords[i], coords[i + 1]);
        }
        return factory.createLineString(coordinates);
    }

    public static GeometryFactory getGeometryFactory() {
        return gf;
    }

    public static P2<LineString> splitGeometryAtPoint(Geometry geometry, Coordinate nearestPoint) {
        LocationIndexedLine line = new LocationIndexedLine(geometry);
        LinearLocation l = line.indexOf(nearestPoint);
        LineString beginning = (LineString) line.extractLine(line.getStartIndex(), l);
        LineString ending = (LineString) line.extractLine(l, line.getEndIndex());
        return new P2<LineString>(beginning, ending);
    }

    public static P2<LineString> splitGeometryAtFraction(Geometry geometry, double fraction) {
        LineString empty = new LineString(null, gf);
        Coordinate[] coordinates = geometry.getCoordinates();
        CoordinateSequence sequence = gf.getCoordinateSequenceFactory().create(coordinates);
        LineString total = new LineString(sequence, gf);
        if (coordinates.length < 2)
            return new P2<LineString>(empty, empty);
        if (fraction <= 0)
            return new P2<LineString>(empty, total);
        if (fraction >= 1)
            return new P2<LineString>(total, empty);
        double totalDistance = total.getLength();
        double requestedDistance = totalDistance * fraction;
        LocationIndexedLine line = new LocationIndexedLine(geometry);
        LinearLocation l = LengthLocationMap.getLocation(geometry, requestedDistance);
        LineString beginning = (LineString) line.extractLine(line.getStartIndex(), l);
        LineString ending = (LineString) line.extractLine(l, line.getEndIndex());
        return new P2<LineString>(beginning, ending);
    }

    public static LineString getInteriorSegment(Geometry geomerty, Coordinate first, Coordinate second) {
        P2<LineString> splitGeom = GeometryUtils.splitGeometryAtPoint(geomerty, first);
        splitGeom = GeometryUtils.splitGeometryAtPoint(splitGeom.second, second);
        return splitGeom.first;
    }

    public static double segmentFraction(double x0, double y0, double x1, double y1, double xp, double yp, double xscale) {
        double dx = (x1 - x0) * xscale;
        double dy = y1 - y0;
        double len2 = dx * dx + dy * dy;
        if (len2 == 0)
            return 0;
        double r = ((xp - x0) * xscale * dx + (yp - y0) * dy) / len2;
        if (r < 0.0)
            return 0.0;
        else if (r > 1.0)
            return 1.0;
        return r;
    }

    public static Geometry convertGeoJsonToJtsGeometry(GeoJsonObject geoJsonGeom) throws UnsupportedGeometryException {
        if (geoJsonGeom instanceof org.geojson.Point) {
            org.geojson.Point geoJsonPoint = (org.geojson.Point) geoJsonGeom;
            return gf.createPoint(new Coordinate(geoJsonPoint.getCoordinates().getLongitude(), geoJsonPoint.getCoordinates().getLatitude()));
        } else if (geoJsonGeom instanceof org.geojson.Polygon) {
            org.geojson.Polygon geoJsonPolygon = (org.geojson.Polygon) geoJsonGeom;
            LinearRing shell = gf.createLinearRing(convertPath(geoJsonPolygon.getExteriorRing()));
            LinearRing[] holes = new LinearRing[geoJsonPolygon.getInteriorRings().size()];
            int i = 0;
            for (List<LngLatAlt> hole : geoJsonPolygon.getInteriorRings()) {
                holes[i++] = gf.createLinearRing(convertPath(hole));
            }
            return gf.createPolygon(shell, holes);
        } else if (geoJsonGeom instanceof org.geojson.MultiPolygon) {
            org.geojson.MultiPolygon geoJsonMultiPolygon = (org.geojson.MultiPolygon) geoJsonGeom;
            Polygon[] jtsPolygons = new Polygon[geoJsonMultiPolygon.getCoordinates().size()];
            int i = 0;
            for (List<List<LngLatAlt>> geoJsonRings : geoJsonMultiPolygon.getCoordinates()) {
                org.geojson.Polygon geoJsonPoly = new org.geojson.Polygon();
                for (List<LngLatAlt> geoJsonRing : geoJsonRings) geoJsonPoly.add(geoJsonRing);
                jtsPolygons[i++] = (Polygon) convertGeoJsonToJtsGeometry(geoJsonPoly);
            }
            return gf.createMultiPolygon(jtsPolygons);
        } else if (geoJsonGeom instanceof org.geojson.LineString) {
            org.geojson.LineString geoJsonLineString = (org.geojson.LineString) geoJsonGeom;
            return gf.createLineString(convertPath(geoJsonLineString.getCoordinates()));
        } else if (geoJsonGeom instanceof org.geojson.MultiLineString) {
            org.geojson.MultiLineString geoJsonMultiLineString = (org.geojson.MultiLineString) geoJsonGeom;
            LineString[] jtsLineStrings = new LineString[geoJsonMultiLineString.getCoordinates().size()];
            int i = 0;
            for (List<LngLatAlt> geoJsonPath : geoJsonMultiLineString.getCoordinates()) {
                org.geojson.LineString geoJsonLineString = new org.geojson.LineString(geoJsonPath.toArray(new LngLatAlt[geoJsonPath.size()]));
                jtsLineStrings[i++] = (LineString) convertGeoJsonToJtsGeometry(geoJsonLineString);
            }
            return gf.createMultiLineString(jtsLineStrings);
        }
        throw new UnsupportedGeometryException(geoJsonGeom.getClass().toString());
    }

    private static Coordinate[] convertPath(List<LngLatAlt> path) {
        Coordinate[] coords = new Coordinate[path.size()];
        int i = 0;
        for (LngLatAlt p : path) {
            coords[i++] = new Coordinate(p.getLatitude(), p.getLongitude());
        }
        return coords;
    }
}