/*
 * Copyright (C) 2016
 * SFINA Team
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
public class CommunicationAgent extends SimpleTimeSteppingAgent {

    protected final static int POST_ADDRESS_CHANGE_RECEIVED = 0;
    protected final static int POST_AGENT_IS_READY = 1;
    protected final static int POST_FINISHED_STEP_RECEIVED = 2;
    protected final static int POST_EVENT_RECEIVED = 3;

    private static final Logger logger = Logger.getLogger(CommunicationAgent.class);
    private Map<Integer, NetworkAddress> externalNetworkAddresses;
    private List<Integer> externalNetworksFinishedStep;
    private boolean externalNetworksConverged;
    private List<Integer> externalNetworksSendEvent;
    private List<Event> eventsToQueue;
    private int totalNumberNetworks;
    private boolean agentIsReady; 

    private Timer initialDelayTimer;

    /**
     * 
     * @param totalNumberNetworks 
     */
    public CommunicationAgent(int totalNumberNetworks) {
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
     * ***************************************
     *         Base Peerlet Functions
     * ***************************************
     */
    
    @Override
    public void stop() {
        super.stop(); //To change body of generated methods, choose Tools | Templates.
//        NetworkAddressMessage message = new NetworkAddressMessage(simulationAgent.getNetworkIndex(), this.networkAddress, true);
//        peer.broadcastMessage(message);
    }

    @Override
    public void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
        
       
        //first not needed as addresses are hard coded
//        NetworkAddress oldAddress = this.networkAddress;
//        /*
//        test if networkaddress can be used in this way, does it implement necessary interfaces?
//         */
//        if (oldAddress == null || !oldAddress.equals(getPeer().getNetworkAddress())) {
//            this.networkAddress = getPeer().getNetworkAddress();
//        }
//        // notify all as during stop also everyon got notified
//        //todo notify about networkaddress change
//        //decide if broadcast or only to those in externalLocations
//        NetworkAddressMessage message = new NetworkAddressMessage(this.simulationAgent.getNetworkIndex(), this.networkAddress);
//
//        sendMessageToNeighbors(message);
        //this.peer.broadcastMessage(message); does not work, as it is not implemented by protopeer

    }

    @Override
    public void init(Peer peer) {
        super.init(peer); //To change body of generated methods, choose Tools | Templates.

     
    }

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
                    postProcessCommunication(POST_EVENT_RECEIVED);
                    break;
                case NETWORK_ADDRESS_CHANGE:
                    NetworkAddressMessage netMessage = (NetworkAddressMessage) sfinaMessage;

                    int id = netMessage.getNetworkIdentifier();
                    NetworkAddress address = netMessage.getAddress();

                    // Network Messages have to be propergated, as no broadcasting function exists
                    if (netMessage.isStopped()) {
                        this.externalNetworkAddresses.remove(netMessage.getNetworkIdentifier());    
                    }else {
                        externalNetworkAddresses.put(id, address);
                    }
                    postProcessCommunication(POST_ADDRESS_CHANGE_RECEIVED);

                    break;
                case FINISHED_STEP:
                    if(!externalNetworksFinishedStep.contains(sfinaMessage.getNetworkIdentifier())){
                        this.externalNetworksFinishedStep.add(sfinaMessage.getNetworkIdentifier());
                        if(!((FinishedStepMessage)sfinaMessage).isConverged())
                            this.externalNetworksConverged = false;
                    }
                    else
                        logger.debug("Attention: Finished Step message already received from this network, shoudn't happen.");
                    postProcessCommunication(POST_FINISHED_STEP_RECEIVED);
                    break;
                default:
                    logger.debug("Message Type not recognized");

            }
        }

    }
    
    /**
     * ***************************************
     *         COMMUNICATION HELPER 
     * ***************************************
     */

    protected void postProcessCommunication(int typeOfPost) {
        // each time something changes this function should be called 
        // handles necessary further steps

        switch (typeOfPost) {
            case POST_ADDRESS_CHANGE_RECEIVED:
                break;
            case POST_AGENT_IS_READY:
                checkAndNextStep();
                break;
            case POST_EVENT_RECEIVED:
                checkAndNextStep();
                break;
            case POST_FINISHED_STEP_RECEIVED:
                checkAndNextStep();
                break;
            default:

        }

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
            if(this.getSimulationAgent().getIteration() == 0 ) // The case after bootstraping, maybe there's a better way to always ensure that after bootsraping it doesn't stop? E.g. make a different method agentFinishedBootstraping()
               getCommandReceiver().progressToNextTimeStep();
            else if(this.pendingEventsInQueue()) // if this network has more events waiting for this time step, continue iterations
                getCommandReceiver().progressToNextIteration();
            else if(this.externalNetworksConverged) // if this network doesn't have more events waiting and the other networks have also finished, continue to next time step
                getCommandReceiver().progressToNextTimeStep();
            else{ // if the above two don't hold, wait
                this.externalNetworksConverged = true;
                logger.debug("No pending events, but other networks still iterating -> Waiting");
            }
        }

    }

    public boolean readyToProgress() {
        //TBD: is this condition enough: it does not check for which keys exactly are inside 
        // -> Ben: now yes, added check when receiving messages
        // PROBLEM when interdependent link in input files to another network is defined, which is not loaded. The case, if 3 networks are prepared, but N = 2;
        // Should ideally be checked somewhere
        return this.externalNetworksFinishedStep.size() == (this.totalNumberNetworks - 1)
                && (this.externalNetworksSendEvent.size() == getSimulationAgent().getConnectedNetworkIndices().size()) 
                && this.agentIsReady;
    }

    /**
     * ************************************************
     *          Communication Agent Functions
     * ************************************************
     */

    @Override
    public void agentFinishedActiveState() {        
        this.agentIsReady = true;
        
        // inform other Communication Agents
        FinishedStepMessage message = new FinishedStepMessage(getSimulationAgent().getNetworkIndex(), getSimulationAgent().getSimulationTime(), getSimulationAgent().getIteration(), this.pendingEventsInQueue());
        sendToAll(message);

        EventMessage eventMessage = new EventMessage(getSimulationAgent().getNetworkIndex(), this.extractPendingInterdependentEvents());
        sendToConnected(eventMessage);

        // post process the communication
        this.postProcessCommunication(POST_AGENT_IS_READY);
    }

    private void sendToAll(AbstractSfinaMessage message){
        for(NetworkAddress address: getAllExternalNetworkAddresses().values())
            getPeer().sendMessage(address, message);
    }
    
    private void sendToConnected(AbstractSfinaMessage message){
        for(NetworkAddress address: getConnectedExternalNetworkAddresses().values())
            getPeer().sendMessage(address, message);
    }
    
    private Map<Integer, NetworkAddress> getAllExternalNetworkAddresses(){
       HashMap<Integer,NetworkAddress> allAddresses = new HashMap<>();
       
       for(int i=0; i<this.totalNumberNetworks; i++){
           if(getPeer().getIndexNumber() != i)
               allAddresses.put(i, new IntegerNetworkAddress(i));
       }
       return allAddresses;
    }
    
    private Map<Integer, NetworkAddress> getConnectedExternalNetworkAddresses(){
        
        Collection<Integer> indices = getSimulationAgent().getConnectedNetworkIndices();
        Map<Integer, NetworkAddress> allAddresses = getAllExternalNetworkAddresses();
        
        Map<Integer, NetworkAddress> connectedAddresses = new HashMap<>();
        
        for(int i : indices)
            connectedAddresses.put(i, allAddresses.get(i));
        
        return connectedAddresses;
    }
    
    /**
     * ************************************************
     *          EVENT METHODS
     * ************************************************
     */
    
    private List<Event> extractPendingInterdependentEvents(){
        List<Event> interdependentEvents = new ArrayList<>();
        for(Event event : this.getSimulationAgent().getEvents())
            if(event.getTime() == this.getSimulationAgent().getSimulationTime() && event.getNetworkComponent().equals(NetworkComponent.INTERDEPENDENT_LINK))
                interdependentEvents.add(event);
        return interdependentEvents;
    }
    
    // TBD: Can be here or in SimulationAgent, what makes more sense?
    // Ben: I think better here, to be easily changeable without touching SimulationAgent
    private void checkEventsForConflicts() {
        logger.debug("Conflict check of events not implemented yet.");
    }

    
