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

import core.SimulationAgent;
import java.io.File;
import protopeer.Experiment;
import protopeer.Peer;
import protopeer.PeerFactory;
import protopeer.SimulatedExperiment;
import protopeer.util.quantities.Time;

/**
 *
 * @author evangelospournaras
 */
public class TestExperiment extends SimulatedExperiment{
    
    private final static String expSeqNum="01";
    private final static String peersLogDirectory="peerlets-log/";
    private static String experimentID="Experiment "+expSeqNum+"/";
    
    //Simulation Parameters
    private final static int runDuration=100;
    private final static int N=100;
    
    // SFINA parameters
    private final static String inputParametersLocation="configuration_files/input/parameters.txt";
    private final static String eventsLocation="configuration_files/input/events.txt";
    private final static String parameterValueSeparator="=";
    private final static String columnSeparator=",";
    
    
    public static void main(String[] args) {
        System.out.println("Experiment "+expSeqNum+"\n");
        Experiment.initEnvironment();
        final TestExperiment test = new TestExperiment();
        test.init();
        final File folder = new File(peersLogDirectory+experimentID);
        clearExperimentFile(folder);
        folder.mkdir();
        PeerFactory peerFactory=new PeerFactory() {
            public Peer createPeer(int peerIndex, Experiment experiment) {
                Peer newPeer = new Peer(peerIndex);
                if (peerIndex == 0) {
                   newPeer.addPeerlet(null);
                }
                newPeer.addPeerlet(new SimulationAgent(experimentID, peersLogDirectory, inputParametersLocation, eventsLocation, parameterValueSeparator, columnSeparator));
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
