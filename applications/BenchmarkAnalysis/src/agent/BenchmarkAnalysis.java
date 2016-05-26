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

import agent.BenchmarkSimulationAgent;
import event.Event;
import event.EventType;
import event.NetworkComponent;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import network.FlowNetwork;
import network.Link;
import network.LinkState;
import network.Node;
import org.apache.log4j.Logger;
import power.input.PowerNodeState;
import protopeer.util.quantities.Time;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import input.TopologyLoader;
import power.backend.PowerBackendParameter;
import power.input.PowerFlowLoader;

/**
 * Cascade if link limits violated. Domain independent.
 *
 * @author Ben
 */
public class BenchmarkAnalysis extends BenchmarkSimulationAgent {

    private static final Logger logger = Logger.getLogger(BenchmarkAnalysis.class);
    private HashMap<Integer, LinkedHashMap<FlowNetwork, Boolean>> temporalIslandStatus = new HashMap();
    public ArrayList<ArrayList<Double>> powerPerIteration = new ArrayList<ArrayList<Double>>(); //2D array to register power per iteration
    public ArrayList<ArrayList<Double>> linkPerIteration = new ArrayList<ArrayList<Double>>(); //2D array to register power per iteration
    public ArrayList<Double> spectralRadius = new ArrayList<Double>();
    public ArrayList<Integer> macroCount = new ArrayList<Integer>(); //registers number of iterations particular line removal proceeds to
    public ArrayList<ArrayList<Double>> linkStatusPerContingency = new ArrayList<ArrayList<Double>>();
    public ArrayList<ArrayList<Boolean>> linkStatusPerContingencyBool = new ArrayList<ArrayList<Boolean>>();

    public BenchmarkAnalysis(String experimentID,
            Time bootstrapTime,
            Time runTime) {
        super(experimentID,
                bootstrapTime,
                runTime);
    }

    /**
     * Implements cascade as a result of overloaded links. Continues until
     * system stabilizes, i.e. no more link overloads occur. Calls
     * mitigateOverload method before finally calling linkOverload method,
     * therefore mitigation strategies can be implemented. Variable int iter =
     * getIteration()-1
     */
    @Override
    public void runFlowAnalysis() {

        int globalCount = 0;
        int localCount = 0;

        for (int j = 1; j < getFlowNetwork().getLinks().size() + 1; j++) {

            //Sets the capacity before executing simulation
            this.setCapacityByToleranceParameter();

            getFlowNetwork().deactivateLink(Integer.toString(j));

            //ArrayList<ArrayList<FlowNetwork>> flownetworkBuffer = new ArrayList<>();
            int iter = 0;
            temporalIslandStatus.put(getSimulationTime(), new LinkedHashMap());

            // This list keeps track of islands where overloads happened
            ArrayList<FlowNetwork> islandBuffer = new ArrayList<>();
            islandBuffer.add(getFlowNetwork());

            while (!islandBuffer.isEmpty()) {
                logger.info("----> Iteration " + (getIteration()) + " <----");

                // Extract islands from the networks where link/node overloads happened
                ArrayList<FlowNetwork> currentIterationIslands = new ArrayList<>();
                for (FlowNetwork net : islandBuffer) {
                    for (FlowNetwork subnet : net.computeIslands()) {
                        currentIterationIslands.add(subnet);
                    }
                }
                islandBuffer.clear();

                // Go through all disconnected components (i.e. islands) of current iteration and perform flow analysis
                for (FlowNetwork currentIsland : currentIterationIslands) {
                    logger.info("treating island with " + currentIsland.getNodes().size() + " nodes");
                    boolean converged = flowConvergenceStrategy(currentIsland);
                    if (converged) {
                        mitigateOverload(currentIsland);
                        boolean linkOverloaded = linkOverload(currentIsland);
                        boolean nodeOverloaded = nodeOverload(currentIsland);
                        if (linkOverloaded || nodeOverloaded) {
                            islandBuffer.add(currentIsland);
                        } else {
                            temporalIslandStatus.get(getSimulationTime()).put(currentIsland, true);
                        }
                    } else {
                        updateNonConvergedIsland(currentIsland);
                        temporalIslandStatus.get(getSimulationTime()).put(currentIsland, false);
                    }
                }

                //add new array to store power for the next iteration
                powerPerIteration.add(new ArrayList<Double>());

                for (Link lin : getFlowNetwork().getLinks()) {
                    if (lin.getFlow() > lin.getCapacity()) {
                        powerPerIteration.get(globalCount).add(lin.getCapacity());
                    } else {
                        powerPerIteration.get(globalCount).add(lin.getFlow());
                    }
                }

                //add new array to store link for next iteration
                linkPerIteration.add(new ArrayList<Double>());

                for (Link lin : getFlowNetwork().getLinks()) {

                    linkPerIteration.get(globalCount).add((lin.isActivated()) ? 1.0 : 0.0);

                }

                //store spectral radius in each iteration
                spectralRadius.add(getSpectralRadius());

                globalCount++;

                // Output data at current iteration and go to next one
                nextIteration();

                // Go to next iteration if there were islands added to it
                iter++;

                // deactivate all overloaded nodes/links
                this.executeAllEvents();

            }

            //get link sttaus
            //getLinkStatus(localCount);
            
            linkStatusPerContingency.add(new ArrayList<Double>());

            for (Link lin : getFlowNetwork().getLinks()) {
                if (lin.isActivated()==false) {
                
                linkStatusPerContingency.get(localCount).add(1.0);
                }else{
                    linkStatusPerContingency.get(localCount).add(lin.getFlow() / lin.getCapacity());
            //linkStatusPerContingency.get(index).add((lin.isActivated()) ? 1.0 : 0.0);

                }
            }

            localCount++;

            logFinalIslands();

            macroCount.add(iter);

            //restoring the network
            loadInputData("time_1");

        }
        System.out.print("Length of Spectral Radius...." + spectralRadius.size() + "..");

    }

