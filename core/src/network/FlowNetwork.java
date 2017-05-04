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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import static org.jgrapht.alg.DijkstraShortestPath.findPathBetween;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * A flow network facilitates and mandates the topology of a flow network. This
 * class can be used for the following:
 * 
 * - Build a flow network topology
 * - Alter the topology by activating/deactivating nodes and links
 * - Access nodes and links
 * - Compute and return the islands (compontents)
 * - Compute topological generic metrics
 * 
 * <code>FlowNetwork</code> is a <code>State</code> class that contains
 * global information about the network.
 * 
 * Recommended use of this class: First add all nodes and then
 * add the links to connect them!
 * 
 * @author evangelospournaras
 */
public class FlowNetwork extends State implements FlowNetworkInterface{
    
    private List<Node> nodes;
    private List<LinkInterface> links;
    private int networkIndex =-1;
    private static final Logger logger = Logger.getLogger(FlowNetwork.class);
    
    /**
     * Simple constructor that instantiates the hash maps. 
     */
    public FlowNetwork(){
        super();
        this.nodes=new ArrayList<>();
        this.links=new ArrayList<>();
    }
    
    /**
     * @return the networkIndex
     */
    @Override
    public int getNetworkIndex() {
        return networkIndex;
    }

    /**
     * @param networkIndex the networkIndex to set
     */
    @Override
    public void setNetworkIndex(int networkIndex) {
        this.networkIndex = networkIndex;
    }
    
    /**
     * Return all connected networks. 
     * A network is connected, if an interdependent link points to or from that 
     * network and is activated and connected.
     * 
     * @return a list of network indices of all connected networks
     */
    @Override
    public Collection<Integer> getConnectedNetworkIndices(){
        Collection<Integer> networkIndices = new ArrayList<>();
        for(InterdependentLink link : this.getLinksInterdependent())
            if(!networkIndices.contains(link.getRemoteNetworkIndex()) && link.isActivated() && link.isConnected()){
                if(link.getRemoteNetworkIndex() != this.getNetworkIndex())
                    networkIndices.add(link.getRemoteNetworkIndex());
            }
        return networkIndices;
    }
    
    /**
     * Returns a collection of the nodes
     * 
     * @return a colelction of the nodes
     */
    @Override
    public Collection<Node> getNodes(){
        return nodes;
    }
    
    /**
     *
     * @return
     */
    @Override
    public Collection<LinkInterface> getLinksAll(){
        return links;
    }
    
    /**
     * Returns a collection of the links
     * 
     * @return a collection of the links
     */
    @Override
    public Collection<Link> getLinks(){
        ArrayList<Link> localLinks = new ArrayList<>();
        for(LinkInterface link : links)
            if(!link.isInterdependent())
                localLinks.add((Link) link);
        return localLinks;
    }
    
    /**
     * Returns a collection of the links
     * 
     * @return a collection of the links
     */
    @Override
    public Collection<InterdependentLink> getLinksInterdependent(){
        ArrayList<InterdependentLink> interLinks = new ArrayList<>();
        for(LinkInterface link : links)
            if(link.isInterdependent())
                interLinks.add((InterdependentLink) link);
        return interLinks;
    }
    
    @Override
    /**
     * Simply adds a node (without links)
     * 
     * @param node the node added in the network
     */
    public void addNode(Node node){
        if(!this.nodes.contains(node))
            this.nodes.add(node);
    }
    
    /**
     * Adds a link and makes the connection of the involved nodes
     * 
     * @param link the added link
     */
    @Override
    public void addLink(LinkInterface link){
        if(!this.links.contains(link))
            this.links.add(link);
        if(link.isActivated()){
            link.getStartNode().addLink(link);
            link.getEndNode().addLink(link);
        }
    }
    
    /**
     * Removes a node and updates the connection with all involved links
     * 
     * @param node the removed node
     */
    @Override
    public void removeNode(Node node){
        for(Link link:node.getIncomingLinks()){
            link.setEndNode(null);
        }
        for(Link link:node.getOutgoingLinks()){
            link.setStartNode(null);
        }
        this.nodes.remove(node);
    }
    
    /**
     * Removes a link and updates the connection with all involved nodes
     * 
     * @param link the removed link
     */
    @Override
    public void removeLink(LinkInterface link){
        for(Node node:nodes){
            node.removeLink(link);
        }
        this.links.remove(link);
        
    }
    
