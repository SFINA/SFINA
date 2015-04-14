package core;

import java.util.List;
import network.Link;
import network.Node;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author evangelospournaras
 */
public class SFINA {

    public enum backend{
        MATPOWER,
        INTERPSS
    }
    
    public enum power_flow{
        AC,
        DC
    }
    
    final static String network="case2383wp.m";
    final static double tolerance=2.0;
    
    private List<Node> nodes;
    private List<Link> links;
    
    public SFINA(){
    
    }
    
    private void runPowerFlowAnalysis(){
    
    }
    
     /**
     * @return the nodes
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * @param nodes the nodes to set
     */
    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    /**
     * @return the links
     */
    public List<Link> getLinks() {
        return links;
    }

    /**
     * @param links the links to set
     */
    public void setLinks(List<Link> links) {
        this.links = links;
    }
    
    
}
    
    