    public void restoreNetwork() {
        //restoring the links
        for (Link linkNew : getFlowNetwork().getLinks()) {
            linkNew.setActivated(true);
        }
        //restoring nodes, and its properties (... more properties also needs to be restored!)
        for (Node nodeNew : getFlowNetwork().getNodes()) {
            nodeNew.setActivated(true);
            nodeNew.replacePropertyElement(PowerNodeState.VOLTAGE_ANGLE, 0.0);
            nodeNew.replacePropertyElement(PowerNodeState.VOLTAGE_MAGNITUDE, 1.0);

        }
    }

    public double getSpectralRadius() {
        final int N = getFlowNetwork().getNodes().size();
        EigenvalueDecomposition E
                = new EigenvalueDecomposition(calculateAdjacencyMatrix().plus(calculateAdjacencyMatrix().transpose()).times(0.5));
        double[] d = E.getRealEigenvalues();
        double maxEigen = d[N - 1];
        return maxEigen;

    }

    public void getLinkStatus(int index) {
        linkStatusPerContingencyBool.add(new ArrayList<Boolean>());
        linkStatusPerContingency.add(new ArrayList<Double>());

        for (Link lin : getFlowNetwork().getLinks()) {

            linkStatusPerContingencyBool.get(index).add(lin.isActivated());
            linkStatusPerContingency.get(index).add(lin.getFlow() / lin.getCapacity());
            //linkStatusPerContingency.get(index).add((lin.isActivated()) ? 1.0 : 0.0);

        }

    }

    /**
     * Domain specific strategy and/or necessary adjustments before backend is
     * executed.
     *
     * @param flowNetwork
     * @return true if flow analysis finally converged, else false
     */
    public boolean flowConvergenceStrategy(FlowNetwork flowNetwork) {
        switch (this.getDomain()) {
            case POWER:
                break;
            case GAS:
                logger.debug("This domain is not supported at this moment");
                break;
            case WATER:
                logger.debug("This domain is not supported at this moment");
                break;
            case TRANSPORTATION:
                logger.debug("This domain is not supported at this moment");
                break;
            default:
                logger.debug("This domain is not supported at this moment");
        }
        return callBackend(flowNetwork);
    }

