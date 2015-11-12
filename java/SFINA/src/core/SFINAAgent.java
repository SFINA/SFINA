package core;

import applications.Metrics;
import dsutil.protopeer.FingerDescriptor;
import event.Event;
import event.EventExecution;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
    private LinkedHashMap<FlowNetwork, Boolean> finalIslands;
    private TopologyLoader topologyLoader;
    private EventLoader eventLoader;
    private FingerDescriptor myAgentDescriptor;
    private MeasurementFileDumper measurementDumper;
    private Domain domain;
    private Backend backend;
    private ArrayList<Event> events;
    
    private HashMap<Integer,HashMap<String,HashMap<Metrics,Object>>> temporalLinkMetrics;
    private HashMap<Integer,HashMap<String,HashMap<Metrics,Object>>> temporalNodeMetrics;
    private HashMap<Integer,HashMap<String,Object>> initialLoadPerEpoch;
    private HashMap<Integer,HashMap<Integer,Object>> flowSimuTime;
    
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
        this.initialLoadPerEpoch=new HashMap();
        this.flowSimuTime = new HashMap();
        this.flowNetwork=new FlowNetwork();
        this.finalIslands=new LinkedHashMap();
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
     * The scheduling of the active state. It is executed periodically. 
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
                loadNetworkData();
                eventLoader=new EventLoader(domain,columnSeparator,missingValue);
                events=eventLoader.loadEvents(eventsLocation);
                //scheduleMeasurements();
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
        Timer loadAgentTimer=getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                timeToken=timeTokenName+(getSimulationTime());
                finalIslands.clear();
                iteration=0;
                flowSimuTime.put(getSimulationTime(), new HashMap());
                
                System.out.println("\n------------------------------------\n-------------- " + timeToken + " --------------");
                
                loadNetworkData();
                
                saveInitialLoad();
                
                executeAllEvents(getSimulationTime());
                
                runCascade();
                
                // Print final steady state after cascade
                System.out.println("--------------------------------------\n" + finalIslands.size() + " final islands:");
                String nodesInIsland;
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
                //for(int i=0; i<iteration;i++)
                //    System.out.println("simu time iteration " + i + ": " + flowSimuTime.get(getSimulationTime()).get(i));
                
                initMeasurements();
                performMeasurements();
                runActiveState(); 
       }
        });
        loadAgentTimer.schedule(this.runTime);
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
    
    public HashMap<String, Object> getInitialLoadPerEpoch(int epochNumber){
        return initialLoadPerEpoch.get(epochNumber);
    }
    
    public int getSimulationTime(){
        return (int)(Time.inSeconds(this.getPeer().getClock().getTime())-Time.inSeconds(this.bootstrapTime));
    }
    
    private void loadNetworkData(){
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
                    if (!systemParameters.containsKey(SystemParameter.FLOW_TYPE)){
                        logger.debug("Flow Type not specified. Setting to AC.");
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
    
    private void outputNetworkData(){
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
                                node.setActivated((Boolean)event.getValue()); // This doesn't reevaluate the status of connected links
                                System.out.println("..deactivating node " + node.getIndex());
                                // Proper way, but can we make it simpler?
//                                if((Boolean)event.getValue())
//                                    flowNetwork.activateNode(node.getIndex());
//                                else if(!(Boolean)event.getValue())
//                                    flowNetwork.deactivateNode(node.getIndex());
//                                else
//                                    logger.debug("Node status cannot be recognised");
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
                                // link.setActivated((Boolean)event.getValue()); // see above for nodes
                                System.out.println("..deactivating link " + link.getIndex());
                                if((Boolean)event.getValue())
                                    flowNetwork.activateLink(link.getIndex());
                                else if(!(Boolean)event.getValue())
                                    flowNetwork.deactivateLink(link.getIndex());
                                else
                                    logger.debug("Node status cannot be recognised");
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
                System.out.println("..changing " + (SystemParameter)event.getParameter());
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
                        break;
                    case ATTACK_STRATEGY:
                        systemParameters.put(SystemParameter.ATTACK_STRATEGY, (AttackStrategy)event.getValue());
                        break;
                    case LINE_RATE_CHANGE_FACTOR:
                        systemParameters.put(SystemParameter.LINE_RATE_CHANGE_FACTOR, (Double)event.getValue());
                        for (Link link : flowNetwork.getLinks())
                            link.setCapacity(link.getCapacity()*(1.0-(Double)systemParameters.get(SystemParameter.LINE_RATE_CHANGE_FACTOR)));
                        break;
                    default:
                        logger.debug("Simulation parameter cannot be regognized.");
                }
            default:
                logger.debug("Event type cannot be recognised");
        }
    }
     
    @Override
    public void runCascade(){
        int iter = 0;
        ArrayList<ArrayList<FlowNetwork>> islandBuffer = new ArrayList<>(); // row index is iteration, each entry is island to be treated at this iteration
        islandBuffer.add(flowNetwork.getIslands());
        while(!islandBuffer.get(iter).isEmpty()){
            System.out.println("---------------------\n---- Iteration " + (iter+1) + " ----");
            islandBuffer.add(new ArrayList<>()); // List of islands for next iteration (iter+1)
            for(int i=0; i < islandBuffer.get(iter).size(); i++){ // go through islands at current iteration
                FlowNetwork currentIsland = islandBuffer.get(iter).get(i);
                System.out.println("---> Treating island with " + currentIsland.getNodes().size() + " nodes.");
                
                boolean converged = flowConvergenceAlgo(currentIsland); // do flow analysis
                System.out.println("=> converged " + converged);
                if (converged){
                    
                    // if mitigation strategy is implemented
                    mitigateOverload(currentIsland);
                    
                    boolean linkOverloaded = linkOverloadAlgo(currentIsland);
                    //boolean nodeOverloaded = nodeOverloadAlgo(currentIsland);
                    System.out.println("=> overloaded " + linkOverloaded);
                    if(linkOverloaded){
                        // add islands of the current island to next iteration
                        for (FlowNetwork net : currentIsland.getIslands())
                            islandBuffer.get(iter+1).add(net);
                    }
                    else
                        finalIslands.put(currentIsland, true);
                }
                else{
                    finalIslands.put(currentIsland, false);
                    for (Node node : currentIsland.getNodes())
                        node.setActivated(false);
                }
            }
            
            // Output network snapshot of current iteration
            iteration = iter+1;
            outputNetworkData(); 
            
            // Go to next iteration if there were islands added to it
            iter++;
        }
        
    }
    
    @Override
    public boolean flowConvergenceAlgo(FlowNetwork flowNetwork){
        boolean converged = false;
        switch(domain){
            case POWER:
                // blackout if isolated node
                if(flowNetwork.getNodes().size() == 1)
                    return false;
                
                // first implementation
                //converged = powerGenLimitAlgo(flowNetwork);
                
                // Jose's implementation
                ArrayList<Node> generators = new ArrayList();
                Node slack = null;
                for (Node node : flowNetwork.getNodes()){
                    if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.GENERATOR))
                        generators.add(node);
                    if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.SLACK_BUS)){
                        slack = node;
                    }
                }
                // Sort generators by max power output
                Collections.sort(generators, new Comparator<Node>(){
                    public int compare(Node node1, Node node2) {
                    return Double.compare((Double)node1.getProperty(PowerNodeState.POWER_MAX_REAL), (Double)node2.getProperty(PowerNodeState.POWER_MAX_REAL));
                    }
                }.reversed());
                
                if (slack == null){
                    if (generators.size() == 0)
                        return false; // blackout if no generator in island
                    else{
                        slack = generators.get(0);
                        slack.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.SLACK_BUS);
                        generators.remove(0);
                    }
                }
                
                boolean limViolation = true;
                converged = runFlowAnalysis(flowNetwork);
                while(limViolation){
                    System.out.println("....converged " + converged);
                    if (converged){
                        limViolation = powerGenLimitAlgo2(flowNetwork, slack);
                        if (limViolation){
                            converged = false;
                            if(generators.size() > 0){ // make next bus a slack
                                slack = generators.get(0);
                                slack.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.SLACK_BUS);
                                generators.remove(0);
                            }
                            else{
                                System.out.println("....no more generators");
                                return false; // all generator limits were hit -> blackout
                            }
                        }
                    }
                    else{
                        converged = powerLoadShedAlgo(flowNetwork);
                        if (!converged)
                            return false; // blackout if no convergence after load shedding
                    }
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
    
    public boolean powerGenLimitAlgo2(FlowNetwork flowNetwork, Node slack){
        boolean limViolation = false;
        if ((Double)slack.getProperty(PowerNodeState.POWER_GENERATION_REAL) > (Double)slack.getProperty(PowerNodeState.POWER_MAX_REAL)){
            slack.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, slack.getProperty(PowerNodeState.POWER_MAX_REAL));
            limViolation = true;
        }
        if ((Double)slack.getProperty(PowerNodeState.POWER_GENERATION_REAL) < (Double)slack.getProperty(PowerNodeState.POWER_MIN_REAL)){
            slack.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, slack.getProperty(PowerNodeState.POWER_MIN_REAL));
            limViolation = true;
        }
        slack.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.GENERATOR);
        if (limViolation)
            System.out.println("....generator limit violated at node " + slack.getIndex());
        else
            System.out.println("....no generator limit violated");
        return limViolation;
    }
    
    public boolean powerLoadShedAlgo(FlowNetwork flowNetwork){
        boolean converged = false;
        int loadIter = 0;
        int maxLoadShedIterations = 15; // according to paper
        double loadReductionFactor = 0.05; // 5%, according to paper
        while (!converged && loadIter < maxLoadShedIterations){
            System.out.println("....Doing load shedding at iteration " + loadIter);
            for (Node node : flowNetwork.getNodes()){
                node.replacePropertyElement(PowerNodeState.POWER_DEMAND_REAL, (Double)node.getProperty(PowerNodeState.POWER_DEMAND_REAL)*(1.0-loadReductionFactor));
                node.replacePropertyElement(PowerNodeState.POWER_DEMAND_REACTIVE, (Double)node.getProperty(PowerNodeState.POWER_DEMAND_REACTIVE)*(1.0-loadReductionFactor));
            }
            converged = runFlowAnalysis(flowNetwork);
            loadIter++;
        }
        return converged;
    }
    
    public boolean powerGenLimitAlgo(FlowNetwork flowNetwork){
        boolean converged = false;
        
        // necessary, because if island has slack, we don't consider it in the gen balancing, but it doesn't have to be a blackout (can still do load shedding) -> see check nrGen == 0 below
        boolean hasSlack = false;

        // extract generators in island and treat slack bus if existent
        ArrayList<Node> generators = new ArrayList<>();
        for (Node node : flowNetwork.getNodes()){
            if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.GENERATOR))
                generators.add(node);
            // Set Slack to limits and make normal generator. It will not be considered for generation balancing later.
            if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.SLACK_BUS)){ // Set Slack to limits and make normal generator
                converged = runFlowAnalysis(flowNetwork);
                hasSlack = true;
                node.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.GENERATOR);
                if ((Double)node.getProperty(PowerNodeState.POWER_GENERATION_REAL) > (Double)node.getProperty(PowerNodeState.POWER_MAX_REAL)){
                    node.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, node.getProperty(PowerNodeState.POWER_MAX_REAL));
                    converged = false;
                }
                    // Could also do these adjustments by means of events                            
                    //events.add(new Event(getSimulationTime(),EventType.FLOW,NetworkComponent.NODE,node.getIndex(),PowerNodeState.POWER_GENERATION_REAL,node.getProperty(PowerNodeState.POWER_MAX_REAL)));
                if ((Double)node.getProperty(PowerNodeState.POWER_GENERATION_REAL) < (Double)node.getProperty(PowerNodeState.POWER_MIN_REAL)){
                    node.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, node.getProperty(PowerNodeState.POWER_MIN_REAL));
                    converged = false;
                }
