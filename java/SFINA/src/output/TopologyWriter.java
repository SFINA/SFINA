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
package output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import network.FlowNetwork;
import network.Link;
import network.Node;
import org.apache.log4j.Logger;

/**
 *
 * @author Ben
 */
public class TopologyWriter {
    
    private FlowNetwork net;
    private String columnSeparator;
    private static final Logger logger = Logger.getLogger(TopologyWriter.class);
    
    public TopologyWriter(FlowNetwork net, String columnSeparator){
        this.net=net;
        this.columnSeparator=columnSeparator;        
    }
    
    public void writeNodes(String location){
        try{
            File file = new File(location);
            File parent = file.getParentFile();
            if(!parent.exists() && !parent.mkdirs())
                logger.debug("Couldn't create output folder");
            file.createNewFile();
            PrintWriter writer = new PrintWriter(new FileWriter(file,false));
            writer.println("id" + columnSeparator + "status");
            for (Node node : net.getNodes()){
                String status;
                if (node.isActivated())
                    status = "1";
                else 
                    status = "0";
                writer.println(node.getIndex() + columnSeparator + status);
            }
            writer.close();
            
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public void writeLinks(String location){
        try{
            File file = new File(location);
            File parent = file.getParentFile();
            if(!parent.exists() && !parent.mkdirs())
                logger.debug("Couldn't create output folder");
            file.createNewFile();
            PrintWriter writer = new PrintWriter(new FileWriter(file,false));
            writer.println("id" + columnSeparator + "from_node_id" + columnSeparator + "to_node_id" + columnSeparator + "status");
            for (Link link : net.getLinks()){
                String status;
                if (link.isActivated())
                    status = "1";
                else 
                    status = "0";
                writer.println(link.getIndex() + columnSeparator + link.getStartNode().getIndex() + columnSeparator + link.getEndNode().getIndex() + columnSeparator + status);
            }
            writer.close();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }    
}
