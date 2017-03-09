/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import backend.FlowDomainAgent;
import event.Event;
import java.util.ArrayList;
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

    /**
     * @return the events
     */
    ArrayList<Event> getEvents();
    
    void removeEvent(Event event);

    /**
     *
     * @return the network
     */
    FlowNetwork getFlowNetwork();
    
     /**
         * Allows TimeSteppingAgent to check if the SimulationAgent doesn't need more iterations.
         * @return 
         */
        boolean isConverged();
        
        
        /*************************************************
         * Methods used by TimeSteppingAgent
         **********************************************/
        
         /**
         * Notifies the Message Receiver, that it can proceed to the next time step.
         * This will happen if and only if the Communication Agent got an EventMessage and an FinishedStepMessage 
         * from all the connected Networks of this receiver and the current time step of all networks has converged.
         */
        public void progressToNextTimeStep();
        
        /**
         * Notifies the Message Receiver, that something changed and that it has 
         * to redo its caluclations.
         * This will happen if and only if the Communication Agent got an EventMessage and an FinishedStepMessage 
         * from all the connected Networks of this receiver.
         */
        public void progressToNextIteration();
        
        /**
         * Notifies the Message Receiver, that nothing has changed, but that it should wait for the other networks, 
         * which are doing another iteration.
         */
        public void skipNextIteration();
    
}
