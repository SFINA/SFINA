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

import backend.FlowNetworkDataTypesInterface;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import network.FlowNetwork;
import network.Link;
import network.Node;
import org.apache.log4j.Logger;

/**
 *
 * @author Ben
 */
public class FlowWriterNew {

    private static final Logger logger = Logger.getLogger(FlowWriterNew.class);

    private FlowNetwork net;
    private String columnSeparator;
    private String missingValue;
    private FlowNetworkDataTypesInterface flowNetworkDataTypes;
    
    public FlowWriterNew(FlowNetwork net, String columnSeparator, String missingValue, FlowNetworkDataTypesInterface flowNetworkDataTypes){
        this.net=net;
        this.columnSeparator=columnSeparator;        
        this.missingValue = missingValue;
        this.flowNetworkDataTypes=flowNetworkDataTypes;
    }
    
    public void writeNodeFlowData(String location){
        try{
            File file = new File(location);
            File parent = file.getParentFile();
            if(!parent.exists() && !parent.mkdirs())
                logger.debug("Couldn't create output folder");
            file.createNewFile();
            PrintWriter writer = new PrintWriter(new FileWriter(file,false));
            writer.print("id");
            ArrayList<Enum> necessaryStates = new ArrayList<>();
            ArrayList<String> stateStrings = new ArrayList<>();
            for (Enum state : this.getFlowNetworkDataTypes().getNodeStates()){
                String stateString = this.getFlowNetworkDataTypes().castNodeStateTypeToString(state);
                if (stateString != null){
                    necessaryStates.add(state);
                    stateStrings.add(stateString);
                }
            }
            
            for (int i=0; i<stateStrings.size(); i++)
                writer.print(columnSeparator + stateStrings.get(i));
            writer.print("\n");
            
            for (Node node : net.getNodes()){
                writer.print(node.getIndex());
                for (int i=0; i<necessaryStates.size(); i++)
                    writer.print(columnSeparator + this.getFlowNetworkDataTypes().castNodeStateValueToString(necessaryStates.get(i), node, missingValue));
                writer.print("\n");
            }
            writer.close();   
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public void writeLinkFlowData(String location){
        try{
            File file = new File(location);
            File parent = file.getParentFile();
            if(!parent.exists() && !parent.mkdirs())
                logger.debug("Couldn't create output folder");
            file.createNewFile();
            PrintWriter writer = new PrintWriter(new FileWriter(file,false));
            writer.print("id");
            ArrayList<Enum> necessaryStates = new ArrayList<>();
            ArrayList<String> stateStrings = new ArrayList<>();
            for (Enum state : this.getFlowNetworkDataTypes().getLinkStates()){
                String stateString = this.getFlowNetworkDataTypes().castLinkStateTypeToString(state);
                if (stateString != null){
                    necessaryStates.add(state);
                    stateStrings.add(stateString);
                }
            }
            
            for (int i=0; i<stateStrings.size(); i++)
                writer.print(columnSeparator + stateStrings.get(i));
            writer.print("\n");
            
            for (Link link : net.getLinks()){
                writer.print(link.getIndex());
                for (int i=0; i<necessaryStates.size(); i++)
                    writer.print(columnSeparator + this.getFlowNetworkDataTypes().castLinkStateValueToString(necessaryStates.get(i), link, missingValue));
                writer.print("\n");
            }
            writer.close();   
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    /**
     * @return the flowNetworkDataTypes
     */
    public FlowNetworkDataTypesInterface getFlowNetworkDataTypes() {
        if(flowNetworkDataTypes == null)
            logger.debug("Domain backend has to call setFlowNetworkDataTypes method, but probably didn't.");
        return flowNetworkDataTypes;
    }

    /**
     * @param flowNetworkDataTypes the flowNetworkDataTypes to set
     */
    public void setFlowNetworkDataTypes(FlowNetworkDataTypesInterface flowNetworkDataTypes) {
        this.flowNetworkDataTypes = flowNetworkDataTypes;
    }
}
