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
package interdependent;

import interdependent.SimulationAgentInterdependence;
import event.Event;
import event.EventType;
import event.NetworkComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import network.FlowNetwork;
import network.Link;
import network.LinkState;
import network.Node;
import network.NodeState;
import org.apache.log4j.Logger;
import power.input.PowerLinkState;
import power.input.PowerNodeState;
import power.input.PowerNodeType;
import protopeer.util.quantities.Time;

/**
 *
 * @author Ben
 */
public class PowerExchangeAgentNew extends SimulationAgentInterdependence{
    
    private static final Logger logger = Logger.getLogger(PowerExchangeAgentNew.class);
    
    // Keeps track of which islands exist in which time step and iteration
    private HashMap<Integer, HashMap<Integer,LinkedHashMap<FlowNetwork, Boolean>>> temporalIslandStatus;
    
    public PowerExchangeAgentNew(
            String experimentID,
            Time bootstrapTime, 
            Time runTime){
        super(experimentID, bootstrapTime, runTime);
        temporalIslandStatus = new HashMap();
    }
    
    @Override
    public void runInitialOperations(){        
        temporalIslandStatus.put(getSimulationTime(), new HashMap()); 
    }    
    
    /**
     * Implements cascade as a result of overloaded links. 
     * Continues until system stabilizes, i.e. no more link overloads occur. 
     * Calls mitigateOverload method before finally calling linkOverload method, 
     * therefore mitigation strategies can be implemented by overwriting the method mitigateOverload.
     * Variable int iter = getIteration()-1
     */
    @Override
    public void runFlowAnalysis(){
        logger.info("\n----> Iteration " + getIteration() + " at network " + getPeer().getNetworkAddress() + " <----");
        temporalIslandStatus.get(getSimulationTime()).put(getIteration(), new LinkedHashMap());
        
        // Go through all disconnected components (i.e. islands) of current iteration and perform flow analysis
        for(FlowNetwork island : this.getFlowNetwork().computeIslands()){
            logger.info("treating island with " + island.getNodes().size() + " nodes");
            boolean converged = flowConvergenceStrategy(island); 
            if(converged){
                mitigateOverload(island);
                boolean linkOverloaded = linkOverload(island);
                boolean nodeOverloaded = nodeOverload(island);
                if(!(linkOverloaded || nodeOverloaded))
                    temporalIslandStatus.get(getSimulationTime()).get(getIteration()).put(island, true);
            }
            else
                updateNonConvergedIsland(island);
        }
    }
    
    /**
     * Substracts the incoming real power flow from the power demand of the node. 
     * Also possible: add to power generation (but not all nodes are generators).
     * If the flow is positive, power demand is reduced. If negative, demand is increased.
     * @param node
     * @param incomingFlow
     * @return 
     */
    @Override
    public Event updateEndNodeWithFlow(Node node, Double incomingFlow){
        logger.debug("Incoming link with non-zero flow at node " + node.getIndex() + " at peer " + this.getPeer().getNetworkAddress());
        Enum nodeState = PowerNodeState.POWER_DEMAND_REAL;
        logger.debug("incoming = " + incomingFlow);
        Double newValue = (Double)node.getProperty(nodeState) - incomingFlow;
        return new Event(this.getSimulationTime(), EventType.FLOW, NetworkComponent.NODE, node.getIndex(), nodeState, newValue);
    }
    
    @Override
    public Event updateEndNodeWhenFailure(Node node) {
        logger.debug("Incoming link which was deactivated or with deactivated start node at node " + node.getIndex());
        // Deactivates the node, if the connected InterLink or its start node is deactivated.
        return new Event(this.getSimulationTime(), EventType.TOPOLOGY, NetworkComponent.NODE, node.getIndex(), NodeState.STATUS, false);
    }
    
