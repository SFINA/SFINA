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

/**
 * Message notifying that message sender finished current time step or iteration
 * @author mcb
 */
public class FinishedActiveStateMessage extends AbstractSfinaMessage{
    private final int time;
    private final int iteration;
    private final boolean converged;

    public FinishedActiveStateMessage(int networkIdentifier, int time, int iteration, boolean converged) {
        super(networkIdentifier);
        this.time = time;
        this.iteration = iteration;
        this.converged = converged;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FINISHED_ACTIVE_STATE;
    }

    /**
     * @return the time
     */
    public int getTime() {
        return time;
    }

    /**
     * @return the iteration
     */
    public int getIteration() {
        return iteration;
    }

    /**
     * @return the converged
     */
    public boolean isConverged() {
        return converged;
    }
     
   
}
