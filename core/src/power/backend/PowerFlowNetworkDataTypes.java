/*
 * Copyright (C) 2016 SFINA Team
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
package power.backend;

import event.FlowNetworkDataTypesInterface;
import java.util.ArrayList;
import java.util.Collection;
import network.Link;
import network.Node;
import org.apache.log4j.Logger;
import power.input.PowerLinkState;
import power.input.PowerNodeState;
import power.input.PowerNodeType;

/**
 *
 * @author evangelospournaras
 */
public class PowerFlowNetworkDataTypes implements FlowNetworkDataTypesInterface{
    
    private static final Logger logger = Logger.getLogger(PowerFlowNetworkDataTypes.class);
    
    public PowerFlowNetworkDataTypes(){
        
    }
    
    @Override
    public Enum[] getNodeStates(){
        return PowerNodeState.values();
    }
    
    @Override
    public Enum[] getLinkStates(){
        return PowerLinkState.values();
    }
    
    @Override
    public PowerNodeState parseNodeStateTypeFromString(String powerNodeState){
        switch(powerNodeState){
            case "id": 
                return PowerNodeState.ID;
            case "type":
                return PowerNodeState.TYPE;
            case "real_power":
                return PowerNodeState.POWER_DEMAND_REAL;
            case "reactive_power":
                return PowerNodeState.POWER_DEMAND_REACTIVE;
            case "Gs":
                return PowerNodeState.SHUNT_CONDUCT;
            case "Bs":
                return PowerNodeState.SHUNT_SUSCEPT;
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
                return PowerNodeState.POWER_GENERATION_REAL;
            case "reactive_gen":
                return PowerNodeState.POWER_GENERATION_REACTIVE;
            case "Qmax":
                return PowerNodeState.POWER_MAX_REACTIVE;
            case "Qmin":
                return PowerNodeState.POWER_MIN_REACTIVE;
            case "Vg":
                return PowerNodeState.VOLTAGE_SETPOINT;
            case "mBase":
                return PowerNodeState.MVA_BASE_TOTAL;
            case "power_max":
                return PowerNodeState.POWER_MAX_REAL;
            case "power_min":
                return PowerNodeState.POWER_MIN_REAL;
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
    
    @Override
    public PowerLinkState parseLinkStateTypeFromString(String powerLinkState){
        switch(powerLinkState){
            case "id":
                return PowerLinkState.ID;
            case "current":
                return PowerLinkState.CURRENT;
            case "real_power_from":
                return PowerLinkState.POWER_FLOW_FROM_REAL;
            case "reactive_power_from":
                return PowerLinkState.POWER_FLOW_FROM_REACTIVE;
            case "real_power_to":
                return PowerLinkState.POWER_FLOW_TO_REAL;
            case "reactive_power_to":
                return PowerLinkState.POWER_FLOW_TO_REACTIVE;
            case "resistance":
                return PowerLinkState.RESISTANCE;
            case "reactance":
                return PowerLinkState.REACTANCE;
            case "susceptance":
                return PowerLinkState.SUSCEPTANCE;
            case "rateA":
                return PowerLinkState.RATE_A;
            case "rateB":
                return PowerLinkState.RATE_B;
            case "rateC":
                return PowerLinkState.RATE_C;
            case "ratio":
                return PowerLinkState.TAP_RATIO;
            case "angle":
                return PowerLinkState.ANGLE_SHIFT;
            case "angmin":
                return PowerLinkState.ANGLE_DIFFERENCE_MIN;
            case "angmax":
                return PowerLinkState.ANGLE_DIFFERENCE_MAX;
            default:
                logger.debug("Power link state is not recognized.");
                return null;
        }
    }
    
    @Override
    public Object parseNodeValuefromString(Enum nodeState, String rawValue){
        PowerNodeState powerNodeState=(PowerNodeState)nodeState;
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
            case POWER_DEMAND_REAL:
                return Double.parseDouble(rawValue);
            case POWER_DEMAND_REACTIVE:
                return Double.parseDouble(rawValue);
            case SHUNT_CONDUCT:
                return Double.parseDouble(rawValue);
            case SHUNT_SUSCEPT:
                return Double.parseDouble(rawValue);
            case AREA:
                return Double.parseDouble(rawValue);
            case VOLTAGE_MAGNITUDE:
                return Double.parseDouble(rawValue);
            case VOLTAGE_ANGLE:
                return Double.parseDouble(rawValue);
            case BASE_VOLTAGE:
                return Double.parseDouble(rawValue);
            case ZONE:
                return Double.parseDouble(rawValue);
            case VOLTAGE_MAX:
                return Double.parseDouble(rawValue);
            case VOLTAGE_MIN:
                return Double.parseDouble(rawValue);
            // From here on generator specific data
            case POWER_GENERATION_REAL:
                return Double.parseDouble(rawValue);
            case POWER_GENERATION_REACTIVE:
                return Double.parseDouble(rawValue);
            case POWER_MAX_REACTIVE:
                return Double.parseDouble(rawValue);
            case POWER_MIN_REACTIVE:
                return Double.parseDouble(rawValue);
            case VOLTAGE_SETPOINT:
                return Double.parseDouble(rawValue);
            case MVA_BASE_TOTAL:
                return Double.parseDouble(rawValue);
            case POWER_MAX_REAL:
                return Double.parseDouble(rawValue);
            case POWER_MIN_REAL:
                return Double.parseDouble(rawValue);
            case PC1:
                return Double.parseDouble(rawValue);
            case PC2:
                return Double.parseDouble(rawValue);
            case QC1_MIN:
                return Double.parseDouble(rawValue);
            case QC1_MAX:
                return Double.parseDouble(rawValue);
            case QC2_MIN:
                return Double.parseDouble(rawValue);
            case QC2_MAX:
                return Double.parseDouble(rawValue);
            case RAMP_AGC:
                return Double.parseDouble(rawValue);
            case RAMP_10:
                return Double.parseDouble(rawValue);
            case RAMP_30:
                return Double.parseDouble(rawValue);
            case RAMP_REACTIVE_POWER:
                return Double.parseDouble(rawValue);
            case AREA_PART_FACTOR:
                return Double.parseDouble(rawValue);
            case MODEL:
                return Double.parseDouble(rawValue);
            case STARTUP:
                return Double.parseDouble(rawValue);
            case SHUTDOWN:
                return Double.parseDouble(rawValue);
            case N_COST:
                return Double.parseDouble(rawValue);
            case COST_PARAM_1:
                return Double.parseDouble(rawValue);
            case COST_PARAM_2:
                return Double.parseDouble(rawValue);
            case COST_PARAM_3:
                return Double.parseDouble(rawValue);
            default:
                logger.debug("Power node state is not recognized.");
                return null;
        }    
    }  
    
    @Override
    public Object parseLinkValueFromString(Enum linkState, String rawValue){
        PowerLinkState powerLinkState=(PowerLinkState)linkState;
        switch(powerLinkState){
            case CURRENT:
                return Double.parseDouble(rawValue);
            case POWER_FLOW_FROM_REAL:
                return Double.parseDouble(rawValue);
            case POWER_FLOW_FROM_REACTIVE:
                return Double.parseDouble(rawValue);
            case POWER_FLOW_TO_REAL:
                return Double.parseDouble(rawValue);
            case POWER_FLOW_TO_REACTIVE:
                return Double.parseDouble(rawValue);
            case RESISTANCE:
                return Double.parseDouble(rawValue);
            case REACTANCE:
                return Double.parseDouble(rawValue);
            case SUSCEPTANCE:
                return Double.parseDouble(rawValue);
            case RATE_A:
                return Double.parseDouble(rawValue);
            case RATE_B:
                return Double.parseDouble(rawValue);
            case RATE_C:
                return Double.parseDouble(rawValue);
            case TAP_RATIO:
                return Double.parseDouble(rawValue);
            case ANGLE_SHIFT:
                return Double.parseDouble(rawValue);
            case ANGLE_DIFFERENCE_MIN:
                return Double.parseDouble(rawValue);
            case ANGLE_DIFFERENCE_MAX:
                return Double.parseDouble(rawValue);
            default:
                logger.debug("Power link state is not recognized.");
                return null;
        }
    }
    
    @Override
    public String castNodeStateTypeToString(Enum nodeState){
        PowerNodeState powerNodeState = (PowerNodeState)nodeState;
        switch(powerNodeState){
            case ID: 
                return null;
            case TYPE:
                return "type";
            case POWER_DEMAND_REAL:
                return "real_power";
            case POWER_DEMAND_REACTIVE:
                return "reactive_power";
            case SHUNT_CONDUCT:
                return "Gs";
            case SHUNT_SUSCEPT:
                return "Bs";
            case AREA:
                return "area";
            case VOLTAGE_MAGNITUDE:
                return "volt_mag";
            case VOLTAGE_ANGLE:
                return "volt_ang";
            case BASE_VOLTAGE:
                return "baseKV";
            case ZONE:
                return "zone";
            case VOLTAGE_MAX:
                return "volt_max";
            case VOLTAGE_MIN:
                return "volt_min";
            case POWER_GENERATION_REAL:
                return "real_gen";
            case POWER_GENERATION_REACTIVE:
                return "reactive_gen";
            case POWER_MAX_REACTIVE:
                return "Qmax";
            case POWER_MIN_REACTIVE:
                return "Qmin";
            case VOLTAGE_SETPOINT:
                return "Vg";
            case MVA_BASE_TOTAL:
                return "mBase";
            case POWER_MAX_REAL:
                return "power_max";
            case POWER_MIN_REAL:
                return "power_min";
            case PC1:
                return "Pc1";
            case PC2:
                return "Pc2";
            case QC1_MIN:
                return "Qc1min";
            case QC1_MAX:
                return "Qc1max";
            case QC2_MIN:
                return "Qc2min";
            case QC2_MAX:
                return "Qc2max";
            case RAMP_AGC:
                return "ramp_agc";
            case RAMP_10:
                return "ramp_10";
            case RAMP_30:
                return "ramp_30";
            case RAMP_REACTIVE_POWER:
                return "ramp_q";
            case AREA_PART_FACTOR:
                return "apf"; 
            case MODEL:
                return "model";
            case STARTUP:
                return "startup";
            case SHUTDOWN:
                return "shutdown";
            case N_COST:
                return "n_cost";
            case COST_PARAM_1:
                return "cost_coeff_1";
            case COST_PARAM_2:
                return "cost_coeff_2";
            case COST_PARAM_3:
                return "cost_coeff_3";
            default:
                logger.debug("Power node state is not recognized.");
                return null;
        }
    }
    
    @Override
    public String castLinkStateTypeToString(Enum linkState){
        PowerLinkState powerLinkState = (PowerLinkState)linkState;
        switch(powerLinkState){
            case ID:
                return null;
            case CURRENT:
                return "current";
            case POWER_FLOW_FROM_REAL:
                return "real_power_from";
            case POWER_FLOW_FROM_REACTIVE:
                return "reactive_power_from";
            case POWER_FLOW_TO_REAL:
                return "real_power_to";
            case POWER_FLOW_TO_REACTIVE:
                return "reactive_power_to";
            case RESISTANCE:
                return "resistance";
            case REACTANCE:
                return "reactance";
            case SUSCEPTANCE:
                return "susceptance";
            case RATE_A:
                return "rateA";
            case RATE_B:
                return "rateB";
            case RATE_C:
                return "rateC";
            case TAP_RATIO:
                return "ratio";
            case ANGLE_SHIFT:
                return "angle";
            case ANGLE_DIFFERENCE_MIN:
                return "angmin";
            case ANGLE_DIFFERENCE_MAX:
                return "angmax";
            case LOSS_REAL:
                return null;
            case LOSS_REACTIVE:
                return null;
            default:
                logger.debug("Power link state is not recognized.");
                return null;
        }
    }    
    
    @Override
    public String castNodeStateValueToString(Enum nodeState, Node node, String missingValue){
        PowerNodeState powerNodeState = (PowerNodeState)nodeState;
        switch(powerNodeState){
            case TYPE:
                switch((PowerNodeType)node.getProperty(powerNodeState)){
                    case BUS:
                        return "BUS";
                    case GENERATOR:
                        return "GEN";
                    case SLACK_BUS:
                        return "SLACK_BUS";
                }
                break;    
        }
        if (node.getProperty(powerNodeState) == null)
            return missingValue;
        
        return String.valueOf(node.getProperty(powerNodeState));
    }
    
    @Override
    public String castLinkStateValueToString(Enum linkState, Link link, String missingValue){
        return String.valueOf(link.getProperty(linkState));
    }
}
