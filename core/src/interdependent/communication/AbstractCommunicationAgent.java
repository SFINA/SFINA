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

import core.SimpleTimeSteppingAgent;
import event.Event;
import event.NetworkComponent;
import interdependent.Messages.AbstractSfinaMessage;
import interdependent.Messages.EventMessage;
import interdependent.Messages.FinishedStepMessage;
import interdependent.Messages.NetworkAddressMessage;
import interdependent.Messages.SfinaMessageType;
import static interdependent.communication.CommunicationAgent.POST_ADDRESS_CHANGE_RECEIVED;
import static interdependent.communication.CommunicationAgent.POST_AGENT_IS_READY;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import protopeer.network.IntegerNetworkAddress;
import protopeer.network.Message;
import protopeer.network.NetworkAddress;
import protopeer.time.Timer;

/**
 * BaseClass for interdependent Communication
 * @author mcb
 */
public abstract class AbstractCommunicationAgent extends SimpleTimeSteppingAgent{
   
    protected final static int POST_ADDRESS_CHANGE_RECEIVED = 0;
    protected final static int POST_AGENT_IS_READY = 1;
    protected final static int POST_FINISHED_STEP_RECEIVED = 2;
    protected final static int POST_EVENT_RECEIVED = 3;

    protected static final Logger logger = Logger.getLogger(CommunicationAgent.class);
    protected Map<Integer, NetworkAddress> externalNetworkAddresses;
    protected List<Integer> externalNetworksFinishedStep;
    protected boolean externalNetworksConverged;
    protected List<Integer> externalNetworksSendEvent;
    protected List<Event> eventsToQueue;
    protected int totalNumberNetworks;
    protected boolean agentIsReady; 

    protected Timer initialDelayTimer;
    
    
    
    /**
     * 
     * @param totalNumberNetworks 
     */
    public AbstractCommunicationAgent(int totalNumberNetworks) {
        super();
        this.externalNetworkAddresses = new HashMap();
        this.totalNumberNetworks = totalNumberNetworks;
        this.externalNetworksFinishedStep = new ArrayList<>();
        this.externalNetworksSendEvent = new ArrayList<>();
        this.eventsToQueue = new ArrayList<>();
        this.agentIsReady = false;
        this.externalNetworksConverged = true;
    }
    
    
    
    
    
    /**
     * ************************************************
     *        TIME STEPPING FUNCTIONS
     * ************************************************
     */
    @Override
    public void agentFinishedActiveState() {        
        this.agentIsReady = true;
        
        // inform other Communication Agents
        FinishedStepMessage message = new FinishedStepMessage(getSimulationAgent().getNetworkIndex(), getSimulationAgent().getSimulationTime(), getSimulationAgent().getIteration(), getCommandReceiver().pendingEventsInQueue());
        sendToAll(message);

        EventMessage eventMessage = new EventMessage(getSimulationAgent().getNetworkIndex(), this.extractPendingInterdependentEvents());
        sendToConnected(eventMessage);

        // post process the communication
        this.postProcessAbstractCommunication(SfinaMessageType.AGENT_IS_READY);
    }
 
    
     /**
     * ***************************************
     *         COMMUNICATION WITH OTHER AGENTS
     * ***************************************
     */
    @Override
    public void handleIncomingMessage(Message message) {

        //check if its a SFINA Message
        if (message instanceof AbstractSfinaMessage) {

            AbstractSfinaMessage sfinaMessage = (AbstractSfinaMessage) message;

            switch (sfinaMessage.getMessageType()) {
                case EVENT_MESSAGE:
                    //TBD: Maybe Collect, or runtime etc.? has to be discussed
                    //Ben: Maybe better, in case some events arrive in the middle of executing events. Probably not a problem, but better be safe.
                    this.eventsToQueue.addAll(((EventMessage) sfinaMessage).getEvents());
                    
                    if(!externalNetworksSendEvent.contains(sfinaMessage.getNetworkIdentifier()))
                        this.externalNetworksSendEvent.add(sfinaMessage.getNetworkIdentifier());
                    else
                        logger.debug("Attention: Event message already received from this network, shoudn't happen.");
                    postProcessAbstractCommunication(SfinaMessageType.EVENT_MESSAGE);
                    break;
                case FINISHED_STEP:
                    if(!externalNetworksFinishedStep.contains(sfinaMessage.getNetworkIdentifier())){
                        this.externalNetworksFinishedStep.add(sfinaMessage.getNetworkIdentifier());
                        if(!((FinishedStepMessage)sfinaMessage).isConverged())
                            this.externalNetworksConverged = false;
                    }
                    else
                        logger.debug("Attention: Finished Step message already received from this network, shoudn't happen.");
                    postProcessAbstractCommunication(SfinaMessageType.FINISHED_STEP);
                    break;
                default:
                    if(!handleMessage(sfinaMessage))
                        logger.debug("Message Type not recognized");
                    else
                        postProcessAbstractCommunication(sfinaMessage.getMessageType());

            }
        }

    }
    protected void sendToAll(AbstractSfinaMessage message){
        for(NetworkAddress address: getAllExternalNetworkAddresses().values())
            getPeer().sendMessage(address, message);
    }
    
