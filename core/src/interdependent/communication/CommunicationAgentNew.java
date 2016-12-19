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

import static java.lang.Integer.min;

/**
 *
 * @author mcb
 */
public class CommunicationAgentNew extends AbstractComunicationAgentLocalSimulation{
    
    private boolean afterBootstrap = false;
  
    /**
     * 
     * @param totalNumberNetworks 
     */
    public CommunicationAgentNew(int totalNumberNetworks) {
        super(totalNumberNetworks);
     
    }

    /**
     * ************************************************
     *          ABSTRACT METHODS
     * ************************************************
     */
     @Override
    protected ProgressType readyToProgress() {
       
//        if(this.externalNetworksFinishedStep.size() == (this.totalNumberNetworks - 1)
//                && (this.externalNetworksSendEvent.size() == min(getSimulationAgent().getConnectedNetworkIndices().size(),this.totalNumberNetworks-1)) 
//                && this.agentIsReady){
//            if(getCommandReceiver().pendingEventsInQueue()){
//                // what happens if events where send after we send our finishedStep message, that we converged!
//                // This has to be handled, when events are Received!
//                // Ben: Don't understand...
//                return ProgressType.DO_NEXT_ITERATION;
//            }else{
//                return ProgressType.DO_NEXT_STEP;
//            }
//        }else{
//            return ProgressType.DO_NOTHING;
//        }
        
        // What we do after bootstraping is actually the same as after any other step, so maybe
        // go back to treating them the same after all. What do you think?
        // Is CommunicationAgent dependent, see below
        if(afterBootstrap){
            this.afterBootstrap = false;
            return ProgressType.DO_NEXT_STEP;
        }
        if(this.externalNetworksFinished.size() == (this.totalNumberNetworks - 1)
                && (this.externalNetworksEvents.size() == min(getSimulationAgent().getConnectedNetworkIndices().size(),this.totalNumberNetworks-1)) 
                && this.agentIsReady){
            if(getCommandReceiver().pendingEventsInQueue()){
                // what happens if events where send after we send our finishedStep message, that we converged!
                // This has to be handled, when events are Received!
                // Ben: Don't understand...
                // Mark: What happens, if we send a Finished Step Message that we converged. But then receive an event and
                // now have pendingEventsInQueue, and hence do another iteration instead of a next step, we should inform
                // all the we have not finished ye, or?
                return ProgressType.DO_NEXT_ITERATION;
            }
            else if(externalNetworksConverged()){
                //Mark: can this lead to a deadlock? As converged is not a "hard criterion", it could be that two 
                // networks did not converge, but also dont run 
                return ProgressType.DO_NEXT_STEP;
            }else
                return ProgressType.DO_NOTHING;
                // here we should increment the iteration number, s.t. they're always in sync between networks
        }else
            return ProgressType.DO_NOTHING;
           
    }
    
    // What we do after bootstraping is actually the same as after any other step, so maybe
    // go back to treating them the same after all. What do you think?
    // Is dependent from Communication Agent: if this one -> progress to Next Step, if Token do nothing etc.
    // Nevertheless: still TBD  
    @Override
    protected boolean handleCommunicationEvent(CommunicationEventType eventType) {
       if(eventType.equals(CommunicationEventType.BOOT_FINISHED)){
           this.afterBootstrap = true;
           return true;
        }
       return false;
    }

   
}
