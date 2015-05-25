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
import org.interpss.numeric.exp.IpssNumericException;
import org.interpss.pssl.plugin.IpssAdapter;
import org.interpss.pssl.simu.IpssDclf;
import org.interpss.pssl.simu.IpssDclf.DclfAlgorithmDSL;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.DclfObjectFactory;
import com.interpss.core.aclf.AclfBranch;
import com.interpss.core.aclf.AclfGenCode;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.algo.AclfMethod;
import com.interpss.core.algo.LoadflowAlgorithm;
import com.interpss.core.dclf.DclfAlgorithm;
import com.interpss.core.dclf.DclfFactory;
import com.interpss.core.dclf.common.ReferenceBusException;
import com.interpss.core.net.Area;
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
	public InterPSS_loadflow(String inputpath, String mode) {
		this.mode = mode;
		this.inputpath = inputpath;
	}
	
	public String runlf() throws ReferenceBusException, IpssNumericException {
		
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
				
				// Trying some methods on branches and buses
//				EList<AclfBranch> branchlist = net.getBranchList();
//				net.getBranch("Bus4","Bus9").setStatus(false);
//				net.getBranch("Bus4","Bus7").setStatus(false);
//				net.getBranch("Bus5","Bus6").setStatus(false);
//				net.getBranch("Bus7","Bus8").setStatus(false);
//				net.getBus("Bus6").setGenCode(AclfGenCode.SWING); // Set first bus of non-converging island to Slack-bus -> seems to converge
//				System.out.println(net.getBranch("Bus4","Bus9").getId());
//				System.out.println(net.getBranch("Bus4","Bus1").getRatingAmps());
//				System.out.println("Bus 8 is isolated: " + net.getBus("Bus8").isIslandBus());
//				for (Branch br : branchlist) {
//					System.out.println(br);
//				}
				
				// Trying some InterPSS methods if they're about islands. doens't seem so...
//				EList<Area> areas = net.getAreaList();
//				System.out.println(areas.toString());
//				System.out.println(net.hasChildNet());
				
				//create a load flow algorithm object
				LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);
				algo.setLfMethod(AclfMethod.NR); // NR = Newton-Raphson, PQ = fast decoupled, (GS = Gauss)
			  	algo.loadflow();
//			  	System.out.println("Voltage Bus 8: " + net.getBus("Bus8").getVoltage().toString());
			  	result = AclfOut_BusStyle.lfResultsBusStyle(net, BusIdStyle.BusId_No).toString();

			} else if (mode.equals("DC")) {
				net = CorePluginObjFactory
						.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
						.load(inputpath)
						.getAclfNet();  

				DclfAlgorithm algo = DclfObjectFactory.createDclfAlgorithm(net);
				algo.calculateDclf();

				result = DclfOutFunc.dclfResults(algo, false).toString();
		  		//assertTrue(Math.abs(algo.getBusPower(net.getBus("Bus1"))-2.1900)<0.01);

				algo.destroy();	
				
			} else
				result = "Give valid input parameter to function runlf: DC or AC";
		  			  			
		} catch (InterpssException e) {
			result = "InterPSS Exception occurred" + e.getMessage();	
		}
		
		return result;
	}
}
