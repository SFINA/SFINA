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
		CallMatpower matpwr = new CallMatpower("createCase14", "AC");
		
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
		
		// Print out results
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
		}
	}
}