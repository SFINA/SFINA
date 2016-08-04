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
package input;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import power.backend.PowerBackend;

/**
 *
 * @author Ben
 */
public class SfinaParameterLoader {
    
    private String columnSeparator;
    private static final Logger logger = Logger.getLogger(SfinaParameterLoader.class);
    
    public SfinaParameterLoader(String columnSeparator){
        this.columnSeparator=columnSeparator;
    }
    
    public HashMap<SfinaParameter,Object> loadSfinaParameters(String location){
        HashMap<SfinaParameter,Object> systemParameters = new HashMap();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            while(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), columnSeparator);
                switch(st.nextToken()){
                    case "domain":
                        Domain domain=null;
                        String domainType=st.nextToken();
                        switch(domainType){
                            case "power":
                                domain=Domain.POWER;
                                break;
                            case "gas":
                                domain=Domain.GAS;
                                break;
                            case "water":
                                domain=Domain.WATER;
                                break;
                            case "transportation":
                                domain=Domain.TRANSPORTATION;
                                break;
                            case "disaster_spread":
                                domain=Domain.DISASTERSPREAD;
                                break;
                            default:
                                logger.debug("This domain is not supported or cannot be recognized");
                        }
                        systemParameters.put(SfinaParameter.DOMAIN, domain);
                        break;
                    case "backend":
                        PowerBackend backend=null;
                        String backendType=st.nextToken();
                        switch(backendType){
                            case "matpower":
                                backend=PowerBackend.MATPOWER;
                                break;
                            case "interpss":
                                backend=PowerBackend.INTERPSS;
                                break;
                            case "helbingetal":
                                backend=PowerBackend.HELBINGETAL;
                                break;
                            default:
                                logger.debug("This backend is not supported or cannot be recognized");
                        }
                        systemParameters.put(SfinaParameter.BACKEND, backend);
                        break;
                    default:
                        logger.debug("This system parameter is not supported or cannot be recognized");
                }
            }
        }
        catch (FileNotFoundException ex){
            ex.printStackTrace();
        }
        return systemParameters;
    }
}
