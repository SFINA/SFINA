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
import event.EventType;
import event.NetworkComponent;
import interdependent.Messages.AbstractSfinaMessage;
import interdependent.Messages.EventMessage;
import interdependent.Messages.FinishedStepMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import network.InterdependentLink;
import network.LinkState;
import network.NodeState;
import org.apache.log4j.Logger;
import protopeer.network.Message;
import protopeer.network.NetworkAddress;

/**
 * BaseClass for interdependent Communication
 * @author mcb
 */
public abstract class AbstractCommunicationAgent extends SimpleTimeSteppingAgent{
   
    protected final static int POST_ADDRESS_CHANGE_RECEIVED = 0;
    protected final static int POST_AGENT_IS_READY = 1;
    protected final static int POST_FINISHED_STEP_RECEIVED = 2;
    protected final static int POST_EVENT_RECEIVED = 3;

    protected static final Logger logger = Logger.getLogger(AbstractCommunicationAgent.class);
    
    protected Map<Integer, Boolean> externalNetworksFinished;
    protected Map<Integer, List<Event>> externalNetworksEvents;
    
    protected int totalNumberNetworks;
    protected boolean agentIsReady; 
    private boolean bootFinished;

    
    /**
     * 
     * @param totalNumberNetworks 
     */
    public AbstractCommunicationAgent(int totalNumberNetworks) {
        super();
        this.totalNumberNetworks = totalNumberNetworks;
        this.externalNetworksFinished = new HashMap<>();
        this.externalNetworksEvents = new HashMap<>();
        this.agentIsReady = false;
        this.bootFinished = false;
    }
    
    
    /**
     * ************************************************
     *        TIME STEPPING FUNCTIONS
     * ************************************************
     */
    
    @Override
    public void agentFinishedBootStrap() {
        this.bootFinished = true;
        this.postProcessAbstractCommunication(CommunicationEventType.BOOT_FINISHED);
    }

    @Override
    public void agentFinishedActiveState() {
        this.agentIsReady = true;

        for(Map.Entry<Integer,List<Event>> entry : this.extractPendingInterdependentEvents().entrySet()){
            EventMessage eventMessage = new EventMessage(getSimulationAgent().getNetworkIndex(), entry.getValue());
            sendToSpecific(eventMessage, entry.getKey());
        }
            
        FinishedStepMessage message = new FinishedStepMessage(getSimulationAgent().getNetworkIndex(), getSimulationAgent().getSimulationTime(), getSimulationAgent().getIteration(), getCommandReceiver().isConverged());
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

            switch (sfinaMessage.getMessageType()) {
                case EVENT_MESSAGE:
                    EventMessage eventMessage = (EventMessage) sfinaMessage;
                    this.externalNetworksEvents.put(sfinaMessage.getNetworkIdentifier(), eventMessage.getEvents());
                    break;
                case FINISHED_STEP:
                    FinishedStepMessage finishedMessage = ((FinishedStepMessage)sfinaMessage);
                    this.externalNetworksFinished.put(finishedMessage.getNetworkIdentifier(), finishedMessage.isConverged());
                    break;
                default:
                    if(!handleMessage(sfinaMessage))
                        logger.debug("Message Type not recognized");
            }
            postProcessAbstractCommunication(sfinaMessage.getMessageType());
        }
    }
    
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

        handleCommunicationEvent(communicationEventType);

        // Decide what to do 
        switch(readyToProgress()){
            case DO_NEXT_ITERATION:
                doNextIteration();
                break;
            case DO_NEXT_STEP:
                doNextStep();
                break;
            case SKIP_NEXT_ITERATION:
                skipNextIteration();
            case DO_NOTHING:
                break;
            default:
                doNextStep(); 
        }     
    }

    private void doNextStep(){
        clearCommunicationAgent();
        injectEvents();
        getCommandReceiver().progressToNextTimeStep();
    }
    
    private void doNextIteration(){
        clearCommunicationAgent();
        injectEvents();
        getCommandReceiver().progressToNextIteration();
    }
    
    private void skipNextIteration(){
        clearCommunicationAgent();
        getCommandReceiver().skipNextIteration();
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

    protected void sendToAll(AbstractSfinaMessage message){
        for(NetworkAddress address: getAllExternalNetworkAddresses().values())
            getPeer().sendMessage(address, message);
    }
    
    protected void sendToConnected(AbstractSfinaMessage message){
        for(NetworkAddress address: getConnectedExternalNetworkAddresses().values())
            getPeer().sendMessage(address, message);
    }
    
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
    
    protected abstract ProgressType readyToProgress();
    
    protected abstract void handleCommunicationEvent(CommunicationEventType eventType);

    protected abstract Map<Integer, NetworkAddress> getAllExternalNetworkAddresses();
    
    protected abstract Map<Integer, NetworkAddress> getConnectedExternalNetworkAddresses();
    
    protected abstract NetworkAddress getNetworkAddress(int networkIndex);
       
}
