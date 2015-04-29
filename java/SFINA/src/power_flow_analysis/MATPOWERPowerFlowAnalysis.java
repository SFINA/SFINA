/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package power_flow_analysis;

import flow_analysis.FlowAnalysisInterface;
import flow_analysis.FlowAnalysisOutcome;
import matlabcontrol.LoggingMatlabProxy;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

/**
 *
 * @author evangelospournaras
 */
public class MATPOWERPowerFlowAnalysis implements FlowAnalysisInterface{
    
    public MATPOWERPowerFlowAnalysis(){
        
    }
    
    @Override
    public FlowAnalysisOutcome flowAnalysis(){
        
        try{
            // Call Matlab from Java
            MatlabProxyFactory factory = new MatlabProxyFactory();
            MatlabProxy proxy = factory.getProxy();

            //Create log
            LoggingMatlabProxy log = new LoggingMatlabProxy(proxy);
            LoggingMatlabProxy.showInConsoleHandler();

            //Test with simple variable
            proxy.eval("a = 5 + 6");
            double result = ((double[]) proxy.getVariable("a"))[0];
            System.out.println("Result: " + result);

            //Call Powerflow analysis
            proxy.eval("grid_data = loadcase('case9')");
            proxy.eval("result = runpf(grid_data)");
            Object matpf_result = proxy.getVariable("result");
            //Double matpf_result = ((Double[]) proxy.getVariable("result"))[0];
            System.out.println("Return of PowerFlow Analysis:" + matpf_result);

            //Close Matlab session
            proxy.exit();
        }
        catch(MatlabConnectionException ex){
            ex.printStackTrace();
        }
        catch(MatlabInvocationException ex){
            ex.printStackTrace();
        }
	return null;	
    }
    
}
