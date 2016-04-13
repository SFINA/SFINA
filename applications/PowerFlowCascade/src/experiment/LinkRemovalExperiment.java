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
package experiment;

import agent.PowerCascadeAgent;
import replayer.BenchmarkLogReplayer;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.log4j.Logger;
import protopeer.Experiment;
import protopeer.Peer;
import protopeer.PeerFactory;
import protopeer.SimulatedExperiment;
import protopeer.util.quantities.Time;

/**
 * Remove links successively in each time step to trigger cascade. Which links to remove is determined randomly. 
 * @author Ben
 */
public class LinkRemovalExperiment extends SimulatedExperiment{
    
    private static final Logger logger = Logger.getLogger(LinkRemovalExperiment.class);
    
    private static String expSeqNum="Case30LinkRemovalRandom";
    private final static String peersLogDirectory="peerlets-log/";
    private static String experimentID="experiment-"+expSeqNum;
    
    //Simulation Parameters
    private final static int bootstrapTime=2000;
    private final static int runTime=1000;
    private final static int runDuration=34;
    private final static int N=1;
    
    private static ArrayList<ArrayList<Integer>> attackLinks = new ArrayList();
    
    public static void main(String args[]){
        int iterations = 1;
        
        // Random
        int linkNr = 41;
        for(int i=0; i<iterations; i++){
            ArrayList<Integer> links = new ArrayList<>();
            for(int j=0; j<linkNr; j++)
                links.add(j+1);
            Collections.shuffle(links);
            attackLinks.add(links);
        }
        //createLinkAttackEvents();
        
        run();
        BenchmarkLogReplayer replayer=new BenchmarkLogReplayer(expSeqNum, 0, 1000);
    }
    
    public static void run() {
        double relCapacityChange = 1.0;
        
        Experiment.initEnvironment();
        final LinkRemovalExperiment test = new LinkRemovalExperiment();
        test.init();
        final File folder = new File(peersLogDirectory+experimentID);
        clearExperimentFile(folder);
        folder.mkdir();
        PeerFactory peerFactory=new PeerFactory() {
            public Peer createPeer(int peerIndex, Experiment experiment) {
                Peer newPeer = new Peer(peerIndex);
//                if (peerIndex == 0) {
//                   newPeer.addPeerlet(null);
//                }
                newPeer.addPeerlet(new PowerCascadeAgent(
                        experimentID, 
                        Time.inMilliseconds(bootstrapTime),
                        Time.inMilliseconds(runTime),
                        relCapacityChange));
                return newPeer;
            }
        };
        test.initPeers(0,N,peerFactory);
        test.startPeers(0,N);
        //run the simulation
        test.runSimulation(Time.inSeconds(runDuration));
    }
    
    private static void createLinkAttackEvents(int iteration){
        try{
            File file = new File("experiments/" + experimentID + "/input/events.txt");
            file.createNewFile();
            PrintWriter writer = new PrintWriter(new FileWriter(file,false));
            writer.println("time,feature,component,id,parameter,value");
            int time = 2;
//            for (int i=0; i<30; i++){
//                for (int j=0; j<18; j++){
//                    int linkId = attackLinks.get(iteration).get(i*18+j);
//                    writer.println(time + columnSeparator + "topology" + columnSeparator + "link" + columnSeparator + linkId + columnSeparator + "status" + columnSeparator + "0");
//                }
//                time++;
//            }
            for(int i=0; i<attackLinks.get(iteration).size(); i++){
                writer.println("time,feature,component,id,parameter,value");
                time++;
            }
            writer.close();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
        
    }
    
    public final static void clearExperimentFile(File experiment){
        File[] files = experiment.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    clearExperimentFile(f);
                } else {
                    f.delete();
                }
            }
        }
        experiment.delete();
    }
}
