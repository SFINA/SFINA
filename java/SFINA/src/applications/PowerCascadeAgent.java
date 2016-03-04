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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import network.FlowNetwork;
import network.Link;
import network.Node;
import org.apache.log4j.Logger;
import power.PowerFlowType;
import power.PowerNodeType;
import power.backend.PowerBackendParameter;
import power.input.PowerNodeState;
import protopeer.measurement.MeasurementFileDumper;
import protopeer.measurement.MeasurementLog;
import protopeer.measurement.MeasurementLoggerListener;
import protopeer.util.quantities.Time;

/**
 * Strategy to make power load flow analysis converge. Balancing generation limits and shedding load.
 * @author Ben
 */
public class PowerCascadeAgent extends CascadeAgent{
    
    private static final Logger logger = Logger.getLogger(PowerCascadeAgent.class);
    private double toleranceParameter;
    private PowerFlowType flowType;
    private Double relRateChangePerEpoch;
    
    public PowerCascadeAgent(String experimentID, 
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
            HashMap systemParameters,
            HashMap backendParameters,
            Double toleranceParameter,
            Double relRateChangePerEpoch){
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
                systemParameters,
                backendParameters);
        this.toleranceParameter = toleranceParameter;
        this.flowType = (PowerFlowType) backendParameters.get(PowerBackendParameter.FLOW_TYPE);
        this.relRateChangePerEpoch = relRateChangePerEpoch;
    }
    
    @Override
    public void performInitialStateOperations(){
        // inherited from BenchmarkSFINAAgent
        this.initMeasurementVariables();
        this.saveStartTime();
        
        this.setCapacityByToleranceParameter();
        if(getSimulationTime()>1)
            this.performRelativeCapacityChange();
        this.calculateInitialLoad();
    }
    
    @Override
    public void performFinalStateOperations(){
        this.calculateFinalLoad();
        this.calculateCascadeMetrics();
        
        // inherited from BenchmarkSFINAAgent
        this.calculateActivationStatus();
        this.calculateFlow();
        this.calculateUtilization();
        this.calculateTotalLines();
        this.saveSimuTime();
        this.saveIterationNumber();
    }
    
    /**
     * Set Capacity by Tolerance Parameter. If no line ratings are given by data.
     */
    private void setCapacityByToleranceParameter(){
        boolean capacityNotSet = false;
        for (Link link : getFlowNetwork().getLinks()){
            if (link.getCapacity() == 0.0){
                capacityNotSet = true;
            }
            else capacityNotSet = false;
        }
        if(capacityNotSet){
            callBackend(getFlowNetwork());
            for (Link link : getFlowNetwork().getLinks()){
                link.setCapacity(toleranceParameter*link.getFlow());
            }
        }
    }
    
    /**
     * Change capacity of links. Relative to former capacity. 1.0 = no change.
     */
    private void performRelativeCapacityChange(){
        logger.info("reducing link capacities by " + relRateChangePerEpoch);
        for(Link link : getFlowNetwork().getLinks()){
            link.setCapacity(link.getCapacity()*relRateChangePerEpoch);
        }
    }
    
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
                if(flowNetwork.getNodes().size() == 1){
                    logger.info("....not enough nodes");
                    return converged;
                }
                
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
                    if (generators.size() == 0){
                        logger.info("....no generator");
                        return converged; // blackout if no generator in island
                    }
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
                    logger.info("....converged " + converged);
                    if (converged){
                        limViolation = GenerationBalancing(flowNetwork, slack);
                        
                        // Without the following line big cases (like polish) even DC doesn't converge..
                        if(flowType.equals(PowerFlowType.DC))
                            limViolation=false;
                        
                        if (limViolation){
                            converged = false;
                            if(generators.size() > 0){ // make next bus a slack
                                slack = generators.get(0);
                                slack.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.SLACK_BUS);
                                generators.remove(0);
                            }
                            else{
                                logger.info("....no more generators");
                                return false; // all generator limits were hit -> blackout
                            }
                        }
                    }
                    else{
                        converged = loadShedding(flowNetwork);
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
    
    private boolean GenerationBalancing(FlowNetwork flowNetwork, Node slack){
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
            logger.info("....generator limit violated at node " + slack.getIndex());
        else
            logger.info("....no generator limit violated");
        return limViolation;
    }
    
    private boolean loadShedding(FlowNetwork flowNetwork){
        boolean converged = false;
        int loadIter = 0;
        int maxLoadShedIterations = 15; // according to paper
        double loadReductionFactor = 0.05; // 5%, according to paper
        while (!converged && loadIter < maxLoadShedIterations){
            logger.info("....Doing load shedding at iteration " + loadIter);
            for (Node node : flowNetwork.getNodes()){
                node.replacePropertyElement(PowerNodeState.POWER_DEMAND_REAL, (Double)node.getProperty(PowerNodeState.POWER_DEMAND_REAL)*(1.0-loadReductionFactor));
                node.replacePropertyElement(PowerNodeState.POWER_DEMAND_REACTIVE, (Double)node.getProperty(PowerNodeState.POWER_DEMAND_REACTIVE)*(1.0-loadReductionFactor));
            }
            converged = callBackend(flowNetwork);
            loadIter++;
        }
        return converged;
    }
    
    @Override
    public void mitigateOverload(FlowNetwork flowNetwork){
        // Implement mitigation strategy here
    }

    /**
     * @return the toleranceParameter
     */
    public double getToleranceParameter() {
        return toleranceParameter;
    }

    /**
     * @param toleranceParameter the toleranceParameter to set
     */
    public void setToleranceParameter(double toleranceParameter) {
        this.toleranceParameter = toleranceParameter;
    }
    
    
    // *************************** Measurements ********************************

    
    private void calculateInitialLoad(){
        for(Node node : this.getFlowNetwork().getNodes()){
            double initialLoad = 0.0;
            if(node.isActivated() && node.isConnected()){
                initialLoad = (Double)node.getProperty(PowerNodeState.POWER_DEMAND_REAL);
            }
            HashMap<Metrics,Object> metrics=this.getTemporalNodeMetrics().get(this.getSimulationTime()).get(node.getIndex());
            metrics.put(Metrics.NODE_INIT_LOADING, initialLoad);
        }
    }
    
    private void calculateFinalLoad(){
        for(Node node : this.getFlowNetwork().getNodes()){
            double finalLoad = 0.0;
            if(node.isActivated() && node.isConnected()){
                finalLoad = (Double)node.getProperty(PowerNodeState.POWER_DEMAND_REAL);
            }
            HashMap<Metrics,Object> metrics=this.getTemporalNodeMetrics().get(this.getSimulationTime()).get(node.getIndex());
            metrics.put(Metrics.NODE_FINAL_LOADING, finalLoad);
        }
    }
    
    private void calculateCascadeMetrics(){
        ArrayList<FlowNetwork> finalIslands = getFlowNetwork().computeIslands();
        int nrIslands = finalIslands.size();
        int nrIsolatedNodes = 0;
        for(FlowNetwork net : finalIslands)
            if(net.getNodes().size()==1)
                nrIsolatedNodes++;
        
        this.getTemporalSystemMetrics().get(this.getSimulationTime()).put(Metrics.ISLANDS, nrIslands);
        this.getTemporalSystemMetrics().get(this.getSimulationTime()).put(Metrics.ISOLATED_NODES, nrIsolatedNodes);
    }
   
    /**
     * Scheduling the measurements for the simulation agent
     */
    @Override
    public void scheduleMeasurements(){
        setMeasurementDumper(new MeasurementFileDumper(getPeersLogDirectory()+this.getExperimentID()+"peer-"+getPeer().getIndexNumber()));
        getPeer().getMeasurementLogger().addMeasurementLoggerListener(new MeasurementLoggerListener(){
            public void measurementEpochEnded(MeasurementLog log, int epochNumber){
                int simulationTime=getSimulationTime();
                
                if(simulationTime>=1){
                    log.logTagSet(simulationTime, new HashSet(getFlowNetwork().getLinks()), simulationTime);
                    for(Link link:getFlowNetwork().getLinks()){
                        HashMap<Metrics,Object> linkMetrics=getTemporalLinkMetrics().get(simulationTime).get(link.getIndex());
                        log.log(simulationTime, Metrics.LINE_UTILIZATION, ((Double)linkMetrics.get(Metrics.LINE_UTILIZATION)));
                        log.log(simulationTime, Metrics.LINE_FLOW, ((Double)linkMetrics.get(Metrics.LINE_FLOW)));
                        log.log(simulationTime, Metrics.ACTIVATED_LINES, ((Double)linkMetrics.get(Metrics.ACTIVATED_LINES)));
                        log.log(simulationTime, Metrics.TOTAL_LINES, ((Double)linkMetrics.get(Metrics.TOTAL_LINES)));
                    }
                    log.logTagSet(simulationTime, new HashSet(getFlowNetwork().getNodes()), simulationTime);
                    for(Node node:getFlowNetwork().getNodes()){
                        HashMap<Metrics,Object> nodeMetrics=getTemporalNodeMetrics().get(simulationTime).get(node.getIndex());
                        log.log(simulationTime, Metrics.NODE_INIT_LOADING, ((Double)nodeMetrics.get(Metrics.NODE_INIT_LOADING)));
                        log.log(simulationTime, Metrics.NODE_FINAL_LOADING, ((Double)nodeMetrics.get(Metrics.NODE_FINAL_LOADING)));
                    }
                    HashMap<Metrics,Object> sysMetrics=getTemporalSystemMetrics().get(simulationTime);
                    log.log(simulationTime, Metrics.TOT_SIMU_TIME, ((Double)sysMetrics.get(Metrics.TOT_SIMU_TIME)));
                    log.log(simulationTime, Metrics.NEEDED_ITERATIONS, ((Integer)sysMetrics.get(Metrics.NEEDED_ITERATIONS)));
                    log.log(simulationTime, Metrics.ISLANDS, ((Integer)sysMetrics.get(Metrics.ISLANDS)));
                    log.log(simulationTime, Metrics.ISOLATED_NODES, ((Integer)sysMetrics.get(Metrics.ISOLATED_NODES)));
                }
                getMeasurementDumper().measurementEpochEnded(log, simulationTime);
                log.shrink(simulationTime, simulationTime+1);
            }
        });
    }
}

    
