/*
 * Copyright (C) 2016 SFINA Team
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
package diseasespread.input;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import network.FlowNetwork;
import network.Link;
import network.Node;
import org.apache.log4j.Logger;

/**
 *
 * @author dinesh
 */
public class DiseaseSpreadFlowLoader {
    
    private final FlowNetwork net;
    private final String parameterValueSeparator;
    private static final Logger logger = Logger.getLogger(DiseaseSpreadFlowLoader.class);
    private final String missingValue;
    
    public DiseaseSpreadFlowLoader(FlowNetwork net, String parameterValueSeparator, String missingValue){
        this.net=net;
        this.parameterValueSeparator=parameterValueSeparator;
        this.missingValue=missingValue;
        
    }
    
    public void loadNodeFlowData(String location){
        ArrayList<Node> nodes = new ArrayList<Node>(net.getNodes());
        ArrayList<DiseaseSpreadNodeState> healthNodeStates = new ArrayList<DiseaseSpreadNodeState>();
        HashMap<String, ArrayList<String>> nodesStateValues = new HashMap<String, ArrayList<String>>();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            if(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), parameterValueSeparator);
                while(st.hasMoreTokens()){
                    String stateName = st.nextToken();
                    DiseaseSpreadNodeState state = this.lookupHealthNodeState(stateName);
                    healthNodeStates.add(state);
                }
            }
            while(scr.hasNext()){
                ArrayList<String> values = new ArrayList<String>();
                StringTokenizer st = new StringTokenizer(scr.next(), parameterValueSeparator);
                while(st.hasMoreTokens()){
                    values.add(st.nextToken());
                }
                nodesStateValues.put(values.get(0),values);
            }
        }
        catch (FileNotFoundException ex){
            ex.printStackTrace();
        }
        //nodes.remove(nodes.size()-1);
        //System.out.println(nodes.size()+" "+healthNodeStates.size()+" "+nodesStateValues.size());
        this.injectNodeStates(nodes, healthNodeStates, nodesStateValues);
    }
    
    
    public void loadLinkFlowData(String location){
        ArrayList<Link> links = new ArrayList(net.getLinks());
        ArrayList<DiseaseSpreadLinkState> healthLinkStates = new ArrayList<DiseaseSpreadLinkState>();
        HashMap<String, ArrayList<String>> linksStateValues = new HashMap<String, ArrayList<String>>();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            if(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), parameterValueSeparator);
                while(st.hasMoreTokens()){
                    String stateName = st.nextToken();
                    DiseaseSpreadLinkState state = this.lookupHealthLinkState(stateName);
                    healthLinkStates.add(state);
                }
            }
            while(scr.hasNext()){
                ArrayList<String> values = new ArrayList<String>();
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
        this.injectLinkStates(links, healthLinkStates, linksStateValues);
                
    }
    
    private void injectNodeStates(ArrayList<Node> nodes, ArrayList<DiseaseSpreadNodeState> healthNodeStates, HashMap<String,ArrayList<String>> nodesStates){
        for(Node node : nodes){
            ArrayList<String> rawValues = nodesStates.get(node.getIndex());
            for(int i=0;i<rawValues.size();i++){
                DiseaseSpreadNodeState state = healthNodeStates.get(i);
                String rawValue = rawValues.get(i);
                if(!rawValue.equals(this.missingValue))
                    node.addProperty(state, this.getActualNodeValue(state, rawValues.get(i)));
            }
        }
    }
    
    private void injectLinkStates(ArrayList<Link> links, ArrayList<DiseaseSpreadLinkState> healthLinkStates, HashMap<String,ArrayList<String>> linksStates){
        for(Link link : links){
            ArrayList<String> rawValues = linksStates.get(link.getIndex());
            for(int i=0;i<rawValues.size();i++){
                DiseaseSpreadLinkState state = healthLinkStates.get(i);
                String rawValue = rawValues.get(i);
                link.addProperty(state, this.getActualLinkValue(state, rawValue));
            }
        }
    }
    
    private DiseaseSpreadNodeState lookupHealthNodeState(String healthNodeState){
        switch(healthNodeState){
            case "id": 
                return DiseaseSpreadNodeState.ID;
            case "health":
                return DiseaseSpreadNodeState.HEALTH;
            case "alpha":
                return DiseaseSpreadNodeState.ALPHA;
            case "beta":
                return DiseaseSpreadNodeState.BETA;
            case "resistance_threshold":
                return DiseaseSpreadNodeState.RESISTANCETHRESHOLD;
            case "recovery_rate":
                return DiseaseSpreadNodeState.RECOVERYRATE;
            default:
                logger.debug("Health node state is not recognized.");
                return null;
        }
    }
   
    private DiseaseSpreadLinkState lookupHealthLinkState(String healthLinkState){
        switch(healthLinkState){
            case "id":
                return DiseaseSpreadLinkState.ID;
            case "connection_strength":
                return DiseaseSpreadLinkState.CONNECTION_STRENGTH;
            case "time_delay":
                return DiseaseSpreadLinkState.TIME_DELAY;
            default:
                logger.debug("Health link state is not recognized.");
                return null;
        }
    }
    
    private Object getActualNodeValue(DiseaseSpreadNodeState healthNodeState, String rawValue){
        switch(healthNodeState){
            case ID:
                return Double.parseDouble(rawValue);
            case HEALTH:
                return Double.parseDouble(rawValue);
            case ALPHA:
                return Double.parseDouble(rawValue);
            case BETA:
                return Double.parseDouble(rawValue);
            case RESISTANCETHRESHOLD:
                return Double.parseDouble(rawValue);
            case RECOVERYRATE:
                return Double.parseDouble(rawValue);
            default:
                logger.debug("Health node state is not recognized.");
                return null;
        }    
    }  
    
    private Object getActualLinkValue(DiseaseSpreadLinkState healthLinkState, String rawValue){
        
        switch(healthLinkState){
            case ID:
                return Double.parseDouble(rawValue);
            case TIME_DELAY:
                return Double.parseDouble(rawValue);
            case CONNECTION_STRENGTH:
                return Double.parseDouble(rawValue);
            default:
                logger.debug("Health link state is not recognized.");
                return null;
        }
    }
}
