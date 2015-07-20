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
package input;

import event.Event;
import event.EventState;
import event.NetworkComponent;
import event.NetworkFeature;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import network.LinkState;
import network.NodeState;
import org.apache.log4j.Logger;
import power.PowerNodeType;
import power.input.PowerLinkState;
import power.input.PowerNodeState;
import static power.input.PowerNodeState.AREA;
import static power.input.PowerNodeState.AREA_PART_FACTOR;
import static power.input.PowerNodeState.BASE_VOLTAGE;
import static power.input.PowerNodeState.COST_PARAM_1;
import static power.input.PowerNodeState.COST_PARAM_2;
import static power.input.PowerNodeState.COST_PARAM_3;
import static power.input.PowerNodeState.ID;
import static power.input.PowerNodeState.MODEL;
import static power.input.PowerNodeState.N_COST;
import static power.input.PowerNodeState.PC1;
import static power.input.PowerNodeState.PC2;
import static power.input.PowerNodeState.QC1_MAX;
import static power.input.PowerNodeState.QC1_MIN;
import static power.input.PowerNodeState.QC2_MAX;
import static power.input.PowerNodeState.QC2_MIN;
import static power.input.PowerNodeState.RAMP_10;
import static power.input.PowerNodeState.RAMP_30;
import static power.input.PowerNodeState.RAMP_AGC;
import static power.input.PowerNodeState.RAMP_REACTIVE_POWER;
import static power.input.PowerNodeState.REACTIVE_POWER_DEMAND;
import static power.input.PowerNodeState.REACTIVE_POWER_GENERATION;
import static power.input.PowerNodeState.REACTIVE_POWER_MAX;
import static power.input.PowerNodeState.REACTIVE_POWER_MIN;
import static power.input.PowerNodeState.REAL_POWER_DEMAND;
import static power.input.PowerNodeState.REAL_POWER_GENERATION;
import static power.input.PowerNodeState.REAL_POWER_MAX;
import static power.input.PowerNodeState.REAL_POWER_MIN;
import static power.input.PowerNodeState.SHUNT_CONDUCT;
import static power.input.PowerNodeState.SHUNT_SUSCEPT;
import static power.input.PowerNodeState.SHUTDOWN;
import static power.input.PowerNodeState.STARTUP;
import static power.input.PowerNodeState.TOTAL_MVA_BASE;
import static power.input.PowerNodeState.TYPE;
import static power.input.PowerNodeState.VOLTAGE_ANGLE;
import static power.input.PowerNodeState.VOLTAGE_MAGNITUDE;
import static power.input.PowerNodeState.VOLTAGE_MAX;
import static power.input.PowerNodeState.VOLTAGE_MIN;
import static power.input.PowerNodeState.VOLTAGE_SETPOINT;
import static power.input.PowerNodeState.ZONE;

/**
 *
 * @author evangelospournaras
 */
public class EventLoader {
    
    private Domain domain;
    private String columnSeparator;
    private static final Logger logger = Logger.getLogger(EventLoader.class);
    
    public EventLoader(Domain domain, String columnSeparator){
        this.domain=domain;
        this.columnSeparator=columnSeparator;
    }
    
