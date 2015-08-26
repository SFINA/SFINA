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
import java.util.Arrays;
import network.FlowNetwork;
import power.PowerFlowType;
import power.flow_analysis.MATPOWERFlowBackend;

/**
 *
 * @author Ben
 */
public class testMatpower {
    public testMatpower(FlowNetwork net, PowerFlowType FlowType){
        
        MATPOWERFlowBackend algo = new MATPOWERFlowBackend(FlowType);
        algo.flowAnalysis(net);

        double[][] buses = algo.getBusesPowerFlowInfo();
        double[][] gens = algo.getGeneratorsPowerFlowInfo();
        double[][] bras = algo.getBranchesPowerFlowInfo();
        double[][] cost = algo.getCostsPowerFlowInfo();
        
        //printDoubleArrays(buses, gens, bras, cost);
       
    }
    
    public void printDoubleArrays(double[][] buses, double[][] gens, double[][] bras, double[][] cost){
        print(buses, "Bus");
        print(gens, "Generators");
        print(bras, "Branches");
        print(cost, "Cost");
    }
    
    private void print(double[][] stuff, String title){
        System.out.println("---------------- " + title + " ------------------");
        System.out.println("Number of columns: " + stuff[0].length);
        for(int i = 0; i<stuff.length; i++){
            for(int j = 0; j<stuff[i].length; j++){
                System.out.format("%10s", stuff[i][j]);
            }    
            System.out.print("\n");
        }
    }
    
}
