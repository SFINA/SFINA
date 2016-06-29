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
package backend;

import event.FlowNetworkDataTypesInterface;
import input.Domain;
import java.util.HashMap;
import network.FlowNetwork;
import protopeer.BasePeerlet;
import protopeer.Peer;
import protopeer.measurement.MeasurementFileDumper;
import protopeer.measurement.MeasurementLog;
import protopeer.measurement.MeasurementLoggerListener;
import static protopeer.time.EventScheduler.logger;
import protopeer.time.Timer;
import protopeer.time.TimerListener;
import protopeer.util.quantities.Time;

/**
 *
 * @author evangelospournaras
 */
public abstract class FlowDomainAgent extends BasePeerlet{

    private String experimentID;
    private Time bootstrapTime;
    private Time runTime;
    
    public final String parameterColumnSeparator="=";
    public final String fileSystemSchema="conf/fileSystem.conf";
    public final String peersLogDirectory="peerlets-log/";
    
    private FlowNetworkDataTypesInterface flowNetworkDataTypes;
    
    private MeasurementFileDumper measurementDumper;
    
    private Domain domain;
    private HashMap<Enum,Object> backendParameters;
    
    public FlowDomainAgent(String experimentID,
            Time bootstrapTime, 
            Time runTime){
        
    }
    
    /**
    * Inititializes the flow domain agent
    *
    * @param peer the local peer
    */
    @Override
    public void init(Peer peer){
        super.init(peer);
    }

    /**
    * Starts the flow domain agent by scheduling the epoch measurements and 
    * bootstrapping the agent
    */
    @Override
    public void start(){
        scheduleMeasurements();
        this.runBootstraping();
    }

    /**
    * Stops the simulation agent
    */
    @Override
    public void stop(){
        
    }
    
    /**
     * The scheduling of system bootstrapping.
     */
    public void runBootstraping(){
        Timer loadAgentTimer= getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                logger.info("### "+experimentID+" ###");
                runActiveState();
            }
        });
        loadAgentTimer.schedule(this.bootstrapTime);
    }
    
    
    /**
     * Run active state. 
     */
    public void runActiveState(){
        Timer loadAgentTimer=getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                
                runActiveState(); 
            }
        });
        loadAgentTimer.schedule(this.runTime);
    }
    
    public abstract boolean flowAnalysis(FlowNetwork net);
    
    public abstract boolean flowAnalysis(FlowNetwork net, HashMap<Enum,Object> backendParameters);
    
    public abstract void setBackendParameters(HashMap<Enum,Object> backendParameters);
    
    public abstract void loadFlowNetwork(FlowNetwork flowNetwork, String columnSeparator, String missingValue, String nodeFlowData, String linkFlowData);
    
    public abstract void setFlowParameters(FlowNetwork flowNetwork);
    
    public abstract void saveFlowNetwork(FlowNetwork flowNetwork, String columnSeparator, String missingValue, String nodeFlowData, String linkFlowData);
    
    public abstract void loadDomainParameters(String sfinaParamLocation, String backendParamLocation, String eventsLocation);
    
    /**
     * Scheduling the measurements for the simulation agent
     */
    public void scheduleMeasurements(){
        this.setMeasurementDumper(new MeasurementFileDumper(getPeersLogDirectory()+this.getExperimentID()+"/peer-"+getPeer().getIndexNumber()));
        getPeer().getMeasurementLogger().addMeasurementLoggerListener(new MeasurementLoggerListener(){
            public void measurementEpochEnded(MeasurementLog log, int epochNumber){
                
                getMeasurementDumper().measurementEpochEnded(log, epochNumber);
                log.shrink(epochNumber, epochNumber+1);
            }
        });
    }
    
    /**
     * @return the experimentID
     */
    public String getExperimentID() {
        return experimentID;
    }

    /**
     * @return the peersLogDirectory
     */
    public String getPeersLogDirectory() {
        return peersLogDirectory;
    }
    
    /**
     * @return the measurementDumper
     */
    public MeasurementFileDumper getMeasurementDumper() {
        return measurementDumper;
    }
    
    /**
     * @param measurementDumper the measurementDumper to set
     */
    public void setMeasurementDumper(MeasurementFileDumper measurementDumper) {
        this.measurementDumper = measurementDumper;
    }

    /**
     * @return the domain
     */
    public Domain getDomain() {
        return domain;
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    /**
     * @return the backendParameters
     */
    public HashMap<Enum,Object> getBackendParameters() {
        return backendParameters;
    }

    /**
     * @return the flowNetworkDataTypes
     */
    public FlowNetworkDataTypesInterface getFlowNetworkDataTypes() {
        return flowNetworkDataTypes;
    }

    /**
     * @param flowNetworkDataTypes the flowNetworkDataTypes to set
     */
    public void setFlowNetworkDataTypes(FlowNetworkDataTypesInterface flowNetworkDataTypes) {
        this.flowNetworkDataTypes = flowNetworkDataTypes;
    }
    
}