    /**
     * Method to mitigate overload. Strategy to respond to (possible)
     * overloading can be implemented here. This method is called before the
     * OverLoadAlgo is called which deactivates affected links/nodes.
     *
     * @param flowNetwork
     */
    public void mitigateOverload(FlowNetwork flowNetwork) {

    }

    /**
     * Checks link limits. If a limit is violated, an event for deactivating the
     * link at the current simulation time is created.
     *
     * @param flowNetwork
     * @return if overload happened
     */
    public boolean linkOverload(FlowNetwork flowNetwork) {
        boolean overloaded = false;
        for (Link link : flowNetwork.getLinks()) {
            if (link.isActivated() && link.getFlow() > link.getCapacity()) {
                logger.info("..violating link " + link.getIndex() + " limit: " + link.getFlow() + " > " + link.getCapacity());
                updateOverloadLink(link);
                overloaded = true;
            }
        }
        return overloaded;
    }

    /**
     * Changes the parameters of the link after an overload happened.
     *
     * @param link which is overloaded
     */
    public void updateOverloadLink(Link link) {
        Event event = new Event(getSimulationTime(), EventType.TOPOLOGY, NetworkComponent.LINK, link.getIndex(), LinkState.STATUS, false);
        this.getEvents().add(event);
    }

    /**
     * Checks node limits. If a limit is violated, an event for deactivating the
     * node at the current simulation time is created.
     *
     * @param flowNetwork
     * @return if overload happened
     */
    public boolean nodeOverload(FlowNetwork flowNetwork) {
        boolean overloaded = false;
        for (Node node : flowNetwork.getNodes()) {
            if (node.isActivated() && node.getFlow() > node.getCapacity()) {
                logger.info("..violating node " + node.getIndex() + " limit: " + node.getFlow() + " > " + node.getCapacity());
                updateOverloadNode(node);
                // Uncomment if node overload should be included
                // overloaded = true;
            }
        }
        return overloaded;
    }

    /**
     * Changes the parameters of the node after an overload happened.
     *
     * @param node which is overloaded
     */
    public void updateOverloadNode(Node node) {
        logger.info("..doing nothing to overloaded node.");
    }

    /**
     * Adjust the network part which didn't converge.
     *
     * @param flowNetwork
     */
    public void updateNonConvergedIsland(FlowNetwork flowNetwork) {
        logger.info("..not changing anything in non-converged part of the network.");
    }

    /**
     * Prints final islands in each time step to console
     */
    private void logFinalIslands() {
        String log = "--------------> " + temporalIslandStatus.get(getSimulationTime()).size() + " final island(s):\n";
        String nodesInIsland;
        for (FlowNetwork net : temporalIslandStatus.get(getSimulationTime()).keySet()) {
            nodesInIsland = "";
            for (Node node : net.getNodes()) {
                nodesInIsland += node.getIndex() + ", ";
            }
            log += "    - " + net.getNodes().size() + " Node(s) (" + nodesInIsland + ")";
            if (temporalIslandStatus.get(getSimulationTime()).get(net)) {
                log += " -> Converged :)\n";
            }
            if (!temporalIslandStatus.get(getSimulationTime()).get(net)) {
                log += " -> Blackout\n";
            }
        }
        logger.info(log);
    }

    private void setCapacityByToleranceParameter() {
        double toleranceParameter = getToleranceParameter();
        boolean capacityNotSet = false;
        for (Link link : getFlowNetwork().getLinks()) {
            if (link.getCapacity() == 0.0) {
                capacityNotSet = true;
            } else {
                capacityNotSet = false; //true when alpha needed
            }
        }
        if (capacityNotSet) {
            callBackend(getFlowNetwork());
            for (Link link : getFlowNetwork().getLinks()) {
                link.setCapacity(toleranceParameter * link.getFlow());
            }
        }
    }

    public double getToleranceParameter() {
        return (Double) this.getBackendParameters().get(PowerBackendParameter.TOLERANCE_PARAMETER);
    }

}
