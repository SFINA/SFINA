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

import applications.BenchmarkAgent;
import applications.BenchmarkLogReplayer;
import input.Backend;
import input.Domain;
import input.SystemParameter;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import power.PowerFlowType;
import protopeer.Experiment;
import protopeer.Peer;
import protopeer.PeerFactory;
import protopeer.SimulatedExperiment;
import protopeer.util.quantities.Time;

/**
 *
 * @author evangelospournaras
 */
public class TimeMeasurementSimu extends SimulatedExperiment{
    
    private final static String expSeqNum="Case118PFTime";
    private final static String peersLogDirectory="peerlets-log/";
    private static String experimentID="experiment-"+expSeqNum+"/";
    
    //Simulation Parameters
    private final static int bootstrapTime=2000;
    private final static int runTime=1000;
    private final static int runDuration=104;
    private final static int N=1;
    
    // SFINA parameters
    private final static HashMap<SystemParameter,Object> simulationParameters = new HashMap();

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
    
    public static void main(String[] args) {
        ArrayList<Backend> backends = new ArrayList();
        backends.add(Backend.MATPOWER);
        backends.add(Backend.INTERPSS);
        ArrayList<PowerFlowType> flowTypes = new ArrayList();
        flowTypes.add(PowerFlowType.AC);
        flowTypes.add(PowerFlowType.DC);
        int iterations = 1;
        
        for(Backend backend : backends){
            for(PowerFlowType flowType : flowTypes){
                for(int i=0; i<iterations; i++){
                    run(backend, flowType);
                    BenchmarkLogReplayer replayer=new BenchmarkLogReplayer(expSeqNum, 0, 1000);
                }
            }
        }
        
    }
    
    private static void run(Backend backend, PowerFlowType flowType){
        // Necessary
        simulationParameters.put(SystemParameter.DOMAIN, Domain.POWER);
        simulationParameters.put(SystemParameter.BACKEND, backend);
        simulationParameters.put(SystemParameter.FLOW_TYPE, flowType);
        simulationParameters.put(SystemParameter.TOLERANCE_PARAMETER, 2.0);
        //simulationParameters.put(SystemParameter.CAPACITY_CHANGE, 0.5);
        
        System.out.println("Experiment "+expSeqNum+"\n");
        Experiment.initEnvironment();
        final TimeMeasurementSimu test = new TimeMeasurementSimu();
        test.init();
        final File folder = new File(peersLogDirectory+experimentID);
        clearExperimentFile(folder);
        folder.mkdir();
        
        //ReplicateFirstInputFolder createTimeFolders = new ReplicateFirstInputFolder(runDuration, configurationFilesLocation, timeTokenName);
        
        PeerFactory peerFactory=new PeerFactory() {
            public Peer createPeer(int peerIndex, Experiment experiment) {
                Peer newPeer = new Peer(peerIndex);
//                if (peerIndex == 0) {
//                   newPeer.addPeerlet(null);
//                }
                newPeer.addPeerlet(new BenchmarkAgent(
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
                        simulationParameters));
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
