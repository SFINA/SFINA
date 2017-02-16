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
package Debug;


import interdependent.Messages.AbstractSfinaMessage;
import interdependent.Messages.FinishedActiveStateMessage;
import interdependent.Messages.ProgressedToNextStepMessage;
import interdependent.Messages.TokenMessage;
import interdependent.communication.AbstractCommunicationAgent;
import interdependent.communication.CommunicationEventType;
import interdependent.communication.ProgressType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import protopeer.Peer;
import protopeer.network.IntegerNetworkAddress;
import protopeer.network.Message;
import protopeer.network.NetworkAddress;
import protopeer.util.quantities.Time;

/**
 *
 * @author mcb
 */
public class TokenCommunicationAgent_Debug extends TimeSteppingAgent_Debug{
   
    protected static final Logger logger = Logger.getLogger(AbstractCommunicationAgent.class);
    
    protected Map<Integer, Boolean> externalNetworksFinished; // keeps track which networks in current iteration finished active State
    protected Set<Integer> progressedToNextStep = new HashSet<>(); // keeps track which networks progressed to the next time step
    
    
    protected int totalNumberNetworks;
    protected boolean agentIsReady;
    private boolean afterBootFinished =false; 
  
    private int stepSkipIterationCounter =0; 
    
    // Token Message Fields
     private boolean hasToken;
    private int nextNetwork; // network to which token has to be send
    private int startingNetwork; // network which is allowed to make time steps
    private int previousNetwork; // network from which tokens should arrive
    private boolean startingNetworkAfterBootstrap =false;
   
    /**
     * 
     * @param bootstrapTime
     * @param runTime
     * @param totalNumberNetworks 
     */
    public TokenCommunicationAgent_Debug(Time bootstrapTime, Time runTime,int totalNumberNetworks) {
        super(bootstrapTime, runTime);
        this.totalNumberNetworks = totalNumberNetworks;
        this.externalNetworksFinished = new HashMap<>();
        this.agentIsReady = false; 
    }
    
    
    /**
     * ************************************************
     *        TIME STEPPING FUNCTIONS
     * ************************************************
     */
    
    @Override
    public void agentFinishedBootStrap() {
         logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Agent Finished Bootstrap ");
       
         //usually in bootstrap handled - moved for simplification of problem
        if(isFirst()){
            this.startingNetworkAfterBootstrap = true;
        }
        this.afterBootFinished = true;
        doNextStep();
       
    }

    @Override
    public void agentFinishedActiveState() {
        
    if(stepSkipIterationCounter!=1){
        logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                ": stepSkipIterationCounter Error!!!! stepsSkipIterationCount = " +Integer.toString(stepSkipIterationCounter));
    }

    stepSkipIterationCounter--;

     logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                ": Agent Finished Active State ");

    logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                ": Time is: "+ Integer.toString(getSimulationTime()));
            
            
        this.agentIsReady = true;
      
        FinishedActiveStateMessage message = new FinishedActiveStateMessage(
                getSimulationAgent().getNetworkIndex(), getSimulationTime(), getSimulationAgent().getIteration(), getSimulationAgent().isConverged());
        sendToAll(message);
        
        this.postProcessAbstractCommunication(CommunicationEventType.AGENT_IS_READY);
    }
 
    
     /**
     * ***************************************
     *         COMMUNICATION WITH OTHER AGENTS
     * ***************************************
     */
    @Override
    public void handleIncomingMessage(Message message) {
        
        //check if its a SFINA Message
        if (message instanceof AbstractSfinaMessage) {
            AbstractSfinaMessage sfinaMessage = (AbstractSfinaMessage) message;
           
            logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Handle Incomming " + sfinaMessage.getMessageType().toString()+" Message  from " + 
                    Integer.toString(sfinaMessage.getNetworkIdentifier()));
            logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": default");
            logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Time is: "+ Integer.toString(getSimulationTime()));
  
            switch (sfinaMessage.getMessageType()) {
                case FINISHED_ACTIVE_STATE:
                    FinishedActiveStateMessage finishedMessage = ((FinishedActiveStateMessage)sfinaMessage);
                    this.externalNetworksFinished.put(finishedMessage.getNetworkIdentifier(), finishedMessage.isConverged());
                    break;
                case PROGRESSED_TO_NEXT_STEP:
                    this.progressedToNextStep.add(sfinaMessage.getNetworkIdentifier());
                    break;                   
                default:
                    if(!handleMessage(sfinaMessage))
                        logger.debug("Message Type not recognized");
            }
            postProcessAbstractCommunication(sfinaMessage.getMessageType());
        }
    }
    
    
     /**
     * ***************************************
     *         COMMUNICATION POSTPROCESS
     * ***************************************
     */
    /**
     * Each time Communication happens/ something chagnes this function should be called 
     * Handles necessary further steps
     * @param communicationEventType 
     */
    private void postProcessAbstractCommunication(CommunicationEventType communicationEventType) {

        postProcessCommunicationEvent(communicationEventType);
        
         logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Before Ready To Progress");
         
        // Determine what to do 
        switch(readyToProgress()){
            case DO_NEXT_ITERATION:
                 logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": do Next Iteration");
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Time is: "+ Integer.toString(getSimulationTime()));
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Current Iteration: " + Integer.toString(getSimulationAgent().getIteration()));
               
                  // do next Iteration
                  doNextIteration();
                break;
            case DO_NEXT_STEP:
                 logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": do Next Step");
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Time is: "+ Integer.toString(getSimulationTime()));
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Current Iteration: " + Integer.toString(getSimulationAgent().getIteration()));
                
                 // Do next Step 
                 doNextStep();
                break;
            case SKIP_NEXT_ITERATION:
                 logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": skip Iteration");
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Time is: "+ Integer.toString(getSimulationTime()));
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Current Iteration: " + Integer.toString(getSimulationAgent().getIteration()));
                  
                // skip Iteration  
                skipNextIteration();
                break;
            case DO_NOTHING:
                 logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": do nothing");
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Time is: "+ Integer.toString(getSimulationTime()));
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Current Iteration: " + Integer.toString(getSimulationAgent().getIteration()));
                break;
            default:
                 logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": default");
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Time is: "+ Integer.toString(getSimulationTime()));
                  logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                    ": Current Iteration: " + Integer.toString(getSimulationAgent().getIteration()));
             
                doNextStep(); 
        }     
    }

    protected void doNextStep(){
        if(stepSkipIterationCounter ==0){
            stepSkipIterationCounter++;
            clearCommunicationAgent();     
            //check if afterBootfsinished (only true if bootstrap is handled by 
            // AbstractCommunicationAgent  - case in default modus). If true: In this case,
            // do nextStep was called by the bootstrap and we do not send notifications
            // to the other Agents etc. (as the first iteration runs in parallel) 
            if(afterBootFinished){
                this.afterBootFinished = false;
            }else{
                 this.progressedToNextStep.clear();
                 sendToAll(new ProgressedToNextStepMessage(getSimulationAgent().getNetworkIndex()));     
            }
             progressCommandReceiverToNextTimeStep();
        }
    }
  
    protected void doNextIteration(){
        if(stepSkipIterationCounter ==0){
            stepSkipIterationCounter++;
        clearCommunicationAgent();
        progressCommandReceiverToNextIteration();
        }
    }
    
    protected void skipNextIteration(){
        if(stepSkipIterationCounter ==0){
             stepSkipIterationCounter++;
            clearCommunicationAgent();
            progressCommandReceiverToSkipNextIteration();
        }
    }
    private void clearCommunicationAgent(){
        this.externalNetworksFinished.clear();
        this.agentIsReady = false;
    }
    
    protected boolean externalNetworksConverged(){
    
        for(Boolean converged : this.externalNetworksFinished.values()){
            if(!converged)
                return false;
        }
        return true;
    }      
  /**
     * ***************************************
     *         MESSAGE SENDING
     * ***************************************
     */

    protected void sendToAll(AbstractSfinaMessage message){
        for(NetworkAddress address: getAllExternalNetworkAddresses().values())
            getPeer().sendMessage(address, message);
    }
    
    protected void sendToConnected(AbstractSfinaMessage message){
        for(NetworkAddress address: getConnectedExternalNetworkAddresses().values())
            getPeer().sendMessage(address, message);
    }
    
    protected void sendToSpecific(AbstractSfinaMessage message, int networkIndex){
        getPeer().sendMessage(getNetworkAddress(networkIndex), message);
    }
        
    /**
     * ************************************************
     *          TOKEN MESSAGE FUNCTIONS
     * ************************************************
     */
    
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
    /**
     * Always called when Message Type was not recognized by AbstractCommunication Agent
     * Subclasses which like to introduce additional Communication Messages have
     * to overwrite this Function
     * @param message 
     * @return TRUE if @message was handled by sublcasses, else FALSE
     */
    protected boolean handleMessage(AbstractSfinaMessage message) {
        switch(message.getMessageType()){
            case TOKEN_MESSAGE:
                this.hasToken = true;
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Always called by the AbstractCommunicationAgent at the very end of a 
     * communication Cycle to detect how it has to progress
     * @return 
     */
    protected ProgressType readyToProgress() {
        
        // in case if we are after a bootstrap, resp. a progress to next time step,
        // the first network should perform an iteration, before it sends its token to the next.
        if(this.startingNetworkAfterBootstrap){
            this.startingNetworkAfterBootstrap =false;
            return ProgressType.DO_NEXT_ITERATION;
        }
        
        
        if(this.hasToken){
            // if previous network progressed to Next Step, then we also progress
            // to next step
            if(this.progressedToNextStep.contains(this.previousNetwork)){
                return ProgressType.DO_NEXT_STEP;
            }
            
            // if our SimulationAgent is not converged, we do another iteration
            if(!getSimulationAgent().isConverged()){
                return ProgressType.DO_NEXT_ITERATION;
            } 
            else{
                if(externalNetworksConverged()){
//                    return ProgressType.DO_NEXT_STEP;
                    // only the starting Network is allowed to progress to next 
                    // time step
                    if(this.isFirst()){
            //            this.startingNetworkAfterBootstrap= true;
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
        }
        else 
            return ProgressType.DO_NOTHING;
    }
    /**
     * Always called after every Communication with the AbstractCommunication Event
     * (agentFinishedStep, incomming EventMessage etc.). Allows subclass to 
     * introduce logic and change it state after each communication.
     * @param messageType 
     */
    protected boolean postProcessCommunicationEvent(CommunicationEventType messageType) {
        switch(messageType){
            case AGENT_IS_READY:
//                if(this.afterbootStrap == true){
//                    this.afterbootStrap = false; // has to be done, so that network is not sending away token if its after bootsrap
//                    return true;
//                }
                if(this.hasToken && !startingNetworkAfterBootstrap) // has to be done, as we are handling after bootstrap differently in readytoProgress
                    sendTokenToNext();
                return true;
          
            default:
                break;
        }
        return false;
    }
    
    private void sendTokenToNext(){
        if(this.hasToken){
            this.hasToken = false;
            TokenMessage message = new TokenMessage(getSimulationAgent().getNetworkIndex());
            sendToSpecific(message, nextNetwork);
            logger.info("At Network " + Integer.toString(this.getSimulationAgent().getNetworkIndex()) + 
                        ": Send Token To Network " + Integer.toString(this.nextNetwork));
            }
    }   
    
    private boolean isFirst(){
        return (this.getSimulationAgent().getNetworkIndex() == startingNetwork);
    }

   /**
    * AbstractLocalSimulationCommunicationAgent Functions in original Implementation
    */
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
        
        // dummy function only for the DEBUG case 
        Collection<Integer> indices = makeSequence(0, this.totalNumberNetworks);
        Map<Integer, NetworkAddress> allAddresses = getAllExternalNetworkAddresses();
        
        Map<Integer, NetworkAddress> connectedAddresses = new HashMap<>();
        
        for(int i : indices)
            connectedAddresses.put(i, allAddresses.get(i));
        
        return connectedAddresses;
    }
    
   List<Integer> makeSequence(int begin, int end) {
        List<Integer> ret = new ArrayList(end - begin + 1);

        for(int i = begin; i <= end; i++, ret.add(i));

        return ret;  
    }


    protected NetworkAddress getNetworkAddress(int networkIndex) {
        return getAllExternalNetworkAddresses().get(networkIndex);
    }
    
}
