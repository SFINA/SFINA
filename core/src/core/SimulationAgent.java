/*
 * Copyright (C) 2017 SFINA Team
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


import backend.FlowDomainAgent;
import dsutil.protopeer.FingerDescriptor;
import event.Event;
import event.EventType;
import event.NetworkComponent;
import input.EventLoader;
import input.FlowLoader;
import input.SfinaParameter;
import input.SfinaParameterLoader;
import input.TopologyLoader;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import network.FlowNetwork;
import network.InterdependentLink;
import network.Link;
import network.LinkState;
import network.Node;
import network.NodeState;
import org.apache.log4j.Logger;
import output.EventWriter;
import output.FlowWriter;
import output.TopologyWriter;
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
 * @author mcb
 */
public class SimulationAgent  extends BasePeerlet implements SimulationAgentInterface, TimeSteppingAgentInterface.CommandReceiver{
    
    private static final Logger logger = Logger.getLogger(SimulationAgent.class);

    private String experimentID;
 //   private Time bootstrapTime;
   // private Time runTime;
    private int iteration;
    private final static String parameterColumnSeparator="=";
    private final static String fileSystemSchema="conf/fileSystem.conf";
    private String peersLogDirectory;
    private String peerToken;
    private String peerTokenName;
    private String timeToken;
    private String timeTokenName;
    private String experimentBaseFolderLocation;
    private String experimentInputFilesLocation;
    private String experimentOutputFilesLocation;
    private String nodesLocation;
    private String linksLocation;
    private String interdependentLinksLocation;
    private String nodesFlowLocation;
    private String linksFlowLocation;
    private String interdependentLinksFlowLocation;
    private String eventsInputLocation;
    private String eventsOutputLocation;
    private String sfinaParamLocation;
    private String backendParamLocation;
    private String columnSeparator;
    private String missingValue;
    private HashMap<SfinaParameter,Object> sfinaParameters;
    private FlowNetwork flowNetwork;
    private TopologyLoader topologyLoader;
    private FlowLoader flowLoader;
    private TopologyWriter topologyWriter;
    private FlowWriter flowWriter;
    private EventWriter eventWriter;
    private SfinaParameterLoader sfinaParameterLoader;
    private EventLoader eventLoader;
    private FingerDescriptor myAgentDescriptor;
    private MeasurementFileDumper measurementDumper;
    private ArrayList<Event> events;
    
    public SimulationAgent(
            String experimentID){
        this.experimentID=experimentID;
        this.flowNetwork=new FlowNetwork();
    }
    
    /***************************************************
     *               BASE PEERLET FUNCTIONS
     * *************************************************/
    
    /**
    * Inititializes the simulation agent by creating the finger descriptor.
    *
    * @param peer the local peer
    */
    @Override
    public void init(Peer peer){
        super.init(peer);
        this.myAgentDescriptor=new FingerDescriptor(getPeer().getFinger());
        this.setNetworkIndex(this.getPeer().getIndexNumber());
    }

    /**
    * Starts the simulation agent by scheduling the epoch measurements and 
    * bootstrapping the agent
    */
    @Override
    public void start(){
  //      this.runBootstraping();
    }

    /**
    * Stops the simulation agent
    */
    @Override
    public void stop(){
        
    }
    
    
    /**************************************************
     *               BOOTSTRAPPING
     **************************************************/
    
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
       
        logger.info("### Bootstraping "+experimentID+" ###");
        loadFileSystem(fileSystemSchema);
        loadExperimentConfigFiles(sfinaParamLocation, backendParamLocation, eventsInputLocation);

        // Clearing output and peers log files from earlier experiments
        File folder = new File(peersLogDirectory+experimentID+"/");
        clearOutputFiles(folder);
        folder.mkdir();
        clearOutputFiles(new File(experimentOutputFilesLocation));

        topologyLoader=new TopologyLoader(flowNetwork, columnSeparator, getNetworkIndex());
        flowLoader=new FlowLoader(flowNetwork, columnSeparator, missingValue, getFlowDomainAgent().getFlowNetworkDataTypes());
        topologyWriter = new TopologyWriter(flowNetwork, columnSeparator, getNetworkIndex());
        flowWriter = new FlowWriter(flowNetwork, columnSeparator, missingValue, getFlowDomainAgent().getFlowNetworkDataTypes());
        eventWriter = new EventWriter(eventsOutputLocation, columnSeparator, missingValue, getFlowDomainAgent().getFlowNetworkDataTypes());

