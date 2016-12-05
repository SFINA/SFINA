/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import backend.FlowDomainAgent;
import event.Event;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import network.FlowNetwork;
import protopeer.network.Message;


/**
 *
 * @author evangelospournaras
 */
public interface SimulationAgentInterface {
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
     * Return all connected networks.
     * A network is connected, if an interdependent link points to or from that
     * network and is activated and connected.
     *
     * @return a list of network indices of all connected networks
     */
    Collection<Integer> getConnectedNetworkIndices();

    /**
     * @return the networkIndex
     */
    int getNetworkIndex();

    /**
     * @return the columnSeparator
     */
    String getParameterColumnSeparator();

    
}
