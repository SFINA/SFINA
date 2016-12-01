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
package interdependent.communication.Archive;

import event.Event;
import java.util.List;
import java.util.Map;
import protopeer.network.NetworkAddress;
import protopeer.util.quantities.Time;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author mcb
 */
public interface CommunicationAgentInterface {
    /*
        Receives Messages from the Communication Agent
    */
    public interface MessageReceiver{
    
       /**
        * Injects events to the Receiver
        * @param events
        */
        public void injectEvents(List<Event> events);
        
                     
        /**
         * gets an Identifier from the Receiver, which is globally representative for the network of the receiver
         * @return 
         */
        public int getNetworkIdentifier();
        
        /**
         * gets a list of Network Identifier to which the Receiver is connected
         * @return 
         */
        public Collection<Integer> getConnectedNetwork();
    }
    /**
     * 
     * @param event
     * @param identifier 
     */
    public void sendEvent(Event event, int identifier);
          
}
