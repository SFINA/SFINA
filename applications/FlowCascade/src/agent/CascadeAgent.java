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

import agent.BenchmarkSFINAAgent;
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
import org.apache.log4j.Logger;
import protopeer.util.quantities.Time;

/**
 * Cascade if link limits violated. Domain independent.
 * @author Ben
 */
public class CascadeAgent extends BenchmarkSFINAAgent{
    
    private static final Logger logger = Logger.getLogger(CascadeAgent.class);
    private HashMap<Integer,LinkedHashMap<FlowNetwork, Boolean>> temporalIslandStatus = new HashMap();
    
    public CascadeAgent(String experimentID,
            Time bootstrapTime, 
            Time runTime){
            super(experimentID,
                bootstrapTime,
                runTime);
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
        temporalIslandStatus.put(getSimulationTime(), new LinkedHashMap());
        
        // This list keeps track of islands where overloads happened
        ArrayList<FlowNetwork> islandBuffer = new ArrayList<>();
        islandBuffer.add(getFlowNetwork());
        
        while(!islandBuffer.isEmpty()){
            logger.info("----> Iteration " + (getIteration()) + " <----");
            
            // Extract islands from the networks where link/node overloads happened
            ArrayList<FlowNetwork> currentIterationIslands = new ArrayList<>();
            for(FlowNetwork net : islandBuffer){
                for(FlowNetwork subnet : net.computeIslands())
                    currentIterationIslands.add(subnet);
            }
            islandBuffer.clear();
            
            // Go through all disconnected components (i.e. islands) of current iteration and perform flow analysis
            for(FlowNetwork currentIsland : currentIterationIslands){
                logger.info("treating island with " + currentIsland.getNodes().size() + " nodes");
                boolean converged = flowConvergenceStrategy(currentIsland); 
                if(converged){
                    mitigateOverload(currentIsland);
                    boolean linkOverloaded = linkOverload(currentIsland);
                    boolean nodeOverloaded = nodeOverload(currentIsland);
                    if(linkOverloaded || nodeOverloaded){
                        islandBuffer.add(currentIsland);
                    }
                    else
                        temporalIslandStatus.get(getSimulationTime()).put(currentIsland, true);
                }
                else{
                    temporalIslandStatus.get(getSimulationTime()).put(currentIsland, false);
                    for(Link link : currentIsland.getLinks()){
                        Event event = new Event(getSimulationTime(),EventType.TOPOLOGY,NetworkComponent.LINK,link.getIndex(),LinkState.STATUS,false);
                        this.executeEvent(currentIsland, event);
                    }
                }
            }
            
            // Output data at current iteration and go to next one
            nextIteration();
   
            // deactivate all overloaded nodes/links
            this.executeAllEvents();
        }
        logFinalIslands();
    }
    
    
    /**
     * Domain specific strategy and/or necessary adjustments before backend is executed. 
     *
     * @param flowNetwork
     * @return true if flow analysis finally converged, else false
     */ 
    public boolean flowConvergenceStrategy(FlowNetwork flowNetwork){
        return callBackend(flowNetwork);
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
            if(link.isActivated() && link.getFlow() > link.getCapacity()){
                logger.info("..violating link " + link.getIndex() + " limit: " + link.getFlow() + " > " + link.getCapacity());
                Event event = new Event(getSimulationTime(),EventType.TOPOLOGY,NetworkComponent.LINK,link.getIndex(),LinkState.STATUS,false);
                this.getEvents().add(event);
                overloaded = true;
            }
        }
        return overloaded;
    }
    
    /**
     * Checks node limits. If a limit is violated, an event 
     * for deactivating the node at the current simulation time is created.
     * @param flowNetwork
     * @return if overload happened
     */
    public boolean nodeOverload(FlowNetwork flowNetwork){
        boolean overloaded = false;
        return overloaded;
    }
    
    /**
     * Prints final islands in each time step to console
     */
    private void logFinalIslands(){
        String log = "--------------> " + temporalIslandStatus.get(getSimulationTime()).size() + " final island(s):\n";
        String nodesInIsland;
        for (FlowNetwork net : temporalIslandStatus.get(getSimulationTime()).keySet()){
            nodesInIsland = "";
            for (Node node : net.getNodes())
                nodesInIsland += node.getIndex() + ", ";
            log += "    - " + net.getNodes().size() + " Node(s) (" + nodesInIsland + ")";
            if(temporalIslandStatus.get(getSimulationTime()).get(net))
                log += " -> Converged :)\n";
            if(!temporalIslandStatus.get(getSimulationTime()).get(net))
                log += " -> Blackout\n";
        }
        logger.info(log);
    }    
}