        scheduleMeasurements();

        logger.debug("### End of bootstraping, calling agentFinishedBootstrap. ###");
        getTimeSteppingAgent().agentFinishedBootStrap();               
        
    }

    /**
     * Scheduling the measurements for the simulation agent
     */
    @Override
    public void scheduleMeasurements(){
        this.setMeasurementDumper(new MeasurementFileDumper(getPeersLogDirectory()+this.getExperimentID()+peerTokenName));
        getPeer().getMeasurementLogger().addMeasurementLoggerListener(new MeasurementLoggerListener(){
            public void measurementEpochEnded(MeasurementLog log, int epochNumber){
                logger.debug("---> Measuring network " + getPeer().getIndexNumber());
                getMeasurementDumper().measurementEpochEnded(log, epochNumber);
                log.shrink(epochNumber, epochNumber+1);
            }
        });
    }
   
    
    /***************************************************
    *               RUN ACTIVE STATE
    **************************************************/
    
    /**
     * The scheduling of the active state.  It is executed periodically. 
 
    This is the fundamental prototype of the simulation runtime. It should
    stay generic. At this moment, the runtime concerns the following:

    1. Counting simulation time.
    2. Checking and loading new data from files.
    3. Triggering event execution at the current time step
    4. Calling three methods, which can be used to implement the actual simulation:
        - initialOperations()
        - runFlowAnalysis()
        - finalOperations()
     */
    
    @Override
    public void progressToNextTimeStep() {
                initTimeStep();
                logIteration();
                runActiveState(); 
    }

    @Override
    public void progressToNextIteration() { 
        this.logIteration();
        this.runActiveState(); 
    }
    
    @Override
    public void skipNextIteration() {
        this.logIteration();
        logger.info("Skipping this iteration");
        getTimeSteppingAgent().agentFinishedActiveState();
    }

    @Override
    public boolean isConverged() {
        for(Event event : getEvents()){
            if(event.getTime() == getSimulationTime())
                return false;
        }
        return true;
    }
    
    @Override
    public void runActiveState(){
        executeAllEvents();

        runInitialOperations();

        runFlowAnalysis();

        runFinalOperations();

        saveOutputData();

        getTimeSteppingAgent().agentFinishedActiveState();
            
    }
    
     /**
     * Initializes the active state by setting iteration = 1 and loading data.
     */
    private void initTimeStep(){
        this.timeToken = this.timeTokenName + this.getSimulationTime();
        logger.info("\n---------------------------------------------------\n--------------> " + this.timeToken + " at network " + this.getNetworkIndex()+ " <--------------");
        resetIteration();        
        loadInputData(timeToken);   
    }
    
    /**
     * Sets iteration to 0.
     */
    private void resetIteration(){
        this.iteration=0;
    }
    
    private void logIteration(){
        this.iteration++;
        logger.info("\n-------> Iteration " + this.getIteration() + " at network " + this.getNetworkIndex() + " (" + this.timeToken + ") <-------");
    }
      
    @Override
    public void runInitialOperations(){
        
    }
    
    /**
     * Performs the simulation between measurements. Handles iterations and calls the backend.
     */
    @Override
    public void runFlowAnalysis(){
        for(FlowNetwork currentIsland : flowNetwork.computeIslands()){
            boolean converged = this.getFlowDomainAgent().flowAnalysis(currentIsland);
        }
        // For testing if iteration advances as expected
        if(this.getNetworkIndex() == 0 && this.getSimulationTime() == 1)
            if(this.getIteration()==1 || this.getIteration()==2 )
                this.queueEvent(new Event(getSimulationTime(),EventType.TOPOLOGY,NetworkComponent.LINK,"1",LinkState.STATUS,false));
        if(this.getSimulationTime() == 2)
            if(this.getIteration()==1 || this.getIteration()==2 )
                this.queueEvent(new Event(getSimulationTime(),EventType.TOPOLOGY,NetworkComponent.LINK,"1",LinkState.STATUS,false));
    }
     
    @Override
    public void runFinalOperations(){
        
    }
    
    @Override
    public void runPassiveState(Message message){
        
    }

    /***************************************
     *          GETTER AND SETTER  
     *****************************************/
    
    @Override
    public int getNetworkIndex() {
        return this.getFlowNetwork().getNetworkIndex(); 
    }

    @Override
    public Collection<Integer> getConnectedNetworkIndices() {
        return this.getFlowNetwork().getConnectedNetworkIndices();
    }
  
    public TimeSteppingAgentInterface getTimeSteppingAgent(){
        return (TimeSteppingAgentInterface) getPeer().getPeerletOfType(TimeSteppingAgentInterface.class);
    }
    
    @Override
    public int getSimulationTime(){
//        // just for testing purposes, does this change something
//        return ((int) (Time.inSeconds(this.getPeer().getClock().getTime())-Time.inSeconds(Time.inMilliseconds(2000))));
        return getTimeSteppingAgent().getSimulationTime();
    }
    
    @Override
    public FlowDomainAgent getFlowDomainAgent(){
        return (FlowDomainAgent)getPeer().getPeerletOfType(FlowDomainAgent.class);
    }
    
    /**
     * 
     * @return the network
     */
    @Override
    public FlowNetwork getFlowNetwork() {
        return flowNetwork;
    }
    
    public void setFlowNetwork(FlowNetwork net) {
        this.flowNetwork = net;
    }
    
    /**
     * @return the events
     */
    public ArrayList<Event> getEvents() {
        return events;
    }
     
    /**
     * 
     * @return the current iteration
     */
    @Override
    public int getIteration(){
        return this.iteration;
    }
     
    /**
     * 
     * @param iteration
     */
    public void setIteration(int iteration){
        this.iteration=iteration;
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
    @Override
    public HashMap<Enum,Object> getDomainParameters() {
        return this.getFlowDomainAgent().getDomainParameters();
    }

    /**
     * @param backendParameters the backendParameters to set
     */
    public void setBackendParameters(HashMap<Enum,Object> backendParameters) {
        this.getFlowDomainAgent().setDomainParameters(backendParameters);
    }
    
    /**
     * @param networkIndex the networkIndex to set
     */
    private void setNetworkIndex(int networkIndex) {
        this.getFlowNetwork().setNetworkIndex(networkIndex);
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
     * @return the columnSeparator
     */
    public String getParameterColumnSeparator() {
        return parameterColumnSeparator;
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
    
    
    
    /***************************************
     *          EVENT HANDLING
     *****************************************/
    
    @Override
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
    
    /**
     * Executes the event if its time corresponds to the current simulation time.
     * @param flowNetwork
     * @param event
     */
    @Override
    public void executeEvent(FlowNetwork flowNetwork, Event event){
        if(event.getTime() == getSimulationTime()){
            boolean successful = true;            
            switch(event.getEventType()){
                case TOPOLOGY:
                    switch(event.getNetworkComponent()){
                        case NODE:
                            Node node = flowNetwork.getNode(event.getComponentID());
                            switch((NodeState)event.getParameter()){
                                case ID:
                                    node.setIndex((String)event.getValue());
                                    break;
                                case STATUS:
                                    if(node.isActivated() == (Boolean)event.getValue()){
                                        successful = false;
                                        logger.debug("Node status same, not changed by event.");
                                    }
                                    else {
                                        node.setActivated((Boolean)event.getValue()); 
                                        logger.info("..setting node " + node.getIndex() + " to activated = " + event.getValue());
                                    }
                                    break;
                                default:
                                    successful = false;
                                    logger.debug("Node state cannot be recognised");
                            }
                            break;
                        case LINK:
                            Link link=flowNetwork.getLink(event.getComponentID());
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
                                    if(link.isActivated() == (Boolean)event.getValue()){
                                        successful = false;
                                        logger.debug("Link status same, not changed by event.");   
                                    } 
                                    else {
                                        link.setActivated((Boolean)event.getValue()); 
                                        logger.info("..setting link " + link.getIndex() + " to activated = " + event.getValue());
                                    }
                                    break;
                                default:
                                    successful = false;
                                    logger.debug("Link state cannot be recognised");
                            }
                            break;
                        case INTERDEPENDENT_LINK:
                            InterdependentLink interdependentLink=flowNetwork.getInterdependentLink(event.getComponentID());
                            switch((LinkState)event.getParameter()){
                                case ID:
                                    interdependentLink.setIndex((String)event.getValue());
                                    break;
                                case FROM_NODE:
                                    interdependentLink.setStartNode(flowNetwork.getNode((String)event.getValue()));
                                    break;
                                case TO_NODE:
                                    interdependentLink.setEndNode(flowNetwork.getNode((String)event.getValue()));
                                    break;
                                case FROM_NET:
                                    if(interdependentLink.isIncoming())
                                        interdependentLink.setRemoteNetworkIndex((int)event.getValue());
                                    else{
                                        successful = false;
                                        logger.debug("Can't change my own network index. Maybe you wan't to remove the interdependent link instead.");
                                    }
                                    break;
                                case TO_NET:
                                    if(interdependentLink.isOutgoing())
                                        interdependentLink.setRemoteNetworkIndex((int)event.getValue());
                                    else{
                                        successful = false;
                                        logger.debug("Can't change my own network index. Maybe you wan't to remove the interdependent link instead.");
                                    }
                                    break;
                                case STATUS:
                                    if(interdependentLink.isActivated() == (Boolean)event.getValue()){
                                        successful = false;
                                        logger.debug("Interdependent link status same, not changed by event.");
                                    }
                                    else{
                                        interdependentLink.setActivated((Boolean)event.getValue()); 
                                        logger.info("..setting interdependent link " + interdependentLink.getIndex() + " to activated = " + event.getValue());
                                    }
                                    break;
                                case REMOTE_NODE_STATUS:
                                    if(interdependentLink.isRemoteNodeActivated() != (Boolean)event.getValue())
                                        interdependentLink.setRemoteNodeActivated((Boolean)event.getValue());
                                    else{
                                        successful = false;
                                        logger.info("Interdependent link remote node status same, not changed by event.");
                                    }
                                default:
                                    successful = false;
                                    logger.debug("Interdependent link state cannot be recognised");
                            }
                            break;
                        default:
                            successful = false;
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
                        case INTERDEPENDENT_LINK:
                            InterdependentLink interdependentLink=flowNetwork.getInterdependentLink(event.getComponentID());
                            interdependentLink.replacePropertyElement(event.getParameter(), event.getValue());
                            break;
                        default:
                            successful = false;
                            logger.debug("Network component cannot be recognised");
                    }
                    break;
                case SYSTEM:
                    logger.info("..executing system parameter event: " + (SfinaParameter)event.getParameter());
                    switch((SfinaParameter)event.getParameter()){
                        case RELOAD:
                            loadInputData(timeTokenName + (String)event.getValue());
                            break;
                        default:
                            successful = false;
                            logger.debug("System parameter cannot be recognized.");
                    }
                    break;
                default:
                    successful = false;
                    logger.debug("Event type cannot be recognised");
            }
            if(successful)
                this.eventWriter.writeEvent(event);
            else
                logger.debug("Attention: Event must be badly defined, could not be executed!");
        }
        else
            logger.debug("Event not executed because defined for different time step.");
    }
     
    /**
     * 
     * @param event 
     */
    @Override
    public void queueEvent(Event event){
        this.events.add(event);
    }
    
    /**
     *
     * @param events
     */
    public void queueEvents(List<Event> events){
        this.events.addAll(events);
    }
    
    @Override
    public void removeEvent(Event event){
        this.events.remove(event);
    }
    
    
    /***************************************
     *          FILE SYSTEM AND LOADING  
     *****************************************/
    
    @Override
    public void loadFileSystem(String location){
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
        String interdependentLinksFileName=null;
        File file = new File(location);
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
                    case "peerToken":
                        this.peerToken=st.nextToken();
                        break;
                    case "peersLogDirectory":
                        this.peersLogDirectory=st.nextToken();
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
                    case "interdependentLinksFileName":
                        interdependentLinksFileName=st.nextToken();
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
        this.peerTokenName = "/"+peerToken+"-"+getPeer().getIndexNumber();
        this.experimentBaseFolderLocation=configurationFilesLocation+experimentID;
        this.experimentInputFilesLocation=experimentBaseFolderLocation+peerTokenName+"/"+inputDirectoryName;
        this.experimentOutputFilesLocation=experimentBaseFolderLocation+peerTokenName+"/"+outputDirectoryName;
        this.eventsInputLocation=experimentInputFilesLocation+eventsFileName;
        this.eventsOutputLocation=experimentOutputFilesLocation+eventsFileName;
        this.sfinaParamLocation=experimentInputFilesLocation+sfinaParamFileName;
        this.backendParamLocation=experimentInputFilesLocation+backendParamFileName;
        this.nodesLocation="/"+topologyDirectoryName+nodesFileName;
        this.linksLocation="/"+topologyDirectoryName+linksFileName;
        this.interdependentLinksLocation="/"+topologyDirectoryName+interdependentLinksFileName;
        this.nodesFlowLocation="/"+flowDirectoryName+nodesFileName;
        this.linksFlowLocation="/"+flowDirectoryName+linksFileName;
        this.interdependentLinksFlowLocation="/"+flowDirectoryName+interdependentLinksFileName;
    }
    
    /**
     * Loads SFINA and backend parameters and events from file. The first has to be provided, will give error otherwise.
     * @param sfinaParamLocation path to sfinaParameters.txt
     * @param backendParamLocation path to backendParameters.txt
     * @param eventsLocation path to events.txt
     */
    @Override
    public void loadExperimentConfigFiles(String sfinaParamLocation, String backendParamLocation, String eventsLocation){
        // Sfina Parameters
        File file = new File(sfinaParamLocation);
        if (file.exists()){
            sfinaParameterLoader = new SfinaParameterLoader(parameterColumnSeparator);
            sfinaParameters = sfinaParameterLoader.loadSfinaParameters(sfinaParamLocation);
            logger.debug("Loaded sfinaParameters: " + sfinaParameters);
        }
        else
            logger.debug("sfinaParameters.txt file not found. Should be here: " + sfinaParamLocation);
        file = new File(backendParamLocation);
        if (file.exists()) {
            this.getFlowDomainAgent().loadDomainParameters(backendParamLocation);
            logger.debug("Loaded backendParameters: " + this.getDomainParameters());
        }
        else
            logger.debug("No backendParameters.txt file provided.");
        
        // Events
        file = new File(eventsLocation);
        if (file.exists()) {
            eventLoader=new EventLoader(columnSeparator,missingValue,this.getFlowDomainAgent().getFlowNetworkDataTypes());
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
     * Loads network data from input files at given time if folder is provided.
     * @param timeToken String "time_x" for time x
     */
    public void loadInputData(String timeToken){
        File file = new File(experimentInputFilesLocation+timeToken);
        if (file.exists() && file.isDirectory()) {
            logger.info("loading data at " + timeToken);
            topologyLoader.loadNodes(experimentInputFilesLocation+timeToken+nodesLocation);
            topologyLoader.loadLinks(experimentInputFilesLocation+timeToken+linksLocation);
            
            // Load flow data if provided
            if (new File(experimentInputFilesLocation+timeToken+nodesFlowLocation).exists())
                flowLoader.loadNodeFlowData(experimentInputFilesLocation+timeToken+nodesFlowLocation);
            else
                logger.debug("No node flow data provided at " + timeToken + ".");
            if(new File(experimentInputFilesLocation+timeToken+linksFlowLocation).exists())
                flowLoader.loadLinkFlowData(experimentInputFilesLocation+timeToken+linksFlowLocation);
            else
                logger.debug("No link flow data provided at " + timeToken + ".");
            
            // Load interdependent link data if provided. 
            if (new File(experimentInputFilesLocation+timeToken+interdependentLinksLocation).exists()){
                topologyLoader.loadLinks(experimentInputFilesLocation+timeToken+interdependentLinksLocation);
                if(new File(experimentInputFilesLocation+timeToken+interdependentLinksFlowLocation).exists())
                    flowLoader.loadInterdependentLinkFlowData(experimentInputFilesLocation+timeToken+interdependentLinksFlowLocation);
            }
            else
                logger.debug("No interdependent link input files provided at " + timeToken + ".");
            
            this.getFlowDomainAgent().setFlowParameters(flowNetwork);
        }
        else
            logger.debug("No input data provided at " + timeToken + ". Continue to use data from before.");
    }
    
    /**
     * Outputs txt files in same format as input. Creates new folder for every iteration.
     */
    @Override
    public void saveOutputData(){
        logger.info("doing output at iteration " + iteration);
        topologyWriter.writeNodes(experimentOutputFilesLocation+timeToken+"/iteration_"+iteration+nodesLocation);
        topologyWriter.writeLinks(experimentOutputFilesLocation+timeToken+"/iteration_"+iteration+linksLocation);
        topologyWriter.writeInterdependentLinks(experimentOutputFilesLocation+timeToken+"/iteration_"+iteration+interdependentLinksLocation);
        flowWriter.writeNodeFlowData(experimentOutputFilesLocation+timeToken+"/iteration_"+iteration+nodesFlowLocation);
        flowWriter.writeLinkFlowData(experimentOutputFilesLocation+timeToken+"/iteration_"+iteration+linksFlowLocation);
        flowWriter.writeInterdependentLinkFlowData(experimentOutputFilesLocation+timeToken+"/iteration_"+iteration+interdependentLinksFlowLocation);
    }
    
}
