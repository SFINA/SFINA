/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testing;

import java.util.ArrayList;
import input.TopologyLoader;
import input.InputParametersLoader;
import power.input.PowerFlowDataLoader;
import network.Link;
import network.Node;
import power.input.PowerNodeState;
import power.input.PowerLinkState;

/**
 *
 * @author Ben
 */
public class testLoader {
    public static void main(String[] args){
        String col_seperator = ",";
        String missingValue="-";
        String nodeLocation = "configuration_files/input/time_1/topology/nodes.txt";
        String linkLocation = "configuration_files/input/time_1/topology/links.txt";
        String nodeFlowLocation = "configuration_files/input/time_1/flow/nodes.txt";
        String linkFlowLocation = "configuration_files/input/time_1/flow/links.txt";

        // Load topology
        TopologyLoader topologyLoader = new TopologyLoader(col_seperator);
        ArrayList<Node> nodes = topologyLoader.loadNodes(nodeLocation);
        ArrayList<Link> links = topologyLoader.loadLinks(linkLocation, nodes);

        // Load meta
        PowerFlowDataLoader flowDataLoader = new PowerFlowDataLoader(col_seperator, missingValue);
        flowDataLoader.loadNodeFlowData(nodeFlowLocation, nodes);
        flowDataLoader.loadLinkFlowData(linkFlowLocation, links); 
       
        // Print information to see if it worked
        String header = "-------------------------\n    NODES\n-------------------------\nID       ACTIVE";
        for (PowerNodeState state : PowerNodeState.values()) header += "    " + state;
        System.out.println(header);
        
        for(Node node : nodes){
            String values = node.getIndex() + "    " + node.isActivated();
            for(PowerNodeState state : PowerNodeState.values()){
                values +=  "   " + node.getProperty(state);
            }
            System.out.println(values);
            // Example how to get state variables of e.g. nodes. Have to cast object to respective values.
            //int id = ((Integer)node.getProperty(PowerNodeState.ID)).intValue();
            
        }
        
        header = ("\n-------------------------\n    LINKS\n-------------------------\nID    StartNode   EndNote Active");
        for(PowerLinkState state : PowerLinkState.values()) header += " " + state;
        System.out.println(header);
        
        for(Link link : links){
            String values = link.getIndex() + " " + link.getStartNode().getIndex() + " " + link.getEndNode().getIndex() + "   " + link.isConnected();
            for(PowerLinkState state : PowerLinkState.values()){
                values +=  "   " + link.getProperty(state);
            }
            System.out.println(values);
        }
        System.out.println("--------------------\nLOADING SUCCESSFUL");
    }
}
