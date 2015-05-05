package ch.ethz.coss;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * 
 * @author Ben
 *
 */
public class Convert {
	private static ArrayList<ArrayList<String>> bus_data = new ArrayList<ArrayList<String>>();
	private static ArrayList<ArrayList<String>> bra_data = new ArrayList<ArrayList<String>>();
	private static ArrayList<ArrayList<String>> bra_meta_data = new ArrayList<ArrayList<String>>();
	private static String bus_str = "";
	private static String gen_str = "";
	private static String bra_str = "";
	private static String gencost_str = "";
	
	/**
	 * Runs the conversion from SFINA custom .txt to Matpower .m format. 
	 * @param branch_path		path to the branch_info.txt file
	 * @param branch_meta_path	path to the branch_meta_info.txt file
	 * @param bus_meta_path		path to the bus_meta_info.txt file
	 * @param save_to_path		path to folder where the output .m file should be saved
	 * @param case_name			file will be named case_name.m
	 * @throws IOException 
	 */
	public static void toMatpower(String branch_path, String branch_meta_path, String bus_meta_path, String save_to_path, String case_name) throws IOException {
		Charset encoding = StandardCharsets.US_ASCII;
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(save_to_path, case_name.toString() + ".m"), encoding);
		BufferedReader read_bus = Files.newBufferedReader(Paths.get(bus_meta_path), encoding);
		BufferedReader read_branch = Files.newBufferedReader(Paths.get(branch_path), encoding);
		BufferedReader read_branch_meta = Files.newBufferedReader(Paths.get(branch_meta_path), encoding);
		
		extractData(read_bus, read_branch, read_branch_meta);
		makeMatStrings();
		writeToMatFile(writer, case_name);
		
		writer.close();
		read_bus.close();
		read_branch.close();
		System.out.println("Conversion successful.");
	}
	
	private static void extractData(BufferedReader read_bus, BufferedReader read_branch, BufferedReader read_branch_meta) throws IOException {
		String line_bus = null;
		String line_bra = null;
		String line_bra_m = null;
		int line_counter = 0;
		
		while ((line_bus = read_bus.readLine()) != null) {
			String[] split = line_bus.split(",");
			bus_data.add(new ArrayList<String>());
			for (int i = 0; i < split.length; i++) { 
				bus_data.get(line_counter).add(split[i]);
			}
			line_counter++;
		}
		
		line_counter = 0;
		while ((line_bra = read_branch.readLine()) != null && (line_bra_m = read_branch_meta.readLine()) != null ) {
			String[] split_bra = line_bra.split(",");
			String[] split_bra_m = line_bra_m.split(",");
			bra_data.add(new ArrayList<String>());
			bra_meta_data.add(new ArrayList<String>());
			for (int i = 0; i < split_bra.length; i++){
				bra_data.get(line_counter).add(split_bra[i]);
			}
			for (int i = 0; i < split_bra_m.length; i++){
				bra_meta_data.get(line_counter).add(split_bra_m[i]);
			}
			line_counter++;
		}
	}
	
	private static void makeMatStrings() {
		// add bus values to bus string
		for (int i = 1; i < bus_data.size(); i++){
			for (int j = 0; j < 13; j++){
				if (j == 1) bus_str += "	" + getBusType(i);
				else bus_str += "	" + bus_data.get(i).get(j);
			}
			bus_str += ";\n";
			// pick out generators and add their id and additional values in generator string and generator cost string
			if (bus_data.get(i).get(1).equals("GEN") || bus_data.get(i).get(1).equals("SLACK_BUS") ) {
				gen_str += "	" + bus_data.get(i).get(0);
				for (int j = 13; j < 33; j++) gen_str += "	" + bus_data.get(i).get(j);
				gen_str += ";\n";
				for (int j = 33; j < bus_data.get(i).size(); j++) gencost_str += "	" + bus_data.get(i).get(j);
				gencost_str += ";\n";
			}
		}

		// add branch values to branch string
		for (int i = 1; i < bra_data.size(); i++){
			for (int j = 1; j < 13; j++){
				if (j == 1 || j == 2) bra_str += "	" + bra_data.get(i).get(j); // from_bus and to_bus
				else if (j == 10) bra_str += "	" + bra_meta_data.get(i).get(j) + "	" + bra_data.get(i).get(3); // include status of line
				else bra_str += "	" + bra_meta_data.get(i).get(j); // all other branch info
			}
			bra_str += ";\n";
		}		
	}
	
	private static void writeToMatFile(BufferedWriter writer, String case_name) throws IOException {
		// Write header information in .m file
		writer.write("function mpc = " + case_name.toString() + "\n");
		writer.write("% Case file generated from SFINA file format\n");
		writer.newLine();
		writer.write("%% MATPOWER Case Format : Version 2\nmpc.version = '2';\n");
		writer.newLine();
		writer.write("%%-----  Power Flow Data  -----%%\n%% system MVA base\nmpc.baseMVA = 100;\n");
		writer.newLine();
				
		// Write bus data
		writer.write("%% bus data\n%	bus_i	type	Pd	Qd	Gs	Bs	area	Vm	Va	baseKV	zone	Vmax	Vmin\nmpc.bus = [\n");
		writer.write(bus_str);
		writer.write("];\n");
		
		// Write generator data
		writer.write("%% generator data\n%	bus	Pg	Qg	Qmax	Qmin	Vg	mBase	status	Pmax	Pmin	Pc1	Pc2	Qc1min	Qc1max	Qc2min	Qc2max	ramp_agc	ramp_10	ramp_30	ramp_q	apf\nmpc.gen = [\n");
		writer.write(gen_str);
		writer.write("];\n");
				
		// Write branch data
		writer.write("%% branch data\n%	fbus	tbus	r	x	b	rateA	rateB	rateC	ratio	angle	status	angmin	angmax\nmpc.branch = [\n");
		writer.write(bra_str);
		writer.write("];\n");
		
		// Write generator cost data
		writer.write("%%-----  OPF Data  -----%%\n%% generator cost data\n%	1	startup	shutdown	n	x1	y1	...	xn	yn\n%	2	startup	shutdown	n	c(n-1)	...	c0\nmpc.gencost = [\n");
		writer.write(gencost_str);
		writer.write("];\n");
	}
	
	private static boolean isIsolated(int bus_row) {
		String bus = bus_data.get(bus_row).get(0);
		boolean is_isolated = true;
		for (int i = 1; i < bra_data.size(); i++){
			if (bra_data.get(i).get(1).equals(bus) || bra_data.get(i).get(2).equals(bus) && bra_data.get(i).get(3).equals("1")) {
				is_isolated = false;
			}
		}
		return is_isolated;
	}

	private static String getBusType(int bus_row){
		String type = bus_data.get(bus_row).get(1);
		String type_matpower = null;
		if (isIsolated(bus_row)) type_matpower = "4";
		else if (type.equals("SLACK_BUS")) type_matpower = "3";
		else if (type.equals("GEN")) type_matpower = "2";
		else if (type.equals("BUS")) type_matpower = "1";
		else { // catch if bus type is not recognized
			type_matpower = "NaN";
			System.out.println("Problem: Didn't recognize bus type!");
		}
		return type_matpower;
	}
}