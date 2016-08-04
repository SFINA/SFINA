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
package disasterspread.input;

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
public class DisasterSpreadFlowLoader {
    
    private final FlowNetwork net;
    private final String parameterValueSeparator;
    private static final Logger logger = Logger.getLogger(DisasterSpreadFlowLoader.class);
    private final String missingValue;
    
    public DisasterSpreadFlowLoader(FlowNetwork net, String parameterValueSeparator, String missingValue){
        this.net=net;
        this.parameterValueSeparator=parameterValueSeparator;
        this.missingValue=missingValue;
        
    }
    
    public void loadNodeFlowData(String location){
        ArrayList<Node> nodes = new ArrayList<Node>(net.getNodes());
        ArrayList<DisasterSpreadNodeState> disasterNodeStates = new ArrayList<DisasterSpreadNodeState>();
        HashMap<String, ArrayList<String>> nodesStateValues = new HashMap<String, ArrayList<String>>();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            if(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), parameterValueSeparator);
                while(st.hasMoreTokens()){
                    String stateName = st.nextToken();
                    DisasterSpreadNodeState state = this.lookupDisasterNodeState(stateName);
                    disasterNodeStates.add(state);
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
        this.injectNodeStates(nodes, disasterNodeStates, nodesStateValues);
    }
    
    
    public void loadLinkFlowData(String location){
        ArrayList<Link> links = new ArrayList(net.getLinks());
        ArrayList<DisasterSpreadLinkState> disasterLinkStates = new ArrayList<DisasterSpreadLinkState>();
        HashMap<String, ArrayList<String>> linksStateValues = new HashMap<String, ArrayList<String>>();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            if(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), parameterValueSeparator);
                while(st.hasMoreTokens()){
                    String stateName = st.nextToken();
                    DisasterSpreadLinkState state = this.lookupDisasterLinkState(stateName);
                    disasterLinkStates.add(state);
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
        this.injectLinkStates(links, disasterLinkStates, linksStateValues);
                
    }
    
    private void injectNodeStates(ArrayList<Node> nodes, ArrayList<DisasterSpreadNodeState> disasterNodeStates, HashMap<String,ArrayList<String>> nodesStates){
        for(Node node : nodes){
            ArrayList<String> rawValues = nodesStates.get(node.getIndex());
            for(int i=0;i<rawValues.size();i++){
                DisasterSpreadNodeState state = disasterNodeStates.get(i);
                String rawValue = rawValues.get(i);
                if(!rawValue.equals(this.missingValue))
                    node.addProperty(state, this.getActualNodeValue(state, rawValues.get(i)));
            }
        }
    }
    
    private void injectLinkStates(ArrayList<Link> links, ArrayList<DisasterSpreadLinkState> disasterLinkStates, HashMap<String,ArrayList<String>> linksStates){
        for(Link link : links){
            ArrayList<String> rawValues = linksStates.get(link.getIndex());
            for(int i=0;i<rawValues.size();i++){
                DisasterSpreadLinkState state = disasterLinkStates.get(i);
                String rawValue = rawValues.get(i);
                link.addProperty(state, this.getActualLinkValue(state, rawValue));
            }
        }
    }
    
    private DisasterSpreadNodeState lookupDisasterNodeState(String disasterNodeState){
        switch(disasterNodeState){
            case "id": 
                return DisasterSpreadNodeState.ID;
            case "damage":
                return DisasterSpreadNodeState.DAMAGE;
            case "alpha":
                return DisasterSpreadNodeState.ALPHA;
            case "beta":
                return DisasterSpreadNodeState.BETA;
            case "tolerance":
                return DisasterSpreadNodeState.TOLERANCE;
            case "init_recovery_rate":
                return DisasterSpreadNodeState.INITIALRECOVERYRATE;
            case "recovery_rate":
                return DisasterSpreadNodeState.RECOVERYRATE;
            default:
                logger.debug("Disaster node state is not recognized.");
                return null;
        }
    }
   
    private DisasterSpreadLinkState lookupDisasterLinkState(String disasterLinkState){
        switch(disasterLinkState){
            case "id":
                return DisasterSpreadLinkState.ID;
            case "connection_strength":
                return DisasterSpreadLinkState.CONNECTION_STRENGTH;
            case "time_delay":
                return DisasterSpreadLinkState.TIME_DELAY;
            default:
                logger.debug("Disaster link state is not recognized.");
                return null;
        }
    }
    
    private Object getActualNodeValue(DisasterSpreadNodeState disasterNodeState, String rawValue){
        switch(disasterNodeState){
            case ID:
                return Double.parseDouble(rawValue);
            case DAMAGE:
                return Double.parseDouble(rawValue);
            case ALPHA:
                return Double.parseDouble(rawValue);
            case BETA:
                return Double.parseDouble(rawValue);
            case TOLERANCE:
                return Double.parseDouble(rawValue);
            case RECOVERYRATE:
                return Double.parseDouble(rawValue);
            case INITIALRECOVERYRATE:
                return Double.parseDouble(rawValue);
            default:
                logger.debug("Disaster node state is not recognized.");
                return null;
        }    
    }  
    
    private Object getActualLinkValue(DisasterSpreadLinkState disasterLinkState, String rawValue){
        
        switch(disasterLinkState){
            case ID:
                return Double.parseDouble(rawValue);
            case TIME_DELAY:
                return Double.parseDouble(rawValue);
            case CONNECTION_STRENGTH:
                return Double.parseDouble(rawValue);
            default:
                logger.debug("Disaster link state is not recognized.");
                return null;
        }
    }
}
