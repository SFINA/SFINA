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
package event;

import dsutil.generic.state.State;

/**
 *
 * @author evangelospournaras
 */
public class Event extends State{
    
    private int time;
    private EventType eventType;
    private NetworkComponent networkComponent;
    private String componentID;
    private Enum parameter;
    private Object value;
    
    public Event(int time, EventType eventType, NetworkComponent networkComponent, String componentID, Enum parameter, Object value){
        super();
        this.time=time;
        this.eventType=eventType;
        this.networkComponent=networkComponent;
        this.componentID=componentID;
        this.parameter=parameter;
        this.value=value;
    }

    /**
     * @return the time
     */
    public int getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(int time) {
        this.time = time;
    }

    /**
     * @return the networkFeature
     */
    public EventType getEventType() {
        return eventType;
    }

    /**
     * @param networkFeature the networkFeature to set
     */
    public void setNetworkFeature(EventType networkFeature) {
        this.eventType = networkFeature;
    }

    /**
     * @return the networkComponent
     */
    public NetworkComponent getNetworkComponent() {
        return networkComponent;
    }

    /**
     * @param networkComponent the networkComponent to set
     */
    public void setNetworkComponent(NetworkComponent networkComponent) {
        this.networkComponent = networkComponent;
    }

    /**
     * @return the componentID
     */
    public String getComponentID() {
        return componentID;
    }

    /**
     * @param componentID the componentID to set
     */
    public void setComponentID(String componentID) {
        this.componentID = componentID;
    }

    /**
     * @return the parameter
     */
    public Enum getParameter() {
        return parameter;
    }

    /**
     * @param parameter the parameter to set
     */
    public void setParameter(Enum parameter) {
        this.parameter = parameter;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Object value) {
        this.value = value;
    }
    
}
