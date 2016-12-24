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
package interdependent.communication;

import event.Event;
import java.util.List;

/**
 * Handles ambiguities of events between interdependent networks.
 * In particular important for dependencies between networks of different domains (power, gas, communication, ...).
 * @author Ben
 */
public interface EventNegotiatorAgentInterface {
    
    /**
     * Negotiate event conflicts. The case if two events scheduled for the same time, 
     * want to change the same parameter of the same object with different values.
     * @param events in conflict
     * @return the event which replaces the list of input events
     */
    Event negotiateEvents(List<Event> events);
    
    /**
     * Translate change on interdependent link to change(s) in node in local network.
     * @param event on an interdependent link
     * @return list of events triggered by input event
     */
    List<Event> translateEvent(Event event);
    
}
