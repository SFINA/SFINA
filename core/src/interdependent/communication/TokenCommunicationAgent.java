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


import interdependent.Messages.AbstractSfinaMessage;
import interdependent.Messages.TokenMessage;
import static java.lang.Integer.min;
import protopeer.Peer;
import protopeer.util.quantities.Time;

/**
 * Circular execution of iterations: first network 1, then network 2, ..., network n, network 1 etc.
 * @author mcb
 */
public class TokenCommunicationAgent extends AbstractLocalSimulationComunicationAgent{

    private boolean hasToken;
    private int nextNetwork;
    private int startingNetwork;
    private int previousNetwork;
    private boolean afterBootstrap =false;
    
   // private boolean startingNetworkAfterBootstrap =false;
    
    public TokenCommunicationAgent(Time bootstrapTime, Time runTime,int totalNumberNetworks, int startingNetwork){
        super(bootstrapTime, runTime,totalNumberNetworks);
        this.startingNetwork = startingNetwork;    
    }
    
    public TokenCommunicationAgent(Time bootstrapTime, Time runTime,int totalNumberNetworks) {
        this(bootstrapTime,runTime,totalNumberNetworks, 0);     
    }

    @Override
    public void init(Peer peer) {
        super.init(peer); 
        this.hasToken = getSimulationAgent().getNetworkIndex()==startingNetwork;
        this.nextNetwork = ((getSimulationAgent().getNetworkIndex()+1) % totalNumberNetworks);
        this.previousNetwork = getSimulationAgent().getNetworkIndex()-1;
        if(this.previousNetwork ==-1){
            this.previousNetwork = totalNumberNetworks-1;
        }
    }
    
    
    @Override
    protected ProgressType readyToProgress() {
              
        
        if(this.hasToken){
            // if previous network progressed to Next Step, then we also progress
            // to next step
//            if(this.progressedToNextStep.contains(this.previousNetwork)){
//                return ProgressType.DO_NEXT_STEP;
//            }
            
            // if our SimulationAgent is not converged, we do another iteration
            if(!getSimulationAgent().isConverged()){
                return ProgressType.DO_NEXT_ITERATION;
            } else if(externalNetworksConverged()){
                if(this.isFirst()){
                   // afterBootstrap = true;
                    return ProgressType.DO_NEXT_STEP;
                }
                else{
                    sendTokenToNext();
                    return ProgressType.DO_NOTHING;
                }
            } 
            else
                return ProgressType.SKIP_NEXT_ITERATION;
           
        }
        else // No token
            return ProgressType.DO_NOTHING;
    }

    /**
     * Always called when Message Type was not recognized by AbstractCommunication Agent
     * Subclasses which like to introduce additional Communication Messages have
     * to overwrite this Function
     * @param message 
     * @return TRUE if @message was handled by sublcasses, else FALSE
     */
    @Override
    protected boolean handleMessage(AbstractSfinaMessage message) {
        switch(message.getMessageType()){
            case TOKEN_MESSAGE:
                this.hasToken = true;
                return true;
            default:
                return false;
        }
    }
    
    

    @Override
    protected boolean postProcessCommunicationEvent(CommunicationEventType messageType) {
        switch(messageType){
            case AGENT_IS_READY:
                if(this.hasToken) 
                  
                        sendTokenToNext();
                    
                        
                return true;
          
            default:
                break;
        }
        return false;
    }
    
    private void sendTokenToNext(){
//        if(this.afterBootstrap){
//            this.afterBootstrap = false;
//            return;
//        }
        
        if(this.hasToken){
            this.hasToken = false;
            
            TokenMessage message = new TokenMessage(getSimulationAgent().getNetworkIndex());
            if(isFirst() && getSimulationAgent().getIteration() ==0){
                sendToSpecific(message, getSimulationAgent().getNetworkIndex());
            }else{
                sendToSpecific(message, nextNetwork);
            }
            logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                        ": Send Token To Network " + Integer.toString(this.nextNetwork));
            }
    }   
    
    private boolean isFirst(){
        return (this.getSimulationAgent().getNetworkIndex() == startingNetwork);
    }
    
   

   
    
    

 
}