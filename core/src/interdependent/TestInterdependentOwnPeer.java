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
import org.apache.log4j.Logger;
import power.backend.InterpssFlowDomainAgent;
import protopeer.Experiment;
import protopeer.Peer;
import protopeer.PeerFactory;
import protopeer.SimulatedExperiment;
import protopeer.network.NetworkAddress;
import protopeer.util.quantities.Time;

/**
 *
 * @author evangelospournaras
 */
public class TestInterdependentOwnPeer extends SimulatedExperiment{
    
    private static final Logger logger = Logger.getLogger(TestInterdependentOwnPeer.class);
    
    private final static String expSeqNum="01";
    
    private static String experimentID="experiment-"+expSeqNum;
    
    //Simulation Parameters
    private final static int bootstrapTime=2000;
    private final static int runTime=1000;
    private final static int runDuration=7; // Number of simulation steps + 2 bootstrap steps
    private final static int N=3; // Number of interconnected networks + 1 network between networks.
    
    
    public static void main(String[] args) {
        Experiment.initEnvironment();
        TestInterdependentOwnPeer test = new TestInterdependentOwnPeer();
        test.init();
        
        boolean isInterdependent = (N>1);
        
        PeerFactory peerFactory = new PeerFactory() {
            public Peer createPeer(int peerIndex, Experiment experiment) {                
                Peer peer = new Peer(peerIndex);
                if(!isInterdependent){
                    peer.addPeerlet(new SimulationAgentNew(
                            experimentID, 
                            Time.inMilliseconds(bootstrapTime),
                            Time.inMilliseconds(runTime)));
                }
                else{
                    NetworkAddress interdependencePeerAddress = null;
                    if(peerIndex == N-1){
                        peer.addPeerlet(new InterdependentNetworkAgent(
                                experimentID, 
                                Time.inMilliseconds(bootstrapTime),
                                Time.inMilliseconds(runTime)));
                        interdependencePeerAddress = peer.getFinger().getNetworkAddress();
                        logger.debug(interdependencePeerAddress);
                    }
                    else
                        peer.addPeerlet(new PowerExchangeAgentNew(
                                experimentID, 
                                Time.inMilliseconds(bootstrapTime),
                                Time.inMilliseconds(runTime)));
                }
                
                // Adding the backend
                peer.addPeerlet(new InterpssFlowDomainAgent(
                    experimentID, 
                    Time.inMilliseconds(bootstrapTime),
                    Time.inMilliseconds(runTime)));
                return peer;
            }
        };
        
        test.initPeers(0,N,peerFactory);
        test.startPeers(0,N);
        
        NetworkAddress interdependencePeerAddress = null;
        for(Peer peer : test.getPeers())
            if(peer.getNetworkAddress().toString().equals(String.valueOf(N-1)))
                interdependencePeerAddress = peer.getNetworkAddress();
        for(Peer peer : test.getPeers())
            if(!peer.getNetworkAddress().toString().equals(String.valueOf(N-1)))
                ((SimulationAgentInterdependence)peer.getPeerletOfType(SimulationAgentInterdependence.class)).sendFlowNetworkToInterdependentPeer(interdependencePeerAddress); 
        
        //run the simulation
        test.runSimulation(Time.inSeconds(runDuration));
    }
}
