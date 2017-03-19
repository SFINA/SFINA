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
package power.input;

import agents.backend.BackendParameterLoaderInterface;
import input.SfinaParameterLoader;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import power.backend.PowerFlowType;
import power.backend.PowerBackendParameter;

/**
 *
 * @author Ben
 */
public class PowerBackendParameterLoader implements BackendParameterLoaderInterface{
    
    private String columnSeparator;
    private static final Logger logger = Logger.getLogger(PowerBackendParameterLoader.class);
    
    public PowerBackendParameterLoader(String columnSeparator){
        this.columnSeparator=columnSeparator;
    }
    
    @Override
    public HashMap<Enum,Object> loadBackendParameters(String location){
        HashMap<Enum,Object> backendParameters = new HashMap();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            while(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), columnSeparator);
                Enum param = null;
                Object value = null;

                switch(st.nextToken()){
                    case "flowType":
                        param = PowerBackendParameter.FLOW_TYPE;
                        switch(st.nextToken()){
                            case "AC":
                                value = PowerFlowType.AC;
                                break;
                            case "DC":
                                value = PowerFlowType.DC;
                                break;
                            default:
                                logger.debug("FlowType cannot be recognized");
                        }
                        break;
                    case "toleranceParameter":
                        param = PowerBackendParameter.TOLERANCE_PARAMETER;
                        value = Double.parseDouble(st.nextToken());
                        break;
                    default:
                        logger.debug("This backend parameter is not supported or cannot be recognized");
                }
                backendParameters.put(param, value);
            }
        }
        catch (FileNotFoundException ex){
            ex.printStackTrace();
        }
        return backendParameters;
    }
}
