/*
 * Copyright (C) 2015 SFINA Team
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

import core.SimulationAgentNew;
import interdependent.EventMessage;
import interdependent.FlowNetworkMessage;
import interdependent.InterdependentFlowNetwork;
import interdependent.InterdependentTopologyLoaderNew;
import interdependent.StatusMessage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import network.FlowNetwork;
import network.Link;
import network.Node;
import org.apache.log4j.Logger;
import interdependent.InterdependentTopologyWriter;
import protopeer.network.Message;
import protopeer.network.NetworkAddress;
import protopeer.util.quantities.Time;

/**
 *
 * @author Ben
 */
public class InterdependentNetworkAgent extends SimulationAgentNew{
    
    private static final Logger logger = Logger.getLogger(InterdependentNetworkAgent.class);
    
    private HashMap<NetworkAddress, FlowNetwork> nets;
    private HashMap<NetworkAddress, StatusMessage> statusMessages;
    
    
    public InterdependentNetworkAgent(
            String experimentID,
            Time bootstrapTime, 
            Time runTime){
        super(experimentID, bootstrapTime, runTime);
        nets = new HashMap<>();
        this.setFlowNetwork(new InterdependentFlowNetwork());
        statusMessages = new HashMap<>();
    }
    
    /**
     * Method called at the end of bootstraping
     */
    @Override
    public void initializeInterdependentNetwork() {
        this.setTopologyLoader(new InterdependentTopologyLoaderNew(getFlowNetwork(), getColumnSeparator()));
        this.setTopologyWriter(new InterdependentTopologyWriter(getFlowNetwork(), getColumnSeparator()));
    }
        
    @Override
    public void handleIncomingMessage(Message message) {
        logger.debug("\n##### Incoming message at interdependent network agent from network " + message.getSourceAddress());
        if(message instanceof StatusMessage){
            processIncomingStatusMessage((StatusMessage)message);
        }
        else if(message instanceof FlowNetworkMessage){
            NetworkAddress sourceAddress = message.getSourceAddress();
            this.nets.put(sourceAddress, ((FlowNetworkMessage)message).getFlowNetwork());
            logger.debug("FlowNetwork message successfully received at InterdependentNetworkAgent from network " + sourceAddress);
            this.getPeer().sendMessage(sourceAddress, new FlowNetworkMessage(this.getFlowNetwork()));
        }
        else if(message instanceof EventMessage)
            this.queueEvent(((EventMessage)message).getEvent());
        else
            logger.debug("Didn't recognize message. Ignoring.");
    }
    
    public void processIncomingStatusMessage(StatusMessage msg) {
        statusMessages.put(msg.getSourceAddress(), msg);
        logger.info("Interdependent network: Message, Network " + msg.getSourceAddress() + ", iteration = " + (msg.getIteration()) + ", flag networkChanged = " + msg.isNetworkChanged());
        logger.info("## Status Message number " + statusMessages.size());
        // Only react when status messages from all other networks arrived
        if(statusMessages.size() == getNumberOfNets()){
            ArrayList<Integer> iterations = new ArrayList<>();
            Boolean netsChanged = false;
            for(StatusMessage statusMsg : statusMessages.values()){
                iterations.add(statusMsg.getIteration());
                if(statusMsg.isNetworkChanged())
                    netsChanged = true;
            }
            for(int i=0; i<iterations.size()-1; i++)
                if(iterations.get(i) - iterations.get(i+1) != 0)
                    logger.debug("Iterations differ more than 1 at networks: " + statusMessages.get(i) + ", " + statusMessages.get(i+1));
            
            statusMessages.clear();
            if(getIteration() == 1){
                logger.info("Interdependent network: All networks have initialized time step. Telling them to start first iteration.");
                for(NetworkAddress address : this.getNetworkAddresses())
                    this.getPeer().sendMessage(address, new StatusMessage(netsChanged, this.getIteration()));
                this.nextIteration();
                
            }
            else if(netsChanged){
                logger.info("Interdependent network: One of the networks has pending changes, therefore sending message, to tell them to continue.");
                this.executeAllEvents();
                for(NetworkAddress address : this.getNetworkAddresses())
                    this.getPeer().sendMessage(address, new StatusMessage(netsChanged, this.getIteration()));
                this.nextIteration();
            }else
                logger.info("Interdependent network: All networks converged.");
            
        }
    }
    
    @Override
    public void initActiveState(){
        this.setTimeToken(this.getTimeTokenName() + this.getSimulationTime());
        logger.info("\n--------------> " + this.getTimeToken() + " at interdependent network <--------------");
        resetIteration();
        // Instead of loading the nodes from file, they are added from the networks.
        // This assumes that the interdependent network is the last peer, s.t. the others have already loaded their data.
        addNodes(); 
        loadInputData(getTimeToken());   
    }
    
    @Override
    public void runFlowAnalysis(){
        checkInterdependentTopology(true);
    }
    
    
    private void addNodes() {
        for(NetworkAddress address : nets.keySet())
            for(Node node : nets.get(address).getNodes()){
                ((InterdependentFlowNetwork)this.getFlowNetwork()).addNode(node);
            }
    }
    
    /**
     * @return the Number of Interdependent Networks
     */
    public int getNumberOfNets() {
        return nets.size();
    }

    /**
     * @return the networkAddresses
     */
    public Collection<NetworkAddress> getNetworkAddresses() {
        return nets.keySet();
    }
    
    
    public boolean checkInterdependentTopology(boolean logDetails){
        boolean intact = true;
        if(logDetails){
            logger.debug("### Checking Interdependent Topology ###");
            logger.debug("Number of interlinks: " + this.getFlowNetwork().getLinks().size());
            logger.debug("Number of involved networks: " + this.getNumberOfNets());
            logger.debug("Network Addresses: " + this.getNetworkAddresses());
            logger.debug("Analyzing links...");
        }
        for(NetworkAddress a : this.getNetworkAddresses())
            if(!this.getNetworkAddresses().contains(a)){
                intact = false;
                logger.debug("! NetworkAddress of FlowNetwork not in NetworkAddress list.");
            }
        for (Link link : this.getFlowNetwork().getLinks()) {
            if(logDetails){
                logger.debug(String.format("Link StartNode: \tIndex = %s, Address = %s, contained in net = %s", link.getStartNode().getIndex(), link.getStartNode().getNetworkAddress(), this.nets.get(link.getStartNode().getNetworkAddress()).getNodes().contains(link.getStartNode())));
                logger.debug(String.format("Link EndNode: \tIndex = %s, Address = %s, contained in net = %s", link.getEndNode().getIndex(), link.getEndNode().getNetworkAddress(), this.nets.get(link.getEndNode().getNetworkAddress()).getNodes().contains(link.getEndNode())));
            }
            if(!this.nets.get(link.getStartNode().getNetworkAddress()).getNodes().contains(link.getStartNode())){
                intact = false;
                logger.debug("! StartNode of InterLink not in corresponding FlowNetwork.");
            }
            if(!this.nets.get(link.getEndNode().getNetworkAddress()).getNodes().contains(link.getEndNode())){
                intact = false;
                logger.debug("! EndNode of InterLink not in corresponding FlowNetwork.");
            }
        }
        logger.debug("-----> " + intact);
        return intact;
    }

    
}