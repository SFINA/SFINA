/*
 * Copyright (C) 2017 SFINA Team
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
package Debug;

import backend.FlowDomainAgent;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import core.Archive.SimulationAgent_old;
import core.TimeSteppingAgentInterface;
import dsutil.protopeer.FingerDescriptor;
import event.Event;
import event.EventType;
import event.NetworkComponent;
import input.EventLoader;
import input.FlowLoader;
import input.SfinaParameter;
import input.SfinaParameterLoader;
import input.TopologyLoader;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import network.FlowNetwork;
import network.InterdependentLink;
import network.Link;
import network.LinkState;
import network.Node;
import network.NodeState;
import org.apache.log4j.Logger;
import output.EventWriter;
import output.FlowWriter;
import output.TopologyWriter;
import protopeer.BasePeerlet;
import protopeer.Peer;
import protopeer.measurement.MeasurementFileDumper;
import protopeer.measurement.MeasurementLog;
import protopeer.measurement.MeasurementLoggerListener;
import protopeer.network.Message;
import protopeer.util.quantities.Time;

/**
 *
 * @author mcb
 */
public class SimulationAgent_Debug extends BasePeerlet implements SimulationAgentInterface_Debug,TimeSteppingInterface_Debug.CommandReceiver{
    
    private static final Logger logger = Logger.getLogger(SimulationAgent_old.class);
    
    private int networkIndex;
    List<Integer> iterationToProgress = new ArrayList<>();

    private String experimentID;
 //   private Time bootstrapTime;
   // private Time runTime;
    private int iteration;

    private String timeToken;
    private String timeTokenName="time_";
    
    
    private FingerDescriptor myAgentDescriptor;
   
    
    public SimulationAgent_Debug(
            String experimentID,int... iterationToProgress){
        this.experimentID=experimentID;
       
       
        //save at whicht iteration SimulationAgent should be converged
        for(int i=0;i< iterationToProgress.length;i++){
            this.iterationToProgress.add(iterationToProgress[i]);
        }
        
        
        
    }
    
    /***************************************************
     *               BASE PEERLET FUNCTIONS
     * *************************************************/
    
    /**
    * Inititializes the simulation agent by creating the finger descriptor.
    *
    * @param peer the local peer
    */
    @Override
    public void init(Peer peer){
        super.init(peer);
        this.myAgentDescriptor=new FingerDescriptor(getPeer().getFinger());
        this.setNetworkIndex(this.getPeer().getIndexNumber());
    }

    
    
    /**************************************************
     *               BOOTSTRAPPINGgetFlowNetwork().getNetworkIndex()
     **************************************************/
    
    /**
     * The scheduling of system bootstrapping. It loads system parameters, 
     * network and event data. At the end, it triggers the active state. 
     * 
     * Simulation is initialized as follows:
     * 
     * 1. Loading the file system parameters
     * 2. Loading SFINA and backend configuration files and static event files
     * 3. Creating a topology loader
     * 5. Clearing up the output files 
     */
    @Override
    public void runBootstraping(){
       
        logger.info("### Bootstraping "+experimentID+" ###");
        
        scheduleMeasurements();

        logger.debug("### End of bootstraping, calling agentFinishedBootstrap. ###");
        getTimeSteppingAgent().agentFinishedBootStrap();               
        
    }

    /**
     * Scheduling the measurements for the simulation agent
     */
    @Override
    public void scheduleMeasurements(){
      
        getPeer().getMeasurementLogger().addMeasurementLoggerListener(new MeasurementLoggerListener(){
            public void measurementEpochEnded(MeasurementLog log, int epochNumber){
                logger.debug("---> Measuring network " + getPeer().getIndexNumber());
                log.shrink(epochNumber, epochNumber+1);
            }
        });
    }
   
    
    /***************************************************
    *               RUN ACTIVE STATE
    **************************************************/
    
    /**
     * The scheduling of the active state.  It is executed periodically. 
 
    This is the fundamental prototype of the simulation runtime. It should
    stay generic. At this moment, the runtime concerns the following:

    1. Counting simulation time.
    2. Checking and loading new data from files.
    3. Triggering event execution at the current time step
    4. Calling three methods, which can be used to implement the actual simulation:
        - initialOperations()
        - runFlowAnalysis()
        - finalOperations()
     */
     @Override
    public void runActiveState(){
       // executeAllEvents();

      //  runInitialOperations();

      //  runFlowAnalysis();

    //    runFinalOperations();

     //   saveOutputData();

        getTimeSteppingAgent().agentFinishedActiveState();
            
    }
    
    @Override
    public void progressToNextTimeStep() {
                initTimeStep();
                logIteration();
                runActiveState(); 
    }

    @Override
    public void progressToNextIteration() { 
        this.logIteration();
        this.runActiveState(); 
    }
    
    @Override
    public void skipNextIteration() {
        this.logIteration();
        logger.info("Skipping this iteration");
        getTimeSteppingAgent().agentFinishedActiveState();
    }

    @Override
    public boolean isConverged() {
        if(getIteration() >= this.iterationToProgress.get(this.getNetworkIndex()))
            return true;
        else 
            return false;
            
    }
    
   
    
     /**
     * Initializes the active state by setting iteration = 1 and loading data.
     */
    private void initTimeStep(){
        this.timeToken = this.timeTokenName + this.getSimulationTime();
        logger.info("\n---------------------------------------------------\n--------------> " + this.timeToken + " at network " + this.getNetworkIndex()+ " <--------------");
        resetIteration();        
       
    }
    
    /**
     * Sets iteration to 0.
     */
    private void resetIteration(){
        this.iteration=0;
    }
    
    private void logIteration(){
        this.iteration++;
        logger.info("\n-------> Iteration " + this.getIteration() + " at network " + this.getNetworkIndex() + " (" + this.timeToken + ") <-------");
    }
      


    /***************************************
     *          GETTER AND SETTER  
     *****************************************/
    
    @Override
    public int getNetworkIndex() {
        return this.networkIndex; 
    }

  
    public TimeSteppingInterface_Debug getTimeSteppingAgent(){
        return (TimeSteppingInterface_Debug) getPeer().getPeerletOfType(TimeSteppingInterface_Debug.class);
    }
    
    @Override
    public int getSimulationTime(){
//        // just for testing purposes, does this change something
//        return ((int) (Time.inSeconds(this.getPeer().getClock().getTime())-Time.inSeconds(Time.inMilliseconds(2000))));
        return getTimeSteppingAgent().getSimulationTime();
    }
    
   
  

    
    
     
    /**
     * 
     * @return the current iteration
     */
    @Override
    public int getIteration(){
        return this.iteration;
    }
     
    /**
     * 
     * @param iteration
     */
    public void setIteration(int iteration){
        this.iteration=iteration;
    }
       
    /**
     * @param networkIndex the networkIndex to set
     */
    private void setNetworkIndex(int networkIndex) {
        this.networkIndex = networkIndex;
    }
    
   
    
     /**
     * @return the experimentID
     */
    public String getExperimentID() {
        return experimentID;
    }

    
    
    
  
    
}
