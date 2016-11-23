/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import dsutil.generic.state.State;
import org.apache.log4j.Logger;

/**
 * A link has the following features:
 * 
 * - It extends the state that is a container of information.
 * - It has a start and end node, by default, a directed link.
 * - It can be activated/deactivated and connected/disconnected.
 * - It can vary the flow type: the flow can be any information contained in the
 * state
 * 
 * Every persistent operation over the start and end node of the link imposes a
 * re-evaluation of the node connectivity.
 * 
 * @author evangelospournaras
 */
public class Link extends State implements LinkInterface{
    
    private String index;
    private NodeInterface startNode;
    private NodeInterface endNode;
    private boolean connected;
    private boolean activated;
    private Enum flowType;
    private Enum capacityType;
    private static final Logger logger = Logger.getLogger(Link.class);
    
    /**
     * Instantiates a link with an index and its activation/deactivation status. 
     * 
     * @param index the index of the link
     * @param activated
     */
    public Link(String index, boolean activated){
        super();
        this.index=index;
        this.activated=activated;
        this.evaluateConnectivity();
    }
    
    /**
     * Instantiates a link with an index, its activation/deactivation status and
     * a start/end node. 
     * 
     * @param index the index of the node
     * @param activated 
     * @param startNode the start node of the link
     * @param endNode the end node of the link
     */
    public Link(String index, boolean activated, NodeInterface startNode, NodeInterface endNode){
        super();
        this.index=index;
        this.startNode=startNode;
        this.endNode=endNode;
        this.activated=activated;
        this.evaluateConnectivity();
    }
    
    /**
     * Check if link is interdependent.
     * Returns true if StartNode and EndNode of link are in same network.
     * 
     * @return if link is interdependent
     */
    @Override
    public boolean isInterdependent(){
        return false;
    }

    /**
     * Returns the index of the link
     * 
     * @return the index of the link
     */
    @Override
    public String getIndex() {
        return index;
    }

    /**
     * Sets the index of the link
     * 
     * @param index the index of the link
     */
    @Override
    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * Returns the start node of the link
     * 
     * @return the startNode of the link
     */
    @Override
    public NodeInterface getStartNode() {
        return this.startNode;
    }

    /**
     * Sets the start node of the link and evaluates connectivity
     * 
     * @param startNode the startNode of the link
     */
    @Override
    public void setStartNode(NodeInterface startNode) {
        this.startNode = startNode;
        this.evaluateConnectivity();
    }

    /**
     * Returns the end node of the link
     * 
     * @return the endNode of the link
     */
    @Override
    public NodeInterface getEndNode() {
        return this.endNode;
    }

    /**
     * Sets the end node of the link and evaluates the connectivity
     * 
     * @param endNode the endNode of the link
     */
    @Override
    public void setEndNode(NodeInterface endNode) {
        this.endNode = endNode;
        this.evaluateConnectivity();
    }

    /**
     * Returns if connected or disconnected
     * 
     * @return if connected or disconnected
     */
    @Override
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Returns if there is an activated  start node
     * 
     * @return if there is an activated  start node
     */
    private boolean hasStartNode(){
        if(this.startNode!=null && this.startNode.isActivated()){
            return true;
        }
        return false;
    }
    
    /**
     * Returns if there is an activated end node
     * 
     * @return if there is an activated end node
     */
    private boolean hasEndNode(){
        if(this.endNode!=null && this.endNode.isActivated()){
            return true;
        }
        return false;
    }
    
    /**
     * Evalautes the connectivity of the link. If the link has both a start and
     * end node, it is connected
     */
    protected void evaluateConnectivity(){
        if(this.hasStartNode() && this.hasEndNode()){
            this.connected=true;
        }
        else{
            this.connected=false;
        }
    }

    /**
     * Returns if the link is activated or not
     * 
     * @return if the link is activated or not
     */
    @Override
    public boolean isActivated() {
        return activated;
    }

    /**
     * Sets the link as activated/deactivated
     * 
     * @param activated the status activated/deactivated of the link
     */
    @Override
    public void setActivated(boolean activated) {
        this.activated = activated;
        if(activated){
            this.getStartNode().addLink(this);
            this.getEndNode().addLink(this);
        }
        else{
            this.setFlow(0.0);
            this.getStartNode().removeLink(this);
            this.getEndNode().removeLink(this);
        }
    }
    
    /**
     * Returns the flow of the link if a flow type is defined
     * 
     * @return the flow of the link
     */
    @Override
    public double getFlow(){
        if(this.flowType==null)
            logger.debug("Flow type is not defined.");
        return Math.abs((double)this.getProperty(flowType));
    }
    
    /**
     * Sets the flow of the link if a flow type is defined
     * 
     * @param flow the flow of the link
     */
    @Override
    public void setFlow(double flow){
        if(this.flowType==null){
            logger.debug("Flow type is not defined.");
        }
        else{
            this.addProperty(flowType, flow);
        }
    }
    
    /**
     * Returns the capacity of the link if a capacity type is defined
     * 
     * @return the capacity of the link
     */
    @Override
    public double getCapacity(){
        if(this.capacityType==null)
            logger.debug("Capacity type is not defined.");
        return Math.abs((double)this.getProperty(capacityType));
    }
    
    /**
     * Sets the capacity of the link if a capacity type is defined
     * 
     * @param capacity the capacity of the link
     */
    @Override
    public void setCapacity(double capacity){
        if(this.capacityType==null){
            logger.debug("Capacity type is not defined.");
        }
        else{
            this.addProperty(capacityType, capacity);
        }
    }
    
    /**
     * Sets the flow type of the link
     * 
     * @param flowType the flow type of the link
     */
    @Override
    public void setFlowType(Enum flowType){
        this.flowType=flowType;
    }
    
    /**
     * Sets the capacity type of the link
     * 
     * @param capacityType the capacity type of the link
     */
    @Override
    public void setCapacityType(Enum capacityType){
        this.capacityType=capacityType;
    }
    
}
