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

import interdependent.Messages.MessageType;
import static java.lang.Integer.min;
import protopeer.util.quantities.Time;

/**
 * Interdependent Communication Agent for the parallel execution of iterations: 
 * After each iteration Peers/ Networks communicate with each other.
 * @author mcb
 */
public class InterdependentCommunicationAgent extends AbstractLocalSimulationComunicationAgent{
    
    /**
     * 
     * @param totalNumberNetworks 
     */
    public InterdependentCommunicationAgent(Time bootstrapTime, Time runTime,int totalNumberNetworks) {
        super(bootstrapTime, runTime, totalNumberNetworks);
    }

    /**
     * ************************************************
     *          ABSTRACT METHODS
     * ************************************************
     */
    
     @Override
    protected ProgressType readyToProgress() {
       

        if(this.externalNetworksFinished.size() >= (this.totalNumberNetworks - 1)
                && (this.externalNetworksEvents.size() >= min(getSimulationAgent().getConnectedNetworkIndices().size(),
                        this.totalNumberNetworks-1)) ){
            if(!getSimulationAgent().isConverged()){
                return ProgressType.DO_NEXT_ITERATION;
            }
            else if(externalNetworksConverged()){
                return ProgressType.DO_NEXT_STEP;
            }
            else
                return ProgressType.SKIP_NEXT_ITERATION;
        }else
            return ProgressType.DO_NOTHING;
           
    }

    @Override
    protected boolean postProcessCommunicationEvent(MessageType eventType) {
       return false;
    }

}
