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
import static java.lang.Integer.min;
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
    protected List<Integer> externalNetworksFinishedStep;
    protected List<Integer> externalNetworksSendEvent;
    protected List<Event> eventsToQueue;
    protected int totalNumberNetworks;
    protected boolean agentIsReady; 

    
    
    
    /**
     * 
     * @param totalNumberNetworks 
     */
    public AbstractCommunicationAgent(int totalNumberNetworks) {
        super();
        this.totalNumberNetworks = totalNumberNetworks;
        this.externalNetworksFinishedStep = new ArrayList<>();
        this.externalNetworksSendEvent = new ArrayList<>();
        this.eventsToQueue = new ArrayList<>();
        this.agentIsReady = false;
  
    }
    
    
    
    
    
    /**
     * ************************************************
     *        TIME STEPPING FUNCTIONS
     * ************************************************
     */
    
    @Override
    public void agentFinishedBootStrap() {
              
        this.postProcessAbstractCommunication(SfinaMessageType.BOOT_FINISHED_MESSAGE);
    }

    @Override
    public void agentFinishedActiveState() {
        this.agentIsReady = true;
        
        
        // Mark: maybe we merge event and Finished Step message? - TBD 
        FinishedStepMessage message = new FinishedStepMessage(getSimulationAgent().getNetworkIndex(), getSimulationAgent().getSimulationTime(), getSimulationAgent().getIteration(), getCommandReceiver().pendingEventsInQueue());
        sendToAll(message);
        
        //TBD Mark: where is it guaranteed that only those messages are send, which belong to that network? Should
        // we do it here or on receive?
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
                 
                    this.eventsToQueue.addAll(((EventMessage) sfinaMessage).getEvents());
                    if(!externalNetworksSendEvent.contains(sfinaMessage.getNetworkIdentifier()))
                        this.externalNetworksSendEvent.add(sfinaMessage.getNetworkIdentifier());
                    else{
                        // Mark: needs to be discussed, I think it could happen, if another network was first finished and then triggered by an event
                        logger.debug("Attention: Event message already received from this network, shoudn't happen.");
                    }
                    break;
                case FINISHED_STEP:
                    FinishedStepMessage finishedMessage = ((FinishedStepMessage)sfinaMessage);
                    // Logic: only if converged is network added to the finished Networks - externalNetworksFinishedStep hence only contain networks which are converged
                    if(finishedMessage.isConverged()){
                        if(!externalNetworksFinishedStep.contains(finishedMessage.getNetworkIdentifier())){
                            this.externalNetworksFinishedStep.add(finishedMessage.getNetworkIdentifier()); 
                        }
                    }else{
                        // Logic: if through send events another network is no longer converged, it will be removed
                        // from the finished Networks
                        if(externalNetworksFinishedStep.contains(finishedMessage.getNetworkIdentifier())){
                            int index = this.externalNetworksFinishedStep.indexOf(finishedMessage.getNetworkIdentifier());
                            this.externalNetworksFinishedStep.remove(index);
                        }
                    }
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
     * @param typeOfPost 
     */
    private void postProcessAbstractCommunication(SfinaMessageType typeOfPost) {
   
        
        // Check if Child handles postProcess, else do default Behavior
        if(!handlePostProcess(typeOfPost)){
            switch(typeOfPost){
                case EVENT_MESSAGE:
                    // Mark message should always be queued or not, as default behavior?
                    if(this.eventsToQueue.size()>0){
                        getSimulationAgent().queueEvents(this.eventsToQueue);
                        if(this.agentIsReady && getCommandReceiver().pendingEventsInQueue()){
                            // this has to be done, as we already signaled to all agents, that we are converged
                            FinishedStepMessage finishedStepMessage = new FinishedStepMessage(getSimulationAgent().getNetworkIndex(), 
                                    getSimulationAgent().getSimulationTime(), getSimulationAgent().getIteration(), false);
                            sendToAll(finishedStepMessage);
                        }
                        this.eventsToQueue.clear();
                    }
                    break;
                case BOOT_FINISHED_MESSAGE:
                    doNextStep();
                    break;
                default: 
            }      
                
        }
        
        // Decide what to do 
        switch(readyToProgress()){
            case DO_ITERATION:
                doNextIteration();
                break;
            case DO_NEXT_STEP:
                doNextStep();
                break;
            case DO_NOTHING:
                
                break;
            default:
                if(this.agentIsReady){
                    doNextIteration();
                }else{
                    doNextStep();
                }   
        }
        
        

    }
    
    private void doNextStep(){
        this.externalNetworksFinishedStep.clear();
        this.externalNetworksSendEvent.clear();
        this.agentIsReady = false;
        getCommandReceiver().progressToNextTimeStep();
    }
    
    private void doNextIteration(){
        this.agentIsReady = false;
        // Mark: should here also be finished step cleaned etc.?
        getCommandReceiver().progressToNextIteration();
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
  
    
    public enum ProgressType {
    DO_NOTHING,
    DO_ITERATION,
    DO_NEXT_STEP,
    DO_DEFAULT
    }
    protected ProgressType readyToProgress(){
        return ProgressType.DO_DEFAULT;
    }
    
    protected boolean handleMessage(AbstractSfinaMessage message){
        return false;
    }
    
    protected boolean handlePostProcess(SfinaMessageType messageType){
        return false;
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
