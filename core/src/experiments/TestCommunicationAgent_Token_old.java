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

import core.Archive.SimulationAgent_old;
import interdependent.communication.TokenCommunicationAgent_old;
import interdependent.communication.PowerEventNegotiatorAgent;
import org.apache.log4j.Logger;
import power.backend.InterpssFlowDomainAgent;
import protopeer.Experiment;
import protopeer.Peer;
import protopeer.PeerFactory;
import protopeer.SimulatedExperiment;
import protopeer.util.quantities.Time;

/**
 *
 * @author root
 */
public class TestCommunicationAgent_Token_old extends SimulatedExperiment{
    
    private static final Logger logger = Logger.getLogger(TestInterpssBackend.class);
    
   
    private final static String expSeqNum="01";
    private static String experimentID="experiment-"+expSeqNum;
    
    //Simulation Parameters
    private final static int bootstrapTime=2000;
    private final static int runTime=1000;
    private final static int runDuration=6;
    private final static int N=3;
    
    public static void main(String[] args) {
        Experiment.initEnvironment();
        TestInterpssBackend test = new TestInterpssBackend();
        test.init();
        
        PeerFactory peerFactory=new PeerFactory() {
            public Peer createPeer(int peerIndex, Experiment experiment) {
                Peer newPeer = new Peer(peerIndex);
                newPeer.addPeerlet(new SimulationAgent_old(
                        experimentID, 
                        Time.inMilliseconds(bootstrapTime),
                        Time.inMilliseconds(runTime)));
                 //NECESSARY HELPER  AGENTS
                newPeer.addPeerlet(new TokenCommunicationAgent_old(N,0));
                newPeer.addPeerlet(new InterpssFlowDomainAgent());
                newPeer.addPeerlet(new PowerEventNegotiatorAgent());
                return newPeer;
            }
        };
        test.initPeers(0,N,peerFactory);
        test.startPeers(0,N);
        
        //run the simulation
        test.runSimulation(Time.inSeconds(runDuration));
       
    }
    
}
