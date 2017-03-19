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
package interdependent.communication.agents;

import core.TimeSteppingAgent;
import event.Event;
import event.EventType;
import event.NetworkComponent;
import interdependent.Messages.AbstractSfinaMessage;
import interdependent.Messages.EventMessage;
import interdependent.Messages.FinishedActiveStateMessage;
import interdependent.Messages.ProgressedToNextStepMessage;
import interdependent.communication.CommunicationEventType;
import interdependent.communication.EventNegotiatorAgentInterface;
import interdependent.communication.ProgressType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import network.InterdependentLink;
import network.LinkState;
import network.NodeState;
import org.apache.log4j.Logger;
import protopeer.network.Message;
import protopeer.network.NetworkAddress;
import protopeer.util.quantities.Time;

/**
 *
 * @author mcb
 */
public abstract class AbstractCommunicationAgent extends TimeSteppingAgent{
   
    protected final static int POST_ADDRESS_CHANGE_RECEIVED = 0;
    protected final static int POST_AGENT_IS_READY = 1;
    protected final static int POST_FINISHED_STEP_RECEIVED = 2;
    protected final static int POST_EVENT_RECEIVED = 3;

    protected static final Logger logger = Logger.getLogger(AbstractCommunicationAgent.class);
    
    protected Map<Integer, Boolean> externalNetworksFinished;
    protected Map<Integer, List<Event>> externalNetworksEvents;
    
    protected Set<Integer> progressedToNextStep = new HashSet<>();
    
    protected int totalNumberNetworks;
    protected boolean agentIsReady;

  
    private boolean eventSendToOthers = false;
    private boolean lastIterationSkipped = false;
    
