/*
 * Copyright (C) 2015 SFINA Team
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
package power.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import network.FlowNetwork;
import network.Link;
import network.Node;
import org.apache.log4j.Logger;
import power.input.PowerNodeType;
import power.input.PowerLinkState;
import power.input.PowerNodeState;

/**
 *
 * @author Ben
 */
public class PowerFlowWriter {

    private FlowNetwork net;
    private String columnSeparator;
    private String missingValue;
    private static final Logger logger = Logger.getLogger(PowerFlowWriter.class);
    
    public PowerFlowWriter(FlowNetwork net, String columnSeparator, String missingValue){
        this.net=net;
        this.columnSeparator=columnSeparator;        
        this.missingValue = missingValue;
    }
    
    public void writeNodeFlowData(String location){
        try{
            File file = new File(location);
            File parent = file.getParentFile();
            if(!parent.exists() && !parent.mkdirs())
                logger.debug("Couldn't create output folder");
            file.createNewFile();
            PrintWriter writer = new PrintWriter(new FileWriter(file,false));
            writer.print("id");
            ArrayList<PowerNodeState> necessaryStates = new ArrayList<PowerNodeState>();
            ArrayList<String> stateStrings = new ArrayList<String>();
            for (PowerNodeState state : PowerNodeState.values()){
                String stateString = lookupPowerNodeState(state);
                if (stateString != null){
                    necessaryStates.add(state);
                    stateStrings.add(stateString);
                }
            }
            
            for (int i=0; i<stateStrings.size(); i++)
                writer.print(columnSeparator + stateStrings.get(i));
            writer.print("\n");
            
            for (Node node : net.getNodes()){
                writer.print(node.getIndex());
                for (int i=0; i<necessaryStates.size(); i++)
                    writer.print(columnSeparator + getActualNodeValue(necessaryStates.get(i),node));
                writer.print("\n");
            }
            writer.close();   
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public void writeLinkFlowData(String location){
        try{
            File file = new File(location);
            File parent = file.getParentFile();
            if(!parent.exists() && !parent.mkdirs())
                logger.debug("Couldn't create output folder");
            file.createNewFile();
            PrintWriter writer = new PrintWriter(new FileWriter(file,false));
            writer.print("id");
            ArrayList<PowerLinkState> necessaryStates = new ArrayList<PowerLinkState>();
            ArrayList<String> stateStrings = new ArrayList<String>();
            for (PowerLinkState state : PowerLinkState.values()){
                String stateString = lookupPowerLinkState(state);
                if (stateString != null){
                    necessaryStates.add(state);
                    stateStrings.add(stateString);
                }
            }
            
            for (int i=0; i<stateStrings.size(); i++)
                writer.print(columnSeparator + stateStrings.get(i));
            writer.print("\n");
            
            for (Link link : net.getLinks()){
                writer.print(link.getIndex());
                for (int i=0; i<necessaryStates.size(); i++)
                    writer.print(columnSeparator + link.getProperty(necessaryStates.get(i)));
                writer.print("\n");
            }
            writer.close();   
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }    

    private String lookupPowerNodeState(PowerNodeState powerNodeState){
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
    
    private String getActualNodeValue(PowerNodeState powerNodeState, Node node){
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
    
    private String lookupPowerLinkState(PowerLinkState powerLinkState){
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
}
