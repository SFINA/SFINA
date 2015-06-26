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
    public testInterpss(FlowNetwork net) {
        
        // AC test
        try{
            InterpssPowerFlowAnalysis IpssObject = new InterpssPowerFlowAnalysis(PowerFlowType.AC);
            IpssObject.flowAnalysis(net);
            IpssObject.compareToCaseLoaded("ieee57.ieee");
        }
        catch(InterpssException ie){
            ie.printStackTrace();
        }
        
        
        System.out.println("\n--------------------------------------------------\n    INTERPSS TESTING SUCCESSFUL\n--------------------------------------------------\n");
    }
}
