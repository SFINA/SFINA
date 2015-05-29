/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package power.input;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import network.Node;
import org.apache.log4j.Logger;
import power.PowerNodeType;

/**
 *
 * @author evangelospournaras
 */
public class PowerMetaInfoLoader {
    
    private final String parameterValueSeparator;
    private static final Logger logger = Logger.getLogger(PowerMetaInfoLoader.class);
    
    public PowerMetaInfoLoader(String parameterValueSeparator){
        this.parameterValueSeparator=parameterValueSeparator;
    }
    
    public void loadNodeMetaInfo(String location, List<Node> nodes){
        ArrayList<PowerNodeState> powerNodeStates=new ArrayList<PowerNodeState>();
        HashMap<String,ArrayList<String>> nodesStates=new HashMap<String,ArrayList<String>>();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            if(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), parameterValueSeparator);
                while(st.hasMoreTokens()){
                    String metaInfo=st.nextToken();
                    PowerNodeState state=this.lookupPowerNodeState(metaInfo);
                    powerNodeStates.add(state);
                }
            }
            while(scr.hasNext()){
                ArrayList<String> values=new ArrayList<String>();
                StringTokenizer st = new StringTokenizer(scr.next(), parameterValueSeparator);
                while (st.hasMoreTokens()) {
                    values.add(st.nextToken());
		}
                nodesStates.put(values.get(0), values);
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        this.injectNodeStates(nodes, powerNodeStates, nodesStates);
        
    }
    
    private void injectNodeStates(List<Node> nodes, ArrayList<PowerNodeState> powerNodeStates, HashMap<String,ArrayList<String>> nodesStates){
        for(Node node:nodes){
            ArrayList<String> rawValues=nodesStates.get(node.getIndex());
            for(int i=0;i<rawValues.size();i++){
                PowerNodeState state=powerNodeStates.get(i);
                node.addProperty(state, this.getActualValue(state, rawValues.get(i)));
            }
        }
    }
    
    private PowerNodeState lookupPowerNodeState(String powerNodeState){
        switch(powerNodeState){
            case "id": 
                return PowerNodeState.ID;
            case "type":
                return PowerNodeState.TYPE;
            case "real_power":
                return PowerNodeState.REAL_POWER_DEMAND;
            case "reactive_power":
                return PowerNodeState.REACTIVE_POWER_DEMAND;
            case "Gs":
                return PowerNodeState.SHUNT_CONDUCT;
            case "Bs":
                return PowerNodeState.SHUNT_SUSCEP;
            case "area":
                return PowerNodeState.AREA;
            case "volt_mag":
                return PowerNodeState.VOLTAGE_MAGNITUDE;
            case "volt_ang":
                return PowerNodeState.VOLTAGE_ANGLE;
            case "baseKV":
                return PowerNodeState.BASE_VOLTAGE;
            case "zone":
                return PowerNodeState.ZONE;
            case "volt_max":
                return PowerNodeState.VOLTAGE_MAX;
            case "volt_min":
                return PowerNodeState.VOLTAGE_MIN;
            case "real_gen":
                return PowerNodeState.REAL_POWER_GENERATION;
            case "reactive_gen":
                return PowerNodeState.REACTIVE_POWER_GENERATION;
            case "Qmax":
                return PowerNodeState.REACTIVE_POWER_MAX;
            case "Qmin":
                return PowerNodeState.REACTIVE_POWER_MIN;
            case "Vg":
                return PowerNodeState.VOLTAGE_SETPOINT;
            case "mBase":
                return PowerNodeState.TOTAL_MVA_BASE;
            case "power_max":
                return PowerNodeState.REAL_POWER_MAX;
            case "power_min":
                return PowerNodeState.REAL_POWER_MIN;
            case "Pc1":
                return PowerNodeState.PC1;
            case "Pc2":
                return PowerNodeState.PC2;
            case "Qc1min":
                return PowerNodeState.QC1_MIN;
            case "Qc1max":
                return PowerNodeState.QC1_MAX;
            case "Qc2min":
                return PowerNodeState.QC2_MIN;
            case "Qc2max":
                return PowerNodeState.QC2_MAX;
            case "ramp_agc":
                return PowerNodeState.RAMP_AGC;
            case "ramp_10":
                return PowerNodeState.RAMP_10;
            case "ramp_30":
                return PowerNodeState.RAMP_30;
            case "ramp_q":
                return PowerNodeState.RAMP_REACTIVE_POWER;
            case "apf":
                return PowerNodeState.AREA_PART_FACTOR; 
            case "model":
                return PowerNodeState.MODEL;
            case "startup":
                return PowerNodeState.STARTUP;
            case "shutdown":
                return PowerNodeState.SHUTDOWN;
            case "n_cost":
                return PowerNodeState.N_COST;
            case "cost_coeff_1":
                return PowerNodeState.COST_PARAM_1;
            case "cost_coeff_2":
                return PowerNodeState.COST_PARAM_2;
            case "cost_coeff_3":
                return PowerNodeState.COST_PARAM_3;
            default:
                logger.debug("Power node state is not recognized.");
                return null;
        }
    }
    
    private PowerLinkState lookupPowerLinkState(String powerLinkState){
        return null;
    }
    
    private Object getActualValue(PowerNodeState powerNodeState, String rawValue){
        switch(powerNodeState){
            case ID:
                return rawValue;
            case TYPE:
                switch(rawValue){
                    case "SLACK_BUS":
                        return PowerNodeType.SLACK_BUS;
                    case "BUS":
                        return PowerNodeType.BUS;
                    case "GEN":
                        return PowerNodeType.GENERATOR;
                    default:
                        logger.debug("Node type cannot be recognized.");
                        return null;
                }
            case REAL_POWER_DEMAND:
                return rawValue;
            default:
                //logger.debug("Power node state is not recognized.");
                return rawValue;
        }
    }  
}
