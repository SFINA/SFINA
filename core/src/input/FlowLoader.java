/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package input;

import backend.FlowNetworkDataTypesInterface;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import network.FlowNetwork;
import network.InterdependentLink;
import network.Node;
import network.Link;
import network.LinkInterface;
import org.apache.log4j.Logger;

/**
 *
 * @author evangelospournaras
 */
public class FlowLoader {
    
    private static final Logger logger = Logger.getLogger(FlowLoader.class);
    
    private final FlowNetwork net;
    private final String parameterValueSeparator;
    private final String missingValue;
    private FlowNetworkDataTypesInterface flowNetworkDataTypes;
    
    public FlowLoader(FlowNetwork net, String parameterValueSeparator, String missingValue, FlowNetworkDataTypesInterface flowNetworkDataTypes){
        this.net=net;
        this.parameterValueSeparator=parameterValueSeparator;
        this.missingValue=missingValue;
        this.flowNetworkDataTypes=flowNetworkDataTypes;
    }
    
    public void loadNodeFlowData(String location){
        ArrayList<Node> nodes = new ArrayList<>(net.getNodes());
        ArrayList<Enum> nodeStates = new ArrayList<>();
        HashMap<String,ArrayList<String>> nodesStateValues = new HashMap<>();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            if(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), parameterValueSeparator);
                while(st.hasMoreTokens()){
                    String stateName = st.nextToken();
                    Enum state = this.getFlowNetworkDataTypes().parseNodeStateTypeFromString(stateName);
                    nodeStates.add(state);
                }
            }
            while(scr.hasNext()){
                ArrayList<String> values=new ArrayList<>();
                StringTokenizer st = new StringTokenizer(scr.next(), parameterValueSeparator);
                while (st.hasMoreTokens()) {
                    values.add(st.nextToken());
		}
                nodesStateValues.put(values.get(0), values);
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        this.injectNodeStates(nodes, nodeStates, nodesStateValues);
        
    }

    public void loadLinkFlowData(String location){
        loadLinkFlowData(location, false);
    }
    
    public void loadInterdependentLinkFlowData(String location){
        loadLinkFlowData(location, true);
    }
    
    private void loadLinkFlowData(String location, boolean isInterdependent){
        ArrayList<LinkInterface> links;
        if(isInterdependent)
            links = new ArrayList(net.getInterdependentLinks());
        else
            links = new ArrayList(net.getLinks());
        ArrayList<Enum> linkStates = new ArrayList<>();
        HashMap<String, ArrayList<String>> linksStateValues = new HashMap<>();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            if(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), parameterValueSeparator);
                while(st.hasMoreTokens()){
                    String stateName = st.nextToken();
                    Enum state = this.getFlowNetworkDataTypes().parseLinkStateTypeFromString(stateName);
                    linkStates.add(state);
                }
            }
            while(scr.hasNext()){
                ArrayList<String> values = new ArrayList<>();
                StringTokenizer st = new StringTokenizer(scr.next(), parameterValueSeparator);
                while (st.hasMoreTokens()) {
                    values.add(st.nextToken());
		}
                linksStateValues.put(values.get(0), values);
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        this.injectLinkStates(links, linkStates, linksStateValues);
                
    }
    
    private void injectNodeStates(ArrayList<Node> nodes, ArrayList<Enum> nodeStates, HashMap<String,ArrayList<String>> nodeStatesValues){
        for(Node node : nodes){
            ArrayList<String> rawValues = nodeStatesValues.get(node.getIndex());
            for(int i=0;i<rawValues.size();i++){
                Enum state = nodeStates.get(i);
                String rawValue = rawValues.get(i);
                if(!rawValue.equals(this.missingValue) && state != null)
                    node.addProperty(state, this.getFlowNetworkDataTypes().parseNodeValuefromString(state, rawValue));
            }
        }
    }
    
    private void injectLinkStates(ArrayList<LinkInterface> links, ArrayList<Enum> linkStates, HashMap<String,ArrayList<String>> linkStatesValues){
        for(LinkInterface link : links){
            ArrayList<String> rawValues = linkStatesValues.get(link.getIndex());
            for(int i=0;i<rawValues.size();i++){
                Enum state = linkStates.get(i);
                String rawValue = rawValues.get(i);
                if(!rawValue.equals(this.missingValue) &&  state != null){
                    if(link.isInterdependent())
                        ((InterdependentLink)link).addProperty(state, this.getFlowNetworkDataTypes().parseLinkValueFromString(state, rawValue));
                    else
                        ((Link)link).addProperty(state, this.getFlowNetworkDataTypes().parseLinkValueFromString(state, rawValue));
                }
            }
        }
    }
    
    /**
     * @return the flowNetworkDataTypes
     */
    public FlowNetworkDataTypesInterface getFlowNetworkDataTypes() {
        if(flowNetworkDataTypes == null)
            logger.debug("Domain backend has to call setFlowNetworkDataTypes method, but probably didn't.");
        return flowNetworkDataTypes;
    }

    /**
     * @param flowNetworkDataTypes the flowNetworkDataTypes to set
     */
    public void setFlowNetworkDataTypes(FlowNetworkDataTypesInterface flowNetworkDataTypes) {
        this.flowNetworkDataTypes = flowNetworkDataTypes;
    }
}
