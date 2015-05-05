/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package power.input;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import network.Node;
import org.apache.log4j.Logger;

/**
 *
 * @author evangelospournaras
 */
public class PowerMetaInfoLoader {
    
    private final String parameterValueSeparator;
    private static final Logger logger = Logger.getLogger(PowerMetaInfoLoader.class);
    
    public PowerMetaInfoLoader(String parameterValueSeparator){
        this.parameterValueSeparator=parameterValueSeparator;
    }
    
    private void loadNodeMetaInfo(String location, List<Node> nodes){
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            if(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), parameterValueSeparator);
                List<PowerNodeState> powerNodeStates=new ArrayList<PowerNodeState>();
                while(st.hasMoreTokens()){
                    String metaInfo=st.nextToken();
                }
            }
            while(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), parameterValueSeparator);
                while (st.hasMoreTokens()) {
                    
		}
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        
    }
    
    private PowerNodeState lookupPowerNodeState(String powerNodeState){
        switch(powerNodeState){
            case "type":
                return PowerNodeState.TYPE;
            default:
                logger.debug("Power node state is not recongised.");
                return null;
        }
    }
    
    
    
    
    
    
}
