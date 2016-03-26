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
package core;

import dsutil.protopeer.FingerDescriptor;
import event.Event;
import power.backend.PowerBackend;
import static power.backend.PowerBackend.INTERPSS;
import static power.backend.PowerBackend.MATPOWER;
import backend.FlowBackendInterface;
import power.input.PowerBackendParameterLoader;
import input.Domain;
import static input.Domain.GAS;
import static input.Domain.POWER;
import static input.Domain.TRANSPORTATION;
import static input.Domain.WATER;
import input.EventLoader;
import input.SfinaParameter;
import input.SfinaParameterLoader;
import input.TopologyLoader;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;
import network.FlowNetwork;
import network.Link;
import network.LinkState;
import network.Node;
import network.NodeState;
import org.apache.log4j.Logger;
import output.TopologyWriter;
import power.backend.InterpssFlowBackend;
import power.backend.MATPOWERFlowBackend;
import power.input.PowerFlowLoader;
import power.input.PowerLinkState;
import power.input.PowerNodeState;
import power.output.PowerFlowWriter;
import protopeer.BasePeerlet;
import protopeer.Peer;
import protopeer.measurement.MeasurementFileDumper;
import protopeer.measurement.MeasurementLog;
import protopeer.measurement.MeasurementLoggerListener;
import protopeer.network.Message;
import protopeer.time.Timer;
import protopeer.time.TimerListener;
import protopeer.util.quantities.Time;

/**
 *
 * @author evangelospournaras
 */
public class SFINAAgent extends BasePeerlet implements SimulationAgentInterface{
    
    private static final Logger logger = Logger.getLogger(SFINAAgent.class);
    
    private String experimentID;
    private Time bootstrapTime;
    private Time runTime;
    private int iteration;
    private final static String parameterColumnSeparator="=";
    private final static String fileSystemSchema="conf/fileSystem.conf";
    private final static String peersLogDirectory="peerlets-log/";
    private String timeToken;
    private String timeTokenName;
    private String experimentInputFilesLocation;
    private String experimentOutputFilesLocation;
    private String nodesLocation;
    private String linksLocation;
    private String nodesFlowLocation;
    private String linksFlowLocation;
    private String eventsLocation;
    private String sfinaParamLocation;
    private String backendParamLocation;
    private String columnSeparator;
    private String missingValue;
    private HashMap<SfinaParameter,Object> sfinaParameters;
    private HashMap<Enum,Object> backendParameters;
    private FlowNetwork flowNetwork;
    private TopologyLoader topologyLoader;
    private SfinaParameterLoader sfinaParameterLoader;
    private PowerBackendParameterLoader backendParameterLoader;
    private EventLoader eventLoader;
    private FingerDescriptor myAgentDescriptor;
    private MeasurementFileDumper measurementDumper;
    private Domain domain;
    private PowerBackend backend;
    private ArrayList<Event> events;
    
    public SFINAAgent(
            String experimentID,
            Time bootstrapTime, 
            Time runTime){
        this.experimentID=experimentID;
        this.bootstrapTime=bootstrapTime;
        this.runTime=runTime;
        this.flowNetwork=new FlowNetwork();
    }
    
    /**
    * Inititializes the simulation agent by creating the finger descriptor.
    *
    * @param peer the local peer
    */
    @Override
    public void init(Peer peer){
        super.init(peer);
        this.myAgentDescriptor=new FingerDescriptor(getPeer().getFinger());
    }

    /**
    * Starts the simulation agent by scheduling the epoch measurements and 
    * bootstrapping the agent
    */
    @Override
    public void start(){
        scheduleMeasurements();
        this.runBootstraping();
    }

    /**
    * Stops the simulation agent
    */
    @Override
    public void stop(){
        
    }
    
