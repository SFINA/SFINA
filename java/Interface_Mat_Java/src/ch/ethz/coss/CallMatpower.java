package ch.ethz.coss;

import matlabcontrol.*;
import matlabcontrol.extensions.MatlabTypeConverter;

public class CallMatpower {
	private double[][] matpf_bra;
	private double[][] matpf_bus;
	private double[][] matpf_gen;
	private String case_name;
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
	
	public CallMatpower() {
		case_name = "case9";
		System.out.println("Attention: Because Matpower case file was not specified, it was automatically set to case9.");
	}
	
	public CallMatpower(String new_case) {
		case_name = new_case;
	}
	
	/**
	 * starts Matlab session, runs Matpower simulation for given case
	 * @throws MatlabConnectionException
	 * @throws MatlabInvocationException
	 */
	public void run() throws MatlabConnectionException, MatlabInvocationException {
		// Call Matlab from Java
		MatlabProxyFactory factory = new MatlabProxyFactory();
		MatlabProxy proxy = factory.getProxy();
	    
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
		
		//Close Matlab session
		proxy.exit();
	}
}