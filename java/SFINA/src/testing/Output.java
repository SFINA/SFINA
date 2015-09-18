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
import power.PowerNodeType;
import power.input.PowerLinkState;
import power.input.PowerNodeState;

/**
 *
 * @author Ben
 */
public class Output {
    FlowNetwork net;
    
    public Output(){
    }
    
    public void printNodesAll(FlowNetwork net){
        this.net = net;
        ArrayList<Node> nodes = new ArrayList<Node>(net.getNodes());
        
        System.out.println("\n-------------------------\n    NODES\n-------------------------\n");
        System.out.format("%15s%15s%15s", "Index", "isActivated", "isConnected");
        for (PowerNodeState state : PowerNodeState.values()) {
            String printStateName = state.toString();
            if (printStateName.length() > 13)
                printStateName = printStateName.substring(0,13);
            System.out.format("%15s", printStateName);
        }
        System.out.print("\n");
        
        for(Node node : nodes){
            System.out.format("%15s%15s%15s", node.getIndex(), node.isActivated(), node.isConnected());
            for(PowerNodeState state : PowerNodeState.values()){
                if (node.getProperty(state) == null)
                    System.out.format("%15s", "--");
                else {
                    String printStateName = node.getProperty(state).toString();
                    if (printStateName.length() > 9)
                        printStateName = printStateName.substring(0,9);
                    System.out.format("%15s", printStateName);
                }
            }
            System.out.print("\n");
        }
    }
    
    public void printLinksAll(FlowNetwork net){
        this.net = net;
        ArrayList<Link> links = new ArrayList<Link>(net.getLinks());

        System.out.println("\n-------------------------\n    LINKS\n-------------------------\n");
        System.out.format("%15s%15s%15s%15s%15s", "Index", "StartNode", "EndNode", "isActivated", "isConnected");

        for(PowerLinkState state : PowerLinkState.values()) {
            String printStateName = state.toString();
            if (printStateName.length() > 13)
                printStateName = printStateName.substring(0,13);
            System.out.format("%15s", printStateName);
        }
        System.out.print("\n");

        for(Link link : links){
            System.out.format("%15s%15s%15s%15s%15s", link.getIndex(), link.getStartNode().getIndex(), link.getEndNode().getIndex(), link.isActivated(), link.isConnected());
            for(PowerLinkState state : PowerLinkState.values()){
                if (link.getProperty(state) == null)
                    System.out.format("%15s", "null");
                else {
                    String printStateName = link.getProperty(state).toString();
                    if (printStateName.length() > 9)
                        printStateName = printStateName.substring(0,9);
                    System.out.format("%15s", printStateName);
                }
            }
            System.out.print("\n");
        }
    }
    
