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
package power.output;

import java.util.ArrayList;
import network.FlowNetwork;
import network.Link;
import network.Node;
import power.input.PowerNodeType;
import power.input.PowerLinkState;
import power.input.PowerNodeState;
import static protopeer.time.EventScheduler.logger;

/**
 *
 * @author Ben
 */
public class PowerConsoleOutput {
    private FlowNetwork net;
    
    public PowerConsoleOutput(){
        
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
                System.out.format(busFormatter, node.getIndex(), node.getProperty(PowerNodeState.VOLTAGE_MAGNITUDE), node.getProperty(PowerNodeState.VOLTAGE_ANGLE), "-","-", node.getProperty(PowerNodeState.POWER_DEMAND_REAL), node.getProperty(PowerNodeState.POWER_DEMAND_REACTIVE));
            else
                System.out.format(genFormatter, node.getIndex(), node.getProperty(PowerNodeState.VOLTAGE_MAGNITUDE), node.getProperty(PowerNodeState.VOLTAGE_ANGLE), node.getProperty(PowerNodeState.POWER_GENERATION_REAL), node.getProperty(PowerNodeState.POWER_GENERATION_REACTIVE), node.getProperty(PowerNodeState.POWER_DEMAND_REAL), node.getProperty(PowerNodeState.POWER_DEMAND_REACTIVE));
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
            //double lossReal = Math.abs(Math.abs((Double)link.getProperty(PowerLinkState.POWER_FLOW_TO_REAL)) - Math.abs((Double)link.getProperty(PowerLinkState.POWER_FLOW_FROM_REAL)));
            //double lossReactive = (Double)link.getProperty(PowerLinkState.POWER_FLOW_TO_REACTIVE) + (Double)link.getProperty(PowerLinkState.POWER_FLOW_FROM_REACTIVE);
            //lossReactive = 0.0;
            System.out.format(braFormatter, link.getIndex(), link.getStartNode().getIndex(), link.getEndNode().getIndex(), link.getProperty(PowerLinkState.POWER_FLOW_FROM_REAL), link.getProperty(PowerLinkState.POWER_FLOW_FROM_REACTIVE), link.getProperty(PowerLinkState.POWER_FLOW_TO_REAL), link.getProperty(PowerLinkState.POWER_FLOW_TO_REACTIVE), link.getProperty(PowerLinkState.LOSS_REAL), link.getProperty(PowerLinkState.LOSS_REACTIVE));
            if (link.getProperty(PowerLinkState.LOSS_REAL) != null) {
                lossRealTot += Math.abs((Double)link.getProperty(PowerLinkState.LOSS_REAL));
                lossReacTot += Math.abs((Double)link.getProperty(PowerLinkState.LOSS_REACTIVE));
            }
        }
        System.out.format(braHeaderFormatter, dashes, dashes, dashes, dashes, dashes, dashes, dashes, dashes, dashes);
        System.out.format("%" + 6*col + "s%" + col + "s%" + col + ".3f%" + col + ".2f\n", "" , "Total:", lossRealTot, lossReacTot);
        
    }
    
    public void compareLFResults(FlowNetwork net1, FlowNetwork net2){
        if(net1.getNodes().size() != net2.getNodes().size() || net1.getLinks().size() != net2.getLinks().size()){
            logger.debug("Comparing different networks not possible.");
            return;
        }
        int col = 10;
        String dashes = "";
        for (int i=0; i<col-1;i++)
             dashes += "-";
        
        String busHeaderFormatter = "%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s\n";
        String genFormatter = "%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s\n";
        String busFormatter = "%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s\n";

        System.out.println();
        System.out.format("%" + 2*col + "s\n", "BUS DATA DIFFERENCE");
        System.out.format(busHeaderFormatter, dashes, dashes, dashes, dashes, dashes, dashes, dashes);
        System.out.format("%" + col + "s%-" + 2*col + "s%-" + 2*col + "s%-" + 2*col + "s\n", "Bus", "       Voltage", "      Generation", "         Load");
        System.out.format(busHeaderFormatter, "id", "Mag(pu)", "Ang(deg)", "P (MW)", "Q (MVAr)", "P (MW)", "Q (MVAr)");
        System.out.format(busHeaderFormatter, dashes, dashes, dashes, dashes, dashes, dashes, dashes);
        for (Node node : net1.getNodes()){
            Node node2 = net2.getNode(node.getIndex());
            if ((PowerNodeType)node.getProperty(PowerNodeState.TYPE) == PowerNodeType.BUS)
                System.out.format(busFormatter, node.getIndex(), getRelative((Double)node.getProperty(PowerNodeState.VOLTAGE_MAGNITUDE), (Double)node2.getProperty(PowerNodeState.VOLTAGE_MAGNITUDE)), getRelative((Double)node.getProperty(PowerNodeState.VOLTAGE_ANGLE), (Double)node2.getProperty(PowerNodeState.VOLTAGE_ANGLE)), "-","-", getRelative((Double)node.getProperty(PowerNodeState.POWER_DEMAND_REAL), (Double)node2.getProperty(PowerNodeState.POWER_DEMAND_REAL)), getRelative((Double)node.getProperty(PowerNodeState.POWER_DEMAND_REACTIVE), (Double)node2.getProperty(PowerNodeState.POWER_DEMAND_REACTIVE)));
            else
                System.out.format(genFormatter, node.getIndex(), getRelative((Double)node.getProperty(PowerNodeState.VOLTAGE_MAGNITUDE), (Double)node2.getProperty(PowerNodeState.VOLTAGE_MAGNITUDE)), getRelative((Double)node.getProperty(PowerNodeState.VOLTAGE_ANGLE), (Double)node2.getProperty(PowerNodeState.VOLTAGE_ANGLE)), getRelative((Double)node.getProperty(PowerNodeState.POWER_GENERATION_REAL), (Double)node2.getProperty(PowerNodeState.POWER_GENERATION_REAL)), getRelative((Double)node.getProperty(PowerNodeState.POWER_GENERATION_REACTIVE), (Double)node2.getProperty(PowerNodeState.POWER_GENERATION_REACTIVE)), getRelative((Double)node.getProperty(PowerNodeState.POWER_DEMAND_REAL), (Double)node2.getProperty(PowerNodeState.POWER_DEMAND_REAL)), getRelative((Double)node.getProperty(PowerNodeState.POWER_DEMAND_REACTIVE), (Double)node2.getProperty(PowerNodeState.POWER_DEMAND_REACTIVE)));
        }
        System.out.format(busHeaderFormatter, dashes, dashes, dashes, dashes, dashes, dashes, dashes);

        
        // Links
        String braHeaderFormatter = "%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s\n";
        String braFormatter = "%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s%" + col + "s\n";

        System.out.println();
        System.out.format("%" + 2*col + "s\n", "BRANCH DATA DIFFERENCE");
        System.out.format(braHeaderFormatter, dashes, dashes, dashes, dashes, dashes, dashes, dashes, dashes, dashes);
        System.out.format("%" + col + "s%" + col + "s%" + col + "s%" + 2*col + "s%" + 2*col + "s%" + 2*col + "s\n", "Branch", "From", "To", "From Bus Injection", "To Bus Injection",  "Loss");
        System.out.format(braHeaderFormatter, "id", "Bus", "Bus", "P (MW)", "Q (MVAr)", "P (MW)", "Q (MVAr)", "P (MW)", "Q (MVAr)");
        System.out.format(braHeaderFormatter, dashes, dashes, dashes, dashes, dashes, dashes, dashes, dashes, dashes);
        double lossRealTot = 0.0;
        double lossReacTot = 0.0;
        for (Link link : net1.getLinks()){
            Link link2 = net2.getLink(link.getIndex());
            //double lossReal = Math.abs(Math.abs((Double)link.getProperty(PowerLinkState.POWER_FLOW_TO_REAL)) - Math.abs((Double)link.getProperty(PowerLinkState.POWER_FLOW_FROM_REAL)));
            //double lossReactive = (Double)link.getProperty(PowerLinkState.POWER_FLOW_TO_REACTIVE) + (Double)link.getProperty(PowerLinkState.POWER_FLOW_FROM_REACTIVE);
            //lossReactive = 0.0;
            System.out.format(braFormatter, link.getIndex(), link.getStartNode().getIndex(), link.getEndNode().getIndex(), getRelative((Double)link.getProperty(PowerLinkState.POWER_FLOW_FROM_REAL), (Double)link2.getProperty(PowerLinkState.POWER_FLOW_FROM_REAL)), getRelative((Double)link.getProperty(PowerLinkState.POWER_FLOW_FROM_REACTIVE), (Double)link2.getProperty(PowerLinkState.POWER_FLOW_FROM_REACTIVE)), getRelative((Double)link.getProperty(PowerLinkState.POWER_FLOW_TO_REAL), (Double)link2.getProperty(PowerLinkState.POWER_FLOW_TO_REAL)), getRelative((Double)link.getProperty(PowerLinkState.POWER_FLOW_TO_REACTIVE), (Double)link2.getProperty(PowerLinkState.POWER_FLOW_TO_REACTIVE)), getRelative((Double)link.getProperty(PowerLinkState.LOSS_REAL), (Double)link2.getProperty(PowerLinkState.LOSS_REAL)), getRelative((Double)link.getProperty(PowerLinkState.LOSS_REACTIVE), (Double)link2.getProperty(PowerLinkState.LOSS_REACTIVE)));
            if (link.getProperty(PowerLinkState.LOSS_REAL) != null) {
                lossRealTot += Math.abs((Double)link.getProperty(PowerLinkState.LOSS_REAL)) - Math.abs((Double)net2.getLink(link.getIndex()).getProperty(PowerLinkState.LOSS_REAL));
                lossReacTot += Math.abs((Double)link.getProperty(PowerLinkState.LOSS_REACTIVE)) - Math.abs((Double)net2.getLink(link.getIndex()).getProperty(PowerLinkState.LOSS_REACTIVE));
            }
//            if (link.getIndex().equals("14")){
//                System.out.println("!!!" + link.getProperty(PowerLinkState.POWER_FLOW_FROM_REAL) + " & " + link2.getProperty(PowerLinkState.POWER_FLOW_FROM_REAL) + "!!!");
//            }
        }
        System.out.format(braHeaderFormatter, dashes, dashes, dashes, dashes, dashes, dashes, dashes, dashes, dashes);
        System.out.format("%" + 6*col + "s%" + col + "s%" + col + ".3f%" + col + ".2f\n", "" , "Total:", lossRealTot, lossReacTot);    
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
    private String getRelative(double val1, double val2){
        double result = Math.abs((val1-val2)/val1);
        result = Math.round(result*10000)/100.0d;
        if (result == 0.0)
            return "-";
        if (result > 1000)
            return ">1000%";
        else 
            return result + "%";
    }    
}
