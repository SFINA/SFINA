/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import dsutil.generic.state.State;
import org.apache.log4j.Logger;

/**
 *
 * @author evangelospournaras
 */
public class Link extends State{
    
    private String index;
    private Node startNode;
    private Node endNode;
    private boolean connected;
    private boolean activated;
    private Enum flowType;
    private static final Logger logger = Logger.getLogger(Link.class);
    
    public Link(String index, boolean activated){
        super();
        this.index=index;
        this.activated=activated;
        this.evaluateConnectivity();
    }
    
    public Link(String index, boolean activated, Node startNode, Node endNode){
        super();
        this.index=index;
        this.startNode=startNode;
        this.endNode=endNode;
        this.activated=activated;
        this.evaluateConnectivity();
    }
    
    public double getFlow(){
        if(this.flowType==null)
            logger.debug("Flow type is not defined.");
        return (double)this.getProperty(flowType);
    }
    
    public void setFlow(double flow){
        if(this.flowType==null){
            logger.debug("Flow type is not defined.");
        }
        else{
            this.addProperty(flowType, flow);
        }
    }
    
    public void setFlowType(Enum flowType){
        this.flowType=flowType;
    }

    /**
     * @return the index
     */
    public String getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * @return the startNode
     */
    public Node getStartNode() {
        return startNode;
    }

    /**
     * @param startNode the startNode to set
     */
    public void setStartNode(Node startNode) {
        this.startNode = startNode;
        this.evaluateConnectivity();
    }

    /**
     * @return the endNode
     */
    public Node getEndNode() {
        return endNode;
    }

    /**
     * @param endNode the endNode to set
     */
    public void setEndNode(Node endNode) {
        this.endNode = endNode;
        this.evaluateConnectivity();
    }

    /**
     * @return the connected
     */
    public boolean isConnected() {
        return connected;
    }
    
    private boolean hasStartNode(){
        if(this.startNode!=null){
            return true;
        }
        return false;
    }
    
    private boolean hasEndNode(){
        if(this.endNode!=null){
            return true;
        }
        return false;
    }
    
    private void evaluateConnectivity(){
        if(this.hasStartNode() && this.hasEndNode()){
            this.connected=true;
        }
        else{
            this.connected=false;
        }
    }

    /**
     * @return the activated
     */
    public boolean isActivated() {
        return activated;
    }

    /**
     * @param activated the activated to set
     */
    public void setActivated(boolean activated) {
        this.activated = activated;
    }
    
}