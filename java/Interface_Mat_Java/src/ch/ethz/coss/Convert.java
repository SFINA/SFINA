package ch.ethz.coss;

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
	
	/**
	 * Runs the conversion from SFINA custom .txt to Matpower .m format. 
	 * @param branch_path		path to the branch_info.txt file
	 * @param branch_meta_path	path to the branch_meta_info.txt file
	 * @param bus_meta_path		path to the bus_meta_info.txt file
	 * @throws IOException 
	 */
	public static void run(String branch_path, String branch_meta_path, String bus_meta_path, String save_to_path, String case_name) throws IOException {
		Charset encoding = StandardCharsets.US_ASCII;
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(save_to_path, case_name.toString() + ".m"), encoding);
		writer.write("function mpc = " + case_name.toString() + ";");
		writer.newLine();
		writer.write("mpc.version = '2';");
		writer.close();
		System.out.println("Done");
	}
}