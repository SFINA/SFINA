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
package testing;

import java.util.ArrayList;
import network.FlowNetwork;
import network.Link;
import network.Node;
import power.input.PowerNodeType;
import power.backend.InterpssFlowBackend;
import power.input.PowerNodeState;
import power.output.PowerConsoleOutput;

/**
 *
 * @author Ben
 */
public class testNetworkMethods {
    static FlowNetwork net;
    
    public static void main(String[] args){
        testLoader loader = new testLoader();
        net = new FlowNetwork();
        loader.load("case57", net);
        String[] removeLinks = {"8","15","16","17","18","19","20","21","22","41","80"};
        //net.getNode("9").replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.SLACK_BUS);
        //String[] removeLinks = {};
        testIslandFinder(removeLinks);
        
        //InterpssFlowBackend IpssObject = new InterpssFlowBackend(FlowType);
        //IpssObject.flowAnalysis(net);
        
        //PowerConsoleOutput printer = new PowerConsoleOutput();
        //printer.printLfResults(net);

        //testMetrics();
    }
    
    public static void testIslandFinder(String[] removedLinks){
        // Remove some branches to create islands
        for (String el : removedLinks)
            net.deactivateLink(el);
        
        ArrayList<FlowNetwork> islands = net.computeIslands();
        
        for (Link link : net.getLinks())
            System.out.format("%3s", link.getIndex());
        
        System.out.println("\n--------------------------------------------------\n    FOUND " + islands.size() + " ISLAND(S)\n--------------------------------------------------\n");
        for (int i=0; i < islands.size(); i++){
            System.out.print("--> Island " + (i+1) + "\nNodes: ");
            for (Node node : islands.get(i).getNodes())
                System.out.format("%3s", node.getIndex());
            System.out.print("\nLinks: ");
            for (Link link : islands.get(i).getLinks())
                System.out.format("%3s", link.getIndex());
            System.out.println("\n\n");
        }
    }
    
    public static void testMetrics(){
        System.out.println("\n--------------------------------------------------\n    METRIC TESTING\n--------------------------------------------------\n");
        System.out.format("%-30s%-10.4f\n", "Average Node Degree:", net.getAvgNodeDegree());
        System.out.format("%-30s%-30s\n", "Degree Distribution:", net.getDegreeDist().toString());
        System.out.format("%-30s%-10.4f\n", "Clustering Coefficient:", net.getClustCoeff());
        System.out.format("%-30s%-10.4f\n", "Average shortest path length:", net.getAvgShortestPath());
        System.out.format("%-30s%-10.4f\n", "Closeness centrality node 5:", net.getClosenessCentrality(net.getNode("5")));
        Node startNode = net.getNode("9");
        Node endNode = net.getNode("30");
        ArrayList<Link> path = net.getShortestPath(startNode, endNode);
        String pathString = "";
        if(path != null){
            for (Link link : path)
                pathString += "(" + link.getStartNode().getIndex() + ":" + link.getEndNode().getIndex() + "), ";
        }
        else
            pathString = "No path found.";
        System.out.format("%-30s%-30s\n", "Shortest path (Nodes " + startNode.getIndex() + "->" + endNode.getIndex() + "):", pathString);
    }
}
