package ch.ethz.coss;

import java.io.IOException;
import java.util.Arrays;

import matlabcontrol.*;

import com.interpss.common.exp.InterpssException;

public class Main {
	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException, InterpssException, IOException {
		
		// Call InterPSS simulation on case IEEE009
		/*String path = "./Data/ieee/IEEE14Bus.dat";
		InterPSS_loadflow lf = new InterPSS_loadflow(path, "AC");
		String result = lf.runlf();
		System.out.println(result);	
		*/
		
		// Test conversion script with sample topology
		/*String branch = "/Users/Ben/Documents/Studium/COSS/SFINA/matlab/sample_topology/link_info/1.txt";
		String branch_meta = "/Users/Ben/Documents/Studium/COSS/SFINA/matlab/sample_topology/link_meta_info/1.txt";
		String bus_meta = "/Users/Ben/Documents/Studium/COSS/SFINA/matlab/sample_topology/node_meta_info/1.txt";
		String save_to = "/Users/Ben/Documents/Studium/COSS/SFINA/matlab/sample_topology/matpower_format/";
		String case_name = "sample_topology";
		Convert.toMatpower(branch, branch_meta, bus_meta, save_to, case_name);
		*/
		
		
		// Use matpower flow analysis with network data in double[][] arrays
		String case_name = "createCase14";
		CallMatpower matpwr = new CallMatpower(case_name, "AC");
		
		// Load case14 from Matpower into java object to use it for the simulation
		matpwr.getCaseDataFromMatpower("case14");
		double[][] bus = matpwr.getBus();
		double[][] generator = matpwr.getGen();
		double[][] branch = matpwr.getBranch() ;
		double[][] gencost = matpwr.getGencost();
		
		// Run powerflow and get back the data
		matpwr.run(bus,generator,branch,gencost);
		bus = matpwr.getBus();
		generator = matpwr.getGen();
		branch = matpwr.getBranch();
		gencost = matpwr.getGencost();
		
		// For trying island function: Remove some lines
		matpwr.executeMatlabCommand(case_name + ".branch(8,11) = 0;");
		matpwr.executeMatlabCommand(case_name + ".branch(9,11) = 0;");
		matpwr.executeMatlabCommand(case_name + ".branch(10,11) = 0;");
		matpwr.executeMatlabCommand(case_name + ".branch(14,11) = 0;");
		matpwr.executeMatlabCommand(case_name + ".branch(15,11) = 0;");
		double[] groups = matpwr.findIslands();
		double[] iso = matpwr.findIsolated();
		String group_str ="";
		for(int i = 0; i < groups.length; i++)
		{
			group_str += groups[i] + "	";
		}
		System.out.println("Bus ids in one island: " + group_str);
		for(int i = 0; i < iso.length; i++)
		{
		    System.out.println("Isolated bus: " + iso[i]);
		}
		
		/*
		// Print powerflow simulation results
		System.out.println("Bus data:");
		for(int i = 0; i < bus.length; i++)
		{
		    System.out.println(Arrays.toString(bus[i]));
		}
		
		System.out.println("\nGenerator data:");
		for(int i = 0; i < generator.length; i++)
		{
		    System.out.println(Arrays.toString(generator[i]));
		}
		
		System.out.println("\nBranch data:");
		for(int i = 0; i < branch.length; i++)
		{
		    System.out.println(Arrays.toString(branch[i]));
		}*/
	}
}