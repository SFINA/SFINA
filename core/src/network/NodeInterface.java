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
     * Returns the index of the node
     *
     * @return the index of the node
     */
    String getIndex();
    
    /**
     * Sets the index of the node
     *
     * @param index the index to set
     */
    void setIndex(String index);
    
    /**
     * Adds a link and evaluates the connectivity of the node.
     *
     * @param link the added link
     */
    void addLink(LinkInterface link);
    
    /**
     * Removes the link and evaluates the connectivity of the node
     *
     * @param link the removed link
     */
    void removeLink(LinkInterface link);
    
    /**
     * Sets the links of the node
     *
     * @param links the links to set
     */
    void setLinks(List<LinkInterface> links);

    /**
     * Returns all the links (local and interdependent) of the node
     *
     * @return the links of the node
     */
    List<LinkInterface> getLinksAll();
    
    /**
     * Returns the local links of the node
     *
     * @return the links of the node
     */
    List<Link> getLinks();
    
    /**
     * Returns the interdependent links of the node
     *
     * @return the links of the node
     */
    List<InterdependentLink> getLinksInterdependent();

    /**
     * Returns a new list with all incoming links of the node
     *
     * @return a new array list with the incoming links of the node
     */
    ArrayList<Link> getIncomingLinks();
    
    /**
     * Returns a new list with all outgoing local links of the node
     *
     * @return a new array list with the outgoing links of the node
     */
    ArrayList<Link> getOutgoingLinks();

    /**
     * Returns a new list with all incoming links of the node
     *
     * @return a new array list with the incoming links of the node
     */
    ArrayList<InterdependentLink> getIncomingInterdependentLinks();
    
    /**
     * Returns a new list with all outgoing interdependent links of the node
     *
     * @return a new array list with the outgoing links of the node
     */
    ArrayList<InterdependentLink> getOutgoingInterdependentLinks();
    
    /**
     * Checks if interdependent links are connected to this node
     * 
     * @return if interdependent links are connected to this node
     */
    boolean hasInterdependentLinks();
    
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

    /**
     * Sets the activated/deactivated status of the node
     *
     * @param activated the activated to set
     */
    void setActivated(boolean activated);

    /**
     * Returns the capacity of the node if a capacity type is defined
     *
     * @return the capacity of the node
     */
    double getCapacity();

    /**
     * Returns the flow value if a flow type is defined.
     *
     * @return the flow
     */
    double getFlow();

    /**
     * Sets the capacity of the node if a capacity type is defined
     *
     * @param capacity the capacity of the node
     */
    void setCapacity(double capacity);

    /**
     * Sets the capacity type of the node
     *
     * @param capacityType the capacity type of the node
     */
    void setCapacityType(Enum capacityType);

    /**
     * Sets the flow if a flow type is defined
     *
     * @param flow the flow set
     */
    void setFlow(double flow);

    /**
     * Sets the flow type
     *
     * @param flowType the set flow type
     */
    void setFlowType(Enum flowType);
    
}
