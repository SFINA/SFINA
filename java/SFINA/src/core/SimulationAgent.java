package core;

import dsutil.protopeer.FingerDescriptor;
import flow_analysis.Backend;
import static flow_analysis.Backend.INTERPSS;
import static flow_analysis.Backend.MATPOWER;
import flow_analysis.FlowAnalysisOutcome;
import input.InputParameter;
import java.util.List;
import java.util.Map;
import network.Link;
import network.Node;
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
    private FingerDescriptor myAgentDescriptor;
    private MeasurementFileDumper measurementDumper;
    
    private Map<InputParameter,Object> inputParameters;
    
    public enum Domain{
        POWER,
        GAS,
        WATER,
        TRANSPORTATION
    }
    private Domain domain;
    private Backend backend;
    
    
    
    final static String network="test.txt";
    final static double tolerance=2.0;
    
    private List<Node> nodes;
    private List<Link> links;
    
    public SimulationAgent(){
    
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
    
    
    
    
    
    @Override
    public FlowAnalysisOutcome runFlowAnalysis(){
        switch(domain){
            case POWER:
              switch(backend){
                  case MATPOWER:
                      
                      break;
                  case INTERPSS:
                      
                      break;
                  default:
                      //something is wrong.
              }
              break;
            case GAS:
                break;
            case WATER:
                break;
            case TRANSPORTATION:
                break;
            default:
                //something is wrong.
        }
        return null;
    }
    
     /**
     * @return the nodes
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * @param nodes the nodes to set
     */
    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    /**
     * @return the links
     */
    public List<Link> getLinks() {
        return links;
    }

    /**
     * @param links the links to set
     */
    public void setLinks(List<Link> links) {
        this.links = links;
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
    
    
