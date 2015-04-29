/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flow_analysis;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author evangelospournaras
 */
public class FlowAnalysisOutcome {
    
    public Map<String,Double> nodesFlow;
    public Map<String,Double> linksFlow;
    
    public FlowAnalysisOutcome(){
        this.nodesFlow=new HashMap();
        this.linksFlow=new HashMap();
    }
    
    
}
