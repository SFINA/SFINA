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
import java.util.List;
import java.util.Map;
import protopeer.network.NetworkAddress;
import protopeer.util.quantities.Time;

/**
 *
 * @author mcb
 */
public interface CommunicationAgentInterface extends CommunicationBetweenMediator {
    
     public final static String parameterColumnSeparator="=";
    
 
     public static String getParameterColumnSeparator() {
        return parameterColumnSeparator;
    }
    
    
    /*
        Receives Messages from the Communication Agent
    */
    public interface MessageReceiver{
    
       /**
        * Injects events to the Receiver
        * @param events
        */
        public void injectEvents(List<Event> events);
        
        /**
         * Notifies the Message Receiver, that it has to initialise (bootstrap) itself.
         * This Method gets called, when CommunicationAgent did its own Bootstrapping
         */
        public void initialise();
              
        /**
         * Notifies the Message Receiver, that it can proceed to the next step
         * This will happen if and only if the Communication Agent got an EventMessage and an FinishedStepMessage 
         * from all the connected Networks of this receiver
         */
        public void progressToNextStep();
     
        /**
         * gets an Identifier from the Receiver, which is globally representative for the network of the receiver
         * @return 
         */
        public int getIdentifier();
        
        /**
         * gets a list of Network Identifier to which the Receiver is connected
         * @return 
         */
        public List<Integer> getConnectedNetwork();
    }
//    /*
//    Handling
//    */
//    public void registerMessageReceiver(MessageReceiver listener);
//    public void removeMessageReceiver(MessageReceiver listener);
    /**
     * 
     * @param event
     * @param identifier 
     */
    public void sendEvent(Event event, int identifier);
    
    public void agentFinishedStep();
    
    /*
    Former Communication
    */
   // public void runBootstraping();
    
    
    
    
    
    
    /**
    Getter and Setter
    * 
    ***/
    /**
     * @return the experimentInputFilesLocation
     */
    public String getExperimentBaseFolderLocation();
    /**
     * @return the time token, i.e. time_x for current time x
     */
    public String getTimeToken();
    
    /**
     * @param timeToken
     */
    public void setTimeToken(String timeToken);
    
    /**
     * @return the time token name, i.e. probably "time_"
     */
    public String getTimeTokenName();
    
    /**
     * @return the peerToken
     */
    public String getPeerToken();

    /**
     * @param aPeerToken the peerToken to set
     */
    public void setPeerToken(String aPeerToken);
    
    /**
     *
     * @return
     */
    public String getMissingValue();
    
    /**
     *
     * @return
     */
    public String getColumnSeparator();
    
    /**
     * @return the linksLocation
     */
    public String getLinksLocation();

    /**
     * @return the linksFlowLocation
     */
    public String getLinksFlowLocation();
    
    
     public String getExperimentID();

 
    public Time getBootstrapTime();
       
    public void setBootstrapTime(Time bootstrapTime);
    
    
    public int getSimulationTime();
    
     public String getExperimentInputFilesLocation();



    public String getExperimentOutputFilesLocation();



    public String getEventsInputLocation();



    public String getEventsOutputLocation();



    public String getPeerTokenName();



    public Map<Integer, NetworkAddress> getExternalMessageLocations();

    
    
    public String getNodesLocation();

   

    public String getNodesFlowLocation();
    
    public Time getRunTime();
    
    public String getSfinaParamLocation();

    public String getBackendParamLocation();
    
   
    
        
       
}
