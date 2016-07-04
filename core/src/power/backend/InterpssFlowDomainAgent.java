/*
 * Copyright (C) 2016 SFINA Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package power.backend;

import backend.FlowDomainAgent;
import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.DclfObjectFactory;
import com.interpss.core.aclf.AclfBranch;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfGenCode;
import com.interpss.core.aclf.AclfLoadCode;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.algo.LoadflowAlgorithm;
import com.interpss.core.dclf.DclfAlgorithm;
import com.interpss.core.dclf.common.ReferenceBusException;
import static java.lang.Math.PI;
import java.util.HashMap;
import java.util.logging.Level;
import network.FlowNetwork;
import network.Link;
import network.Node;
import org.apache.commons.math3.complex.Complex;
import org.apache.log4j.Logger;
import org.interpss.IpssCorePlugin;
import org.interpss.numeric.datatype.Unit;
import org.interpss.numeric.exp.IpssNumericException;
import power.input.PowerBackendParameterLoader;
import power.input.PowerLinkState;
import power.input.PowerNodeState;
import power.input.PowerNodeType;
import protopeer.util.quantities.Time;

/**
 *
 * @author evangelospournaras
 */
public class InterpssFlowDomainAgent extends FlowDomainAgent{

    private static final Logger logger = Logger.getLogger(InterpssFlowDomainAgent.class);
    
    private PowerFlowType powerFlowType;
    private Double toleranceParameter;
    private FlowNetwork SfinaNet;
    private AclfNetwork IpssNet;
    private boolean converged;
    
    public InterpssFlowDomainAgent(String experimentID,
            Time bootstrapTime, 
            Time runTime){
        super(experimentID, bootstrapTime, runTime);
        // Is there a better way to force initializing the FlowNetworkDataTypes class?
        this.setFlowNetworkDataTypes(new PowerFlowNetworkDataTypes()); 
        logger.debug("initializing Interpss backend");
        IpssCorePlugin.init();
    }
    
