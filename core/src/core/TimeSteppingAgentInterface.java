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
package core;

/**
 *
 * @author McB
 */
public interface TimeSteppingAgentInterface {

    /**
    *   SimulationAgent has to implement this to be able to communicate with the 
    *   TimeSteppingAgentInterface
    */
    public interface CommandReceiver{              
        /**
         * Notifies the Message Receiver, that it can proceed to the next time step.
         * This will happen if and only if the Communication Agent got an EventMessage and an FinishedStepMessage 
         * from all the connected Networks of this receiver and the current time step of all networks has converged.
         */
        public void progressToNextTimeStep();
        
        /**
         * Notifies the Message Receiver, that something changed and that it has 
         * to redo its caluclations.
         * This will happen if and only if the Communication Agent got an EventMessage and an FinishedStepMessage 
         * from all the connected Networks of this receiver.
         */
        public void progressToNextIteration();
        
        /**
         * Notifies the Message Receiver, that nothing has changed, but that it should wait for the other networks, 
         * which are doing another iteration.
         */
        public void skipNextIteration();
        
        /**
         * Allows TimeSteppingAgent to check if the SimulationAgent needs more iterations.
         * @param events
         * @return 
         */
        boolean pendingEventsInQueue();
    }
    
    /**
     * CommandReceiver can notify the TimeSteppingAgent that it finished its Step
     * 
     */
    public void agentFinishedActiveState();
    
 
    /**
     * CommandReceiver can notify the TimeSteppingAgent that it finished its Bootstrap
     */
    public void agentFinishedBootStrap();
   
}
