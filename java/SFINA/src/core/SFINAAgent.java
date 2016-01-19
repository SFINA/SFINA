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
import input.AttackStrategy;
import input.Domain;
import static input.Domain.GAS;
import static input.Domain.POWER;
import static input.Domain.TRANSPORTATION;
import static input.Domain.WATER;
import input.EventLoader;
import input.SystemParameter;
import input.TopologyLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
    private int iteration;
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
    private HashMap<Integer,LinkedHashMap<FlowNetwork, Boolean>> temporalIslandStatus;
    private HashMap<Integer,HashMap<String,Object>> initialLoadPerEpoch;
    private HashMap<Integer,HashMap<Integer,Object>> flowSimuTime;
    private HashMap<Integer,Object> totalSimuTime;
    private HashMap<Integer,Integer> nrFlowSimuCalled;
    
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
        this.iteration=0;
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
        this.initialLoadPerEpoch=new HashMap();
        this.flowSimuTime = new HashMap();
        this.totalSimuTime = new HashMap();
        this.nrFlowSimuCalled = new HashMap();
        this.flowNetwork=new FlowNetwork();
        this.temporalIslandStatus=new HashMap();
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
                //scheduleMeasurements();
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
     * 2. Checking and loading new 
     * 
     */
    @Override
    public void runActiveState(){
        Timer loadAgentTimer=getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                timeToken=timeTokenName+(getSimulationTime());
                System.out.println("\n------------------------------------\n-------------- " + timeToken + " --------------");
                temporalIslandStatus.put(getSimulationTime(), new LinkedHashMap());
                iteration=0;
                nrFlowSimuCalled.put(getSimulationTime(), 0);
                flowSimuTime.put(getSimulationTime(), new HashMap());
                long simulationStartTime = System.currentTimeMillis();
                
                loadInputData();
                
                // It seems that this method should not be here:
                saveInitialLoad();
                
                executeAllEvents(getSimulationTime());
                
                runFlowAnalysis();
                
                totalSimuTime.put(getSimulationTime(), System.currentTimeMillis()-simulationStartTime);
                
                // It seems that we also do not need this
                printFinalIslands();
                //printFinalIslandsFromNetworkObject();
                
                initMeasurements();
                performMeasurements();
                runActiveState(); 
            }
        });
        loadAgentTimer.schedule(this.runTime);
    }

        
    
    
    
    public HashMap<String, Object> getInitialLoadPerEpoch(int epochNumber){
        return initialLoadPerEpoch.get(epochNumber);
    }
    
    public int getSimulationTime(){
        return (int)(Time.inSeconds(this.getPeer().getClock().getTime())-Time.inSeconds(this.bootstrapTime));
    }
    
    private void loadInputData(){
        File file = new File(experimentConfigurationFilesLocation+timeToken);
        if (file.exists() && file.isDirectory()) {
            System.out.println("loading data at time " + timeToken);
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
                    this.adjustCapacityBySystemParameter();
                    if (!systemParameters.containsKey(SystemParameter.FLOW_TYPE)){
                        logger.debug("Flow Type not specified.");
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
        }
    }
    
    /**
     * Sets capacity according to SystemParameter user inputs. Setting the capacity by tolerance parameter if doesn't exist or is 0 in loaded data. Reduce capacity if specified in SystemParameters.
     */
    private void adjustCapacityBySystemParameter(){
        runFlowAnalysis(flowNetwork);
        if (!systemParameters.containsKey(SystemParameter.TOLERANCE_PARAMETER)){
            Double tolParam = 2.0;
            systemParameters.put(SystemParameter.TOLERANCE_PARAMETER, tolParam);
            logger.debug("SystemParameter.TOLERANCE_PARAMETER not specified, automatically set to " + tolParam + ".");
        }
        events.add(new Event(getSimulationTime(),EventType.SYSTEM,null,null,SystemParameter.TOLERANCE_PARAMETER,systemParameters.get(SystemParameter.TOLERANCE_PARAMETER)));
        if (systemParameters.containsKey(SystemParameter.CAPACITY_CHANGE))
            events.add(new Event(getSimulationTime(),EventType.SYSTEM,null,null,SystemParameter.CAPACITY_CHANGE,systemParameters.get(SystemParameter.CAPACITY_CHANGE)));
        executeAllEvents(getSimulationTime());
    }
    
    public void outputNetworkData(){
        System.out.println("doing output at iteration " + iteration);
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
        
        HashMap<String,HashMap<Metrics,Object>> nodeMetrics=new HashMap<String,HashMap<Metrics,Object>>();
        for(Node node:this.getFlowNetwork().getNodes()){
            HashMap<Metrics,Object> metrics=new HashMap<Metrics,Object>();
            nodeMetrics.put(node.getIndex(), metrics);
        }
        this.getTemporalNodeMetrics().put(this.getSimulationTime(), nodeMetrics);
    }
    
    
    public void executeAllEvents(int time){
        System.out.println("Executing events");
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
                                System.out.println("..deactivating node " + node.getIndex());
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
                                System.out.println("..deactivating link " + link.getIndex());
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
                System.out.println("..changing/setting system param: " + (SystemParameter)event.getParameter());
                switch((SystemParameter)event.getParameter()){
                    case DOMAIN:
                        systemParameters.put(SystemParameter.DOMAIN, (Domain)event.getValue());
                        domain=(Domain)event.getValue();
                        break;
                    case BACKEND:
                        systemParameters.put(SystemParameter.BACKEND, (Backend)event.getValue());
                        backend=(Backend)event.getValue();
                        break;
                    case FLOW_TYPE:
                        systemParameters.put(SystemParameter.FLOW_TYPE, (PowerFlowType)event.getValue());
                        break;
                    case TOLERANCE_PARAMETER:
                        systemParameters.put(SystemParameter.TOLERANCE_PARAMETER, (Double)event.getValue());
                        for (Link link : flowNetwork.getLinks()){
                            Double capacity = link.getCapacity();
                            if (capacity == null || capacity == 0.0){
                                link.setCapacity((Double)systemParameters.get(SystemParameter.TOLERANCE_PARAMETER)*link.getFlow());
                            }
                        }
//                        for (Node node : flowNetwork.getNodes()){
//                            Double capacity = node.getCapacity();
//                            if (capacity == null || capacity == 0.0)
//                                node.setCapacity((Double)systemParameters.get(SystemParameter.TOLERANCE_PARAMETER)*node.getFlow());
//                        }
                        break;
                    case ATTACK_STRATEGY:
                        systemParameters.put(SystemParameter.ATTACK_STRATEGY, (AttackStrategy)event.getValue());
                        break;
                    case CAPACITY_CHANGE:
                        systemParameters.put(SystemParameter.CAPACITY_CHANGE, (Double)event.getValue());
                        for (Link link : flowNetwork.getLinks())
                            link.setCapacity(link.getCapacity()*(1.0-(Double)systemParameters.get(SystemParameter.CAPACITY_CHANGE)));
//                        for (Node node : flowNetwork.getNodes())
//                            node.setCapacity(node.getCapacity()*(1.0-(Double)systemParameters.get(SystemParameter.CAPACITY_CHANGE)));
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
        iteration = 1;
        for(FlowNetwork currentIsland : flowNetwork.computeIslands()){
            boolean converged = flowConvergenceStrategy(currentIsland);
            temporalIslandStatus.get(getSimulationTime()).put(currentIsland, converged);
        }
        outputNetworkData();
    }
    
    /**
     * Domain specific strategy and/or necessary adjustments before loadflow simulation is executed. 
     * - Power: Most simple strategy: Not converged if isolated node. For current island, turn first generator into slack if it doesn't exist yet. 
     * @param flowNetwork
     * @return true if flow analysis finally converged, else false
     */
    @Override 
    public boolean flowConvergenceStrategy(FlowNetwork flowNetwork){
        switch(domain){
            case POWER:
                if(flowNetwork.getNodes().size() == 1)
                    return false;
                ArrayList<Node> generators = new ArrayList();
                boolean hasSlack = false;
                for(Node node : flowNetwork.getNodes()){
                    if(node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.SLACK_BUS)){
                        hasSlack = true;
                    }
                    if(node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.GENERATOR)){
                        generators.add(node);
                    } 
                }
                if(!hasSlack && !generators.isEmpty())
                    generators.get(0).replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.SLACK_BUS);
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
        return runFlowAnalysis(flowNetwork);
    }
    
    /**
     * Executes domain and backend specific flow analysis. Currently implemented for power domain: Matpower and InterPSS.
     * @param flowNetwork
     * @return true if converged, else false.
     */
    @Override
    public boolean runFlowAnalysis(FlowNetwork flowNetwork){
        FlowBackendInterface flowBackend;
        boolean converged = false;
        long analysisStartTime = System.currentTimeMillis();
        nrFlowSimuCalled.put(getSimulationTime(), nrFlowSimuCalled.get(getSimulationTime())+1);
        switch(domain){
            case POWER:
                switch(backend){
                    case MATPOWER:
                        flowBackend=new MATPOWERFlowBackend((PowerFlowType)this.systemParameters.get(SystemParameter.FLOW_TYPE));
                        converged=flowBackend.flowAnalysis(flowNetwork);
                        break;
                    case INTERPSS:
                        flowBackend=new InterpssFlowBackend((PowerFlowType)this.systemParameters.get(SystemParameter.FLOW_TYPE));
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
        long analysisEndTime = System.currentTimeMillis();
        flowSimuTime.get(getSimulationTime()).put(iteration,analysisEndTime-analysisStartTime);
        return converged;
    }
    
    /**
     * Method to mitigate overload. Strategy to respond to (possible) overloading can be implemented here. This method is called before the OverLoadAlgo is called which deactivates affected links/nodes.
     * @param flowNetwork 
     */
    @Override
    public void mitigateOverload(FlowNetwork flowNetwork){
    }
    
    /**
     * Checks link limits. If a limit is violated, an event is executed which deactivates the link.
     * @param flowNetwork
     * @return 
     */
    @Override
    public boolean linkOverload(FlowNetwork flowNetwork){
        boolean overloaded = false;
        for (Link link : flowNetwork.getLinks()){
            if(link.isActivated() && link.getFlow() > link.getCapacity()){
                System.out.println("..violating link " + link.getIndex() + ": " + link.getFlow() + " > " + link.getCapacity());
                Event event = new Event(getSimulationTime(),EventType.TOPOLOGY,NetworkComponent.LINK,link.getIndex(),LinkState.STATUS,false);
                events.add(event);
                overloaded = true;
            }
        }
        if (overloaded)
            executeAllEvents(getSimulationTime());
        return overloaded;
    }
    
    @Override
    public boolean nodeOverload(FlowNetwork flowNetwork){
        boolean overloaded = false;
        for (Node node : flowNetwork.getNodes()){
            if(node.isActivated() && node.getFlow() > node.getCapacity()){
                System.out.println("..violating node " + node.getIndex() + ": " + node.getFlow() + " > " + node.getCapacity());
                Event event = new Event(getSimulationTime(),EventType.TOPOLOGY,NetworkComponent.NODE,node.getIndex(),NodeState.STATUS,false);
                events.add(event);
                overloaded = true;
            }
        }
        if (overloaded)
            executeAllEvents(getSimulationTime());
        return overloaded;
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
    
    public void setCurrentIteration(Integer iter){
        this.iteration=iter;
    }
    
    public HashMap<SystemParameter,Object> getSystemParameters(){
        return systemParameters;
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
     * 
     * @return the temporalIslandStatus.
     */
    public HashMap<Integer,LinkedHashMap<FlowNetwork, Boolean>> getTemporalIslandStatus(){
        return temporalIslandStatus;
    }
    
    /**
     * 
     * @return time of flow simulation in Milliseconds per time and iteration.
     */
    public HashMap<Integer,HashMap<Integer,Object>> getFlowSimuTime(){
        return flowSimuTime;
    }
    
    /**
     * 
     * @return time of whole simulation per epoch
     */
    public HashMap<Integer,Object> getTotalSimuTime(){
        return totalSimuTime;
    }
    
    /**
     * @param measurementDumper the measurementDumper to set
     */
    public void setMeasurementDumper(MeasurementFileDumper measurementDumper) {
        this.measurementDumper = measurementDumper;
    }
    
    // *************************** Test Methods ********************************
    
    /**
     * Prints final islands in each time step to console
     */
    private void printFinalIslands(){
        System.out.println("--------------------------------------\n" + temporalIslandStatus.get(getSimulationTime()).size() + " final islands:");
        String nodesInIsland;
        for (FlowNetwork net : temporalIslandStatus.get(getSimulationTime()).keySet()){
            nodesInIsland = "";
            for (Node node : net.getNodes())
                nodesInIsland += node.getIndex() + ", ";
            System.out.print(net.getNodes().size() + " Node(s) (" + nodesInIsland + ")");
            if(temporalIslandStatus.get(getSimulationTime()).get(net))
                System.out.print(" -> Converged :)\n");
            if(!temporalIslandStatus.get(getSimulationTime()).get(net))
                System.out.print(" -> Blackout\n");
        }
    }
    
    private void printFinalIslandsFromNetworkObject(){
        System.out.println("--------------------------------------(From network method)\n" + temporalIslandStatus.get(getSimulationTime()).size() + " final islands:");
        String nodesInIsland;
        HashMap<FlowNetwork, Boolean> finalIslands = flowNetwork.getFinalIslands();
        for (FlowNetwork net : finalIslands.keySet()){
            nodesInIsland = "";
            for (Node node : net.getNodes())
                nodesInIsland += node.getIndex() + ", ";
            System.out.print(net.getNodes().size() + " Node(s) (" + nodesInIsland + ")");
            if(finalIslands.get(net))
                System.out.print(" -> Converged :)\n");
            if(!finalIslands.get(net))
                System.out.print(" -> Blackout\n");
        }
    }
    
    /**
     * Saves the load in Power Analysis. Executed at beginning of each epoch. 
     */
    private void saveInitialLoad(){
        initialLoadPerEpoch.put(getSimulationTime(), new HashMap<String,Object>());
        for (Node node : flowNetwork.getNodes()){
            if(node.isActivated() && node.isConnected())
                initialLoadPerEpoch.get(getSimulationTime()).put(node.getIndex(), node.getProperty(PowerNodeState.POWER_DEMAND_REAL));
            else
                initialLoadPerEpoch.get(getSimulationTime()).put(node.getIndex(), 0.0);
        }
    }
    
    // *************************************************************************
}
