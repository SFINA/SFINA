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
package backend;

import network.Link;
import network.Node;



/**
 * Translation of data to java variables for input and output.
 * @author evangelospournaras
 */
public interface FlowNetworkDataTypesInterface {
    
    /**
     * 
     * @return domain specific NodeStates as Enum[].
     */
    public Enum[] getNodeStates();
    
    /**
     * 
     * @return domain specific LinkStates as Enum[].
     */
    public Enum[] getLinkStates();
    
    public Enum parseNodeStateTypeFromString(String nodeState);
    
    public Enum parseLinkStateTypeFromString(String linkState);
    
    public Object parseNodeValuefromString(Enum nodeState, String rawValue);
    
    public Object parseLinkValueFromString(Enum linkState, String rawValue);
    
    public String castNodeStateTypeToString(Enum nodeState);
    
    public String castLinkStateTypeToString(Enum linkState);
    
    public String castNodeStateValueToString(Enum nodeState, Node node, String missingValue);
    
    public String castLinkStateValueToString(Enum linkState, Link link, String missingValue);
    
}
