/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testing;

import event.Event;
import input.Domain;
import input.EventLoader;
import java.util.HashMap;
import java.util.ArrayList;
import input.TopologyLoader;
import input.InputParameter;
import input.InputParametersLoader;
import java.util.List;
import network.FlowNetwork;
import power.input.PowerFlowDataLoader;
import network.Link;
import network.Node;
import org.apache.log4j.Logger;
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
    private static String eventLocation = "configuration_files/input/events.txt";
    
    // make imported nodes and links accessible for testing other stuff
    private FlowNetwork net;
    private static ArrayList<Node> nodes;
    private static ArrayList<Link> links;
    
    public testLoader(FlowNetwork net, boolean printResults){
        this.net = net;
        
        // Load topology
        TopologyLoader topologyLoader = new TopologyLoader(net, col_seperator);
        topologyLoader.loadNodes(nodeLocation);
        topologyLoader.loadLinks(linkLocation);

        // Load meta
        PowerFlowDataLoader flowDataLoader = new PowerFlowDataLoader(net, col_seperator, missingValue);
        flowDataLoader.loadNodeFlowData(nodeFlowLocation);
        flowDataLoader.loadLinkFlowData(linkFlowLocation); 
        
        // Get Nodes and links
        nodes = new ArrayList<Node>(net.getNodes());
        links = new ArrayList<Link>(net.getLinks());
        
        // Load Input Parameters
        InputParametersLoader paramLoader = new InputParametersLoader(param_seperator);
        HashMap<InputParameter,Object> parameters = paramLoader.loadInputParameters(paramLocation);
        
        // Load Event Parameters
        EventLoader eventLoader = new EventLoader(Domain.POWER, col_seperator);
        ArrayList<Event> events = eventLoader.loadEvents(eventLocation);
               
        // print out data to check
        if (printResults) {
            printNodes(nodes);
            printLinks(links);
            printParam(parameters);
            printEvents(events);
        }
        System.out.println("\n--------------------------------------------------\n    LOADING DATA SUCCESSFUL\n--------------------------------------------------\n");

    }
    
    private static void printNodes(ArrayList<Node> nodes){
        // Print information to see if it worked
        String header = "\n-------------------------\n    NODES\n-------------------------\nIndex       ACTIVE     Connected";
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
        String header = ("\n-------------------------\n    LINKS\n-------------------------\nIndex    StartNode   EndNote Active");
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
            System.out.println(entry.getKey() + ": " + entry.getValue());           
        }
    }
    
    private static void printEvents(ArrayList<Event> events){
        System.out.println("\n-------------------------\n    Event PARAMETERS\n-------------------------");
        
        for (Event event : events) {
            System.out.println("--- New Event ---");
            System.out.println("Event time: " + event.getTime());
            System.out.println("Network Feature: " + event.getNetworkFeature());
            System.out.println("Network Component: " + event.getNetworkComponent());
            System.out.println("Component ID: " + event.getComponentID());
            System.out.println("Parameter and value: " + event.getParameter() + " = " + event.getValue());
        }
    }
    
    public ArrayList<Node> getNodes(){
        return nodes;
    }
    
    public ArrayList<Link> getLinks() {
        return links;
    }
}
