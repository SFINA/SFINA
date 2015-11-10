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
            HashMap simulationParameters){
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
                simulationParameters);
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
    
    
    @Override
    public void performMeasurements(){
        this.calculateActivationStatus();
        this.calculateFlow();
        this.calculateUtilization();
        this.calculateTotalLines();
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
                    System.out.println("------------- epoch time " + epochNumber);
                    System.out.println("------------- sime time " + simulationTime);
                    log.logTagSet(epochNumber, new HashSet(getFlowNetwork().getLinks()), epochNumber);
                    for(Link link:getFlowNetwork().getLinks()){
                        HashMap<Metrics,Object> linkMetrics=getTemporalLinkMetrics().get(simulationTime).get(link.getIndex());
                        log.log(epochNumber, Metrics.LINE_UTILIZATION, ((Double)linkMetrics.get(Metrics.LINE_UTILIZATION)).doubleValue());
                        log.log(epochNumber, Metrics.LINE_FLOW, ((Double)linkMetrics.get(Metrics.LINE_FLOW)).doubleValue());
                        log.log(epochNumber, Metrics.ACTIVATED_LINES, ((Double)linkMetrics.get(Metrics.ACTIVATED_LINES)).doubleValue());
                        log.log(epochNumber, Metrics.TOTAL_LINES, ((Double)linkMetrics.get(Metrics.TOTAL_LINES)).doubleValue());
                    }
                }
                getMeasurementDumper().measurementEpochEnded(log, epochNumber);
                log.shrink(epochNumber, epochNumber+1);
            }
        });
    }
    
}
