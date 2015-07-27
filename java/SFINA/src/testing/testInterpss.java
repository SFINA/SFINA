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
import power.flow_analysis.InterpssPowerFlowAnalysis;

/**
 *
 * @author Ben
 */
public class testInterpss {
    FlowNetwork net;
    
    public testInterpss(FlowNetwork net) {
        this.net = net;
        
    }
    
    public void compareData(){
            InterpssPowerFlowAnalysis IpssObject = new InterpssPowerFlowAnalysis(PowerFlowType.AC);
            try{
                IpssObject.compareDataToCaseLoaded(net, "ieee57.ieee");
            }
            catch(InterpssException ie){
                ie.printStackTrace();
            }
            System.out.println(">>>>> Compared data from IEEE file from SFINA loaders.");
            System.out.println("\n--------------------------------------------------\n    INTERPSS TESTING SUCCESSFUL\n--------------------------------------------------\n");
        }

    public void compareResults(){
            InterpssPowerFlowAnalysis IpssObject = new InterpssPowerFlowAnalysis(PowerFlowType.AC);
            try {
                IpssObject.compareLFResultsToCaseLoaded(net, "ieee57.ieee");
            } catch (InterpssException ie) {
                ie.printStackTrace();
            }
            System.out.println(">>>>> Compared LF results for IEEE data loaded and SFINA data directly.");
            System.out.println("\n--------------------------------------------------\n    INTERPSS TESTING SUCCESSFUL\n--------------------------------------------------\n");
        }

    
    public void runRealInterss(){
            InterpssPowerFlowAnalysis IpssObject = new InterpssPowerFlowAnalysis(PowerFlowType.AC);
            IpssObject.flowAnalysis(net);
            System.out.println("\n--------------------------------------------------\n    INTERPSS TESTING SUCCESSFUL\n--------------------------------------------------\n");
    }
}