    public ArrayList<Event> loadEvents(String location){
        ArrayList<EventState> eventStates=new ArrayList<EventState>(); // not used at this moment
        ArrayList<Event> events=new ArrayList<Event>();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            if(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), columnSeparator);
                while(st.hasMoreTokens()){
                    String eventStateName = st.nextToken();
                    EventState eventState = this.lookupEventState(eventStateName);
                    eventStates.add(eventState);
                }
            }
            while(scr.hasNext()){
                ArrayList<String> values = new ArrayList<String>();
                StringTokenizer st = new StringTokenizer(scr.next(), columnSeparator);
                while (st.hasMoreTokens()) {
                    values.add(st.nextToken());
		}
                //Now here we reconstruct all the parameters of an event:
                // I currently do this manually assuming I know the order of the
                //columns. However in the future should happen in an automated
                //way by using the eventStates array list constructed by reading
                //the first line of the event file. 
                // 1. Time
                int time=Integer.parseInt(values.get(0));
                // 2. Network feature
                NetworkFeature networkFeature=null;
                switch(values.get(1)){
                    case "topology":
                        networkFeature=NetworkFeature.TOPOLOGY;
                        break;
                    case "flow":
                        networkFeature=NetworkFeature.FLOW;
                        break;
                    default:
                        logger.debug("Network feature cannot be recognized.");
                }
                // 3. Network component
                NetworkComponent networkComponent=null;
                switch(values.get(2)){
                    case "node":
                        networkComponent=NetworkComponent.Node;
                        break;
                    case "link":
                        networkComponent=NetworkComponent.Link;
                        break;
                    default:
                        logger.debug("Network component cannot be recognized.");
                }
                // 3. Component id
                String id=values.get(3);
                // 4. Parameter
                Enum parameter=null;
                switch(networkFeature){
                    case TOPOLOGY:
                        switch(networkComponent){
                            case Node:
                                parameter=this.lookupNodeState(values.get(4));
                                break;
                            case Link:
                                parameter=this.lookupLinkState(values.get(4));
                                break;
                            default:
                                logger.debug("Network component cannot be recognized.");
                        }
                        break;
                    case FLOW:
                        switch(networkComponent){
                            case Node:
                                switch(domain){
                                    case POWER:
                                        parameter=this.lookupPowerNodeState(values.get(4));
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
                                break;
                            case Link:
                                switch(domain){
                                    case POWER:
                                        parameter=this.lookupPowerLinkState(values.get(4));
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
                                break;
                            default:
                                logger.debug("Network component cannot be recognized.");
                        }
                        break;
                    default:
                        logger.debug("Network feature cannot be recognized.");
                }
                // 5. Value
                Object value=null;
                switch(networkFeature){
                    case TOPOLOGY:
                        switch(networkComponent){
                            case Node:
                                value=this.getActualNodeValue((NodeState)parameter,values.get(5));
                                break;
                            case Link:
                                value=this.getActualLinkValue((LinkState)parameter,values.get(5));
                                break;
                            default:
                                logger.debug("Network component cannot be recognized.");
                        }
                        break;
                    case FLOW:
                        switch(networkComponent){
                            case Node:
                                switch(domain){
                                    case POWER:
                                        value=this.getActualPowerNodeValue((PowerNodeState)parameter,values.get(5));
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
                                break;
                            case Link:
                                switch(domain){
                                    case POWER:
                                        value=this.getActualPowerLinkValue((PowerLinkState)parameter,values.get(5));
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
                                break;
                            default:
                                logger.debug("Network component cannot be recognized.");
                        }
                        break;
                    default:
                        logger.debug("Network feature cannot be recognized.");
                }
                Event event=new Event(time,networkFeature,networkComponent,id,parameter,value);
                events.add(event);
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return events;
    }
    
    private EventState lookupEventState(String eventState){
        switch(eventState){
            case "time": 
                return EventState.ID;
            case "feature":
                return EventState.NETWORK_FEATURE;
            case "component":
                return EventState.NETWORK_COMPONENT;
            case "id":
                return EventState.ID;
            case "parameter":
                return EventState.PARAMETER;
            case "value":
                return EventState.VALUE;
            default:
                logger.debug("Event state is not recognized.");
                return null;
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
        switch(powerLinkState){
            case "id":
                return PowerLinkState.ID;
            case "current":
                return PowerLinkState.CURRENT;
            case "real_power":
                return PowerLinkState.REAL_POWER_FLOW;
            case "reactive_power":
                return PowerLinkState.REACTIVE_POWER_FLOW;
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
    
    private NodeState lookupNodeState(String nodeState){
        switch(nodeState){
            case "id": 
                return NodeState.ID;
            case "status":
                return NodeState.STATUS;
            default:
                logger.debug("Node state is not recognized.");
                return null;
        }
    }
    
    private LinkState lookupLinkState(String linkState){
        switch(linkState){
            case "id":
                return LinkState.ID;
            case "from_node_id":;
                return LinkState.FROM_NODE;
            case "to_node_id":
                return LinkState.TO_NODE;
            case "status":
                return LinkState.STATUS;
            default:
                logger.debug("Link state is not recognized.");
                return null;
        }
    }
    
    private Object getActualNodeValue(NodeState nodeState, String rawValue){
        switch(nodeState){
            case ID:
                return rawValue;
            case STATUS:
                switch(rawValue){
                    case "1":
                        return true;
                    case "0":
                        return false;
                    default:
                        logger.debug("Something is wrong with status of the NODES.");
                }
            default:
                logger.debug("Node state is not recognized.");
                return null;
        }    
    } 
    
    private Object getActualLinkValue(LinkState linkState, String rawValue){
        switch(linkState){
            case ID:
                return rawValue;
            case FROM_NODE:
                return rawValue;
            case TO_NODE:
                return rawValue;
            case STATUS:
                switch(rawValue){
                    case "1":
                        return true;
                    case "0":
                        return false;
                    default:
                        logger.debug("Something is wrong with status of the links.");
                }
            default:
                logger.debug("Link state is not recognized.");
                return null;
        }    
    }
    
    private Object getActualPowerNodeValue(PowerNodeState powerNodeState, String rawValue){
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
                return Double.parseDouble(rawValue);
            case REACTIVE_POWER_DEMAND:
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
            case REAL_POWER_GENERATION:
                return Double.parseDouble(rawValue);
            case REACTIVE_POWER_GENERATION:
                return Double.parseDouble(rawValue);
            case REACTIVE_POWER_MAX:
                return Double.parseDouble(rawValue);
            case REACTIVE_POWER_MIN:
                return Double.parseDouble(rawValue);
            case VOLTAGE_SETPOINT:
                return Double.parseDouble(rawValue);
            case TOTAL_MVA_BASE:
                return Double.parseDouble(rawValue);
            case REAL_POWER_MAX:
                return Double.parseDouble(rawValue);
            case REAL_POWER_MIN:
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
    private Object getActualPowerLinkValue(PowerLinkState powerLinkState, String rawValue){
        switch(powerLinkState){
            case CURRENT:
                return Double.parseDouble(rawValue);
            case REAL_POWER_FLOW:
                return Double.parseDouble(rawValue);
            case REACTIVE_POWER_FLOW:
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