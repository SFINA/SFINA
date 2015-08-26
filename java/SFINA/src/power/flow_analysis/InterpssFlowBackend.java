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
import com.interpss.core.aclf.AclfLoadCode;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.algo.AclfMethod;
import com.interpss.core.algo.LoadflowAlgorithm;
import com.interpss.core.dclf.DclfAlgorithm;
import com.interpss.core.dclf.common.ReferenceBusException;
import com.interpss.core.net.Area;
import com.interpss.core.net.Zone;
import flow_analysis.FlowBackendInterface;
import static java.lang.Math.PI;
import java.util.ArrayList;
import network.FlowNetwork;
import network.Link;
import network.Node;
import org.apache.commons.math3.complex.Complex;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.interpss.CorePluginObjFactory;
import org.interpss.IpssCorePlugin;
import org.interpss.display.AclfOutFunc.BusIdStyle;
import org.interpss.display.DclfOutFunc;
import org.interpss.display.impl.AclfOut_BusStyle;
import org.interpss.fadapter.IpssFileAdapter;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.interpss.numeric.exp.IpssNumericException;
import power.PowerFlowType;
import power.PowerNodeType;
import power.input.PowerLinkState;
import power.input.PowerNodeState;

/**
 *
 * @author evangelospournaras
 */
public class InterpssFlowBackend implements FlowBackendInterface{
    
    private PowerFlowType powerFlowType;
    private FlowNetwork SfinaNet;
    private AclfNetwork IpssNet;
    private static final Logger logger = Logger.getLogger(InterpssFlowBackend.class);
    
