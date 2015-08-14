package core;

import dsutil.protopeer.FingerDescriptor;
import event.Event;
import input.Backend;
import static input.Backend.INTERPSS;
import static input.Backend.MATPOWER;
import flow_analysis.FlowAnalysisInterface;
import input.Domain;
import static input.Domain.GAS;
import static input.Domain.POWER;
import static input.Domain.TRANSPORTATION;
import static input.Domain.WATER;
import input.EventLoader;
import input.InputParameter;
import input.InputParametersLoader;
import input.TopologyLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import network.FlowNetwork;
import network.Link;
import network.LinkState;
import network.Node;
import network.NodeState;
import org.apache.log4j.Logger;
import power.PowerFlowType;
import power.flow_analysis.InterpssPowerFlowAnalysis;
import power.flow_analysis.MATPOWERPowerFlowAnalysis;
import protopeer.BasePeerlet;
import protopeer.Peer;
import protopeer.measurement.MeasurementFileDumper;
import protopeer.measurement.MeasurementLog;
import protopeer.measurement.MeasurementLoggerListener;
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
public class SimulationAgent extends BasePeerlet implements SimulationAgentInterface{
    
    private static final Logger logger = Logger.getLogger(SimulationAgent.class);
    
    private String experimentID;
    private String peersLogDirectory;
    private Time bootstrapTime;
    private Time runTime;
    private String inputParametersLocation;
    private String eventsLocation;
    private String parameterValueSeparator;
    private String columnSeparator;
    
    private InputParametersLoader inputParametersLoader;
    private FlowNetwork flowNetwork;
    private TopologyLoader topologyLoader;
    private EventLoader eventLoader;
    
    private FingerDescriptor myAgentDescriptor;
    private MeasurementFileDumper measurementDumper;
    
    
    private Map<InputParameter,Object> inputParameters;
    
    
    
    private Domain domain;
    private Backend backend;
    
    
    public SimulationAgent(String experimentID, String peersLogDirectory, Time bootstrapTime, Time runTime, String inputParametersLocation, String eventsLocation, String parameterValueSeparator, String columnSeparator){
        this.experimentID=experimentID;
        this.peersLogDirectory=peersLogDirectory;
        this.bootstrapTime=bootstrapTime;
        this.runTime=runTime;
        this.inputParametersLocation=inputParametersLocation;
        this.eventsLocation=eventsLocation;
        this.parameterValueSeparator=parameterValueSeparator;
        this.columnSeparator=columnSeparator;
        
        this.inputParametersLoader=new InputParametersLoader(this.parameterValueSeparator);
        this.flowNetwork=new FlowNetwork();
        this.topologyLoader=new TopologyLoader(flowNetwork, this.columnSeparator);
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
        this.runBootstrap();
        scheduleMeasurements();
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
    private void runBootstrap(){
        Timer loadAgentTimer= getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                inputParameters=inputParametersLoader.loadInputParameters(inputParametersLocation);
                eventLoader=new EventLoader((Domain)inputParameters.get(InputParameter.DOMAIN), columnSeparator);
                ArrayList<Event> events=eventLoader.loadEvents(eventsLocation);
                for(Event event:events){
                    //we may need to adjust time here because the clock is already at time 2!
                    runEventExecution(event.getTime(),event);
                }
                runActiveState();
            }
        });
        loadAgentTimer.schedule(bootstrapTime);
    }
    
    /**
     * The scheduling of the active state.  It is executed periodically. 
     */
    private void runActiveState(){
        Timer loadAgentTimer= getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                runFlowAnalysis();
                runActiveState();
        }
        });
        loadAgentTimer.schedule(runTime);
    }
    
    /**
     * The scheduling of the active state.  It is executed periodically. 
     */
    private void runEventExecution(int time, Event event){
        Timer loadAgentTimer= getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                executeEvent(flowNetwork,event);
        }
        });
        loadAgentTimer.schedule(Time.inMilliseconds(time*1000));
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
    public void runFlowAnalysis(){
        FlowAnalysisInterface flowAnalysis;
        switch(domain){
            case POWER:
                switch(backend){
                    case MATPOWER:
                        flowAnalysis=new MATPOWERPowerFlowAnalysis((PowerFlowType)this.inputParameters.get(InputParameter.FLOW_TYPE));
                        flowAnalysis.flowAnalysis(flowNetwork);
                        break;
                    case INTERPSS:
                        flowAnalysis=new InterpssPowerFlowAnalysis((PowerFlowType)this.inputParameters.get(InputParameter.FLOW_TYPE));
                        flowAnalysis.flowAnalysis(flowNetwork);
                        break;
                    default:
                        logger.debug("Wrong backend detected.");
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
                logger.debug("Wrong backend detected.");
        }
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
    private void scheduleMeasurements(){
        this.measurementDumper=new MeasurementFileDumper(peersLogDirectory+this.experimentID+getPeer().getIdentifier().toString());
        getPeer().getMeasurementLogger().addMeasurementLoggerListener(new MeasurementLoggerListener(){
            public void measurementEpochEnded(MeasurementLog log, int epochNumber){
                
                measurementDumper.measurementEpochEnded(log, epochNumber);
                log.shrink(epochNumber, epochNumber+1);
            }
        });
    }
}