    public void printLfResults(FlowNetwork net){
        this.net = net;
        // Nodes
        int col = 10;
        String dashes = "";
        for (int i=0; i<col-1;i++)
             dashes += "-";
        
        String busHeaderFormatter = "%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s\n";
        String genFormatter = "%" + col + "s%" + col + ".3f%" + col + ".3f%" + col + ".2f%" + col + ".2f%" + col + ".2f%" + col + ".2f\n";
        String busFormatter = "%" + col + "s%" + col + ".3f%" + col + ".3f%" + col + "s%" + col + "s%" + col + ".2f%" + col + ".2f\n";

        System.out.println();
        System.out.format("%" + 2*col + "s\n", "BUS DATA");
        System.out.format(busHeaderFormatter, dashes, dashes, dashes, dashes, dashes, dashes, dashes);
        System.out.format("%" + col + "s%-" + 2*col + "s%-" + 2*col + "s%-" + 2*col + "s\n", "Bus", "       Voltage", "      Generation", "         Load");
        System.out.format(busHeaderFormatter, "id", "Mag(pu)", "Ang(deg)", "P (MW)", "Q (MVAr)", "P (MW)", "Q (MVAr)");
        System.out.format(busHeaderFormatter, dashes, dashes, dashes, dashes, dashes, dashes, dashes);
        for (Node node : net.getNodes()){
            if ((PowerNodeType)node.getProperty(PowerNodeState.TYPE) == PowerNodeType.BUS)
                System.out.format(busFormatter, node.getIndex(), node.getProperty(PowerNodeState.VOLTAGE_MAGNITUDE), node.getProperty(PowerNodeState.VOLTAGE_ANGLE), "-","-", node.getProperty(PowerNodeState.REAL_POWER_DEMAND), node.getProperty(PowerNodeState.REACTIVE_POWER_DEMAND));
            else
                System.out.format(genFormatter, node.getIndex(), node.getProperty(PowerNodeState.VOLTAGE_MAGNITUDE), node.getProperty(PowerNodeState.VOLTAGE_ANGLE), node.getProperty(PowerNodeState.REAL_POWER_GENERATION), node.getProperty(PowerNodeState.REACTIVE_POWER_GENERATION), node.getProperty(PowerNodeState.REAL_POWER_DEMAND), node.getProperty(PowerNodeState.REACTIVE_POWER_DEMAND));
        }
        System.out.format(busHeaderFormatter, dashes, dashes, dashes, dashes, dashes, dashes, dashes);

        
        // Links
        String braHeaderFormatter = "%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s\n";
        String braFormatter = "%" + col + "s%" + col + "s%" + col + "s%" + col + ".2f%" + col + ".2f%" + col + ".2f%" + col + ".2f%" + col + ".3f%" + col + ".2f\n";

        System.out.println();
        System.out.format("%" + 2*col + "s\n", "BRANCH DATA");
        System.out.format(braHeaderFormatter, dashes, dashes, dashes, dashes, dashes, dashes, dashes, dashes, dashes);
        System.out.format("%" + col + "s%" + col + "s%" + col + "s%" + 2*col + "s%" + 2*col + "s%" + 2*col + "s\n", "Branch", "From", "To", "From Bus Injection", "To Bus Injection",  "Loss");
        System.out.format(braHeaderFormatter, "id", "Bus", "Bus", "P (MW)", "Q (MVAr)", "P (MW)", "Q (MVAr)", "P (MW)", "Q (MVAr)");
        System.out.format(braHeaderFormatter, dashes, dashes, dashes, dashes, dashes, dashes, dashes, dashes, dashes);
        double lossRealTot = 0.0;
        double lossReacTot = 0.0;
        for (Link link : net.getLinks()){
            //double lossReal = Math.abs(Math.abs((Double)link.getProperty(PowerLinkState.REAL_POWER_FLOW_TO)) - Math.abs((Double)link.getProperty(PowerLinkState.REAL_POWER_FLOW_FROM)));
            //double lossReactive = (Double)link.getProperty(PowerLinkState.REACTIVE_POWER_FLOW_TO) + (Double)link.getProperty(PowerLinkState.REACTIVE_POWER_FLOW_FROM);
            //lossReactive = 0.0;
            System.out.format(braFormatter, link.getIndex(), link.getStartNode().getIndex(), link.getEndNode().getIndex(), link.getProperty(PowerLinkState.REAL_POWER_FLOW_FROM), link.getProperty(PowerLinkState.REACTIVE_POWER_FLOW_FROM), link.getProperty(PowerLinkState.REAL_POWER_FLOW_TO), link.getProperty(PowerLinkState.REACTIVE_POWER_FLOW_TO), link.getProperty(PowerLinkState.LOSS_REAL), link.getProperty(PowerLinkState.LOSS_REACTIVE));
            if (link.getProperty(PowerLinkState.LOSS_REAL) != null) {
                lossRealTot += (Double)link.getProperty(PowerLinkState.LOSS_REAL);
                lossReacTot += (Double)link.getProperty(PowerLinkState.LOSS_REACTIVE);
            }
        }
        System.out.format(braHeaderFormatter, dashes, dashes, dashes, dashes, dashes, dashes, dashes, dashes, dashes);
        System.out.format("%" + 6*col + "s%" + col + "s%" + col + ".3f%" + col + ".2f\n", "" , "Total:", lossRealTot, lossReacTot);
        
    }
}
