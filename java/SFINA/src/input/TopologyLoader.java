/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package input;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import network.Link;
import network.Node;
import org.apache.log4j.Logger;

/**
 *
 * @author evangelospournaras
 */
public class TopologyLoader {
    
    private String columnSeparator;
    private static final Logger logger = Logger.getLogger(TopologyLoader.class);
    
    public TopologyLoader(String columnSeparator){
        this.columnSeparator=columnSeparator;
    }
    
    public ArrayList<Link> loadAttackedLinks(String location, List<Link> links){
        ArrayList<Link> attackedLinks=new ArrayList<Link>();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            while(scr.hasNext()){
                String attackedLinkIndex=scr.next();
                Link attackedLink=null;
                for(Link link:links){
                    if(attackedLinkIndex.equals(link.getIndex())){
                        attackedLink=link;
                    }
                }
                if(attackedLink!=null){
                    attackedLinks.add(attackedLink);
                }
                else{
                    logger.debug("Something is wrong with the index of the links.");
                }
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return attackedLinks;
    }
    
    public ArrayList<Node> loadNodes(String location){
        ArrayList<Node> nodes=new ArrayList<Node>();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            scr.next();
            while(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), columnSeparator);
                while (st.hasMoreTokens()) {
                    String nodeIndex=st.nextToken();
                    String status=st.nextToken();
                    boolean activated = false;
                    switch(status){
                        case "1":
                            activated=true;
                            break;
                        case "0":
                            activated=false;
                        default:
                            logger.debug("Something is wrong with status of the nodes.");
                    }
                    Node node=new Node(nodeIndex, activated);
                    nodes.add(node);
                    
		}
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return nodes;
    }
    
    public ArrayList<Link> loadLinks(String location, ArrayList<Node> nodes){
        ArrayList<Link> links=new ArrayList<Link>();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            scr.next();
            while(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), columnSeparator);
                String linkIndex=st.nextToken();
                String startIndex=st.nextToken();
                String endIndex=st.nextToken();
                boolean activated=Boolean.getBoolean(st.nextToken());
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
                    Link link=new Link(linkIndex,activated,startNode,endNode);
                    links.add(link);
                }
                else{
                    logger.debug("Something went wrong with the indices of nodes and links.");
		}
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return links;
    }
}
