/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package power.input;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import network.Node;
import org.apache.log4j.Logger;
import power.PowerNodeType;

/**
 *
 * @author evangelospournaras
 */
public class PowerMetaInfoLoader {
    
    private final String parameterValueSeparator;
    private static final Logger logger = Logger.getLogger(PowerMetaInfoLoader.class);
    
    public PowerMetaInfoLoader(String parameterValueSeparator){
        this.parameterValueSeparator=parameterValueSeparator;
    }
    
    public void loadNodeMetaInfo(String location, List<Node> nodes){
        ArrayList<PowerNodeState> powerNodeStates=new ArrayList<PowerNodeState>();
        HashMap<String,ArrayList<String>> nodesStates=new HashMap<String,ArrayList<String>>();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            if(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), parameterValueSeparator);
                while(st.hasMoreTokens()){
                    String metaInfo=st.nextToken();
                    PowerNodeState state=this.lookupPowerNodeState(metaInfo);
                    powerNodeStates.add(state);
                }
            }
            while(scr.hasNext()){
                ArrayList<String> values=new ArrayList<String>();
                StringTokenizer st = new StringTokenizer(scr.next(), parameterValueSeparator);
                while (st.hasMoreTokens()) {
                    values.add(st.nextToken());
		}
                nodesStates.put(values.get(0), values);
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        this.injectNodeStates(nodes, powerNodeStates, nodesStates);
        
    }
    
    private void injectNodeStates(List<Node> nodes, ArrayList<PowerNodeState> powerNodeStates, HashMap<String,ArrayList<String>> nodesStates){
        for(Node node:nodes){
            ArrayList<String> rawValues=nodesStates.get(node.getIndex());
            for(int i=0;i<rawValues.size();i++){
                PowerNodeState state=powerNodeStates.get(i);
                node.addProperty(state, this.getActualValue(state, rawValues.get(i)));
            }
        }
    }
    
    private PowerNodeState lookupPowerNodeState(String powerNodeState){
        switch(powerNodeState){
            case "id": 
                return PowerNodeState.ID;
            case "type":
                return PowerNodeState.TYPE;
            case "real_power":
                return PowerNodeState.REAL_POWER_DEMAND;
            case "reactive_power":
                return PowerNodeState.REACTIVE_POWER_DEMAND;
            default:
                logger.debug("Power node state is not recognized.");
                return null;
        }
    }
    
    private PowerLinkState lookupPowerLinkState(String powerLinkState){
        return null;
    }
    
    private Object getActualValue(PowerNodeState powerNodeState, String rawValue){
        switch(powerNodeState){
            case ID:
                return rawValue;
            case TYPE:
                switch(rawValue){
                    case "SLACK_BUS":
                        return PowerNodeType.SLACK_BUS;
                    case "BUS":
                        return PowerNodeType.BUS;
                    case "GEN":
                        return PowerNodeType.GENERATOR;
                    default:
                        logger.debug("Node type cannot be recognized.");
                        return null;
                }
            case REAL_POWER_DEMAND:
                return rawValue;
            default:
                logger.debug("Power node state is not recognized.");
                return null;
        }
    }
    
    
    
    
    
    
}