    @Override
    public boolean flowAnalysis(FlowNetwork net){
        logger.debug("calculating loadflow in network with Interpss");
        this.SfinaNet = net;
        
        try{
            switch(powerFlowType){
                case AC:
                    buildIpssNet();
                    LoadflowAlgorithm acAlgo = CoreObjectFactory.createLoadflowAlgorithm(IpssNet);
                    //acAlgo.setLfMethod(AclfMethod.NR); // NR = Newton-Raphson, PQ = fast decoupled, (GS = Gauss)
                    acAlgo.loadflow();
                    getIpssACResults();
                    this.converged = IpssNet.isLfConverged();
                    break;
                case DC:
                    buildIpssNet();
                    DclfAlgorithm dcAlgo = DclfObjectFactory.createDclfAlgorithm(IpssNet);
                    dcAlgo.calculateDclf();
                    getIpssDCResults(dcAlgo);
                    this.converged = dcAlgo.isDclfCalculated();
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
        
        return converged;
    }
    
    @Override
    public void setFlowParameters(FlowNetwork flowNetwork){
        flowNetwork.setLinkFlowType(PowerLinkState.POWER_FLOW_FROM_REAL);
        flowNetwork.setNodeFlowType(PowerNodeState.VOLTAGE_MAGNITUDE);
        flowNetwork.setLinkCapacityType(PowerLinkState.RATE_C);
        flowNetwork.setNodeCapacityType(PowerNodeState.VOLTAGE_MAX);
    }
    
    @Override
    public boolean flowAnalysis(FlowNetwork net, HashMap<Enum,Object> backendParameters){
        setDomainParameters(backendParameters);
        return flowAnalysis(net);
    }
    
    @Override
    public void extractDomainParameters(){
        this.powerFlowType = (PowerFlowType)getDomainParameters().get(PowerBackendParameter.FLOW_TYPE);
        this.toleranceParameter = (Double)getDomainParameters().get(PowerBackendParameter.TOLERANCE_PARAMETER);
    }
    
    /**
     * Loads backend parameters and events from file. The first has to be provided, will give error otherwise. PowerBackend parameters and events are optional.
     * @param backendParamLocation path to backendParameters.txt
     */
    @Override
    public void loadDomainParameters(String backendParamLocation){
        PowerBackendParameterLoader backendParameterLoader = new PowerBackendParameterLoader(parameterColumnSeparator);
        this.setDomainParameters(backendParameterLoader.loadBackendParameters(backendParamLocation));
    }   
    
    /**
     * Transfrom SFINA to IPSS network
     */
    private void buildIpssNet(){
        IpssNet = CoreObjectFactory.createAclfNetwork();
        
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
                        IpssBus.setGenP((Double)node.getProperty(PowerNodeState.POWER_GENERATION_REAL)/IpssNet.getBaseMva()); // in MW
                        IpssBus.setGenQ((Double)node.getProperty(PowerNodeState.POWER_GENERATION_REACTIVE)/IpssNet.getBaseMva()); // in MVAr
                        // Missing reactive_power_max, reactive_power_min, real_power_max, real_power_min
                        // In InterPSS methods existing: .setExpLoadP(), .setExpLoadQ()
                        IpssBus.setDesiredVoltMag((Double)node.getProperty(PowerNodeState.VOLTAGE_MAGNITUDE)); // derived from comparison with IEEE loaded data
                        //if (node.getProperty(PowerNodeState.VOLTAGE_SETPOINT) != null) 
                        //    IpssBus.setVoltageMag((Double)node.getProperty(PowerNodeState.VOLTAGE_SETPOINT)); // in p.u. But kindof redundant information, already set further below

                        break;
                    case SLACK_BUS:
                        IpssBus.setGenCode(AclfGenCode.SWING);
                        IpssBus.toSwingBus();
                        IpssBus.setGenP((Double)node.getProperty(PowerNodeState.POWER_GENERATION_REAL)/IpssNet.getBaseMva()); // in MW
                        IpssBus.setGenQ((Double)node.getProperty(PowerNodeState.POWER_GENERATION_REACTIVE)/IpssNet.getBaseMva()); // in MVAr
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
                if ((Double)node.getProperty(PowerNodeState.POWER_DEMAND_REAL) == 0.0)
                   IpssBus.setLoadCode(AclfLoadCode.NON_LOAD);
                
                IpssBus.setLoadP((Double)node.getProperty(PowerNodeState.POWER_DEMAND_REAL)/IpssNet.getBaseMva()); // in MW
                IpssBus.setLoadQ((Double)node.getProperty(PowerNodeState.POWER_DEMAND_REACTIVE)/IpssNet.getBaseMva()); // in MVAr
                
                Complex y = new Complex((Double)node.getProperty(PowerNodeState.SHUNT_CONDUCT)/IpssNet.getBaseMva(),(Double)node.getProperty(PowerNodeState.SHUNT_SUSCEPT)/IpssNet.getBaseMva());
                IpssBus.setShuntY(y);

                IpssBus.setVoltageMag((Double)node.getProperty(PowerNodeState.VOLTAGE_MAGNITUDE)); // in p.u.
                IpssBus.setVoltageAng((Double)node.getProperty(PowerNodeState.VOLTAGE_ANGLE)*PI/180); // in rad. convert from deg to rad.
                
                if ((Double)node.getProperty(PowerNodeState.BASE_VOLTAGE) == 0.0)
                    IpssBus.setBaseVoltage(1000.0);
                else 
                    IpssBus.setBaseVoltage((Double)node.getProperty(PowerNodeState.BASE_VOLTAGE)*1000.0);
            }
            
            for (Link link : SfinaNet.getLinks()){

                AclfBranch IpssBranch = CoreObjectFactory.createAclfBranch();
                IpssNet.addBranch(IpssBranch, link.getStartNode().getIndex(), link.getEndNode().getIndex()); // names of buses in InterPSS are index of SFINA nodes
                IpssBranch.setId(link.getIndex()); // set branch id to SFINA branch id to make better accessible
                IpssBranch.setStatus(link.isActivated());
                // Set Impedance z = r + j*x
                Complex z = new Complex((Double)link.getProperty(PowerLinkState.RESISTANCE),(Double)link.getProperty(PowerLinkState.REACTANCE));
                IpssBranch.setZ(z);
                // Set shunt admittance
                Complex y = new Complex(0,(Double)link.getProperty(PowerLinkState.SUSCEPTANCE)/2); // divided by 2, because it's in matpower total line charging susceptance, in InterPSS half of total branch shunt admittance
                IpssBranch.setHShuntY(y);
                // Set Ratings
                //IpssBranch.setRatingMva1((Double)link.getProperty(PowerLinkState.RATE_A));
                //IpssBranch.setRatingMva2((Double)link.getProperty(PowerLinkState.RATE_B));
                //IpssBranch.setRatingMva3((Double)link.getProperty(PowerLinkState.RATE_C));
                // Set Tap ratio: in matpower nominal tap ratio n = from/to. So we set to = 1 -> n = from
                if ((Double)link.getProperty(PowerLinkState.TAP_RATIO) != 0.0)
                    IpssBranch.setFromTurnRatio((Double)link.getProperty(PowerLinkState.TAP_RATIO));
                else
                    IpssBranch.setFromTurnRatio(1.0);
                IpssBranch.setFromTurnRatio(1.0);
                IpssBranch.setToTurnRatio(1.0);
                     
                //IpssBranch.setFromTurnRatio((Double)link.getProperty(PowerLinkState.TAP_RATIO));
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
    
    private void getIpssDCResults(DclfAlgorithm dcAlgo){
        int j = 0;
        for(AclfBus bus : IpssNet.getBusList()) {
            Node SfinaNode = SfinaNet.getNode(bus.getId());
            SfinaNode.replacePropertyElement(PowerNodeState.VOLTAGE_MAGNITUDE, 1.0);
            SfinaNode.replacePropertyElement(PowerNodeState.VOLTAGE_ANGLE, dcAlgo.getBusAngle(j++)*180/PI); // Somehow in the object dcAlgo the buses are still referenced by 0..n-1 instead of 1..n
            if (!bus.isGenPQ()){
                SfinaNode.replacePropertyElement(PowerNodeState.POWER_GENERATION_REACTIVE, 0.0);
            }
            if (bus.isSwing()){
                SfinaNode.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, dcAlgo.getBusPower(bus)*IpssNet.getBaseMva());
            }
            SfinaNode.replacePropertyElement(PowerNodeState.POWER_DEMAND_REACTIVE, 0.0);
                
        }
        for(AclfBranch branch : IpssNet.getBranchList()){
            Link SfinaLink = SfinaNet.getLink(branch.getId());
            SfinaLink.replacePropertyElement(PowerLinkState.POWER_FLOW_FROM_REAL, branch.getDclfFlow()*IpssNet.getBaseMva());
            SfinaLink.replacePropertyElement(PowerLinkState.POWER_FLOW_TO_REAL, -branch.getDclfFlow()*IpssNet.getBaseMva());
            SfinaLink.replacePropertyElement(PowerLinkState.POWER_FLOW_FROM_REACTIVE, 0.0);
            SfinaLink.replacePropertyElement(PowerLinkState.POWER_FLOW_TO_REACTIVE, 0.0);
            SfinaLink.addProperty(PowerLinkState.LOSS_REAL, 0.0);
            SfinaLink.addProperty(PowerLinkState.LOSS_REACTIVE, 0.0);
        }
    }
    
    /**
     * Transform IPSS results back to SFINA
     */
    private void getIpssACResults(){

        for(AclfBus bus : IpssNet.getBusList()) {
            Node SfinaNode = SfinaNet.getNode(bus.getId());
            
            SfinaNode.replacePropertyElement(PowerNodeState.VOLTAGE_MAGNITUDE, bus.getVoltageMag(Unit.UnitType.PU));
            SfinaNode.replacePropertyElement(PowerNodeState.VOLTAGE_ANGLE, bus.getVoltageAng(Unit.UnitType.Deg));
            
            if(bus.isSwing()){
                SfinaNode.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, bus.getNetGenResults().getReal()*IpssNet.getBaseMva());
                SfinaNode.replacePropertyElement(PowerNodeState.POWER_GENERATION_REACTIVE, bus.getNetGenResults().getImaginary()*IpssNet.getBaseMva());
            }
            if (bus.isGenPV()){
                SfinaNode.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, bus.getGenP()*IpssNet.getBaseMva());
                SfinaNode.replacePropertyElement(PowerNodeState.POWER_GENERATION_REACTIVE, bus.getGenQ()*IpssNet.getBaseMva());
            }
            
        }
        
        for(AclfBranch branch : IpssNet.getBranchList()){
            Link SfinaLink = SfinaNet.getLink(branch.getId());
            
            SfinaLink.replacePropertyElement(PowerLinkState.POWER_FLOW_FROM_REAL, branch.powerFrom2To(Unit.UnitType.mW).getReal());
            SfinaLink.replacePropertyElement(PowerLinkState.POWER_FLOW_FROM_REACTIVE, branch.powerFrom2To(Unit.UnitType.mVar).getImaginary());
            SfinaLink.replacePropertyElement(PowerLinkState.POWER_FLOW_TO_REAL, branch.powerTo2From(Unit.UnitType.mW).getReal());
            SfinaLink.replacePropertyElement(PowerLinkState.POWER_FLOW_TO_REACTIVE, branch.powerTo2From(Unit.UnitType.mVar).getImaginary());
            // Current
            SfinaLink.replacePropertyElement(PowerLinkState.CURRENT, branch.current(Unit.UnitType.Amp));            
            // Loss 
            double lossReal = Math.abs(Math.abs((Double)SfinaLink.getProperty(PowerLinkState.POWER_FLOW_TO_REAL)) - Math.abs((Double)SfinaLink.getProperty(PowerLinkState.POWER_FLOW_FROM_REAL)));
            double lossReactive = (Double)SfinaLink.getProperty(PowerLinkState.POWER_FLOW_TO_REACTIVE) + (Double)SfinaLink.getProperty(PowerLinkState.POWER_FLOW_FROM_REACTIVE);
            lossReactive = 0.0;
            //double lossReal = (Double)SfinaLink.getProperty(PowerLinkState.CURRENT)*(Double)SfinaLink.getProperty(PowerLinkState.CURRENT)*(Double)SfinaLink.getProperty(PowerLinkState.RESISTANCE);
            //double lossReactive = (Double)SfinaLink.getProperty(PowerLinkState.CURRENT)*(Double)SfinaLink.getProperty(PowerLinkState.CURRENT)*(Double)SfinaLink.getProperty(PowerLinkState.REACTANCE);
            SfinaLink.addProperty(PowerLinkState.LOSS_REAL, lossReal);
            SfinaLink.addProperty(PowerLinkState.LOSS_REACTIVE, lossReactive);
            
        }
        
    }
}
