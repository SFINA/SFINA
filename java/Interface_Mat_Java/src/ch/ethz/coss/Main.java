package ch.ethz.coss;

import java.io.IOException;

import matlabcontrol.*;

import com.interpss.common.exp.InterpssException;

public class Main {
	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException, InterpssException, IOException {
		
		// Call InterPSS simulation on case IEEE009
		/*String path = "./Data/ieee/IEEE14Bus.dat";
		nterPSS_loadflow lf = new InterPSS_loadflow(path, "AC");
		String result = lf.runlf();
		System.out.println(result);*/	
		
		// Test conversion script with sample topology
		String branch = "/Users/Ben/Documents/Studium/COSS/SFINA/matlab/sample_topology/branch_info/1.txt";
		String branch_meta = "/Users/Ben/Documents/Studium/COSS/SFINA/matlab/sample_topology/branch_meta_info/1.txt";
		String bus_meta = "/Users/Ben/Documents/Studium/COSS/SFINA/matlab/sample_topology/bus_meta_info/1.txt";
		String save_to = "/Users/Ben/Documents/Studium/COSS/SFINA/matlab/sample_topology/matpower_format/";
		String case_name = "sample_topology";
		Convert.toMatpower(branch, branch_meta, bus_meta, save_to, case_name);
		
		/*
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
		proxy.exit();;
		*/
	}
}