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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    protected static final Logger logger = Logger.getLogger(CommunicationAgent.class);
//    protected List<Integer> externalNetworksFinishedStep;
//    protected List<Integer> externalNetworksSendEvent;
//    protected List<Event> eventsToQueue;
    
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
//        this.externalNetworksFinishedStep = new ArrayList<>();
//        this.externalNetworksSendEvent = new ArrayList<>();
//        this.eventsToQueue = new ArrayList<>();
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
        
        // Mark: maybe we merge event and Finished Step message? - TBD 
        // Ben: Argument against: One has to be send to all, the other only to connected networks
        // Mark: what is with the case in the interdependent case, that one finished its step, sends finished, but also sends an event
        // to another note? The note which only gets the finished step message could/ would start progressing in some cases
        // but maybe an event is triggered through the events obtained, which also has to be send to the note which already started progressing?
        // Ben: But if there are also events sent, then the isConverged field of the finished step message is false and therefore takes care of
        // preventing the progression.
        FinishedStepMessage message = new FinishedStepMessage(getSimulationAgent().getNetworkIndex(), getSimulationAgent().getSimulationTime(), getSimulationAgent().getIteration(), !getCommandReceiver().pendingEventsInQueue());
        sendToAll(message);
        
        //TBD Mark: where is it guaranteed that only those messages are send, which belong to that network? Should
        // we do it here or on receive?
        // Ben: What do you mean by that?
        // Mark: A network could have three Interdependent Events: Event A goes to network 2, Event B to network 6, and Event C to network seven
        // currently we are sending all three events to all three networks. But network 2 only needs event A and only need to inject this one
        // Ben: Oh damn, you're right, this could cause trouble. Also, this means that the indices of the interdependent events have to be unique
        // accross all the networks. Didn't think of that yet...
        EventMessage eventMessage = new EventMessage(getSimulationAgent().getNetworkIndex(), this.extractPendingInterdependentEvents());
        sendToConnected(eventMessage);

        // post process the communication
        // The AGENT_IS_READDY is never used in processing -> can we remove it or make it more useful?
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
                    // Queue the received Messages
//                    this.eventsToQueue.addAll(((EventMessage) sfinaMessage).getEvents());
                    this.externalNetworksEvents.put(sfinaMessage.getNetworkIdentifier(), ((EventMessage) sfinaMessage).getEvents());
                    // add the Networkid of the sender to ourl ist
//                    if(!externalNetworksSendEvent.contains(sfinaMessage.getNetworkIdentifier()))
//                        this.externalNetworksSendEvent.add(sfinaMessage.getNetworkIdentifier());
//                    else{
//                        // Mark: needs to be discussed, I think it could happen, if another network was first finished and then triggered by an event
//                        // That's why I put the logging, so we know when it happens :)
                    // Mark: jep, but this can happen regularly, or am I wrong?
//                        logger.debug("Attention: Event message already received from this network, shoudn't happen.");
//                    }
                    break;
                case FINISHED_STEP:
                    FinishedStepMessage finishedMessage = ((FinishedStepMessage)sfinaMessage);
                    // Logic: only if converged is network added to the finished Networks - externalNetworksFinishedStep hence only contain networks which are converged
                    // this simplifies convergence logic. TBD: Is it too much simplified, do we miss some complexity?
                    
                    // Ben: I think this wouldn't work, because convergence is not a hard criterion. A network can be finished
                    // with an iteration but still has events waiting to be executed in the next iteration. In this case, the 
                    // network should be added to the list, but it hasn't converged.
                    
//                    if(finishedMessage.isConverged()){
//                        if(!externalNetworksFinishedStep.contains(finishedMessage.getNetworkIdentifier())){
//                            this.externalNetworksFinishedStep.add(finishedMessage.getNetworkIdentifier()); 
//                        }
//                    }else{
//                        // Logic: if through send events another network is no longer converged, it will be removed
//                        // from the finished Networks
//                        // TBD: on EventMessage Receive, if Simulation Agent gets new Events for the current finished iteration, then it is no longer converged
//                        // Hence a finishedStepMessage with non convergence has to be send, resp. a new message type?
//                        // We need to elaborate how this whole logic works. Which logic has to be delegated to to concrete CommunicationAgent Instantiation?
//                        if(externalNetworksFinishedStep.contains(finishedMessage.getNetworkIdentifier())){
//                            int index = this.externalNetworksFinishedStep.indexOf(finishedMessage.getNetworkIdentifier());
//                            this.externalNetworksFinishedStep.remove(index);
//                        }
//                    }
                    this.externalNetworksFinished.put(finishedMessage.getNetworkIdentifier(), finishedMessage.isConverged());
                    break;
                default:
                    if(!handleMessage(sfinaMessage))
                        logger.debug("Message Type not recognized");
            }
            postProcessAbstractCommunication(sfinaMessage.getMessageType());
        }

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
        
        // Check if Child handles postProcess, else do default Behavior
//        if(!handleCommunicationEvent(communicationEventType)){
//            -> moved to handleCommunicationEvent(...)  
//                
//        }
        
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
                doNextStep(); // Moved the check into readyToProgress()
//                if(this.agentIsReady){
//                    doNextIteration();
//                }else{
//                    doNextStep();
//                }   
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
    
    private void injectEvents(){
        for(List<Event> events : this.externalNetworksEvents.values())
            getSimulationAgent().queueEvents(events);
        checkEventsForConflicts();
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
     * ************************************************************
     *          METHODS WITH DEFAULT BEHAVIOR - TO BE OVERWRITTEN
     * ************************************************************
     */ 
    
    protected abstract ProgressType readyToProgress();
    
    protected boolean handleMessage(AbstractSfinaMessage message){
        return false;
    }
    
    protected void handleCommunicationEvent(CommunicationEventType eventType){
        switch(eventType){
//                case EVENT_MESSAGE:
//                    // Mark message should always be queued or not, as default behavior?
//                    if(this.eventsToQueue.size()>0){
//                        injectEvents();
//                        if(this.agentIsReady && getCommandReceiver().pendingEventsInQueue()){
//                            // this has to be done, as we already signaled to all agents, that we are converged
//                            // does this have to happen here or in the concrete instantiation? 
//                            // Concrete instantiation could just return true in handleCommunicationEvent, but does this make sense?
//                            // What should be the default behavior?
//                            FinishedStepMessage finishedStepMessage = new FinishedStepMessage(getSimulationAgent().getNetworkIndex(), 
//                                    getSimulationAgent().getSimulationTime(), getSimulationAgent().getIteration(), false);
//                            sendToAll(finishedStepMessage);
//                        }
//                        this.eventsToQueue.clear();
//                    }
//                    break;
                // currently not used, as in SimulationAgent the bootstrap method also calls finishedActiveState
//                case BOOT_FINISHED: 
//                    doNextStep();
//                case BOOT_FINISHED_MESSAGE:
//                  // should this really be the default behavior? I think it would be better to 
//                  // do nothing here and lete child decide, as this is specific? or
//                  doNextStep();
//                    break;
                default: 
                    break;
            }
    }
    
   
    
    
    /**
     * ************************************************
     *          ABSTRACT METHODS
     * ************************************************
     */
    protected abstract Map<Integer, NetworkAddress> getAllExternalNetworkAddresses();
    
    // DANGEROUS
    protected abstract Map<Integer, NetworkAddress> getConnectedExternalNetworkAddresses();
    
    protected abstract NetworkAddress getNetworkAddress(int networkIndex);
       
}
