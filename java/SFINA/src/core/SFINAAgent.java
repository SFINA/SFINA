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

import applications.Metrics;
import dsutil.protopeer.FingerDescriptor;
import event.Event;
import input.Backend;
import static input.Backend.INTERPSS;
import static input.Backend.MATPOWER;
import flow_analysis.FlowBackendInterface;
import input.Domain;
import static input.Domain.GAS;
import static input.Domain.POWER;
import static input.Domain.TRANSPORTATION;
import static input.Domain.WATER;
import input.EventLoader;
import input.FilesConfiguration;
import input.SystemParameter;
import input.TopologyLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import network.FlowNetwork;
import network.Link;
import network.LinkState;
import network.Node;
import network.NodeState;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import output.TopologyWriter;
import power.flow_analysis.InterpssFlowBackend;
import power.flow_analysis.MATPOWERFlowBackend;
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
    private boolean printInfoToConsole = true;
    
    private String experimentID;
    private Time bootstrapTime;
    private Time runTime;
    private int iteration;
    
    private HashMap<FilesConfiguration, String> fileConfiguration;
    
    private String peersLogDirectory;
    private String timeToken;
    private String timeTokenName;
    private String experimentConfigurationFilesLocation;
    private String experimentOutputFilesLocation;
    private String nodesLocation;
    private String linksLocation;
    private String nodesFlowLocation;
    private String linksFlowLocation;
    private String eventsLocation;
    private String columnSeparator;
    private String missingValue;
    private HashMap<SystemParameter,Object> systemParameters;
    private FlowNetwork flowNetwork;
    private TopologyLoader topologyLoader;
    private EventLoader eventLoader;
    private FingerDescriptor myAgentDescriptor;
    private MeasurementFileDumper measurementDumper;
    private Domain domain;
    private Backend backend;
    private ArrayList<Event> events;
    
    // Measurement variables
    private HashMap<Integer,HashMap<String,HashMap<Metrics,Object>>> temporalLinkMetrics;
    private HashMap<Integer,HashMap<String,HashMap<Metrics,Object>>> temporalNodeMetrics;
    private HashMap<Integer,HashMap<Metrics,Object>> temporalSystemMetrics;
    
    public SFINAAgent(
            String experimentID, 
            String peersLogDirectory, 
            Time bootstrapTime, 
            Time runTime, 
            String timeTokenName, 
            String experimentConfigurationFilesLocation, 
            String experimentOutputFilesLocation,
            String nodesLocation, 
            String linksLocation, 
            String nodesFlowLocation, 
            String linksFlowLocation, 
            String eventsLocation, 
            String columnSeparator, 
            String missingValue,
            HashMap simulationParameters){
        this.experimentID=experimentID;
        this.peersLogDirectory=peersLogDirectory;
        this.bootstrapTime=bootstrapTime;
        this.runTime=runTime;
        this.timeTokenName=timeTokenName;
        this.experimentConfigurationFilesLocation=experimentConfigurationFilesLocation;
        this.experimentOutputFilesLocation=experimentOutputFilesLocation;
        this.iteration=1;
        this.nodesLocation=nodesLocation;
        this.linksLocation=linksLocation;
        this.nodesFlowLocation=nodesFlowLocation;
        this.linksFlowLocation=linksFlowLocation;
        this.eventsLocation=eventsLocation;
        this.columnSeparator=columnSeparator;
        this.missingValue=missingValue;
        this.systemParameters=simulationParameters;
        this.temporalLinkMetrics=new HashMap();
        this.temporalNodeMetrics=new HashMap();
        this.temporalSystemMetrics=new HashMap();
        this.flowNetwork=new FlowNetwork();
        this.topologyLoader=new TopologyLoader(flowNetwork, this.columnSeparator);
        this.timeToken=this.timeTokenName+Time.inSeconds(0).toString();
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
     */
    @Override
    public void runBootstraping(){
        Timer loadAgentTimer= getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                timeToken=timeTokenName+(getSimulationTime());
                if (systemParameters.containsKey(SystemParameter.DOMAIN))
                    domain = (Domain)systemParameters.get(SystemParameter.DOMAIN);
                else 
                    logger.debug("Domain not specified.");
                if (systemParameters.containsKey(SystemParameter.BACKEND))
                    backend = (Backend)systemParameters.get(SystemParameter.BACKEND);
                else
                    logger.debug("Backend not specified.");
                loadInputData();
                eventLoader=new EventLoader(domain,columnSeparator,missingValue);
                events=eventLoader.loadEvents(eventsLocation);
                clearOutputFiles(new File(experimentOutputFilesLocation));
                runActiveState();
            }
        });
        loadAgentTimer.schedule(this.bootstrapTime);
    }
    
    /**
     * It clears the directory with the output files.
     * 
     * @param experiment 
     */
    private final static void clearOutputFiles(File experiment){
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
     * The scheduling of the active state.  It is executed periodically. 
     * 
     * This is the fundamental prototype of the simulation runtime. It should
     * stay generic. At this moment, the runtime concerns the following:
     * 
     * 1. Counting simulation time.
     * 2. Checking and loading new data from files.
     * 
     */
    @Override
    public void runActiveState(){
        Timer loadAgentTimer=getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                timeToken=timeTokenName+(getSimulationTime());
                logger.log(Priority.INFO, "\n------------------------------------\n-------------- " + timeToken + " --------------");
                
                resetIteration();
                
                loadInputData();
                
                initMeasurements();
                performInitialStateOperations();
                
                executeAllEvents(getSimulationTime());
                
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
    
    private void loadInputData(){
        File file = new File(experimentConfigurationFilesLocation+timeToken);
        if (file.exists() && file.isDirectory()) {
            if(this.getIfConsoleOutput()) System.out.println("loading data at time " + timeToken);
            logger.log(Priority.INFO, "loading data at time " + timeToken);
            topologyLoader.loadNodes(experimentConfigurationFilesLocation+timeToken+nodesLocation);
            topologyLoader.loadLinks(experimentConfigurationFilesLocation+timeToken+linksLocation);
            switch(domain){
                case POWER:
                    PowerFlowLoader flowLoader=new PowerFlowLoader(flowNetwork, columnSeparator, missingValue);
                    flowLoader.loadNodeFlowData(experimentConfigurationFilesLocation+timeToken+nodesFlowLocation);
                    flowLoader.loadLinkFlowData(experimentConfigurationFilesLocation+timeToken+linksFlowLocation);
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
    }
    
    /**
     * Outputs txt files in same format as input. Automatically creates incremental iterations starting at 1 for each output as long as the current epoch is running. 
     */
    private void outputNetworkData(){
        if(this.getIfConsoleOutput()) System.out.println("doing output at iteration " + iteration);
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
    
    @Override
    public void initMeasurements(){
        HashMap<String,HashMap<Metrics,Object>> linkMetrics=new HashMap<String,HashMap<Metrics,Object>>();
        for(Link link:this.getFlowNetwork().getLinks()){
            HashMap<Metrics,Object> metrics=new HashMap<Metrics,Object>();
            linkMetrics.put(link.getIndex(), metrics);
        }
        this.getTemporalLinkMetrics().put(this.getSimulationTime(), linkMetrics);
        
        HashMap<String,HashMap<Metrics,Object>> nodeMetrics=new HashMap<String,HashMap<Metrics,Object>>();
        for(Node node:this.getFlowNetwork().getNodes()){
            HashMap<Metrics,Object> metrics=new HashMap<Metrics,Object>();
            nodeMetrics.put(node.getIndex(), metrics);
        }
        this.getTemporalNodeMetrics().put(this.getSimulationTime(), nodeMetrics);
        
        this.getTemporalSystemMetrics().put(this.getSimulationTime(), new HashMap<Metrics,Object>());
    }
    
    
    public void executeAllEvents(int time){
        if(this.getIfConsoleOutput()) System.out.println("Executing events");
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
        //EventExecution eventExecution = new EventExecution();
        //eventExecution.execute(flowNetwork, systemParameters, event);
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
                                if(this.getIfConsoleOutput()) System.out.println("..deactivating node " + node.getIndex());
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
                                if(this.getIfConsoleOutput()) System.out.println("..deactivating link " + link.getIndex());
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
                if(this.getIfConsoleOutput()) System.out.println("..changing/setting system param: " + (SystemParameter)event.getParameter());
                switch((SystemParameter)event.getParameter()){
                    case DOMAIN:
                        systemParameters.put(SystemParameter.DOMAIN, (Domain)event.getValue());
                        domain=(Domain)event.getValue();
                        break;
                    case BACKEND:
                        systemParameters.put(SystemParameter.BACKEND, (Backend)event.getValue());
                        backend=(Backend)event.getValue();
                        break;
                    case CAPACITY_CHANGE_LINK:
                        systemParameters.put(SystemParameter.CAPACITY_CHANGE_LINK, (Double)event.getValue());
                        for (Link link : flowNetwork.getLinks())
                            link.setCapacity(link.getCapacity()*(1.0-(Double)systemParameters.get(SystemParameter.CAPACITY_CHANGE_LINK)));
                        break;
                    case CAPACITY_CHANGE_NODE:
                        for (Node node : flowNetwork.getNodes())
                            node.setCapacity(node.getCapacity()*(1.0-(Double)systemParameters.get(SystemParameter.CAPACITY_CHANGE_LINK)));
                        break;
                    default:
                        logger.debug("Simulation parameter cannot be regognized.");
                }
            default:
                logger.debug("Event type cannot be recognised");
        }
    }
     
    /**
     * Iterates islands and calls flowConvergenceStrategy(). Saves the island and a boolean value describing if it converged. Finally outputs the network data. Doesn't call mitigateOverload or linkOverload method.
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
        switch(domain){
            case POWER:
                switch(backend){
                    case MATPOWER:
                        flowBackend=new MATPOWERFlowBackend();
                        converged=flowBackend.flowAnalysis(flowNetwork);
                        break;
                    case INTERPSS:
                        flowBackend=new InterpssFlowBackend();
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
    public Backend getBackend(){
        return backend;
    }
    
    public void setBackend(Backend backend){
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
        this.outputNetworkData();
        this.iteration++;
    }
    
    public int getIteration(){
        return this.iteration;
    }
    
    /**
     * @return the events
     */
    public ArrayList<Event> getEvents() {
        return events;
    }
    
    public HashMap<SystemParameter,Object> getSystemParameters(){
        return systemParameters;
    }
    
    public boolean getIfConsoleOutput(){
        return printInfoToConsole;
    }
    
    //****************** MEASUREMENTS ******************
    
    /**
     * Scheduling the measurements for the simulation agent
     */
    @Override
    public void scheduleMeasurements(){
        this.setMeasurementDumper(new MeasurementFileDumper(getPeersLogDirectory()+this.getExperimentID()+"peer-"+getPeer().getIndexNumber()));
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
     * @return the temporalLinkMetrics
     */
    public HashMap<Integer,HashMap<String,HashMap<Metrics,Object>>> getTemporalLinkMetrics() {
        return temporalLinkMetrics;
    }

    /**
     * @return the temporalNodeMetrics
     */
    public HashMap<Integer,HashMap<String,HashMap<Metrics,Object>>> getTemporalNodeMetrics() {
        return temporalNodeMetrics;
    }
    
    /**
     * 
     * @return the temporalSystemMetrics
     */
    public HashMap<Integer,HashMap<Metrics,Object>> getTemporalSystemMetrics() {
        return temporalSystemMetrics;
    }

    
    /**
     * @param measurementDumper the measurementDumper to set
     */
    public void setMeasurementDumper(MeasurementFileDumper measurementDumper) {
        this.measurementDumper = measurementDumper;
    }
}