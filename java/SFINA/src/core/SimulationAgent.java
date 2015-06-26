package core;

import dsutil.protopeer.FingerDescriptor;
import input.Backend;
import static input.Backend.INTERPSS;
import static input.Backend.MATPOWER;
import flow_analysis.FlowAnalysisInterface;
import input.Domain;
import static input.Domain.GAS;
import static input.Domain.POWER;
import static input.Domain.TRANSPORTATION;
import static input.Domain.WATER;
import input.InputParameter;
import input.InputParametersLoader;
import input.TopologyLoader;
import java.util.List;
import java.util.Map;
import network.FlowNetwork;
import network.Link;
import network.Node;
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
    
    private String peersLogDirectory;
    
    private String experimentID;
    private String inputParametersLocation;
    private String attackedLinesLocation;
    private String parameterValueSeparator;
    private String columnSeparator;
    private FingerDescriptor myAgentDescriptor;
    private MeasurementFileDumper measurementDumper;
    private InputParametersLoader inputParametersLoader;
    private TopologyLoader topologyLoader;
    
    private Map<InputParameter,Object> inputParameters;
    private List<Link> attackedLinks;
    
    private static final Logger logger = Logger.getLogger(SimulationAgent.class);
    
    private Domain domain;
    private Backend backend;
    
    
    
    final static String network="test.txt";
    
    private FlowNetwork net;
    
    public SimulationAgent(String inputParametersLocation, String attackedLinesLocation, String parameterValueSeparator, String columnSeparator){
        this.inputParametersLocation=inputParametersLocation;
        this.attackedLinesLocation=attackedLinesLocation;
        this.parameterValueSeparator=parameterValueSeparator;
        this.columnSeparator=columnSeparator;
        this.inputParametersLoader=new InputParametersLoader(this.parameterValueSeparator);
        this.topologyLoader=new TopologyLoader(net, this.columnSeparator);
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
                        flowAnalysis.flowAnalysis(net);
                        break;
                    case INTERPSS:
                        flowAnalysis=new InterpssPowerFlowAnalysis((PowerFlowType)this.inputParameters.get(InputParameter.FLOW_TYPE));
                        flowAnalysis.flowAnalysis(net);
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
        return net;
    }
    
    public void setFlowNetwork(FlowNetwork net) {
        this.net = net;
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
    
    
