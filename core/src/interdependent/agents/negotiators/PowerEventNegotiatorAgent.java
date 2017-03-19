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
package interdependent.agents.negotiators;


import core.SimulationAgentInterface;
import event.Event;
import event.EventType;
import event.NetworkComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import network.InterdependentLink;
import network.Node;
import org.apache.log4j.Logger;
import power.input.PowerLinkState;
import power.input.PowerNodeState;
import protopeer.BasePeerlet;

/**
 * 
 * @author Ben
 */
public class PowerEventNegotiatorAgent extends BasePeerlet implements EventNegotiatorAgentInterface{
    
    private static final Logger logger = Logger.getLogger(PowerEventNegotiatorAgent.class);
    
    public PowerEventNegotiatorAgent(){
        super();
    }
    
    /**
     * In this example, just take average of the double values.
     * @param events
     * @return 
     */
    @Override
    public Event negotiateEvents(List<Event> events){
        ArrayList<Double> conflictValues = new ArrayList<>();
        for(Event event : events){
            conflictValues.add((Double) event.getValue());
        }
        Double newValue = conflictValues.stream().mapToDouble(a -> a).average().getAsDouble();
        Event event = events.get(0);
        return new Event(event.getTime(), event.getEventType(), event.getNetworkComponent(), event.getComponentID(), event.getParameter(), newValue);
    }

    @Override
    public List<Event> translateEvent(Event event) {
        InterdependentLink link = getSimulationAgent().getFlowNetwork().getInterdependentLink(event.getComponentID());
        Node targetNode = null;
        if(link.isIncoming())
            targetNode = (Node)link.getEndNode();
        else
            targetNode = (Node)link.getStartNode();
        List<Event> events = new ArrayList<>();
        events.add(event);
        if(event.getEventType().equals(EventType.FLOW)){
            PowerNodeState nodeState = null;
            switch((PowerLinkState)event.getParameter()){
                case POWER_FLOW_FROM_REAL:
                    nodeState = PowerNodeState.POWER_DEMAND_REAL;
                    break;
                case POWER_FLOW_TO_REAL:
                    nodeState = PowerNodeState.POWER_GENERATION_REAL;
                    break;
                case POWER_FLOW_FROM_REACTIVE:
                    nodeState = PowerNodeState.POWER_DEMAND_REACTIVE;
                    break;
                case POWER_FLOW_TO_REACTIVE:
                    nodeState = PowerNodeState.POWER_GENERATION_REACTIVE;
                    break;
                default:
                    logger.debug("Couldn't recognize power link state.");
            }
            Double newValue = null;
            if(link.isIncoming())
                newValue = (Double)targetNode.getProperty(nodeState) + (Double)event.getValue();
            else
                newValue = (Double)targetNode.getProperty(nodeState) - (Double)event.getValue();
            events.add(new Event(event.getTime(), EventType.FLOW, NetworkComponent.NODE, targetNode.getIndex(), nodeState, newValue));
        }
        return events;
    }
    
    private SimulationAgentInterface getSimulationAgent(){
        return (SimulationAgentInterface) this.getPeer().getPeerletOfType(SimulationAgentInterface.class);
    }
}
