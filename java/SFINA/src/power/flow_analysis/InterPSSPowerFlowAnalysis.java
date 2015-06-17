/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package power.flow_analysis;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.DclfObjectFactory;
import com.interpss.core.aclf.AclfBranch;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfGenCode;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.algo.AclfMethod;
import com.interpss.core.algo.LoadflowAlgorithm;
import com.interpss.core.dclf.DclfAlgorithm;
import com.interpss.core.dclf.common.ReferenceBusException;
import com.interpss.core.net.Area;
import com.interpss.core.net.Zone;
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
import org.interpss.numeric.datatype.Unit;
import org.interpss.numeric.exp.IpssNumericException;
import power.PowerFlowType;
import power.PowerNodeType;
import power.input.PowerLinkState;
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
                    System.out.println(resultAsString);
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
                //System.out.println(node.toString());
                AclfBus IpssBus = CoreObjectFactory.createAclfBus(node.getIndex(), net); // name of bus in InterPSS is index of node in SFINA. Buses are referenced by this name.
                IpssBus.setStatus(node.isActivated());
                switch((PowerNodeType)node.getProperty(PowerNodeState.TYPE)){
                    case GENERATOR:
                        IpssBus.setGenCode(AclfGenCode.GEN_PV);
                        IpssBus.toPVBus(); // don't know if necessary
                        IpssBus.setGenP((Double)node.getProperty(PowerNodeState.REAL_POWER_GENERATION)); // in MW
                        IpssBus.setGenQ((Double)node.getProperty(PowerNodeState.REACTIVE_POWER_GENERATION)); // in MVAr
                        // Missing reactive_power_max, reactive_power_min, real_power_max, real_power_min
                        // In InterPSS methods existing: .setExpLoadP(), .setExpLoadQ()
                        if (node.getProperty(PowerNodeState.VOLTAGE_SETPOINT) != null) 
                            IpssBus.setVoltageMag((Double)node.getProperty(PowerNodeState.VOLTAGE_SETPOINT)); // in p.u. But kindof redundant information, already set further below
                        
                        break;
                    case SLACK_BUS:
                        IpssBus.setGenCode(AclfGenCode.SWING);
                        IpssBus.toSwingBus();
                        System.out.println(node.getIndex() + " is set to Swing Bus");
                        System.out.println(IpssBus.toString());
                        break;
                    case BUS:
                        IpssBus.setGenCode(AclfGenCode.NON_GEN);
                        break;
                    default:
                        logger.debug("PowerNodeType is not recognized.");
                }
                
                // Properties for all buses (Gen and non-Gen)
                
                //System.out.println("SFINA Load P: " + node.getProperty(PowerNodeState.REAL_POWER_DEMAND));
                IpssBus.setLoadP((Double)node.getProperty(PowerNodeState.REAL_POWER_DEMAND)); // in MW
                IpssBus.setLoadQ((Double)node.getProperty(PowerNodeState.REACTIVE_POWER_DEMAND)); // in MVAr
                //System.out.println("IPSS Load P: " + IpssBus.getLoadP());

                Complex y = new Complex((Double)node.getProperty(PowerNodeState.SHUNT_CONDUCT),(Double)node.getProperty(PowerNodeState.SHUNT_SUSCEPT));
                IpssBus.setShuntY(y);

                //Area area = new Area();
                //IpssBus.setArea((Area)node.getProperty(PowerNodeState.AREA)); 
                
                IpssBus.setVoltageMag((Double)node.getProperty(PowerNodeState.VOLTAGE_MAGNITUDE)); // in p.u.
                IpssBus.setVoltageAng((Double)node.getProperty(PowerNodeState.VOLTAGE_ANGLE)); // in degrees
                
                if ((Double)node.getProperty(PowerNodeState.BASE_VOLTAGE) == 0.0)
                    IpssBus.setBaseVoltage(1000.0);
                else 
                    IpssBus.setBaseVoltage((Double)node.getProperty(PowerNodeState.BASE_VOLTAGE));
                
                if (node.getIndex().equals("1"))
                        System.out.println(IpssBus.toString());
                
                //Zone zone = new Zone();
                //IpssBus.setZone((Zone)node.getProperty(PowerNodeState.ZONE));
                // Missing voltage_max, voltage_min
                // In InterPSS methods existing: .setDesiredVoltAng(), .setDesiredVoltMag()
                
                // Admittance matrix
            }
            for (Link link : linksSfina){
                //System.out.println(link.toString());
                AclfBranch IpssBranch = CoreObjectFactory.createAclfBranch();
                net.addBranch(IpssBranch, link.getStartNode().getIndex(), link.getEndNode().getIndex()); // names of buses in InterPSS are index of SFINA nodes
                // Set Impedance z = r + j*x
                Complex z = new Complex((Double)link.getProperty(PowerLinkState.RESISTANCE),(Double)link.getProperty(PowerLinkState.REACTANCE));
                IpssBranch.setZ(z);
                // Set shunt admittance
                Complex y = new Complex(0,(Double)link.getProperty(PowerLinkState.SUSCEPTANCE)/2); // divided by 2, because it's in matpower total line charging susceptance, in InterPSS half of total branch shunt admittance
                IpssBranch.setHShuntY(y);
                // Set Ratings
                IpssBranch.setRatingMva1((Double)link.getProperty(PowerLinkState.RATE_A));
                IpssBranch.setRatingMva2((Double)link.getProperty(PowerLinkState.RATE_B));
                IpssBranch.setRatingMva3((Double)link.getProperty(PowerLinkState.RATE_C));
                // Set Tap ratio: in matpower nominal tap ratio n = from/to. So we set to = 1 -> n = from
                IpssBranch.setToTurnRatio(1.0);
                IpssBranch.setFromTurnRatio((Double)link.getProperty(PowerLinkState.TAP_RATIO));
                // Set angle difference: in matpower only one value is defined. Assume that it's the angle difference, so we set in InterPSS from = 0 and to = angle_shift
                IpssBranch.setFromPSXfrAngle(0.0);
                IpssBranch.setToPSXfrAngle((Double)link.getProperty(PowerLinkState.ANGLE_SHIFT));
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
