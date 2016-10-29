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
package executable;

import core.SimulationAgentNew;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.StringTokenizer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import power.backend.InterpssFlowDomainAgent;
import power.backend.MatpowerFlowDomainAgent;
import protopeer.Experiment;
import protopeer.Peer;
import protopeer.PeerFactory;
import protopeer.SimulatedExperiment;
import protopeer.util.quantities.Time;

/**
 *
 * @author Mark
 */
public class CommandLineExecutable extends SimulatedExperiment{
    public static final String INTERPSS_BACKEND = "interpss";
    public static final String MATPOWER_BACKEND = "matpower";
    
    private static Logger logger = Logger.getLogger(CommandLineExecutable.class);
    
    private static String backendType = INTERPSS_BACKEND;
    
     
    /*
        Default Values
    */
    private static String experimentFolderName= "experiment-";
    private static String expSeqNum="01";
    private static String peersLogDirectory="peerlets-log/";
    private static String experimentID=experimentFolderName+expSeqNum;
    

    //Simulation Parameters
    private static int bootstrapTime=2000;
    private static int runTime=1000;
    private static int runDuration=6;
    private static int N=1;
    
    
    
    
    /*
        MAIN Function
        - argument can either have the absolute or the relative path for the 
        experimentConfig file.
    */
    public static void main(String[] args) {
        
        /*
            File Path parsing
        */
        Options options = new Options();
        options.addOption(
        Option.builder("ep")
            .required(false)
            .hasArg(true)
            .desc("Path to Experiment configuration, if not provided default settings are utilized")
            .longOpt("expath")
            .build()
        );
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try{
            cmd = parser.parse(options,args);
        }catch(ParseException e){
            System.err.println( "Could not parse the command due to: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "java", options );
            return;
        }
        // get file to configuation file
        String path = cmd.getOptionValue("ep");
        
        /*
            Load the parameters
        */
        if(isValid(path))
            loadExperimentConfiguration(path);
        
        //Setup
        Experiment.initEnvironment();       
        CommandLineExecutable test = new CommandLineExecutable();
        test.init();
        
        // Can move these three lines to SimulationAgent also? Reason: loading peersLogDirectory name from the config file.
        File folder = new File(peersLogDirectory+experimentID+"/");
        clearExperimentFile(folder);
        folder.mkdir();
        
        PeerFactory peerFactory=new PeerFactory() {
            public Peer createPeer(int peerIndex, Experiment experiment) {
                Peer newPeer = new Peer(peerIndex);
//                if (peerIndex == 0) {
//                   newPeer.addPeerlet(null);
//                }
                newPeer.addPeerlet(new SimulationAgentNew(
                        experimentID, 
                        Time.inMilliseconds(bootstrapTime),
                        Time.inMilliseconds(runTime)));
                switch(backendType){
                    case INTERPSS_BACKEND:
                        newPeer.addPeerlet(new InterpssFlowDomainAgent(
                                experimentID, 
                                Time.inMilliseconds(bootstrapTime),
                                Time.inMilliseconds(runTime)));
                        break;
                        case MATPOWER_BACKEND:
                            newPeer.addPeerlet(new MatpowerFlowDomainAgent(
                                    experimentID, 
                                    Time.inMilliseconds(bootstrapTime), 
                                    Time.inMilliseconds(runTime)));
                            break;
                }
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

    
    private static void loadExperimentConfiguration(String path){
        File file = new File(path);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            while(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), "=");
                String parameter = st.nextToken();
                String value = st.nextToken();
                switch(parameter){
                    case "backendType":
                        backendType = getTestedValue(value, backendType);
                        break;
                    case "expSeqNum":
                        expSeqNum =getTestedValue(value, expSeqNum);
                        experimentID = experimentFolderName + expSeqNum;
                        break;
                    case "peersLogDirectory":
                        peersLogDirectory = getTestedValue(value, peersLogDirectory);
                        break;
                    case "experimentFolderName":
                        experimentFolderName = getTestedValue(value, experimentFolderName);
                        experimentID = experimentFolderName +expSeqNum;
                        break;
                    case "bootstrapTime":
                        bootstrapTime = (getTestedValue(Integer.valueOf(value), bootstrapTime));
                        break;
                    case "runTime":
                        runTime = (getTestedValue(Integer.valueOf(value), runTime));
                        break;
                    case "runDuration":
                        runDuration = (getTestedValue(Integer.valueOf(value).intValue(), runTime));
                        break;
                    case "N":
                        N = (getTestedValue(Integer.valueOf(value), N));
                        break;
                    default:
                         logger.debug(parameter + " is as an experiment parameter not supported or cannot be recognized");
                }
            }
               
        }catch(FileNotFoundException ex){
            ex.printStackTrace();
        }
   
    
    }
    
     private static <T> T getTestedValue(T value, T defaultValue){
        if(isValid(value)){
            return value;
        }else{
            return defaultValue;
        }
    }
  
    private static <T> boolean isValid(T test){
        if(test instanceof String){
            return (((String) test)!= null && !((String)test).equals(""));
        }else if( test instanceof Integer){
            return ((Integer) test) >0;
        }else {
            return false;
        }
    }
    /*
    private static String getTestedValue(String value, String defaultValue){
        if(stringIsValid(value)){
            return value;
        }else{
            return defaultValue;
        }
    }
    
    private static int getTestedValue(int value, int defaultValue){
        if(intIsValid(value)){
            return value;
        }else{
            return defaultValue;
        }
    }
    
      
    private static boolean stringIsValid(String test){
        return (test!=null && !test.equals(""));
    }
    
    private static boolean intIsValid(int test){
        return (test>0);
    }
*/
   
    
}
