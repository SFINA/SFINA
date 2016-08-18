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

import core.SimulationAgentNew;
import event.Event;
import java.util.Collection;
import network.FlowNetwork;
import network.Link;
import network.Node;
import org.apache.log4j.Logger;
import protopeer.network.Message;
import protopeer.network.NetworkAddress;
import protopeer.time.Timer;
import protopeer.time.TimerListener;
import protopeer.util.quantities.Time;

/**
 *
 * @author Ben
 */
public abstract class SimulationAgentInterdependence extends SimulationAgentNew{
    
    private static final Logger logger = Logger.getLogger(SimulationAgentInterdependence.class);
    
    private NetworkAddress interdependentNetworkAgentAddress;
    private FlowNetwork interdependentFlowNetwork;
    
    public SimulationAgentInterdependence(
            String experimentID,
            Time bootstrapTime, 
            Time runTime){
        super(experimentID, bootstrapTime, runTime);
    }
    
    public void sendFlowNetworkToInterdependentPeer(NetworkAddress interdependentNetworkAgentAddress){
        this.interdependentNetworkAgentAddress = interdependentNetworkAgentAddress;
        this.getPeer().sendMessage(interdependentNetworkAgentAddress, new FlowNetworkMessage(this.getFlowNetwork()));
    }
    
    public void injectInterdependentEvent(Event event){
        this.getPeer().sendMessage(interdependentNetworkAgentAddress, new EventMessage(event));
    }
    
    private void sendStatusToInterdependentPeer(){
        this.getPeer().sendMessage(interdependentNetworkAgentAddress, new StatusMessage(isNetworkChanged(), getIteration()));
    }
    
    @Override
    public void runActiveState(){
        Timer loadAgentTimer=getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                initActiveState();
                
                executeAllEvents();
                
                sendStatusToInterdependentPeer();
                
                runActiveState(); 
            }
        });
        loadAgentTimer.schedule(this.getRunTime());
    }
    
    public void runInterdependentIteration(){
        runInitialOperations();
        executeAllEvents();
        runFlowAnalysis();
        runFinalOperations();
        nextIteration();
        sendStatusToInterdependentPeer();
    }
    
    @Override
    public abstract void runFlowAnalysis();
    
    
    @Override
    public void handleIncomingMessage(Message message) {
        logger.debug("\n##### Incoming message at network " + this.getPeer().getNetworkAddress() + " from interdependent network agent");
        if(message instanceof StatusMessage){
            int masterIteration = ((StatusMessage)message).getIteration();
            if(this.getIteration() != masterIteration){
                logger.debug("Iteration at network " + getPeer().getNetworkAddress() + " with iteration " + this.getIteration() + " differs from interdependent network iteration " + masterIteration);
            }
            
            updateFromInterdependentLinks();
            
            if(getIteration() == 1){
                logger.info("Net " + getPeer().getNetworkAddress() + ": Finished initializing time step, starting first iteration.");
                runInterdependentIteration();
            }
            else if(isNetworkChanged()){
                logger.info("Net " + getPeer().getNetworkAddress() + ": Network changed -> executing queued events and running next iteration.");
                runInterdependentIteration();
            }
            else{
                logger.info("Net " + getPeer().getNetworkAddress() + ": No network change, doing nothing.");
                setIteration(this.getIteration()+1);
                sendStatusToInterdependentPeer();
            }            
        }
        
        else if(message instanceof FlowNetworkMessage){
            FlowNetworkMessage netMessage = (FlowNetworkMessage)message;
            this.interdependentFlowNetwork = netMessage.getFlowNetwork();
            logger.debug("Interdependent flow network was successfully received at network " + getPeer().getNetworkAddress());
        }
    }
    
    /**
     * Checks if there are events in the queue for the current time.
     * @return if events for current time are queued
     */
    private boolean isNetworkChanged(){
        for(Event event : this.getEvents()){
            if(event.getTime() == this.getSimulationTime()){
                return true;
            }
        }
        return false;
    }
    
    public Collection<Link> getIncomingInterLinks(){
        return ((InterdependentFlowNetwork)interdependentFlowNetwork).getIncomingInterLinks(this.getPeer().getNetworkAddress());
    }
    
    public Collection<Link> getOutgoingInterLinks(){
        return ((InterdependentFlowNetwork)interdependentFlowNetwork).getOutgoingInterLinks(this.getPeer().getNetworkAddress());
    }

    private void updateFromInterdependentLinks(){
        for(Link link : getIncomingInterLinks()){
            if(link.isConnected() && link.isActivated()){
                if(link.getFlow() != 0.0){
                    this.queueEvent(updateEndNodeWithFlow(link.getEndNode(), link.getFlow()));
                }
            }
            else{
                this.queueEvent(updateEndNodeWhenFailure(link.getEndNode()));
            }
        }
    }
    
    /**
     * Update the end node of an incoming InterLink flow quantity. 
     * This is executed if the flow in the InterLink is non-zero, 
     * and the link is connected and activated.
     * Backend specific.
     * 
     * @param node
     * @param incomingFlow
     * @return event encoding the corresponding change to the end node.
     */
    public abstract Event updateEndNodeWithFlow(Node node, Double incomingFlow);
    
    /**
     * Update the end node of an incoming failed InterLink. 
     * This is executed the incoming link is either disconnected or deactivated.
     * Backend independent
     * 
     * @param node
     * @return event encoding the corresponding change to the end node.
     */
    public abstract Event updateEndNodeWhenFailure(Node node);
       
}