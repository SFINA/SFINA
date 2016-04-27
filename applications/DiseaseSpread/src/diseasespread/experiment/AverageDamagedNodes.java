/*
 * Copyright (C) 2016 SFINA Team
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
package diseasespread.experiment;

import diseasespread.DiseaseSpreadAgent;
import diseasespread.input.DiseaseSpreadBackendParameter;
import input.Domain;
import input.SfinaParameter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import org.apache.log4j.Logger;
import power.backend.PowerBackend;
import protopeer.Experiment;
import protopeer.Peer;
import protopeer.PeerFactory;
import protopeer.SimulatedExperiment;
import protopeer.util.quantities.Time;
import replayer.BenchmarkLogReplayer;

/**
 *
 * @author dinesh
 */
public class AverageDamagedNodes extends SimulatedExperiment{
    private static final Logger logger = Logger.getLogger(AverageDamagedNodes.class);
    
    private static String expSeqNum="Case100Grid_AverageDamagedNodes";
    private final static String peersLogDirectory="peerlets-log/";
    private static String experimentID="experiment-"+expSeqNum;
    
    //Simulation Parameters
    private final static int bootstrapTime=2000;
    private final static int runTime=1000;
    private final static int runDuration=3;
    private final static int N=1;
    private final static String columnSeparator = ",";
    
    public static void main(String arfs[]){
        //int networkSize = 30;
        //createNodeInfectionEvents();
        run();
        BenchmarkLogReplayer replayer = new BenchmarkLogReplayer(expSeqNum, 0, 1000);
    }
    
    public static void run() {
        Experiment.initEnvironment();
        final AverageDamagedNodes test = new AverageDamagedNodes();
        test.init();
        final File folder = new File(peersLogDirectory+experimentID);
        clearExperimentFile(folder);
        folder.mkdir();
        //createLinkAttackEvents();
        PeerFactory peerFactory=new PeerFactory() {
            public Peer createPeer(int peerIndex, Experiment experiment) {
                Peer newPeer = new Peer(peerIndex);
//                if (peerIndex == 0) {
//                   newPeer.addPeerlet(null);
//                }
                newPeer.addPeerlet(new DiseaseSpreadAgent(
                        experimentID,
                        Time.inMilliseconds(bootstrapTime),
                        Time.inMilliseconds(runTime)));
                return newPeer;
            }
        };
        test.initPeers(0,N,peerFactory);
        test.startPeers(0,N);
        //run the simulation
        test.runSimulation(Time.inSeconds(runDuration));
    }
    /*
    private static void createNodeInfectionEvents(){
        try{
            File file = new File("experiments/" + experimentID + "/input/events.txt");
            file.createNewFile();
            PrintWriter writer = new PrintWriter(new FileWriter(file,false));
            writer.println("time" + columnSeparator + "feature" + columnSeparator + "component" + columnSeparator + "id" + columnSeparator + "parameter" + columnSeparator + "value");
            writer.println(1 + columnSeparator + "flow" + columnSeparator + "node" + columnSeparator + 1 + columnSeparator + "health" + columnSeparator + "1");
            writer.close();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }
    */
    /*
    public final static void createNetwork(NetworkTypes ntype, File experiment){
        // create/open file
        switch(ntype){
            case NetworkTypes.GRID:
                // the parameters 25 and 20 were used same as in the paper
                
            case NetworkTypes.SMALL_WORLD:
                // 

            case NetworkTypes.SCALE_FREE:
                // Not generated yet
            default:

        }
    }
    */
    
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
