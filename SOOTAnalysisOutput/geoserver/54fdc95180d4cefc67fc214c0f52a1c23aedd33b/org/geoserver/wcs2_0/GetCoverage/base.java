package org.geoserver.wcs2_0;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.BorderExtender;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.WarpAffine;
import net.opengis.wcs20.ExtensionItemType;
import net.opengis.wcs20.ExtensionType;
import net.opengis.wcs20.GetCoverageType;
import net.opengis.wcs20.InterpolationAxesType;
import net.opengis.wcs20.InterpolationAxisType;
import net.opengis.wcs20.InterpolationMethodType;
import net.opengis.wcs20.InterpolationType;
import net.opengis.wcs20.RangeIntervalType;
import net.opengis.wcs20.RangeItemType;
import net.opengis.wcs20.RangeSubsetType;
import net.opengis.wcs20.ScalingType;
import org.eclipse.emf.common.util.EList;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.data.util.CoverageUtils;
import org.geoserver.platform.ServiceException;
import org.geoserver.wcs.CoverageCleanerCallback;
import org.geoserver.wcs.WCSInfo;
import org.geoserver.wcs2_0.exception.WCS20Exception;
import org.geoserver.wcs2_0.exception.WCS20Exception.WCS20ExceptionCode;
import org.geoserver.wcs2_0.response.DimensionBean;
import org.geoserver.wcs2_0.response.GranuleStackImpl;
import org.geoserver.wcs2_0.response.WCSDimensionsSubsetHelper;
import org.geoserver.wcs2_0.util.EnvelopeAxesLabelsMapper;
import org.geoserver.wcs2_0.util.NCNameResourceCodec;
import org.geoserver.wcs2_0.util.RequestUtils;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.StructuredGridCoverage2DReader;
import org.geotools.coverage.processing.CoverageProcessor;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.resources.coverage.CoverageUtilities;
import org.geotools.util.DefaultProgressListener;
import org.geotools.util.Utilities;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.processing.Operation;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.vfny.geoserver.util.WCSUtils;
import org.vfny.geoserver.wcs.WcsException;

public class GetCoverage {

    private final static Set<String> mdFormats;

    static {
        mdFormats = new HashSet<String>();
        mdFormats.add("application/x-netcdf");
    }

    private Logger LOGGER = Logging.getLogger(GetCoverage.class);

    private WCSInfo wcs;

    private Catalog catalog;

    private EnvelopeAxesLabelsMapper envelopeDimensionsMapper;

    private CRSAuthorityFactory lonLatCRSFactory;

    private CRSAuthorityFactory latLonCRSFactory;

    public final static String SRS_STARTER = "http://www.opengis.net/def/crs/EPSG/0/";

    public GetCoverage(WCSInfo serviceInfo, Catalog catalog, EnvelopeAxesLabelsMapper envelopeDimensionsMapper) {
        this.wcs = serviceInfo;
        this.catalog = catalog;
        this.envelopeDimensionsMapper = envelopeDimensionsMapper;
        Hints hints = GeoTools.getDefaultHints().clone();
        hints.add(new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
        hints.add(new Hints(Hints.FORCE_AXIS_ORDER_HONORING, "http-uri"));
        lonLatCRSFactory = ReferencingFactoryFinder.getCRSAuthorityFactory("http://www.opengis.net/def", hints);
        hints.add(new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.FALSE));
        hints.add(new Hints(Hints.FORCE_AXIS_ORDER_HONORING, "http-uri"));
        latLonCRSFactory = ReferencingFactoryFinder.getCRSAuthorityFactory("http://www.opengis.net/def", hints);
    }

    public static boolean formatSupportMDOutput(String format) {
        return mdFormats.contains(format);
    }

