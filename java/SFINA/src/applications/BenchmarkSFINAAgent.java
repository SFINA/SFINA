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
import java.util.HashMap;
import java.util.HashSet;
import network.Link;
import network.Node;
import protopeer.measurement.MeasurementFileDumper;
import protopeer.measurement.MeasurementLog;
import protopeer.measurement.MeasurementLoggerListener;
import protopeer.util.quantities.Time;

/**
 * General domain independent measurements.
 * @author evangelospournaras
 */
public class BenchmarkSFINAAgent extends SFINAAgent{
    
    private HashMap<Integer,HashMap<String,HashMap<Metrics,Object>>> temporalLinkMetrics;
    private HashMap<Integer,HashMap<String,HashMap<Metrics,Object>>> temporalNodeMetrics;
    private HashMap<Integer,HashMap<Metrics,Object>> temporalSystemMetrics;
    private long simulationStartTime;
    
    public BenchmarkSFINAAgent(String experimentID, 
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
            String sfinaParamLocation,
            String backendParamLocation,
            String columnSeparator, 
            String missingValue){
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
                sfinaParamLocation,
                backendParamLocation,
                columnSeparator,
                missingValue);
        this.temporalLinkMetrics=new HashMap();
        this.temporalNodeMetrics=new HashMap();
        this.temporalSystemMetrics=new HashMap();
    }
    
    public void initMeasurementVariables(){
        HashMap<String,HashMap<Metrics,Object>> linkMetrics=new HashMap<>();
        for(Link link:this.getFlowNetwork().getLinks()){
            HashMap<Metrics,Object> metrics=new HashMap<>();
            linkMetrics.put(link.getIndex(), metrics);
        }
        this.getTemporalLinkMetrics().put(this.getSimulationTime(), linkMetrics);
        
        HashMap<String,HashMap<Metrics,Object>> nodeMetrics=new HashMap<>();
        for(Node node:this.getFlowNetwork().getNodes()){
            HashMap<Metrics,Object> metrics=new HashMap<>();
            nodeMetrics.put(node.getIndex(), metrics);
        }
        this.getTemporalNodeMetrics().put(this.getSimulationTime(), nodeMetrics);
        
        this.getTemporalSystemMetrics().put(this.getSimulationTime(), new HashMap<>());
    }
    
    public void calculateTotalLines(){
        for(Link link:this.getFlowNetwork().getLinks()){
            HashMap<Metrics,Object> metrics=this.getTemporalLinkMetrics().get(this.getSimulationTime()).get(link.getIndex());
            metrics.put(Metrics.TOTAL_LINES, 1.0);
        }
    }
    
    public void calculateActivationStatus(){
        for(Link link:this.getFlowNetwork().getLinks()){
            boolean activationStatus=link.isActivated();
            HashMap<Metrics,Object> metrics=this.getTemporalLinkMetrics().get(this.getSimulationTime()).get(link.getIndex());
            metrics.put(Metrics.ACTIVATED_LINES, (activationStatus==true) ? 1.0 : 0.0);
        }
    }
    
    public void calculateFlow(){
        for(Link link:this.getFlowNetwork().getLinks()){
            double flow=link.getFlow();
            HashMap<Metrics,Object> metrics=this.getTemporalLinkMetrics().get(this.getSimulationTime()).get(link.getIndex());
            metrics.put(Metrics.LINE_FLOW, (link.isActivated()) ? flow : 0.0);
        }
    }
    
    public void calculateUtilization(){
        for(Link link:this.getFlowNetwork().getLinks()){
            double flow=link.getFlow();
            double capacity=link.getCapacity();
            double utilization=flow/capacity;
            HashMap<Metrics,Object> metrics=this.getTemporalLinkMetrics().get(this.getSimulationTime()).get(link.getIndex());
            metrics.put(Metrics.LINE_UTILIZATION, (link.isActivated()) ? utilization : 1.0);
        }
    }
    
    public void saveStartTime(){
        this.simulationStartTime = System.currentTimeMillis();
    }
    
    public void saveSimuTime(){
        double totSimuTime = System.currentTimeMillis() - simulationStartTime;
        this.getTemporalSystemMetrics().get(this.getSimulationTime()).put(Metrics.TOT_SIMU_TIME, totSimuTime);
    }
    
    public void saveIterationNumber(){
        int iter = getIteration()-1;
        this.getTemporalSystemMetrics().get(this.getSimulationTime()).put(Metrics.NEEDED_ITERATIONS, iter);
    }
    
    @Override
    public void performInitialStateOperations(){
        this.initMeasurementVariables();
        this.saveStartTime();
    }
    
    @Override
    public void performFinalStateOperations(){
        this.calculateActivationStatus();
        this.calculateFlow();
        this.calculateUtilization();
        this.calculateTotalLines();
        this.saveSimuTime();
        this.saveIterationNumber();
    }
        
    /**
     * @return the temporalLinkMetrics
     */
    public HashMap<Integer,HashMap<String,HashMap<Metrics,Object>>> getTemporalLinkMetrics() {
        return temporalLinkMetrics;
    }

    /**
     * @return the temporalNodeMetrics
     */
    public HashMap<Integer,HashMap<String,HashMap<Metrics,Object>>> getTemporalNodeMetrics() {
        return temporalNodeMetrics;
    }
    
    /**
     * 
     * @return the temporalSystemMetrics
     */
    public HashMap<Integer,HashMap<Metrics,Object>> getTemporalSystemMetrics() {
        return temporalSystemMetrics;
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
                        log.log(simulationTime, Metrics.LINE_UTILIZATION, ((Double)linkMetrics.get(Metrics.LINE_UTILIZATION)));
                        log.log(simulationTime, Metrics.LINE_FLOW, ((Double)linkMetrics.get(Metrics.LINE_FLOW)));
                        log.log(simulationTime, Metrics.ACTIVATED_LINES, ((Double)linkMetrics.get(Metrics.ACTIVATED_LINES)));
                        log.log(simulationTime, Metrics.TOTAL_LINES, ((Double)linkMetrics.get(Metrics.TOTAL_LINES)));
                    }
                    HashMap<Metrics,Object> sysMetrics=getTemporalSystemMetrics().get(simulationTime);
                    log.log(simulationTime, Metrics.TOT_SIMU_TIME, ((Double)sysMetrics.get(Metrics.TOT_SIMU_TIME)));
                    log.log(simulationTime, Metrics.NEEDED_ITERATIONS, ((Integer)sysMetrics.get(Metrics.NEEDED_ITERATIONS)));
                }
                getMeasurementDumper().measurementEpochEnded(log, simulationTime);
                log.shrink(simulationTime, simulationTime+1);
            }
        });
    }
}
