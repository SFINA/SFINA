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
public class SimpleTimeSteppingAgent_new extends BasePeerlet implements TimeSteppingAgentInterface_new {
    
    private Time bootstrapTime;
    private Time runTime;

    
    public SimpleTimeSteppingAgent_new(Time bootstrapTime, Time runTime) {
        this.bootstrapTime = bootstrapTime;
        this.runTime = runTime;
    }
      
    
    @Override
    public void agentFinishedActiveState() {
        if(!getSimulationAgent().isConverged())
            progressCommandReceiverToNextIteration();
        else
            progressCommandReceiverToNextTimeStep();
    }

    @Override
    public void agentFinishedBootStrap() {
        progressCommandReceiverToNextTimeStep();
    }

    @Override
    public int getSimulationTime() {
       return ((int)(Time.inSeconds(this.getPeer().getClock().getTime())-Time.inSeconds(this.bootstrapTime)));
    }
    
    
    public SimulationAgentInterface getSimulationAgent() {
        return (SimulationAgentInterface) getPeer().getPeerletOfType(SimulationAgentInterface.class);
    }
        
    private CommandReceiver getCommandReceiver(){
        return (CommandReceiver) getPeer().getPeerletOfType(TimeSteppingAgentInterface_new.CommandReceiver.class);
    }

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
    
    protected final void progressCommandReceiverToNextTimeStep(){
        Timer loadAgentTimer=getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                getCommandReceiver().progressToNextTimeStep();
            }
        });
        loadAgentTimer.schedule(this.runTime);         
    }
    
    protected final void progressCommandReceiverToNextIteration(){
        getCommandReceiver().progressToNextIteration(); 
    }
    
    protected final void progressCommandReceiverToSkipNextIteration(){
        getCommandReceiver().skipNextIteration();         
    }
    
    
    
    
    
}
