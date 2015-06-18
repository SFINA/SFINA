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
package input;

import event.EventState;
import event.NetworkComponent;
import event.NetworkFeature;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import power.input.PowerLinkState;

/**
 *
 * @author evangelospournaras
 */
public class EventLoader {
    
    private String columnSeparator;
    private static final Logger logger = Logger.getLogger(EventLoader.class);
    
    public EventLoader(String columnSeparator){
        this.columnSeparator=columnSeparator;
    }
    
    public void loadEvents(String location){
        ArrayList<EventState> eventStates=new ArrayList<EventState>();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            if(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), columnSeparator);
                while(st.hasMoreTokens()){
                    String eventStateName = st.nextToken();
                    EventState eventState = this.lookupEventState(eventStateName);
                    eventStates.add(eventState);
                }
            }
            while(scr.hasNext()){
                ArrayList<String> values = new ArrayList<String>();
                StringTokenizer st = new StringTokenizer(scr.next(), columnSeparator);
                while (st.hasMoreTokens()) {
                    values.add(st.nextToken());
		}
                
            }
            
            
            
            
            while(scr.hasNext()){
                String line=scr.next();
                
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    
    private EventState lookupEventState(String eventState){
        switch(eventState){
            case "time": 
                return EventState.ID;
            case "feature":
                return EventState.NETWORK_FEATURE;
            case "component":
                return EventState.NETWORK_COMPONENT;
            case "id":
                return EventState.ID;
            case "parameter":
                return EventState.PARAMETER;
            case "value":
                return EventState.VALUE;
            default:
                logger.debug("Event state is not recognized.");
                return null;
        }
    }
    
    private Object getActualEventProperty(EventState eventState, String rawProperty){
        switch(eventState){
            case TIME:
                return Integer.parseInt(rawProperty);
            case NETWORK_FEATURE:
                switch(rawProperty){
                    case "topology":
                        return NetworkFeature.TOPOLOGY;
                    case "flow":
                        return NetworkFeature.FLOW;
                    default:
                        logger.debug("Network feature cannot be recognized.");
                        return null;
                }
            case NETWORK_COMPONENT:
                switch(rawProperty){
                    case "node":
                        return NetworkComponent.Node;
                    case "link":
                        return NetworkComponent.Link;
                    default:
                        logger.debug("Network component cannot be recognized.");
                        return null;
                }
//            case ID:
//                return rawProperty;
//            case PARAMETER:
//                return EventState.PARAMETER;
//            case "value":
//                return EventState.VALUE;
            default:
                logger.debug("Event state is not recognized.");
                return null;
        }
    }
    
    
}
