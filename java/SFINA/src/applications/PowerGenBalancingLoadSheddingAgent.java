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
 * Strategy to make power load flow analysis converge. Balancing generation limits and shedding load.
 * @author Ben
 */
public class PowerGenBalancingLoadSheddingAgent extends CascadeAgent{
    
    public PowerGenBalancingLoadSheddingAgent(String experimentID, 
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
    
    private static final Logger logger = Logger.getLogger(PowerGenBalancingLoadSheddingAgent.class);
    
    /**
     * Jose's strategy to meet generator limits and make flow analysis converge by load shedding.
     * @param flowNetwork
     * @return true if power flow analysis converged, else false.
     */
    @Override
    public boolean flowConvergenceStrategy(FlowNetwork flowNetwork){
        boolean converged = false;
        switch(getDomain()){
            case POWER: 
                // blackout if isolated node
                if(flowNetwork.getNodes().size() == 1)
                    return converged;
                
                // or for example to get all generators and the slack bus if it exists
                ArrayList<Node> generators = new ArrayList();
                Node slack = null;
                for (Node node : flowNetwork.getNodes()){
                    if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.GENERATOR))
                        generators.add(node);
                    if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.SLACK_BUS)){
                        slack = node;
                    }
                }
                
                // To sort generators by max power output
                Collections.sort(generators, new Comparator<Node>(){
                    public int compare(Node node1, Node node2) {
                    return Double.compare((Double)node1.getProperty(PowerNodeState.POWER_MAX_REAL), (Double)node2.getProperty(PowerNodeState.POWER_MAX_REAL));
                    }
                }.reversed());
                
                // check if there's a slack in the island, if not make the generator with biggest power output to a slack bus
                if (slack == null){
                    if (generators.size() == 0)
                        return converged; // blackout if no generator in island
                    else{
                        slack = generators.get(0);
                        // this is how one changes node/link properties
                        slack.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.SLACK_BUS);
                        generators.remove(0);
                    }
                }
                
                boolean limViolation = true;
                while(limViolation){
                    converged = callBackend(flowNetwork);
                    if(this.getIfConsoleOutput()) System.out.println("....converged " + converged);
                    if (converged){
                        limViolation = powerGenLimitAlgo(flowNetwork, slack);
                        
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
                                if(this.getIfConsoleOutput()) System.out.println("....no more generators");
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
    
    private boolean powerGenLimitAlgo(FlowNetwork flowNetwork, Node slack){
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
            if(this.getIfConsoleOutput()) System.out.println("....generator limit violated at node " + slack.getIndex());
        else
            if(this.getIfConsoleOutput()) System.out.println("....no generator limit violated");
        return limViolation;
    }
    
    private boolean powerLoadShedAlgo(FlowNetwork flowNetwork){
        boolean converged = false;
        int loadIter = 0;
        int maxLoadShedIterations = 15; // according to paper
        double loadReductionFactor = 0.05; // 5%, according to paper
        while (!converged && loadIter < maxLoadShedIterations){
            if(this.getIfConsoleOutput()) System.out.println("....Doing load shedding at iteration " + loadIter);
            for (Node node : flowNetwork.getNodes()){
                node.replacePropertyElement(PowerNodeState.POWER_DEMAND_REAL, (Double)node.getProperty(PowerNodeState.POWER_DEMAND_REAL)*(1.0-loadReductionFactor));
                node.replacePropertyElement(PowerNodeState.POWER_DEMAND_REACTIVE, (Double)node.getProperty(PowerNodeState.POWER_DEMAND_REACTIVE)*(1.0-loadReductionFactor));
            }
            converged = callBackend(flowNetwork);
            loadIter++;
        }
        return converged;
    }
}
