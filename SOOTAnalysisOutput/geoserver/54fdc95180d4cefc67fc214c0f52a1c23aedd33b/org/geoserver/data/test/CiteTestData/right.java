package org.geoserver.data.test;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;

public abstract class CiteTestData implements TestData {

    public static String CITE_PREFIX = "cite";

    public static String CITE_URI = "http://www.opengis.net/cite";

    public static QName BASIC_POLYGONS = new QName(CITE_URI, "BasicPolygons", CITE_PREFIX);

    public static QName BRIDGES = new QName(CITE_URI, "Bridges", CITE_PREFIX);

    public static QName BUILDINGS = new QName(CITE_URI, "Buildings", CITE_PREFIX);

    public static QName DIVIDED_ROUTES = new QName(CITE_URI, "DividedRoutes", CITE_PREFIX);

    public static QName FORESTS = new QName(CITE_URI, "Forests", CITE_PREFIX);

    public static QName LAKES = new QName(CITE_URI, "Lakes", CITE_PREFIX);

    public static QName MAP_NEATLINE = new QName(CITE_URI, "MapNeatline", CITE_PREFIX);

    public static QName NAMED_PLACES = new QName(CITE_URI, "NamedPlaces", CITE_PREFIX);

    public static QName PONDS = new QName(CITE_URI, "Ponds", CITE_PREFIX);

    public static QName ROAD_SEGMENTS = new QName(CITE_URI, "RoadSegments", CITE_PREFIX);

    public static QName STREAMS = new QName(CITE_URI, "Streams", CITE_PREFIX);

    public static String CDF_PREFIX = "cdf";

    public static String CDF_URI = "http://www.opengis.net/cite/data";

    public static QName DELETES = new QName(CDF_URI, "Deletes", CDF_PREFIX);

    public static QName FIFTEEN = new QName(CDF_URI, "Fifteen", CDF_PREFIX);

    public static QName INSERTS = new QName(CDF_URI, "Inserts", CDF_PREFIX);

    public static QName LOCKS = new QName(CDF_URI, "Locks", CDF_PREFIX);

    public static QName NULLS = new QName(CDF_URI, "Nulls", CDF_PREFIX);

    public static QName OTHER = new QName(CDF_URI, "Other", CDF_PREFIX);

    public static QName SEVEN = new QName(CDF_URI, "Seven", CDF_PREFIX);

    public static QName UPDATES = new QName(CDF_URI, "Updates", CDF_PREFIX);

    public static String CGF_PREFIX = "cgf";

    public static String CGF_URI = "http://www.opengis.net/cite/geometry";

    public static QName LINES = new QName(CGF_URI, "Lines", CGF_PREFIX);

    public static QName MLINES = new QName(CGF_URI, "MLines", CGF_PREFIX);

    public static QName MPOINTS = new QName(CGF_URI, "MPoints", CGF_PREFIX);

    public static QName MPOLYGONS = new QName(CGF_URI, "MPolygons", CGF_PREFIX);

    public static QName POINTS = new QName(CGF_URI, "Points", CGF_PREFIX);

    public static QName POLYGONS = new QName(CGF_URI, "Polygons", CGF_PREFIX);

    public static String SF_PREFIX = "sf";

    public static String SF_URI = "http://cite.opengeospatial.org/gmlsf";

    public static QName PRIMITIVEGEOFEATURE = new QName(SF_URI, "PrimitiveGeoFeature", SF_PREFIX);

    public static QName AGGREGATEGEOFEATURE = new QName(SF_URI, "AggregateGeoFeature", SF_PREFIX);

    public static QName GENERICENTITY = new QName(SF_URI, "GenericEntity", SF_PREFIX);

    public static QName GTOPO_DEM = new QName(CDF_URI, "W020N90", CDF_PREFIX);

    public static QName USA_WORLDIMG = new QName(CDF_URI, "usa", CDF_PREFIX);

    public static String DEM = "dem";

    public static String PNG = "png";

    public static String WCS_PREFIX = "wcs";

    public static String WCS_URI = "http://www.opengis.net/wcs/1.1.1";

    public static QName TASMANIA_DEM = new QName(WCS_URI, "DEM", WCS_PREFIX);

