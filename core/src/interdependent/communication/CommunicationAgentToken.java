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
 * Circular execution of iterations: first node 1, then node 2, ..., node n, node 1 etc.
 * @author mcb
 */
public class CommunicationAgentToken extends AbstractComunicationAgentLocalSimulation{

    
    private boolean hasToken;
    private int nextNetwork;
    private int startingNetwork;
    
    private boolean afterbootStrap =false;
    
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
    
    
    @Override
    protected ProgressType readyToProgress() {
        //as a quick and dirty fix -> all networks after bootup will run one iteration
        // see below
        if(afterbootStrap){
                this.afterbootStrap = false;
                return ProgressType.DO_NEXT_STEP;
        }
        
        // Ben: some more checks than just hasToken is necessary, 
        // because otherwise a next step will be triggered once for every incoming message
        if(this.hasToken
                && this.externalNetworksFinished.size() == (this.totalNumberNetworks - 1)
                && (this.externalNetworksEvents.size() == min(getSimulationAgent().getConnectedNetworkIndices().size(),this.totalNumberNetworks-1)) 
                && this.agentIsReady){
            // here is a problem, readyToProgress assumes, that algorithm was already running once. But after bootstrap
            // currently we do nothing. Hence we arrive at that point not having executed the code once. 
            // as a quick and dirty fix -> all networks after bootup will run one iteration
            // throuh this iteration 2 will happen first at the network after the starting network
            if(getCommandReceiver().pendingEventsInQueue())
                return ProgressType.DO_NEXT_ITERATION;
//            else if(isFirst()){
//                return ProgressType.DO_NEXT_STEP;
//            }
            else if(externalNetworksConverged()){
                // Ben: To me it seems not to be executed in the right order, is that possible?
                return ProgressType.DO_NEXT_STEP;
//                if(isFirst())
//                    return ProgressType.DO_NEXT_STEP;
//                else{
//                    sendTokenToFirst();
//                    return ProgressType.DO_NOTHING;
//                }                
            }
            else{
                // here we should increment the iteration number, s.t. they're always in sync between networks
                // or here should the Token be send to the next, as we are having the token
                // Handled by skipNextIteration() which calls again agentIsReady, which sends the token message
                //sendTokenToNext();
                return ProgressType.SKIP_NEXT_ITERATION;
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
    protected void handleCommunicationEvent(CommunicationEventType messageType) {
        switch(messageType){
            case BOOT_FINISHED:
                this.afterbootStrap = true;
            // See above
//            case TOKEN_MESSAGE:
//                getSimulationAgent().queueEvents(eventsToQueue);
//                return false;
                break;
            case AGENT_IS_READY:
//                if(this.agentIsReady && this.hasToken ){
//                    if(!getCommandReceiver().pendingEventsInQueue()){
//                        sendTokenToNext();
//                        return true;
//                    }else{
//                        return true;
//                    }
//                }
                // Ben: Why check if agent is ready and has token? both should be always true.
                // Events injected automatically before executing next iteration/step
                    // Mark: Is correct, but pendingEventsInQueue should be checked. 
                    // Or we agree, that after each iteration we progress to the next SimulationAgent,
                    // in this case we should not check for converged and not converged as it is currently done
                    // in readyToProgress, bu then we have to send The token in readyToProgress if we do nothing! Else deadlock. (this is the current version)
//                this.hasToken = false;
//                TokenMessage message = new TokenMessage(getSimulationAgent().getNetworkIndex());
//                sendToSpecific(message, nextNetwork);
//                return false;
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
    
    private void sendTokenToFirst(){
        this.hasToken = false;
        TokenMessage message = new TokenMessage(getSimulationAgent().getNetworkIndex());
        sendToSpecific(message, startingNetwork);
    }    
    
    private boolean isFirst(){
        return this.getSimulationAgent().getNetworkIndex() == startingNetwork;
    }
}