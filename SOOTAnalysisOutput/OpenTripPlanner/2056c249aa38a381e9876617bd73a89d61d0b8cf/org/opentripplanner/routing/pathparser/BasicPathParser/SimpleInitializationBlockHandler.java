package org.opentripplanner.routing.pathparser;

import org.opentripplanner.routing.automata.DFA;
import org.opentripplanner.routing.automata.Nonterminal;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.location.StreetLocation;
import org.opentripplanner.routing.vertextype.BikeRentalStationVertex;
import org.opentripplanner.routing.vertextype.OffboardVertex;
import org.opentripplanner.routing.vertextype.OnboardVertex;
import org.opentripplanner.routing.vertextype.StreetVertex;
import static org.opentripplanner.routing.automata.Nonterminal.*;

public class BasicPathParser extends PathParser {

    private static final int STATION = 1;

    private static final int TRANSIT = 2;

    private static final DFA DFA;

    static {
        Nonterminal bikeNonStreet = star(choice(StreetEdge.CLASS_CROSSING, StreetEdge.CLASS_OTHERPATH));
        Nonterminal optionalNontransitLeg = seq(bikeNonStreet, star(plus(StreetEdge.CLASS_STREET), star(StreetEdge.CLASS_CROSSING), StreetEdge.CLASS_OTHERPATH, bikeNonStreet), seq(star(StreetEdge.CLASS_STREET), bikeNonStreet));
        Nonterminal transitLeg = seq(plus(STATION), plus(TRANSIT), plus(STATION));
        Nonterminal departOnStreetItinerary = seq(optionalNontransitLeg, star(transitLeg, optionalNontransitLeg));
        Nonterminal onBoardDepartTransitLeg = seq(plus(TRANSIT), plus(STATION));
        Nonterminal departOnBoardItinerary = seq(onBoardDepartTransitLeg, optionalNontransitLeg, star(transitLeg, optionalNontransitLeg));
        Nonterminal itinerary = choice(departOnStreetItinerary, departOnBoardItinerary);
        DFA = itinerary.toDFA().minimize();
        System.out.println(DFA.toGraphViz());
        System.out.println(DFA.dumpTable());
    }

    @Override
    protected DFA getDFA() {
        return DFA;
    }

    @Override
    public int terminalFor(State state) {
        Vertex v = state.getVertex();
        if (v instanceof StreetVertex || v instanceof StreetLocation) {
            TraverseModeSet modes = state.getOptions().getModes();
            if (modes.contains(TraverseMode.BICYCLE) && (!modes.contains(TraverseMode.WALK) || !state.isBikeRenting())) {
                Edge edge = state.getBackEdge();
                if (edge instanceof StreetEdge) {
                    int cls = ((StreetEdge) edge).getStreetClass();
                    return cls & StreetEdge.CROSSING_CLASS_MASK;
                } else {
                    return StreetEdge.CLASS_OTHERPATH;
                }
            } else {
                return StreetEdge.CLASS_OTHERPATH;
            }
        }
        if (v instanceof OnboardVertex)
            return TRANSIT;
        if (v instanceof OffboardVertex)
            return STATION;
        if (v instanceof BikeRentalStationVertex)
            return StreetEdge.CLASS_OTHERPATH;
        else
            throw new RuntimeException("failed to tokenize path");
    }
}