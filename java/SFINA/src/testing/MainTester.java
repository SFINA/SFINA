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

import event.Event;
import input.EventLoader;
import input.InputParameter;
import java.util.ArrayList;
import java.util.HashMap;
import network.FlowNetwork;
import network.Link;
import network.Node;
import org.apache.log4j.Logger;
import power.PowerFlowType;
import power.PowerNodeType;
import power.input.PowerLinkState;
import power.input.PowerNodeState;

/**
 *
 * @author Ben
 */
public class MainTester {
    
    public static void main(String[] args){
        testLoader loader = new testLoader();
        Output printer = new Output();
        
        // Test Matpower
        FlowNetwork net1 = new FlowNetwork();
        loader.load("case30", net1);
        //testloader.printLoadedData(net1);
        //testMatpower testmatpwr = new testMatpower(net1, PowerFlowType.AC);
        printer.printLfResults(net1); 
        
        // Test InterPSS
//        FlowNetwork net2 = new FlowNetwork(); // Load data again to avoid powerflow simu from before to affect it
//        loader.load("case57", net2);
//        testInterpss testinterpss = new testInterpss(net2, PowerFlowType.DC);
//        testinterpss.runInterpssOurData();
//        //testinterpss.runInterpssTheirLoader();
//        //testinterpss.compareData();
//        printer.printLfResults(net2);
      
        // Test Island Extraction
        //testNetworkMethods testnetmethods = new testNetworkMethods(net);
        //String[] removeLinks = {"8","15","16","17","18","19","20","21","22","41","80"};
        //testnetmethods.testIslandFinder(removeLinks);
        //testnetmethods.testMetrics();
    }
    
}
