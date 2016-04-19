/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import event.Event;
import java.util.HashMap;
import network.FlowNetwork;
import protopeer.network.Message;


/**
 *
 * @author evangelospournaras
 */
public interface SimulationAgentInterface {
    
    public void runBootstraping();
    
    public void runActiveState();
    
    public int getSimulationTime();
    
    public HashMap<Enum,Object> getBackendParameters();
    
    public void saveOutputData();
    
    public int getIteration();
    
    public void queueEvent(Event event);
    
    public void setFlowParameters();
    
    public void runPassiveState(Message message);
    
    public void executeEvent(FlowNetwork flowNetwork, Event event);
    
    public void executeAllEvents();
    
    public void runInitialOperations();
    
    public void runFinalOperations();
        
    public void runFlowAnalysis();
        
    public boolean callBackend(FlowNetwork flowNetwork);

    public void scheduleMeasurements();
    
    public void loadFileSystem(String schema);
    
    public void loadExperimentConfigFiles(String sfinaParamLocation, String backendParamLocation, String eventsLocation);                

    
}