    public static QName TASMANIA_BM = new QName(WCS_URI, "BlueMarble", WCS_PREFIX);

    public static QName ROTATED_CAD = new QName(WCS_URI, "RotatedCad", WCS_PREFIX);

    public static QName WORLD = new QName(WCS_URI, "World", WCS_PREFIX);

    public static String TIFF = "tiff";

    public static String DEFAULT_PREFIX = "gs";

    public static String DEFAULT_URI = "http://geoserver.org";

    public static QName GEOMETRYLESS = new QName(CITE_URI, "Geometryless", CITE_PREFIX);

    public static QName[] TYPENAMES = new QName[] { BASIC_POLYGONS, BRIDGES, BUILDINGS, DIVIDED_ROUTES, FORESTS, LAKES, MAP_NEATLINE, NAMED_PLACES, PONDS, ROAD_SEGMENTS, STREAMS, GEOMETRYLESS, DELETES, FIFTEEN, INSERTS, LOCKS, NULLS, OTHER, SEVEN, UPDATES, LINES, MLINES, MPOINTS, MPOLYGONS, POINTS, POLYGONS, PRIMITIVEGEOFEATURE, AGGREGATEGEOFEATURE, GENERICENTITY };

    public static QName[] WMS_TYPENAMES = new QName[] { BASIC_POLYGONS, BRIDGES, BUILDINGS, DIVIDED_ROUTES, FORESTS, LAKES, MAP_NEATLINE, NAMED_PLACES, PONDS, ROAD_SEGMENTS, STREAMS, GEOMETRYLESS };

    public static QName[] WCS_TYPENAMES = new QName[] { TASMANIA_DEM, TASMANIA_BM, ROTATED_CAD, WORLD };

    public static QName[] WFS10_TYPENAMES = new QName[] { DELETES, FIFTEEN, INSERTS, LOCKS, NULLS, OTHER, SEVEN, UPDATES, LINES, MLINES, MPOINTS, MPOLYGONS, POINTS, POLYGONS };

    public static QName[] WFS11_TYPENAMES = new QName[] { PRIMITIVEGEOFEATURE, AGGREGATEGEOFEATURE, GENERICENTITY };

    public static QName[] CDF_TYPENAMES = new QName[] { DELETES, FIFTEEN, INSERTS, LOCKS, NULLS, OTHER, SEVEN, UPDATES };

    public static QName[] CGF_TYPENAMES = new QName[] { LINES, MLINES, MPOINTS, MPOLYGONS, POINTS, POLYGONS };

    public static QName[] SF_TYPENAMES = WFS11_TYPENAMES;

    public static QName[] CITE_TYPENAMES = WMS_TYPENAMES;

    public static HashMap<QName, Integer> SRS = new HashMap<QName, Integer>();

    static {
        for (int i = 0; i < WFS10_TYPENAMES.length; i++) {
            SRS.put(WFS10_TYPENAMES[i], 32615);
        }
        for (int i = 0; i < WFS11_TYPENAMES.length; i++) {
            SRS.put(WFS11_TYPENAMES[i], 4326);
        }
    }

    public static String DEFAULT_VECTOR_STYLE = "Default";

    public static String DEFAULT_RASTER_STYLE = "raster";

    public static HashMap<QName, String[]> COVERAGES = new HashMap<QName, String[]>();

    static {
        COVERAGES.put(TASMANIA_DEM, new String[] { "tazdem.tiff", TIFF });
        COVERAGES.put(TASMANIA_BM, new String[] { "tazbm.tiff", TIFF });
        COVERAGES.put(ROTATED_CAD, new String[] { "rotated.tiff", TIFF });
        COVERAGES.put(WORLD, new String[] { "world.tiff", TIFF });
    }

    public static final ReferencedEnvelope DEFAULT_LATLON_ENVELOPE = new ReferencedEnvelope(-180, 180, -90, 90, DefaultGeographicCRS.WGS84);

    public static void registerNamespaces(Map<String, String> namespaces) {
        namespaces.put(CITE_PREFIX, CITE_URI);
        namespaces.put(CDF_PREFIX, CDF_URI);
        namespaces.put(CGF_PREFIX, CGF_URI);
        namespaces.put(SF_PREFIX, SF_URI);
    }
}
