/*
 * Copyright (C) 2015 SFINA Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package agent;

import event.Event;
import event.EventType;
import event.NetworkComponent;
import input.TopologyLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import network.FlowNetwork;
import network.Link;
import network.LinkState;
import network.NodeState;
import network.Node;
import org.apache.log4j.Logger;
import power.input.PowerFlowLoader;
import protopeer.util.quantities.Time;

/**
 * Cascade if link limits violated. Domain independent.
 *
 * @author Manish
 */
public class Interdependent extends BenchmarkSimulationAgent {

    static FlowNetwork net;
    private static final Logger logger = Logger.getLogger(Interdependent.class);
    private HashMap<Integer, LinkedHashMap<FlowNetwork, Boolean>> temporalIslandStatus = new HashMap();

    public Interdependent(String experimentID,
            Time bootstrapTime,
            Time runTime) {
        super(experimentID,
                bootstrapTime,
                runTime);
    }
    public ArrayList<String> nodeRemovedA;
    public ArrayList<String> nodeRemovedB;
    public ArrayList<Integer> listTotalIsland;

    /**
     * Implements cascade as a result of node overload. Continues until system
     * stabilizes.
     *
     */
    @Override
    public void runFlowAnalysis() {
        int iter = 0;

        // Load topology
        net = new FlowNetwork();
        //File inputpathNodes = new File(getExperimentInputFilesLocation()+"time_1/topology/nodes.txt");
        //File inputpathLinks = new File(getExperimentInputFilesLocation()+"/experiment-interdep/input/time_1/topology/links.txt");

        TopologyLoader topologyLoader = new TopologyLoader(net, ",");

        topologyLoader.loadNodes("experiments/experiment-standard/peer-0/input/experiment-interdep/nodes.txt");
        topologyLoader.loadLinks("experiments/experiment-standard/peer-0/input/experiment-interdep/links.txt");

        nodeRemovedA = new ArrayList<String>();
        nodeRemovedB = new ArrayList<String>();

        listTotalIsland = new ArrayList<Integer>();
        listTotalIsland.add(0); //initializing array lists 
        //listTotalIsland.add(2); //initializing another array lists

        //getFlowNetwork().deactivateNode("4"); //Initial attack (discuss with Ben or Evangelos)
        while (true) {

            ArrayList<FlowNetwork> islandBufferA = getFlowNetwork().computeIslands();

            logger.info("----> Iteration " + (iter + 1) + " <----");

            for (int i = 0; i < islandBufferA.size(); i++) { // go through islands at current iteration for standard topology             
                FlowNetwork islandA = islandBufferA.get(i);
                int sizeA = islandA.getNodes().size();
                if (sizeA == 1) {
                    for (Node nodeA : islandA.getNodes()) {
                        nodeRemovedA.add(nodeA.getIndex());
                    }
                }
            }
            for (String nA : nodeRemovedA) {
                net.deactivateNode(nA);
            }

            ArrayList<FlowNetwork> islandBufferB = net.computeIslands();

            for (int k = 0; k < islandBufferB.size(); k++) { //go through islands at current iteration for interdependent topology
                FlowNetwork islandB = islandBufferB.get(k);
                int sizeB = islandB.getNodes().size();
                if (sizeB == 1) {
                    for (Node nodeB : islandB.getNodes()) {
                        nodeRemovedB.add(nodeB.getIndex());
                        Event event = new Event(getSimulationTime(), EventType.TOPOLOGY, NetworkComponent.NODE, nodeB.getIndex(), NodeState.STATUS, false);

                        this.executeEvent(islandB, event); //problem
                    }
                }
            }
            for (String nB : nodeRemovedB) {
                getFlowNetwork().deactivateNode(nB);
            }
            // Output data at current iteration and go to next one
            nextIteration();
            // Go to next iteration if there were islands added to it
            iter++;

            listTotalIsland.add(islandBufferA.size());
            logger.info("Total Number of Islands ---->" + listTotalIsland.get(iter) + "<----");

            if (listTotalIsland.get(iter) == listTotalIsland.get(iter - 1)) {
                break;
            }

        }

    }

    /**
     * Checks link limits. If a limit is violated, an event is executed which
     * deactivates the link.
     *
     * @param flowNetwork
     * @return if overload happened
     */
    public boolean linkOverload(FlowNetwork flowNetwork) {
        boolean overloaded = false;
        for (Link link : flowNetwork.getLinks()) {
            if (link.isActivated() && link.getFlow() > link.getCapacity()) {
                logger.info("..violating link " + link.getIndex() + " limit: " + link.getFlow() + " > " + link.getCapacity());
                Event event = new Event(getSimulationTime(), EventType.TOPOLOGY, NetworkComponent.LINK, link.getIndex(), LinkState.STATUS, false);
                this.getEvents().add(event);
                overloaded = true;
            }
        }
        if (overloaded) {
            this.executeAllEvents();
        }
        return overloaded;
    }

    @Override
    public void runInitialOperations() {
        // inherited from BenchmarkSFINAAgent
        this.initMeasurementVariables();
        this.saveStartTime();
    }
}
