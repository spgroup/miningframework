package org.geoserver.wms.worldwind.util;

import java.util.HashMap;
import javax.media.jai.Interpolation;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.CoverageProcessor;
import org.geotools.coverage.processing.operation.Crop;
import org.geotools.coverage.processing.operation.FilteredSubsample;
import org.geotools.coverage.processing.operation.Interpolate;
import org.geotools.coverage.processing.operation.Resample;
import org.geotools.coverage.processing.operation.Scale;
import org.geotools.coverage.processing.operation.SelectSampleDimension;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.coverage.Coverage;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.vfny.geoserver.util.WCSUtils;
import org.vfny.geoserver.wcs.WcsException;

public class BilWCSUtils extends WCSUtils {

    public final static Hints LENIENT_HINT = new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);

    private final static SelectSampleDimension bandSelectFactory = new SelectSampleDimension();

    private final static Crop cropFactory = new Crop();

    private final static Interpolate interpolateFactory = new Interpolate();

    private final static Scale scaleFactory = new Scale();

    private final static FilteredSubsample filteredSubsampleFactory = new FilteredSubsample();

    private final static Resample resampleFactory = new Resample();

    static {
        final CoverageProcessor processor = new CoverageProcessor();
        bandSelectParams = processor.getOperation("SelectSampleDimension").getParameters();
        cropParams = processor.getOperation("CoverageCrop").getParameters();
        interpolateParams = processor.getOperation("Interpolate").getParameters();
        scaleParams = processor.getOperation("Scale").getParameters();
        resampleParams = processor.getOperation("Resample").getParameters();
        filteredSubsampleParams = processor.getOperation("FilteredSubsample").getParameters();
    }

    private final static ParameterValueGroup bandSelectParams;

    private final static ParameterValueGroup cropParams;

    private final static ParameterValueGroup interpolateParams;

    private final static ParameterValueGroup resampleParams;

    private final static ParameterValueGroup scaleParams;

    private final static ParameterValueGroup filteredSubsampleParams;

    private final static Hints hints = new Hints(new HashMap(5));

    static {
        hints.add(LENIENT_HINT);
    }

    public static GridCoverage2D reproject(GridCoverage2D coverage, final CoordinateReferenceSystem sourceCRS, final CoordinateReferenceSystem targetCRS, final Interpolation interpolation) throws WcsException {
        if (!CRS.equalsIgnoreMetadata(sourceCRS, targetCRS)) {
            final ParameterValueGroup param = (ParameterValueGroup) resampleParams.clone();
            param.parameter("Source").setValue(coverage);
            param.parameter("CoordinateReferenceSystem").setValue(targetCRS);
            param.parameter("GridGeometry").setValue(null);
            param.parameter("InterpolationType").setValue(interpolation);
            coverage = (GridCoverage2D) resampleFactory.doOperation(param, hints);
        }
        return coverage;
    }

    public static GridCoverage2D scale(final GridCoverage2D coverage, final GridEnvelope newGridRange, final GridCoverage sourceCoverage, final CoordinateReferenceSystem sourceCRS, final GeneralEnvelope destinationEnvelopeInSourceCRS) {
        GridGeometry2D scaledGridGeometry = new GridGeometry2D(newGridRange, (destinationEnvelopeInSourceCRS != null) ? destinationEnvelopeInSourceCRS : sourceCoverage.getEnvelope());
        final ParameterValueGroup param = (ParameterValueGroup) resampleParams.clone();
        param.parameter("Source").setValue(coverage);
        param.parameter("CoordinateReferenceSystem").setValue(sourceCRS);
        param.parameter("GridGeometry").setValue(scaledGridGeometry);
        param.parameter("InterpolationType").setValue(Interpolation.getInstance(Interpolation.INTERP_NEAREST));
        final GridCoverage2D scaledGridCoverage = (GridCoverage2D) resampleFactory.doOperation(param, hints);
        return scaledGridCoverage;
    }

    public static GridCoverage2D crop(final Coverage coverage, final GeneralEnvelope sourceEnvelope, final CoordinateReferenceSystem sourceCRS, final GeneralEnvelope destinationEnvelopeInSourceCRS, final Boolean conserveEnvelope) throws WcsException {
        final GridCoverage2D croppedGridCoverage;
        final GeneralEnvelope intersectionEnvelope = new GeneralEnvelope(destinationEnvelopeInSourceCRS);
        intersectionEnvelope.setCoordinateReferenceSystem(sourceCRS);
        intersectionEnvelope.intersect((GeneralEnvelope) sourceEnvelope);
        if (intersectionEnvelope.isEmpty()) {
            throw new WcsException("The Intersection is null. Check the requested BBOX!");
        }
        if (!intersectionEnvelope.equals((GeneralEnvelope) sourceEnvelope)) {
            final ParameterValueGroup param = (ParameterValueGroup) cropParams.clone();
            param.parameter("Source").setValue(coverage);
            param.parameter("Envelope").setValue(intersectionEnvelope);
            croppedGridCoverage = (GridCoverage2D) cropFactory.doOperation(param, hints);
        } else {
            croppedGridCoverage = (GridCoverage2D) coverage;
        }
        croppedGridCoverage.prefetch(intersectionEnvelope.toRectangle2D());
        return croppedGridCoverage;
    }
}