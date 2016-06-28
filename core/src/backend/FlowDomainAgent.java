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

import backend.FlowBackendInterface;
import java.util.HashMap;
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
public class FlowDomainAgent extends BasePeerlet{
    
    private String experimentID;
    private Time bootstrapTime;
    private Time runTime;
    
    private final static String parameterColumnSeparator="=";
    private final static String fileSystemSchema="conf/fileSystem.conf";
    private final static String peersLogDirectory="peerlets-log/";
    
    private MeasurementFileDumper measurementDumper;
    
    private HashMap<Enum,Object> backendParameters;
    
    public FlowDomainAgent(String experimentID,
            Time bootstrapTime, 
            Time runTime,
            HashMap<Enum,Object> backendParameters){
        
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
    
}
