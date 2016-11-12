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


/**
 *
 * @author Ben
 */
public interface LinkInterface{

    /**
     * Returns the capacity of the link if a capacity type is defined
     *
     * @return the capacity of the link
     */
    double getCapacity();

    /**
     * Returns the end node of the link
     *
     * @return the endNode of the link
     */
    NodeInterface getEndNode();

    /**
     * Returns the flow of the link if a flow type is defined
     *
     * @return the flow of the link
     */
    double getFlow();

    /**
     * Returns the index of the link
     *
     * @return the index of the link
     */
    String getIndex();

    /**
     * Returns the start node of the link
     *
     * @return the startNode of the link
     */
    NodeInterface getStartNode();

    /**
     * Returns if the link is activated or not
     *
     * @return if the link is activated or not
     */
    boolean isActivated();

    /**
     * Returns if connected or disconnected
     *
     * @return if connected or disconnected
     */
    boolean isConnected();

    /**
     * Check if link is interdependent.
     * Returns true if StartNode and EndNode of link are in same network.
     *
     * @return if link is interdependent
     */
    boolean isInterdependent();

    /**
     * Sets the link as activated/deactivated
     *
     * @param activated the status activated/deactivated of the link
     */
    void setActivated(boolean activated);

    /**
     * Sets the capacity of the link if a capacity type is defined
     *
     * @param capacity the capacity of the link
     */
    void setCapacity(double capacity);

    /**
     * Sets the capacity type of the link
     *
     * @param capacityType the capacity type of the link
     */
    void setCapacityType(Enum capacityType);

    /**
     * Sets the end node of the link and evaluates the connectivity
     *
     * @param endNode the endNode of the link
     */
    void setEndNode(Node endNode);

    /**
     * Sets the flow of the link if a flow type is defined
     *
     * @param flow the flow of the link
     */
    void setFlow(double flow);

    /**
     * Sets the flow type of the link
     *
     * @param flowType the flow type of the link
     */
    void setFlowType(Enum flowType);

    /**
     * Sets the index of the link
     *
     * @param index the index of the link
     */
    void setIndex(String index);

    /**
     * Sets the start node of the link and evaluates connectivity
     *
     * @param startNode the startNode of the link
     */
    void setStartNode(Node startNode);
    
}
