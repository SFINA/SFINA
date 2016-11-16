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
package output;

import event.Event;
import backend.FlowNetworkDataTypesInterface;
import input.SfinaParameter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import network.LinkState;
import network.NodeState;
import org.apache.log4j.Logger;

/**
 *
 * @author Ben
 */
public class EventWriter {
    
    private static final Logger logger = Logger.getLogger(FlowWriterNew.class);
    
    private String columnSeparator;
    private String missingValue;
    private File file;
    private FlowNetworkDataTypesInterface flowNetworkDataTypes;
    
    public EventWriter(String location, String columnSeparator, String missingValue, FlowNetworkDataTypesInterface flowNetworkDataTypes){
        this.columnSeparator=columnSeparator;
        this.missingValue=missingValue;
        this.file = new File(location);
        this.flowNetworkDataTypes=flowNetworkDataTypes;
        this.setupEventOutputFile();
    }
    
    private void setupEventOutputFile(){
        File parent = file.getParentFile();
        if(!parent.exists() && !parent.mkdirs())
            logger.debug("Couldn't create output folder");
        try{
            file.createNewFile();
            PrintWriter writer = new PrintWriter(new FileWriter(file,false));
            writer.println("time" + this.columnSeparator + "feature" + this.columnSeparator + "component" + this.columnSeparator + "id" + this.columnSeparator + "parameter" + this.columnSeparator + "value");
            writer.close();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public void writeEvent(Event event){
        ArrayList<String> eventStrings = new ArrayList<>();
        eventStrings.add(String.valueOf(event.getTime()));
        switch(event.getEventType()){
            case TOPOLOGY:
                eventStrings.add("topology");
                switch(event.getNetworkComponent()){
                    case NODE:
                        eventStrings.add("node");
                        eventStrings.add(event.getComponentID());
                        switch((NodeState)event.getParameter()){
                            case ID:
                                eventStrings.add("id");
                                eventStrings.add(event.getValue().toString());
                                break;
                            case STATUS:
                                eventStrings.add("status");
                                eventStrings.add((Boolean)event.getValue() ? "1" : "0");
                                break;
                            default:
                                logger.debug("Node state cannot be recognised");
                        }
                        break;
                    case LINK:
                        eventStrings.add("link");
                        eventStrings.add(event.getComponentID());
                        switch((LinkState)event.getParameter()){
                            case ID:
                                eventStrings.add("id");
                                eventStrings.add(event.getValue().toString());
                                break;
                            case FROM_NODE:
                                eventStrings.add("from_node_id");
                                eventStrings.add(event.getValue().toString());
                                break;
                            case TO_NODE:
                                eventStrings.add("to_node_id");
                                eventStrings.add(event.getValue().toString());
                                break;
                            case STATUS:
                                eventStrings.add("status");
                                eventStrings.add((Boolean)event.getValue() ? "1" : "0");
                                break;
                            default:
                                logger.debug("Link state cannot be recognised");
                        }
                        break;
                    default:
                        logger.debug("Network component cannot be recognised");
                }
                break;
            case FLOW:
                eventStrings.add("flow");
                switch(event.getNetworkComponent()){
                    case NODE:
                        eventStrings.add("node");
                        eventStrings.add(event.getComponentID());
                        eventStrings.add(this.flowNetworkDataTypes.castNodeStateTypeToString(event.getParameter()));
                        eventStrings.add(event.getValue().toString());
                        break;
                    case LINK:
                        eventStrings.add("link");
                        eventStrings.add(event.getComponentID());
                        eventStrings.add(this.flowNetworkDataTypes.castLinkStateTypeToString(event.getParameter()));
                        eventStrings.add(event.getValue().toString());
                        break;
                    default:
                        logger.debug("Network component cannot be recognised");
                }
                break;
            case SYSTEM:
                eventStrings.add("system");
                eventStrings.add(missingValue);
                eventStrings.add(missingValue);
                switch((SfinaParameter)event.getParameter()){
                    case RELOAD:
                        eventStrings.add("reload");
                        eventStrings.add(event.getValue().toString());
                        break;
                    default:
                        logger.debug("System parameter cannot be recognized.");
                }
                break;
            default:
                logger.debug("Event type cannot be recognised");
        }
        
        try{
            PrintWriter writer = new PrintWriter(new FileWriter(file,true));
            for (int i=0; i<6; i++){
                writer.print(eventStrings.get(i));
                if(i==5)
                    writer.print("\n");
                else
                    writer.print(this.columnSeparator);
            }
            writer.close();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }       
    }
}
