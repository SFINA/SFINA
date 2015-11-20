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
package event;

import core.SFINAAgent;
import input.AttackStrategy;
import input.Backend;
import input.Domain;
import input.SystemParameter;
import java.util.HashMap;
import network.FlowNetwork;
import network.Link;
import network.LinkState;
import network.Node;
import network.NodeState;
import org.apache.log4j.Logger;
import power.PowerFlowType;

/**
 *
 * @author Ben
 */
public class EventExecution {
    private static final Logger logger = Logger.getLogger(EventExecution.class);
    /**
     * Constructor
     */
    public EventExecution(){
        
    }
    public void execute(FlowNetwork flowNetwork, HashMap<SystemParameter,Object> systemParameters, Event event){
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
                                node.setActivated((Boolean)event.getValue()); // This doesn't reevaluate the status of connected links
                                System.out.println("..deactivating node " + node.getIndex());
                                // Proper way, but can we make it simpler?
//                                if((Boolean)event.getValue())
//                                    flowNetwork.activateNode(node.getIndex());
//                                else if(!(Boolean)event.getValue())
//                                    flowNetwork.deactivateNode(node.getIndex());
//                                else
//                                    logger.debug("Node status cannot be recognised");
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
                                // link.setActivated((Boolean)event.getValue()); // see above for nodes
                                System.out.println("..deactivating link " + link.getIndex());
                                if((Boolean)event.getValue())
                                    flowNetwork.activateLink(link.getIndex());
                                else if(!(Boolean)event.getValue())
                                    flowNetwork.deactivateLink(link.getIndex());
                                else
                                    logger.debug("Node status cannot be recognised");
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
            case SYSTEM:
                System.out.println("..changing " + (SystemParameter)event.getParameter());
                switch((SystemParameter)event.getParameter()){
                    case DOMAIN:
                        systemParameters.put(SystemParameter.DOMAIN, (Domain)event.getValue());
                        //SFINAAgent.setDomain((Domain)event.getValue());
                        break;
                    case BACKEND:
                        systemParameters.put(SystemParameter.BACKEND, (Backend)event.getValue());
                        //backend=(Backend)event.getValue();
                        break;
                    case FLOW_TYPE:
                        systemParameters.put(SystemParameter.FLOW_TYPE, (PowerFlowType)event.getValue());
                        break;
                    case TOLERANCE_PARAMETER:
                        systemParameters.put(SystemParameter.TOLERANCE_PARAMETER, (Double)event.getValue());
                        break;
                    case ATTACK_STRATEGY:
                        systemParameters.put(SystemParameter.ATTACK_STRATEGY, (AttackStrategy)event.getValue());
                        break;
                    case CAPACITY_CHANGE:
                        systemParameters.put(SystemParameter.CAPACITY_CHANGE, (Double)event.getValue());
                        for (Link link : flowNetwork.getLinks())
                            link.setCapacity(link.getCapacity()*(1.0-(Double)systemParameters.get(SystemParameter.CAPACITY_CHANGE)));
                        break;
                    default:
                        logger.debug("Simulation parameter cannot be regognized.");
                }
            default:
                logger.debug("Event type cannot be recognised");
        }
    }
}
