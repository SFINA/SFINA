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
import input.SfinaParameter;
import java.io.File;
import network.FlowNetwork;
import power.input.PowerFlowLoader;
import network.Link;
import network.Node;
import power.output.PowerConsoleOutput;

/**
 *
 * @author Ben
 */
public class testLoader {
    private static String col_seperator = ",";
    private static String param_seperator = "=";
    private static String missingValue="-";
    private String nodeLocation;
    private String linkLocation;
    private String nodeFlowLocation;
    private String linkFlowLocation;
    private static String eventLocation = "configuration_files/input/events.txt";
    
    // make imported nodes and links accessible for testing other stuff
    private FlowNetwork net;
    private static ArrayList<Node> nodes;
    private static ArrayList<Link> links;
    private static HashMap<SfinaParameter,Object> parameters;
    private static ArrayList<Event> events;
    
    public testLoader(){
    }
    
    public void load(String caseName, FlowNetwork net){
        this.net = net;
        
        // Construct paths to case files
        this.nodeLocation = "configuration_files/input/" + caseName + "/topology/nodes.txt";
        this.linkLocation = "configuration_files/input/" + caseName + "/topology/links.txt";
        this.nodeFlowLocation = "configuration_files/input/" + caseName + "/flow/nodes.txt";
        this.linkFlowLocation = "configuration_files/input/" + caseName + "/flow/links.txt";
        
        // Check if caseName does exist as a directory
        File file=new File(nodeLocation);
        if (!file.exists()){
            System.out.println("WARNING: The specified case file folder seems to not exist! Cant't load data. Exit.");
            System.exit(0);
        }
        
        // Load topology
        TopologyLoader topologyLoader = new TopologyLoader(net, col_seperator);
        topologyLoader.loadNodes(nodeLocation);
        topologyLoader.loadLinks(linkLocation);

        // Load meta
        PowerFlowLoader flowDataLoader = new PowerFlowLoader(net, col_seperator, missingValue);
        flowDataLoader.loadNodeFlowData(nodeFlowLocation);
        flowDataLoader.loadLinkFlowData(linkFlowLocation); 
        
        // Get Nodes and links
        nodes = new ArrayList<Node>(net.getNodes());
        links = new ArrayList<Link>(net.getLinks());
        
        // Load Event Parameters
        EventLoader eventLoader = new EventLoader(Domain.POWER, col_seperator, missingValue);
        events = eventLoader.loadEvents(eventLocation);
         
        //System.out.println("\n--------------------------------------------------\n    LOADING DATA SUCCESSFUL\n--------------------------------------------------\n");

    }
    
    public void printLoadedData(){
            PowerConsoleOutput printer = new PowerConsoleOutput();
            printer.printNodesAll(net);
            printer.printLinksAll(net);
            printParam();
            printEvents();
    }
    
    private static void printParam(){
        System.out.println("\n-------------------------\n    INPUT PARAMETERS\n-------------------------");
        
        for (HashMap.Entry<SfinaParameter, Object> entry : parameters.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());           
        }
    }
    
    private static void printEvents(){
        System.out.println("\n-------------------------\n    Event PARAMETERS\n-------------------------");
        
        for (Event event : events) {
            System.out.println("--- New Event ---");
            System.out.println("Event time: " + event.getTime());
            System.out.println("Network Feature: " + event.getEventType());
            System.out.println("Network Component: " + event.getNetworkComponent());
            System.out.println("Component ID: " + event.getComponentID());
            System.out.println("Parameter and value: " + event.getParameter() + " = " + event.getValue());
        }
    }
}