    /**
     * 
     * @param totalNumberNetworks 
     */
    public AbstractCommunicationAgent(Time bootstrapTime, Time runTime,int totalNumberNetworks) {
        super(bootstrapTime, runTime);
        this.totalNumberNetworks = totalNumberNetworks;
        this.externalNetworksFinished = new HashMap<>();
        this.externalNetworksEvents = new HashMap<>();
        this.agentIsReady = false;
        
    }
    
    
    /**
     * ************************************************
     *        TIME STEPPING FUNCTIONS
     * ************************************************
     */
    @Override
    public void agentFinishedActiveState() {
        
         logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Agent Finished Active State ");
     
            logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Time is: "+ Integer.toString(getSimulationTime()));
            
            
        this.agentIsReady = true;
        this.eventSendToOthers = false;
        for(Map.Entry<Integer,List<Event>> entry : this.extractPendingInterdependentEvents().entrySet()){
            this.eventSendToOthers = true;
            EventMessage eventMessage = new EventMessage(getSimulationAgent().getNetworkIndex(), entry.getValue());
            sendToSpecific(eventMessage, entry.getKey());
        }
            
        FinishedActiveStateMessage message = new FinishedActiveStateMessage(
                getSimulationAgent().getNetworkIndex(), getSimulationTime(), getSimulationAgent().getIteration(), getSimulationAgent().isConverged());
        sendToAll(message);
        
        this.postProcessAbstractCommunication(CommunicationEventType.AGENT_IS_READY);
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
         
            logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Handle Incomming " + sfinaMessage.getMessageType().toString()+" Message  from " + 
                    Integer.toString(sfinaMessage.getNetworkIdentifier()));
            logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": default");
            logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Time is: "+ Integer.toString(getSimulationTime()));
            
            
            switch (sfinaMessage.getMessageType()) {
                case EVENT_MESSAGE:
                    EventMessage eventMessage = (EventMessage) sfinaMessage;
                    this.externalNetworksEvents.put(sfinaMessage.getNetworkIdentifier(), eventMessage.getEvents());
                    break;
                case FINISHED_ACTIVE_STATE:
                    FinishedActiveStateMessage finishedMessage = ((FinishedActiveStateMessage)sfinaMessage);
                    this.externalNetworksFinished.put(finishedMessage.getNetworkIdentifier(), finishedMessage.isConverged());
                    break;
                case PROGRESSED_TO_NEXT_STEP:
                    this.progressedToNextStep.add(sfinaMessage.getNetworkIdentifier());
                    break;                   
                default:
                    if(!handleMessage(sfinaMessage))
                        logger.debug("Message Type not recognized");
            }
            postProcessAbstractCommunication(sfinaMessage.getMessageType());
        }
    }
    
    /**
     * Always called when Message Type was not recognized by AbstractCommunication Agent
     * Subclasses which like to introduce additional Communication Messages have
     * to overwrite this Function
     * @param message 
     * @return TRUE if @message was handled by sublcasses, else FALSE
     */
    protected boolean handleMessage(AbstractSfinaMessage message){
        return false;
    }     
     /**
     * ***************************************
     *         COMMUNICATION POSTPROCESS
     * ***************************************
     */
    /**
     * Each time Communication happens/ something chagnes this function should be called 
     * Handles necessary further steps
     * @param communicationEventType 
     */
    private void postProcessAbstractCommunication(CommunicationEventType communicationEventType) {

        postProcessCommunicationEvent(communicationEventType);
        

         logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Before Ready To Progress");
        // Decide what to do 
        switch(readyToProgress()){
            case DO_NEXT_ITERATION:
                this.lastIterationSkipped = false;
                 logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": do Next Iteration");
                 
                 
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Time is: "+ Integer.toString(getSimulationTime()));
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Current Iteration: " + Integer.toString(getSimulationAgent().getIteration()));
                doNextIteration();
                break;
            case DO_NEXT_STEP:
                this.lastIterationSkipped = false;
                 logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": do Next Step");
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Time is: "+ Integer.toString(getSimulationTime()));
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Current Iteration: " + Integer.toString(getSimulationAgent().getIteration()));
                doNextStep();
                break;
            case SKIP_NEXT_ITERATION:
                this.lastIterationSkipped = true;
                 logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": skip Iteration");
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Time is: "+ Integer.toString(getSimulationTime()));
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Current Iteration: " + Integer.toString(getSimulationAgent().getIteration()));
                skipNextIteration();
                break;
            case DO_NOTHING:
                this.lastIterationSkipped = false;
                 logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": do nothing");
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Time is: "+ Integer.toString(getSimulationTime()));
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Current Iteration: " + Integer.toString(getSimulationAgent().getIteration()));
                break;
            default:
                 logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": default");
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Time is: "+ Integer.toString(getSimulationTime()));
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Current Iteration: " + Integer.toString(getSimulationAgent().getIteration()));
                this.lastIterationSkipped = false;
                doNextStep(); 
        }     
    }

    protected void doNextStep(){

            clearCommunicationAgent();
            injectEvents();         
            this.progressedToNextStep.clear();
            sendToAll(new ProgressedToNextStepMessage(getSimulationAgent().getNetworkIndex()));
            
     
    }
    
    protected void doNextIteration(){

        clearCommunicationAgent();
        injectEvents();
        progressCommandReceiverToNextIteration();
  
    }
    
    protected void skipNextIteration(){
            clearCommunicationAgent();
            progressCommandReceiverToSkipNextIteration();

    }
    
    private void clearCommunicationAgent(){
        this.externalNetworksEvents.clear();
        this.externalNetworksFinished.clear();
        this.agentIsReady = false;
    }
    
    protected boolean externalNetworksConverged(){
        
        for(Boolean converged : this.externalNetworksFinished.values()){
            if(!converged)
                return false;
        }
             return true;
    }
        
  /**
     * ***************************************
     *         MESSAGE SENDING
     * ***************************************
     */

    /**
     * Sends message to all existing Peers
     * @param message 
     */
    protected void sendToAll(AbstractSfinaMessage message){
        for(NetworkAddress address: getAllExternalNetworkAddresses().values())
            getPeer().sendMessage(address, message);
    }
    
    /**
     * Sends message to Peers which have A Network connected to this Peer
     * @param message 
     */
    protected void sendToConnected(AbstractSfinaMessage message){
        for(NetworkAddress address: getConnectedExternalNetworkAddresses().values())
            getPeer().sendMessage(address, message);
    }
    
    /**
     * Sends message to Peer which has the Network with networkIndex
     * @param message
     * @param networkIndex 
     */
    protected void sendToSpecific(AbstractSfinaMessage message, int networkIndex){
        getPeer().sendMessage(getNetworkAddress(networkIndex), message);
    }
        
            
  
     /**
     * ************************************************
     *          EVENT METHODS
     * ************************************************
     */
 
    /**
     * Retrieves all events relevant for the interdependent link.
     * Separates them per network.
     * @return Map with List of events mapped to the network index they have to
     * be send to.
     */
    protected Map<Integer, List<Event>> extractPendingInterdependentEvents(){
        Map<Integer, List<Event>> interdependentEvents = new HashMap<>();
        for(Integer netID : getSimulationAgent().getConnectedNetworkIndices())
            interdependentEvents.put(netID, new ArrayList<>());
        for(Event event : this.getSimulationAgent().getEvents()){
            if(event.getTime() >= this.getSimulationAgent().getSimulationTime() && event.getNetworkComponent() != null){
                if(event.getNetworkComponent().equals(NetworkComponent.INTERDEPENDENT_LINK)){
                    int targetNetwork = this.getSimulationAgent().getFlowNetwork().getInterdependentLink(event.getComponentID()).getRemoteNetworkIndex();
                    interdependentEvents.get(targetNetwork).add(event);
                }
                if(event.getNetworkComponent().equals(NetworkComponent.NODE) 
                    && event.getParameter().equals(NodeState.STATUS)
                    && !getSimulationAgent().getFlowNetwork().getNode(event.getComponentID()).getLinksInterdependent().isEmpty()){
                    // We have to notify the other network also, when the status of a node with attached interdependent link is changed
                    // That should be the only property, which is not encoded in the interdependent link events.
                    // This solution is kinda ugly, maybe there's a better one??
                    for(InterdependentLink link : getSimulationAgent().getFlowNetwork().getNode(event.getComponentID()).getLinksInterdependent()){
                        int targetNetwork = link.getRemoteNetworkIndex();
                        Event remoteNodeStatusChange = new Event(event.getTime(), EventType.TOPOLOGY, NetworkComponent.INTERDEPENDENT_LINK, link.getIndex(), LinkState.REMOTE_NODE_STATUS, event.getValue());
                        interdependentEvents.get(targetNetwork).add(remoteNodeStatusChange);
                    }
                }
            }
        }
        return interdependentEvents;
    }
    
    private void injectEvents(){
        for(List<Event> events : this.externalNetworksEvents.values()){
            for(Event event : events){
                getSimulationAgent().queueEvents(translateEvent(checkEventForConflict(event)));
            }
        }
    }
    
    private Event checkEventForConflict(Event event) {
        ArrayList<Event> conflictingEvents = getConflictingEvents(event);
        if(conflictingEvents.isEmpty())
            return event;
        else{
            for(Event conflictingEvent : conflictingEvents)
                getSimulationAgent().removeEvent(conflictingEvent);
            conflictingEvents.add(event);
            // Simplest case: event exactly the same. In that case, just return first one.
            boolean allSame = false;
            for(Event e1 : conflictingEvents){
                for(Event e2 : conflictingEvents){
                    if(e1.getValue().equals(e2.getValue()))
                        allSame = true;
                    else
                        allSame = false;
                }
            }
            if(allSame)
                return conflictingEvents.get(0);
            else
                return negotiateEvents(conflictingEvents);
        }
    }   
    
    private ArrayList<Event> getConflictingEvents(Event event) {
        ArrayList<Event> conflictingEvents = new ArrayList<>();
        for(Event agentEvent : getSimulationAgent().getEvents())
            if(sameEventTarget(agentEvent, event))
                conflictingEvents.add(agentEvent);
        return conflictingEvents;
    }
    
    private boolean sameEventTarget(Event e1, Event e2){
        if(e1.getTime() == e2.getTime()
                && e1.getEventType() == e2.getEventType()
                && e1.getNetworkComponent() == e2.getNetworkComponent()
                && e1.getComponentID().equals(e2.getComponentID())
                && e1.getParameter() == e2.getParameter())
            return true;
        return false;
    }    

    private Event negotiateEvents(List<Event> events) {
        return ((EventNegotiatorAgentInterface) this.getPeer().getPeerletOfType(EventNegotiatorAgentInterface.class)).negotiateEvents(events);
    }
    
    private List<Event> translateEvent(Event event) {
        return ((EventNegotiatorAgentInterface) this.getPeer().getPeerletOfType(EventNegotiatorAgentInterface.class)).translateEvent(event);
    }

  
    /**
     * ************************************************
     *          ABSTRACT METHODS
     * ************************************************
     */
    
    /**
     * Always called by the AbstractCommunicationAgent at the very end of a 
     * communication Cycle to detect how it has to progress
     * @return 
     */
    protected abstract ProgressType readyToProgress();
    
    /**
     * Always called after every Communication with the AbstractCommunication Event
     * (agentFinishedStep, incomming message from other peer). Allows subclass to 
     * introduce logic and change it state after each communication.
     * @param eventType 
     * @return true if if evenType has been handled by Subclass
     */
    protected abstract boolean postProcessCommunicationEvent(CommunicationEventType eventType);
    
 
    /**************  NETWORKING FUNCTIONS ************************
    
    /**
     * Returns all Addresses of Networks of the Simulation
     * @return Map containing the networkIndex as a key and the corresponding NetworkAddres as Value
     */
    protected abstract Map<Integer, NetworkAddress> getAllExternalNetworkAddresses();
    
    /**
     * Returns all Addresses of Networks connected to the Network of this Peer
     * @return Map containing the networkIndex as a kez and the corresponding NetworkAddres as Value
     */
    protected abstract Map<Integer, NetworkAddress> getConnectedExternalNetworkAddresses();
    
    
    /**
     * Returns the Address of the Network identified by the networkIndex
     * @param networkIndex
     * @return the NetworkAddress
     */
    protected abstract NetworkAddress getNetworkAddress(int networkIndex);
       
}