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

import core.SFINAAgent;
import input.Backend;
import input.Domain;
import input.SystemParameter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
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
public class TestSFINAAgent extends SimulatedExperiment{
    
    private final static String expSeqNum="01";
    private final static String peersLogDirectory="peerlets-log/";
    private static String experimentID="experiment-"+expSeqNum+"/";
    
    //Simulation Parameters
    private final static int bootstrapTime=2000;
    private final static int runTime=1000;
    private final static int runDuration=4;
    private final static int N=1;
    
    // SFINA parameters
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
    private final static String systemParametersLocation=experimentConfigurationFilesLocation+"/system-parameters.txt";
    private final static String nodesLocation="/"+topologyDirectoryName+"/nodes.txt";
    private final static String linksLocation = "/"+topologyDirectoryName+"/links.txt";
    private final static String nodesFlowLocation ="/"+flowDirectoryName+"/nodes.txt";
    private final static String linksFlowLocation ="/"+flowDirectoryName+"/links.txt";
    
    public static void main(String[] args) {
        HashMap<SystemParameter,Object> systemParameters=loadSystemParameters(systemParametersLocation,columnSeparator);
        System.out.println(systemParameters);
//        // Necessary
//        systemParameters.put(SystemParameter.DOMAIN, Domain.POWER);
//        systemParameters.put(SystemParameter.BACKEND, Backend.MATPOWER);
//        systemParameters.put(SystemParameter.FLOW_TYPE, PowerFlowType.AC);
//        
//        // Optional, not yet implemented to afffect anything
//        systemParameters.put(SystemParameter.TOLERANCE_PARAMETER, 2.0);
//        systemParameters.put(SystemParameter.CAPACITY_CHANGE, 0.0);
        
        System.out.println("Experiment "+expSeqNum+"\n");
        Experiment.initEnvironment();
        TestSFINAAgent test = new TestSFINAAgent();
        test.init();
        File folder = new File(peersLogDirectory+experimentID);
        clearExperimentFile(folder);
        folder.mkdir();
        PeerFactory peerFactory=new PeerFactory() {
            public Peer createPeer(int peerIndex, Experiment experiment) {
                Peer newPeer = new Peer(peerIndex);
//                if (peerIndex == 0) {
//                   newPeer.addPeerlet(null);
//                }
                newPeer.addPeerlet(new SFINAAgent(
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
                        systemParameters));
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
    
    public final static HashMap<SystemParameter,Object> loadSystemParameters(String location, String separator){
        HashMap<SystemParameter,Object> systemParameters = new HashMap();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            while(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), separator);
                switch(st.nextToken()){
                    case "domain":
                        Domain domain=null;
                        String domainType=st.nextToken();
                        switch(domainType){
                            case "power":
                                domain=Domain.POWER;
                                break;
                            case "gas":
                                domain=Domain.GAS;
                                break;
                            case "water":
                                domain=Domain.WATER;
                                break;
                            case "transportation":
                                domain=Domain.TRANSPORTATION;
                                break;
                            default:
                                logger.debug("This domain is not supported or cannot be recognized");
                        }
                        systemParameters.put(SystemParameter.DOMAIN, domain);
                        break;
                    case "backend":
                        Backend backend=null;
                        String backendType=st.nextToken();
                        switch(backendType){
                            case "MATPOWER":
                                backend=Backend.MATPOWER;
                                break;
                            case "InterPSS":
                                backend=Backend.INTERPSS;
                                break;
                            default:
                                logger.debug("This backend is not supported or cannot be recognized");
                        }
                        systemParameters.put(SystemParameter.BACKEND, backend);
                        break;
                    case "flow_type":
                        PowerFlowType powerFlow=null;
                        String powerFlowType=st.nextToken();
                        switch(powerFlowType){
                            case "DC":
                                powerFlow=PowerFlowType.DC;
                                break;
                            case "AC":
                                powerFlow=PowerFlowType.AC;
                                break;
                            default:
                                logger.debug("This flow type is not supported or cannot be recognized");
                        }
                        systemParameters.put(SystemParameter.FLOW_TYPE, powerFlow);
                        break;
                    case "tolerance_parameter":
                        systemParameters.put(SystemParameter.TOLERANCE_PARAMETER, Double.parseDouble(st.nextToken()));
                        break;
                    case "capacity_change":
                        systemParameters.put(SystemParameter.CAPACITY_CHANGE, Double.parseDouble(st.nextToken()));
                        break;
                    default:
                        logger.debug("This system parameter is not supported or cannot be recognized");
                }
            }
        }
        catch (FileNotFoundException ex){
            ex.printStackTrace();
        }
        return systemParameters;
    }
}
