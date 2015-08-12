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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import network.FlowNetwork;
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
    private Map<Integer,List<Event>> eventSchedule;
    
    
    
    private Domain domain;
    private Backend backend;
    
    
    public SimulationAgent(String experimentID, String peersLogDirectory, String inputParametersLocation, String eventsLocation, String parameterValueSeparator, String columnSeparator){
        this.experimentID=experimentID;
        this.peersLogDirectory=peersLogDirectory;
        this.inputParametersLocation=inputParametersLocation;
        this.eventsLocation=eventsLocation;
        this.parameterValueSeparator=parameterValueSeparator;
        this.columnSeparator=columnSeparator;
        
        this.inputParametersLoader=new InputParametersLoader(this.parameterValueSeparator);
        this.flowNetwork=new FlowNetwork();
        this.topologyLoader=new TopologyLoader(flowNetwork, this.columnSeparator);
        this.eventSchedule=new HashMap<Integer,List<Event>>();
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
                List<Event> events=eventLoader.loadEvents(eventsLocation);
                scheduleEvents(events);
                runActiveState();
            }
        });
        loadAgentTimer.schedule(Time.inMilliseconds(2000));
    }
    
    /**
     * The scheduling of the active state.  It is executed periodically. 
     */
    private void runActiveState(){
        Timer loadAgentTimer= getPeer().getClock().createNewTimer();
        loadAgentTimer.addTimerListener(new TimerListener(){
            public void timerExpired(Timer timer){
                //runActiveState();
        }
        });
        loadAgentTimer.schedule(Time.inMilliseconds(1000));
    }
    
    private void scheduleEvents(List<Event> events){
        for(Event event:events){
            int eventTime=event.getTime();
            if(this.eventSchedule.containsKey(eventTime)){
                this.eventSchedule.get(eventTime).add(event);
            }
            else{
                List<Event> timeEvents=new ArrayList<Event>();
                timeEvents.add(event);
                this.eventSchedule.put(eventTime, timeEvents);
            }
        }
    }
    
    private void buildTopology(){
        //adds the links in the nodes! 
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
    
    
