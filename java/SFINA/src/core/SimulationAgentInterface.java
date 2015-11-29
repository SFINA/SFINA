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
    
    /**
     * Wrapper to handle cascades. This method takes care of the iterations and calls flowConvergenceAlgo().
     */
    public void runCascade();
    
    /**
     * Wrapper to handle convergence. Strategy to make flow analysis converge can be implemented here
     * @param flowNetwork
     * @return true if flow analysis finally converged, else false
     */
    public boolean flowConvergenceAlgo(FlowNetwork flowNetwork);

    /**
     * Wrapper to mitigate overload. Strategy to respond to (possible) overloading can be implemented here. This method is called before the OverLoadAlgo is called which deactivates affected links/nodes.
     * @param flowNetwork 
     */
    public void mitigateOverload(FlowNetwork flowNetwork);
    
    public boolean linkOverloadAlgo(FlowNetwork flowNetwork);
    
    public boolean nodeOverloadAlgo(FlowNetwork flowNetwork);
    
    public boolean runFlowAnalysis(FlowNetwork flowNetwork);

    public void scheduleMeasurements();
    
}
