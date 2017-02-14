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

import Debug.TimeSteppingInterface_Debug;
import protopeer.time.Timer;
import protopeer.time.TimerListener;
import protopeer.util.quantities.Time;
import Debug.SimulationAgentInterface_Debug;
import protopeer.BasePeerlet;

/**
 *
 * @author mcb
 */
public class TimeSteppingAgent_Debug extends BasePeerlet implements TimeSteppingInterface_Debug {
    private Time bootstrapTime;
    private Time runTime;

    
    public TimeSteppingAgent_Debug(Time bootstrapTime, Time runTime) {
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
    
    
    public SimulationAgentInterface_Debug getSimulationAgent() {
        return (SimulationAgentInterface_Debug) getPeer().getPeerletOfType(SimulationAgentInterface_Debug.class);
    }
        
    private TimeSteppingAgent_Debug.CommandReceiver getCommandReceiver(){
        return (TimeSteppingAgent_Debug.CommandReceiver) getPeer().getPeerletOfType(TimeSteppingAgent_Debug.CommandReceiver.class);
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
    
    protected void progressCommandReceiverToNextTimeStep(){
        Timer loadAgentTimer=getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                getCommandReceiver().progressToNextTimeStep();
            }
        });
        loadAgentTimer.schedule(this.runTime);         
        
    }
    
    protected void progressCommandReceiverToNextIteration(){
        getCommandReceiver().progressToNextIteration(); 
    }
    
    protected void progressCommandReceiverToSkipNextIteration(){
        getCommandReceiver().skipNextIteration();         
    }
    
    
}
