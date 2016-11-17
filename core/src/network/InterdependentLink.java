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
package network;

import org.apache.log4j.Logger;

/**
 *
 * @author Ben
 */
public class InterdependentLink extends Link implements LinkInterface{
    
    private int thisNetworkIndex;
    private int remoteNetworkIndex;
    private boolean isOutgoing;
    private static final Logger logger = Logger.getLogger(InterdependentLink.class);
    
    public InterdependentLink(String index, boolean activated, NodeInterface startNode, NodeInterface endNode, int thisNetworkIndex, int remoteNetworkIndex, String remoteNodeIndex, boolean isRemoteNodeActivated) {
        super(index, activated, startNode, endNode);
        this.thisNetworkIndex = thisNetworkIndex;
        this.remoteNetworkIndex = remoteNetworkIndex;
        if (endNode == null && startNode != null){
            this.isOutgoing = true;
            this.setEndNode(new Node(remoteNodeIndex, isRemoteNodeActivated));
        }
        else if (startNode == null && endNode != null){
            this.isOutgoing = false;
            this.setStartNode(new Node(remoteNodeIndex, isRemoteNodeActivated));
        }
        else
            logger.debug("The start or end node of interdependent link which is in other network should be null.");
    }

    /**
     * Check if link is interdependent.
     * Returns true if StartNode and EndNode of link are in same network.
     * 
     * @return if link is interdependent
     */
    @Override
    public boolean isInterdependent(){
        return true;
    }
    
    /**
     *
     * @return if the node points to or away from this network
     */
    public Boolean isOutgoing(){
        return this.isOutgoing;
    }
    
    /**
     *
     * @return
     */
    public Boolean isIncoming(){
        return !isOutgoing();
    }

    /**
     * @return the thisNetworkIndex
     */
    public int getThisNetworkIndex() {
        return thisNetworkIndex;
    }

    /**
     * @param thisNetworkIndex the thisNetworkIndex to set
     */
    public void setThisNetworkIndex(int thisNetworkIndex) {
        this.thisNetworkIndex = thisNetworkIndex;
    }

    /**
     * @return the remoteNetworkIndex
     */
    public int getRemoteNetworkIndex() {
        return remoteNetworkIndex;
    }

    /**
     * @param remoteNetworkIndex the remoteNetworkIndex to set
     */
    public void setRemoteNetworkIndex(int remoteNetworkIndex) {
        this.remoteNetworkIndex = remoteNetworkIndex;
    }
}
