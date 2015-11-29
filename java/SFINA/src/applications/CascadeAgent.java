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
package applications;

import core.SFINAAgent;
import input.SystemParameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import network.FlowNetwork;
import network.Node;
import org.apache.log4j.Logger;
import power.PowerFlowType;
import power.PowerNodeType;
import power.input.PowerNodeState;
import protopeer.util.quantities.Time;

/**
 *
 * @author Ben
 */
public class CascadeAgent extends BenchmarkSFINAAgent{
    
    private static final Logger logger = Logger.getLogger(CascadeAgent.class);
    
    public CascadeAgent(String experimentID, 
            String peersLogDirectory, 
            Time bootstrapTime, 
            Time runTime, 
            String timeTokenName, 
            String experimentConfigurationFilesLocation, 
            String experimentOutputFilesLocation,
            String nodesLocation, 
            String linksLocation, 
            String nodesFlowLocation, 
            String linksFlowLocation, 
            String eventsLocation, 
            String columnSeparator, 
            String missingValue,
            HashMap systemParameters){
        super(experimentID,
                peersLogDirectory,
                bootstrapTime,
                runTime,
                timeTokenName,
                experimentConfigurationFilesLocation,
                experimentOutputFilesLocation,
                nodesLocation,
                linksLocation,
                nodesFlowLocation,
                linksFlowLocation,
                eventsLocation,
                columnSeparator,
                missingValue,
                systemParameters);
    }
    
    @Override
    public void runCascade(){
        int iter = 0;
        ArrayList<ArrayList<FlowNetwork>> islandBuffer = new ArrayList<>(); // row index is iteration, each entry is island to be treated at this iteration
        islandBuffer.add(getFlowNetwork().getIslands());
        while(!islandBuffer.get(iter).isEmpty()){
            System.out.println("---------------------\n---- Iteration " + (iter+1) + " ----");
            islandBuffer.add(new ArrayList<>()); // List of islands for next iteration (iter+1)
            for(int i=0; i < islandBuffer.get(iter).size(); i++){ // go through islands at current iteration
                FlowNetwork currentIsland = islandBuffer.get(iter).get(i);
                System.out.println("---> Treating island with " + currentIsland.getNodes().size() + " nodes.");
                
                boolean converged = flowConvergenceAlgo(currentIsland); // do flow analysis
                System.out.println("=> converged " + converged);
                if (converged){
                    
                    // if mitigation strategy is implemented
                    mitigateOverload(currentIsland);
                    
                    boolean linkOverloaded = linkOverloadAlgo(currentIsland);
                    //boolean nodeOverloaded = nodeOverloadAlgo(currentIsland);
                    System.out.println("=> overloaded " + linkOverloaded);
                    if(linkOverloaded){
                        // add islands of the current island to next iteration
                        for (FlowNetwork net : currentIsland.getIslands())
                            islandBuffer.get(iter+1).add(net);
                    }
                    else
                        getTemporalIslandStatus().get(getSimulationTime()).put(currentIsland, true);
                }
                else{
                    getTemporalIslandStatus().get(getSimulationTime()).put(currentIsland, false);
                    for (Node node : currentIsland.getNodes())
                        node.setActivated(false);
                }
            }
            
            // Output network snapshot of current iteration
            setCurrentIteration(iter+1);
            outputNetworkData();
            
            // Go to next iteration if there were islands added to it
            iter++;
        }
    }
    
