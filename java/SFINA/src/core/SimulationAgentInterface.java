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
    
    public void runBootstraping(Time bootstrapTime);
    
    public void runActiveState(Time runTime);
    
    public void runEventExecution(int time, Event event);
    
    public void runPassiveState(Message message);
    
    public void executeEvent(FlowNetwork flowNetwork, Event event);
    
    public void runFlowAnalysis();
    
    public void scheduleMeasurements();
    
}
