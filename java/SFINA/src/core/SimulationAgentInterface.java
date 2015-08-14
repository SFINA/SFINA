/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import event.Event;
import network.FlowNetwork;


/**
 *
 * @author evangelospournaras
 */
public interface SimulationAgentInterface {
    
    public void executeEvent(FlowNetwork flowNetwork, Event event);
    public void runFlowAnalysis();
    
}
