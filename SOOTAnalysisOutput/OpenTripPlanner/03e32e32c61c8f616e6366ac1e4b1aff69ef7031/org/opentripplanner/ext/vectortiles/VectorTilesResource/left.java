package org.opentripplanner.ext.vectortiles;

import com.wdtinc.mapbox_vector_tile.VectorTile;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.geotools.geometry.Envelope2D;
import org.locationtech.jts.geom.Envelope;
import org.opentripplanner.common.geometry.WebMercatorTile;
import org.opentripplanner.ext.vectortiles.layers.stations.StationsLayerBuilder;
import org.opentripplanner.ext.vectortiles.layers.stops.StopsLayerBuilder;
import org.opentripplanner.ext.vectortiles.layers.vehicleparkings.VehicleParkingGroupsLayerBuilder;
import org.opentripplanner.ext.vectortiles.layers.vehicleparkings.VehicleParkingsLayerBuilder;
import org.opentripplanner.ext.vectortiles.layers.vehiclerental.VehicleRentalLayerBuilder;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.standalone.api.OtpServerRequestContext;
import org.opentripplanner.standalone.config.VectorTileConfig;
import org.opentripplanner.transit.service.TransitService;
import org.opentripplanner.util.WorldEnvelope;

@Path("/routers/{ignoreRouterId}/vectorTiles")
public class VectorTilesResource {

    private static final Map<LayerType, LayerBuilderFactory> layers = new HashMap<>();

    private final OtpServerRequestContext serverContext;

    private final String ignoreRouterId;

    static {
        layers.put(LayerType.Stop, StopsLayerBuilder::new);
        layers.put(LayerType.Station, StationsLayerBuilder::new);
        layers.put(LayerType.VehicleRental, VehicleRentalLayerBuilder::new);
        layers.put(LayerType.VehicleParking, VehicleParkingsLayerBuilder::new);
        layers.put(LayerType.VehicleParkingGroup, VehicleParkingGroupsLayerBuilder::new);
    }

    public VectorTilesResource(@Context OtpServerRequestContext serverContext, @Deprecated @PathParam("ignoreRouterId") String ignoreRouterId) {
        this.serverContext = serverContext;
        this.ignoreRouterId = ignoreRouterId;
    }

    @GET
    @Path("/{layers}/{z}/{x}/{y}.pbf")
    @Produces("application/x-protobuf")
    public Response tileGet(@PathParam("x") int x, @PathParam("y") int y, @PathParam("z") int z, @PathParam("layers") String requestedLayers) {
        VectorTile.Tile.Builder mvtBuilder = VectorTile.Tile.newBuilder();
        if (z < VectorTileConfig.MIN_ZOOM) {
            return Response.status(Response.Status.OK).entity(mvtBuilder.build().toByteArray()).build();
        }
        Envelope2D env = WebMercatorTile.tile2Envelope(x, y, z);
        Envelope envelope = new Envelope(env.getMaxX(), env.getMinX(), env.getMaxY(), env.getMinY());
        List<String> layers = Arrays.asList(requestedLayers.split(","));
        int cacheMaxSeconds = Integer.MAX_VALUE;
        for (LayerParameters layerParameters : serverContext.routerConfig().vectorTileLayers().layers()) {
            if (layers.contains(layerParameters.name()) && layerParameters.minZoom() <= z && z <= layerParameters.maxZoom()) {
                cacheMaxSeconds = Math.min(cacheMaxSeconds, layerParameters.cacheMaxSeconds());
                mvtBuilder.addLayers(VectorTilesResource.layers.get(LayerType.valueOf(layerParameters.type())).create(serverContext.graph(), serverContext.transitService(), layerParameters).build(envelope, layerParameters));
            }
        }
        CacheControl cacheControl = new CacheControl();
        if (cacheMaxSeconds != Integer.MAX_VALUE) {
            cacheControl.setMaxAge(cacheMaxSeconds);
        }
        byte[] bytes = mvtBuilder.build().toByteArray();
        return Response.status(Response.Status.OK).cacheControl(cacheControl).entity(bytes).build();
    }

    @GET
    @Path("/{layers}/tilejson.json")
    @Produces(MediaType.APPLICATION_JSON)
    public TileJson getTileJson(@Context UriInfo uri, @Context HttpHeaders headers, @PathParam("layers") String requestedLayers) {
        return new TileJson(serverContext.graph(), serverContext.transitService(), uri, headers, requestedLayers);
    }

    private String getBaseAddress(UriInfo uri, HttpHeaders headers) {
        String protocol;
        if (headers.getRequestHeader("X-Forwarded-Proto") != null) {
            protocol = headers.getRequestHeader("X-Forwarded-Proto").get(0);
        } else {
            protocol = uri.getRequestUri().getScheme();
        }
        String host;
        if (headers.getRequestHeader("X-Forwarded-Host") != null) {
            host = headers.getRequestHeader("X-Forwarded-Host").get(0);
        } else if (headers.getRequestHeader("Host") != null) {
            host = headers.getRequestHeader("Host").get(0);
        } else {
            host = uri.getBaseUri().getHost() + ":" + uri.getBaseUri().getPort();
        }
        return protocol + "://" + host;
    }

    enum LayerType {

        Stop, Station, VehicleRental, VehicleParking, VehicleParkingGroup
    }

    public interface LayersParameters {

        List<LayerParameters> layers();
    }

    public interface LayerParameters {

        String name();

        String type();

        String mapper();

        int maxZoom();

        int minZoom();

        int cacheMaxSeconds();

        double expansionFactor();
    }

    private class TileJson implements Serializable {

        @SuppressWarnings("unused")
        public final String tilejson = "2.2.0";

        @SuppressWarnings("unused")
        public final String scheme = "xyz";

        @SuppressWarnings("unused")
        public final int minzoom = VectorTileConfig.MIN_ZOOM;

        @SuppressWarnings("unused")
        public final int maxzoom = VectorTileConfig.MAX_ZOOM;

        public final String name = "OpenTripPlanner";

        public final String attribution;

        public final String[] tiles;

        public final double[] bounds;

        public final double[] center;

        private TileJson(Graph graph, TransitService transitService, UriInfo uri, HttpHeaders headers, String layers) {
            attribution = transitService.getFeedIds().stream().map(transitService::getFeedInfo).filter(Predicate.not(Objects::isNull)).map(feedInfo -> "<a href='" + feedInfo.getPublisherUrl() + "'>" + feedInfo.getPublisherName() + "</a>").collect(Collectors.joining(", "));
            tiles = new String[] { getBaseAddress(uri, headers) + "/otp/routers/" + ignoreRouterId + "/vectorTiles/" + layers + "/{z}/{x}/{y}.pbf" };
            WorldEnvelope envelope = graph.getEnvelope();
            bounds = new double[] { envelope.getLowerLeftLongitude(), envelope.getLowerLeftLatitude(), envelope.getUpperRightLongitude(), envelope.getUpperRightLatitude() };
            center = transitService.getCenter().map(coordinate -> new double[] { coordinate.x, coordinate.y, 9 }).orElse(null);
        }
    }
}
