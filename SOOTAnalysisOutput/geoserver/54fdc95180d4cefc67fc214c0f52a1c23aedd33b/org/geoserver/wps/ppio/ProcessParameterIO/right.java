package org.geoserver.wps.ppio;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.geoserver.platform.GeoServerExtensions;
import org.geotools.data.Parameter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.springframework.context.ApplicationContext;
import com.vividsolutions.jts.geom.Envelope;

public abstract class ProcessParameterIO {

    static List<ProcessParameterIO> defaults;

    static {
        defaults = new ArrayList<ProcessParameterIO>();
        defaults.add(new LiteralPPIO(BigInteger.class));
        defaults.add(new LiteralPPIO(BigDecimal.class));
        defaults.add(new LiteralPPIO(Double.class));
        defaults.add(new LiteralPPIO(double.class));
        defaults.add(new LiteralPPIO(Float.class));
        defaults.add(new LiteralPPIO(float.class));
        defaults.add(new LiteralPPIO(Integer.class));
        defaults.add(new LiteralPPIO(int.class));
        defaults.add(new LiteralPPIO(Long.class));
        defaults.add(new LiteralPPIO(long.class));
        defaults.add(new LiteralPPIO(Short.class));
        defaults.add(new LiteralPPIO(short.class));
        defaults.add(new LiteralPPIO(Byte.class));
        defaults.add(new LiteralPPIO(byte.class));
        defaults.add(new LiteralPPIO(Number.class));
        defaults.add(new LiteralPPIO(Boolean.class));
        defaults.add(new LiteralPPIO(boolean.class));
        defaults.add(new LiteralPPIO(String.class));
        defaults.add(new LiteralPPIO(CharSequence.class));
        defaults.add(new LiteralPPIO(Date.class));
        defaults.add(new LiteralPPIO(java.sql.Date.class));
        defaults.add(new LiteralPPIO(Time.class));
        defaults.add(new LiteralPPIO(Timestamp.class));
        defaults.add(new GMLPPIO.GML3.Geometry());
        defaults.add(new GMLPPIO.GML2.Geometry());
        defaults.add(new WKTPPIO());
        defaults.add(new GeoJSONPPIO.Geometries());
        defaults.add(new GMLPPIO.GML3.GeometryAlternate());
        defaults.add(new GMLPPIO.GML2.GeometryAlternate());
        defaults.add(new WFSPPIO.WFS10());
        defaults.add(new WFSPPIO.WFS11());
        defaults.add(new GeoJSONPPIO.FeatureCollections());
        defaults.add(new WFSPPIO.WFS10Alternate());
        defaults.add(new WFSPPIO.WFS11Alternate());
        defaults.add(new CoordinateReferenceSystemPPIO());
        defaults.add(new GeoTiffPPIO());
        defaults.add(new ArcGridPPIO());
        defaults.add(new ImagePPIO.PNGPPIO());
        defaults.add(new ImagePPIO.JPEGPPIO());
        defaults.add(new BoundingBoxPPIO(Envelope.class));
        defaults.add(new BoundingBoxPPIO(ReferencedEnvelope.class));
        defaults.add(new BoundingBoxPPIO(org.opengis.geometry.Envelope.class));
        defaults.add(new FilterPPIO.Filter10());
        defaults.add(new FilterPPIO.Filter11());
        defaults.add(new CQLFilterPPIO());
    }

    public static ProcessParameterIO find(Parameter<?> p, ApplicationContext context, String mime) {
        if (p.type.isEnum()) {
            return new LiteralPPIO(p.type);
        }
        List<ProcessParameterIO> all = findAll(p, context);
        if (all.isEmpty()) {
            return null;
        }
        if (mime != null) {
            for (ProcessParameterIO ppio : all) {
                if (ppio instanceof ComplexPPIO && ((ComplexPPIO) ppio).getMimeType().equals(mime)) {
                    return ppio;
                }
            }
        }
        if (all.size() > 0) {
            Collections.sort(all, new Comparator<ProcessParameterIO>() {

                public int compare(ProcessParameterIO o1, ProcessParameterIO o2) {
                    Class c1 = o1.getType();
                    Class c2 = o2.getType();
                    if (c1.equals(c2)) {
                        return 0;
                    }
                    if (c1.isAssignableFrom(c2)) {
                        return 1;
                    }
                    return -1;
                }
            });
        }
        return all.get(0);
    }

    public static List<ProcessParameterIO> findAll(Parameter<?> p, ApplicationContext context) {
        if (p.type.isEnum()) {
            List<ProcessParameterIO> result = new ArrayList<ProcessParameterIO>();
            result.add(new LiteralPPIO(p.type));
            return result;
        }
        List<ProcessParameterIO> l = new ArrayList<ProcessParameterIO>(defaults);
        if (context != null) {
            l.addAll(GeoServerExtensions.extensions(ProcessParameterIO.class, context));
        } else {
            l.addAll(GeoServerExtensions.extensions(ProcessParameterIO.class));
        }
        List<ProcessParameterIO> matches = new ArrayList<ProcessParameterIO>();
        for (ProcessParameterIO ppio : l) {
            if (ppio.getIdentifer() != null && ppio.getIdentifer().equals(p.key) && ppio.getType().isAssignableFrom(p.type)) {
                matches.add(ppio);
            }
        }
        if (matches.isEmpty()) {
            for (ProcessParameterIO ppio : l) {
                if (ppio.getType().isAssignableFrom(p.type)) {
                    matches.add(ppio);
                }
            }
        }
        return matches;
    }

    final protected Class externalType;

    final protected Class internalType;

    protected String identifer;

    protected ProcessParameterIO(Class externalType, Class internalType) {
        this(externalType, internalType, null);
    }

    protected ProcessParameterIO(Class externalType, Class internalType, String identifier) {
        this.externalType = externalType;
        this.internalType = internalType;
        this.identifer = identifier;
    }

    public final Class getExternalType() {
        return externalType;
    }

    public final Class getType() {
        return internalType;
    }

    public final String getIdentifer() {
        return identifer;
    }
}
