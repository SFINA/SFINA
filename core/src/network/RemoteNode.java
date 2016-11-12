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
public class RemoteNode extends State implements NodeInterface{
    
    private String index;
    private boolean activated;
    private List<Link> links;
    private List<InterdependentLink> interdependentLinks;
    private boolean connected;
    private Integer networkIndex;
    private static final Logger logger = Logger.getLogger(RemoteNode.class);
    
    /**
     * Instantiates a disconnected node. This is the recommended constructor as the addition
     * of the links should be performed by a <code>FlowNetworkInterface</code>.
     * 
     * @param index the index of the node
     * @param activated initialized as activated or deactivated
     * @param networkIndex of the global node
     */
    public RemoteNode(String index, boolean activated, Integer networkIndex){
        super();
        this.index=index;
        this.activated=activated;
        this.connected=false;
        this.links=new ArrayList<>();
        this.interdependentLinks=new ArrayList<>();
        this.networkIndex = networkIndex;
        this.evaluateConnectivity();
    }
    
    /**
     * Returns the index of the node
     * 
     * @return the index of the node
     */
    @Override
    public String getIndex() {
        return index;
    }

    /**
     * Sets the index of the node
     * 
     * @param index the index to set
     */
    @Override
    public void setIndex(String index) {
        this.index = index;
    }
    
    /**
     * @return the networkIndex
     */
    @Override
    public Integer getNetworkIndex() {
        return networkIndex;
    }

    /**
     * @param networkIndex the networkIndex to set
     */
    @Override
    public void setNetworkIndex(Integer networkIndex) {
        this.networkIndex = networkIndex;
    }
    
    /**
     * Returned the activated/deactivated status of the node
     * 
     * @return the activated/deactivated status of the node
     */
    @Override
    public boolean isActivated() {
        return activated;
    }
    
    @Override
    public boolean isRemoteNode(){
        return this instanceof RemoteNode;
    }
    
    /**
     * Adds a link and evaluates the connectivity of the node.
     * 
     * @param link the added link
     */
    @Override
    public void addLink(LinkInterface link) {
        if (link.isInterdependent() && !this.getInterdependentLinks().contains((InterdependentLink)link)) {
            this.getInterdependentLinks().add((InterdependentLink)link);
        } else if (!link.isInterdependent() && !this.getLinks().contains((Link)link)){
            this.getLinks().add((Link)link);
        }
        this.evaluateConnectivity();
    }

    /**
     * Removes the link and evaluates the connectivity of the node
     * 
     * @param link the removed link
     */
    @Override
    public void removeLink(LinkInterface link){
        if(link.isInterdependent())
            this.getInterdependentLinks().remove((InterdependentLink)link);
        else
            this.getLinks().remove((Link)link);
        this.evaluateConnectivity();
    }
    
    /**
     * Returns a new list with all incoming links of the node
     * 
     * @return a new array list with the incoming links of the node
     */
    @Override
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
    @Override
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
     * Returns a new list with all incoming links of the node
     * 
     * @return a new array list with the incoming links of the node
     */
    @Override
    public ArrayList<InterdependentLink> getIncomingInterdependentLinks(){
        ArrayList<InterdependentLink> incomingLinks=new ArrayList<>();
        for(InterdependentLink link:getInterdependentLinks()){
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
    @Override
    public ArrayList<InterdependentLink> getOutgoingInterdependentLinks(){
        ArrayList<InterdependentLink> outgoingLinks=new ArrayList<>();
        for(InterdependentLink link:getInterdependentLinks()){
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
    @Override
    public boolean isConnected(){
        return connected;
    }


    /**
     * Returns the links of the node
     * 
     * @return the links of the node
     */
    @Override
    public List<Link> getLinks() {
        return links;
    }
    
    @Override
    public List<InterdependentLink> getInterdependentLinks(){
        return interdependentLinks;
    }

    /**
     * Sets the links of the node
     * 
     * @param links the links to set
     */
    @Override
    public void setLinks(List<Link> links) {
        this.links = links;
        this.evaluateConnectivity();
    }
    
    /**
     * Evaluates the connectivity of the node
     */
    protected void evaluateConnectivity(){
        if(!getLinks().isEmpty()){
            this.connected=true;
        }
        else{
            this.connected=false;
        }
    }

    /**
     * Sets the activated/deactivated status of the node
     * 
     * @param activated the activated to set
     */
    @Override
    public void setActivated(boolean activated) {
        if(activated != this.isActivated()){
            this.activated = activated;
            for(Link link : this.getLinks())
                link.evaluateConnectivity();
            for(Link link : this.getInterdependentLinks())
                link.evaluateConnectivity();
        }
        else
            logger.debug("Not changing activation status of Node. Already " + activated);
    }
}