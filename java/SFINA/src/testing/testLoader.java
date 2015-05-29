/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testing;

import java.util.ArrayList;
import input.TopologyLoader;
import input.InputParametersLoader;
import power.input.PowerMetaInfoLoader;
import network.Link;
import network.Node;

/**
 *
 * @author Ben
 */
public class testLoader {
    public static void main(String[] args){
        String col_seperator = ",";
        String nodelocation = "configuration_files/input/time_1/topology/nodes.txt";
        String linklocation = "configuration_files/input/time_1/topology/links.txt";
        String nodemetalocation = "configuration_files/input/time_1/flow/nodes.txt";
        String linkmetalocation = "configuration_files/input/time_1/flow/links.txt";

        // Load topology
        TopologyLoader topologyloader = new TopologyLoader(col_seperator);
        ArrayList<Node> nodes = topologyloader.loadNodes(nodelocation);
        ArrayList<Link> links = topologyloader.loadLinks(linklocation, nodes);

        // Load meta
        PowerMetaInfoLoader metaloader = new PowerMetaInfoLoader(col_seperator);
        metaloader.loadNodeMetaInfo(nodemetalocation, nodes);
        //metaloader.loadLinkMetaInfo(linkmetalocation, links);
       
        // Print information to see if it worked
        System.out.println("---------- Nodes -----------");
        for(Node node : nodes){
            System.out.println("Node " + node.getIndex() + "; Connected = " + node.isConnected());
        }
        System.out.println("---------- Links -----------");
        for(Link link : links){
            System.out.println("Link " + link.getIndex() + "; Connected = " + link.isConnected());
        }
        System.out.println("Loading successful");
    }
}
