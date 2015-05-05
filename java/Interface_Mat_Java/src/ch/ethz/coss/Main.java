package ch.ethz.coss;

import java.io.IOException;
import java.util.Arrays;

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
		/*String branch = "/Users/Ben/Documents/Studium/COSS/SFINA/matlab/sample_topology/link_info/1.txt";
		String branch_meta = "/Users/Ben/Documents/Studium/COSS/SFINA/matlab/sample_topology/link_meta_info/1.txt";
		String bus_meta = "/Users/Ben/Documents/Studium/COSS/SFINA/matlab/sample_topology/node_meta_info/1.txt";
		String save_to = "/Users/Ben/Documents/Studium/COSS/SFINA/matlab/sample_topology/matpower_format/";
		String case_name = "sample_topology";
		Convert.toMatpower(branch, branch_meta, bus_meta, save_to, case_name);
		*/
		
	    //Call Matpower flow analysis
		CallMatpower matpwr = new CallMatpower("case14");
		matpwr.run();
		double[][] bus = matpwr.get("bus");
		double[][] branch = matpwr.get("bra");
		double[][] generator = matpwr.get("gen");
		
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