    /**
     * Returns the node given the node index
     * 
     * @param index the node index
     * @return the node
     */
    @Override
    public Node getNode(String index){
        for(Node node : nodes)
            if(node.getIndex().equals(index))
                return node;
        return null;
    }
    
    /**
     * Returns the link given the link index
     * 
     * @param index the link index
     * @return the link
     */
    @Override
    public Link getLink(String index){
        for(LinkInterface link : links)
            if(link.getIndex().equals(index) && !link.isInterdependent())
                return (Link)link;
        return null;
    }
    
    /**
     * Returns the interdependent link given the link index
     * 
     * @param index the link index
     * @return the link
     */
    @Override
    public InterdependentLink getInterdependentLink(String index){
        for(LinkInterface link : links)
            if(link.getIndex().equals(index) && link.isInterdependent())
                return (InterdependentLink)link;
        return null;
    }
    
    /**
     * Returns the link given the start node and end node
     * 
     * @param startNode the start node of the link
     * @param endNode the end node of the link
     * @return the link or null if no link between the nodes exists
     */
    @Override
    public Link getLink(Node startNode, Node endNode){
        for (LinkInterface link : links){
            if (!link.isInterdependent() && link.getStartNode() == startNode && link.getEndNode() == endNode) // User can't know the remote node of the other network for interdependent link.
                return (Link)link;
        }
        return null;
    }
    
    /**
     * Sets the flow type of the links
     * 
     * @param flowType the flow type of the links
     */
    public void setLinkFlowType(Enum flowType){
        for(Link link:this.getLinks()){
            link.setFlowType(flowType);
        }
    }
    
//    /**
//     * Sets the flow type of the interdependent links
//     * 
//     * @param flowType the flow type of the links
//     */
//    public void setInterdependentLinkFlowType(Enum flowType){
//        for(Link link:this.getLinksInterdependent()){
//            link.setFlowType(flowType);
//        }
//    }
    
    /**
     * Sets the flow type of the nodes
     * 
     * @param flowType the flow type of the nodes
     */
    public void setNodeFlowType(Enum flowType){
        for(Node node:this.getNodes()){
            node.setFlowType(flowType);
        }
    }
    
    /**
     * Sets the capacity type of the links
     * 
     * @param capacityType the capacity type of the links
     */
    public void setLinkCapacityType(Enum capacityType){
        for(Link link:this.getLinks()){
            link.setCapacityType(capacityType);
        }
    }
    
    /**
     * Sets the capacity type of the nodes
     * 
     * @param capacityType the capacity time of nodes
     */
    public void setNodeCapacityType(Enum capacityType){
        for(Node node:this.getNodes()){
            node.setCapacityType(capacityType);
        }
    }
    
    /**
     * Activates a node and updates the topology.
     * 
     * @param index the index of the activated node.
     */
    @Override
    public void activateNode(String index){
        this.getNode(index).setActivated(true);
    }
     
    /**
     * Activates a link and updates the topology
     * 
     * @param index the index of the activated link
     */
    @Override
    public void activateLink(String index){
        this.getLink(index).setActivated(true);
    }
    
    /**
     * Deactivates a node and updates the topology
     * 
     * @param index the index of the deactivated node
     */
    @Override
    public void deactivateNode(String index){
        this.getNode(index).setActivated(false);
    }
    
    /**
     * Deactivates a link and updates the topology
     * 
     * @param index the index of the deactivated link
     */
    @Override
    public void deactivateLink(String index){
        this.getLink(index).setActivated(false);
    }
    
