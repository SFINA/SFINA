/*
 * Copyright (C) 2016 SFINA Team
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
package interdependent;

import java.util.ArrayList;
import java.util.Collection;
import network.FlowNetwork;
import network.Link;
import network.Node;
import org.apache.log4j.Logger;
import protopeer.network.NetworkAddress;

/**
 *
 * @author Ben
 */
public class InterdependentFlowNetwork extends FlowNetwork{
    
    private static final Logger logger = Logger.getLogger(InterdependentFlowNetwork.class);
    private final ArrayList<Node> interdependentNodes;
    
    public InterdependentFlowNetwork(){
        super();
        this.interdependentNodes=new ArrayList<>();
    }
    
    @Override
    public void addNode(Node node){
        this.interdependentNodes.add(node);
    }
    
    @Override
    public void removeNode(Node node){
        for(Link link:node.getIncomingLinks()){
            link.setEndNode(null);
        }
        for(Link link:node.getOutgoingLinks()){
            link.setStartNode(null);
        }
        this.interdependentNodes.remove(node);
    }
    
    @Override
    public Collection<Node> getNodes(){
        return interdependentNodes;
    }
    
    /**
     *
     * @param address
     * @return Links that point to this network
     */
    public Collection<Link> getIncomingInterLinks(NetworkAddress address){
        ArrayList<Link> incomingLinks = new ArrayList<>();
        for (Link link : this.getLinks()){
            if(link.getStartNode().getNetworkAddress().equals(address))
                incomingLinks.add(link);
        }
        return incomingLinks;
    }
    
    /**
     *
     * @param address
     * @return Links that point away from this network
     */
    public Collection<Link> getOutgoingInterLinks(NetworkAddress address){
        ArrayList<Link> outgoingLinks = new ArrayList<>();
        for (Link link : this.getLinks()){
            if(link.getEndNode().getNetworkAddress().equals(address))
                outgoingLinks.add(link);
        }
        return outgoingLinks;
    }
    
    @Override
    public String toString() {
        String string = "";
        string += "---- Interlinks ----\n";
        string += String.format("%-10s%-20s%-20s\n", "", "StartNode", "EndNode");
        string += String.format("%-10s%-10s%-10s%-10s%-10s%-10s\n", "Link ID", "ID", "Address", "ID", "Address", "Flow");
        for (Link link : this.getLinks()) {
            string += String.format("%-10s%-10s%-10s%-10s%-10s%-10s\n", link.getIndex(), link.getStartNode().getIndex(), link.getStartNode().getNetworkAddress(), link.getEndNode().getIndex(), link.getEndNode().getNetworkAddress(), link.getFlow());
        }
        return string += "--------------------";
    }
}
