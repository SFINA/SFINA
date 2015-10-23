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

import com.interpss.common.exp.InterpssException;
import network.FlowNetwork;
import network.Link;
import network.Node;
import power.PowerFlowType;
import power.flow_analysis.InterpssFlowBackend;
import power.flow_analysis.MATPOWERFlowBackend;
import power.input.PowerLinkState;
import power.output.PowerConsoleOutput;

/**
 *
 * @author Ben
 */
public class testInterpss {
    static PowerFlowType FlowType = PowerFlowType.AC;
    static String caseName = "case57";
    
    public static void main(String[] args){
        testLoader loader = new testLoader();
        PowerConsoleOutput printer = new PowerConsoleOutput();
        
        FlowNetwork net1 = new FlowNetwork();
        loader.load(caseName, net1); 
        FlowNetwork net2 = new FlowNetwork();
        loader.load(caseName, net2);
        //resetLfData(net1);

        //compareData();
              
        runInterpssOurData(net1);
        runInterpssTheirLoader(net2);
        //runMatlabSimu(net2);
        //System.out.println("\n--------------------------------------------------\n    " + FlowType + ", " +net.getNodes().size()+ " BUS\n--------------------------------------------------\n");
        //printer.printLfResults(net1);
        //printer.printLfResults(net2);
        printer.compareLFResults(net1, net2);
    }

    
    private static void runInterpssOurData(FlowNetwork net){
        InterpssFlowBackend IpssObject = new InterpssFlowBackend(FlowType);
        IpssObject.flowAnalysis(net);
        System.out.println("\n--------------------------------------------------\n    INTERPSS WITH SFINA DATA CONVERSION\n--------------------------------------------------");
    }

    private static void runInterpssTheirLoader(FlowNetwork net){
        InterpssFlowBackend IpssObject = new InterpssFlowBackend(FlowType);
        IpssObject.flowAnalysisIpssDataLoader(net, caseName);
        
        System.out.println("\n--------------------------------------------------\n    INTERPSS WITH DATA LOADED BY INTERPSS' LOADERS\n--------------------------------------------------");        
    }    
    
    private static void compareData(){
        FlowNetwork net = new FlowNetwork();
        testLoader loader = new testLoader();
        loader.load(caseName, net);
        InterpssFlowBackend IpssObject = new InterpssFlowBackend(FlowType);
        try{
            IpssObject.compareDataToCaseLoaded(net, caseName);
        }
        catch(InterpssException ie){
            ie.printStackTrace();
        }
        System.out.println("\n--------------------------------------------------\n    COMPARED DATA FROM IEEE FILE <-> SFINA LOADERS\n--------------------------------------------------");
    }
    
    private static void runMatlabSimu(FlowNetwork net){
        MATPOWERFlowBackend algo = new MATPOWERFlowBackend(FlowType);
        algo.flowAnalysis(net);
    }
    
    private static void resetLfData(FlowNetwork net){
        for(Node bus : net.getNodes()) {
            //bus.replacePropertyElement(PowerNodeState.VOLTAGE_MAGNITUDE, 0.0);
            //bus.replacePropertyElement(PowerNodeState.VOLTAGE_ANGLE, 0.0);
        }
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

}
