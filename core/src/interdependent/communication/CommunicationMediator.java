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
 *
 * @author mcb
 */
public interface CommunicationMediator extends CommunicationBetweenMediator {
    
    /*
        Receives Messages from the Communication Mediator
        has to register itself through the registerMessageReceiver method
    */
    public interface MessageReceiver{
    
       /**
        * Injects events to the Receiver
        * @param events
        */
        public void injectEvents(List<Event> events);
              
        /**
         * Notifies the Message Receiver, that it can proceed to the next step
         * This will happen if and only if the mediator got an EventMessage and an FinishedStepMessage 
         * fromr all the connected Networks of this receiver
         */
        public void progressToNextStep();
     
        /**
         * gets an Identifier from the Receiver, which is globally representative for the network of the receiver
         * @return 
         */
        public int getIdentifier();
    }
    /*
    Handling
    */
    public void registerMessageReceiver(MessageReceiver listener);
    public void removeMessageReceiver(MessageReceiver listener);
    /**
     * 
     * @param event
     * @param identifier 
     */
    public void sendEvent(Event event, int identifier);
    
    public void agentIsRead(boolean ready);
    
        
       
}
