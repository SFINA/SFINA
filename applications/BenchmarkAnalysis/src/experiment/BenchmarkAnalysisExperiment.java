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


import replayer.LogReplayerPerIteration;
import agent.BenchmarkEvolution;
import java.io.File;
import org.apache.log4j.Logger;
import protopeer.Experiment;
import protopeer.Peer;
import protopeer.PeerFactory;
import protopeer.SimulatedExperiment;
import protopeer.util.quantities.Time;

/**
 * Experiment to demonstrate cascading failure in interdependent system. 
 * @author Manish
 */
public class BenchmarkAnalysisExperiment extends SimulatedExperiment{
    
    private static final Logger logger = Logger.getLogger(BenchmarkAnalysisExperiment.class);
    
    private static String expSeqNum="case39";
    private final static String peersLogDirectory="peerlets-log/";
    private static String experimentID="experiment-"+expSeqNum;
    
    //Simulation Parameters
    private final static int bootstrapTime=2000;
    private final static int runTime=1000;
    private final static int runDuration=3; //before 5, here it should be 3 for full iteration
    private final static int N=1;
    
     public static void main(String args[]){
        run(); 
        LogReplayerPerIteration replayer=new LogReplayerPerIteration(expSeqNum, 0, 1000);
        //BenchmarkRegister replayer=new BenchmarkRegister(expSeqNum, 0, 1000);
    }
    
    public static void run() {
           
        Experiment.initEnvironment();
        final BenchmarkAnalysisExperiment test = new BenchmarkAnalysisExperiment();
        test.init();
        final File folder = new File(peersLogDirectory+experimentID);
        clearExperimentFile(folder);
        folder.mkdir();
        PeerFactory peerFactory=new PeerFactory() {
            public Peer createPeer(int peerIndex, Experiment experiment) {
                Peer newPeer = new Peer(peerIndex);
                newPeer.addPeerlet(new BenchmarkEvolution(
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