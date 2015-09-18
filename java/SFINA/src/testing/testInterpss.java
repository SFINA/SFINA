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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.FlowNetwork;
import network.Link;
import network.Node;
import power.PowerFlowType;
import power.flow_analysis.InterpssFlowBackend;
import power.input.PowerLinkState;
import power.input.PowerNodeState;

/**
 *
 * @author Ben
 */
public class testInterpss {
    static FlowNetwork net;
    static PowerFlowType FlowType;
    
    public static void main(String[] args){
        net = new FlowNetwork();
        testLoader loader = new testLoader();
        Output printer = new Output();
        
        loader.load("case57", net);
        FlowType = PowerFlowType.DC;
              
        runInterpssOurData();
        //runInterpssTheirLoader();
        //compareData();
        System.out.println("\n--------------------------------------------------\n    " + FlowType + ", " +net.getNodes().size()+ " BUS\n--------------------------------------------------\n");
        printer.printLfResults(net);
    }

    
    private static void runInterpssOurData(){
        InterpssFlowBackend IpssObject = new InterpssFlowBackend(FlowType);
        IpssObject.flowAnalysis(net);
        System.out.println("\n--------------------------------------------------\n    INTERPSS WITH SFINA DATA CONVERSION\n--------------------------------------------------");
    }

    private static void runInterpssTheirLoader(){
        InterpssFlowBackend IpssObject = new InterpssFlowBackend(FlowType);
        try {
            IpssObject.flowAnalysisIpssDataLoader(net, "ieee57.ieee");
        } catch (InterpssException ie) {
            ie.printStackTrace();
        }
        System.out.println("\n--------------------------------------------------\n    INTERPSS WITH DATA LOADED BY INTERPSS' LOADERS\n--------------------------------------------------");        
    }    
    
    private static void compareData(){
        InterpssFlowBackend IpssObject = new InterpssFlowBackend(FlowType);
        try{
            IpssObject.compareDataToCaseLoaded(net, "ieee57.ieee");
        }
        catch(InterpssException ie){
            ie.printStackTrace();
        }
        System.out.println("\n--------------------------------------------------\n    COMPARED DATA FROM IEEE FILE <-> SFINA LOADERS\n--------------------------------------------------");
    }
    
    private static void resetLfData(FlowNetwork net){
        for(Node bus : net.getNodes()) {
            bus.replacePropertyElement(PowerNodeState.VOLTAGE_MAGNITUDE, 0.0);
            bus.replacePropertyElement(PowerNodeState.VOLTAGE_ANGLE, 0.0);
        }
        for(Link link : net.getLinks()){
            link.replacePropertyElement(PowerLinkState.REAL_POWER_FLOW_FROM, 0.0);
            link.replacePropertyElement(PowerLinkState.REACTIVE_POWER_FLOW_FROM, 0.0);
            link.replacePropertyElement(PowerLinkState.REAL_POWER_FLOW_TO, 0.0);
            link.replacePropertyElement(PowerLinkState.REACTIVE_POWER_FLOW_TO, 0.0);
            link.replacePropertyElement(PowerLinkState.CURRENT, 0.0);            
            link.addProperty(PowerLinkState.LOSS_REAL, 0.0);
            link.addProperty(PowerLinkState.LOSS_REACTIVE, 0.0);   
        }
    }

}
