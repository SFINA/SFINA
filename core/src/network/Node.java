/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import dsutil.generic.state.State;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * A node has the following features:
 * 
 * - It extends the state that is a container of information.
 * - It has a number of incoming and outgoing links.
 * - It can be activated/deactivated and connected/disconnected.
 * - It can vary the flow type: the flow can be any information contained in the
 * state
 * 
 * Every persistent operation over the list of links imposes a re-evaluation of
 * the node connectivity.
 * 
 * @author evangelospournaras
 */
public class Node extends State{
    
    private String index;
    private List<Link> links;
    private boolean connected;
    private boolean activated;
    private Enum flowType;
    private Enum capacityType;
    private static final Logger logger = Logger.getLogger(Node.class);
    
    /**
     * Instantiates a disconnected node. This is the recommended constructor as the addition
     * of the links should be performed by a <code>FlowNetworkInterface</code>.
     * 
     * @param index the index of the node
     * @param activated initialized as activated or deactivated
     */
    public Node(String index, boolean activated){
        super();
        this.index=index;
        this.connected=false;
        this.activated=activated;
        this.links=new ArrayList<Link>();
        this.evaluateConnectivity();
    }
    
    /**
     * Instantiates a connected node. This is not a recommended constructor as 
     * the states of the nodes and links should be coordinated with a class 
     * implementing the <code>FlowNetworkInterface</code>.
     * 
     * @param index
     * @param activated
     * @param links 
     */
    public Node(String index, boolean activated, List<Link> links){
        super();
        this.index=index;
        this.connected=false;
        this.activated=activated;
        this.links=new ArrayList<Link>();
        this.links.addAll(links);
        this.evaluateConnectivity();
    }
    
    /**
     * Returns the flow value if a flow type is defined.
     * 
     * @return the flow
     */
    public double getFlow(){
        if(this.flowType==null)
            logger.debug("Flow type is not defined.");
        return Math.abs((double)this.getProperty(flowType));
    }
    
    /**
     * Sets the flow if a flow type is defined
     * 
     * @param flow the flow set
     */
    public void setFlow(double flow){
        if(this.flowType==null){
            logger.debug("Flow type is not defined.");
        }
        else{
            this.addProperty(flowType, flow);
        }
    }
    
    /**
     * Returns the capacity of the node if a capacity type is defined
     * 
     * @return the capacity of the node
     */
    public double getCapacity(){
        if(this.capacityType==null)
            logger.debug("Capacity type is not defined.");
        return Math.abs((double)this.getProperty(capacityType));
    }
    
    /**
     * Sets the capacity of the node if a capacity type is defined
     * 
     * @param capacity the capacity of the node
     */
    public void setCapacity(double capacity){
        if(this.capacityType==null){
            logger.debug("Capacity type is not defined.");
        }
        else{
            this.addProperty(capacityType, capacity);
        }
    }
    
    /**
     * Sets the flow type
     * 
     * @param flowType the set flow type
     */
    public void setFlowType(Enum flowType){
        this.flowType=flowType;
    }
    
    /**
     * Sets the capacity type of the node
     * 
     * @param capacityType the capacity type of the node
     */
    public void setCapacityType(Enum capacityType){
        this.capacityType=capacityType;
    }
    
    /**
     * Adds a link and evaluates the connectivity of the node.
     * 
     * @param link the added link
     */
    public void addLink(Link link){
        if (!this.getLinks().contains(link))
            this.getLinks().add(link);
        this.evaluateConnectivity();
    }
    
    /**
     * Removes the link and evaluates the connectivity of the node
     * 
     * @param link the removed link
     */
    public void removeLink(Link link){
        this.getLinks().remove(link);
        this.evaluateConnectivity();
    }
    
    /**
     * Returns a new list with all incoming links of the node
     * 
     * @return a new array list with the incoming links of the node
     */
    public ArrayList<Link> getIncomingLinks(){
        ArrayList<Link> incomingLinks=new ArrayList<Link>();
        for(Link link:getLinks()){
            if(link.getEndNode().equals(this)){
                incomingLinks.add(link);
            }
        }
        return incomingLinks;
    }
    
    /**
     * Returns a new list with all outgoing links of the node
     * 
     * @return a new array list with the outgoing links of the node
     */
    public ArrayList<Link> getOutgoingLinks(){
        ArrayList<Link> outgoingLinks=new ArrayList<Link>();
        for(Link link:getLinks()){
            if(link.getStartNode().equals(this)){
                outgoingLinks.add(link);
            }
        }
        return outgoingLinks;
    }
    
    /**
     * Returns the status of the node connectivity
     * 
     * @return if the node is connected or disconnected
     */
    public boolean isConnected(){
        return connected;
    }

    /**
     * Returns the index of the node
     * 
     * @return the index of the node
     */
    public String getIndex() {
        return index;
    }

    /**
     * Sets the index of the node
     * 
     * @param index the index to set
     */
    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * Returns the links of the node
     * 
     * @return the links of the node
     */
    public List<Link> getLinks() {
        return links;
    }

    /**
     * Sets the links of the node
     * 
     * @param links the links to set
     */
    public void setLinks(List<Link> links) {
        this.links = links;
        this.evaluateConnectivity();
    }
    
    /**
     * Evaluates the connectivity of the node
     */
    private void evaluateConnectivity(){
        if(links.size()!=0){
            this.connected=true;
        }
        else{
            this.connected=false;
        }
    }

    /**
     * Returned the activated/deactivated status of the node
     * 
     * @return the activated/deactivated status of the node
     */
    public boolean isActivated() {
        return activated;
    }

    /**
     * Sets the activated/deactivated status of the node
     * 
     * @param activated the activated to set
     */
    public void setActivated(boolean activated) {
        this.activated = activated;
        if(activated){
            for(Link link : this.getIncomingLinks()){
                link.setEndNode(this);
            }
            for(Link link : this.getOutgoingLinks()){
                link.setStartNode(this);
            }
        }
        else{
            this.setFlow(0.0);
            for(Link link:this.getIncomingLinks()){
                link.setEndNode(null);
            }
            for(Link link : this.getOutgoingLinks()){
                link.setStartNode(null);
            }
        }
    }
}