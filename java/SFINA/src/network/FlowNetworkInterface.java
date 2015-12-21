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
package network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 *
 * @author evangelospournaras
 */
public interface FlowNetworkInterface {
    
    public Collection<Node> getNodes();
    
    public Collection<Link> getLinks();
    
    public void addNode(Node node);
    
    public void addLink(Link link);
    
    public void removeNode(Node node);
    
    public void removeLink(Link link);
    
    public Node getNode(String index);
    
    public Link getLink(String index);
    
    public Link getLink(Node startNode, Node endNode);
    
    public void activateNode(String index);
    
    public void activateLink(String index);
    
    public void deactivateNode(String index);
    
    public void deactivateLink(String index);
    
    public ArrayList<FlowNetwork> computeIslands();
    
    public double getAvgNodeDegree();
    
    public LinkedHashMap getDegreeDist();
    
    public double getClustCoeff();
    
    public double getAvgShortestPath();

    public double getDegCentrality(Node node);
    
    public double getClosenessCentrality(Node node);
    
    /*public double getEffGraphRes();
    
    public double getBtwnCentrality(Node node);
    
    public double getBtwnCentrality(Link link);

    public double getDegCentrality(Link link);*/
    
}
