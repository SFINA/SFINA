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
package disasterspread.backend;

import backend.FlowBackendInterface;
import disasterspread.input.DisasterSpreadLinkState;
import disasterspread.input.DisasterSpreadNodeState;
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
public class HelbingEtAlModelBackend implements FlowBackendInterface{
    
    // network parameters from paper (assumed to be constants)
    private final double a = 4;
    private final double b = 3;
    
    // strategy constants from paper
    private final double q=0.15;
    private final double k=0.8;
    
    // simulation parameters
    private boolean converged;
    private double timeStep = 1;
    
    private FlowNetwork net;
    private HashMap<Enum,Object> backendParameters;
    private static final Logger logger = Logger.getLogger(HelbingEtAlModelBackend.class);
    
    public HelbingEtAlModelBackend(){
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
        
        for(Node node1:net.getNodes()){
            effectFromNeighbours = 0;
            for(Node node2:net.getNodes()){
                double tDelay = getTimeDelay(node1,node2);
                int healthIndex = (int)Math.round(tDelay/timeStep);
                effectFromNeighbours += (getConnectionStrengthFromNodeIndices(node1.getIndex(), node2.getIndex())*
                                ((ArrayList<Double>)node2.getProperty(DisasterSpreadNodeState.DAMAGEHISTORY)).get(healthIndex)*
                                Math.exp(-(Double)node2.getProperty(DisasterSpreadNodeState.BETA)*
                                healthIndex))/(Double)networkConnectivityWeight(node2);
            }
            runNode(newHealth, node1, effectFromNeighbours);
        }
        for(Node n:net.getNodes()){
            ((ArrayList<Double>)n.getProperty(DisasterSpreadNodeState.DAMAGEHISTORY)).remove(((ArrayList<Double>)n.getProperty(DisasterSpreadNodeState.DAMAGEHISTORY)).size()-1);
            ((ArrayList<Double>)n.getProperty(DisasterSpreadNodeState.DAMAGEHISTORY)).add(0, newHealth.get(n.getIndex()));
            n.replacePropertyElement(DisasterSpreadNodeState.DAMAGE, newHealth.get(n.getIndex()));
        }
    }

    private double getTimeDelay(Node node1, Node node2){
        for(Link link:node1.getOutgoingLinks()){
            if((String)link.getEndNode().getIndex()==node2.getIndex()){
                return (double)link.getProperty(DisasterSpreadLinkState.TIME_DELAY);
            }
        }
        return 0;
    }
        
    private double getConnectionStrengthFromNodeIndices(String index1, String index2){
        for(Link link:net.getLinks()){
            if((String)link.getStartNode().getIndex()==index1 && (String)link.getEndNode().getIndex()==index2){
                return (double)link.getProperty(DisasterSpreadLinkState.CONNECTION_STRENGTH);
            }
        }
        return 0;
    }
    
    private void runNode(HashMap<String,Double> newHealth, Node node, double effectFromNeighbours){
        double health = (Double)node.getProperty(DisasterSpreadNodeState.DAMAGE);
        double nH = (-health/(Double)node.getProperty(DisasterSpreadNodeState.RECOVERYRATE)+sigmoid(node, effectFromNeighbours))*timeStep + health;
        newHealth.put(node.getIndex(), nH);
    }
    
    private double sigmoid(Node node, double effectFromNeighbours){
        double alpha = (Double)node.getProperty(DisasterSpreadNodeState.ALPHA);
        double resistanceThreshold = (Double)node.getProperty(DisasterSpreadNodeState.TOLERANCE);
        return (1.0-Math.exp(-alpha*effectFromNeighbours))/(1.0+Math.exp(-alpha*(effectFromNeighbours-resistanceThreshold)));
    }
    
    private double networkConnectivityWeight(Node node){
        double Oj = node.getOutgoingLinks().size();
        return (a*Oj)/(1.0 + b*Oj);
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