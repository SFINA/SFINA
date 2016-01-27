/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import event.Event;
import network.FlowNetwork;
import protopeer.network.Message;
import protopeer.util.quantities.Time;


/**
 *
 * @author evangelospournaras
 */
public interface SimulationAgentInterface {
    
    public void runBootstraping();
    
    public void runActiveState();
    
    public void runPassiveState(Message message);
    
    public void executeEvent(FlowNetwork flowNetwork, Event event);
    
    public void performInitialMeasurements();
    
    public void performFinalMeasurements();
    
    public void initMeasurements();
    
    public void runFlowAnalysis();
    
    public boolean flowConvergenceStrategy(FlowNetwork flowNetwork);

    public void mitigateOverload(FlowNetwork flowNetwork);
    
    public boolean linkOverload(FlowNetwork flowNetwork);
    
    public boolean nodeOverload(FlowNetwork flowNetwork);
    
    public boolean callBackend(FlowNetwork flowNetwork);

    public void scheduleMeasurements();
    
}
