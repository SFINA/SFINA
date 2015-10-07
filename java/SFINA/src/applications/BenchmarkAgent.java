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
            String inputParametersLocation, 
            String nodesLocation, 
            String linksLocation, 
            String nodesFlowLocation, 
            String linksFlowLocation, 
            String eventsLocation, 
            String parameterValueSeparator, 
            String columnSeparator, 
            String missingValue){
        super(experimentID,
                peersLogDirectory,
                bootstrapTime,
                runTime,
                timeTokenName,
                experimentConfigurationFilesLocation,
                inputParametersLocation,
                nodesLocation,
                linksLocation,
                nodesFlowLocation,
                linksFlowLocation,
                eventsLocation,
                parameterValueSeparator,
                columnSeparator,
                missingValue);
    }
    
    private void calculateActivationStatus(){
        for(Link link:this.getFlowNetwork().getLinks()){
            boolean activationStatus=link.isActivated();
            HashMap<Metrics,Object> metrics=this.getTemporalLinkMetrics().get(this.getSimulationTime()).get(link.getIndex());
            metrics.put(Metrics.ACTIVATION_STATUS, (activationStatus==true) ? 1.0 : 0.0);
        }
    }
    
    private void calculateFlow(){
        for(Link link:this.getFlowNetwork().getLinks()){
            double flow=link.getFlow();
            HashMap<Metrics,Object> metrics=this.getTemporalLinkMetrics().get(this.getSimulationTime()).get(link.getIndex());
            metrics.put(Metrics.FLOW, flow);
        }
    }
    
    private void calculateUtilization(){
        for(Link link:this.getFlowNetwork().getLinks()){
            double flow=link.getFlow();
            double capacity=link.getCapacity();
            double utilization=flow/capacity;
            HashMap<Metrics,Object> metrics=this.getTemporalLinkMetrics().get(this.getSimulationTime()).get(link.getIndex());
            metrics.put(Metrics.UTILIZATION, utilization);
        }
    }
    
    
    @Override
    public void performMeasurements(){
        this.calculateActivationStatus();
        this.calculateFlow();
        this.calculateUtilization();
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
//                System.out.println("Simulation time:"+getSimulationTime());
                int simulationTime=getSimulationTime();
                if(simulationTime>=1){
                    for(Link link:getFlowNetwork().getLinks()){
                        HashMap<Metrics,Object> linkMetrics=getTemporalLinkMetrics().get(simulationTime).get(link.getIndex());
                        log.log(simulationTime, link.getIndex(), Metrics.UTILIZATION, ((Double)linkMetrics.get(Metrics.UTILIZATION)).doubleValue());
                        log.log(simulationTime, link.getIndex(), Metrics.FLOW, ((Double)linkMetrics.get(Metrics.FLOW)).doubleValue());
                        log.log(simulationTime, link.getIndex(), Metrics.ACTIVATION_STATUS, ((Double)linkMetrics.get(Metrics.ACTIVATION_STATUS)).doubleValue());
                    }
                }
                
                getMeasurementDumper().measurementEpochEnded(log, simulationTime);
                log.shrink(simulationTime, simulationTime+1);
            }
        });
    }
    
}