//                if ((Double)node.getProperty(PowerNodeState.POWER_GENERATION_REACTIVE) > (Double)node.getProperty(PowerNodeState.POWER_MAX_REACTIVE))
//                    node.replacePropertyElement(PowerNodeState.POWER_GENERATION_REACTIVE, node.getProperty(PowerNodeState.POWER_MAX_REACTIVE));
//                if ((Double)node.getProperty(PowerNodeState.POWER_GENERATION_REACTIVE) < (Double)node.getProperty(PowerNodeState.POWER_MIN_REACTIVE))
//                    node.replacePropertyElement(PowerNodeState.POWER_GENERATION_REACTIVE, node.getProperty(PowerNodeState.POWER_MIN_REACTIVE));
            }
        }

        // set needed variables
        int genIterator = 0;
        int nrGen = generators.size();

        // blackout if no generator in island
        if (nrGen == 0 && !hasSlack)
            return false; 

        // Sort Generators according to their real power generation in descending order
        Collections.sort(generators, new Comparator<Node>(){
            public int compare(Node node1, Node node2) {
            return Double.compare((Double)node1.getProperty(PowerNodeState.POWER_MAX_REAL), (Double)node2.getProperty(PowerNodeState.POWER_MAX_REAL));
            }
        }.reversed());

        // Gen balancing
        while (!converged && genIterator < nrGen){
            System.out.println("..Doing gen balancing");
            Node currentGen = generators.get(genIterator);
            
            currentGen.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.SLACK_BUS);
            converged = runFlowAnalysis(flowNetwork);
            currentGen.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.GENERATOR);
            
            if ((Double)currentGen.getProperty(PowerNodeState.POWER_GENERATION_REAL) > (Double)currentGen.getProperty(PowerNodeState.POWER_MAX_REAL)){
                currentGen.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, currentGen.getProperty(PowerNodeState.POWER_MAX_REAL));
                converged = false; // limits violated
            }
            if ((Double)currentGen.getProperty(PowerNodeState.POWER_GENERATION_REAL) < (Double)currentGen.getProperty(PowerNodeState.POWER_MIN_REAL)){
                currentGen.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, currentGen.getProperty(PowerNodeState.POWER_MIN_REAL));
                converged = false; // limits violated
            }

            // Jose didn't check the reactive limits
