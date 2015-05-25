import java.io.IOException;
import java.util.Arrays;

import org.interpss.numeric.exp.IpssNumericException;

import matlabcontrol.*;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.dclf.common.ReferenceBusException;

public class Main {
	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException, InterpssException, IOException, ReferenceBusException, IpssNumericException {		
		System.out.println("______InterPSS DC on Case14___________________________________________________________________");
		testInterPSSloadflow();
		
		System.out.println("______Matpower DC on Case14___________________________________________________________________");
		testMatpowerloadflow();
		
		//testConversion();
	}
	
	private static void testInterPSSloadflow() throws ReferenceBusException, IpssNumericException{
		// Call InterPSS simulation on case IEEE009
		String path = "./Data/ieee/IEEE14Bus.dat";
		InterPSS_loadflow lf = new InterPSS_loadflow(path, "DC");
		String result = lf.runlf();
		System.out.println(result);	
		return;
	}
	
	private static void testMatpowerloadflow() throws MatlabConnectionException, MatlabInvocationException{
		// Use matpower flow analysis with network data in double[][] arrays
		CallMatpower matpwr = new CallMatpower("DC");
		
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
		}
		
		/*
		// For island function: Remove some lines
		matpwr.disableBranch(8);
		matpwr.disableBranch(9);
		matpwr.disableBranch(10);
		matpwr.disableBranch(14);
		matpwr.disableBranch(15);

		// Try island function and plot results
		double[] islands = matpwr.findIslands();
		double[] iso = matpwr.findIsolated();
		String group_str ="";
		System.out.println("\n");
		for(int i = 0; i < islands.length; i++)
		{
			group_str += islands[i] + "	";
		}
		System.out.println("Bus ids in one island: " + group_str);
		for(int i = 0; i < iso.length; i++)
		{
		    System.out.println("Isolated bus: " + iso[i]);
		}
		*/
		
		// Close Matlab session
		matpwr.closeMatlabSession();
		return;
	}
	
	private static void testConversion() throws IOException{
		// Test conversion script with sample topology
		String branch = "/Users/Ben/Documents/Studium/COSS/SFINA/matlab/sample_topology/link_info/1.txt";
		String branch_meta = "/Users/Ben/Documents/Studium/COSS/SFINA/matlab/sample_topology/link_meta_info/1.txt";
		String bus_meta = "/Users/Ben/Documents/Studium/COSS/SFINA/matlab/sample_topology/node_meta_info/1.txt";
		String save_to = "/Users/Ben/Documents/Studium/COSS/SFINA/matlab/sample_topology/matpower_format/";
		String case_name = "sample_topology";
		Convert.toMatpower(branch, branch_meta, bus_meta, save_to, case_name);
		return;
	}
}