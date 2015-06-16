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

/**
 *
 * @author evangelospournaras
 */
public class FlowNetwork extends State{
    
    private HashMap<String,Node> nodes;
    private HashMap<String,Link> links;
    
    public FlowNetwork(){
        
    }
    
    public Collection<Node> getNodes(){
        return nodes.values();
    }
    
    public Collection<Link> getLinks(){
        return links.values();
    }
    
    public void addNode(Node node){
        this.nodes.put(node.getIndex(), node);
    }
    
    public void addLink(Link link){
        this.links.put(link.getIndex(), link);
        link.getStartNode().addLink(link);
        link.getEndNode().addLink(link);
    }
    
    public void removeNode(Node node){
        if(!this.links.isEmpty()){
            for(Link link:links.values()){
                if(node.equals(link.getStartNode())){
                    link.setStartNode(null);
                }
                if(node.equals(link.getEndNode())){
                    link.setEndNode(null);
                }
            }
        }
        this.nodes.remove(node.getIndex());
    }
    
    public void removeLink(Link link){
        for(Node node:nodes.values()){
            node.getLinks().remove(link);
        }
        this.links.remove(link);
    }
    
    public Node getNode(String index){
        return this.nodes.get(index);
    }
    
    public Link getLink(String index){
        return links.get(index);
    }
    
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
    
    public void activateLink(String index){
        Link activatedLink=links.get(index);
        activatedLink.setActivated(true);
        activatedLink.getStartNode().addLink(activatedLink);
        activatedLink.getEndNode().addLink(activatedLink);
    }
    
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
    
    public void deactivateLink(String index){
        Link deactivatedLink=links.get(index);
        deactivatedLink.setActivated(false);
        deactivatedLink.getStartNode().removeLink(deactivatedLink);
        deactivatedLink.getEndNode().removeLink(deactivatedLink);
    }
}
