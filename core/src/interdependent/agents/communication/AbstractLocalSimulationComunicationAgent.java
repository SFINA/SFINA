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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package interdependent.agents.communication;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import protopeer.network.IntegerNetworkAddress;
import protopeer.network.NetworkAddress;
import protopeer.util.quantities.Time;

/**
 * Base Class for Interdependent Communication on a single machine. Extend this 
 * class to add a own interdependent communication logic. 
 * @author mcb
 */
public abstract class AbstractLocalSimulationComunicationAgent extends AbstractCommunicationAgent{
    
    public AbstractLocalSimulationComunicationAgent(Time bootstrapTime, Time runTime, int totalNumberNetworks){
        super(bootstrapTime, runTime, totalNumberNetworks);
    }
    
    
    protected Map<Integer, NetworkAddress> getAllExternalNetworkAddresses(){
       HashMap<Integer,NetworkAddress> allAddresses = new HashMap<>();
       
       for(int i=0; i<this.totalNumberNetworks; i++){
           if(getPeer().getIndexNumber() != i)
               allAddresses.put(i, new IntegerNetworkAddress(i));
       }
       return allAddresses;
    }
    
    // DANGEROUS
    protected Map<Integer, NetworkAddress> getConnectedExternalNetworkAddresses(){
        
        Collection<Integer> indices = getSimulationAgent().getConnectedNetworkIndices();
        Map<Integer, NetworkAddress> allAddresses = getAllExternalNetworkAddresses();
        
        Map<Integer, NetworkAddress> connectedAddresses = new HashMap<>();
        
        for(int i : indices)
            connectedAddresses.put(i, allAddresses.get(i));
        
        return connectedAddresses;
    }

    @Override
    protected NetworkAddress getNetworkAddress(int networkIndex) {
        return new IntegerNetworkAddress(networkIndex);
    }
    
    
    
}
