/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package input;

import power.PowerFlowType;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import power.PowerFlowAnalysisType;

/**
 *
 * @author evangelospournaras
 */
public class InputParametersLoader {
    
    private final String parameterValueSeparator;
    private static final Logger logger = Logger.getLogger(InputParametersLoader.class);
    
    public InputParametersLoader(String parameterValueSeparator){
        this.parameterValueSeparator=parameterValueSeparator;
    }
    
    public HashMap<InputParameter,Object> loadInputParameters(String location){
        HashMap<InputParameter,Object> inputParameters=new HashMap<InputParameter,Object>();
        File file = new File(location);
        Scanner scr = null;
        try {
            scr = new Scanner(file);
            while(scr.hasNext()){
                StringTokenizer st = new StringTokenizer(scr.next(), parameterValueSeparator);
                while (st.hasMoreTokens()) {
                    InputParameter inputParameter=lookupInputParameter(st.nextToken());
                    Object value=this.getObjectFromString(inputParameter, st.nextToken());
                    inputParameters.put(inputParameter, value);
		}
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return inputParameters;
    }
    
    private InputParameter lookupInputParameter(String inputParameter){
        switch(inputParameter){
            case "domain":
                return InputParameter.DOMAIN;
            case "backend":
                return InputParameter.BACKEND;
            case "flow_type":
                return InputParameter.FLOW_TYPE;
            case "flow_analysis_type":
                return InputParameter.FLOW_ANALYSIS_TYPE;
            case "tolerance":
                return InputParameter.TOLERANCE_PARAMETER;
            case "attack_strategy":
                return InputParameter.ATTACK_STRATEGY;
            case "time_steps":
                return InputParameter.TIME_STEPS;
            default:
                logger.debug("Input parameter is not recongised.");
                return null;
        }
    }
    
    private Object getObjectFromString(InputParameter inputParameter, String stringValue){
        switch(inputParameter){
            case DOMAIN:
                switch(stringValue){
                    case "power":
                        return Domain.POWER;
                    case "gas":
                        return Domain.GAS;
                    case "water":
                        return Domain.WATER;
                    case "transportation":
                        return Domain.TRANSPORTATION;
                    default:
                        logger.debug("Domain input parameter is not recongised.");
                        return null;
                }
            case BACKEND:
                switch(stringValue){
                    case "matpower":
                        return Backend.MATPOWER;
                    case "interpss":
                        return Backend.INTERPSS;
                    default:
                        logger.debug("Backend input parameter is not recongised.");
                        return null;
                }
            case FLOW_TYPE:
                switch(stringValue){
                    case "dc":
                        return PowerFlowType.DC;
                    case "ac":
                        return PowerFlowType.AC;
                    default:
                        logger.debug("Flow type input parameter is not recongised.");
                        return null;
                }
            case FLOW_ANALYSIS_TYPE:
                switch(stringValue){
                    case "current":
                        return PowerFlowAnalysisType.CURRENT;
                    case "power":
                        return PowerFlowAnalysisType.POWER;
                    case "voltage":
                        return PowerFlowAnalysisType.VOLTAGE;
                    default:
                        logger.debug("Flow analysis type input parameter is not recongised.");
                        return null;
                }
            case TOLERANCE_PARAMETER:
                return Double.parseDouble(stringValue);
            case ATTACK_STRATEGY:
                switch(stringValue){
                    case "sequential":
                        return AttackStrategy.SEQUENTIAL;
                    case "simultaneous":
                        return AttackStrategy.SIMULTANEOUS;
                    default:
                        logger.debug("Attack strategy input parameter is not recongised.");
                        return null;
                }
            case TIME_STEPS:
                return Double.parseDouble(stringValue);
            default:
                logger.debug("Input parameter is not recongised.");
                return null;
        }
    }
    
    
}
