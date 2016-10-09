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
package interdependent;

import dsutil.protopeer.FingerDescriptor;
import org.apache.log4j.Logger;
import protopeer.BasePeerlet;
import protopeer.Peer;
import protopeer.time.Timer;
import protopeer.time.TimerListener;
import protopeer.util.quantities.Time;

/**
 *
 * @author evangelospournaras
 */
public class CommunicationAgent extends BasePeerlet{
    
    private static final Logger logger = Logger.getLogger(CommunicationAgent.class);

    private String experimentID;
    private Time bootstrapTime;
    private Time runTime;
    private FingerDescriptor myAgentDescriptor;
    
    public CommunicationAgent(
            String experimentID,
            Time bootstrapTime, 
            Time runTime){
        this.experimentID=experimentID;
        this.bootstrapTime=bootstrapTime;
        this.runTime=runTime;
    }
    
    /**
    * Inititializes the simulation agent by creating the finger descriptor.
    *
    * @param peer the local peer
    */
    @Override
    public void init(Peer peer){
        super.init(peer);
        this.myAgentDescriptor=new FingerDescriptor(getPeer().getFinger());
    }

    /**
    * Starts the simulation agent by scheduling the epoch measurements and 
    * bootstrapping the agent
    */
    @Override
    public void start(){
        this.runBootstraping();
    }

    /**
    * Stops the simulation agent
    */
    @Override
    public void stop(){
        
    }
    
    public void runBootstraping(){
        Timer loadAgentTimer= getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                 }
        });
        loadAgentTimer.schedule(this.bootstrapTime);
    }
}