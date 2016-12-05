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
package interdependent.Messages;

import event.Event;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author root
 */
public class EventMessage extends AbstractSfinaMessage{
    
    private List<Event> eventList;

    public EventMessage(int networkIdentifier, List<Event> events) {
        super(networkIdentifier);
        this.eventList = events;
    }
    
    public EventMessage(int networkIdentifier, Event event){
        super(networkIdentifier);
        this.eventList = new ArrayList();
        this.eventList.add(event);
       
    }
    
    public List<Event> getEvents() {
        return eventList;
    }

    @Override
    public SfinaMessageType getMessageType() {
        return SfinaMessageType.EVENT_MESSAGE;
    }
    
    
 
    
    
    
}
