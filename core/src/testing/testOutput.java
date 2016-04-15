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

import network.FlowNetwork;
import output.TopologyWriter;
import power.output.PowerFlowWriter;

/**
 *
 * @author Ben
 */
public class testOutput {
    private static FlowNetwork net = new FlowNetwork();
    
    public static void main(String[] args){
        testLoader loader = new testLoader();
        loader.load("case14", net);
        String colSeparator = ",";
        String missingValue = "-";
        
        String nodeTopLocation = "configuration_files/output/time_1/topology/nodes.txt";
        String linkTopLocation = "configuration_files/output/time_1/topology/links.txt";
        String nodeFlowLocation = "configuration_files/output/time_1/flow/nodes.txt";
        String linkFlowLocation = "configuration_files/output/time_1/flow/links.txt";
        
        TopologyWriter topOut = new TopologyWriter(net,colSeparator);
        topOut.writeNodes(nodeTopLocation);
        topOut.writeLinks(linkTopLocation);
        
        PowerFlowWriter flowOut = new PowerFlowWriter(net,colSeparator,missingValue);
        flowOut.writeNodeFlowData(nodeFlowLocation);
        flowOut.writeLinkFlowData(linkFlowLocation);
        
    }
}