    @Override
    public boolean flowConvergenceAlgo(FlowNetwork flowNetwork){
        boolean converged = false;
        switch(getDomain()){
            case POWER:
                // blackout if isolated node
                if(flowNetwork.getNodes().size() == 1)
                    return false;
                
                // first implementation
                //converged = powerGenLimitAlgo(flowNetwork);
                
                // Jose's implementation
                ArrayList<Node> generators = new ArrayList();
                Node slack = null;
                for (Node node : flowNetwork.getNodes()){
                    if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.GENERATOR))
                        generators.add(node);
                    if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.SLACK_BUS)){
                        slack = node;
                    }
                }
                // Sort generators by max power output
                Collections.sort(generators, new Comparator<Node>(){
                    public int compare(Node node1, Node node2) {
                    return Double.compare((Double)node1.getProperty(PowerNodeState.POWER_MAX_REAL), (Double)node2.getProperty(PowerNodeState.POWER_MAX_REAL));
                    }
                }.reversed());
                
                if (slack == null){
                    if (generators.size() == 0)
                        return false; // blackout if no generator in island
                    else{
                        slack = generators.get(0);
                        slack.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.SLACK_BUS);
                        generators.remove(0);
                    }
                }
                
                boolean limViolation = true;
                while(limViolation){
                    converged = runFlowAnalysis(flowNetwork);
                    System.out.println("....converged " + converged);
                    if (converged){
                        limViolation = powerGenLimitAlgo2(flowNetwork, slack);
                        
                        // Without the following line big cases (like polish) even DC doesn't converge..
                        if(getSystemParameters().get(SystemParameter.FLOW_TYPE).equals(PowerFlowType.DC))
                            limViolation=false;
                        
                        if (limViolation){
                            converged = false;
                            if(generators.size() > 0){ // make next bus a slack
                                slack = generators.get(0);
                                slack.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.SLACK_BUS);
                                generators.remove(0);
                            }
                            else{
                                System.out.println("....no more generators");
                                return false; // all generator limits were hit -> blackout
                            }
                        }
                    }
                    else{
                        converged = powerLoadShedAlgo(flowNetwork);
                        if (!converged)
                            return false; // blackout if no convergence after load shedding
                    }
                }
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
        return converged;
    }
    
    public boolean powerGenLimitAlgo2(FlowNetwork flowNetwork, Node slack){
        boolean limViolation = false;
        if ((Double)slack.getProperty(PowerNodeState.POWER_GENERATION_REAL) > (Double)slack.getProperty(PowerNodeState.POWER_MAX_REAL)){
            slack.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, slack.getProperty(PowerNodeState.POWER_MAX_REAL));
            limViolation = true;
        }
        if ((Double)slack.getProperty(PowerNodeState.POWER_GENERATION_REAL) < (Double)slack.getProperty(PowerNodeState.POWER_MIN_REAL)){
            slack.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, slack.getProperty(PowerNodeState.POWER_MIN_REAL));
            limViolation = true;
        }
        slack.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.GENERATOR);
        if (limViolation)
            System.out.println("....generator limit violated at node " + slack.getIndex());
        else
            System.out.println("....no generator limit violated");
        return limViolation;
    }
    
    public boolean powerLoadShedAlgo(FlowNetwork flowNetwork){
        boolean converged = false;
        int loadIter = 0;
        int maxLoadShedIterations = 15; // according to paper
        double loadReductionFactor = 0.05; // 5%, according to paper
        while (!converged && loadIter < maxLoadShedIterations){
            System.out.println("....Doing load shedding at iteration " + loadIter);
            for (Node node : flowNetwork.getNodes()){
                node.replacePropertyElement(PowerNodeState.POWER_DEMAND_REAL, (Double)node.getProperty(PowerNodeState.POWER_DEMAND_REAL)*(1.0-loadReductionFactor));
                node.replacePropertyElement(PowerNodeState.POWER_DEMAND_REACTIVE, (Double)node.getProperty(PowerNodeState.POWER_DEMAND_REACTIVE)*(1.0-loadReductionFactor));
            }
            converged = runFlowAnalysis(flowNetwork);
            loadIter++;
        }
        return converged;
    }
    
    public boolean powerGenLimitAlgo(FlowNetwork flowNetwork){
        boolean converged = false;
        
        // necessary, because if island has slack, we don't consider it in the gen balancing, but it doesn't have to be a blackout (can still do load shedding) -> see check nrGen == 0 below
        boolean hasSlack = false;

        // extract generators in island and treat slack bus if existent
        ArrayList<Node> generators = new ArrayList<>();
        for (Node node : flowNetwork.getNodes()){
            if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.GENERATOR))
                generators.add(node);
            // Set Slack to limits and make normal generator. It will not be considered for generation balancing later.
            if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.SLACK_BUS)){ // Set Slack to limits and make normal generator
                converged = runFlowAnalysis(flowNetwork);
                hasSlack = true;
                node.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.GENERATOR);
                if ((Double)node.getProperty(PowerNodeState.POWER_GENERATION_REAL) > (Double)node.getProperty(PowerNodeState.POWER_MAX_REAL)){
                    node.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, node.getProperty(PowerNodeState.POWER_MAX_REAL));
                    converged = false;
                }
                    // Could also do these adjustments by means of events                            
                    //events.add(new Event(getSimulationTime(),EventType.FLOW,NetworkComponent.NODE,node.getIndex(),PowerNodeState.POWER_GENERATION_REAL,node.getProperty(PowerNodeState.POWER_MAX_REAL)));
                if ((Double)node.getProperty(PowerNodeState.POWER_GENERATION_REAL) < (Double)node.getProperty(PowerNodeState.POWER_MIN_REAL)){
                    node.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, node.getProperty(PowerNodeState.POWER_MIN_REAL));
                    converged = false;
                }
            }
        }

        // set needed variables
        int genIterator = 0;
        int nrGen = generators.size();

        // blackout if no generator in island
        if (nrGen == 0 && !hasSlack)
            return false; 

        // Sort Generators according to their real power generation in descending order
        Collections.sort(generators, new Comparator<Node>(){
            public int compare(Node node1, Node node2) {
            return Double.compare((Double)node1.getProperty(PowerNodeState.POWER_MAX_REAL), (Double)node2.getProperty(PowerNodeState.POWER_MAX_REAL));
            }
        }.reversed());

        // Gen balancing
        while (!converged && genIterator < nrGen){
            System.out.println("..Doing gen balancing");
            Node currentGen = generators.get(genIterator);
            
            currentGen.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.SLACK_BUS);
            converged = runFlowAnalysis(flowNetwork);
            currentGen.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.GENERATOR);
            
            if ((Double)currentGen.getProperty(PowerNodeState.POWER_GENERATION_REAL) > (Double)currentGen.getProperty(PowerNodeState.POWER_MAX_REAL)){
                currentGen.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, currentGen.getProperty(PowerNodeState.POWER_MAX_REAL));
                converged = false; // limits violated
            }
            if ((Double)currentGen.getProperty(PowerNodeState.POWER_GENERATION_REAL) < (Double)currentGen.getProperty(PowerNodeState.POWER_MIN_REAL)){
                currentGen.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, currentGen.getProperty(PowerNodeState.POWER_MIN_REAL));
                converged = false; // limits violated
            }

            // Jose didn't check the reactive limits
//            if ((Double)currentGen.getProperty(PowerNodeState.POWER_GENERATION_REACTIVE) > (Double)currentGen.getProperty(PowerNodeState.POWER_MAX_REACTIVE)){
//                currentGen.replacePropertyElement(PowerNodeState.POWER_GENERATION_REACTIVE, currentGen.getProperty(PowerNodeState.POWER_MAX_REACTIVE));
//                converged = false; // limits violated
//            }
//            if ((Double)currentGen.getProperty(PowerNodeState.POWER_GENERATION_REACTIVE) < (Double)currentGen.getProperty(PowerNodeState.POWER_MIN_REACTIVE)){
//                currentGen.replacePropertyElement(PowerNodeState.POWER_GENERATION_REACTIVE, currentGen.getProperty(PowerNodeState.POWER_MIN_REACTIVE));
//                converged = false; // limits violated
//            }
            genIterator++;
        }
        
        // Load shedding
        if (!converged){
            generators.get(0).replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.SLACK_BUS);
            converged = powerLoadShedAlgo(flowNetwork);
        }
        
        return converged;
    }
    
}
