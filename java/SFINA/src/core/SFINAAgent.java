package core;

import applications.Metrics;
import dsutil.protopeer.FingerDescriptor;
import event.Event;
import event.EventType;
import event.NetworkComponent;
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
import input.InputParameter;
import input.InputParametersLoader;
import input.TopologyLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import network.FlowNetwork;
import network.Link;
import network.LinkState;
import network.Node;
import network.NodeState;
import org.apache.log4j.Logger;
import output.TopologyWriter;
import power.PowerFlowType;
import power.PowerNodeType;
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author evangelospournaras
 */
public class SFINAAgent extends BasePeerlet implements SimulationAgentInterface{
    
    private static final Logger logger = Logger.getLogger(SFINAAgent.class);
    
    private String experimentID;
    private String peersLogDirectory;
    private Time bootstrapTime;
    private Time runTime;
    private String timeToken;
    private String timeTokenName;
    private String experimentConfigurationFilesLocation;
    private String experimentOutputFilesLocation;
    private String inputParametersLocation;
    private String nodesLocation;
    private String linksLocation;
    private String nodesFlowLocation;
    private String linksFlowLocation;
    private String eventsLocation;
    private String parameterValueSeparator;
    private String columnSeparator;
    private String missingValue;
    private Map<InputParameter,Object> inputParameters;
    private InputParametersLoader inputParametersLoader;
    private FlowNetwork flowNetwork;
    private HashMap<FlowNetwork, Boolean> finalIslands;
    private TopologyLoader topologyLoader;
    private EventLoader eventLoader;
    private FingerDescriptor myAgentDescriptor;
    private MeasurementFileDumper measurementDumper;
    private Domain domain;
    private ArrayList<Event> events;
    
    private HashMap<Integer,HashMap<String,HashMap<Metrics,Object>>> temporalLinkMetrics;
    
