/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import dsutil.generic.state.State;
import dsutil.protopeer.FingerDescriptor;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author evangelospournaras
 */
public class Node {
    
    public static String id=UUID.randomUUID().toString();
    private String index;
    private List<String> incomingLinks;
    private List<String> outgoingLinks;
    private boolean connected;
    public State state;
    
    public Node(String index){
        this.index=index;
        this.connected=false;
    }
    
    public void addLink(Link link){
        
    }
    
    public void removeLink(Link link){
        
    }
    
    public boolean isConnected(){
        return connected;
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
     * @return the incomingLinks
     */
    public List<String> getIncomingLinks() {
        return incomingLinks;
    }

    /**
     * @param incomingLinks the incomingLinks to set
     */
    public void setIncomingLinks(List<String> incomingLinks) {
        this.incomingLinks = incomingLinks;
    }

    /**
     * @return the outgoingLinks
     */
    public List<String> getOutgoingLinks() {
        return outgoingLinks;
    }

    /**
     * @param outgoingLinks the outgoingLinks to set
     */
    public void setOutgoingLinks(List<String> outgoingLinks) {
        this.outgoingLinks = outgoingLinks;
    }
    
    
}
