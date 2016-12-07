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
    protected boolean readyToProgress() {
        //TBD: is this condition enough: it does not check for which keys exactly are inside 
        // -> Ben: now yes, added check when receiving messages
        // PROBLEM when interdependent link in input files to another network is defined, which is not loaded. The case, if 3 networks are prepared, but N = 2;
        // Should ideally be checked somewhere
        // Mark: can be checked here, maybe log it? currently I just take the min
        
        return this.externalNetworksFinishedStep.size() == (this.totalNumberNetworks - 1)
                && (this.externalNetworksSendEvent.size() == min(getSimulationAgent().getConnectedNetworkIndices().size(),this.totalNumberNetworks-1)) 
                && this.agentIsReady;
    }

    @Override
    protected boolean handleMessage(AbstractSfinaMessage message) {
        return false;
    }

    @Override
    protected boolean handlePostProcess(SfinaMessageType messageType) {
       return false;
    }

    @Override
    protected void handleCommandReceiverFinished() {
        
    }
    
    
    
    
    
 
    
}
