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

import interdependent.communication.Messages.NetworkAddressMessage;
import interdependent.EventMessage;
import event.Event;
import interdependent.communication.Messages.AbstractSfinaMessage;
import interdependent.communication.Messages.EventMessageNew;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import protopeer.BasePeerlet;
import protopeer.NeighborManager;
import protopeer.Peer;
import protopeer.network.Message;
import protopeer.network.NetworkAddress;
import interdependent.communication.Messages.SfinaMessageInterface;

/**
 *
 * @author root
 */
public class CommunicationAgent extends BasePeerlet implements CommunicationMediator {

    protected final static int POST_ADDRESS_CHANGE = 0;
    protected final static int POST_AGENT_IS_READY = 1;
    
    
    /*
    idea, communication Agent needs to know to how many other communicationAgents 
    it does need to keep contact to
    */
    
    
    
   
    
   // private Map<Integer, MessageReceiver> localMessageLocations;
     
//    private List<MessageReceiver> messageReceivers;
    
    private Map<Integer, NetworkAddress> externalMessageLocations; 
        
    private int totalNumberNetworks;
    
    private CommunicationMediator.MessageReceiver messageReceiver;
    
    private boolean agentIsReady =false;
    
    private NetworkAddress networkAddress;
    
    private Peer peer;
    
//    public CommunicationAgent(){
//        this.externalMessageLocations = new HashMap();
//        this.messageReceivers = new ArrayList();
//        this.totalNumberNetworks = 0;
//    }
    
    public CommunicationAgent(int totalNumberNetworks){
       // this();
       this.externalMessageLocations = new HashMap();      
       this.totalNumberNetworks = totalNumberNetworks;
    }
    
     protected void postProcessCommunication(int typeOfPost){
         // each time something changes this function should be called 
         // handles necessary further steps
         
         //1. after networkAddress change message, do case evaluation and start agent etc.
         switch(typeOfPost){
             case POST_ADDRESS_CHANGE:
                 
                 break;
             case POST_AGENT_IS_READY:
                 break;
             default:
      
         }
        
    }
    
    
    
    
    /**************
    Base Peerlet Functions
    */    
    @Override
    public void handleIncomingMessage(Message message) {
     
        if(message instanceof AbstractSfinaMessage){
            AbstractSfinaMessage sfinaMessage = (AbstractSfinaMessage) message;
            switch(sfinaMessage.getMessageType()){
                case SfinaMessageInterface.EVENT_MESSAGE:
                    if(this.messageReceiver != null){
                        messageReceiver.injectEvents(((EventMessageNew) sfinaMessage).getEvents());
                    }
                    break;    
                case SfinaMessageInterface.NETWORK_ADDRES_CHANGE:
                    NetworkAddressMessage netMessage = (NetworkAddressMessage) sfinaMessage;
                    
                     int id = netMessage.getNetworkIdentifier();
                    NetworkAddress address = netMessage.getAddress();
                    if(netMessage.isStopped())
                        this.externalMessageLocations.remove(netMessage.getNetworkIdentifier());
                    else
                        externalMessageLocations.put(id, address);
                    postProcessCommunication(POST_ADDRESS_CHANGE);
                    break;
                case SfinaMessageInterface.FINISHED_STEP:
                    
                    //todo
                    break;
                default:
                    
            }
        }

//To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void stop() {
        super.stop(); //To change body of generated methods, choose Tools | Templates.
        NetworkAddressMessage message = new NetworkAddressMessage(messageReceiver.getIdentifier(), this.networkAddress, true);
        peer.broadcastMessage(message);
    }

    @Override
    public void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
        NetworkAddress oldAddress = this.networkAddress;
        /*
        test if networkaddress can be used in this way, does it implement necessary interfaces?
        */
        if(oldAddress == null || !oldAddress.equals(getPeer().getNetworkAddress())){

            this.networkAddress = getPeer().getNetworkAddress();
            //todo notify about networkaddress change
            //decide if broadcast or only to those in externalLocations
            NetworkAddressMessage message = new NetworkAddressMessage(this.messageReceiver.getIdentifier(), this.networkAddress);
            peer.broadcastMessage(message);
        }
        
    }
    @Override
    public void init(Peer peer) {
        super.init(peer); //To change body of generated methods, choose Tools | Templates.
        this.peer = peer;
        this.messageReceiver =(MessageReceiver) peer.getPeerletOfType(MessageReceiver.class); 
    }
    
    
    
    
    /*****************
     * Communication Mediator Functions
     */
    
      @Override
    public void registerMessageReceiver(MessageReceiver listener) {
        this.messageReceiver = listener;
//        this.messageReceivers.add(listener);
//        NetworkAddressChangeMessage message = new NetworkAddressChangeMessage(listener.getIdentifier(),this.networkAddress, true);
//        getPeer().broadcastMessage(message);
    }

    @Override
    public void removeMessageReceiver(MessageReceiver listener) {
        this.messageReceiver =null;
      //  NetworkAddressChangeMessage message = new NetworkAddressChangeMessage(listener.getIdentifier(),this.networkAddress,false);
       
    }
    

    @Override
    public void sendEvent(Event event, int identifier) {
        
       NetworkAddress address = this.externalMessageLocations.get(identifier); 
       EventMessage message = new EventMessage(event);
    
       if(address != null)
            this.peer.sendMessage(address, message);
      
    }  
 
    @Override
    public void agentIsRead(boolean ready) {
        this.agentIsReady = ready;
        this.postProcessCommunication(POST_AGENT_IS_READY);
    }
    
   
    
}
