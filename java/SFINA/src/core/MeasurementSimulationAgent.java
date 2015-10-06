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
package core;

import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import network.Link;
import protopeer.measurement.MeasurementFileDumper;
import protopeer.measurement.MeasurementLog;
import protopeer.measurement.MeasurementLoggerListener;
import protopeer.util.quantities.Time;

/**
 *
 * @author evangelospournaras
 */
public class MeasurementSimulationAgent extends SimulationAgent{
    
    private HashMap<Integer,HashMap<String,HashMap<Metrics,Object>>> temporalLinkMetrics;
    
    public MeasurementSimulationAgent(String experimentID, 
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
        this.temporalLinkMetrics=new HashMap<Integer,HashMap<String,HashMap<Metrics,Object>>>();
    }
    @Override
    public void initMeasurements(){
        HashMap<String,HashMap<Metrics,Object>> linkMetrics=new HashMap<String,HashMap<Metrics,Object>>();
        for(Link link:this.getFlowNetwork().getLinks()){
            HashMap<Metrics,Object> metrics=new HashMap<Metrics,Object>();
            linkMetrics.put(link.getIndex(), metrics);
        }
        this.temporalLinkMetrics.put(this.getSimulationTime(), linkMetrics);
    }
    
    private void calculateUtilization(){
        for(Link link:this.getFlowNetwork().getLinks()){
            double flow=link.getFlow();
            double capacity=link.getCapacity();
            double utilization=flow/capacity;
            HashMap<Metrics,Object> metrics=temporalLinkMetrics.get(this.getSimulationTime()).get(link.getIndex());
            metrics.put(Metrics.UTILIZATION, utilization);
        }
    }
    
    @Override
    public void performMeasurements(){
        this.calculateUtilization();
    }
    
    //****************** MEASUREMENTS ******************
        
    /**
     * Scheduling the measurements for the simulation agent
     */
    @Override
    public void scheduleMeasurements(){
        this.measurementDumper=new MeasurementFileDumper(peersLogDirectory+this.experimentID+getPeer().getIdentifier().toString());
        getPeer().getMeasurementLogger().addMeasurementLoggerListener(new MeasurementLoggerListener(){
            public void measurementEpochEnded(MeasurementLog log, int epochNumber){
                log.log(epochNumber, Metrics.UTILIZATION, 0.0);
                measurementDumper.measurementEpochEnded(log, epochNumber);
                log.shrink(epochNumber, epochNumber+1);
            }
        });
    }
    
}
