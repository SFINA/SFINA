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
package interdependent.communication;

import static java.lang.Integer.min;
import protopeer.util.quantities.Time;

/**
 *
 * @author root
 */
public class CommunicationAgentInterdependentSimulation_new extends AbstractComunicationAgentLocalSimulation_new{
    
    private boolean afterBootstrap = false;
  
    /**
     * 
     * @param totalNumberNetworks 
     */
    public CommunicationAgentInterdependentSimulation_new(Time bootstrapTime, Time runTime,int totalNumberNetworks) {
        super(bootstrapTime, runTime, totalNumberNetworks);
     
    }

    /**
     * ************************************************
     *          ABSTRACT METHODS
     * ************************************************
     */
     @Override
    protected ProgressType readyToProgress() {
       
        if(afterBootstrap){
            this.afterBootstrap = false;
            return ProgressType.DO_NEXT_STEP;
        }
        if(this.externalNetworksFinished.size() == (this.totalNumberNetworks - 1)
                && (this.externalNetworksEvents.size() == min(getSimulationAgent().getConnectedNetworkIndices().size(),this.totalNumberNetworks-1)) 
                && this.agentIsReady){
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
    protected boolean postProcessCommunicationEvent(CommunicationEventType eventType) {
       if(eventType.equals(CommunicationEventType.BOOT_FINISHED)){
           this.afterBootstrap = true;
           return true;
        }
       return false;
    }

   
}
