/*
 * Copyright (C) 2016 SFINA Team
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
package diseasespread;

import core.SimulationAgent;
import diseasespread.input.DiseaseSpreadNodeState;
import diseasespread.input.DiseaseSpreadLinkState;
import diseasespread.backend.JavaDiseaseSpreadBackend;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import network.FlowNetwork;
import network.Link;
import network.Node;
import org.apache.log4j.Logger;
import protopeer.util.quantities.Time;
import backend.FlowBackendInterface;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import protopeer.measurement.MeasurementFileDumper;
import protopeer.measurement.MeasurementLog;
import protopeer.measurement.MeasurementLoggerListener;
/**
 *
 * @author dinesh
 */
public class DiseaseSpreadAgent extends SimulationAgent{
    
    //private HashMap<String,ArrayList<Double>> nodeHealthHistory;
    private double timeStep = 0.1;
    private static final Logger logger = Logger.getLogger(DiseaseSpreadAgent.class);
    private int maxIterations = 100;
    
    public DiseaseSpreadAgent(String experimentID,
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
            HashMap backendParameters){
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
    }
    
    @Override
    public void runFlowAnalysis(){
        // set parameters
        HashMap<String,ArrayList<Double>> nodeHealthHistory = new HashMap<String,ArrayList<Double>>();
        for(Node node:getFlowNetwork().getNodes()){
            nodeHealthHistory.put(node.getIndex(), new ArrayList<Double>(Collections.nCopies(maxHistory(),0.0)));
        }
        for(Node node:getFlowNetwork().getNodes()){
            nodeHealthHistory.get(node.getIndex()).add(0, (Double)node.getProperty(DiseaseSpreadNodeState.HEALTH));
        }
        
        // set nodeHealthHistory as backend parameter.
        getBackendParameters().put(DiseaseSpreadBackendParameter.NodeHealthHistory, nodeHealthHistory);
        getBackendParameters().put(DiseaseSpreadBackendParameter.TIME, getIteration());
        
        // TODO log the information rather than print it
        while(getIteration()<maxIterations){
            getBackendParameters().put(DiseaseSpreadBackendParameter.TIME, getIteration());
            callBackend(getFlowNetwork());
            System.out.println("Total Damaged Nodes: "+totalDamagedNodes());
            nextIteration();
        }
    }
    
    public int totalDamagedNodes(){
        int damaged = 0;
        for(Node node:getFlowNetwork().getNodes()){
            if((Double)node.getProperty(DiseaseSpreadNodeState.HEALTH)>(Double)node.getProperty(DiseaseSpreadNodeState.RESISTANCETHRESHOLD)){
                damaged++;
            }
        }
        return damaged;
    }
    
    @Override
    public boolean callBackend(FlowNetwork flowNetwork){
        FlowBackendInterface flowBackend;
        boolean converged = false;
        logger.info("executing " + getBackend() + " backend");
        switch(getDomain()){
             case DISEASESPREAD:
                switch(getBackend()){
//                  case HEALTH_MATLAB:
//                        flowBackend=new MatlabHealthBackend();
//                        converged=flowBackend.flowAnalysis(flowNetwork, getBackendParameters());
//                        break;
                    case DISEASESPREAD_JAVA:
                        flowBackend=new JavaDiseaseSpreadBackend();
                        converged=flowBackend.flowAnalysis(flowNetwork, getBackendParameters());
                        break;
                    default:
                        logger.debug("This flow backend is not supported at this moment.");
                }
                break;
            default:
                logger.debug("Domain and corresponding Application (Agent) do not match.");
        }
        return converged;
    }
    
    private int maxHistory(){
        double maxConnectionDelay = 0;
        for(Link link:getFlowNetwork().getLinks()){
            if((Double)link.getProperty(DiseaseSpreadLinkState.TIME_DELAY)>maxConnectionDelay){
                maxConnectionDelay = (Double)link.getProperty(DiseaseSpreadLinkState.TIME_DELAY);
            }
        }
        return (int)Math.ceil(maxConnectionDelay/timeStep);
    }
    
    
}