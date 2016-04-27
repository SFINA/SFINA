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
package power.output;

import diseasespread.input.DiseaseSpreadLinkState;
import diseasespread.input.DiseaseSpreadNodeState;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import network.FlowNetwork;
import network.Node;
import network.Link;
import org.apache.log4j.Logger;

/**
 *
 * @author dinesh
 */
public class DiseaseSpreadFlowWriter {
    private FlowNetwork net;
    private String columnSeparator;
    private String missingValue;
    private static final Logger logger = Logger.getLogger(DiseaseSpreadFlowWriter.class);
    
    public DiseaseSpreadFlowWriter(FlowNetwork net, String columnSeparator, String missingValue){
        this.net = net;
        this.columnSeparator=columnSeparator;
        this.missingValue = missingValue;
    }
    
    public void writeNodeFlowData(String location){
        try{
            File file = new File(location);
            File parent = file.getParentFile();
            if(!parent.exists()&&!parent.mkdirs()){
                logger.debug("Couldn't create output folder");
            }
            file.createNewFile();
            PrintWriter writer = new PrintWriter(new FileWriter(file, false));
            writer.print("id");
            ArrayList<DiseaseSpreadNodeState> necessaryStates = new ArrayList<DiseaseSpreadNodeState>();
            ArrayList<String> stateStrings = new ArrayList<String>();
            for (DiseaseSpreadNodeState state : DiseaseSpreadNodeState.values()){
                String stateString = lookupHealthNodeState(state);
                if(stateString!=null){
                    necessaryStates.add(state);
                    stateStrings.add(stateString);
                }
            }
            
            for(int i=0; i<stateStrings.size();i++){
                writer.print(columnSeparator+stateStrings.get(i));
            }
            writer.print("\n");
            
            for(Node node:net.getNodes()){
                writer.print(node.getIndex());
                for(int i =0;i<necessaryStates.size();i++)
                    writer.print(columnSeparator+node.getProperty(necessaryStates.get(i)));
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
            ArrayList<DiseaseSpreadLinkState> necessaryStates = new ArrayList<DiseaseSpreadLinkState>();
            ArrayList<String> stateStrings = new ArrayList<String>();
            for (DiseaseSpreadLinkState state : DiseaseSpreadLinkState.values()){
                String stateString = lookupHealthLinkState(state);
                if (stateString != null){
                    necessaryStates.add(state);
                    stateStrings.add(stateString);
                }
            }

            for(int i=0; i<stateStrings.size();i++)
                writer.print(columnSeparator+stateStrings.get(i));
            writer.print("\n");

            for(Link link:net.getLinks()){
                writer.print(link.getIndex());
                for(int i=0;i<necessaryStates.size();i++)
                    writer.print(columnSeparator+link.getProperty(necessaryStates.get(i)));
                writer.print("\n");
            }
            writer.close();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    private String lookupHealthNodeState(DiseaseSpreadNodeState healthNodeState){
        switch(healthNodeState){
            case ID:
                return null;
            case HEALTH:
                return "health";
            case ALPHA:
                return "alpha";
            case BETA:
                return "beta";
            case RESISTANCETHRESHOLD:
                return "resistance_threshold";
            case RECOVERYRATE:
                return "recovery_rate";
            default:
                logger.debug("health node state is not recognized.");
                return null;
        }
    }
    
    private String lookupHealthLinkState(DiseaseSpreadLinkState healthLinkState){
        switch(healthLinkState){
            case ID:
                return null;
            case CONNECTION_STRENGTH:
                return "connection_strength";
            case TIME_DELAY:
                return "time_delay";
            default:
                logger.debug("Power link state is not recognized.");
                return null;
        }
    }
}
