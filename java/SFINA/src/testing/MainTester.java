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
package testing;

import java.util.ArrayList;
import network.Link;
import network.Node;

/**
 *
 * @author Ben
 */
public class MainTester {
     
    public static void main(String[] args){
        // Test Loader. Argument true if loaded data should be printed to Output
        testLoader testloader = new testLoader(true);
        
        // Get loaded data for other testers
        ArrayList<Node> nodes = testloader.getNodes();
        ArrayList<Link> links = testloader.getLinks();
        
        // Test InterPSS
        testInterpss testinterpss = new testInterpss(nodes, links);
    }
}
