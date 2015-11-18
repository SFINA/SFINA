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
package experiments;

import applications.BenchmarkLogReplayer;
import input.Backend;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import power.PowerFlowType;

/**
 *
 * @author Ben
 */
public class wrapperRandRemovalMeasurement {
    
    private final static String expSeqNum="LineRemovalRandomConsistentCase30";
    private final static String expID="experiment-"+expSeqNum+"/";
    private final static String resultID="results/"+expSeqNum+"/";
    private final static String columnSeparator=",";
    private static String experimentID="experiment-"+expSeqNum+"/";
    private final static String configurationFilesLocation = "configuration_files/";
    private final static String inputDirectoryName="input";
    private final static String experimentConfigurationFilesLocation=configurationFilesLocation+experimentID+inputDirectoryName+"/";
    private final static String eventsLocation=experimentConfigurationFilesLocation+"/events.txt";

    private static ArrayList<ArrayList<Integer>> attackLinks = new ArrayList();
    
    public static void main(String args[]){
        int iterations = 10;
        ArrayList<Backend> backends = new ArrayList();
        backends.add(Backend.MATPOWER);
        backends.add(Backend.INTERPSS);
        ArrayList<PowerFlowType> flowTypes = new ArrayList();
        flowTypes.add(PowerFlowType.AC);
        flowTypes.add(PowerFlowType.DC);
        
        int linkNr = 41;
        for(int i=0; i<iterations; i++){
            ArrayList<Integer> links = new ArrayList<>();
            for(int j=0; j<linkNr; j++)
                links.add(j+1);
            Collections.shuffle(links);
            attackLinks.add(links);
        }
        
        for(Backend backend : backends){
            for(PowerFlowType flowType : flowTypes){
                for(int i=0; i<iterations; i++){
                    createLinkAttackEvents(i);
                    SuccessiveLineRemoval rmAlgo = new SuccessiveLineRemoval(backend, flowType);
                    rmAlgo.run();
                    BenchmarkLogReplayer replayer=new BenchmarkLogReplayer("peerlets-log/"+expID, 0, 1000);
                }
            }
        }
        
    }
    private static void createLinkAttackEvents(int iteration){
        try{
            File file = new File(eventsLocation);
            file.createNewFile();
            PrintWriter writer = new PrintWriter(new FileWriter(file,false));
            writer.println("time" + columnSeparator + "feature" + columnSeparator + "component" + columnSeparator + "id" + columnSeparator + "parameter" + columnSeparator + "value");
            int time = 2;
            for (int linkId : attackLinks.get(iteration)){
                writer.println(time + columnSeparator + "topology" + columnSeparator + "link" + columnSeparator + linkId + columnSeparator + "status" + columnSeparator + "0");
                time++;
            }
            writer.close();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
        
    }
}