    public GridCoverage run(GetCoverageType request) {
        final LayerInfo linfo = NCNameResourceCodec.getCoverage(catalog, request.getCoverageId());
        if (linfo == null) {
            throw new WCS20Exception("Could not locate coverage " + request.getCoverageId(), WCS20Exception.WCS20ExceptionCode.NoSuchCoverage, "coverageId");
        }
        final CoverageInfo cinfo = (CoverageInfo) linfo.getResource();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Executing GetCoverage request on coverage :" + linfo.toString());
        }
        GridCoverage coverage = null;
        try {
            Map<String, ExtensionItemType> extensions = extractExtensions(request);
            final Hints hints = GeoTools.getDefaultHints();
            hints.add(WCSUtils.getReaderHints(wcs));
            hints.add(new RenderingHints(JAI.KEY_BORDER_EXTENDER, BorderExtender.createInstance(BorderExtender.BORDER_COPY)));
            final GridCoverage2DReader reader = (GridCoverage2DReader) cinfo.getGridCoverageReader(new DefaultProgressListener(), hints);
            WCSDimensionsSubsetHelper helper = parseGridCoverageRequest(cinfo, reader, request, extensions);
            GridCoverageRequest gcr = helper.getGridCoverageRequest();
            final GridCoverageFactory coverageFactory = CoverageFactoryFinder.getGridCoverageFactory(hints);
            if (reader instanceof StructuredGridCoverage2DReader && formatSupportMDOutput(request.getFormat())) {
                final List<GridCoverageRequest> requests = helper.splitRequest();
                if (requests == null || requests.isEmpty()) {
                    throw new IllegalArgumentException("Splitting requests returned nothing");
                } else {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Splitting request generated " + requests.size() + " sub requests");
                    }
                }
                final List<DimensionBean> dimensions = helper.setupDimensions();
                final String nativeName = cinfo.getNativeCoverageName();
                final String coverageName = nativeName != null ? nativeName : reader.getGridCoverageNames()[0];
                final GranuleStackImpl stack = new GranuleStackImpl(coverageName, reader.getCoordinateReferenceSystem(), dimensions);
                long outputLimit = wcs.getMaxOutputMemory() * 1024;
                long inputLimit = wcs.getMaxInputMemory() * 1024;
                ImageSizeRecorder incrementalOutputSize = new ImageSizeRecorder(outputLimit, false);
                ImageSizeRecorder incrementalInputSize = new ImageSizeRecorder(inputLimit, true);
                int numRequests = requests.size();
                GridCoverageRequest firstRequest = requests.remove(0);
                GridCoverage2D firstCoverage = setupCoverage(helper, firstRequest, request, reader, hints, extensions, dimensions, incrementalOutputSize, incrementalInputSize, coverageFactory);
                long actual = incrementalInputSize.finalSize();
                long estimatedSize = actual * numRequests;
                if (outputLimit > 0 && estimatedSize > outputLimit) {
                    throw new WcsException("This request is trying to generate too much data, " + "the limit is " + formatBytes(outputLimit) + " but the estimated amount of bytes to be " + "written in the output is " + formatBytes(estimatedSize));
                }
                stack.addCoverage(firstCoverage);
                for (GridCoverageRequest subRequest : requests) {
                    GridCoverage2D singleCoverage = setupCoverage(helper, subRequest, request, reader, hints, extensions, dimensions, incrementalOutputSize, incrementalInputSize, coverageFactory);
                    stack.addCoverage(singleCoverage);
                }
                coverage = stack;
            } else {
                coverage = setupCoverage(helper, gcr, request, reader, hints, extensions, null, null, null, coverageFactory);
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new WCS20Exception("Failed to read the coverage " + request.getCoverageId(), e);
        } finally {
            if (coverage != null) {
                CoverageCleanerCallback.addCoverages(coverage);
            }
        }
        return coverage;
    }

    private GridCoverage2D setupCoverage(final WCSDimensionsSubsetHelper helper, final GridCoverageRequest gridCoverageRequest, final GetCoverageType coverageType, final GridCoverage2DReader reader, final Hints hints, final Map<String, ExtensionItemType> extensions, final List<DimensionBean> coverageDimensions, ImageSizeRecorder incrementalOutputSize, ImageSizeRecorder incrementalInputSize, final GridCoverageFactory coverageFactory) throws Exception {
        GridCoverage2D coverage = null;
        coverage = readCoverage(helper.getCoverageInfo(), gridCoverageRequest, reader, hints, incrementalInputSize);
        if (coverage == null) {
            throw new IllegalStateException("Unable to read a coverage for the current request" + coverageType.toString());
        }
        coverage = handleRangeSubsettingExtension(coverage, extensions, hints);
        coverage = handleSubsettingExtension(coverage, gridCoverageRequest.getSpatialSubset(), hints);
        coverage = handleScaling(coverage, extensions, gridCoverageRequest.getSpatialInterpolation(), hints);
        coverage = handleReprojection(coverage, gridCoverageRequest.getOutputCRS(), gridCoverageRequest.getSpatialInterpolation(), hints);
        final boolean enforceLatLonAxesOrder = requestingLatLonAxesOrder(gridCoverageRequest.getOutputCRS());
        if (enforceLatLonAxesOrder) {
            coverage = enforceLatLongOrder(coverage, hints, gridCoverageRequest.getOutputCRS());
        }
        if (incrementalOutputSize == null) {
            WCSUtils.checkOutputLimits(wcs, coverage.getGridGeometry().getGridRange2D(), coverage.getRenderedImage().getSampleModel());
        } else {
            incrementalOutputSize.addSize(coverage);
        }
        if (reader instanceof StructuredGridCoverage2DReader && coverageDimensions != null) {
            Map map = coverage.getProperties();
            for (DimensionBean coverageDimension : coverageDimensions) {
                helper.setCoverageDimensionProperty(map, gridCoverageRequest, coverageDimension);
            }
            coverage = coverageFactory.create(coverage.getName(), coverage.getRenderedImage(), coverage.getEnvelope(), coverage.getSampleDimensions(), null, map);
        }
        return coverage;
    }

    private WCSDimensionsSubsetHelper parseGridCoverageRequest(CoverageInfo ci, GridCoverage2DReader reader, GetCoverageType request, Map<String, ExtensionItemType> extensions) throws IOException {
        final CoordinateReferenceSystem subsettingCRS = extractSubsettingCRS(reader, extensions);
        final CoordinateReferenceSystem outputCRS = extractOutputCRS(reader, extensions, subsettingCRS);
        WCSDimensionsSubsetHelper subsetHelper = new WCSDimensionsSubsetHelper(reader, request, ci, subsettingCRS, envelopeDimensionsMapper);
        GridCoverageRequest requestSubset = subsetHelper.createGridCoverageRequestSubset();
        final Map<String, InterpolationPolicy> axesInterpolations = extractInterpolation(reader, extensions);
        final Interpolation spatialInterpolation = extractSpatialInterpolation(axesInterpolations, reader.getOriginalEnvelope());
        assert spatialInterpolation != null;
        GridCoverageRequest gcr = new GridCoverageRequest();
        gcr.setOutputCRS(outputCRS);
        gcr.setSpatialInterpolation(spatialInterpolation);
        gcr.setSpatialSubset(requestSubset.getSpatialSubset());
        gcr.setTemporalSubset(requestSubset.getTemporalSubset());
        gcr.setElevationSubset(requestSubset.getElevationSubset());
        gcr.setDimensionsSubset(requestSubset.getDimensionsSubset());
        gcr.setFilter(request.getFilter());
        subsetHelper.setGridCoverageRequest(gcr);
        return subsetHelper;
    }

    private GridCoverage2D enforceLatLongOrder(GridCoverage2D coverage, final Hints hints, final CoordinateReferenceSystem outputCRS) throws Exception {
        final Integer epsgCode = CRS.lookupEpsgCode(outputCRS, false);
        if (epsgCode != null && epsgCode > 0) {
            CoordinateReferenceSystem finalCRS = latLonCRSFactory.createCoordinateReferenceSystem(SRS_STARTER + epsgCode);
            if (CRS.getAxisOrder(outputCRS).equals(CRS.getAxisOrder(finalCRS))) {
                return coverage;
            }
            final AffineTransform g2w = new AffineTransform((AffineTransform2D) coverage.getGridGeometry().getGridToCRS2D());
            g2w.preConcatenate(CoverageUtilities.AXES_SWAP);
            final GridGeometry2D finalGG = new GridGeometry2D(coverage.getGridGeometry().getGridRange(), PixelInCell.CELL_CENTER, new AffineTransform2D(g2w), finalCRS, hints);
            coverage = CoverageFactoryFinder.getGridCoverageFactory(hints).create(coverage.getName(), coverage.getRenderedImage(), finalGG, coverage.getSampleDimensions(), new GridCoverage[] { coverage }, coverage.getProperties());
        }
        return coverage;
    }

    private boolean requestingLatLonAxesOrder(CoordinateReferenceSystem outputCRS) {
        try {
            final Integer epsgCode = CRS.lookupEpsgCode(outputCRS, false);
            if (epsgCode != null && epsgCode > 0) {
                CoordinateReferenceSystem originalCRS = latLonCRSFactory.createCoordinateReferenceSystem(SRS_STARTER + epsgCode);
                return !CRS.getAxisOrder(originalCRS).equals(CRS.getAxisOrder(outputCRS));
            }
        } catch (FactoryException e) {
            LOGGER.log(Level.INFO, e.getMessage(), e);
            return false;
        }
        return false;
    }

    private Interpolation extractSpatialInterpolation(Map<String, InterpolationPolicy> axesInterpolations, Envelope envelope) {
        Interpolation interpolation = InterpolationPolicy.getDefaultPolicy().getInterpolation();
        for (String axisLabel : axesInterpolations.keySet()) {
            final int index = envelopeDimensionsMapper.getAxisIndex(envelope, axisLabel);
            if (index == 0 || index == 1) {
                interpolation = axesInterpolations.get(axisLabel).getInterpolation();
                break;
            }
        }
        return interpolation;
    }

    private GridCoverage2D readCoverage(CoverageInfo cinfo, GridCoverageRequest request, GridCoverage2DReader reader, Hints hints, ImageSizeRecorder incrementalInputSize) throws Exception {
        Interpolation spatialInterpolation = request.getSpatialInterpolation();
        Utilities.ensureNonNull("interpolation", spatialInterpolation);
        final CoordinateReferenceSystem coverageCRS = reader.getCoordinateReferenceSystem();
        GeneralEnvelope subset = request.getSpatialSubset();
        if (!CRS.equalsIgnoreMetadata(subset.getCoordinateReferenceSystem(), coverageCRS)) {
            subset = CRS.transform(subset, coverageCRS);
            subset.setCoordinateReferenceSystem(coverageCRS);
        }
        final GridGeometry2D readGG;
        CoordinateReferenceSystem outputCRS = request.getOutputCRS();
        final boolean equalsMetadata = CRS.equalsIgnoreMetadata(outputCRS, coverageCRS);
        boolean sameCRS;
        try {
            sameCRS = equalsMetadata ? true : CRS.findMathTransform(outputCRS, coverageCRS, true).isIdentity();
        } catch (FactoryException e1) {
            final IOException ioe = new IOException();
            ioe.initCause(e1);
            throw ioe;
        }
        final ParameterValueGroup readParametersDescriptor = reader.getFormat().getReadParameters();
        GeneralParameterValue[] readParameters = CoverageUtils.getParameters(readParametersDescriptor, cinfo.getParameters());
        readParameters = (readParameters != null ? readParameters : new GeneralParameterValue[0]);
        readParameters = WCSUtils.replaceParameter(readParameters, Boolean.FALSE, AbstractGridFormat.USE_JAI_IMAGEREAD);
        if (request.getTemporalSubset() != null) {
            List<GeneralParameterDescriptor> descriptors = readParametersDescriptor.getDescriptor().descriptors();
            List<Object> times = new ArrayList<Object>();
            times.add(request.getTemporalSubset());
            readParameters = CoverageUtils.mergeParameter(descriptors, readParameters, times, "TIME", "Time");
        }
        if (request.getElevationSubset() != null) {
            List<GeneralParameterDescriptor> descriptors = readParametersDescriptor.getDescriptor().descriptors();
            List<Object> elevations = new ArrayList<Object>();
            elevations.add(request.getElevationSubset());
            readParameters = CoverageUtils.mergeParameter(descriptors, readParameters, elevations, "ELEVATION", "Elevation");
        }
        if (request.getFilter() != null) {
            List<GeneralParameterDescriptor> descriptors = readParametersDescriptor.getDescriptor().descriptors();
            readParameters = CoverageUtils.mergeParameter(descriptors, readParameters, request.getFilter(), "Filter");
        }
        if (request.getDimensionsSubset() != null && !request.getDimensionsSubset().isEmpty()) {
            final List<GeneralParameterDescriptor> descriptors = new ArrayList<GeneralParameterDescriptor>(readParametersDescriptor.getDescriptor().descriptors());
            Set<ParameterDescriptor<List>> dynamicParameters = reader.getDynamicParameters();
            descriptors.addAll(dynamicParameters);
            Map<String, List<Object>> dimensionsSubset = request.getDimensionsSubset();
            Set<String> dimensionKeys = dimensionsSubset.keySet();
            for (String key : dimensionKeys) {
                List<Object> dimValues = dimensionsSubset.get(key);
                readParameters = CoverageUtils.mergeParameter(descriptors, readParameters, dimValues, key);
            }
        }
        GridCoverage2D coverage = null;
        if (sameCRS) {
            readGG = new GridGeometry2D(PixelInCell.CELL_CENTER, reader.getOriginalGridToWorld(PixelInCell.CELL_CENTER), subset, hints);
        } else {
            Rectangle rasterRange = CRS.transform(reader.getOriginalGridToWorld(PixelInCell.CELL_CORNER).inverse(), subset).toRectangle2D().getBounds();
            rasterRange.setBounds(rasterRange.x - 10, rasterRange.y - 10, rasterRange.width + 20, rasterRange.height + 20);
            rasterRange = rasterRange.intersection((GridEnvelope2D) reader.getOriginalGridRange());
            readGG = new GridGeometry2D(new GridEnvelope2D(rasterRange), PixelInCell.CELL_CENTER, reader.getOriginalGridToWorld(PixelInCell.CELL_CENTER), coverageCRS, hints);
        }
        WCSUtils.checkInputLimits(wcs, cinfo, reader, readGG);
        coverage = RequestUtils.readBestCoverage(reader, readParameters, readGG, spatialInterpolation, hints);
        if (coverage != null) {
            if (incrementalInputSize == null) {
                WCSUtils.checkInputLimits(wcs, coverage);
            } else {
                incrementalInputSize.addSize(coverage);
            }
        }
        return coverage;
    }

    private CoordinateReferenceSystem extractOutputCRS(GridCoverage2DReader reader, Map<String, ExtensionItemType> extensions, CoordinateReferenceSystem subsettingCRS) {
        return extractCRSInternal(extensions, subsettingCRS, true);
    }

    private CoordinateReferenceSystem extractCRSInternal(Map<String, ExtensionItemType> extensions, CoordinateReferenceSystem defaultCRS, boolean isOutputCRS) throws WCS20Exception {
        Utilities.ensureNonNull("defaultCRS", defaultCRS);
        final String identifier = isOutputCRS ? "outputCrs" : "subsettingCrs";
        if (extensions == null || extensions.size() == 0 || !extensions.containsKey(identifier)) {
            return defaultCRS;
        }
        final ExtensionItemType extensionItem = extensions.get(identifier);
        if (extensionItem.getName().equals(identifier)) {
            String crsName = extensionItem.getSimpleContent();
            if (crsName == null) {
                throw new WCS20Exception(identifier + " was null", WCS20ExceptionCode.NotACrs, "null");
            }
            try {
                return lonLatCRSFactory.createCoordinateReferenceSystem(crsName);
            } catch (Exception e) {
                final WCS20Exception exception = new WCS20Exception("Invalid " + identifier, isOutputCRS ? WCS20Exception.WCS20ExceptionCode.OutputCrsNotSupported : WCS20Exception.WCS20ExceptionCode.SubsettingCrsNotSupported, crsName);
                exception.initCause(e);
                throw exception;
            }
        }
        return defaultCRS;
    }

    private CoordinateReferenceSystem extractSubsettingCRS(GridCoverage2DReader reader, Map<String, ExtensionItemType> extensions) {
        Utilities.ensureNonNull("reader", reader);
        return extractCRSInternal(extensions, reader.getCoordinateReferenceSystem(), false);
    }

    private Map<String, ExtensionItemType> extractExtensions(GetCoverageType request) {
        Utilities.ensureNonNull("request", request);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Extracting extensions from provided request");
        }
        final ExtensionType extension = request.getExtension();
        final Map<String, ExtensionItemType> parsedExtensions = new HashMap<String, ExtensionItemType>();
        if (extension != null) {
            final EList<ExtensionItemType> extensions = extension.getContents();
            for (final ExtensionItemType extensionItem : extensions) {
                final String extensionName = extensionItem.getName();
                if (extensionName == null || extensionName.length() <= 0) {
                    throw new WCS20Exception("Null extension");
                }
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Parsing extension " + extensionName);
                }
                if (extensionName.equals("subsettingCrs")) {
                    parsedExtensions.put("subsettingCrs", extensionItem);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Added extension subsettingCrs");
                    }
                } else if (extensionName.equals("outputCrs")) {
                    parsedExtensions.put("outputCrs", extensionItem);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Added extension outputCrs");
                    }
                } else if (extensionName.equals("Scaling")) {
                    parsedExtensions.put("Scaling", extensionItem);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Added extension Scaling");
                    }
                } else if (extensionName.equals("Interpolation")) {
                    parsedExtensions.put("Interpolation", extensionItem);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Added extension Interpolation");
                    }
                } else if (extensionName.equals("rangeSubset") || extensionName.equals("RangeSubset")) {
                    parsedExtensions.put("rangeSubset", extensionItem);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Added extension rangeSubset");
                    }
                }
            }
        } else if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("No extensions found in provided request");
        }
        return parsedExtensions;
    }

    private Map<String, InterpolationPolicy> extractInterpolation(GridCoverage2DReader reader, Map<String, ExtensionItemType> extensions) {
        final Map<String, InterpolationPolicy> returnValue = new HashMap<String, InterpolationPolicy>();
        final Envelope envelope = reader.getOriginalEnvelope();
        final List<String> axesNames = envelopeDimensionsMapper.getAxesNames(envelope, true);
        for (String axisName : axesNames) {
            returnValue.put(axisName, InterpolationPolicy.getDefaultPolicy());
        }
        if (extensions == null || extensions.size() == 0 || !extensions.containsKey("Interpolation")) {
            return returnValue;
        }
        final ExtensionItemType extensionItem = extensions.get("Interpolation");
        InterpolationType interpolationType = (InterpolationType) extensionItem.getObjectContent();
        if (interpolationType.getInterpolationMethod() != null) {
            InterpolationMethodType method = interpolationType.getInterpolationMethod();
            InterpolationPolicy policy = InterpolationPolicy.getPolicy(method);
            for (String axisName : axesNames) {
                returnValue.put(axisName, policy);
            }
        } else if (interpolationType.getInterpolationAxes() != null) {
            final List<String> foundAxes = new ArrayList<String>();
            final InterpolationAxesType axes = interpolationType.getInterpolationAxes();
            for (InterpolationAxisType axisInterpolation : axes.getInterpolationAxis()) {
                final String method = axisInterpolation.getInterpolationMethod();
                final InterpolationPolicy policy = InterpolationPolicy.getPolicy(method);
                final String axis = axisInterpolation.getAxis();
                int index = axis.lastIndexOf("/");
                final String axisLabel = (index >= 0 ? axis.substring(index + 1, axis.length()) : axis);
                if (foundAxes.contains(axisLabel)) {
                    throw new WCS20Exception("Duplicated axis", WCS20Exception.WCS20ExceptionCode.InvalidAxisLabel, axisLabel);
                }
                foundAxes.add(axisLabel);
                if (!returnValue.containsKey(axisLabel)) {
                    throw new WCS20Exception("Invalid axes URI", WCS20Exception.WCS20ExceptionCode.NoSuchAxis, axisLabel);
                }
                returnValue.put(axisLabel, policy);
            }
        }
        InterpolationPolicy lat = null, lon = null;
        if (returnValue.containsKey("Long")) {
            lon = returnValue.get("Long");
        }
        if (returnValue.containsKey("Lat")) {
            lat = returnValue.get("Lat");
        }
        if (lat != lon) {
            throw new WCS20Exception("We don't support different interpolations on Lat,Lon", WCS20Exception.WCS20ExceptionCode.InterpolationMethodNotSupported, "");
        }
        returnValue.get("Lat");
        return returnValue;
    }

    private GridCoverage2D handleReprojection(GridCoverage2D coverage, CoordinateReferenceSystem targetCRS, Interpolation spatialInterpolation, Hints hints) {
        Utilities.ensureNonNull("interpolation", spatialInterpolation);
        if (CRS.equalsIgnoreMetadata(coverage.getCoordinateReferenceSystem2D(), targetCRS)) {
            return coverage;
        }
        final CoverageProcessor processor = hints == null ? CoverageProcessor.getInstance() : CoverageProcessor.getInstance(hints);
        final Operation operation = processor.getOperation("Resample");
        final ParameterValueGroup parameters = operation.getParameters();
        parameters.parameter("Source").setValue(coverage);
        parameters.parameter("CoordinateReferenceSystem").setValue(targetCRS);
        parameters.parameter("GridGeometry").setValue(null);
        parameters.parameter("InterpolationType").setValue(spatialInterpolation);
        return (GridCoverage2D) processor.doOperation(parameters);
    }

    private GridCoverage2D handleRangeSubsettingExtension(GridCoverage2D coverage, Map<String, ExtensionItemType> extensions, Hints hints) {
        final List<String> returnValue = new ArrayList<String>();
        if (extensions == null || extensions.size() == 0 || !extensions.containsKey("rangeSubset")) {
            return coverage;
        }
        final GridSampleDimension[] bands = coverage.getSampleDimensions();
        final List<String> bandsNames = new ArrayList<String>();
        for (GridSampleDimension band : bands) {
            bandsNames.add(band.getDescription().toString());
        }
        final ExtensionItemType extensionItem = extensions.get("rangeSubset");
        assert extensionItem != null;
        final RangeSubsetType range = (RangeSubsetType) extensionItem.getObjectContent();
        for (RangeItemType rangeItem : range.getRangeItems()) {
            final String rangeComponent = rangeItem.getRangeComponent();
            if (rangeComponent == null) {
                final RangeIntervalType rangeInterval = rangeItem.getRangeInterval();
                final String startRangeComponent = rangeInterval.getStartComponent();
                final String endRangeComponent = rangeInterval.getEndComponent();
                if (!bandsNames.contains(startRangeComponent)) {
                    throw new WCS20Exception("Invalid Band Name", WCS20Exception.WCS20ExceptionCode.NoSuchField, rangeComponent);
                }
                if (!bandsNames.contains(endRangeComponent)) {
                    throw new WCS20Exception("Invalid Band Name", WCS20Exception.WCS20ExceptionCode.NoSuchField, rangeComponent);
                }
                boolean add = false;
                for (SampleDimension sd : bands) {
                    if (sd instanceof GridSampleDimension) {
                        final GridSampleDimension band = (GridSampleDimension) sd;
                        final String name = band.getDescription().toString();
                        if (name.equals(startRangeComponent)) {
                            returnValue.add(startRangeComponent);
                            add = true;
                        } else if (name.equals(endRangeComponent)) {
                            returnValue.add(endRangeComponent);
                            add = false;
                        } else if (add) {
                            returnValue.add(name);
                        }
                    }
                }
                if (add) {
                    throw new IllegalStateException("Unable to close range in band identifiers");
                }
            } else {
                if (bandsNames.contains(rangeComponent)) {
                    returnValue.add(rangeComponent);
                } else {
                    throw new WCS20Exception("Invalid Band Name", WCS20Exception.WCS20ExceptionCode.NoSuchField, rangeComponent);
                }
            }
        }
        if (returnValue.isEmpty()) {
            return coverage;
        }
        final int[] indexes = new int[returnValue.size()];
        int i = 0;
        for (String bandName : returnValue) {
            indexes[i++] = bandsNames.indexOf(bandName);
        }
        if (coverage.getNumSampleDimensions() < indexes.length) {
            WCSUtils.checkOutputLimits(wcs, coverage, indexes);
        }
        return (GridCoverage2D) WCSUtils.bandSelect(coverage, indexes);
    }

    private GridCoverage2D handleSubsettingExtension(GridCoverage2D coverage, GeneralEnvelope subset, Hints hints) {
        if (subset != null) {
            return WCSUtils.crop(coverage, subset);
        }
        return coverage;
    }

    private GridCoverage2D handleScaling(GridCoverage2D coverage, Map<String, ExtensionItemType> extensions, Interpolation spatialInterpolation, Hints hints) {
        Utilities.ensureNonNull("interpolation", spatialInterpolation);
        if (extensions == null || extensions.size() == 0 || !extensions.containsKey("Scaling")) {
            if (spatialInterpolation instanceof InterpolationNearest) {
                return coverage;
            } else {
                final Operation operation = CoverageProcessor.getInstance().getOperation("Warp");
                final ParameterValueGroup parameters = operation.getParameters();
                parameters.parameter("Source").setValue(coverage);
                parameters.parameter("warp").setValue(new WarpAffine(AffineTransform.getScaleInstance(1, 1)));
                parameters.parameter("interpolation").setValue(spatialInterpolation != null ? spatialInterpolation : InterpolationPolicy.getDefaultPolicy().getInterpolation());
                parameters.parameter("backgroundValues").setValue(CoverageUtilities.getBackgroundValues(coverage));
                return (GridCoverage2D) CoverageProcessor.getInstance().doOperation(parameters, hints);
            }
        }
        final ExtensionItemType extensionItem = extensions.get("Scaling");
        assert extensionItem != null;
        ScalingType scaling = (ScalingType) extensionItem.getObjectContent();
        if (scaling == null) {
            throw new IllegalStateException("Scaling extension contained a null ScalingType");
        }
        final ScalingPolicy scalingPolicy = ScalingPolicy.getPolicy(scaling);
        return scalingPolicy.scale(coverage, scaling, spatialInterpolation, hints, wcs);
    }

    static class ImageSizeRecorder {

        private long incrementalSize = 0;

        private final long limit;

        private final boolean input;

        ImageSizeRecorder(long limit, boolean input) {
            this.limit = limit;
            this.input = input;
        }

        public void addSize(GridCoverage2D coverage) {
            incrementalSize += getCoverageSize(coverage.getGridGeometry().getGridRange2D(), coverage.getRenderedImage().getSampleModel());
            isSizeExceeded();
        }

        public long finalSize() {
            return incrementalSize;
        }

        private void isSizeExceeded() {
            if (limit > 0 && incrementalSize > limit) {
                throw new WcsException("This request is trying to " + (input ? "read" : "generate") + " too much data, the limit is " + formatBytes(limit) + " but the actual amount of bytes to be " + (input ? "read" : "written") + " is " + formatBytes(incrementalSize));
            }
        }

        public void reset() {
            incrementalSize = 0;
        }

        private static long getCoverageSize(GridEnvelope2D envelope, SampleModel sm) {
            final long pixelsNumber = computePixelsNumber(envelope);
            long pixelSize = 0;
            final int numBands = sm.getNumBands();
            for (int i = 0; i < numBands; i++) {
                pixelSize += sm.getSampleSize(i);
            }
            return pixelsNumber * pixelSize / 8;
        }

        private static long computePixelsNumber(GridEnvelope2D rasterEnvelope) {
            long pixelsNumber = 1;
            final int dimensions = rasterEnvelope.getDimension();
            for (int i = 0; i < dimensions; i++) {
                pixelsNumber *= rasterEnvelope.getSpan(i);
            }
            return pixelsNumber;
        }
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1024 * 1024) {
            return new DecimalFormat("#.##").format(bytes / 1024.0) + "KB";
        } else {
            return new DecimalFormat("#.##").format(bytes / 1024.0 / 1024.0) + "MB";
        }
    }
}
