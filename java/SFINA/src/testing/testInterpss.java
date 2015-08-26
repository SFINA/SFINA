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

import com.interpss.common.exp.InterpssException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.FlowNetwork;
import network.Link;
import network.Node;
import power.PowerFlowType;
import power.flow_analysis.InterpssFlowBackend;

/**
 *
 * @author Ben
 */
public class testInterpss {
    FlowNetwork net;
    PowerFlowType FlowType;
    
    public testInterpss(FlowNetwork net, PowerFlowType FlowType) {
        this.net = net;
        this.FlowType = FlowType;
    }
    
    public void runInterpssOurData(){
        InterpssFlowBackend IpssObject = new InterpssFlowBackend(FlowType);
        IpssObject.flowAnalysis(net);
        System.out.println(">>>>> InterPSS flow analysis with SFINA data conversion.");
    }

    public void runInterpssTheirLoader(){
        InterpssFlowBackend IpssObject = new InterpssFlowBackend(FlowType);
        try {
            IpssObject.flowAnalysisIpssDataLoader(net, "ieee57.ieee");
        } catch (InterpssException ie) {
            ie.printStackTrace();
        }
        System.out.println(">>>>> InterPSS flow analysis with data loaded by InterPSS directly.");
    }    
    
    public void compareData(){
        InterpssFlowBackend IpssObject = new InterpssFlowBackend(FlowType);
        try{
            IpssObject.compareDataToCaseLoaded(net, "ieee57.ieee");
        }
        catch(InterpssException ie){
            ie.printStackTrace();
        }
        System.out.println(">>>>> Compared data from IEEE file from SFINA loaders.");
        System.out.println("\n--------------------------------------------------\n    INTERPSS TESTING SUCCESSFUL\n--------------------------------------------------\n");
    }

}