    public InterpssFlowBackend(PowerFlowType powerFlowType){
        this.powerFlowType=powerFlowType;
    }
    
    
    @Override
    public void flowAnalysis(FlowNetwork net){
        
        this.SfinaNet = net;
        
        IpssCorePlugin.init();
        
        try{
            switch(powerFlowType){
                case AC:
                    buildIpssNet();
                    LoadflowAlgorithm acAlgo = CoreObjectFactory.createLoadflowAlgorithm(IpssNet);
                    acAlgo.setLfMethod(AclfMethod.NR); // NR = Newton-Raphson, PQ = fast decoupled, (GS = Gauss)
                    acAlgo.loadflow();
                    getIpssResults();
                    break;
                case DC:
                    buildIpssNet();
                    DclfAlgorithm dcAlgo = DclfObjectFactory.createDclfAlgorithm(IpssNet);
                    dcAlgo.calculateDclf();
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
        IpssNet = CoreObjectFactory.createAclfNetwork();
        
        // Set baseKVA. Not sure if we have this in our data, or if we need it. Or we just set it here to a default value.
        IpssNet.setBaseKva(100000.0);
        
        try {
            for (Node node : SfinaNet.getNodes()) {
                AclfBus IpssBus = CoreObjectFactory.createAclfBus(node.getIndex(), IpssNet); // name of bus in InterPSS is index of node in SFINA. Buses are referenced by this name.
                IpssBus.setNumber(Long.parseLong(node.getIndex()));
                IpssBus.setStatus(node.isActivated());
                switch((PowerNodeType)node.getProperty(PowerNodeState.TYPE)){
                    case GENERATOR:
                        IpssBus.setGenCode(AclfGenCode.GEN_PV);
                        IpssBus.toPVBus(); // don't know if necessary
                        IpssBus.setGenP((Double)node.getProperty(PowerNodeState.REAL_POWER_GENERATION)/IpssNet.getBaseMva()); // in MW
                        IpssBus.setGenQ((Double)node.getProperty(PowerNodeState.REACTIVE_POWER_GENERATION)/IpssNet.getBaseMva()); // in MVAr
                        // Missing reactive_power_max, reactive_power_min, real_power_max, real_power_min
                        // In InterPSS methods existing: .setExpLoadP(), .setExpLoadQ()
                        IpssBus.setDesiredVoltMag((Double)node.getProperty(PowerNodeState.VOLTAGE_MAGNITUDE)); // derived from comparison with IEEE loaded data
                        //if (node.getProperty(PowerNodeState.VOLTAGE_SETPOINT) != null) 
                        //    IpssBus.setVoltageMag((Double)node.getProperty(PowerNodeState.VOLTAGE_SETPOINT)); // in p.u. But kindof redundant information, already set further below

                        break;
                    case SLACK_BUS:
                        IpssBus.setGenCode(AclfGenCode.SWING);
                        IpssBus.toSwingBus();
                        IpssBus.setGenP((Double)node.getProperty(PowerNodeState.REAL_POWER_GENERATION)/IpssNet.getBaseMva()); // in MW
                        IpssBus.setGenQ((Double)node.getProperty(PowerNodeState.REACTIVE_POWER_GENERATION)/IpssNet.getBaseMva()); // in MVAr
                        IpssBus.setDesiredVoltMag((Double)node.getProperty(PowerNodeState.VOLTAGE_MAGNITUDE)); // derived from comparison with IEEE loaded data
                        break;
                    case BUS:
                        IpssBus.setGenCode(AclfGenCode.GEN_PQ);
                        IpssBus.toPQBus();
                        break;
                    default:
                        logger.debug("PowerNodeType is not recognized.");
                }
                
                // Properties for all buses (Gen and non-Gen)

                
                IpssBus.setLoadCode(AclfLoadCode.CONST_P);
                
                IpssBus.setLoadP((Double)node.getProperty(PowerNodeState.REAL_POWER_DEMAND)/IpssNet.getBaseMva()); // in MW
                IpssBus.setLoadQ((Double)node.getProperty(PowerNodeState.REACTIVE_POWER_DEMAND)/IpssNet.getBaseMva()); // in MVAr
                
                Complex y = new Complex((Double)node.getProperty(PowerNodeState.SHUNT_CONDUCT),(Double)node.getProperty(PowerNodeState.SHUNT_SUSCEPT));
                IpssBus.setShuntY(y);

                IpssBus.setVoltageMag((Double)node.getProperty(PowerNodeState.VOLTAGE_MAGNITUDE)); // in p.u.
                IpssBus.setVoltageAng((Double)node.getProperty(PowerNodeState.VOLTAGE_ANGLE)*PI/180); // in rad. convert from deg to rad.
                
                if ((Double)node.getProperty(PowerNodeState.BASE_VOLTAGE) == 0.0)
                    IpssBus.setBaseVoltage(1000.0);
                else 
                    IpssBus.setBaseVoltage((Double)node.getProperty(PowerNodeState.BASE_VOLTAGE));
                
                
                /*** Area and Zone are optional for Power Flow calculation ***/
                
                //Area area = new Area(); // Doesn't work
                //IpssBus.setArea((Area)node.getProperty(PowerNodeState.AREA)); 
                
                //Zone zone = new Zone(); // Doesn't work
                //IpssBus.setZone((Zone)node.getProperty(PowerNodeState.ZONE));
                
                // Missing voltage_max, voltage_min
                // In InterPSS methods existing: .setDesiredVoltAng(), .setDesiredVoltMag()

                
            }
            for (Link link : SfinaNet.getLinks()){

                AclfBranch IpssBranch = CoreObjectFactory.createAclfBranch();
                IpssNet.addBranch(IpssBranch, link.getStartNode().getIndex(), link.getEndNode().getIndex()); // names of buses in InterPSS are index of SFINA nodes
                IpssBranch.setId(link.getIndex()); // set branch id to SFINA branch id to make better accessible
                
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
                
                // Admittance matrix
                //System.out.println(net.formYMatrix());
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

        for(AclfBus bus : IpssNet.getBusList()) {
            Node SfinaNode = SfinaNet.getNode(bus.getId());
            
            SfinaNode.replacePropertyElement(PowerNodeState.VOLTAGE_MAGNITUDE, bus.getVoltageMag(UnitType.PU));
            SfinaNode.replacePropertyElement(PowerNodeState.VOLTAGE_ANGLE, bus.getVoltageAng(UnitType.Deg));
            
            if(bus.isSwing()){
                SfinaNode.replacePropertyElement(PowerNodeState.REAL_POWER_GENERATION, bus.getNetGenResults().getReal()*IpssNet.getBaseMva());
                SfinaNode.replacePropertyElement(PowerNodeState.REACTIVE_POWER_GENERATION, bus.getNetGenResults().getImaginary()*IpssNet.getBaseMva());
            }
            if (bus.isGenPV()){
                SfinaNode.replacePropertyElement(PowerNodeState.REAL_POWER_GENERATION, bus.getGenP()*IpssNet.getBaseMva());
                SfinaNode.replacePropertyElement(PowerNodeState.REACTIVE_POWER_GENERATION, bus.getGenQ()*IpssNet.getBaseMva());
            }
            
        }
        
        for(AclfBranch branch : IpssNet.getBranchList()){
            Link SfinaLink = SfinaNet.getLink(branch.getId());
            
            SfinaLink.replacePropertyElement(PowerLinkState.REAL_POWER_FLOW_FROM, branch.powerFrom2To(UnitType.mW).getReal());
            SfinaLink.replacePropertyElement(PowerLinkState.REACTIVE_POWER_FLOW_FROM, branch.powerFrom2To(UnitType.mVar).getImaginary());
            SfinaLink.replacePropertyElement(PowerLinkState.REAL_POWER_FLOW_TO, branch.powerTo2From(UnitType.mW).getReal());
            SfinaLink.replacePropertyElement(PowerLinkState.REACTIVE_POWER_FLOW_TO, branch.powerTo2From(UnitType.mVar).getImaginary());
            
            SfinaLink.replacePropertyElement(PowerLinkState.CURRENT, branch.current(UnitType.Amp));
        }
        
    };
    
    
    public void flowAnalysisIpssDataLoader(FlowNetwork net, String CaseName) throws InterpssException{
        this.SfinaNet = net;
        IpssCorePlugin.init();
        this.IpssNet = CorePluginObjFactory.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF).load("/Users/Ben/Documents/Studium/COSS/SFINA/java/SFINA/configuration_files/case_files/" + CaseName).getAclfNet();
        LoadflowAlgorithm directAlgo = CoreObjectFactory.createLoadflowAlgorithm(IpssNet);
        directAlgo.loadflow();
        
        // Make bus and branch index compatible with SFINA
        int i = 0;
        for (AclfBus bus : IpssNet.getBusList()){
            bus.setId(String.valueOf(++i));
        }
        i = 0;
        for (AclfBranch branch : IpssNet.getBranchList()){
            branch.setId(String.valueOf(++i));
        }
        
        getIpssResults();

        String resultLoaded = AclfOut_BusStyle.lfResultsBusStyle(IpssNet, BusIdStyle.BusId_No).toString();
        //System.out.println(resultLoaded);
        
        /*
        System.out.format("%10s%20s%20s%20s%20s","Bus", "GenP load", "GenQ load", "GenP dir", "GenQ dir\n");
        for (int i=1; i < 58; i++){
            AclfBus busLoaded = caseNet.getBus("Bus" + i);
            AclfBus busDirect = IpssNet.getBus("" + i);
            if(busLoaded.getGenCode().equals(AclfGenCode.GEN_PV) || busLoaded.getGenCode().equals(AclfGenCode.SWING))
                System.out.format("%10s%20s%20s%20s%20s\n", i, busLoaded.getGenP(),busLoaded.getGenQ(),busDirect.getGenP(),busDirect.getGenQ());
        }
                */

    }    
    
    
    
    public void compareDataToCaseLoaded(FlowNetwork net, String CaseName) throws InterpssException{
        this.SfinaNet = net;
        IpssCorePlugin.init();
        buildIpssNet();
        AclfNetwork caseNet = CorePluginObjFactory.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF).load("/Users/Ben/Documents/Studium/COSS/SFINA/java/SFINA/configuration_files/case_files/" + CaseName).getAclfNet();
                
        String BusData = "";
        String BranchData = "";
        for (int i=1; i < 58; i++){
            AclfBus busLoaded = caseNet.getBus("Bus" + i);
            AclfBus busDirect = IpssNet.getBus("" + i);
            if (i == 1 || i == 2){
            //    System.out.println(busLoaded);
            //    System.out.println(busDirect);
            }
            BusData += i + "   " + (busLoaded.getLoadP() - busDirect.getLoadP()) + "  |   " + (busLoaded.getLoadQ() - busDirect.getLoadQ()) + "   |   "   + busLoaded.getLoadCode() + "     " + busDirect.getLoadCode()+ "   |   " + caseNet.getBaseMva()+ "       " + IpssNet.getBaseMva() + "   |   "   + caseNet.getBaseKva()+ "       " + IpssNet.getBaseKva()  + "\n";
            BusData += i + "   " + (busLoaded.getGenP() - busDirect.getGenP()) + "   |  " + (busLoaded.getGenQ() - busDirect.getGenQ()) + "   |   "   + busLoaded.getGenCode() + "     " + busDirect.getGenCode() + "   |   " + busLoaded.getGenPartFactor() + "        " + busDirect.getGenPartFactor() + "\n";
        }
        EList<AclfBranch> netBranches = IpssNet.getBranchList();
        EList<AclfBranch> caseNetBranches = caseNet.getBranchList();
            
        for (int i=1; i < caseNet.getNoBranch()+1; i++){
            AclfBranch branchLoaded = caseNetBranches.get(i-1);
            AclfBranch branchDirect = netBranches.get(i-1);
            
            if(i==1){
                //System.out.println(branchLoaded);
                //System.out.println(branchDirect);
            }

            
            BranchData += i + "   " + branchLoaded.getZ() + "        " + branchDirect.getZ() + "  |   " + branchLoaded.getHShuntY()+ "       " + branchDirect.getHShuntY() +  "   |   "   + branchLoaded.getY()+ "       " + branchDirect.getY() + "  |   " + branchLoaded.getFromTurnRatio() + "       " + branchDirect.getFromTurnRatio()+ "  |   " + branchLoaded.getToTurnRatio()+ "       " + branchDirect.getToTurnRatio()+ "   |   "   + branchLoaded.getFromPSXfrAngle()+ "       " + branchDirect.getFromPSXfrAngle()+ "   |   "   + branchLoaded.getToPSXfrAngle()+ "       " + branchDirect.getToPSXfrAngle() + "\n";
            
        }
        System.out.println("Bus  |   LoadP loaded    LoadP direct  |  LoadQ loaded    LoadQ direct  |  BaseVoltage loaded    BaseVoltage direct \n GenP loaded    GenP direct   |   GenQ l    GenQ dir    |   GenCode l   GenCode dir |   GenPartFactor l     GenPartFactor dir\n--------------------------------------------------------------\n");
        System.out.println(BusData + "\n--------------------------------------------------------------");
        System.out.println("\n---------\n");
        System.out.println("Branch  |   Z loaded    Z direct  |  HShuntY loaded    HShuntY direct  |  Y Susceptance l     Y Susceptance dir   |  FromTurnRatio l     FromTurnRatio dir    | ToTurnRatio l      ToTurnRatio dir  |  FromPSXfrAngle l      FromPSXfrAngle dir  |       FromPSXfrAngle l      FromPSXfrAngle dir   |       ToPSXfrAngle l      ToPSXfrAngle dir     \n--------------------------------------------------------------\n");
        System.out.println(BranchData + "\n--------------------------------------------------------------");

    }
    
}
