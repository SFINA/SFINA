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
    public PowerNodeState getNodeStateType(String powerNodeState){
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
    public PowerLinkState getLinkStateType(String powerLinkState){
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
    public Object getNodeStateValue(Enum nodeState, String rawValue){
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
    public Object getLinkStateValue(Enum linkState, String rawValue){
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
    
}
