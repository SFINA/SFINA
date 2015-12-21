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
package applications;

import java.util.HashMap;
import network.FlowNetwork;
import org.apache.log4j.Logger;
import protopeer.util.quantities.Time;

/**
 * Overrides mitigateOverload method. Here, mitigation strategies can be implemented, which will be called before the CascadeAgent calls the linkOverload check.
 * @author Ben
 */
public class PowerMitigationAgent extends BenchmarkDomainAgent{
    
    public PowerMitigationAgent(String experimentID, 
            String peersLogDirectory, 
            Time bootstrapTime, 
            Time runTime, 
            String timeTokenName, 
            String experimentConfigurationFilesLocation, 
            String experimentOutputFilesLocation,
            String nodesLocation, 
            String linksLocation, 
            String nodesFlowLocation, 
            String linksFlowLocation, 
            String eventsLocation, 
            String columnSeparator, 
            String missingValue,
            HashMap systemParameters){
        super(experimentID,
                peersLogDirectory,
                bootstrapTime,
                runTime,
                timeTokenName,
                experimentConfigurationFilesLocation,
                experimentOutputFilesLocation,
                nodesLocation,
                linksLocation,
                nodesFlowLocation,
                linksFlowLocation,
                eventsLocation,
                columnSeparator,
                missingValue,
                systemParameters);
    }
    
    private static final Logger logger = Logger.getLogger(PowerMitigationAgent.class);
    
    @Override
    public void mitigateOverload(FlowNetwork flowNetwork){
        switch(getDomain()){
            case POWER:
                
                // ---- implement mitigation strategy here ---- //
                
                break;
            case GAS:
                logger.debug("This domain is not supported at this moment");
                break;
            case WATER:
                logger.debug("This domain is not supported at this moment");
                break;
            case TRANSPORTATION:
                logger.debug("This domain is not supported at this moment");
                break;
            default:
                logger.debug("This domain is not supported at this moment");
        }
    }
}
