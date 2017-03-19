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
package interdependent.Messages;

import interdependent.communication.CommunicationType;
import protopeer.network.Message;

/**
 * Base Class for a default SFINA message. 
 * @author mcb
 */
public abstract class AbstractSfinaMessage extends Message implements SfinaMessageInterface{
    
    private int networkIdentifier; // network where message originates from

    /**
     * Constructor
     * @param networkIdentifier : Network where messgae originates from
     */
    public AbstractSfinaMessage(int networkIdentifier){
        this.networkIdentifier = networkIdentifier;
    }

    @Override
    public int getNetworkIdentifier() {
        return networkIdentifier;
    }
    
    @Override
    public abstract CommunicationType getMessageType();
    
    
}
