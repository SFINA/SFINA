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

import interdependent.communication.Messages.NetworkAddressChangeMessage;
import interdependent.EventMessage;
import event.Event;
import interdependent.communication.Messages.AbstractSfinaMessage;
import interdependent.communication.Messages.EventMessageNew;
import interdependent.communication.Messages.SfinaMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import protopeer.BasePeerlet;
import protopeer.Peer;
import protopeer.network.Message;
import protopeer.network.NetworkAddress;

/**
 *
 * @author root
 */
public class CommunicationAgent extends BasePeerlet implements CommunicationMediator {

    /*
    idea, communication Agent needs to know to how many other communicationAgents 
    it does need to keep contact to
    */
    
    
    private Map<Integer, NetworkAddress> externalMessageLocations; 
    
    private Map<Integer, MessageReceiver> localMessageLocations;
     
    private List<MessageReceiver> messageReceivers;
        
    private int totalNumberNetworks;
    
    
    
    private NetworkAddress networkAddress;
    
    public CommunicationAgent(){
        this.externalMessageLocations = new HashMap();
        this.messageReceivers = new ArrayList();
        this.totalNumberNetworks = 0;
    }
    
    public CommunicationAgent(int totalNumberNetworks){
        this();
        this.totalNumberNetworks = totalNumberNetworks;
    }
    
    
    
    /**************
    Base Peerlet Functions
    */    
    @Override
    public void handleIncomingMessage(Message message) {
     
        if(message instanceof AbstractSfinaMessage){
            AbstractSfinaMessage sfinaMessage = (AbstractSfinaMessage) message;
            switch(sfinaMessage.getMessageType()){
                case SfinaMessage.EVENT_MESSAGE:
                    MessageReceiver receiver = this.localMessageLocations.get(sfinaMessage.getNetworkIdentifier());
                    if(receiver != null){
                        receiver.injectEvents(((EventMessageNew) sfinaMessage).getEvents());
                    }
                    break;
                case SfinaMessage.NETWORK_ADDRES_CHANGE:
                    //todo
                    break;
                case SfinaMessage.FINISHED_STEP:
                    
                    //todo
                    break;
                default:
                    
            }
        }

//To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void messageSent(Message message) {
        super.messageSent(message); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void networkExceptionHappened(NetworkAddress remoteAddress, Message message, Throwable cause) {
        super.networkExceptionHappened(remoteAddress, message, cause); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handleOutgoingMessage(Message message) {
        super.handleOutgoingMessage(message); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stop() {
        super.stop(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
        NetworkAddress oldAddress = this.networkAddress;
        
        /*
        test if networkaddress can be used in this way, does it implement necessary interfaces?
        */
        if(!oldAddress.equals(getPeer().getNetworkAddress())){
        this.networkAddress = getPeer().getNetworkAddress();
        //todo notify about networkaddress change
        //decide if broadcast or only to those in externalLocations
       
     }
        
    }

    @Override
    public void init(Peer peer) {
        super.init(peer); //To change body of generated methods, choose Tools | Templates.
        this.networkAddress = peer.getNetworkAddress();
        
    }
    
    /*****************
     * Communication Mediator Functions
     */
    
      @Override
    public void registerMessageReceiver(MessageReceiver listener) {
        this.messageReceivers.add(listener);
        NetworkAddressChangeMessage message = new NetworkAddressChangeMessage(listener.getIdentifier(),this.networkAddress, true);
        getPeer().broadcastMessage(message);
    }

    @Override
    public void removeMessageReceiver(MessageReceiver listener) {
        this.messageReceivers.remove(listener);
        NetworkAddressChangeMessage message = new NetworkAddressChangeMessage(listener.getIdentifier(),this.networkAddress,false);
       
    }

    @Override
    public void sendEvent(Event event, int identifier) {
        
       NetworkAddress address = this.externalMessageLocations.get(identifier); 
       EventMessage message = new EventMessage(event);
       
       getPeer().sendMessage(address, message);
       
    }

    
    
    
    
    
    
    
    
    
    
    
    
}
