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
package messages;

import protopeer.network.Message;
import protopeer.network.NetworkAddress;

/**
 * Message notifying that the network address of message sender changed.
 * @author mcb
 */
public class NetworkAddressMessage extends AbstractSfinaMessage{
    

    private NetworkAddress address;
    private boolean stopped = false;
    
    public NetworkAddressMessage(int id, NetworkAddress address){
        super(id);
        this.address = address;
    }
    
    
    public NetworkAddressMessage(int id, NetworkAddress address, boolean stopped){
        this(id, address);
        this.stopped = stopped;
    }

  

    //todo make it immutable??
    public NetworkAddress getAddress() {
        return address;
    }
   
    @Override
    public MessageType getMessageType() {
        return MessageType.NETWORK_ADDRESS_CHANGE;
    }
    
    public boolean isStopped(){
        return this.stopped;
    }
   
    
    
    
}
