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
package interdependent;

import backend.FlowDomainAgent;
import dsutil.protopeer.FingerDescriptor;
import event.Event;
import input.EventLoaderNew;
import input.FlowLoaderNew;
import input.SfinaParameter;
import input.SfinaParameterLoader;
import input.TopologyLoader;
import interdependent.communication.CommunicationAgentInterface;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import network.FlowNetwork;
import network.Link;
import network.LinkState;
import network.Node;
import network.NodeState;
import org.apache.log4j.Logger;
import output.EventWriter;
import output.FlowWriterNew;
import output.TopologyWriter;
import protopeer.BasePeerlet;
import protopeer.Peer;
import protopeer.network.Message;
import protopeer.time.Timer;
import protopeer.time.TimerListener;
import protopeer.util.quantities.Time;

/**
 *
 * @author root
 */
public class SimulationAgentNewNew extends BasePeerlet implements SimulationAgentInterfaceNewNew, CommunicationAgentInterface.MessageReceiver{
    
    private static final Logger logger = Logger.getLogger(SimulationAgentNewNew.class);

    
  
    private int iteration;
    
    
    private final static String peersLogDirectory="peerlets-log/";
    
   
    private SfinaParameterLoader sfinaParameterLoader;
    
    private HashMap<SfinaParameter,Object> sfinaParameters;
    private FlowNetwork flowNetwork;
    private TopologyLoader topologyLoader;
    private FlowLoaderNew flowLoader;
    private TopologyWriter topologyWriter;
    private FlowWriterNew flowWriter;
    private EventWriter eventWriter;
    
    private CommunicationAgentInterface communicationAgent;
   
    private EventLoaderNew eventLoader;
    private FingerDescriptor myAgentDescriptor;
   
    private ArrayList<Event> events;
    
    /**
     * BASE PEERLET METHODS
     */
    
    public SimulationAgentNewNew(
            String experimentID,
            Time bootstrapTime, 
            Time runTime){
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
        this.communicationAgent = (CommunicationAgentInterface) getPeer().getPeerletOfType(CommunicationAgentInterface.class);
        
    }

//    /**
//    * Starts the simulation agent by scheduling the epoch measurements and 
//    * bootstrapping the agent
//    */
//    @Override
//    public void start(){
//       
//    }
//    /**
//    * Stops the simulation agent
//    */
//    @Override
//    public void stop(){
//        
//    }
    
     /**
     * Message Receiver Methods
     */
    @Override
    public void injectEvents(List<Event> events) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void initialise() {
       this.runBootstraping();
    }
    
    @Override
    public void progressToNextStep() {
        this.runActiveState();
    }