//            if ((Double)currentGen.getProperty(PowerNodeState.POWER_GENERATION_REACTIVE) > (Double)currentGen.getProperty(PowerNodeState.POWER_MAX_REACTIVE)){
//                currentGen.replacePropertyElement(PowerNodeState.POWER_GENERATION_REACTIVE, currentGen.getProperty(PowerNodeState.POWER_MAX_REACTIVE));
//                converged = false; // limits violated
//            }
//            if ((Double)currentGen.getProperty(PowerNodeState.POWER_GENERATION_REACTIVE) < (Double)currentGen.getProperty(PowerNodeState.POWER_MIN_REACTIVE)){
//                currentGen.replacePropertyElement(PowerNodeState.POWER_GENERATION_REACTIVE, currentGen.getProperty(PowerNodeState.POWER_MIN_REACTIVE));
//                converged = false; // limits violated
//            }
            genIterator++;
        }
        
        // Load shedding
        if (!converged){
            generators.get(0).replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.SLACK_BUS);
            converged = powerLoadShedAlgo(flowNetwork);
        }
        
        return converged;
    }
    
    @Override
    public void mitigateOverload(FlowNetwork flowNetwork){
        
    }
    
    @Override
    public boolean linkOverloadAlgo(FlowNetwork flowNetwork){
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
    
    public boolean nodeOverloadAlgo(FlowNetwork flowNetwork){
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
    
    
    @Override
    public boolean runFlowAnalysis(FlowNetwork flowNetwork){
        FlowBackendInterface flowBackend;
        long analysisStartTime;
        switch(domain){
            case POWER:
                switch(backend){
                    case MATPOWER:
                        flowBackend=new MATPOWERFlowBackend((PowerFlowType)this.systemParameters.get(SystemParameter.FLOW_TYPE));
                        analysisStartTime = System.currentTimeMillis();
                        flowBackend.flowAnalysis(flowNetwork);
                        flowSimuTime.get(getSimulationTime()).put(iteration,System.currentTimeMillis()-analysisStartTime);
                        return flowBackend.isConverged();
                    case INTERPSS:
                        flowBackend=new InterpssFlowBackend((PowerFlowType)this.systemParameters.get(SystemParameter.FLOW_TYPE));
                        analysisStartTime = System.currentTimeMillis();
                        flowBackend.flowAnalysis(flowNetwork);
                        flowSimuTime.get(getSimulationTime()).put(iteration,System.currentTimeMillis()-analysisStartTime);
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
     * 
     * @return time of Flow Simulation in Milliseconds per time and iteration.
     */
    public HashMap<Integer,HashMap<Integer,Object>> getFlowSimuTime(){
        return this.flowSimuTime;
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
     * @param measurementDumper the measurementDumper to set
     */
    public void setMeasurementDumper(MeasurementFileDumper measurementDumper) {
        this.measurementDumper = measurementDumper;
    }
}
