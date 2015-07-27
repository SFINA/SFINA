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
    private static HashMap<InputParameter,Object> parameters;
    private static ArrayList<Event> events;
    
    public testLoader(FlowNetwork net){
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
        parameters = paramLoader.loadInputParameters(paramLocation);
        
        // Load Event Parameters
        EventLoader eventLoader = new EventLoader(Domain.POWER, col_seperator);
        events = eventLoader.loadEvents(eventLocation);
         
        System.out.println("\n--------------------------------------------------\n    LOADING DATA SUCCESSFUL\n--------------------------------------------------\n");

    }
    
    public void printLoadedData(){
            printNodes();
            printLinks();
            printParam();
            printEvents();
    }
    
    private static void printNodes(){
        System.out.println("\n-------------------------\n    NODES\n-------------------------\n");
        System.out.format("%15s%15s%15s", "Index", "isActivated", "isConnected");
        for (PowerNodeState state : PowerNodeState.values()) {
            String printStateName = state.toString();
            if (printStateName.length() > 13)
                printStateName = printStateName.substring(0,13);
            System.out.format("%15s", printStateName);
        }
        System.out.print("\n");
        
        for(Node node : nodes){
            System.out.format("%15s%15s%15s", node.getIndex(), node.isActivated(), node.isConnected());
            for(PowerNodeState state : PowerNodeState.values())
                System.out.format("%15s", node.getProperty(state));
            System.out.print("\n");
        }
    }
    
    private static void printLinks(){
        System.out.println("\n-------------------------\n    LINKS\n-------------------------\n");
        System.out.format("%15s%15s%15s%15s%15s", "Index", "StartNode", "EndNode", "isActivated", "isConnected");

        for(PowerLinkState state : PowerLinkState.values()) {
            String printStateName = state.toString();
            if (printStateName.length() > 13)
                printStateName = printStateName.substring(0,13);
            System.out.format("%15s", printStateName);
        }
        System.out.print("\n");

        for(Link link : links){
            System.out.format("%15s%15s%15s%15s%15s", link.getIndex(), link.getStartNode().getIndex(), link.getEndNode().getIndex(), link.isActivated(), link.isConnected());
            for(PowerLinkState state : PowerLinkState.values())
                System.out.format("%15s", link.getProperty(state));
            System.out.print("\n");
        }
    }
    
    private static void printParam(){
        System.out.println("\n-------------------------\n    INPUT PARAMETERS\n-------------------------");
        
        for (HashMap.Entry<InputParameter, Object> entry : parameters.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());           
        }
    }
    
    private static void printEvents(){
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
}
