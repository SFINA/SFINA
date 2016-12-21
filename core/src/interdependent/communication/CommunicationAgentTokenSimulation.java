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
import static java.lang.Integer.min;
import protopeer.Peer;

/**
 * Circular execution of iterations: first network 1, then network 2, ..., network n, network 1 etc.
 * @author mcb
 */
public class CommunicationAgentTokenSimulation extends AbstractComunicationAgentLocalSimulation{

    
    private boolean hasToken;
    private int nextNetwork;
    private int startingNetwork;
    
    private boolean afterbootStrap =false;
    
    public CommunicationAgentTokenSimulation(int totalNumberNetworks, int startingNetwork){
        super(totalNumberNetworks);
        this.startingNetwork = startingNetwork;    
    }
    
    public CommunicationAgentTokenSimulation(int totalNumberNetworks) {
        this(totalNumberNetworks, 0);     
    }

    @Override
    public void init(Peer peer) {
        super.init(peer); 
        this.hasToken = getSimulationAgent().getNetworkIndex()==startingNetwork;
        this.nextNetwork = ((getSimulationAgent().getNetworkIndex()+1) % totalNumberNetworks);
    }
    
    
    @Override
    protected ProgressType readyToProgress() {
        if(this.hasToken){
            if(afterbootStrap){
                this.afterbootStrap = false;
                return ProgressType.DO_NEXT_STEP;
            }
            if(!getCommandReceiver().isConverged()){
                return ProgressType.DO_NEXT_ITERATION;
            } 
            else{
                if(isFirst() && externalNetworksConverged()){
                    return ProgressType.DO_NEXT_STEP;
                } 
                else
                    return ProgressType.SKIP_NEXT_ITERATION;
            }
        }
        else 
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
    protected void handleCommunicationEvent(CommunicationEventType messageType) {
        switch(messageType){
            case BOOT_FINISHED:
                this.afterbootStrap = true;
                break;
            case AGENT_IS_READY:
                sendTokenToNext();
                break;
            default:
                break;
        }
    }
    
    private void sendTokenToNext(){
        this.hasToken = false;
        TokenMessage message = new TokenMessage(getSimulationAgent().getNetworkIndex());
        sendToSpecific(message, nextNetwork);
    }   
    
    private boolean isFirst(){
        return this.getSimulationAgent().getNetworkIndex() == startingNetwork;
    }
}