    @Override
    public int getIdentifier() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Integer> getConnectedNetwork() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Bootstrapping Called from Communication Agent
     */
    @Override
    public void runBootstraping() {
        
        this.loadExperimentConfigFiles(getCommunicationAgent().getSfinaParamLocation(),getCommunicationAgent().getBackendParamLocation(),getCommunicationAgent().getEventsInputLocation());
        
        topologyLoader=new TopologyLoader(flowNetwork, getCommunicationAgent().getColumnSeparator());
         flowLoader=new FlowLoaderNew(flowNetwork, getCommunicationAgent().getColumnSeparator(), getCommunicationAgent().getMissingValue(), getFlowDomainAgent().getFlowNetworkDataTypes());
         topologyWriter = new TopologyWriter(flowNetwork, getCommunicationAgent().getColumnSeparator());
         flowWriter = new FlowWriterNew(flowNetwork, getCommunicationAgent().getColumnSeparator(), getCommunicationAgent().getMissingValue(), getFlowDomainAgent().getFlowNetworkDataTypes());
         eventWriter = new EventWriter(getCommunicationAgent().getEventsOutputLocation(),  getCommunicationAgent().getColumnSeparator(),getCommunicationAgent().getMissingValue(), getFlowDomainAgent().getFlowNetworkDataTypes());         
    }
    
    
    /**
     * ACTIVE STATE AND ITS METHODS
     */
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
    public void runActiveState(){
        Timer loadAgentTimer=getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                initActiveState();
                
                runInitialOperations();
                
                executeAllEvents();
                
                runFlowAnalysis();

                runFinalOperations();
                
                getCommunicationAgent().agentFinishedStep();
            }
        });
        loadAgentTimer.schedule(getCommunicationAgent().getRunTime());
    }     
    
      /**
     * Initializes the active state by setting iteration = 1 and loading data.
     */
    public void initActiveState(){
        getCommunicationAgent().setTimeToken(getCommunicationAgent().getTimeTokenName() + getCommunicationAgent().getSimulationTime());
        logger.info("\n--------------> " + getCommunicationAgent().getTimeToken() + " at peer " + getPeer().getNetworkAddress() + " <--------------");
        resetIteration();        
        loadInputData(getCommunicationAgent().getTimeToken());   
    }
    
    @Override
    public void runInitialOperations(){

    }
    
    @Override
    public void runFinalOperations(){
        
    }
     @Override
    public void runPassiveState(Message message){
        
    }
    
    @Override
    public void executeAllEvents(){
        int time = getCommunicationAgent().getSimulationTime();
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
     * Performs the simulation between measurements. Handles iterations and calls the backend.
     */
    @Override
    public void runFlowAnalysis(){
        for(FlowNetwork currentIsland : flowNetwork.computeIslands()){
            boolean converged = this.getFlowDomainAgent().flowAnalysis(currentIsland);
        }
        nextIteration();
    }
  
     /**
     * Executes the event if its time corresponds to the current simulation time.
     * @param flowNetwork
     * @param event
     */
    @Override
    public void executeEvent(FlowNetwork flowNetwork, Event event){
        if(event.getTime() == getCommunicationAgent().getSimulationTime()){
            
            this.eventWriter.writeEvent(event);
            
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
                                    logger.info("..setting node " + node.getIndex() + " to activated = " + event.getValue());
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
                                    logger.info("..setting link " + link.getIndex() + " to activated = " + event.getValue());
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
                        case RELOAD:
                            loadInputData(getCommunicationAgent().getTimeTokenName() + (String)event.getValue());
                            break;
                        default:
                            logger.debug("System parameter cannot be recognized.");
                    }
                    break;
                default:
                    logger.debug("Event type cannot be recognised");
            }
        }
        else
            logger.debug("Event not executed because defined for different time step.");
    }
     
    
    /***
     * UTILITY FUNCTIONS - Loading etc
     */  
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
        if (!file.exists())
            logger.debug("sfinaParameters.txt file not found. Should be here: " + sfinaParamLocation);

        sfinaParameterLoader = new SfinaParameterLoader(CommunicationAgentInterface.getParameterColumnSeparator());
        sfinaParameters = sfinaParameterLoader.loadSfinaParameters(sfinaParamLocation);
        logger.debug("Loaded sfinaParameters: " + sfinaParameters);
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
            eventLoader=new EventLoaderNew(getCommunicationAgent().getColumnSeparator(),getCommunicationAgent().getMissingValue(),this.getFlowDomainAgent().getFlowNetworkDataTypes());
            events=eventLoader.loadEvents(eventsLocation);
        }
        else
            logger.debug("No events.txt file provided.");
    }
    
    /**
     * Loads network data from input files at given time if folder is provided.
     * @param timeToken String "time_x" for time x
     */
    public void loadInputData(String timeToken){
        File file = new File(getCommunicationAgent().getExperimentInputFilesLocation()+timeToken);
        if (file.exists() && file.isDirectory()) {
            logger.info("loading data at " + timeToken);
            topologyLoader.loadNodes(getCommunicationAgent().getExperimentInputFilesLocation()+timeToken+getCommunicationAgent().getNodesLocation());
            topologyLoader.loadLinks(getCommunicationAgent().getExperimentInputFilesLocation()+timeToken+getCommunicationAgent().getLinksLocation());
            if (new File(getCommunicationAgent().getExperimentInputFilesLocation()+timeToken+getCommunicationAgent().getNodesFlowLocation()).exists())
                flowLoader.loadNodeFlowData(getCommunicationAgent().getExperimentInputFilesLocation()+timeToken+getCommunicationAgent().getNodesFlowLocation());
            else
                logger.debug("No node flow data provided for at " + timeToken + ".");
            if(new File(getCommunicationAgent().getExperimentInputFilesLocation()+timeToken+getCommunicationAgent().getLinksFlowLocation()).exists())
                flowLoader.loadLinkFlowData(getCommunicationAgent().getExperimentInputFilesLocation()+timeToken+getCommunicationAgent().getLinksFlowLocation());
            else
                logger.debug("No link flow data provided at " + timeToken + ".");
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
        topologyWriter.writeNodes(getCommunicationAgent().getExperimentOutputFilesLocation()+getCommunicationAgent().getTimeToken()+"/iteration_"+iteration+getCommunicationAgent().getNodesLocation());
        topologyWriter.writeLinks(getCommunicationAgent().getExperimentOutputFilesLocation()+getCommunicationAgent().getTimeToken()+"/iteration_"+iteration+getCommunicationAgent().getLinksLocation());
        flowWriter.writeNodeFlowData(getCommunicationAgent().getExperimentOutputFilesLocation()+getCommunicationAgent().getTimeToken()+"/iteration_"+iteration+getCommunicationAgent().getNodesFlowLocation());
        flowWriter.writeLinkFlowData(getCommunicationAgent().getExperimentOutputFilesLocation()+getCommunicationAgent().getTimeToken()+"/iteration_"+iteration+getCommunicationAgent().getLinksFlowLocation());
    }
    
   
    
   /****
    * GETTER, SETTER AND ONELINER
    */
    
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
        this.saveOutputData();
        this.iteration++;
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
     * @return the events
     */
    public ArrayList<Event> getEvents() {
        return events;
    }
    
    /**
     * 
     * @param event 
     */
    @Override
    public void queueEvent(Event event){
        this.getEvents().add(event);
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
     * @return the eventWriter
     */
    public EventWriter getEventWriter() {
        return eventWriter;
    }
    
   
    
     public CommunicationAgentInterface getCommunicationAgent(){
        if(this.communicationAgent == null){
            this.communicationAgent = (CommunicationAgentInterface) getPeer().getPeerletOfType(CommunicationAgentInterface.class);
        }
         return this.communicationAgent;
    }
     
      @Override
    public FlowDomainAgent getFlowDomainAgent(){
        return (FlowDomainAgent)getPeer().getPeerletOfType(FlowDomainAgent.class);
    }

    
  
    
   
}