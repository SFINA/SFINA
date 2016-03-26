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
import java.util.HashMap;
import network.FlowNetwork;
import network.Link;
import network.Node;
import output.TopologyWriter;
import power.backend.PowerFlowType;
import power.input.PowerNodeType;
import power.backend.InterpssFlowBackend;
import power.backend.MATPOWERFlowBackend;
import power.backend.PowerBackendParameter;
import power.input.PowerLinkState;
import power.input.PowerNodeState;
import power.output.PowerConsoleOutput;
import power.output.PowerFlowWriter;

/**
 *
 * @author Ben
 */
public class testInterpss {
    private static HashMap<Enum,Object> backendParameters = new HashMap();
    private static String caseName = "case30";
    
    public static void main(String[] args){
        backendParameters.put(PowerBackendParameter.FLOW_TYPE, PowerFlowType.AC);
        testLoader loader = new testLoader();
        PowerConsoleOutput printer = new PowerConsoleOutput();
        
        FlowNetwork net1 = new FlowNetwork();
        loader.load(caseName, net1);
        FlowNetwork net2 = new FlowNetwork();
        loader.load(caseName, net2);
        FlowNetwork net3 = new FlowNetwork();
        loader.load(caseName, net3);
        //resetLfData(net1);
        
        getIpssData(net1);
        for(Node node : net1.getNodes()){
            net2.getNode(node.getIndex()).replacePropertyElement(PowerNodeState.VOLTAGE_MAGNITUDE, node.getProperty(PowerNodeState.VOLTAGE_MAGNITUDE));
            net2.getNode(node.getIndex()).replacePropertyElement(PowerNodeState.VOLTAGE_ANGLE, node.getProperty(PowerNodeState.VOLTAGE_ANGLE));
        }
        compareData(net2);
        runInterpssOurData(net2);
        runInterpssTheirLoader(net3);
        
        
//        runMatlabSimu(net3);
//        printer.printLfResults(net3);

        //runInterpssOurData(net1);
        //printer.printLfResults(net1);
        
        //adjustGenReact(net1,net3);
        
        //runInterpssTheirLoader(net2);
        //printer.printLfResults(net2);
        
        

        
        
        //System.out.println("\n--------------------------------------------------\n    COMPARING INTERPSS OUR DATA <-> INTERPSS THEIR LOADER\n--------------------------------------------------");
        //printer.compareLFResults(net1, net2);
//        System.out.println("\n--------------------------------------------------\n    COMPARING INTERPSS OUR DATA <-> MATPOWER OUR DATA\n--------------------------------------------------");
//        printer.compareLFResults(net1, net3);
//        System.out.println("\n--------------------------------------------------\n    COMPARING INTERPSS THEIR LOADER <-> MATPOWER OUR DATA\n--------------------------------------------------");
        printer.compareLFResults(net2, net3);
    }

    private static void adjustGenReact(FlowNetwork net1, FlowNetwork net2){
        for (Node node1 : net1.getNodes()){
            if (node1.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.GENERATOR)){
                //node1.replacePropertyElement(PowerNodeState.POWER_GENERATION_REACTIVE, net2.getNode(node1.getIndex()).getProperty(PowerNodeState.POWER_GENERATION_REACTIVE));
                node1.replacePropertyElement(PowerNodeState.POWER_GENERATION_REACTIVE, net2.getNode(node1.getIndex()).getProperty(PowerNodeState.POWER_GENERATION_REACTIVE));
            }            
        }
    }
    
    private static void getIpssData(FlowNetwork net){
        InterpssFlowBackend IpssObject = new InterpssFlowBackend(backendParameters);
        IpssObject.getIpssData(net, caseName);
    }
    
    private static void writeData(FlowNetwork net){
        String topLocation = "";
        String flowLocation = "";
        String colSep = ",";
        String missVal = "-";
        TopologyWriter topWriter = new TopologyWriter(net, colSep);
        topWriter.writeNodes(topLocation);
        topWriter.writeLinks(topLocation);
        PowerFlowWriter flowWriter = new PowerFlowWriter(net, colSep, missVal);
        flowWriter.writeNodeFlowData(flowLocation);
        flowWriter.writeLinkFlowData(flowLocation);
    }
    
    private static void runInterpssOurData(FlowNetwork net){
        System.out.println("\n--------------------------------------------------\n    INTERPSS, SFINA DATA\n--------------------------------------------------");
        InterpssFlowBackend IpssObject = new InterpssFlowBackend(backendParameters);
        boolean converged = IpssObject.flowAnalysis(net);
        System.out.println("Ipss our data converged = " + converged);
    }

    private static void runInterpssTheirLoader(FlowNetwork net){
        System.out.println("\n--------------------------------------------------\n    INTERPSS, DATA LOADED BY INTERPSS' LOADERS\n--------------------------------------------------");        
        InterpssFlowBackend IpssObject = new InterpssFlowBackend(backendParameters);
        boolean converged = IpssObject.flowAnalysisIpssDataLoader(net, caseName);    
        System.out.println("Ipss their loader converged = " + converged);
    }    
    
    private static void compareData(FlowNetwork net){
        System.out.println("\n--------------------------------------------------\n    COMPARING DATA FROM INTERPSS & SFINA LOADER\n--------------------------------------------------");
//        FlowNetwork net = new FlowNetwork();
//        testLoader loader = new testLoader();
//        loader.load(caseName, net);
        InterpssFlowBackend IpssObject = new InterpssFlowBackend(backendParameters);
        try{
            IpssObject.compareDataToCaseLoaded(net, caseName);
        }
        catch(InterpssException ie){
            ie.printStackTrace();
        }
    }
    
    private static void runMatlabSimu(FlowNetwork net){
        System.out.println("\n--------------------------------------------------\n    MATPOWER, SFINA DATA\n--------------------------------------------------");
        MATPOWERFlowBackend algo = new MATPOWERFlowBackend(backendParameters);
        boolean converged = algo.flowAnalysis(net);
        System.out.println("Matpwr our data converged = " + converged);
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
