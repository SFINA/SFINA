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
 *
 * @author mcb
 */
public class TimeSteppingAgent extends BasePeerlet implements TimeSteppingAgentInterface {
    
    private Time bootstrapTime;
    private Time runTime;

    
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
   
    @Override
    public void agentFinishedActiveState() {
        if(!getSimulationAgent().isConverged())
            progressCommandReceiverToNextIteration();

    }

    /************************************************************
     *      TimeSteppingAgentInterface Functions
     *************************************************************/
    
    @Override
    public final void agentFinishedBootStrap() {
        afterBootstrapFinished(); // sub clases can do some logic
        progressCommandReceiverToNextTimeStep(); // progress to Time 1
    }

    @Override
    public int getSimulationTime() {
       return ((int)(Time.inSeconds(this.getPeer().getClock().getTime())-Time.inSeconds(this.bootstrapTime)));
    }
    
    
    public SimulationAgentInterface getSimulationAgent() {
        return (SimulationAgentInterface) getPeer().getPeerletOfType(SimulationAgentInterface.class);
    }
        
    private CommandReceiver getCommandReceiver(){
        return (CommandReceiver) getPeer().getPeerletOfType(TimeSteppingAgentInterface.CommandReceiver.class);
    }

    /*************************************************************
     *      TimeSteppingAgent internal functions
     *************************************************************/
    
    /**
     * Recursive Function, which handels the Protopeer Time Stepping and hence
     * the Simulation Time.
     */
    private void progressCommandReceiverToNextTimeStep(){
        Timer loadAgentTimer=getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                getCommandReceiver().progressToNextTimeStep();
                
                progressCommandReceiverToNextTimeStep();
            }
        });
        loadAgentTimer.schedule(this.runTime);         
        
    }
    
    /**
     * Called by Subclasses to simulate another iteration.
     */
    protected final void progressCommandReceiverToNextIteration(){
        getCommandReceiver().progressToNextIteration(); 
    }
    /**
     * Called by Subclasses to simulate a skipped Iteration.
     */
    protected final void progressCommandReceiverToSkipNextIteration(){
        getCommandReceiver().skipNextIteration();         
    }
    
    /**
     * Can be overwritten by Subclasses to add some logic AFTER the CommandReceiver/
     * SimulationAgent finished its bootstrap and BEFORE it progresses to the 
     * first Time Step.
     */
    protected void afterBootstrapFinished(){
        
    }
    
    
    
}