    /**
     * Compute islands of this FlowNetwork.
     * 
     * @return ArrayList where each entry is a new FlowNetwork object containing nodes and links belonging to this island. 
     */
    @Override
    public ArrayList<FlowNetwork> computeIslands(){
        ArrayList<FlowNetwork> islands = new ArrayList<>();
        ArrayList<Node> leftNodes = new ArrayList();
        for (Node node : this.getNodes())
            if(node.isActivated())
                leftNodes.add(node);
        while (leftNodes.size() > 0){
            ArrayList<Node> newIslandNodes = new ArrayList();
            ArrayList<Link> newIslandLinks = new ArrayList();
            Node currentNode = leftNodes.get(0);
            iterateIsland(currentNode, newIslandNodes, newIslandLinks, leftNodes);
            
            Collections.sort(newIslandNodes, new Comparator<Node>(){
                public int compare(Node node1, Node node2) {
                return Integer.compare(Integer.parseInt(node1.getIndex()), Integer.parseInt(node2.getIndex()));
                }
            });
            
            Collections.sort(newIslandLinks, new Comparator<Link>(){
                public int compare(Link link1, Link link2) {
                return Integer.compare(Integer.parseInt(link1.getIndex()), Integer.parseInt(link2.getIndex()));
                }
            });
            
            FlowNetwork newIsland = new FlowNetwork();
            for (Node node : newIslandNodes)
                newIsland.addNode(node);
            for (Link link : newIslandLinks)
                newIsland.addLink(link);
            
            islands.add(newIsland);
        }
        return islands;
    }
    
    private void iterateIsland(Node currentNode, ArrayList<Node> currentIslandNodes, ArrayList<Link> currentIslandLinks, ArrayList<Node> leftNodes){
        if (currentIslandNodes.contains(currentNode)){
            logger.debug("Attention: Island iterator was called even though node is already in island, which should not happen! Check algorithm!");
            return;
        }
        currentIslandNodes.add(currentNode);
        
        if (!leftNodes.contains(currentNode)){
            logger.debug("Attention: Node " + currentNode.getIndex() + " not in array of nodes not assigned to island, which should not happen! Check algorithm!");
            return;            
        }    
        leftNodes.remove(currentNode);
        
        // Check all links connected to current node.
        ArrayList<Link> currentLinks = (ArrayList)currentNode.getLinks();
        if (currentLinks.size() > 0){
            for (Link link : currentLinks){
                if (!currentIslandLinks.contains(link))
                    currentIslandLinks.add(link);
                // Restart Iteration with EndNode of current Link if it is connected (i.e. has node on other end.
                if (link.isConnected()){
                    currentNode = (Node)link.getEndNode();
                    if (!currentIslandNodes.contains(currentNode)){
                        iterateIsland(currentNode, currentIslandNodes, currentIslandLinks, leftNodes);
                    }
                    // Restart Iteration with StartNode of current Link
                    currentNode = (Node)link.getStartNode();
                    if (!currentIslandNodes.contains(currentNode)){
                        iterateIsland(currentNode, currentIslandNodes, currentIslandLinks, leftNodes);
                    }
                }
            }
        }
    }
    
    /**
     * An example of a topological metric: average node degree
     * 
     * @return the average node degree
     */
    @Override
    public double getAvgNodeDegree(){
        double totalNodeDegree=0.0;
        for(Node node:this.nodes){
            totalNodeDegree+=node.getLinks().size();
        }
        return totalNodeDegree/this.nodes.size();
    }
    
    /**
     * Calculates closeness centrality for given node
     * @param node
     * @return centrality
     */
    @Override
    public double getClosenessCentrality(Node node){
        double distance = 0.0;
        for (Node otherNode : nodes){
            if (otherNode != node){
                ArrayList<Link> asp = getShortestPath(otherNode, node);
                if (asp == null)
                    return 0.0;
                else
                    distance += asp.size();
            }
        }
        return (nodes.size()-1)/distance;
    }
    
    @Override
    public double getDegCentrality(Node node){
        return node.getLinks().size();
    }
    
    @Override
    public LinkedHashMap getDegreeDist(){
        LinkedHashMap<Integer,Integer> dist = new LinkedHashMap<>();
        ArrayList<Integer> allDegrees = new ArrayList<>();
        for (Node node : this.nodes){
            allDegrees.add(node.getLinks().size());
        }
        for (int i=0; i < Collections.max(allDegrees)+1; i++)
            dist.put(i, Collections.frequency(allDegrees, i));
        return dist;
    }
    
