/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

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
public class Node extends RemoteNode implements NodeInterface{
    
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
        super(index, activated, null);
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
        super(index, activated, null);
        this.setLinks(links);
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
     * Sets the activated/deactivated status of the node
     * 
     * @param activated the activated to set
     */
    @Override
    public void setActivated(boolean activated) {
        super.setActivated(activated);
        if(!activated)
            this.setFlow(0.0);
    }
}