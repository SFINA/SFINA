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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ben
 */
public interface NodeInterface {

    /**
     * Adds a link and evaluates the connectivity of the node.
     *
     * @param link the added link
     */
    void addLink(LinkInterface link);

    /**
     * Returns a new list with all incoming links of the node
     *
     * @return a new array list with the incoming links of the node
     */
    ArrayList<InterdependentLink> getIncomingInterdependentLinks();

    /**
     * Returns a new list with all incoming links of the node
     *
     * @return a new array list with the incoming links of the node
     */
    ArrayList<Link> getIncomingLinks();

    /**
     * Returns the index of the node
     *
     * @return the index of the node
     */
    String getIndex();

    List<InterdependentLink> getInterdependentLinks();

    /**
     * Returns the links of the node
     *
     * @return the links of the node
     */
    List<Link> getLinks();

    /**
     * Returns a new list with all outgoing links of the node
     *
     * @return a new array list with the outgoing links of the node
     */
    ArrayList<InterdependentLink> getOutgoingInterdependentLinks();

    /**
     * Returns a new list with all outgoing links of the node
     *
     * @return a new array list with the outgoing links of the node
     */
    ArrayList<Link> getOutgoingLinks();

    /**
     * Returned the activated/deactivated status of the node
     *
     * @return the activated/deactivated status of the node
     */
    boolean isActivated();

    /**
     * Returns the status of the node connectivity
     *
     * @return if the node is connected or disconnected
     */
    boolean isConnected();

    boolean isRemoteNode();

    /**
     * Removes the link and evaluates the connectivity of the node
     *
     * @param link the removed link
     */
    void removeLink(LinkInterface link);

    /**
     * Sets the activated/deactivated status of the node
     *
     * @param activated the activated to set
     */
    void setActivated(boolean activated);

    /**
     * Sets the index of the node
     *
     * @param index the index to set
     */
    void setIndex(String index);

    /**
     * Sets the links of the node
     *
     * @param links the links to set
     */
    void setLinks(List<Link> links);

    /**
     * @return the networkIndex
     */
    Integer getNetworkIndex();

    /**
     * @param networkIndex the networkIndex to set
     */
    void setNetworkIndex(Integer networkIndex);
    
}
