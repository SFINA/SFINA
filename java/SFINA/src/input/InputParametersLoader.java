/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package input;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

/**
 *
 * @author evangelospournaras
 */
public class InputParametersLoader {
    
    private String parameterValueSeparator;
    private static final Logger logger = Logger.getLogger(InputParametersLoader.class);
    
    public InputParametersLoader(String parameterValueSeparator){
        this.parameterValueSeparator=parameterValueSeparator;
    }
    
    public HashMap<InputParameter,Object> loadInputParameters(String location){
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            while(scr.hasNext()){
                System.out.println("line : "+scr.next());
                StringTokenizer st = new StringTokenizer(scr.next(), parameterValueSeparator);
                while (st.hasMoreElements()) {
			System.out.println(st.nextElement());
		}
                
                
                
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public ArrayList<String> loadAttackedLines(String location){
        return null;
    }
    
    private InputParameter lookupInputParameter(String inputParameter){
        switch(inputParameter){
            case "domain":
                return InputParameter.DOMAIN;
            case "backend":
                return InputParameter.BACKEND;
            case "flow_type":
                return InputParameter.FLOW_TYPE;
            default:
                logger.debug("Input parameter is not recongised.");
                return null;
        }
    }
    
    
}
