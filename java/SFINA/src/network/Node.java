/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import dsutil.generic.state.State;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author evangelospournaras
 */
public class Node extends State{
    
    private String index;
    private List<Link> links;
    private boolean connected;
    
    public Node(String index){
        super();
        this.index=index;
        this.connected=false;
        this.links=new ArrayList<Link>();
    }
    
    public void addLink(Link link){
        this.getLinks().add(link);
        this.connected=true;
    }
    
    public void removeLink(Link link){
        this.getLinks().remove(link);
        if(getLinks().size()==0)
            this.connected=false;
    }
    
    public ArrayList<Link> getIncomingLinks(){
        ArrayList<Link> incomingLinks=new ArrayList<Link>();
        for(Link link:getLinks()){
            if(link.getEndNode().equals(this)){
                incomingLinks.add(link);
            }
        }
        return incomingLinks;
    }
    
    public ArrayList<Link> getOutgoingLinks(){
        ArrayList<Link> outgoingLinks=new ArrayList<Link>();
        for(Link link:getLinks()){
            if(link.getStartNode().equals(this)){
                outgoingLinks.add(link);
            }
        }
        return outgoingLinks;
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
        if(links.size()!=0)
            this.connected=true;
    }
}
