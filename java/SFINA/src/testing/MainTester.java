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
import power.PowerNodeType;
import power.input.PowerLinkState;
import power.input.PowerNodeState;

/**
 *
 * @author Ben
 */
public class MainTester {
    
    public static void main(String[] args){
        
        // Create network object
        FlowNetwork net = new FlowNetwork();
        Output printer1 = new Output(net);
        
        // Test Loader. Argument true if loaded data should be printed to Output
        testLoader testloader = new testLoader(net);
        //testloader.printLoadedData();

        // Test Matpower
        testMatpower testmatpwr = new testMatpower(net);
        printer1.printLfResults(); 
        
        // Test InterPSS
        testInterpss testinterpss = new testInterpss(net);
//        //testinterpss.compareData();
//        testinterpss.compareResults();
//        System.out.println("========= LF results when directly loaded from InterPSS case file =========");
//        printer.printLfResults();
//        
        System.out.println("========= LF results when our data passed =========");
        FlowNetwork net2 = new FlowNetwork(); // Load data again to avoid powerflow simu from before to affect it
        Output printer2 = new Output(net);
        testLoader testloader2 = new testLoader(net2); 
        testinterpss.runRealInterpss();
        printer2.printLfResults();
      
        // Test Island Extraction
        testNetworkMethods testnetmethods = new testNetworkMethods(net);
        //String[] removeLinks = {"8","15","16","17","18","19","20","21","22","41","80"};
        //testnetmethods.testIslandFinder(removeLinks);
        //testnetmethods.testMetrics();
    }
    
}