    /**
     * The scheduling of system bootstrapping. It loads system parameters, 
     * network and event data. At the end, it triggers the active state. 
     * 
     * Simulation is initialized as follows:
     * 
     * 1. Loading the file system parameters
     * 2. Loading SFINA and backend configuration files and static event files
     * 3. Creating a topology loader
     * 5. Clearing up the output files 
     */
    @Override
    public void runBootstraping(){
        Timer loadAgentTimer= getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                logger.info("### "+experimentID+" ###");
                loadFileSystem(fileSystemSchema);
                loadExperimentConfigFiles(sfinaParamLocation, backendParamLocation, eventsLocation);
                topologyLoader=new TopologyLoader(flowNetwork, columnSeparator);
                clearOutputFiles(new File(experimentOutputFilesLocation));
                runActiveState();
            }
        });
        loadAgentTimer.schedule(this.bootstrapTime);
    }
    
    
    /**
     * The scheduling of the active state.  It is executed periodically. 
 
    This is the fundamental prototype of the simulation runtime. It should
    stay generic. At this moment, the runtime concerns the following:

    1. Counting simulation time.
    2. Checking and loading new data from files.
    3. Triggering event execution at the current time step
    4. Calling three methods, which can be used to implement the actual simulation:
        - performInitialStateOperations()
        - runFlowAnalysis()
        - performFinalStateOperations()
     */
    @Override
    public void runActiveState(){
        Timer loadAgentTimer=getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                timeToken = timeTokenName + getSimulationTime();
                logger.info("--------------> " + timeToken + " <--------------");
                
                resetIteration();
                
                loadData();
                
                performInitialStateOperations();
                
                executeAllEvents();
                
                runFlowAnalysis();
                
                performFinalStateOperations();
                
                runActiveState(); 
            }
        });
        loadAgentTimer.schedule(this.runTime);
    }
    
    public int getSimulationTime(){
        return (int)(Time.inSeconds(this.getPeer().getClock().getTime())-Time.inSeconds(this.bootstrapTime));
    }
    
    /**
     * Load parameters determining file system structure from conf/fileSystem.conf
     */
    @Override
    public void loadFileSystem(String schema){
        String inputDirectoryName=null;
        String outputDirectoryName=null;
        String topologyDirectoryName=null;
        String flowDirectoryName=null;
        String configurationFilesLocation=null;
        String eventsFileName=null;
        String sfinaParamFileName=null;
        String backendParamFileName=null;
        String nodesFileName=null;
        String linksFileName=null;
        File file = new File(schema);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            while(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), parameterColumnSeparator);
                switch(st.nextToken()){
                    case "columnSeparator":
                        this.columnSeparator=st.nextToken();
                        break;
                    case "missingValue":
                        this.missingValue=st.nextToken();
                        break;
                    case "timeTokenName":
                        this.timeTokenName=st.nextToken();
                        break;
                    case "inputDirectoryName":
                        inputDirectoryName=st.nextToken();
                        break;
                    case "outputDirectoryName":
                        outputDirectoryName=st.nextToken();
                        break;
                    case "topologyDirectoryName":
                        topologyDirectoryName=st.nextToken();
                        break;
                    case "flowDirectoryName":
                        flowDirectoryName=st.nextToken();
                        break;
                    case "configurationFilesLocation":
                        configurationFilesLocation=st.nextToken();
                        break;
                    case "eventsFileName":
                        eventsFileName=st.nextToken();
                        break;
                    case "sfinaParamFileName":
                        sfinaParamFileName=st.nextToken();
                        break;
                    case "backendParamFileName":
                        backendParamFileName=st.nextToken();
                        break;
                    case "nodesFileName":
                        nodesFileName=st.nextToken();
                        break;
                    case "linksFileName":
                        linksFileName=st.nextToken();
                        break;
                    default:
                        logger.debug("File system parameter couldn't be recognized.");
                }
            }
        }
        catch (FileNotFoundException ex){
            ex.printStackTrace();
        }
        this.timeToken=this.timeTokenName+Time.inSeconds(0).toString();
        this.experimentInputFilesLocation=configurationFilesLocation+experimentID+"/"+inputDirectoryName;
        this.experimentOutputFilesLocation=configurationFilesLocation+experimentID+"/"+outputDirectoryName;
        this.eventsLocation=experimentInputFilesLocation+eventsFileName;
        this.sfinaParamLocation=experimentInputFilesLocation+sfinaParamFileName;
        this.backendParamLocation=experimentInputFilesLocation+backendParamFileName;
        this.nodesLocation="/"+topologyDirectoryName+nodesFileName;
        this.linksLocation="/"+topologyDirectoryName+linksFileName;
        this.nodesFlowLocation="/"+flowDirectoryName+nodesFileName;
        this.linksFlowLocation="/"+flowDirectoryName+linksFileName;
    }
    
    /**
     * Loads SFINA and backend parameters and events from file. The first has to be provided, will give error otherwise. PowerBackend parameters and events are optional.
     * @param sfinaParamLocation path to sfinaParameters.txt
     * @param backendParamLocation path to backendParameters.txt
     * @param eventsLocation path to events.txt
     */
    @Override
    public void loadExperimentConfigFiles(String sfinaParamLocation, String backendParamLocation, String eventsLocation){
        // Sfina Parameters
        File file = new File(sfinaParamLocation);
        if (!file.exists())
            logger.debug("sfinaParameters.txt file not found. This will give problems. Should be here: " + sfinaParamLocation);
        sfinaParameterLoader = new SfinaParameterLoader(parameterColumnSeparator);
        sfinaParameters = sfinaParameterLoader.loadSfinaParameters(sfinaParamLocation);
        logger.debug("Loaded sfinaParameters: " + sfinaParameters);
        if (getSfinaParameters().containsKey(SfinaParameter.DOMAIN))
            setDomain((Domain)getSfinaParameters().get(SfinaParameter.DOMAIN));
        else 
            logger.debug("Domain not specified.");
        if (getSfinaParameters().containsKey(SfinaParameter.BACKEND))
            setBackend((PowerBackend)getSfinaParameters().get(SfinaParameter.BACKEND));
        else
            logger.debug("Backend not specified.");
        
        // PowerBackend Parameters
        file = new File(backendParamLocation);
        if (file.exists()) {
            backendParameterLoader = new PowerBackendParameterLoader(getDomain(),parameterColumnSeparator);
            backendParameters = backendParameterLoader.loadBackendParameters(backendParamLocation);
            logger.debug("Loaded backendParameters: " + backendParameters);
        }
        else
            logger.debug("No backendParameters.txt file provided.");
        
        // Events
        file = new File(eventsLocation);
        if (file.exists()) {
            eventLoader=new EventLoader(domain,columnSeparator,missingValue);
            events=eventLoader.loadEvents(eventsLocation);
        }
        else
            logger.debug("No events.txt file provided.");
    }
    
    /**
     * It clears the directory with the output files.
     * 
     * @param experiment 
     */
    private static void clearOutputFiles(File experiment){
        File[] files = experiment.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    clearOutputFiles(f);
                } else {
                    f.delete();
                }
            }
        }
        experiment.delete();
    }
    
    /**
     * Loads network data from input files at current time if folder is provided.
     */
    private void loadData(){
        File file = new File(experimentInputFilesLocation+timeToken);
        if (file.exists() && file.isDirectory()) {
            logger.info("loading data at time " + timeToken);
            topologyLoader.loadNodes(experimentInputFilesLocation+timeToken+nodesLocation);
            topologyLoader.loadLinks(experimentInputFilesLocation+timeToken+linksLocation);
            switch(domain){
                case POWER:
                    PowerFlowLoader flowLoader=new PowerFlowLoader(flowNetwork, columnSeparator, missingValue);
                    if (new File(experimentInputFilesLocation+timeToken+nodesFlowLocation).exists())
                        flowLoader.loadNodeFlowData(experimentInputFilesLocation+timeToken+nodesFlowLocation);
                    else
                        logger.debug("No flow data provided for nodes at " + timeToken + ".");
                    if (new File(experimentInputFilesLocation+timeToken+linksFlowLocation).exists())
                        flowLoader.loadLinkFlowData(experimentInputFilesLocation+timeToken+linksFlowLocation);
                    else
                        logger.debug("No flow data provided for links at " + timeToken + ".");
                    break;
                case GAS:
                    logger.debug("This domain is not supported at this moment");
                    break;
                case WATER:
                    logger.debug("This domain is not supported at this moment");
                    break;
                case TRANSPORTATION:
                    logger.debug("This domain is not supported at this moment");
                    break;
                default:
                    logger.debug("This domain is not supported at this moment");
            }
            setFlowParameters();
        }
        else
            logger.debug("No input data provided at " + timeToken + ". Continue to use data from before.");
    }
    
    /**
     * Loads network data from input files at given time.
     * @param time defines the input folder from where data is loaded. Given x it loads from time_x.
     */
    public void loadData(String time){
        timeToken = timeTokenName + time;
        loadData();
        timeToken = timeTokenName + getSimulationTime();
    }
    
    @Override
    public void setFlowParameters(){
        switch(domain){
            case POWER:
                flowNetwork.setLinkFlowType(PowerLinkState.POWER_FLOW_FROM_REAL);
                flowNetwork.setNodeFlowType(PowerNodeState.VOLTAGE_MAGNITUDE);
                flowNetwork.setLinkCapacityType(PowerLinkState.RATE_C);
                flowNetwork.setNodeCapacityType(PowerNodeState.VOLTAGE_MAX);
                break;
            case GAS:
                logger.debug("This domain is not supported at this moment");
                break;
            case WATER:
                logger.debug("This domain is not supported at this moment");
                break;
            case TRANSPORTATION:
                logger.debug("This domain is not supported at this moment");
                break;
            default:
                logger.debug("This domain is not supported at this moment");
        }
    }
    
    /**
     * Outputs txt files in same format as input. Creates new folder for every iteration.
     */
    private void outputData(){
        logger.info("doing output at iteration " + iteration);
        TopologyWriter topologyWriter = new TopologyWriter(flowNetwork, columnSeparator);
        topologyWriter.writeNodes(experimentOutputFilesLocation+timeToken+"/iteration_"+iteration+nodesLocation);
        topologyWriter.writeLinks(experimentOutputFilesLocation+timeToken+"/iteration_"+iteration+linksLocation);
        switch(domain){
                case POWER:
                    PowerFlowWriter flowLoader=new PowerFlowWriter(flowNetwork, columnSeparator, missingValue);
                    flowLoader.writeNodeFlowData(experimentOutputFilesLocation+timeToken+"/iteration_"+iteration+nodesFlowLocation);
                    flowLoader.writeLinkFlowData(experimentOutputFilesLocation+timeToken+"/iteration_"+iteration+linksFlowLocation);
                    break;
                case GAS:
                    logger.debug("This domain is not supported at this moment");
                    break;
                case WATER:
                    logger.debug("This domain is not supported at this moment");
                    break;
                case TRANSPORTATION:
                    logger.debug("This domain is not supported at this moment");
                    break;
                default:
                    logger.debug("This domain is not supported at this moment");
        }
    }
    
    @Override
    public void runPassiveState(Message message){
        
    }
    
    @Override
    public void performInitialStateOperations(){

    }
    
    @Override
    public void performFinalStateOperations(){
        
    }
    
    public void executeAllEvents(){
        int time = getSimulationTime();
        logger.info("executing all events at time_" + time);
        Iterator<Event> i = getEvents().iterator();
        while (i.hasNext()){
            Event event = i.next();
            if(event.getTime() == time){
                this.executeEvent(flowNetwork, event);
                i.remove(); // removes event from the events list, to avoid being executed several times. this is favorable for efficiency (especially for checking islanding, however probably not necessary
            }
        }
    }
    
    @Override
    public void executeEvent(FlowNetwork flowNetwork, Event event){
        if(event.getTime() != getSimulationTime())
            logger.debug("Event executed at different time than it was initialized for.");
        switch(event.getEventType()){
            case TOPOLOGY:
                switch(event.getNetworkComponent()){
                    case NODE:
                        Node node=flowNetwork.getNode(event.getComponentID());
                        switch((NodeState)event.getParameter()){
                            case ID:
                                node.setIndex((String)event.getValue());
                                break;
                            case STATUS:
                                if(node.isActivated() == (Boolean)event.getValue())
                                    logger.debug("Node status same, not changed by event.");
                                node.setActivated((Boolean)event.getValue()); 
                                logger.info("..changing status of node " + node.getIndex());
                                break;
                            default:
                                logger.debug("Node state cannot be recognised");
                        }
                        break;
                    case LINK:
                        Link link=flowNetwork.getLink(event.getComponentID());
                        link.replacePropertyElement(event.getParameter(), event.getValue());
                        switch((LinkState)event.getParameter()){
                            case ID:
                                link.setIndex((String)event.getValue());
                                break;
                            case FROM_NODE:
                                link.setStartNode(flowNetwork.getNode((String)event.getValue()));
                                break;
                            case TO_NODE:
                                link.setEndNode(flowNetwork.getNode((String)event.getValue()));
                                break;
                            case STATUS:
                                if(link.isActivated() == (Boolean)event.getValue())
                                    logger.debug("Link status same, not changed by event.");
                                link.setActivated((Boolean)event.getValue()); 
                                logger.info("..changing status of link " + link.getIndex());
                                break;
                            default:
                                logger.debug("Link state cannot be recognised");
                        }
                        break;
                    default:
                        logger.debug("Network component cannot be recognised");
                }
                break;
            case FLOW:
                switch(event.getNetworkComponent()){
                    case NODE:
                        Node node=flowNetwork.getNode(event.getComponentID());
                        node.replacePropertyElement(event.getParameter(), event.getValue());
                        break;
                    case LINK:
                        Link link=flowNetwork.getLink(event.getComponentID());
                        link.replacePropertyElement(event.getParameter(), event.getValue());
                        break;
                    default:
                        logger.debug("Network component cannot be recognised");
                }
                break;
            case SYSTEM:
                logger.info("..executing system parameter event: " + (SfinaParameter)event.getParameter());
                switch((SfinaParameter)event.getParameter()){
                    case DOMAIN:
                        getSfinaParameters().put(SfinaParameter.DOMAIN, (Domain)event.getValue());
                        setDomain((Domain)event.getValue());
                        break;
                    case BACKEND:
                        getSfinaParameters().put(SfinaParameter.BACKEND, (PowerBackend)event.getValue());
                        setBackend((PowerBackend)event.getValue());
                        break;
                    case RELOAD:
                        this.loadData((String)event.getValue());
                        break;
                    default:
                        logger.debug("System parameter cannot be recognized.");
                }
                break;
            default:
                logger.debug("Event type cannot be recognised");
        }
    }
     
    /**
     * Performs the simulation between measurements. Handles iterations and calls the backend.
     */
    @Override
    public void runFlowAnalysis(){
        for(FlowNetwork currentIsland : flowNetwork.computeIslands()){
            boolean converged = callBackend(currentIsland);
        }
        nextIteration();
    }
    
    /**
     * Executes domain and backend specific flow analysis. Currently implemented for power domain: Matpower and InterPSS.
     * @param flowNetwork
     * @return true if converged, else false.
     */
    @Override
    public boolean callBackend(FlowNetwork flowNetwork){
        FlowBackendInterface flowBackend;
        boolean converged = false;
        logger.info("executing " + backend + " backend");
        switch(domain){
            case POWER:
                switch(backend){
                    case MATPOWER:
                        flowBackend=new MATPOWERFlowBackend(getBackendParameters());
                        converged=flowBackend.flowAnalysis(flowNetwork);
                        break;
                    case INTERPSS:
                        flowBackend=new InterpssFlowBackend(getBackendParameters());
                        converged=flowBackend.flowAnalysis(flowNetwork);
                        break;
                    default:
                        logger.debug("This flow backend is not supported at this moment.");
                }
                break;
            case GAS:
                logger.debug("This domain is not supported at this moment");
                break;
            case WATER:
                logger.debug("This domain is not supported at this moment");
                break;
            case TRANSPORTATION:
                logger.debug("This domain is not supported at this moment");
                break;
            default:
                logger.debug("This domain is not supported at this moment");
        }
        return converged;
    }
    
    /**
     * 
     * @return the network
     */
    public FlowNetwork getFlowNetwork() {
        return flowNetwork;
    }
    
    public void setFlowNetwork(FlowNetwork net) {
        this.flowNetwork = net;
    }
    
    /**
     * 
     * @return the domain
     */
    public Domain getDomain(){
        return domain;
    }
    
    public void setDomain(Domain domain){
        this.domain=domain;
    }
    
    /**
     * 
     * @return the simulation backend
     */
    public PowerBackend getBackend(){
        return backend;
    }
    
    public void setBackend(PowerBackend backend){
        this.backend=backend;
    }
    
    /**
     * Sets iteration to 1.
     */
    public void resetIteration(){
        this.iteration=1;
    }
    
    /**
     * Goes to next iteration and initiates output. First outputs network data at current iteration, then increases iteration by one.
     * Has to be called at the end of the iteration.
     */
    public void nextIteration(){
        this.outputData();
        this.iteration++;
    }
    
    /**
     * 
     * @return the current iteration
     */
    public int getIteration(){
        return this.iteration;
    }
    
    /**
     * @return the events
     */
    public ArrayList<Event> getEvents() {
        return events;
    }
    
    /**
     * @return the sfinaParameters
     */
    public HashMap<SfinaParameter,Object> getSfinaParameters() {
        return sfinaParameters;
    }

    /**
     * @param sfinaParameters the sfinaParameters to set
     */
    public void setSfinaParameters(HashMap<SfinaParameter,Object> sfinaParameters) {
        this.sfinaParameters = sfinaParameters;
    }

    /**
     * @return the backendParameters
     */
    public HashMap<Enum,Object> getBackendParameters() {
        return backendParameters;
    }

    /**
     * @param backendParameters the backendParameters to set
     */
    public void setBackendParameters(HashMap<Enum,Object> backendParameters) {
        this.backendParameters = backendParameters;
    }
    
    //****************** MEASUREMENTS ******************
    
    /**
     * Scheduling the measurements for the simulation agent
     */
    @Override
    public void scheduleMeasurements(){
        this.setMeasurementDumper(new MeasurementFileDumper(getPeersLogDirectory()+this.getExperimentID()+"/peer-"+getPeer().getIndexNumber()));
        getPeer().getMeasurementLogger().addMeasurementLoggerListener(new MeasurementLoggerListener(){
            public void measurementEpochEnded(MeasurementLog log, int epochNumber){
                
                getMeasurementDumper().measurementEpochEnded(log, epochNumber);
                log.shrink(epochNumber, epochNumber+1);
            }
        });
    }

    /**
     * @return the experimentID
     */
    public String getExperimentID() {
        return experimentID;
    }

    /**
     * @return the peersLogDirectory
     */
    public String getPeersLogDirectory() {
        return peersLogDirectory;
    }

    /**
     * @return the measurementDumper
     */
    public MeasurementFileDumper getMeasurementDumper() {
        return measurementDumper;
    }
    
    /**
     * @param measurementDumper the measurementDumper to set
     */
    public void setMeasurementDumper(MeasurementFileDumper measurementDumper) {
        this.measurementDumper = measurementDumper;
    }
}