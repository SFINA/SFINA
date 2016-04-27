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
package diseasespread.backend;

import backend.FlowBackendInterface;
import diseasespread.input.DiseaseSpreadLinkState;
import diseasespread.input.DiseaseSpreadNodeState;
import diseasespread.input.DiseaseSpreadBackendParameter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import network.FlowNetwork;
import network.Link;
import network.Node;
import org.apache.log4j.Logger;

/**
 *
 * @author dinesh
 */
public class JavaDiseaseSpreadBackend implements FlowBackendInterface{
    
    // resource distribution parameters from paper
    private final double a1 = 10;//530;
    private final double b1 = 1;//.6;
    private final double c1 = 0.01;//.2;
    
    // network parameters from paper (assumed to be constants)
    private final double a = 4;
    private final double b = 3;
    
    // strategy constants from paper
    private final double q=0.15;
    private final double k=0.8;
    
    // simulation parameters
    private boolean converged;
    private double timeStep = 1;
    private final int strategy = 0;
    
    private FlowNetwork net;
    private HashMap<Enum,Object> backendParameters;
    private static final Logger logger = Logger.getLogger(JavaDiseaseSpreadBackend.class);
    
    public JavaDiseaseSpreadBackend(){
        this.converged=false;
    }
    
    @Override
    public boolean flowAnalysis(FlowNetwork net, HashMap<Enum,Object> parameters) {
        this.net = net;
        this.backendParameters = parameters;
        simulateNetwork();
        return false;
    }
    
    public void simulateNetwork(){
        double effectFromNeighbours;
        HashMap<String, Double> newHealth = new HashMap();
        HashMap<String,ArrayList<Double>> nodeHealthHistory = (HashMap<String,ArrayList<Double>>)(this.backendParameters).get(DiseaseSpreadBackendParameter.NodeHealthHistory);
        
        for(Node node1:net.getNodes()){
            effectFromNeighbours = 0;
            for(Node node2:net.getNodes()){
                double tDelay = getTimeDelayFromNodeIndices(node1.getIndex(),node2.getIndex());
                int healthIndex = (int)Math.round(tDelay/timeStep);
                effectFromNeighbours += (getConnectionStrengthFromNodeIndices(node1.getIndex(), node2.getIndex())*
                                nodeHealthHistory.get(node2.getIndex()).get(healthIndex)*
                                Math.exp(-(Double)node2.getProperty(DiseaseSpreadNodeState.BETA)*
                                healthIndex))/(Double)networkConnectivityWeight(node2);
            }
            runNode(newHealth, node1, effectFromNeighbours);
        }
        for(Node n:net.getNodes()){
            nodeHealthHistory.get(n.getIndex()).remove(nodeHealthHistory.get(n.getIndex()).size()-1);
            nodeHealthHistory.get(n.getIndex()).add(0, newHealth.get(n.getIndex()));
            n.replacePropertyElement(DiseaseSpreadNodeState.HEALTH, newHealth.get(n.getIndex()));
        }
    }
    
    private double getTimeDelayFromNodeIndices(String index1, String index2){
        for(Link link:this.net.getLinks()){
            if((String)link.getStartNode().getIndex()==index1 && (String)link.getEndNode().getIndex()==index2){
                return (double)link.getProperty(DiseaseSpreadLinkState.TIME_DELAY);
            }
        }
        return 0;
    }
    
    private double getConnectionStrengthFromNodeIndices(String index1, String index2){
        for(Link link:net.getLinks()){
            if((String)link.getStartNode().getIndex()==index1 && (String)link.getEndNode().getIndex()==index2){
                return (double)link.getProperty(DiseaseSpreadLinkState.CONNECTION_STRENGTH);
            }
        }
        return 0;
    }
    
    private void runNode(HashMap<String,Double> newHealth, Node node, double effectFromNeighbours){
        double health = (Double)node.getProperty(DiseaseSpreadNodeState.HEALTH);
        double nH = (-health/recoveryRate(node)+sigmoid(node, effectFromNeighbours))*timeStep + health;
        newHealth.put(node.getIndex(), nH);
    }
    
    private double sigmoid(Node node, double effectFromNeighbours){
        double alpha = (Double)node.getProperty(DiseaseSpreadNodeState.ALPHA);
        double resistanceThreshold = (Double)node.getProperty(DiseaseSpreadNodeState.RESISTANCETHRESHOLD);
        return (1.0-Math.exp(-alpha*effectFromNeighbours))/(1.0+Math.exp(-alpha*(effectFromNeighbours-resistanceThreshold)));
    }
    
    private double networkConnectivityWeight(Node node){
        double Oj = node.getOutgoingLinks().size();
        return (a*Oj)/(1.0 + b*Oj);
    }
    
