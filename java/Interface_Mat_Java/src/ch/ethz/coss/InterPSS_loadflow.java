package ch.ethz.coss;

import org.interpss.CorePluginObjFactory;
import org.interpss.IpssCorePlugin;
import org.interpss.display.AclfOutFunc;
import org.interpss.display.AclfOutFunc.BusIdStyle;
import org.interpss.display.impl.AclfOut_BusStyle;
import org.interpss.fadapter.IpssFileAdapter;
import org.interpss.pssl.simu.IpssDclf;
import org.interpss.pssl.simu.IpssDclf.DclfAlgorithmDSL;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.algo.LoadflowAlgorithm;

public class InterPSS_loadflow {
	private String mode;
	private String inputpath;
	
	// Default Constructor sets AC loadflow analysis
	public InterPSS_loadflow() {
		mode = new String("AC");
		inputpath = new String("/");
	}
	// Constructor with argument AC or DC
	public InterPSS_loadflow(String new_inputpath, String input_mode) {
		mode = input_mode;
		inputpath = new_inputpath;
	}
	
	// Run InterPSS Power Flow analysis
	public String runlf() {
		
		//Initialize logger and Spring config
		IpssCorePlugin.init();
		
		// import IEEE CDF format data to create a network object
		AclfNetwork net;
		String result = "There seems to be a problem";
		try {
			if (mode.equals("AC")) {
				net = CorePluginObjFactory
						.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
						.load(inputpath)
						.getAclfNet();
				//create a load flow algorithm object
				LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);
				//run load flow using default setting
			  	algo.loadflow();
			  	result = AclfOutFunc.loadFlowSummary(net).toString();

			} else if (mode.equals("DC")) {
				//net = IpssAdapter.importAclfNet("testData/aclf/ieee14.ieee")
                //        .setFormat(IpssAdapter.FileFormat.IEEECommonFormat)
                //        .load()
                //        .getAclfNet();
				/*net = CorePluginObjFactory
						.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
						.load(inputpath)
						.getAclfNet();

				DclfAlgorithmDSL algoDsl = IpssDclf.createDclfAlgorithm(net)
                        .runDclfAnalysis();

				result = IpssUtil.outDclfResult(algoDsl).toString();
				*/
				result = "DC is not yet implemented";
				
			} else
				result = "Give valid input parameter to function runlf: DC or AC";
		  			  			
		} catch (InterpssException e) {
			result = "InterPSS Exception occurred" + e.getMessage();	
		}
		
		return result;
	}
}
