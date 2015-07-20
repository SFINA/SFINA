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
import network.Node;

/**
 *
 * @author Ben
 */
public class testNetworkMethods {
    FlowNetwork net;
    
    public testNetworkMethods(FlowNetwork net){
        this.net = net;
    }
    
    public void testIslandFinder(String[] removedLinks){
        // Remove some branches to create islands
        for (String el : removedLinks)
            net.deactivateLink(el);
        
        ArrayList<ArrayList<Node>> islands = net.getIslands();
        System.out.println("\n--------------------------------------------------\n    FOUND " + islands.size() + " ISLAND(S)\n--------------------------------------------------\n");
        for (int i=0; i < islands.size(); i++){
            System.out.println("Island " + i);
            for (Node node : islands.get(i)){
                System.out.format("%5s", node.getIndex());
            }
            System.out.println("\n");
        }
    }
    
    public void testMetrics(){
        System.out.println("\n--------------------------------------------------\n    METRIC TESTING NOT YET IMPLEMENTED\n--------------------------------------------------\n");
    }
}