    private double recoveryRate(Node node){
        double rStart = (Double)node.getProperty(DiseaseSpreadNodeState.RECOVERYRATE);
        double alpha2 = 0.58;
        double beta2 = .2;
        int time = (int)(this.backendParameters).get(DiseaseSpreadBackendParameter.TIME);
        //int strategy = (int)(this.backendParameters).get(DiseaseSpreadBackendParameter.STRATEGY);
        return (rStart - beta2)*Math.exp(-alpha2*externalResource(node, time, strategy))+beta2;
    }
    
    private double externalResource(Node node, int time, int strategy){
        if(time<10){
            return 0;
        }
        double r = a1*Math.pow(time-10, b1)*Math.exp(-c1*(time-10));
        int damaged=0;
        int challenged=0;
        double theta = (Double)node.getProperty(DiseaseSpreadNodeState.RESISTANCETHRESHOLD);
        int totalOutgoingOfDamaged = 0;
        int totalOutgoingOfChallenged = 0;
        for(Node n: net.getNodes()){
            if((Double)n.getProperty(DiseaseSpreadNodeState.HEALTH)>theta){
                damaged++;
                totalOutgoingOfDamaged += node.getOutgoingLinks().size();
            }
            if((Double)n.getProperty(DiseaseSpreadNodeState.HEALTH)>0.){
                challenged++;
                totalOutgoingOfChallenged += node.getOutgoingLinks().size();
            }
        }
        switch(strategy){
            case 0:
                return 0;
            case 1:
                return r/(net.getNodes().size());
            case 2:
                return r*(node.getOutgoingLinks().size())/(net.getLinks().size());
            case 3:
                if((Double)node.getProperty(DiseaseSpreadNodeState.HEALTH)>0){
                    return r/(double)challenged;
                } else {
                    return 0;
                }
                
            case 4:
                if(damaged==0){
                    if((Double)node.getProperty(DiseaseSpreadNodeState.HEALTH)>0){
                        return r/(double)challenged;
                    } else {
                        return 0;
                    }
                } else if ((Double)node.getProperty(DiseaseSpreadNodeState.HEALTH)>theta){
                    return r/damaged;
                } else{
                    return 0;
                }
                
            case 5:
                List<Map.Entry<String, Integer>> entries =  new ArrayList<Map.Entry<String, Integer>>();
                for(Node n:net.getNodes()){
                    entries.add(new AbstractMap.SimpleEntry<String, Integer>(n.getIndex(), n.getOutgoingLinks().size()));
                }
                Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
                    public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b){
                      return b.getValue().compareTo(a.getValue());
                    }
                });
                int highlyConnected = (int)Math.ceil(q*net.getNodes().size());
//              int notHighlyConnected = net.getNodes().size() - highlyConnected;
                
//              uniformly for highly connected edges
                for(int i = 0;i<highlyConnected;i++){
                    // TODO few details need to be checked
                    if(entries.get(i).getKey()==node.getIndex()){
                        return (r*k)/(double)highlyConnected;
                    }
                }
                // Use S4 for these edges
                // Count Damaged and Challenged
                damaged = 0;
                challenged = 0;
                for(int i = highlyConnected;i<entries.size();i++){
                    if((Double)(((Node)net.getNode(entries.get(i).getKey())).getProperty(DiseaseSpreadNodeState.HEALTH))>(Double)(((Node)net.getNode(entries.get(i).getKey())).getProperty(DiseaseSpreadNodeState.RESISTANCETHRESHOLD))){
                        damaged ++;
                    }
                    if((Double)(((Node)net.getNode(entries.get(i).getKey())).getProperty(DiseaseSpreadNodeState.HEALTH))>0){
                        challenged ++;
                    }
                }
                if(damaged==0){
                    if((Double)node.getProperty(DiseaseSpreadNodeState.HEALTH)>0){
                        return r*(1-k)/(double)challenged;
                    } else {
                        return 0;
                    }
                } else if ((Double)node.getProperty(DiseaseSpreadNodeState.HEALTH)>theta){
                    return (1-k)/damaged;
                } else{
                    return 0;
                }
            case 6:
                if(totalOutgoingOfDamaged==0){
                    if((Double)node.getProperty(DiseaseSpreadNodeState.HEALTH)>0){
                        return r*(node.getOutgoingLinks().size())/totalOutgoingOfChallenged;
                    } else {
                        return 0;
                    }
                } else if ((Double)node.getProperty(DiseaseSpreadNodeState.HEALTH)>theta){
                    return r*(node.getOutgoingLinks().size())/(double)totalOutgoingOfDamaged;
                } else{
                    return 0;
                }
                
            default:
                return 0;
        }
    }
    
    public void setTimeStep(double timeStep){
        this.timeStep=timeStep;
    }
    
    @Override
    public boolean flowAnalysis(FlowNetwork net) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void setBackendParameters(HashMap<Enum,Object> backendParameters) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}