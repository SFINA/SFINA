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
package interdependent.communication;

import event.Event;
import event.NetworkComponent;
import interdependent.Messages.AbstractSfinaMessage;
import interdependent.Messages.EventMessage;
import interdependent.Messages.FinishedStepMessage;
import interdependent.Messages.NetworkAddressMessage;
import interdependent.Messages.SfinaMessageType;
import static java.lang.Integer.min;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import protopeer.Peer;
import protopeer.network.IntegerNetworkAddress;
import protopeer.network.Message;
import protopeer.network.NetworkAddress;
import protopeer.time.Timer;

/**
 *
 * @author mcb
 */
public class CommunicationAgentNew extends AbstractComunicationAgentLocalSimulation{
    
    private boolean afterBootstrap = false;
  
    /**
     * 
     * @param totalNumberNetworks 
     */
    public CommunicationAgentNew(int totalNumberNetworks) {
        super(totalNumberNetworks);
     
    }

    /**
     * ************************************************
     *          ABSTRACT METHODS
     * ************************************************
     */
     @Override
    protected ProgressType readyToProgress() {
       
        if(afterBootstrap){
            this.afterBootstrap = false;
            return ProgressType.DO_NEXT_STEP;
        }
        if(this.externalNetworksFinishedStep.size() == (this.totalNumberNetworks - 1)
                && (this.externalNetworksSendEvent.size() == min(getSimulationAgent().getConnectedNetworkIndices().size(),this.totalNumberNetworks-1)) 
                && this.agentIsReady){
            if(getCommandReceiver().pendingEventsInQueue()){
                // what happens if events where send after we send our finishedStep message, that we converged!
                // This has to be handled, when events are Received!
                return ProgressType.DO_ITERATION;
            }else{
                return ProgressType.DO_NEXT_STEP;
            }
        }else{
            return ProgressType.DO_NOTHING;
        }
           
    }

    @Override
    protected boolean handlePostProcess(SfinaMessageType messageType) {
       if(messageType.equals(SfinaMessageType.BOOT_FINISHED_MESSAGE)){
           this.afterBootstrap = true;
           return true;
        }
       return false;
    }

   

    
}
