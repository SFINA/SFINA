/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testing;

import java.util.ArrayList;
import input.TopologyLoader;
import input.InputParametersLoader;
import power.input.PowerMetaInfoLoader;
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
        String nodelocation = "configuration_files/input/time_1/topology/nodes.txt";
        String linklocation = "configuration_files/input/time_1/topology/links.txt";
        String nodemetalocation = "configuration_files/input/time_1/flow/nodes.txt";
        String linkmetalocation = "configuration_files/input/time_1/flow/links.txt";

        // Load topology
        TopologyLoader topologyloader = new TopologyLoader(col_seperator);
        ArrayList<Node> nodes = topologyloader.loadNodes(nodelocation);
        ArrayList<Link> links = topologyloader.loadLinks(linklocation, nodes);

        // Load meta
        PowerMetaInfoLoader metaloader = new PowerMetaInfoLoader(col_seperator, missingValue);
        metaloader.loadNodeMetaInfo(nodemetalocation, nodes);
        metaloader.loadLinkMetaInfo(linkmetalocation, links); 
       
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
