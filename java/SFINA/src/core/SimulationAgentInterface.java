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
    
    public void performMeasurements();
    
    public void initMeasurements();
    
    public void runAnalysis(FlowNetwork flowNetwork);

    public boolean handleConvergence(FlowNetwork flowNetwork);

    public boolean handleOverload(FlowNetwork flowNetwork);
    
    public boolean runFlowAnalysis(FlowNetwork flowNetwork);

    public void scheduleMeasurements();
    
}
