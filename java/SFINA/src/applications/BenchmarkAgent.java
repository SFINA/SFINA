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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import network.FlowNetwork;
import network.Link;
import network.Node;
import power.input.PowerNodeState;
import protopeer.measurement.MeasurementFileDumper;
import protopeer.measurement.MeasurementLog;
import protopeer.measurement.MeasurementLoggerListener;
import protopeer.util.quantities.Time;

/**
 *
 * @author evangelospournaras
 */
public class BenchmarkAgent extends SFINAAgent{
    
    public BenchmarkAgent(String experimentID, 
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
    
    private void calculateTotalLines(){
        for(Link link:this.getFlowNetwork().getLinks()){
            HashMap<Metrics,Object> metrics=this.getTemporalLinkMetrics().get(this.getSimulationTime()).get(link.getIndex());
            metrics.put(Metrics.TOTAL_LINES, 1.0);
        }
    }
    
    private void calculateActivationStatus(){
        for(Link link:this.getFlowNetwork().getLinks()){
            boolean activationStatus=link.isActivated();
            HashMap<Metrics,Object> metrics=this.getTemporalLinkMetrics().get(this.getSimulationTime()).get(link.getIndex());
            metrics.put(Metrics.ACTIVATED_LINES, (activationStatus==true) ? 1.0 : 0.0);
        }
    }
    
    private void calculateFlow(){
        for(Link link:this.getFlowNetwork().getLinks()){
            double flow=link.getFlow();
            HashMap<Metrics,Object> metrics=this.getTemporalLinkMetrics().get(this.getSimulationTime()).get(link.getIndex());
            metrics.put(Metrics.LINE_FLOW, flow);
        }
    }
    
    private void calculateUtilization(){
        for(Link link:this.getFlowNetwork().getLinks()){
            double flow=link.getFlow();
            double capacity=link.getCapacity();
            double utilization=flow/capacity;
            HashMap<Metrics,Object> metrics=this.getTemporalLinkMetrics().get(this.getSimulationTime()).get(link.getIndex());
            metrics.put(Metrics.LINE_UTILIZATION, utilization);
        }
    }
    
    private void calculateInitialLoad(){
        for(Node node : this.getFlowNetwork().getNodes()){
            double initialLoadPerEpoch = (Double)this.getInitialLoadPerEpoch(this.getSimulationTime()).get(node.getIndex());
            HashMap<Metrics,Object> metrics=this.getTemporalNodeMetrics().get(this.getSimulationTime()).get(node.getIndex());
            metrics.put(Metrics.NODE_INIT_LOADING, initialLoadPerEpoch);
        }
    }
    
    private void calculateFinalLoad(){
        for(Node node : this.getFlowNetwork().getNodes()){
            if(node.isActivated()){
                double load = (Double)node.getProperty(PowerNodeState.POWER_DEMAND_REAL);
                HashMap<Metrics,Object> metrics=this.getTemporalNodeMetrics().get(this.getSimulationTime()).get(node.getIndex());
                metrics.put(Metrics.NODE_FINAL_LOADING, load);
            }
            else{
                HashMap<Metrics,Object> metrics=this.getTemporalNodeMetrics().get(this.getSimulationTime()).get(node.getIndex());
                metrics.put(Metrics.NODE_FINAL_LOADING, 0.0);
            }
                
        }
    }
    
//    private void calculateFinalLoad1(){
//        for(FlowNetwork net : this.getFinalIslands().keySet()){
//            if(this.getFinalIslands().get(net)){
//                for(Node node : net.getNodes()){
//                    double load = (Double)node.getProperty(PowerNodeState.POWER_DEMAND_REAL);
//                    HashMap<Metrics,Object> metrics=this.getTemporalNodeMetrics().get(this.getSimulationTime()).get(node.getIndex());
//                    metrics.put(Metrics.NODE_FINAL_LOADING, load);
//                }
//            }
//            else{
//                for(Node node : net.getNodes()){
//                    double load = 0.0;
//                    HashMap<Metrics,Object> metrics=this.getTemporalNodeMetrics().get(this.getSimulationTime()).get(node.getIndex());
//                    metrics.put(Metrics.NODE_FINAL_LOADING, load);
//                }
//            }
//        }
//    }
    
    @Override
    public void performMeasurements(){
        this.calculateActivationStatus();
        this.calculateFlow();
        this.calculateUtilization();
        this.calculateTotalLines();
        this.calculateInitialLoad();
        this.calculateFinalLoad();
    }
    
    //****************** MEASUREMENTS ******************
        
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
                        log.log(simulationTime, Metrics.LINE_UTILIZATION, ((Double)linkMetrics.get(Metrics.LINE_UTILIZATION)).doubleValue());
                        log.log(simulationTime, Metrics.LINE_FLOW, ((Double)linkMetrics.get(Metrics.LINE_FLOW)).doubleValue());
                        log.log(simulationTime, Metrics.ACTIVATED_LINES, ((Double)linkMetrics.get(Metrics.ACTIVATED_LINES)).doubleValue());
                        log.log(simulationTime, Metrics.TOTAL_LINES, ((Double)linkMetrics.get(Metrics.TOTAL_LINES)).doubleValue());
                    }
                    log.logTagSet(simulationTime, new HashSet(getFlowNetwork().getNodes()), simulationTime);
                    for(Node node:getFlowNetwork().getNodes()){
                        HashMap<Metrics,Object> nodeMetrics=getTemporalNodeMetrics().get(simulationTime).get(node.getIndex());
                        log.log(simulationTime, Metrics.NODE_INIT_LOADING, ((Double)nodeMetrics.get(Metrics.NODE_INIT_LOADING)).doubleValue());
                        log.log(simulationTime, Metrics.NODE_FINAL_LOADING, ((Double)nodeMetrics.get(Metrics.NODE_FINAL_LOADING)).doubleValue());
                    }
                    // this logTagSet uses as tagSet the iterations per epoch as collection of Integers
//                    log.logTagSet(simulationTime, new HashSet((Collection)getFlowSimuTime().get(simulationTime).keySet()), simulationTime);
                    for(int iteration : getFlowSimuTime().get(simulationTime).keySet()){
                        log.log(simulationTime, Metrics.SYSTEM_FLOW_SIMU_TIME, ((Long)getFlowSimuTime().get(simulationTime).get(iteration)).doubleValue());
                        
                    }
                    log.log(simulationTime, Metrics.SYSTEM_TOT_SIMU_TIME, ((Long)getTotalSimuTime().get(simulationTime)).doubleValue());
                    log.log(simulationTime, Metrics.NEEDED_ITERATIONS, ((Integer)getFlowSimuTime().get(simulationTime).size()));
                }
                getMeasurementDumper().measurementEpochEnded(log, simulationTime);
                log.shrink(simulationTime, simulationTime+1);
            }
        });
    }
    
}