//
//    private NeighborManager getNeighborManager() {
////        if(this.neighborManager == null){
////            this.neighborManager = (NeighborManager) getPeer().getPeerletOfType(NeighborManager.class);
////        }
////        return this.neighborManager;
//
//        return (NeighborManager) getPeer().getPeerletOfType(NeighborManager.class);
//    }

//    private void sendMessageToNeighbors(AbstractSfinaMessage message) {
//
//        for (Finger neighbor : getNeighborManager().getNeighbors()) {
//            getPeer().sendMessage(neighbor.getNetworkAddress(), message);
//        }
//
//    }

//    private boolean netAddressIsInMap(NetworkAddressMessage netmessage) {
//
//        int id = netmessage.getNetworkIdentifier();
//        NetworkAddress address = netmessage.getAddress();
//
//        if (this.externalNetworkAddresses.containsKey(id) && (this.externalNetworkAddresses.get(id).equals(address))) {
//            return true;
//        } else {
//            return false;
//        }
//
//    }

//    private void sendNetworkLocation() {
//        NetworkAddress oldAddress = this.networkAddress;
//        /*
//        test if networkaddress can be used in this way, does it implement necessary interfaces?
//         */
//        if (oldAddress == null || !oldAddress.equals(getPeer().getNetworkAddress())) {
//            this.networkAddress = getPeer().getNetworkAddress();
//        }
//        // notify all as during stop also everyon got notified
//        //todo notify about networkaddress change
//        //decide if broadcast or only to those in externalLocations
//        NetworkAddressMessage message = new NetworkAddressMessage(this.simulationAgent.getNetworkIndex(), this.networkAddress);
//
//    //    sendMessageToNeighbors(message);
//    }
    
    
}
