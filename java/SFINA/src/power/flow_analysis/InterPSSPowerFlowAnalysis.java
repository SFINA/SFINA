/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package power.flow_analysis;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.DclfObjectFactory;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.algo.AclfMethod;
import com.interpss.core.algo.LoadflowAlgorithm;
import com.interpss.core.dclf.DclfAlgorithm;
import com.interpss.core.dclf.common.ReferenceBusException;
import flow_analysis.FlowAnalysisInterface;
import java.util.List;
import network.Link;
import network.Node;
import org.apache.log4j.Logger;
import org.interpss.CorePluginObjFactory;
import org.interpss.IpssCorePlugin;
import org.interpss.display.AclfOutFunc.BusIdStyle;
import org.interpss.display.DclfOutFunc;
import org.interpss.display.impl.AclfOut_BusStyle;
import org.interpss.fadapter.IpssFileAdapter;
import org.interpss.numeric.exp.IpssNumericException;
import power.PowerFlowType;

/**
 *
 * @author evangelospournaras
 */
public class InterPSSPowerFlowAnalysis implements FlowAnalysisInterface{
    
    private String mode;
    private String inputpath;
    private PowerFlowType powerFlowType;
    private static final Logger logger = Logger.getLogger(InterPSSPowerFlowAnalysis.class);
    
    public InterPSSPowerFlowAnalysis(PowerFlowType powerFlowType){
        this.powerFlowType=powerFlowType;
    }
    
    
    @Override
    public void flowAnalysis(List<Node> nodes, List<Link> links){
        //Initialize logger and Spring config
        IpssCorePlugin.init();
        // import IEEE CDF format data to create a network object
        AclfNetwork net;
        String result = "There seems to be a problem";
        try{
            switch(powerFlowType){
                case AC:
                    net = CorePluginObjFactory.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF).load(inputpath).getAclfNet();
                    //create a load flow algorithm object
                    LoadflowAlgorithm acAlgo = CoreObjectFactory.createLoadflowAlgorithm(net);
                    acAlgo.setLfMethod(AclfMethod.NR); // NR = Newton-Raphson, PQ = fast decoupled, (GS = Gauss)
                    acAlgo.loadflow();
                    result = AclfOut_BusStyle.lfResultsBusStyle(net, BusIdStyle.BusId_No).toString();
                    break;
                case DC:
                    net = CorePluginObjFactory.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF).load(inputpath).getAclfNet();  
                    DclfAlgorithm dcAlgo = DclfObjectFactory.createDclfAlgorithm(net);
                    dcAlgo.calculateDclf();
                    result = DclfOutFunc.dclfResults(dcAlgo, false).toString();
                                //assertTrue(Math.abs(algo.getBusPower(net.getBus("Bus1"))-2.1900)<0.01);
                    dcAlgo.destroy();	
                    break;
                default:
                    logger.debug("Power flow type is not recognized.");
            }
        }
        catch(ReferenceBusException rbe){
            rbe.printStackTrace();
        }
        catch(IpssNumericException ine){
            ine.printStackTrace();
        }
        catch(InterpssException ie){
            ie.printStackTrace();
        }
        
    }
    
}
