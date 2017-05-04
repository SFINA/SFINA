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
import event.EventType;
import agents.backend.FlowNetworkDataTypesInterface;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import network.LinkState;
import network.NodeState;
import org.apache.log4j.Logger;

/**
 *
 * @author evangelospournaras
 */
public class EventLoader {

    private static final Logger logger = Logger.getLogger(EventLoader.class);
    
    private String columnSeparator;
    private String missingValue;
    private ArrayList<Event> events;
    private FlowNetworkDataTypesInterface flowNetworkDataTypes;
    
    
    public EventLoader(String columnSeparator, String missingValue, FlowNetworkDataTypesInterface flowNetworkDataTypes){
//        this.domain=domain;
        this.columnSeparator=columnSeparator;
        this.missingValue=missingValue;
        this.events=new ArrayList<Event>();
        this.flowNetworkDataTypes=flowNetworkDataTypes;
    }
    
    public ArrayList<Event> loadEvents(String location){
        ArrayList<EventState> eventStates=new ArrayList<EventState>(); // not used at this moment
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
                logger.debug("Loading event: " + values);
                //Now here we reconstruct all the parameters of an event:
                // I currently do this manually assuming I know the order of the
                //columns. However in the future should happen in an automated
                //way by using the eventStates array list constructed by reading
                //the first line of the event file. 
                // 1. Time
                int time=Integer.parseInt(values.get(0));
                // 2. Network feature
                EventType networkFeature=null;
                switch(values.get(1)){
                    case "topology":
                        networkFeature=EventType.TOPOLOGY;
                        break;
                    case "flow":
                        networkFeature=EventType.FLOW;
                        break;
                    case "system":
                        networkFeature=EventType.SYSTEM;
                        break;
                    default:
                        logger.debug("Network feature cannot be recognized.");
                }
                // 3. Network component
                NetworkComponent networkComponent=null;
                switch(values.get(2)){
                    case "node":
                        networkComponent=NetworkComponent.NODE;
                        break;
                    case "link":
                        networkComponent=NetworkComponent.LINK;
                        break;
                    case "interLink":
                        networkComponent=NetworkComponent.INTERDEPENDENT_LINK;
                        break;
                    case "-": // Might want to change this, so it corresponds to the variable missingValue
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
                            case NODE:
                                parameter=this.lookupNodeState(values.get(4));
                                break;
                            case LINK:
                                parameter=this.lookupLinkState(values.get(4));
                                break;
                            case INTERDEPENDENT_LINK:
                                parameter=this.lookupLinkState(values.get(4));
                                break;
                            default:
                                logger.debug("Network component cannot be recognized.");
                        }
                        break;
                    case FLOW:
                        switch(networkComponent){
                            case NODE:
                                parameter=this.getFlowNetworkDataTypes().parseNodeStateTypeFromString(values.get(4));
                                break;
                            case LINK:
                                parameter=this.getFlowNetworkDataTypes().parseLinkStateTypeFromString(values.get(4));
                                break;
                            case INTERDEPENDENT_LINK:
                                parameter=this.getFlowNetworkDataTypes().parseLinkStateTypeFromString(values.get(4));
                                break;
                            default:
                                logger.debug("Network component cannot be recognized.");
                        }
                        break;
                    case SYSTEM:
                        parameter=this.lookupSystemParameterState(values.get(4));
                        break;
                    default:
                        logger.debug("Network feature cannot be recognized.");
                }
                // 5. Value
                Object value=null;
                switch(networkFeature){
                    case TOPOLOGY:
                        switch(networkComponent){
                            case NODE:
                                value=this.getActualNodeValue((NodeState)parameter,values.get(5));
                                break;
                            case LINK:
                                value=this.getActualLinkValue((LinkState)parameter,values.get(5));
                                break;
                            case INTERDEPENDENT_LINK:
                                value=this.getActualLinkValue((LinkState)parameter,values.get(5));
                                break;
                            default:
                                logger.debug("Network component cannot be recognized.");
                        }
                        break;
                    case FLOW:
                        switch(networkComponent){
                            case NODE:
                                value=this.getFlowNetworkDataTypes().parseNodeValuefromString(parameter,values.get(5));
                                break;
                            case LINK:
                                value=this.getFlowNetworkDataTypes().parseLinkValueFromString(parameter,values.get(5));
                                break;
                            case INTERDEPENDENT_LINK:
                                value=this.getFlowNetworkDataTypes().parseLinkValueFromString(parameter,values.get(5));
                                break;
                            default:
                                logger.debug("Network component cannot be recognized.");
                        }
                        break;
                    case SYSTEM:
                        value=this.getActualSystemValue((SfinaParameter)parameter, values.get(5));
                        break;
                    default:
                        logger.debug("Network feature cannot be recognized.");
                }
                Event event=new Event(time,networkFeature,networkComponent,id,parameter,value);
                getEvents().add(event);
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return getEvents();
    }
    
    private EventState lookupEventState(String eventState){
        switch(eventState){
            case "time": 
                return EventState.ID;
            case "feature":
                return EventState.EVENT_TYPE;
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
    
    private SfinaParameter lookupSystemParameterState(String systemParameterState){
        switch(systemParameterState){
            case "reload":
                return SfinaParameter.RELOAD;
            default:
                logger.debug(("System parameter state is not recognized."));
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
            case "from_net_id":
                return LinkState.FROM_NET;
            case "to_net_id":
                return LinkState.TO_NET;
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
            case FROM_NET:
                return Integer.parseInt(rawValue);
            case TO_NET:
                return Integer.parseInt(rawValue);
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
    
    private Object getActualSystemValue(SfinaParameter systemParameter, String rawValue){
        switch(systemParameter){
            case RELOAD:
                return rawValue;
            default:
                logger.debug("Parameter value is not recognized.");
                return null;
        }
    }

    /**
     * @return the events
     */
    public ArrayList<Event> getEvents() {
        return events;
    }

    /**
     * @return the flowNetworkDataTypes
     */
    public FlowNetworkDataTypesInterface getFlowNetworkDataTypes() {
        if(flowNetworkDataTypes == null)
            logger.debug("Domain backend has to call setFlowNetworkDataTypes method, but probably didn't.");
        return flowNetworkDataTypes;
    }

    /**
     * @param flowNetworkDataTypes the flowNetworkDataTypes to set
     */
    public void setFlowNetworkDataTypes(FlowNetworkDataTypesInterface flowNetworkDataTypes) {
        this.flowNetworkDataTypes = flowNetworkDataTypes;
    }
}