    /**
     * Adjust the network part which didn't converge.
     * Sets a negative flow to outgoing interLinks from non-converged island and 
     * reduces its own power demand accordingly (i.e. effectively doing load shedding).
     * @param net 
     */
    public void updateNonConvergedIsland(FlowNetwork net){
        logger.debug("Updating non-converged island at peer " + this.getPeer().getNetworkAddress());
        
        // Ensures that a fixed quantity is requested from other net, distributed over all available links.
        double neededFlow = - 5.0;
        Enum nodeState = PowerNodeState.POWER_DEMAND_REAL;
        ArrayList<Link> links = new ArrayList<>();
        for(Link link : this.getOutgoingInterLinks()){
            if(net.getNodes().contains(link.getStartNode()) && ((Double)link.getStartNode().getProperty(nodeState) > 0.0)){
                links.add(link);
            }
        }
        
        // Getting power from other network and reducing own power demand accordingly (like load shedding).
        // Until power demand in this island reached 0. Then deactivating the nodes.
        if(!links.isEmpty()){
            for(Link link : links){
                Node node = link.getStartNode();
                Double oldValue = (Double)node.getProperty(nodeState);
                Double newValue = Math.max(0.0, oldValue + neededFlow/links.size());
                logger.debug("old = " + oldValue + ", new = " + newValue);
                //link.setFlow(newValue - oldValue); Instead now (but can't implicitly set flow by event...):
                Event event = new Event(getSimulationTime(), EventType.FLOW, NetworkComponent.LINK, link.getIndex(), PowerLinkState.POWER_FLOW_FROM_REAL, newValue - oldValue);
                injectInterdependentEvent(event);
                logger.debug("Setting flow of interLink " + link.getIndex() + " to " + (newValue - oldValue));
                queueEvent(new Event(this.getSimulationTime(), EventType.FLOW, NetworkComponent.NODE, node.getIndex(), nodeState, newValue));
            }
        }
        else{
            for (Node node : net.getNodes()){
                Event event = new Event(getSimulationTime(), EventType.TOPOLOGY, NetworkComponent.NODE, node.getIndex(), NodeState.STATUS, false);
                queueEvent(event);
            }
            temporalIslandStatus.get(getSimulationTime()).get(getIteration()).put(net, false);
        }
    }
    
    /**
     * Domain specific strategy and/or necessary adjustments before backend is executed. 
     *
     * @param flowNetwork
     * @return true if flow analysis finally converged, else false
     */ 
    public boolean flowConvergenceStrategy(FlowNetwork flowNetwork){
        if (flowNetwork.getNodes().size() < 2)
            return false;
        boolean hasGen = false;
        Node gen = null;
        boolean hasSlack = false;
        for (Node node : flowNetwork.getNodes()){
            if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.GENERATOR)){
                hasGen = true;
                gen = node;
            }
            if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.SLACK_BUS))
                hasSlack = true;
        }
        if(hasGen){
            if(!hasSlack)
                gen.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.SLACK_BUS);
            return this.getFlowDomainAgent().flowAnalysis(flowNetwork);
        }
        else
            return false;
    }
    
    /**
     * Method to mitigate overload. Strategy to respond to (possible) overloading can be implemented here. This method is called before the OverLoadAlgo is called which deactivates affected links/nodes.
     * @param flowNetwork 
     */
    public void mitigateOverload(FlowNetwork flowNetwork){
        
    }
    
    /**
     * Checks link limits. If a limit is violated, an event 
     * for deactivating the link at the current simulation time is created.
     * @param flowNetwork
     * @return if overload happened
     */
    public boolean linkOverload(FlowNetwork flowNetwork){
        boolean overloaded = false;
        for (Link link : flowNetwork.getLinks()){
            if(link.isActivated() && Math.abs(link.getFlow()) > Math.abs(link.getCapacity())){
                logger.info("..violating link " + link.getIndex() + " limit: " + link.getFlow() + " > " + link.getCapacity());
                updateOverloadLink(link);
                overloaded = true;
            }
        }
        return overloaded;
    }
    
    /**
     * Changes the parameters of the link after an overload happened.
     * @param link which is overloaded
     */
    public void updateOverloadLink(Link link){
        Event event = new Event(getSimulationTime(),EventType.TOPOLOGY,NetworkComponent.LINK,link.getIndex(),LinkState.STATUS,false);
        this.queueEvent(event);
    }
    
    /**
     * Checks node limits. If a limit is violated, an event 
     * for deactivating the node at the current simulation time is created.
     * @param flowNetwork
     * @return if overload happened
     */
    public boolean nodeOverload(FlowNetwork flowNetwork){
        boolean overloaded = false;
        for (Node node : flowNetwork.getNodes()){
            if(node.isActivated() && Math.abs(node.getFlow()) > Math.abs(node.getCapacity())){
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
     * @param node which is overloaded
     */
    public void updateOverloadNode(Node node){
        logger.info("..doing nothing to overloaded node.");
    }
    
    /**
     * Prints final islands in each time step to console
     */
    private void logFinalIslands(){
        String log = "----> " + temporalIslandStatus.get(getSimulationTime()).get(getIteration()).size() + " island(s) at iteration " + getIteration() + ":\n";
        String nodesInIsland;
        for (FlowNetwork net : temporalIslandStatus.get(getSimulationTime()).get(getIteration()).keySet()){
            nodesInIsland = "";
            for (Node node : net.getNodes())
                nodesInIsland += node.getIndex() + ", ";
            log += "    - " + net.getNodes().size() + " Node(s) (" + nodesInIsland + ")";
            if(temporalIslandStatus.get(getSimulationTime()).get(getIteration()).get(net))
                log += " -> Converged :)\n";
            if(!temporalIslandStatus.get(getSimulationTime()).get(getIteration()).get(net))
                log += " -> Blackout\n";
        }
        logger.info(log);
    }    

}