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


import dsutil.protopeer.FingerDescriptor;

import org.apache.log4j.Logger;
import protopeer.BasePeerlet;
import protopeer.Peer;
import protopeer.measurement.MeasurementLog;
import protopeer.measurement.MeasurementLoggerListener;
import java.util.List;
import java.util.ArrayList;


/**
 *
 * @author mcb
 */
public class SimulationAgent_Debug extends BasePeerlet implements SimulationAgentInterface_Debug,TimeSteppingInterface_Debug.CommandReceiver{
    
    private static final Logger logger = Logger.getLogger(SimulationAgent_Debug.class);
    
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
       
        
       // Dummy functionality: number of iteration the simulation Agent has to perform
       // per Time step, before it is converged (we are not using events etc. in the 
       // dummy set up
       if(iterationToProgress.length == 0){
           this.iterationToProgress.add(2);
       }else{
            //save at whicht iteration SimulationAgent should be converged
            for(int i=0;i< iterationToProgress.length;i++){
                this.iterationToProgress.add(iterationToProgress[i]);
            }
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
     *               BOOTSTRAPPING
     **************************************************/
    
    /**
     * The scheduling of system bootstrapping. 
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
     */
     @Override
    public void runActiveState(){
      // dummy, just return immediately
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
