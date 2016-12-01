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
package experiments;

import interdependent.communication.CommunicationAgent;
import interdependent.communication.SimpleTimeSteppingAgent;
import interdependent.communication.SimulationAgentCommunication;
import org.apache.log4j.Logger;
import power.backend.InterpssFlowDomainAgent;
import protopeer.Experiment;
import protopeer.NeighborManager;
import protopeer.Peer;
import protopeer.PeerFactory;
import protopeer.SimulatedExperiment;
import protopeer.servers.bootstrap.BootstrapClient;
import protopeer.servers.bootstrap.BootstrapServer;
import protopeer.servers.bootstrap.SimpleConnector;
import protopeer.servers.bootstrap.SimplePeerIdentifierGenerator;
import protopeer.util.quantities.Time;

/**
 *
 * @author root
 */
public class TestCommunicationAgent_communicationTimeStepping extends SimulatedExperiment{
    
    private static final Logger logger = Logger.getLogger(TestInterpssBackend.class);
    
   
    private final static String expSeqNum="01";
    private static String experimentID="experiment-"+expSeqNum;
    
    //Simulation Parameters
    private final static int bootstrapTime=2000;
    private final static int runTime=1000;
    private final static int runDuration=6;
    private final static int N=2;
    
    public static void main(String[] args) {
        Experiment.initEnvironment();
        TestInterpssBackend test = new TestInterpssBackend();
        test.init();
        
        PeerFactory peerFactory=new PeerFactory() {
            public Peer createPeer(int peerIndex, Experiment experiment) {
                Peer newPeer = new Peer(peerIndex);
//                if (peerIndex == 0) {
//                   newPeer.addPeerlet(null);
//                }
//                if (peerIndex == 0) {
//                    newPeer.addPeerlet(new BootstrapServer());
//                }
//                //Connection AGENTS
//                 newPeer.addPeerlet(new NeighborManager());
//                  newPeer.addPeerlet(new SimpleConnector());
//                   newPeer.addPeerlet(new BootstrapClient(Experiment.getSingleton().getAddressToBindTo(0), new
//                SimplePeerIdentifierGenerator()));

                //SFINA AGENTS
                newPeer.addPeerlet(new CommunicationAgent(N));
                newPeer.addPeerlet(new SimulationAgentCommunication(
                        experimentID, 
                        Time.inMilliseconds(bootstrapTime),
                        Time.inMilliseconds(runTime)));
                newPeer.addPeerlet(new InterpssFlowDomainAgent(
                        experimentID, 
                        Time.inMilliseconds(bootstrapTime),
                        Time.inMilliseconds(runTime)));
               
               
               
                return newPeer;
            }
        };
        test.initPeers(0,N,peerFactory);
        test.startPeers(0,N);
        
        //run the simulation
        test.runSimulation(Time.inSeconds(runDuration));
       
    }
    
}