    protected void sendToConnected(AbstractSfinaMessage message){
        for(NetworkAddress address: getConnectedExternalNetworkAddresses().values())
            getPeer().sendMessage(address, message);
    }
    
    
     /**
     * ***************************************
     *         COMMUNICATION POSTPROCESS
     * ***************************************
     */
    private void postProcessAbstractCommunication(SfinaMessageType typeOfPost) {
        // each time something changes this function should be called 
        // handles necessary further steps
        if(!handlePostProcess(typeOfPost));
            checkAndNextStep();
       
    }
    
    public void checkAndNextStep() {
        if (readyToProgress()) {
            
            this.externalNetworksFinishedStep.clear();
            this.externalNetworksSendEvent.clear();
            this.agentIsReady = false;
            this.getSimulationAgent().queueEvents(eventsToQueue);
            this.eventsToQueue.clear();
            this.checkEventsForConflicts();
            
            // TBD: This part should be double checked. logic a bit tricky.
            // Mark: still not 100 percent secure as a no events could be waiting + 
            // other networks send finished step, but they have events and still need to progress
            // then we are not allowed to go to next time step
            // Mark: solution: finished step message contains all events, which have to be send, when i got from
            // all networks a finished message, with no events! AND I did not send events, then progress. 
            // IN this way we know, that everyone finished. 
            // This approach assumes, that a Simulationagent only finshes, when it converged. (Hence convergence logic is
            // handled in the SimulationAgent)
            
            // this really has to be rechecked, i think one should remove pendingEventsInQueue, SimulationAGent should handle the 
            // case when it still has 
//            if(this.getSimulationAgent().getIteration() == 0 ) // The case after bootstraping, maybe there's a better way to always ensure that after bootsraping it doesn't stop? E.g. make a different method agentFinishedBootstraping()
//                getCommandReceiver().progressToNextTimeStep();
//            else
//                getCommandReceiver().progressToNextTimeStep();
            
             if(this.getSimulationAgent().getIteration() == 0 ) // The case after bootstraping, maybe there's a better way to always ensure that after bootsraping it doesn't stop? E.g. make a different method agentFinishedBootstraping()
               getCommandReceiver().progressToNextTimeStep();
            else if(getCommandReceiver().pendingEventsInQueue()) // if this network has more events waiting for this time step, continue iterations
                getCommandReceiver().progressToNextIteration();
            else if(this.externalNetworksConverged) // if this network doesn't have more events waiting and the other networks have also finished, continue to next time step
                getCommandReceiver().progressToNextTimeStep();
            else{ // if the above two don't hold, wait ?? it is not really connected?
                this.externalNetworksConverged = true;
                //Todo this is not working
                logger.debug("No pending events, but other networks still iterating -> Waiting");
                getCommandReceiver().progressToNextTimeStep();
            }
        }

    }
  
     /**
     * ************************************************
     *          EVENT METHODS
     * ************************************************
     */
 
    // todo mark: what is with events, which will be triggered in next time steps, i have 
    // the feeling, that when the agent is readyToProgress, one has to send maybe another
    // Events, which will be for the next time steps? What do you think? or are Event
    // Messages only there for communication changes in the current iteration? has to be discussed
    protected List<Event> extractPendingInterdependentEvents(){
        List<Event> interdependentEvents = new ArrayList<>();
        for(Event event : this.getSimulationAgent().getEvents())
            if(event.getTime() == this.getSimulationAgent().getSimulationTime() && event.getNetworkComponent().equals(NetworkComponent.INTERDEPENDENT_LINK))
                interdependentEvents.add(event);
        return interdependentEvents;
    }
    
    // TBD: Can be here or in SimulationAgent, what makes more sense?
    // Ben: I think better here, to be easily changeable without touching SimulationAgent
    // Mark: yes its true only point is maybe, that Simulationagent should so 
    // or so make sure when events are inserted, that these events are valid, or 
    // am i wrong?
    private void checkEventsForConflicts() {
        logger.debug("Conflict check of events not implemented yet.");
    }   
    
    
    
     /**
     * ************************************************
     *          ABSTRACT METHODS
     * ************************************************
     */
    protected abstract boolean readyToProgress();
    
    protected abstract Map<Integer, NetworkAddress> getAllExternalNetworkAddresses();
    
    // DANGEROUS
    protected abstract Map<Integer, NetworkAddress> getConnectedExternalNetworkAddresses();
    
    protected abstract boolean handleMessage(AbstractSfinaMessage message);
    
    protected abstract boolean handlePostProcess(SfinaMessageType messageType);
    
    protected abstract void handleCommandReceiverFinished();
      
}
