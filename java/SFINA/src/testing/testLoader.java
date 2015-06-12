/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testing;

import java.util.HashMap;
import java.util.ArrayList;
import input.TopologyLoader;
import input.InputParameter;
import input.InputParametersLoader;
import java.util.List;
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
    private static String col_seperator = ",";
    private static String param_seperator = "=";
    private static String missingValue="-";
    private static String nodeLocation = "configuration_files/input/time_1/topology/nodes.txt";
    private static String linkLocation = "configuration_files/input/time_1/topology/links.txt";
    private static String nodeFlowLocation = "configuration_files/input/time_1/flow/nodes.txt";
    private static String linkFlowLocation = "configuration_files/input/time_1/flow/links.txt";
    private static String paramLocation = "configuration_files/input/parameters.txt";
    
    // make imported nodes and links accessible for testing other stuff
    private static ArrayList<Node> nodes;
    private static ArrayList<Link> links;
    
    public testLoader(boolean printResults){
                
        // Load topology
        TopologyLoader topologyLoader = new TopologyLoader(col_seperator);
        nodes = topologyLoader.loadNodes(nodeLocation);
        links = topologyLoader.loadLinks(linkLocation, nodes);

        // Load meta
        PowerFlowDataLoader flowDataLoader = new PowerFlowDataLoader(col_seperator, missingValue);
        flowDataLoader.loadNodeFlowData(nodeFlowLocation, nodes);
        flowDataLoader.loadLinkFlowData(linkFlowLocation, links); 
        
        // Load Input Parameters
        InputParametersLoader paramLoader = new InputParametersLoader(param_seperator);
        HashMap<InputParameter,Object> parameters = paramLoader.loadInputParameters(paramLocation);
               
        // print out data to check
        if (printResults) {
            printNodes(nodes);
            printLinks(links);
            printParam(parameters);
        }
        System.out.println("\n--------------------------------------------------\n    LOADING DATA SUCCESSFUL\n--------------------------------------------------\n");

    }
    
    private static void printNodes(ArrayList<Node> nodes){
        // Print information to see if it worked
        String header = "\n-------------------------\n    NODES\n-------------------------\nID       ACTIVE     Connected";
        for (PowerNodeState state : PowerNodeState.values()) header += "    " + state;
        System.out.println(header);
        
        for(Node node : nodes){
            String values = node.getIndex() + "    " + node.isActivated() + "   " + node.isConnected();
            for(PowerNodeState state : PowerNodeState.values()){
                values +=  "   " + node.getProperty(state);
            }
            System.out.println(values);
            // Example how to get state variables of e.g. nodes. Have to cast object to respective values.
            //int id = ((Integer)node.getProperty(PowerNodeState.ID)).intValue();  
        }
    }
    
    private static void printLinks(ArrayList<Link> links){
        String header = ("\n-------------------------\n    LINKS\n-------------------------\nID    StartNode   EndNote Active");
        for(PowerLinkState state : PowerLinkState.values()) header += " " + state;
        System.out.println(header);
        
        for(Link link : links){
            String values = link.getIndex() + " " + link.getStartNode().getIndex() + " " + link.getEndNode().getIndex() + "   " + link.isActivated();
            for(PowerLinkState state : PowerLinkState.values()){
                values +=  "   " + link.getProperty(state);
            }
            System.out.println(values);
        }
    }
    
    private static void printParam(HashMap<InputParameter,Object> parameters){
        System.out.println("\n-------------------------\n    INPUT PARAMETERS\n-------------------------");
        
        for (HashMap.Entry<InputParameter, Object> entry : parameters.entrySet()) {
            System.out.println(entry.getKey() + "   " + entry.getValue());           
        }
    }
    
    public ArrayList<Node> getNodes(){
        return nodes;
    }
    
    public ArrayList<Link> getLinks() {
        return links;
    }
}
