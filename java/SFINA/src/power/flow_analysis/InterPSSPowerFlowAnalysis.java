/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package power.flow_analysis;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.DclfObjectFactory;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfGenCode;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.algo.AclfMethod;
import com.interpss.core.algo.LoadflowAlgorithm;
import com.interpss.core.dclf.DclfAlgorithm;
import com.interpss.core.dclf.common.ReferenceBusException;
import flow_analysis.FlowAnalysisInterface;
import java.util.List;
import network.Link;
import network.Node;
import org.apache.commons.math3.complex.Complex;
import org.apache.log4j.Logger;
import org.interpss.CorePluginObjFactory;
import org.interpss.IpssCorePlugin;
import org.interpss.display.AclfOutFunc.BusIdStyle;
import org.interpss.display.DclfOutFunc;
import org.interpss.display.impl.AclfOut_BusStyle;
import org.interpss.fadapter.IpssFileAdapter;
import org.interpss.numeric.exp.IpssNumericException;
import power.PowerFlowType;
import power.PowerNodeType;
import power.input.PowerNodeState;

/**
 *
 * @author evangelospournaras
 */
public class InterPSSPowerFlowAnalysis implements FlowAnalysisInterface{
    
    private List<Node> nodesSfina;
    private List<Link> linksSfina;
    private PowerFlowType powerFlowType;
    private AclfNetwork net;
    private String resultAsString;
    private static final Logger logger = Logger.getLogger(InterPSSPowerFlowAnalysis.class);
    
    public InterPSSPowerFlowAnalysis(PowerFlowType powerFlowType){
        this.powerFlowType=powerFlowType;
    }
    
    
    @Override
    public void flowAnalysis(List<Node> nodes, List<Link> links){
        
        this.nodesSfina = nodes;
        this.linksSfina = links;
        
        //Initialize logger and Spring config
        IpssCorePlugin.init();
        // import IEEE CDF format data to create a network object
        try{
            switch(powerFlowType){
                case AC:
                    buildIpssNet();
                    //net = CorePluginObjFactory.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF).load(inputpath).getAclfNet();
                    //create a load flow algorithm object
                    LoadflowAlgorithm acAlgo = CoreObjectFactory.createLoadflowAlgorithm(net);
                    acAlgo.setLfMethod(AclfMethod.NR); // NR = Newton-Raphson, PQ = fast decoupled, (GS = Gauss)
                    acAlgo.loadflow();
                    resultAsString = AclfOut_BusStyle.lfResultsBusStyle(net, BusIdStyle.BusId_No).toString();
                    getIpssResults();
                    break;
                case DC:
                    buildIpssNet();
                    //net = CorePluginObjFactory.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF).load(inputpath).getAclfNet();  
                    DclfAlgorithm dcAlgo = DclfObjectFactory.createDclfAlgorithm(net);
                    dcAlgo.calculateDclf();
                    resultAsString = DclfOutFunc.dclfResults(dcAlgo, false).toString();
                    //assertTrue(Math.abs(algo.getBusPower(net.getBus("Bus1"))-2.1900)<0.01);
                    getIpssResults();
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
    
    /**
     * Transfrom SFINA to IPSS network
     */
    private void buildIpssNet(){
        net = CoreObjectFactory.createAclfNetwork();
        
        // Set baseKVA. Not sure if we have this in our data, or if we need it. Or we just set it here to a default value.
        double baseKva = 100000.0;
        net.setBaseKva(baseKva);
        try {
            for (Node node : nodesSfina) {
                AclfBus IpssBus = CoreObjectFactory.createAclfBus(node.getIndex(), net);
                IpssBus.setStatus(node.isActivated());
                switch((PowerNodeType)node.getProperty(PowerNodeState.TYPE)){
                    case GENERATOR:
                        IpssBus.setGenCode(AclfGenCode.GEN_PV); // or GEN_PQ ?
                        break;
                    case SLACK_BUS:
                        IpssBus.setGenCode(AclfGenCode.SWING);
                        IpssBus.toSwingBus();
                        break;
                    case BUS:
                        IpssBus.setGenCode(AclfGenCode.NON_GEN);
                        break;
                    default:
                        logger.debug("PowerNodeType is not recognized.");
                }
                IpssBus.setLoadP((Double)node.getProperty(PowerNodeState.REAL_POWER_DEMAND));
                IpssBus.setLoadQ((Double)node.getProperty(PowerNodeState.REACTIVE_POWER_DEMAND));
                // IpssBus.... Other data transformation to be implemented. Next: Shunt_cond,...
                IpssBus.setVoltageMag((Double)node.getProperty(PowerNodeState.VOLTAGE_MAGNITUDE));
                IpssBus.setVoltageAng((Double)node.getProperty(PowerNodeState.VOLTAGE_ANGLE));
                IpssBus.setBaseVoltage((Double)node.getProperty(PowerNodeState.BASE_VOLTAGE));
                
                
            }
        }
        catch(InterpssException ie){
            ie.printStackTrace();
        }
    };
    
    /**
     * Transform IPSS results back to SFINA
     */
    private void getIpssResults(){
        
    };
    
}
