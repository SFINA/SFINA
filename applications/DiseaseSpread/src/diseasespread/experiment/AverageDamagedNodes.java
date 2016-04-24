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
import diseasespread.DiseaseSpreadBackendParameter;
import diseasespread.testDiseaseSpreadBenchmarkAgent;
import input.Domain;
import input.SfinaParameter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
    
    private static String expSeqNum="Case500Grid_AverageDamagedNodes";
    private final static String peersLogDirectory="peerlets-log/";
    private static String experimentID="experiment-"+expSeqNum+"/";
    
    //Simulation Parameters
    private final static int bootstrapTime=2000;
    private final static int runTime=1000;
    private final static int runDuration=3;
    private final static int N=1;
    
    // SFINA parameters
    private final static HashMap<SfinaParameter,Object> sfinaParameters = new HashMap();
    private final static HashMap<Enum,Object> backendParameters = new HashMap();    
    
    private final static String columnSeparator=",";
    private final static String missingValue="-";
    
    private final static String configurationFilesLocation = "configuration_files/";
    private final static String timeTokenName="time_";
    private final static String inputDirectoryName="input";
    private final static String outputDirectoryName="output";
    private final static String topologyDirectoryName="topology";
    private final static String flowDirectoryName="flow";
    
    private final static String experimentConfigurationFilesLocation=configurationFilesLocation+experimentID+inputDirectoryName+"/";
    private final static String experimentOutputFilesLocation=configurationFilesLocation+experimentID+outputDirectoryName+"/";
    private final static String eventsLocation=experimentConfigurationFilesLocation+"/events.txt";
    private final static String nodesLocation="/"+topologyDirectoryName+"/nodes.txt";
    private final static String linksLocation = "/"+topologyDirectoryName+"/links.txt";
    private final static String nodesFlowLocation ="/"+flowDirectoryName+"/nodes.txt";
    private final static String linksFlowLocation ="/"+flowDirectoryName+"/links.txt";
    
    public static void main(String arfs[]){
        int networkSize = 30;
        createNodeInfectionEvents(networkSize);
        run();
        BenchmarkLogReplayer replayer = new BenchmarkLogReplayer(expSeqNum, 0, 1000);
    }
    
    public static void run() {
        sfinaParameters.put(SfinaParameter.DOMAIN, Domain.DISEASESPREAD);
        sfinaParameters.put(SfinaParameter.BACKEND, PowerBackend.DISEASESPREAD_JAVA);
        backendParameters.put(DiseaseSpreadBackendParameter.STRATEGY, 0);
        
        System.out.println("Experiment "+expSeqNum+"\n");
        Experiment.initEnvironment();
        final testDiseaseSpreadBenchmarkAgent test = new testDiseaseSpreadBenchmarkAgent();
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
                        columnSeparator,
                        missingValue,
                        sfinaParameters,
                        backendParameters));
                return newPeer;
            }
        };
        test.initPeers(0,N,peerFactory);
        test.startPeers(0,N);
        //run the simulation
        test.runSimulation(Time.inSeconds(runDuration));
    }
    
    private static void createNodeInfectionEvents(int networkSize){
        try{
            File file = new File(eventsLocation);
            file.createNewFile();
            PrintWriter writer = new PrintWriter(new FileWriter(file,false));
            writer.println("time" + columnSeparator + "feature" + columnSeparator + "component" + columnSeparator + "id" + columnSeparator + "parameter" + columnSeparator + "value");
            // Infect a (several) node(s)
            writer.println(1 + columnSeparator + "flow" + columnSeparator + "node" + columnSeparator + 1 + columnSeparator + "health" + columnSeparator + "1");
            //writer.println(1 + columnSeparator + "flow" + columnSeparator + "node" + columnSeparator + 5 + columnSeparator + "health" + columnSeparator + "5");
            //writer.println(1 + columnSeparator + "flow" + columnSeparator + "node" + columnSeparator + 30 + columnSeparator + "health" + columnSeparator + "10");
            
            writer.close();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
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
