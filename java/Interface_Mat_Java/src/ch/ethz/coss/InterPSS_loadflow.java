package ch.ethz.coss;

import org.eclipse.emf.common.util.EList;
import org.interpss.CorePluginObjFactory;
import org.interpss.IpssCorePlugin;
import org.interpss.display.AclfOutFunc;
import org.interpss.display.AclfOutFunc.BusIdStyle;
import org.interpss.display.DclfOutFunc;
import org.interpss.display.impl.AclfOut_BusStyle;
//import org.interpss.display.AclfOutFunc.BusIdStyle;
//import org.interpss.display.impl.AclfOut_BusStyle;
import org.interpss.fadapter.IpssFileAdapter;
import org.interpss.pssl.simu.IpssDclf;
import org.interpss.pssl.simu.IpssDclf.DclfAlgorithmDSL;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBranch;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.algo.LoadflowAlgorithm;
import com.interpss.core.dclf.DclfAlgorithm;
import com.interpss.core.dclf.DclfFactory;
import com.interpss.core.net.Branch;

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
				
				EList<AclfBranch> branchlist = net.getBranchList();
				net.getBranch("Bus4","Bus9").setStatus(false);
				net.getBranch("Bus4","Bus7").setStatus(false);
				net.getBranch("Bus5","Bus6").setStatus(false);
				for (Branch br : branchlist) {
					System.out.println(br);
				}
				
				//create a load flow algorithm object
				LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);
				//run load flow using default setting
			  	algo.loadflow();
			  	result = AclfOutFunc.loadFlowSummary(net).toString() + AclfOut_BusStyle.lfResultsBusStyle(net, BusIdStyle.BusId_No).toString();

			} else if (mode.equals("DC")) {
				//net = IpssAdapter.importAclfNet("testData/aclf/ieee14.ieee")
                //        .setFormat(IpssAdapter.FileFormat.IEEECommonFormat)
                //        .load()
                //        .getAclfNet();
				/*net = CorePluginObjFactory
						.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
						.load(inputpath)
						.getAclfNet();     
        
				DclfAlgorithmDSL algoDsl = IpssPTrading.createDclfAlgorithm(net);
				algoDsl.runDclfAnalysis();

				result = IpssUtil.outDclfResult(algoDsl, false)
                        .toString();   
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
