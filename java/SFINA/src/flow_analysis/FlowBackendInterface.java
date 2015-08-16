/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flow_analysis;

import java.util.List;
import network.FlowNetwork;
import network.Link;
import network.Node;

/**
 *
 * @author evangelospournaras
 */
public interface FlowBackendInterface {
    
    public void flowAnalysis(FlowNetwork net);
    
}
