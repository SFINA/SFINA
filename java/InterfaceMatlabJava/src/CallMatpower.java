import matlabcontrol.*;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;

public class CallMatpower {
	private double[][] matpf_bus;
	private double[][] matpf_gen;
	private double[][] matpf_bra;
	private double[][] matpf_gencost;
	private String pf_type;
	private MatlabProxyFactory factory;
	
	/**
	 * Returns bus 2-dim double array.
	 * @return double 2-dim array.
	 */
	public double[][] getBus() {
		return matpf_bus;
	}
	/**
	 * Returns Generator 2-dim double array.
	 * @return double 2-dim array.
	 */	
	public double[][] getGen() {
		return matpf_gen;
	}
	/**
	 * Returns Branch 2-dim double array.
	 * @return double 2-dim array.
	 */
	public double[][] getBranch() {
		return matpf_bra;
	}
	/**
	 * Returns Generator Cost 2-dim double array.
	 * @return double 2-dim array.
	 */
	public double[][] getGencost() {
		return matpf_gencost;
	}

	/**
	 * Constructor to create an object to perform Matpower flow analysis.
	 * @param String case_name
	 * @param String pf_type: Specify "AC" or "DC" for corresponding flow analysis.
	 * @throws MatlabConnectionException 
	 * @throws MatlabInvocationException 
	 */	
	public CallMatpower(String ac_dc) throws MatlabConnectionException, MatlabInvocationException {
		if (ac_dc.equals("AC") || ac_dc.equals("DC")) pf_type = ac_dc;
		else {System.out.println("ATTENTION: Couldn't recognize AC or DC. Check your input parameters. Program exit.");System.exit(0);}
		
		// Connect to an existing running session of Matlab if available to avoid start-up time delay
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
												.setUsePreviouslyControlledSession(true)
												.build();
		factory = new MatlabProxyFactory(options);
	}
	
	/**
	 * starts Matlab session, runs Matpower simulation given network data (in Matpower format)
	 * @param bus: Bus data as double[][]
	 * @param gen: Generator data data as double[][]
	 * @param bra: Branch data as double[][]
	 * @param gencost: Generator cost data as double[][]
	 * @throws MatlabConnectionException
	 * @throws MatlabInvocationException
	 */
	public void run(double[][] bus, double[][] gen, double[][] bra, double[][] gencost) throws MatlabConnectionException, MatlabInvocationException {
		// Initialize local variables
		matpf_bus = bus;
		matpf_gen = gen;
		matpf_bra = bra;
		matpf_gencost = gencost;
		
		// Connect to Matlab
		MatlabProxy proxy = factory.getProxy();

		// Conversion object to send and retrieve arrays to and from matlab
		MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
		
		// Send data to matlab to create matpower struct
		proxy.eval("data.version = '2'");
		proxy.eval("data.baseMVA = [100]");
		processor.setNumericArray("data.bus", new MatlabNumericArray(matpf_bus, null));
		processor.setNumericArray("data.gen", new MatlabNumericArray(matpf_gen, null));
		processor.setNumericArray("data.branch", new MatlabNumericArray(matpf_bra, null));
		processor.setNumericArray("data.gencost", new MatlabNumericArray(matpf_gencost, null));
		
		// Run AC or DC powerflow analysis
		if (pf_type.equals("AC")) proxy.eval("result = runpf(data);");
		else proxy.eval("result = rundcpf(data);");
		
		// Get results
		matpf_bra = processor.getNumericArray("result.branch").getRealArray2D();
		matpf_bus = processor.getNumericArray("result.bus").getRealArray2D();
		matpf_gen = processor.getNumericArray("result.gen").getRealArray2D();
		matpf_gencost = processor.getNumericArray("result.gencost").getRealArray2D();
		
		// Warn if there's problem with Powerflow Analysis
		double success = ((double[]) proxy.getVariable("result.success"))[0];
		if (success == 0.0) {System.out.println("ATTENTION: Problem with Powerflow. Probably didn't converge."); System.exit(0);}
		else {System.out.println("Simulation successful.");};
		
		// Disconnect proxy from Matlab session
		proxy.disconnect();
	}
	
	/**
	 * Method to close running Matlab session.
	 * @throws MatlabConnectionException
	 * @throws MatlabInvocationException
	 */
	public void closeMatlabSession() throws MatlabInvocationException {
		try {
			MatlabProxy proxy = factory.getProxy();
			proxy.exit();
			proxy.disconnect();
			System.out.println("Matlab closed.");
		}
		catch (MatlabConnectionException e) {
			System.out.println("Matlab cannot be closed if no session is open.");
		}
	}
	
	/**
	 * Method to load a Matpower case file data into java double[][] arrays, which then can be retrieved by getBus(), getGen(), getBranch() and getGencost().
	 * @param which_case: Name of case file
	 * @throws MatlabConnectionException
	 * @throws MatlabInvocationException
	 */
	public void getCaseDataFromMatpower(String which_case) throws MatlabConnectionException, MatlabInvocationException{
		MatlabProxy proxy = factory.getProxy();
		proxy.eval("load_data = loadcase('" + which_case + "');");
		
		MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
		matpf_bra = processor.getNumericArray("load_data.branch").getRealArray2D();
		matpf_bus = processor.getNumericArray("load_data.bus").getRealArray2D();
		matpf_gen = processor.getNumericArray("load_data.gen").getRealArray2D();
		matpf_gencost = processor.getNumericArray("load_data.gencost").getRealArray2D();
		proxy.eval("clear");
		proxy.disconnect();
		System.out.println("Loaded " + which_case + ".");
	}
	
	/**
	 * Execute arbitrary (valid) Matlab command
	 * @param command: String
	 * @throws MatlabConnectionException
	 * @throws MatlabInvocationException
	 */
	public void executeMatlabCommand(String command) throws MatlabConnectionException, MatlabInvocationException {
		MatlabProxy proxy = factory.getProxy();
		proxy.eval(command);
		proxy.disconnect();
	}
	
	/**
	 * Method disables branch with specific (Matpower) id 
	 * @param branch_id
	 * @throws MatlabInvocationException
	 * @throws MatlabConnectionException
	 */
	public void disableBranch(int branch_id) throws MatlabInvocationException, MatlabConnectionException {
		MatlabProxy proxy = factory.getProxy();
		proxy.eval("data.branch(" + branch_id + ",11) = 0;");
		proxy.disconnect();
	}
	
	/**
	 * Finds out islands of current network. This is experimental, so far the indices of just one island are returned. 
	 * @return double[]
	 * @throws MatlabConnectionException
	 * @throws MatlabInvocationException
	 */
	public double[] findIslands() throws MatlabConnectionException, MatlabInvocationException{
		MatlabProxy proxy = factory.getProxy();
		MatlabTypeConverter processor = new MatlabTypeConverter(proxy);		
		proxy.eval("[groups,isolated] = find_islands(data);");
		double[] groups = processor.getNumericArray("groups{1,1}").getRealArray2D()[0];
		proxy.disconnect();
		return groups;
	}
	
	/**
	 * Returns double array of isolated buses.
	 * @return double[]
	 * @throws MatlabConnectionException
	 * @throws MatlabInvocationException
	 */
	public double[] findIsolated() throws MatlabConnectionException, MatlabInvocationException {
		MatlabProxy proxy = factory.getProxy();
		MatlabTypeConverter processor = new MatlabTypeConverter(proxy);		
		proxy.eval("[groups,isolated] = find_islands(data);");
		double[] isolated = processor.getNumericArray("isolated").getRealArray2D()[0];
		proxy.disconnect();
		return isolated;		
	}
}