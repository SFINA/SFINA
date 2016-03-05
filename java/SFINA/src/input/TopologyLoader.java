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
package input;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import network.FlowNetwork;
import network.Link;
import network.LinkState;
import network.Node;
import network.NodeState;
import org.apache.log4j.Logger;

/**
 *
 * @author evangelospournaras
 */
public class TopologyLoader {
    
    private FlowNetwork net;
    private String columnSeparator;
    private static final Logger logger = Logger.getLogger(TopologyLoader.class);
    
    public TopologyLoader(FlowNetwork net, String columnSeparator){
        this.net=net;
        this.columnSeparator=columnSeparator;
        
        // empty network if it has nodes and links
        emptyNetwork();
    }
    
    private void emptyNetwork(){
        if (net.getLinks().size() != 0){
            ArrayList<Link> links = new ArrayList<Link>(net.getLinks());
            for (Link link : links)
                net.removeLink(link);
        }
        if (net.getNodes().size() != 0){
            ArrayList<Node> nodes = new ArrayList<Node>(net.getNodes());
            for (Node node : nodes)
                net.removeNode(node);
        }
    }
    
    public void loadNodes(String location){
        ArrayList<NodeState> nodeStates=new ArrayList<NodeState>();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            if(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), columnSeparator);
                while(st.hasMoreTokens()){
                    // Here properties from NodeState like ID are added, but later no values are assigned (id==null), because the internal variables are used.
                    NodeState nodeState=this.lookupNodeState(st.nextToken());
                    nodeStates.add(nodeState);
                }
            }
            while(scr.hasNext()){
                ArrayList<String> values=new ArrayList<String>();
                StringTokenizer st = new StringTokenizer(scr.next(), columnSeparator);
                while(st.hasMoreTokens()){
                    values.add(st.nextToken());
                }
                String nodeIndex=(String)this.getActualNodeValue(nodeStates.get(0), values.get(0));
                boolean status=(Boolean)this.getActualNodeValue(nodeStates.get(1), values.get(1));
                Node node=new Node(nodeIndex, status);
                net.addNode(node);
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    public void loadLinks(String location){
        ArrayList<Node> nodes = new ArrayList<Node>(net.getNodes());
        ArrayList<LinkState> linkStates=new ArrayList<LinkState>();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            if(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), columnSeparator);
                while(st.hasMoreTokens()){
                    // Same as for nodes, here properties like ID, from_node, to_node, etc are added but never used.
                    LinkState linkState=this.lookupLinkState(st.nextToken());
                    linkStates.add(linkState);
                }
            }
            while(scr.hasNext()){
                ArrayList<String> values=new ArrayList<String>();
                StringTokenizer st = new StringTokenizer(scr.next(), columnSeparator);
                while(st.hasMoreTokens()){
                    values.add(st.nextToken());
                }
                String linkIndex=(String)this.getActualLinkValue(linkStates.get(0), values.get(0));
                String startIndex=(String)this.getActualLinkValue(linkStates.get(1), values.get(1));
                String endIndex=(String)this.getActualLinkValue(linkStates.get(2), values.get(2));
                boolean status=(Boolean)this.getActualLinkValue(linkStates.get(3), values.get(3));
                Node startNode=null;
                Node endNode=null;
                for(Node node:nodes){
                    if(startIndex.equals(node.getIndex())){
                        startNode=node;
                    }
                    if(endIndex.equals(node.getIndex())){
                        endNode=node;
                    }
                }
                if(startNode!=null && endNode!=null){
                    Link link=new Link(linkIndex,status,startNode,endNode);
                    net.addLink(link);
                }
                else{
                    logger.debug("Something went wrong with the indices of nodes and links.");
		}
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    private Object getActualNodeValue(NodeState nodeState, String rawValue){
        switch(nodeState){
            case ID:
                return rawValue;
            case STATUS:
                switch(rawValue){
                    case "1":
                        return true;
                    case "0":
                        return false;
                    default:
                        logger.debug("Something is wrong with status of the NODES.");
                }
            default:
                logger.debug("Node state is not recognized.");
                return null;
        }    
    } 
    
    private Object getActualLinkValue(LinkState linkState, String rawValue){
        switch(linkState){
            case ID:
                return rawValue;
            case FROM_NODE:
                return rawValue;
            case TO_NODE:
                return rawValue;
            case STATUS:
                switch(rawValue){
                    case "1":
                        return true;
                    case "0":
                        return false;
                    default:
                        logger.debug("Something is wrong with status of the links.");
                }
            default:
                logger.debug("Link state is not recognized.");
                return null;
        }    
    }
    
    private NodeState lookupNodeState(String nodeState){
        switch(nodeState){
            case "id": 
                return NodeState.ID;
            case "status":
                return NodeState.STATUS;
            default:
                logger.debug("Node state is not recognized.");
                return null;
        }
    }
    
    private LinkState lookupLinkState(String linkState){
        switch(linkState){
            case "id":
                return LinkState.ID;
            case "from_node_id":;
                return LinkState.FROM_NODE;
            case "to_node_id":
                return LinkState.TO_NODE;
            case "status":
                return LinkState.STATUS;
            default:
                logger.debug("Link state is not recognized.");
                return null;
        }
    }
}
