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

import event.Event;

import interdependent.communication.Messages.AbstractSfinaMessage;
import interdependent.communication.Messages.EventMessageNew;
import interdependent.communication.Messages.FinishedStepMessage;
import interdependent.communication.Messages.NetworkAddressMessage;
import interdependent.communication.Messages.SfinaMessageInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import protopeer.BasePeerlet;
import protopeer.Peer;
import protopeer.network.Message;
import protopeer.network.NetworkAddress;


/**
 *
 * @author mcb
 */
public class CommunicationAgent extends SimpleTimeSteppingAgent implements CommunicationAgentInterface {

    protected final static int POST_ADDRESS_CHANGE = 0;
    protected final static int POST_AGENT_IS_READY = 1;
    protected final static int POST_FINISHED_STEP = 2;
    protected final static int POST_EVENT_SEND = 3;
    
    private static final Logger logger = Logger.getLogger(CommunicationAgent.class);
    private Map<Integer, NetworkAddress> externalMessageLocations; 
    private List<Integer> externalNetworksFinishedStep;    
    private List<Integer> externalNetworksSendEvent;
    private int totalNumberNetworks;
    private CommunicationAgentInterface.MessageReceiver messageReceiver;
    private boolean bootstrapingFinished = false;
    private boolean agentIsReady =false;
    private NetworkAddress networkAddress;
    private Peer peer; 

    
    /**************************************************
     *             CONSTRUCTORS
     **************************************************/
    public CommunicationAgent(int totalNumberNetworks){
        //previously SimulationAgent
//        this.experimentID=experimentID;
//        this.bootstrapTime=bootstrapTime;
//        this.runTime=runTime;
       
        // this();
        this.externalMessageLocations = new HashMap();      
        this.totalNumberNetworks = totalNumberNetworks;
        this.externalNetworksFinishedStep = new ArrayList<>();
        this.externalNetworksSendEvent = new ArrayList<>();
    }
    
    /*****************************************
            Base Peerlet Functions
    *******************************************/    
  
    @Override
    public void stop() {
        super.stop(); //To change body of generated methods, choose Tools | Templates.
        NetworkAddressMessage message = new NetworkAddressMessage(messageReceiver.getNetworkIdentifier(), this.networkAddress, true);
       // peer.broadcastMessage(message);
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
        }
        // notify all as during stop also everyon got notified
        //todo notify about networkaddress change
        //decide if broadcast or only to those in externalLocations
        NetworkAddressMessage message = new NetworkAddressMessage(this.messageReceiver.getNetworkIdentifier(), this.networkAddress);
      //  peer.broadcastMessage(message);
                
    }
    @Override
    public void init(Peer peer) {
        super.init(peer); //To change body of generated methods, choose Tools | Templates.
        this.peer = peer;
        this.messageReceiver =(MessageReceiver) peer.getPeerletOfType(MessageReceiver.class); 
    }
        
    @Override
    public void handleIncomingMessage(Message message) {
     
        //check if its a SFINA Message
        if(message instanceof AbstractSfinaMessage){
            
            AbstractSfinaMessage sfinaMessage = (AbstractSfinaMessage) message;
            
            switch(sfinaMessage.getMessageType()){
                case SfinaMessageInterface.EVENT_MESSAGE:
                    if(this.messageReceiver != null){
                        //TBD: Maybe Collect, or runtime etc.? has to be discussed
                        messageReceiver.injectEvents(((EventMessageNew) sfinaMessage).getEvents());
                    }
                    this.externalNetworksSendEvent.add(sfinaMessage.getNetworkIdentifier());
                    postProcessCommunication(POST_EVENT_SEND);
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
                    this.externalNetworksFinishedStep.add(sfinaMessage.getNetworkIdentifier());
                    postProcessCommunication(POST_FINISHED_STEP);
                    break;
                default:
                    
            }
        }

    }
    
    /******** COMMUNICATION HELPER **********************************/
    
     protected void postProcessCommunication(int typeOfPost){
         // each time something changes this function should be called 
         // handles necessary further steps
         
         //1. after networkAddress change message, do case evaluation and start agent etc.
         switch(typeOfPost){
             case POST_ADDRESS_CHANGE:
                 break;
             case POST_AGENT_IS_READY:
                 checkAndNextStep();
                 break;
             case POST_EVENT_SEND:
                 checkAndNextStep();
                 break;
             case POST_FINISHED_STEP:
                 checkAndNextStep();
                 break;
             default:
      
         }
        
    }
    
     public void checkAndNextStep(){
         if(readyToProgress()){
             
             this.externalNetworksFinishedStep.clear();
             this.externalNetworksSendEvent.clear();
             this.agentIsReady = false;
             
             getCommandReceiver().progressToNextTimeStep();
        }
         
     }
     
     public boolean readyToProgress(){
         // Collection<Integer> identifiers = getMessageReceiver().getConnectedNetwork();
          //TBD: progress when all Finished step or when connected?
        return this.externalNetworksFinishedStep.size()==(this.totalNumberNetworks-1) && 
                this.externalNetworksSendEvent.size() == (this.totalNumberNetworks-1) &&
                         this.agentIsReady;
     }
    
     
    /**************************************************
     *          Communication Agent Functions
     *************************************************/
    
    @Override
    public void sendEvent(Event event, int identifier) {
        
       /*
        Should be collected? Or what should happen?
        */ 
       NetworkAddress address = this.externalMessageLocations.get(identifier); 
       EventMessageNew message = new EventMessageNew(getMessageReceiver().getNetworkIdentifier(),event);
      
       if(address != null)
            this.peer.sendMessage(address, message);
      
    }  
 
    @Override
    public void agentFinishedStep() {
        // only executed once, so that bootstrapping can immediately 
        if(!this.bootstrapingFinished){
            this.bootstrapingFinished = true;
            this.agentIsReady = false;
            getCommandReceiver().progressToNextTimeStep();
        }else{
            // mark that agent is ready
            this.agentIsReady = true;
            
            // inform other Communication Agents
             FinishedStepMessage message = new FinishedStepMessage(getMessageReceiver().getNetworkIdentifier());
             Collection<Integer> identifiers = getMessageReceiver().getConnectedNetwork();
             for(int i: identifiers){
                 NetworkAddress address = this.externalMessageLocations.get(i);
                 this.peer.sendMessage(address, message); 
            }
             
             // TODO: here new EVENTS should be send
             
            // post process the communication
            this.postProcessCommunication(POST_AGENT_IS_READY);
        }
    }
    public MessageReceiver getMessageReceiver(){
        if(messageReceiver == null){
            messageReceiver=(MessageReceiver) getPeer().getPeerletOfType(MessageReceiver.class);
        }
        return messageReceiver;
    }
    
    
    

   
}
