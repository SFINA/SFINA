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
package interdependent.communication.Messages;

import protopeer.network.Message;
import protopeer.network.NetworkAddress;

/**
 *
 * @author mcb
 */
public class NetworkAddressChangeMessage extends AbstractSfinaMessage{
    

    private NetworkAddress address;
    private boolean added;
    
    public NetworkAddressChangeMessage(int id, NetworkAddress changedAddress, boolean added){
        super(id);
        this.address = address;
        this.added = added;
    }

  

    //todo make it immutable??
    public NetworkAddress getAddress() {
        return address;
    }
    
    public boolean getAdded(){
        return added;
    } 

    @Override
    public String getMessageType() {
        return SfinaMessage.NETWORK_ADDRES_CHANGE;
    }
    
    
}