    public SFINAAgent(
            String experimentID, 
            String peersLogDirectory, 
            Time bootstrapTime, 
            Time runTime, 
            String timeTokenName, 
            String experimentConfigurationFilesLocation, 
            String experimentOutputFilesLocation,
            String inputParametersLocation, 
            String nodesLocation, 
            String linksLocation, 
            String nodesFlowLocation, 
            String linksFlowLocation, 
            String eventsLocation, 
            String parameterValueSeparator, 
            String columnSeparator, 
            String missingValue){
        this.experimentID=experimentID;
        this.peersLogDirectory=peersLogDirectory;
        this.bootstrapTime=bootstrapTime;
        this.runTime=runTime;
        this.timeTokenName=timeTokenName;
        this.experimentConfigurationFilesLocation=experimentConfigurationFilesLocation;
        this.experimentOutputFilesLocation=experimentOutputFilesLocation;
        this.inputParametersLocation=inputParametersLocation;
        this.nodesLocation=nodesLocation;
        this.linksLocation=linksLocation;
        this.nodesFlowLocation=nodesFlowLocation;
        this.linksFlowLocation=linksFlowLocation;
        this.eventsLocation=eventsLocation;
        this.parameterValueSeparator=parameterValueSeparator;
        this.columnSeparator=columnSeparator;
        this.missingValue=missingValue;
        this.inputParametersLoader=new InputParametersLoader(this.parameterValueSeparator);
        this.temporalLinkMetrics=new HashMap<Integer,HashMap<String,HashMap<Metrics,Object>>>();
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
    * defining its network state
    */
    @Override
    public void start(){
        this.runBootstraping();
    }

    /**
    * Stops the simulation agent
    */
    @Override
    public void stop(){
        
    }
    
    /**
     * The scheduling of the active state. It is executed periodically. 
     */
    @Override
    public void runBootstraping(){
        Timer loadAgentTimer= getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                timeToken=timeTokenName+(getSimulationTime()+1);
                inputParameters=inputParametersLoader.loadInputParameters(inputParametersLocation);
                domain=(Domain)inputParameters.get(InputParameter.DOMAIN);
                loadNetworkData();
                eventLoader=new EventLoader(domain,columnSeparator);
                events=eventLoader.loadEvents(eventsLocation);
                scheduleMeasurements();
                runActiveState();
            }
        });
        loadAgentTimer.schedule(this.bootstrapTime);
    }
    
    /**
     * The scheduling of the active state.  It is executed periodically. 
     */
    @Override
    public void runActiveState(){
        Timer loadAgentTimer= getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                timeToken=timeTokenName+(getSimulationTime()+1);
                loadNetworkData();
                executeAllEvents(getSimulationTime());
                runAnalysis(flowNetwork);
                initMeasurements();
                performMeasurements();
                runActiveState(); 
       }
        });
        loadAgentTimer.schedule(this.runTime);
    }
    
    
    public int getSimulationTime(){
        return (int)(Time.inSeconds(this.getPeer().getClock().getTime())-Time.inSeconds(this.bootstrapTime));
    }
    
    private void loadNetworkData(){
        File file = new File(experimentConfigurationFilesLocation+timeToken);
        if (file.exists() && file.isDirectory()) {
            topologyLoader.loadNodes(experimentConfigurationFilesLocation+timeToken+nodesLocation);
            topologyLoader.loadLinks(experimentConfigurationFilesLocation+timeToken+linksLocation);
            switch(domain){
                case POWER:
                    PowerFlowLoader flowLoader=new PowerFlowLoader(flowNetwork, columnSeparator, missingValue);
                    flowLoader.loadNodeFlowData(experimentConfigurationFilesLocation+timeToken+nodesFlowLocation);
                    flowLoader.loadLinkFlowData(experimentConfigurationFilesLocation+timeToken+linksFlowLocation);
                    flowNetwork.setLinkFlowType(PowerLinkState.POWER_FLOW_FROM_REAL);
                    flowNetwork.setNodeFlowType(PowerNodeState.POWER_DEMAND_REAL);
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
    
    private void outputNetworkData(){
        TopologyWriter topologyWriter = new TopologyWriter(flowNetwork, columnSeparator);
        topologyWriter.writeNodes(experimentOutputFilesLocation+timeToken+nodesLocation);
        topologyWriter.writeLinks(experimentOutputFilesLocation+timeToken+linksLocation);
        switch(domain){
                case POWER:
                    PowerFlowWriter flowLoader=new PowerFlowWriter(flowNetwork, columnSeparator, missingValue);
                    flowLoader.writeNodeFlowData(experimentOutputFilesLocation+timeToken+nodesFlowLocation);
                    flowLoader.writeLinkFlowData(experimentOutputFilesLocation+timeToken+linksFlowLocation);
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
    public void performMeasurements(){
        
    }
    
    @Override
    public void initMeasurements(){
        HashMap<String,HashMap<Metrics,Object>> linkMetrics=new HashMap<String,HashMap<Metrics,Object>>();
        for(Link link:this.getFlowNetwork().getLinks()){
            HashMap<Metrics,Object> metrics=new HashMap<Metrics,Object>();
            linkMetrics.put(link.getIndex(), metrics);
        }
        this.getTemporalLinkMetrics().put(this.getSimulationTime(), linkMetrics);
    }
    
    
    public void executeAllEvents(int time){
        boolean TopologyChange = false;
        Iterator<Event> i = events.iterator();
        while (i.hasNext()){
            Event event = i.next();
            if(event.getTime() == time){
                this.executeEvent(flowNetwork, event);
                if (event.getEventType() == EventType.TOPOLOGY)
                    TopologyChange = true;
                i.remove(); // removes event from the events list, to avoid being executed several times. this is favorable for efficiency (especially for checking islanding, however probably not necessary
            }
        }
//        if (TopologyChange)
//            checkIslands();
//        
//        for(Event event:events){
//            if(event.getTime()==time){
//                this.executeEvent(flowNetwork, event);
//            }
//        }
    }
    
    @Override
    public void executeEvent(FlowNetwork flowNetwork, Event event){
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
                                node.setActivated((Boolean)event.getValue());
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
                                link.setActivated((Boolean)event.getValue());
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
            default:
                logger.debug("Event type cannot be recognised");
        }
    }
    
    @Override
    public void runAnalysis(FlowNetwork flowNetwork){
        ArrayList<FlowNetwork> islands = flowNetwork.getIslands();
        for (FlowNetwork island : islands){
            boolean islandConverged = handleConvergence(island); // handels convergence itself. returns false if blackout.
            if (islandConverged){
                boolean overload = handleOverload(island);
                if(overload)
                    runAnalysis(island);
                finalIslands.put(island, true);
            }
            else
                finalIslands.put(island, false); // blackout
        }
    }
    
    @Override
    public boolean handleConvergence(FlowNetwork flowNetwork){
        boolean converged = false;
        Domain domain=(Domain)this.inputParameters.get(InputParameter.DOMAIN);
        switch(domain){
            case POWER:
                ArrayList<Node> generators = new ArrayList<>();
                for (Node node : flowNetwork.getNodes()){
                    if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.GENERATOR))
                        generators.add(node);
                    // Set Slack to limits and make normal generator. It will not be considered for generation balancing later.
                    if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.SLACK_BUS)){ // Set Slack to limits and make normal generator
                        node.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.GENERATOR);
                        if ((Double)node.getProperty(PowerNodeState.POWER_GENERATION_REAL) > (Double)node.getProperty(PowerNodeState.POWER_MAX_REAL))
                            node.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, node.getProperty(PowerNodeState.POWER_MAX_REAL));
                        if ((Double)node.getProperty(PowerNodeState.POWER_GENERATION_REAL) < (Double)node.getProperty(PowerNodeState.POWER_MIN_REAL))
                            node.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, node.getProperty(PowerNodeState.POWER_MIN_REAL));
                        if ((Double)node.getProperty(PowerNodeState.POWER_GENERATION_REACTIVE) > (Double)node.getProperty(PowerNodeState.POWER_MAX_REACTIVE))
                            node.replacePropertyElement(PowerNodeState.POWER_GENERATION_REACTIVE, node.getProperty(PowerNodeState.POWER_MAX_REACTIVE));
                        if ((Double)node.getProperty(PowerNodeState.POWER_GENERATION_REACTIVE) < (Double)node.getProperty(PowerNodeState.POWER_MIN_REACTIVE))
                            node.replacePropertyElement(PowerNodeState.POWER_GENERATION_REACTIVE, node.getProperty(PowerNodeState.POWER_MIN_REACTIVE));
                    }
                }
                int genIterator = 0;
                int nrGen = generators.size();
                int loadIterator = 0;
                int maxLoadShedIterations = 20;
                double loadReductionFactor = 0.05;
                
                // blackout if no generator in island
                if (nrGen == 0)
                    return false; 
                
                // Gen balancing
                while (!converged && genIterator < nrGen){
                    Node currentGen = generators.get(genIterator);
                    currentGen.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.SLACK_BUS);
                    converged = runFlowAnalysis(flowNetwork);
                    if ((Double)currentGen.getProperty(PowerNodeState.POWER_GENERATION_REAL) > (Double)currentGen.getProperty(PowerNodeState.POWER_MAX_REAL)){
                        currentGen.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, currentGen.getProperty(PowerNodeState.POWER_MAX_REAL));
                        converged = false; // limits violated
                    }
                    if ((Double)currentGen.getProperty(PowerNodeState.POWER_GENERATION_REAL) < (Double)currentGen.getProperty(PowerNodeState.POWER_MIN_REAL)){
                        currentGen.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, currentGen.getProperty(PowerNodeState.POWER_MIN_REAL));
                        converged = false; // limits violated
                    }
                    if ((Double)currentGen.getProperty(PowerNodeState.POWER_GENERATION_REACTIVE) > (Double)currentGen.getProperty(PowerNodeState.POWER_MAX_REACTIVE)){
                        currentGen.replacePropertyElement(PowerNodeState.POWER_GENERATION_REACTIVE, currentGen.getProperty(PowerNodeState.POWER_MAX_REACTIVE));
                        converged = false; // limits violated
                    }
                    if ((Double)currentGen.getProperty(PowerNodeState.POWER_GENERATION_REACTIVE) < (Double)currentGen.getProperty(PowerNodeState.POWER_MIN_REACTIVE)){
                        currentGen.replacePropertyElement(PowerNodeState.POWER_GENERATION_REACTIVE, currentGen.getProperty(PowerNodeState.POWER_MIN_REACTIVE));
                        converged = false; // limits violated
                    }
                    genIterator++;
                }
                
                // Load shedding
                while (!converged && loadIterator < maxLoadShedIterations){
                    for (Node node : flowNetwork.getNodes()){
                        node.replacePropertyElement(PowerNodeState.POWER_DEMAND_REAL, (Double)node.getProperty(PowerNodeState.POWER_DEMAND_REAL)*(1.0-loadReductionFactor));
                        node.replacePropertyElement(PowerNodeState.POWER_DEMAND_REACTIVE, (Double)node.getProperty(PowerNodeState.POWER_DEMAND_REACTIVE)*(1.0-loadReductionFactor));
                    }
                    converged = runFlowAnalysis(flowNetwork);
                    loadIterator++;
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
    
    @Override
    public boolean handleOverload(FlowNetwork flowNetwork){
        boolean overloaded = false;
        for (Link link : flowNetwork.getLinks()){
            if(link.getFlow() > link.getCapacity()){
                Event event = new Event(getSimulationTime(),EventType.TOPOLOGY,NetworkComponent.LINK,link.getIndex(),LinkState.STATUS,false);
                events.add(event);
                overloaded = true;
            }
        }
        for (Node node : flowNetwork.getNodes()){
            if(node.getFlow() > node.getCapacity()){
                Event event = new Event(getSimulationTime(),EventType.TOPOLOGY,NetworkComponent.NODE,node.getIndex(),NodeState.STATUS,false);
                events.add(event);
                overloaded = true;
            }
        }
        if (overloaded)
            executeAllEvents(getSimulationTime());
        return overloaded;
    }
    
    
    @Override
    public boolean runFlowAnalysis(FlowNetwork flowNetwork){
        FlowBackendInterface flowBackend;
        Domain domain=(Domain)this.inputParameters.get(InputParameter.DOMAIN);
        Backend backend=(Backend)this.inputParameters.get(InputParameter.BACKEND);
        switch(domain){
            case POWER:
                switch(backend){
                    case MATPOWER:
                        flowBackend=new MATPOWERFlowBackend((PowerFlowType)this.inputParameters.get(InputParameter.FLOW_TYPE));
                        flowBackend.flowAnalysis(flowNetwork);
                        return flowBackend.isConverged();
                    case INTERPSS:
                        flowBackend=new InterpssFlowBackend((PowerFlowType)this.inputParameters.get(InputParameter.FLOW_TYPE));
                        flowBackend.flowAnalysis(flowNetwork);
                        return flowBackend.isConverged();
                    default:
                        logger.debug("Flow backend is not supported at this moment.");
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
        return false;
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
     * @param measurementDumper the measurementDumper to set
     */
    public void setMeasurementDumper(MeasurementFileDumper measurementDumper) {
        this.measurementDumper = measurementDumper;
    }
}
