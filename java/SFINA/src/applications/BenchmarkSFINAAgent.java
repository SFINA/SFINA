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
import protopeer.measurement.MeasurementFileDumper;
import protopeer.measurement.MeasurementLog;
import protopeer.measurement.MeasurementLoggerListener;
import protopeer.util.quantities.Time;

/**
 * General domain independent measurements.
 * @author evangelospournaras
 */
public class BenchmarkSFINAAgent extends SFINAAgent{
    
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
    
    private long simulationStartTime;

    private void saveStartTime(){
        this.simulationStartTime = System.currentTimeMillis();
    }
    
    private void saveSimuTime(){
        double totSimuTime = System.currentTimeMillis() - simulationStartTime;
        this.getTemporalSystemMetrics().get(this.getSimulationTime()).put(Metrics.TOT_SIMU_TIME, totSimuTime);
    }
    
    @Override
    public void performInitialMeasurements(){
        this.saveStartTime();
    }
    
    @Override
    public void performFinalMeasurements(){
        this.calculateActivationStatus();
        this.calculateFlow();
        this.calculateUtilization();
        this.calculateTotalLines();
        this.saveSimuTime();
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
                }
                getMeasurementDumper().measurementEpochEnded(log, simulationTime);
                log.shrink(simulationTime, simulationTime+1);
            }
        });
    }
}
