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

import backend.FlowDomainAgent;
import event.Event;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import network.FlowNetwork;
import protopeer.network.Message;

/**
 *
 * @author mcb
 */
public interface SimulationAgentCommunicationInterface {
    void runBootstraping();
    
    void runActiveState();
    
    int getSimulationTime();
    
    HashMap<Enum,Object> getDomainParameters();
    
    void saveOutputData();
    
    int getIteration();
    
    void queueEvent(Event event);
    
    void queueEvents(List<Event> events);
    
    void runPassiveState(Message message);
    
    void executeEvent(FlowNetwork flowNetwork, Event event);
    
    void executeAllEvents();
    
    void runInitialOperations();
    
    void runFinalOperations();
        
    void runFlowAnalysis();
        
    FlowDomainAgent getFlowDomainAgent();

    void scheduleMeasurements();
    
    void loadFileSystem(String schema);
    
    void loadExperimentConfigFiles(String sfinaParamLocation, String backendParamLocation, String eventsLocation);                

      
                     
       /**
        * @return the networkIndex
        */
        public int getNetworkIndex();
        
        /**
        * Return all connected networks.
        * A network is connected, if an interdependent link points to or from that
        * network and is activated and connected.
        *
        * @return a list of network indices of all connected networks
        */
        public Collection<Integer> getConnectedNetworkIndices();
     
}
