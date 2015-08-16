package core;

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
import input.InputParameter;
import input.InputParametersLoader;
import input.TopologyLoader;
import java.util.ArrayList;
import java.util.Map;
import network.FlowNetwork;
import network.Link;
import network.LinkState;
import network.Node;
import network.NodeState;
import org.apache.log4j.Logger;
import power.PowerFlowType;
import power.flow_analysis.InterpssFlowBackend;
import power.flow_analysis.MATPOWERFlowBackend;
import power.input.PowerFlowLoader;
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
public class SimulationAgent extends BasePeerlet implements SimulationAgentInterface{
    
    private static final Logger logger = Logger.getLogger(SimulationAgent.class);
    
    private String experimentID;
    private String peersLogDirectory;
    private Time bootstrapTime;
    private Time runTime;
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
    private TopologyLoader topologyLoader;
    private EventLoader eventLoader;
    private FingerDescriptor myAgentDescriptor;
    private MeasurementFileDumper measurementDumper;
    
    public SimulationAgent(String experimentID, String peersLogDirectory, Time bootstrapTime, Time runTime, String inputParametersLocation, String nodesLocation, String linksLocation, String nodesFlowLocation, String linksFlowLocation, String eventsLocation, String parameterValueSeparator, String columnSeparator, String missingValue){
        this.experimentID=experimentID;
        this.peersLogDirectory=peersLogDirectory;
        this.bootstrapTime=bootstrapTime;
        this.runTime=runTime;
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
        this.runBootstraping(this.bootstrapTime);
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
    @Override
    public void runBootstraping(Time bootstratTime){
        Timer loadAgentTimer= getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                inputParameters=inputParametersLoader.loadInputParameters(inputParametersLocation);
                topologyLoader.loadNodes(nodesLocation);
                topologyLoader.loadLinks(linksLocation);
                Domain domain=(Domain)inputParameters.get(InputParameter.DOMAIN);
                switch(domain){
                    case POWER:
                        PowerFlowLoader flowLoader=new PowerFlowLoader(flowNetwork, parameterValueSeparator, missingValue);
                        flowLoader.loadNodeFlowData(nodesFlowLocation);
                        flowLoader.loadLinkFlowData(linksFlowLocation);
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
                eventLoader=new EventLoader((Domain)inputParameters.get(InputParameter.DOMAIN), columnSeparator);
                ArrayList<Event> events=eventLoader.loadEvents(eventsLocation);
                for(Event event:events){
                    //we may need to adjust time here because the clock is already at time 2!
                    //runEventExecution(event.getTime()-(int)Time.inSeconds(runTime),event);
                }
                runActiveState(runTime);
            }
        });
        loadAgentTimer.schedule(bootstrapTime);
    }
    
    /**
     * The scheduling of the active state.  It is executed periodically. 
     */
    @Override
    public void runActiveState(Time runtime){
        Timer loadAgentTimer= getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                runFlowAnalysis();
                runActiveState(runtime);
        }
        });
        loadAgentTimer.schedule(runTime);
    }
    
    @Override
    public void runPassiveState(Message message){
        
    }
    
    /**
     * The scheduling of the active state.  It is executed periodically. 
     */
    @Override
    public void runEventExecution(int time, Event event){
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
        FlowBackendInterface flowBackend;
        Domain domain=(Domain)this.inputParameters.get(InputParameter.DOMAIN);
        Backend backend=(Backend)this.inputParameters.get(InputParameter.BACKEND);
        switch(domain){
            case POWER:
                switch(backend){
                    case MATPOWER:
                        flowBackend=new MATPOWERFlowBackend((PowerFlowType)this.inputParameters.get(InputParameter.FLOW_TYPE));
                        flowBackend.flowAnalysis(flowNetwork);
                        break;
                    case INTERPSS:
                        flowBackend=new InterpssFlowBackend((PowerFlowType)this.inputParameters.get(InputParameter.FLOW_TYPE));
                        flowBackend.flowAnalysis(flowNetwork);
                        break;
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
        this.measurementDumper=new MeasurementFileDumper(peersLogDirectory+this.experimentID+getPeer().getIdentifier().toString());
        getPeer().getMeasurementLogger().addMeasurementLoggerListener(new MeasurementLoggerListener(){
            public void measurementEpochEnded(MeasurementLog log, int epochNumber){
                
                measurementDumper.measurementEpochEnded(log, epochNumber);
                log.shrink(epochNumber, epochNumber+1);
            }
        });
    }
}