    @Override
    public double getClustCoeff(){
        // Compute local clustering coefficient for each node
        ArrayList<Double> localClustCoeff = new ArrayList();
        for(Node node:this.nodes){
            // Get neighbors of current node
            ArrayList<Node> neighborNodes = new ArrayList();
            for(Link link : node.getLinks()){
                if(!link.getStartNode().getIndex().equals(node.getIndex()))
                    neighborNodes.add((Node)link.getStartNode());
                else if(!link.getEndNode().getIndex().equals(node.getIndex()))
                    neighborNodes.add((Node)link.getEndNode());
            }
            
            // Get number of neighbors of current node that are connected
            Double connectedNeighbors = 0.0;
            for(Node neighbor : neighborNodes){
                for(Link neighborLink : neighbor.getLinks()){
                    if(neighborNodes.contains(neighborLink.getEndNode()) && neighborLink.getEndNode() != neighbor){
                        connectedNeighbors += 1.0;
                    }
                    if(neighborNodes.contains(neighborLink.getStartNode()) && neighborLink.getStartNode() != neighbor){
                        connectedNeighbors += 1.0;
                    }
                }
            }
            connectedNeighbors /= 2; // Every neighbor connection is counted twice
            
            // Compute local clustering coefficient of current node = number of connected neighbors / (d*(d-1))
            if(node.getLinks().size() > 1) // prevent division by 0
                localClustCoeff.add(2*connectedNeighbors/node.getLinks().size()/(node.getLinks().size()-1));
            
            /*System.out.println("---------------");
            System.out.println(node.getIndex() + " has " + connectedNeighbors + " connected neighbors");
            System.out.println(node.getIndex() + " has " + node.getLinks().size() + " neighbors");
            System.out.println("Local ClustCoeff of node " + node.getIndex() + "is:  " + 2*connectedNeighbors/node.getLinks().size()/(node.getLinks().size()-1));*/
        }        
        
        // Compute global clustering coefficient = sum(localClustCoeff)/n
        Double globalClustCoeff = 0.0;
        for (Double el : localClustCoeff)
            globalClustCoeff += el;
        
        return globalClustCoeff/nodes.size();
    };

    /**
     * Computes the average shortest path length of the network
     * @return average shortest path length
     */
    public double getAvgShortestPath(){
        double asp = 0.0;
        for (Node v : nodes){
            for (Node w : nodes){
                ArrayList<Link> path = getShortestPath(v,w);
                if (path != null)
                    asp += path.size();
            }
        }
        return asp/nodes.size()/nodes.size();
    }
    
    /**
     * Uses package JGraphT (http://jgrapht.org/) to find the shortest path between two nodes
     * @param v start node
     * @param w end node
     * @return Link sequence of shortest path or null if no path exists
     */
    public ArrayList<Link> getShortestPath(Node v, Node w){
        ArrayList<Link> path = new ArrayList();
        Graph<String, DefaultEdge> jgraphtGraph = buildJGraphT();
        List<DefaultEdge> jgraphtPath = findPathBetween(jgraphtGraph,v.getIndex(),w.getIndex());
        if (jgraphtPath == null)
            return null;
        for (DefaultEdge edge : jgraphtPath){
            path.add(this.getLink(this.getNode(jgraphtGraph.getEdgeSource(edge)), this.getNode(jgraphtGraph.getEdgeTarget(edge))));
        }
        return path;
    }
    
    private ArrayList<Link> getShortestPath(Node v, Node w, Graph<String, DefaultEdge> g){
        ArrayList<Link> path = new ArrayList();
        List<DefaultEdge> jgraphtPath = findPathBetween(g,v.getIndex(),w.getIndex());
        if (jgraphtPath == null)
            return null;
        for (DefaultEdge edge : jgraphtPath){
            path.add(this.getLink(this.getNode(g.getEdgeSource(edge)), this.getNode(g.getEdgeTarget(edge))));
        }
        return path;
    }
//    
//    private ArrayList<ArrayList<Link>> getAllShortestPaths(){
//        ArrayList<ArrayList<Link>> paths = new ArrayList();
//        ArrayList<Link> currentPath = new ArrayList();
//        Graph<String, DefaultEdge> g = buildJGraphT();
//        FloydWarshallShortestPaths floydAlgo = new FloydWarshallShortestPaths(g);
//        Collection<GraphPath<String,DefaultEdge>> pathsJGT = floydAlgo.getShortestPaths();
//        return paths;
//    }
    
    private Graph<String, DefaultEdge> buildJGraphT(){
        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        for (Node node : nodes)
            if(node.isActivated())
                g.addVertex(node.getIndex());
        for (LinkInterface link : links)
            if(link.isActivated())
                g.addEdge(link.getStartNode().getIndex(), link.getEndNode().getIndex());
        return g;
    }

}
