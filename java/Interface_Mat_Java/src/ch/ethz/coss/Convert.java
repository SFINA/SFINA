package ch.ethz.coss;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 
 * @author Ben
 *
 */
public class Convert {
	private static String bus = "";
	private static String gen = "";
	private static String bra = "";
	
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
		
		extract(read_bus, read_branch, read_branch_meta);
		
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
		writer.write(bus);
		writer.write("];\n");
		
		// Write generator data
		writer.write("%% generator data\n%	bus	Pg	Qg	Qmax	Qmin	Vg	mBase	status	Pmax	Pmin	Pc1	Pc2	Qc1min	Qc1max	Qc2min	Qc2max	ramp_agc	ramp_10	ramp_30	ramp_q	apf\nmpc.gen = [\n");
		writer.write(gen);
		writer.write("];\n");
		
		// Write branch data
		writer.write("%% branch data\n%	fbus	tbus	r	x	b	rateA	rateB	rateC	ratio	angle	status	angmin	angmax\nmpc.branch = [\n");
		writer.write(bra);
		writer.write("];\n");
		
		writer.close();
		read_bus.close();
		read_branch.close();
		System.out.println("Conversion successful.");
	}
	private static void extract(BufferedReader read_bus, BufferedReader read_branch, BufferedReader read_branch_meta) throws IOException {
		String line_bus = null;
		String line_bra = null;
		String line_bra_m = null;
		while ((line_bus = read_bus.readLine()) != null) {
			String[] split = line_bus.split(",");
			if (split[0].equals("node_id")) continue; // skip labels of first line
			for (int i = 0; i < 13; i++) { // extract bus info and append it to variable bus
				if (i == 1) { // sort out the type of bus
					if (split[1].equals("SLACK_BUS")) bus += "	3";
					else if (split[1].equals("GEN")) bus += "	2";
					else if (split[1].equals("BUS")) bus += "	1";
					else { // catch if bus type is not recognized
						bus += "	NaN";
						System.out.println("Problem: Didn't recognize bus type! node_id = " + split[0].toString());
					}
				}
				else 
					bus += "	" + split[i];
			}
			bus += ";\n"; // end of line

			if (split[1].equals("GEN")) { // extract which buses are generators and append their additional info to variable gen
				gen += split[0];
				for (int i = 13; i < split.length; i++) gen += "	" + split[i];
				gen += ";\n";
			}
		}
		while ((line_bra = read_branch.readLine()) != null && (line_bra_m = read_branch_meta.readLine()) != null ) {
			String[] split_bra = line_bra.split(",");
			String[] split_bra_m = line_bra_m.split(",");
			if (split_bra[0].equals("branch_id")) continue; // skip labels of first line
			bra += "	" + split_bra[1]; // from_bus
			bra += "	" + split_bra[2]; // to_bus
			for (int i = 3; i < split_bra_m.length - 2; i++) {
				bra += "	" + split_bra_m[i];
			}
			bra += "	" + split_bra[3]; // status
			bra += "	" + split_bra_m[split_bra_m.length - 2] + "	" + split_bra_m[split_bra_m.length - 1]; // angmin and angmax  
			bra += ";\n"; 
		}
	}
}