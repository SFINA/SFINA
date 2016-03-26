/*
 * Copyright (C) 2015 SFINA Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package testing;

import java.util.HashMap;
import network.FlowNetwork;
import network.Link;
import power.backend.PowerFlowType;
import power.backend.MATPOWERFlowBackend;
import power.backend.PowerBackendParameter;
import power.input.PowerLinkState;
import power.output.PowerConsoleOutput;

/**
 *
 * @author Ben
 */
public class testMatpower {
    
    static FlowNetwork net = new FlowNetwork();
    private static HashMap<Enum,Object> backendParameters = new HashMap();

    public static void main(String[] args){
        backendParameters.put(PowerBackendParameter.FLOW_TYPE, PowerFlowType.DC);
        
        testLoader loader = new testLoader();
        PowerConsoleOutput printer = new PowerConsoleOutput();
        
        loader.load("case30", net);
        
        MATPOWERFlowBackend algo = new MATPOWERFlowBackend(backendParameters);
        //resetLfData(net);
        algo.flowAnalysis(net);
        printer.printLfResults(net);
     
        double[][] buses = algo.getBusesPowerFlowInfo();
        double[][] gens = algo.getGeneratorsPowerFlowInfo();
        double[][] bras = algo.getBranchesPowerFlowInfo();
        double[][] cost = algo.getCostsPowerFlowInfo();
        //printDoubleArrays(buses, gens, bras, cost);
       
    }
    
    private static void resetLfData(FlowNetwork net){
        for(Link link : net.getLinks()){
            link.replacePropertyElement(PowerLinkState.POWER_FLOW_FROM_REAL, 0.0);
            link.replacePropertyElement(PowerLinkState.POWER_FLOW_FROM_REACTIVE, 0.0);
            link.replacePropertyElement(PowerLinkState.POWER_FLOW_TO_REAL, 0.0);
            link.replacePropertyElement(PowerLinkState.POWER_FLOW_TO_REACTIVE, 0.0);
            link.replacePropertyElement(PowerLinkState.CURRENT, 0.0);            
            link.addProperty(PowerLinkState.LOSS_REAL, 0.0);
            link.addProperty(PowerLinkState.LOSS_REACTIVE, 0.0);
            
        }
    }
    
    public void printDoubleArrays(double[][] buses, double[][] gens, double[][] bras, double[][] cost){
        print(buses, "Bus");
        print(gens, "Generators");
        print(bras, "Branches");
        print(cost, "Cost");
    }
    
    private void print(double[][] stuff, String title){
        System.out.println("---------------- " + title + " ------------------");
        System.out.println("Number of columns: " + stuff[0].length);
        for(int i = 0; i<stuff.length; i++){
            for(int j = 0; j<stuff[i].length; j++){
                System.out.format("%10s", stuff[i][j]);
            }    
            System.out.print("\n");
        }
    }
    
}
