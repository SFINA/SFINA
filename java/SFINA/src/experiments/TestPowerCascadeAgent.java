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
package experiments;

import applications.BenchmarkLogReplayer;
import applications.PowerCascadeAgent;
import java.io.File;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import protopeer.Experiment;
import protopeer.Peer;
import protopeer.PeerFactory;
import protopeer.SimulatedExperiment;
import protopeer.util.quantities.Time;

/**
 *
 * @author Ben
 */
public class TestPowerCascadeAgent extends SimulatedExperiment{
    
    private static final Logger logger = Logger.getLogger(TestPowerCascadeAgent.class);
    
    private final static String expSeqNum="01";
    private final static String peersLogDirectory="peerlets-log/";
    private static String experimentID="experiment-"+expSeqNum+"/";
    
    //Simulation Parameters
    private final static int bootstrapTime=2000;
    private final static int runTime=1000;
    private final static int runDuration=6;
    private final static int N=1;
    
    private final static String columnSeparator=",";
    private final static String missingValue="-";
    
    private final static String configurationFilesLocation = "experiments/";
    private final static String timeTokenName="time_";
    private final static String inputDirectoryName="input";
    private final static String outputDirectoryName="output";
    private final static String topologyDirectoryName="topology";
    private final static String flowDirectoryName="flow";
    
    private final static String experimentConfigurationFilesLocation=configurationFilesLocation+experimentID+inputDirectoryName+"/";
    private final static String experimentOutputFilesLocation=configurationFilesLocation+experimentID+outputDirectoryName+"/";
    private final static String eventsLocation=experimentConfigurationFilesLocation+"events.txt";
    private final static String sfinaParamLocation=experimentConfigurationFilesLocation+"sfinaParameters.txt";
    private final static String backendParamLocation=experimentConfigurationFilesLocation+"backendParameters.txt";
    private final static String nodesLocation="/"+topologyDirectoryName+"/nodes.txt";
    private final static String linksLocation = "/"+topologyDirectoryName+"/links.txt";
    private final static String nodesFlowLocation ="/"+flowDirectoryName+"/nodes.txt";
    private final static String linksFlowLocation ="/"+flowDirectoryName+"/links.txt";
    
    public static void main(String[] args) {
        PropertyConfigurator.configure("conf/log4j.properties");
        logger.info("### EXPERIMENT "+expSeqNum+" ###");
        double relCapacityChange = 1.0;
        
        Experiment.initEnvironment();
        final TestPowerCascadeAgent test = new TestPowerCascadeAgent();
        test.init();
        final File folder = new File(peersLogDirectory+experimentID);
        clearExperimentFile(folder);
        folder.mkdir();
        PeerFactory peerFactory=new PeerFactory() {
            public Peer createPeer(int peerIndex, Experiment experiment) {
                Peer newPeer = new Peer(peerIndex);
                newPeer.addPeerlet(new PowerCascadeAgent(
                        experimentID, 
                        peersLogDirectory, 
                        Time.inMilliseconds(bootstrapTime),
                        Time.inMilliseconds(runTime),                        
                        timeTokenName,
                        experimentConfigurationFilesLocation,
                        experimentOutputFilesLocation,
                        nodesLocation,
                        linksLocation,
                        nodesFlowLocation,
                        linksFlowLocation,
                        eventsLocation,
                        sfinaParamLocation,
                        backendParamLocation,
                        columnSeparator,
                        missingValue,
                        relCapacityChange));
                return newPeer;
            }
        };
        test.initPeers(0,N,peerFactory);
        test.startPeers(0,N);
        //run the simulation
        test.runSimulation(Time.inSeconds(runDuration));
        BenchmarkLogReplayer replayer=new BenchmarkLogReplayer(expSeqNum, 0, 1000);
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
