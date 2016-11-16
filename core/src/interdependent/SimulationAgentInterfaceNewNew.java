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
package interdependent;

import backend.FlowDomainAgent;
import event.Event;
import java.util.HashMap;
import network.FlowNetwork;
import protopeer.network.Message;

/**
 *
 * @author root
 */
public interface SimulationAgentInterfaceNewNew {
    
    public void runBootstraping();
    
    public void runActiveState();
    
    public HashMap<Enum,Object> getDomainParameters();
    
    public void saveOutputData();
    
    public int getIteration();
    
    public void queueEvent(Event event);
    
    public void runPassiveState(Message message);
    
    public void executeEvent(FlowNetwork flowNetwork, Event event);
    
    public void executeAllEvents();
    
    public void runInitialOperations();
    
    public void runFinalOperations();
        
    public void runFlowAnalysis();
        
    public FlowDomainAgent getFlowDomainAgent();
        
    public void loadExperimentConfigFiles(String sfinaParamLocation, String backendParamLocation, String eventsLocation);                

    
}
