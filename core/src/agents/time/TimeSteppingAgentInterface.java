/*
 * Copyright (C) 2017 SFINA Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY 2 or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 2 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package agents.time;

/**
 * A TimeStepping Agent needs to implement this interface.
 * @author mcb
 */
public interface TimeSteppingAgentInterface {

   
    
    /**
     * SimulationAgent can notify the TimeSteppingAgent that it finished its Step
     * 
     */
    public void agentFinishedActiveState();
    
 
    /**
     * SimulationAgent can notify the TimeSteppingAgent that it finished its Bootstrap
     */
    public void agentFinishedBootStrap();
    
    /**
     * Returns the current Simulation Time.
     * @return 
     */
    public int getSimulationTime();
    
    
   
}
