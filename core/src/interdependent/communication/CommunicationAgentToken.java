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

import interdependent.Messages.AbstractSfinaMessage;
import interdependent.Messages.TokenMessage;
import protopeer.Peer;

/**
 * Circular execution of iterations: first node 1, then node 2, ..., node n, node 1 etc.
 * @author mcb
 */
public class CommunicationAgentToken extends AbstractComunicationAgentLocalSimulation{

    
    private boolean hasToken;
    private int nextNetwork;
    private int startingNetwork;
    
    public CommunicationAgentToken(int totalNumberNetworks, int startingNetwork){
        super(totalNumberNetworks);
        this.startingNetwork = startingNetwork;    
    }
    
    public CommunicationAgentToken(int totalNumberNetworks) {
        this(totalNumberNetworks, 0);     
    }

    @Override
    public void init(Peer peer) {
        super.init(peer); 
        this.hasToken = getSimulationAgent().getNetworkIndex()==startingNetwork;
        this.nextNetwork = ((getSimulationAgent().getNetworkIndex()+1) % totalNumberNetworks);
    }
    
    //Just dummy!
    // Ben: ?
    @Override
    protected ProgressType readyToProgress() {
        if(this.hasToken){
            if(getCommandReceiver().pendingEventsInQueue())
                return ProgressType.DO_NEXT_ITERATION;
            else if(externalNetworksConverged())
                return ProgressType.DO_NEXT_STEP;
            else{
                // here we should increment the iteration number, s.t. they're always in sync between networks
                return ProgressType.DO_NOTHING;
            }
        }else
            return ProgressType.DO_NOTHING;
    }

    @Override
    protected boolean handleMessage(AbstractSfinaMessage message) {
        switch(message.getMessageType()){
            case TOKEN_MESSAGE:
                this.hasToken = true;
                return true;
            default:
                return false;
        }
    }

    @Override
    protected boolean handleCommunicationEvent(CommunicationEventType messageType) {
        switch(messageType){
            // See above
//            case TOKEN_MESSAGE:
//                getSimulationAgent().queueEvents(eventsToQueue);
//                return false;
            case BOOT_FINISHED:
            case AGENT_IS_READY:
//                if(this.agentIsReady && this.hasToken ){
//                    if(this.eventsToQueue.size()==0 && !getCommandReceiver().pendingEventsInQueue()){
//                        this.hasToken = false;
//                        TokenMessage message = new TokenMessage(getSimulationAgent().getNetworkIndex());
//                        sendToSpecific(message, nextNetwork);
//                        return true;
//                    }else{
//                        getSimulationAgent().queueEvents(eventsToQueue);
//                        return true;
//                    }
//                }
                // Ben: Why check if agent is ready and has token? both should be always true.
                // Events injected automatically before executing next iteration/step
                this.hasToken = false;
                TokenMessage message = new TokenMessage(getSimulationAgent().getNetworkIndex());
                sendToSpecific(message, nextNetwork);
                return false;
            default:
                return false;    
        }
    }

    
}