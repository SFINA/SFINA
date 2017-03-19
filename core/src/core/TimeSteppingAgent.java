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
package core;

import protopeer.BasePeerlet;
import protopeer.time.Timer;
import protopeer.time.TimerListener;
import protopeer.util.quantities.Time;

/**
 * Base Class responsible for SFINA Time and Iteration Management.
 * @author mcb
 */
public class TimeSteppingAgent extends BasePeerlet implements TimeSteppingAgentInterface {
    
    private Time bootstrapTime;
    private Time runTime;

    /**
     * Constructor
     * @param bootstrapTime
     * @param runTime 
     */
    public TimeSteppingAgent(Time bootstrapTime, Time runTime) {
        this.bootstrapTime = bootstrapTime;
        this.runTime = runTime;
    }
    
    /***********************************************************************
     *      BASEPEERLET FUNCTIONS
     ***********************************************************************/
    
    @Override
    public void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
        Timer loadAgentTimer= getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                       getSimulationAgent().runBootstraping();
                           }
        });
        loadAgentTimer.schedule(this.bootstrapTime);
    }
   
    /************************************************************
     *      TimeSteppingAgentInterface Functions
     *************************************************************/
    
     @Override
    public void agentFinishedActiveState() {
        if(!getSimulationAgent().isConverged())
            progressCommandReceiverToNextIteration();

    }
    
    
    @Override
    public final void agentFinishedBootStrap() {
        afterBootstrapFinished(); // sub clases can do some logic
        progressCommandReceiverToNextTimeStep(); // progress to Time 1
    }

    @Override
    public int getSimulationTime() {
       return ((int)(Time.inSeconds(this.getPeer().getClock().getTime())-Time.inSeconds(this.bootstrapTime)));
    }
    
    /**
     * Get the SimulationAgent.
     * @return 
     */
    public SimulationAgentInterface getSimulationAgent() {
        return (SimulationAgentInterface) getPeer().getPeerletOfType(SimulationAgentInterface.class);
    }
        
    /*************************************************************
     *      TimeSteppingAgent internal functions
     *************************************************************/
    
    /**
     * Recursive Function, which handels the Protopeer Time Stepping and hence
     * the SFINA Simulation Time. This function is a crucial part of SFINA. Only
     * modify if you are sure, what you are doing.
     */
    private void progressCommandReceiverToNextTimeStep(){
        Timer loadAgentTimer=getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                getSimulationAgent().progressToNextTimeStep();
                
                progressCommandReceiverToNextTimeStep();
            }
        });
        loadAgentTimer.schedule(this.runTime);         
        
    }
    
    /**
     * Perform another iteration of Simulation.
     */
    protected final void progressCommandReceiverToNextIteration(){
        getSimulationAgent().progressToNextIteration(); 
    }
    /**
     * Skip next iteration of Simulation.
     */
    protected final void progressCommandReceiverToSkipNextIteration(){
        getSimulationAgent().skipNextIteration();         
    }
    
    /**
     * Can be overwritten by Subclasses to add some logic AFTER the CommandReceiver/
     * SimulationAgent finished its bootstrap and BEFORE it progresses to the 
     * first Time Step.
     */
    protected void afterBootstrapFinished(){
        
    }
    
    
    
}
