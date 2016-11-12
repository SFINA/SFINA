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
    
    private int startNodeNetworkIndex;
    private int endNodeNetworkIndex;
    private static final Logger logger = Logger.getLogger(InterdependentLink.class);
    
    public InterdependentLink(String index, boolean activated, NodeInterface startNode, NodeInterface endNode, int startNodeNetworkIndex, int endNodeNetworkIndex) {
        super(index, activated, startNode, endNode);
        this.startNodeNetworkIndex = startNodeNetworkIndex;
        this.endNodeNetworkIndex = endNodeNetworkIndex;
    }

    /**
     * @return the endNodeNetworkIndex
     */
    public int getEndNodeNetworkIndex() {
        return endNodeNetworkIndex;
    }

    /**
     * @param endNodeNetworkIndex the endNodeNetworkIndex to set
     */
    public void setEndNodeNetworkIndex(int endNodeNetworkIndex) {
        this.endNodeNetworkIndex = endNodeNetworkIndex;
    }

    /**
     * @return the startNodeNetworkIndex
     */
    public int getStartNodeNetworkIndex() {
        return startNodeNetworkIndex;
    }

    /**
     * @param startNodeNetworkIndex the startNodeNetworkIndex to set
     */
    public void setStartNodeNetworkIndex(int startNodeNetworkIndex) {
        this.startNodeNetworkIndex = startNodeNetworkIndex;
    }

    /**
     *
     * @return if the node points to or away from this network
     */
    public Boolean isOutgoing(){
        boolean startIsRemote = this.getStartNode() instanceof RemoteNode;
        boolean endIsRemote = this.getEndNode() instanceof RemoteNode;
        if(startIsRemote || endIsRemote)
            return endIsRemote;
        else {
            logger.debug("InterdependentLink doesn't have a RemoteNode at one of its ends. Shouldn't happen. Index = " + this.getIndex());
            return null;
        }
    }
    
    /**
     *
     * @return
     */
    public Boolean isIncoming(){
        return !isOutgoing();
    }
}
