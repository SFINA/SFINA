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
    String col_seperator = ",";
    String nodelocation = "/";
    String linklocation = "/";
    String nodemetalocation = "/";
    TopologyLoader topologyLoader = new TopologyLoader(col_seperator);
    PowerMetaInfoLoader metaloader = new PowerMetaInfoLoader(col_seperator);
    ArrayList<Node> nodes = topologyLoader.loadNodes(nodelocation);
    ArrayList<Link> links = topologyLoader.loadLinks(linklocation, nodes);
    metaloader.loadNodeMetaInfo(nodemetalocation,nodes);
    
}
