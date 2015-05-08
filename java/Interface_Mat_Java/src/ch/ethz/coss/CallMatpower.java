package ch.ethz.coss;

import java.util.Arrays;

import matlabcontrol.*;
import matlabcontrol.extensions.MatlabTypeConverter;

public class CallMatpower {
	private double[][] matpf_bra;
	private double[][] matpf_bus;
	private double[][] matpf_gen;
	private String case_name;
	private String pf_type;
	private boolean close_matlab;
	private MatlabProxyFactory factory;
	/**
	 * Returns bus, generator or branch 2-dim double array depending on input parameter.
	 * @param what: "bra" for branch, "bus" for bus, "gen" for generator (String).
	 * @return double 2-dim array.
	 */
	public double[][] get(String what) {
		if (what.equals("bra")) return matpf_bra;
		else if (what.equals("bus")) return matpf_bus;
		else if (what.equals("gen")) return matpf_gen;
		else {
			System.out.println("You didn't give a valid input parameter.");
			return null;
		}
	}
	
	/**
	 * Constructor to create an object with method run(int[] failed_branches) to perform Matpower flow analysis.
	 * Parameter close_matlab is optional. False by default and the Matlab session will not be closed at the end.
	 * @param String case_name
	 * @param String pf_type: Specify "AC" or "DC" for corresponding flow analysis.
	 * @param Boolean close_matlab: Optional parameter. If true Matlab session will be closed after the simulation. False if not specified.
	 */
	public CallMatpower() {
		System.out.println("ATTENTION: Please specify a Matpower case. Program exit.");
		System.exit(0);
	}
	
	/**
	 * Constructor to create an object with method run(int[] failed_branches) to perform Matpower flow analysis.
	 * Parameter close_matlab is optional. False by default and the Matlab session will not be closed at the end.
	 * @param String case_name
	 * @param String pf_type: Specify "AC" or "DC" for corresponding flow analysis.
	 * @param Boolean close_matlab: Optional parameter. If true Matlab session will be closed after the simulation. False if not specified.
	 */	
	public CallMatpower(String new_case, String ac_dc) {
		case_name = new_case;
		if (ac_dc.equals("AC") || ac_dc.equals("DC")) pf_type = ac_dc;
		else {System.out.println("ATTENTION: Couldn't recognize AC or DC. Check your input parameters. Program exit.");System.exit(0);}
		close_matlab = false;
		// Connect to an existing running session of Matlab if available to avoid start-up time delay
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
												.setUsePreviouslyControlledSession(true)
												.build();
		factory = new MatlabProxyFactory(options);
	}
	
	/**
	 * Constructor to create an object with method run(int[] failed_branches) to perform Matpower flow analysis.
	 * Parameter close_matlab is optional. False by default and the Matlab session will not be closed at the end.
	 * @param String case_name
	 * @param String pf_type: Specify "AC" or "DC" for corresponding flow analysis.
	 * @param Boolean close_matlab: Optional parameter. If true Matlab session will be closed after the simulation. False if not specified.
	 */
	public CallMatpower(String new_case, String ac_dc, boolean close) {
		case_name = new_case;
		pf_type = ac_dc;
		close_matlab = close;
		// Connect to an existing running session of Matlab if available to avoid start-up time delay
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
												.setUsePreviouslyControlledSession(true)
												.build();
		factory = new MatlabProxyFactory(options);
	}
	
	/**
	 * starts Matlab session, runs Matpower simulation for given case
	 * @param failed_bra integer array with id's of failed branches
	 * @throws MatlabConnectionException
	 * @throws MatlabInvocationException
	 */
	public void run(int[] failed_bra) throws MatlabConnectionException, MatlabInvocationException {
		MatlabProxy proxy = factory.getProxy();
		if (proxy.isExistingSession()) {
			System.out.println("Connected to running Matlab session.");
		}
		else System.out.println("New Matlab session opened.");
		//Create log
		//LoggingMatlabProxy log = new LoggingMatlabProxy(proxy);
	    //LoggingMatlabProxy.showInConsoleHandler();
		proxy.eval("grid_data = loadcase('" + case_name + "');");
		for (int branch : failed_bra) {
			proxy.eval("grid_data.branch(" + Integer.toString(branch) + ",11)=0;");
		}
		System.out.println("Disabled branches: " + Arrays.toString(failed_bra));
		//Run AC or DC powerflow analysis
		if (pf_type.equals("AC")) proxy.eval("result = runpf(grid_data);");
		else proxy.eval("result = rundcpf(grid_data);");
		proxy.eval("bra = result.branch;");
		proxy.eval("bus = result.bus;");
		proxy.eval("gen = result.gen;");
		proxy.eval("success = result.success;");
		MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
		matpf_bra = processor.getNumericArray("bra").getRealArray2D();
		matpf_bus = processor.getNumericArray("bus").getRealArray2D();
		matpf_gen = processor.getNumericArray("gen").getRealArray2D();
		double success = ((double[]) proxy.getVariable("success"))[0];
		if (success == 0.0) {System.out.println("ATTENTION: Problem with Powerflow. Probably didn't converge."); System.exit(0);};
		
		//Close Matlab session if parameter close_matlab = true
		if (close_matlab == true) {
			proxy.exit();
			System.out.println("Matlab session was terminated.");
		}
		
		//Disconnect proxy from Matlab session
		proxy.disconnect();
	}
}