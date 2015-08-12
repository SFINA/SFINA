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

import network.FlowNetwork;
import network.Node;
import network.NodeState;

/**
 *
 * @author evangelospournaras
 */
public class EventExecutor {
    
    private FlowNetwork flowNetwork;
    
    public EventExecutor(FlowNetwork flowNetwork){
        
    }
    
    public void executeEvent(FlowNetwork flowNetwork, Event event){
        switch(event.getNetworkFeature()){
            case TOPOLOGY:
                switch(event.getNetworkComponent()){
                    case NODE:
                        Node node=flowNetwork.getNode(event.getComponentID());
                        switch((NodeState)event.getParameter()){
                            case ID:
                                node.setIndex((String)event.getValue());
                                break;
                            case STATUS:
                                node.setActivated((Boolean)event.getValue());
                                //do sth
                                break;
                            default:
                                //do sth
                        }
                        // DO STH
                        break;
                    case LINK:
                        // DO STH
                        break;
                    default:
                        //do sth
                }
                //DO STH
                break;
            case FLOW:
                //DO STH
                break;
            default:
                //do sth
        }
    }
    
}
