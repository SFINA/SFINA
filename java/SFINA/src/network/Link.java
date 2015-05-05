/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import dsutil.generic.state.State;
import dsutil.protopeer.FingerDescriptor;
import java.util.UUID;

/**
 *
 * @author evangelospournaras
 */
public class Link extends State{
    
    private String index;
    private Node startNode;
    private Node endNode;
    private boolean connected;
    
    public Link(String index, Node startNodeID, Node endNodeID, boolean isolated){
        super();
        this.index=index;
        this.startNode=startNode;
        this.endNode=endNode;
        this.connected=connected;
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
    }

    /**
     * @return the connected
     */
    public boolean isConnected() {
        return connected;
    }
    
}
