package ch.ethz.coss;

import matlabcontrol.*;
import matlabcontrol.extensions.MatlabTypeConverter;

public class CallMatpower {
	private double[][] matpf_bra;
	private double[][] matpf_bus;
	private double[][] matpf_gen;
	private String case_name;
	private boolean close_matlab;
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
	 * Constructor to create an object with method run() to perform Matpower flow analysis.
	 * If no parameters are specified, the case is set to case9 and the Matlab session will not be closed at the end.
	 * @param String case_name
	 * @param Boolean close_matlab: Optional parameter. If true Matlab session will be closed after the simulation. False if not specified.
	 */
	public CallMatpower() {
		case_name = "case9";
		close_matlab = false;
		System.out.println("Attention: Because Matpower case file was not specified, it was automatically set to case9.");
	}
	
	/**
	 * Constructor to create an object with method run() to perform Matpower flow analysis.
	 * If no parameters are specified, the case is set to case9 and the Matlab session will not be closed at the end.
	 * @param String case_name
	 * @param Boolean close_matlab: Optional parameter. If true Matlab session will be closed after the simulation. False if not specified.
	 */	
	public CallMatpower(String new_case) {
		case_name = new_case;
		close_matlab = false;
	}
	
	/**
	 * Constructor to create an object with method run() to perform Matpower flow analysis.
	 * If no parameters are specified, the case is set to case9 and the Matlab session will not be closed at the end.
	 * @param String case_name
	 * @param Boolean close_matlab: Optional parameter. If true Matlab session will be closed after the simulation. False if not specified.
	 */
	public CallMatpower(String new_case, boolean close) {
		case_name = new_case;
		close_matlab = close;
	}
	
	/**
	 * starts Matlab session, runs Matpower simulation for given case
	 * @throws MatlabConnectionException
	 * @throws MatlabInvocationException
	 */
	public void run() throws MatlabConnectionException, MatlabInvocationException {
		// Connect to an existing running session of Matlab if available to avoid start-up time delay
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
												.setUsePreviouslyControlledSession(true)
												.build();
		MatlabProxyFactory factory = new MatlabProxyFactory(options);
		MatlabProxy proxy = factory.getProxy();
		if (proxy.isExistingSession()) {
			System.out.println("Connected to running Matlab session.");
			proxy.eval("clear");
		}
		else System.out.println("New Matlab session opened.");
	    
	    //Test with simple variable
	    /*proxy.eval("a = 5 + 6");
	    double result = ((double[]) proxy.getVariable("a"))[0];
	    //System.out.println("Result: " + result);
	    */
		
		//Create log
		//LoggingMatlabProxy log = new LoggingMatlabProxy(proxy);
	    //LoggingMatlabProxy.showInConsoleHandler();
		proxy.eval("grid_data = loadcase('" + case_name + "')");
		proxy.eval("result = runpf(grid_data)");
		proxy.eval("bra = result.branch");
		proxy.eval("bus = result.bus");
		proxy.eval("gen = result.gen");
		MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
		matpf_bra = processor.getNumericArray("bra").getRealArray2D();
		matpf_bus = processor.getNumericArray("bus").getRealArray2D();
		matpf_gen = processor.getNumericArray("gen").getRealArray2D();
		
		//Close Matlab session if parameter close_matlab = true
		if (close_matlab == true) {
			proxy.exit();
			System.out.println("Matlab session was terminated.");
		}
		
		//Disconnect proxy from Matlab session
		proxy.disconnect();
	}
}