/*
 * Copyright (C) 2015 SFINA Team
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
package network;

import dsutil.generic.state.State;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * A flow network facilitates and mandates the topology of a flow network. This
 * class can be used for the following:
 * 
 * - Build a flow network topology
 * - Alter the topology by activating/deactivating nodes and links
 * - Access nodes and links
 * - Compute topological generic metrics
 * 
 * <code>FlowNetwork</code> is a <code>State</code> class so that it contain
 * global information about the network.
 * 
 * Recommended use of this class: First add all nodes as disconnected and then
 * add the links to connect them!
 * 
 * @author evangelospournaras
 */
public class FlowNetwork extends State implements FlowNetworkInterface{
    
    private HashMap<String,Node> nodes;
    private HashMap<String,Link> links;
    private static final Logger logger = Logger.getLogger(FlowNetwork.class);
    
    /**
     * Simple constructor that instantiates the hash maps. 
     */
    public FlowNetwork(){
        super();
        this.nodes=new HashMap<String,Node>();
        this.links=new HashMap<String,Link>();
    }
    
    @Override
    /**
     * Returns a collection of the nodes
     * 
     * @return a colelction of the nodes
     */
    public Collection<Node> getNodes(){
        return nodes.values();
    }
    
    @Override
    /**
     * Returns a collection of the links
     * 
     * @return a collection of the links
     */
    public Collection<Link> getLinks(){
        return links.values();
    }
    
    @Override
    /**
     * Simply adds a node (without links)
     * 
     * @param node the node added in the network
     */
    public void addNode(Node node){
        this.nodes.put(node.getIndex(), node);
    }
    
    @Override
    /**
     * Adds a link and makes the connection of the involved nodes
     * 
     * @param link the added link
     */
    public void addLink(Link link){
        this.links.put(link.getIndex(), link);
        link.getStartNode().addLink(link);
        link.getEndNode().addLink(link);
    }
    
    @Override
    /**
     * Removes a node and updates the connection with all involved links
     * 
     * @param node the removed node
     */
    public void removeNode(Node node){
        for(Link link:node.getIncomingLinks()){
            link.setEndNode(null);
        }
        for(Link link:node.getOutgoingLinks()){
            link.setStartNode(null);
        }
        this.nodes.remove(node.getIndex());
    }
    
    @Override
    /**
     * Removes a link and updates the connection with all involved nodes
     * 
     * @param link the removed link
     */
    public void removeLink(Link link){
        for(Node node:nodes.values()){
            node.getLinks().remove(link);
        }
        this.links.remove(link.getIndex());
    }
    
    @Override
    /**
     * Returns the node given the node index
     * 
     * @param index the node index
     * @return the node
     */
    public Node getNode(String index){
        return this.nodes.get(index);
    }
    
    @Override
    /**
     * Returns the link given the link index
     * 
     * @param index the link index
     * @return the link
     */
    public Link getLink(String index){
        return links.get(index);
    }
    
    @Override
    /**
     * Activates a node and updates the topology.
     * 
     * @param index the index of the activated node.
     */
    public void activateNode(String index){
        Node activatedNode=nodes.get(index);
        activatedNode.setActivated(true);
        List<Link> incomingLinks=activatedNode.getIncomingLinks();
        for(Link incomingLink:incomingLinks){
            incomingLink.setEndNode(activatedNode);
        }
        List<Link> outgoingLinks=activatedNode.getOutgoingLinks();
        for(Link outgoingLink:outgoingLinks){
            outgoingLink.setStartNode(activatedNode);
        }
    }
    
    @Override
    /**
     * Activates a link and updates the topology
     * 
     * @param index the index of the activated link
     */
    public void activateLink(String index){
        Link activatedLink=links.get(index);
        activatedLink.setActivated(true);
        activatedLink.getStartNode().addLink(activatedLink);
        activatedLink.getEndNode().addLink(activatedLink);
    }
    
    @Override
    /**
     * Deactivates a node and updates the topology
     * 
     * @param index the index of the deactivated node
     */
    public void deactivateNode(String index){
        Node deactivatedNode=nodes.get(index);
        deactivatedNode.setActivated(false);
        List<Link> incomingLinks=deactivatedNode.getIncomingLinks();
        for(Link incomingLink:incomingLinks){
            incomingLink.setEndNode(null);
        }
        List<Link> outgoingLinks=deactivatedNode.getOutgoingLinks();
        for(Link outgoingLink:outgoingLinks){
            outgoingLink.setStartNode(null);
        }
    }
    
    @Override
    /**
     * Deactivates a link and updates the topology
     * 
     * @param index the index of the deactivated link
     */
    public void deactivateLink(String index){
        Link deactivatedLink=links.get(index);
        deactivatedLink.setActivated(false);
        deactivatedLink.getStartNode().removeLink(deactivatedLink);
        deactivatedLink.getEndNode().removeLink(deactivatedLink);
    }
    
    @Override
    /**
     * An example of a topological metric: average node degree
     * 
     * @return the average node degree
     */
    public double getAvgNodeDegree(){
        double totalNodeDegree=0.0;
        for(Node node:this.nodes.values()){
            totalNodeDegree+=node.getLinks().size();
        }
        return totalNodeDegree/this.nodes.size();
    }
}