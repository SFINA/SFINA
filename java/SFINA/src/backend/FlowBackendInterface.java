/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend;

import java.util.HashMap;
import network.FlowNetwork;

/**
 *
 * @author evangelospournaras
 */
public interface FlowBackendInterface {
    
    public boolean flowAnalysis(FlowNetwork net);
    
    public boolean flowAnalysis(FlowNetwork net, HashMap<Enum,Object> backendParameters);
    
    public void setBackendParameters(HashMap<Enum,Object> backendParameters);
    
}
