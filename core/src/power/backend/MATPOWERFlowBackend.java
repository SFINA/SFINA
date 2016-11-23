/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package power.backend;

import backend.FlowBackendInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;
import network.FlowNetwork;
import network.Link;
import network.Node;
import org.apache.log4j.Logger;
import power.input.PowerNodeType;
import power.input.PowerLinkState;
import power.input.PowerNodeState;

/**
 *
 * @author evangelospournaras
 */
public class MATPOWERFlowBackend implements FlowBackendInterface{
    
    private FlowNetwork net;
    private PowerFlowType powerFlowType;
    private double[][] busesPowerFlowInfo;
    private double[][] generatorsPowerFlowInfo;
    private double[][] branchesPowerFlowInfo;
    private double[][] costsPowerFlowInfo;
    private MatlabProxyFactory factory;
    private MatlabProxy proxy;
    private boolean converged;
    private final String caseFile="DumpCase";
    private static final Logger logger = Logger.getLogger(MATPOWERFlowBackend.class);
    
    
    public MATPOWERFlowBackend(HashMap<Enum,Object> backendParameters){
        MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder().setUsePreviouslyControlledSession(true).build();
        factory = new MatlabProxyFactory(options);
        this.converged=false;
        setBackendParameters(backendParameters);
    }
    
    @Override
    public void setBackendParameters(HashMap<Enum,Object> backendParameters){
        this.powerFlowType = (PowerFlowType)backendParameters.get(PowerBackendParameter.FLOW_TYPE);
    }
    
    @Override
    public boolean flowAnalysis(FlowNetwork net, HashMap<Enum,Object> backendParameters){
        setBackendParameters(backendParameters);
        return flowAnalysis(net);
    }
    
    @Override
    public boolean flowAnalysis(FlowNetwork net){
        // Initialize local variables
        this.net = net;
        
        ArrayList<Node> nodes = new ArrayList<Node>(this.net.getNodes());
        ArrayList<Link> links = new ArrayList<Link>(this.net.getLinks());
        busesPowerFlowInfo = this.getBuses(nodes);
        generatorsPowerFlowInfo = this.getGenerators(nodes);
        branchesPowerFlowInfo = this.getBranches(links);
        costsPowerFlowInfo = this.getGenerationCosts(nodes);
        
        try{
            // Connect to Matlab
            MatlabProxy proxy = factory.getProxy();

            // Conversion object to send and retrieve arrays to and from matlab
            MatlabTypeConverter processor = new MatlabTypeConverter(proxy);

            // Send data to matlab to create matpower struct
            proxy.eval(caseFile + ".version = '2'");
            proxy.eval(caseFile + ".baseMVA = [100]");
            processor.setNumericArray(caseFile + ".bus", new MatlabNumericArray(getBusesPowerFlowInfo(), null));
            processor.setNumericArray(caseFile + ".gen", new MatlabNumericArray(getGeneratorsPowerFlowInfo(), null));
            processor.setNumericArray(caseFile + ".branch", new MatlabNumericArray(getBranchesPowerFlowInfo(), null));
            processor.setNumericArray(caseFile + ".gencost", new MatlabNumericArray(getCostsPowerFlowInfo(), null));
            
            switch(powerFlowType){
                case AC:
                    proxy.eval("result = runpf(" + caseFile + ");");
                    break;
                case DC:
                    proxy.eval("result = rundcpf(" + caseFile + ");");
                    break;
                default:
                    logger.debug("Power flow type is not recognized.");
            }
            if(((double[]) proxy.getVariable("result.success"))[0]==0.0){
                this.converged=false;
            }
            else{
                this.converged=true;
            }

            // Get results
            branchesPowerFlowInfo = processor.getNumericArray("result.branch").getRealArray2D();
            busesPowerFlowInfo = processor.getNumericArray("result.bus").getRealArray2D();
            generatorsPowerFlowInfo = processor.getNumericArray("result.gen").getRealArray2D();
            costsPowerFlowInfo = processor.getNumericArray("result.gencost").getRealArray2D();
            this.updateNodes(nodes);
            this.updateLinks(links);
            // Disconnect proxy from Matlab session
            proxy.disconnect();
        }
        catch(MatlabConnectionException mcex){
            mcex.printStackTrace();
        }
        catch(MatlabInvocationException miex){
            miex.printStackTrace();
        }
        
        return converged;
    }
        
    private double[][] getBuses(List<Node> nodes){
        ArrayList MatpowerBusData = new ArrayList();
        ArrayList<String> neededBusData = new ArrayList();
        neededBusData.addAll(Arrays.asList(
            "BUS_I",
            "BUS_TYPE",
            "PD",
            "QD",
            "GS",
            "BS",
            "BUS_AREA",
            "VM",
            "VA",
            "BASE_KV",
            "ZONE",
            "VMAX",
            "VMIN")
        );
        for (Node node : nodes){
            ArrayList<Double> row = new ArrayList<>();
            for (String what : neededBusData){
                row.add(getNeededBusData(node, what));
            }
            MatpowerBusData.add(row);
        }
        return convertToDoubleArray(MatpowerBusData);
    }
    
    private double getNeededBusData(Node node, String what){
        switch(what){
            case "BUS_I":
                return Double.parseDouble(node.getIndex());
            case "BUS_TYPE":
                if (!node.isConnected())
                    return 4.0;
                else if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.BUS))
                    return 1.0;
                else if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.GENERATOR))
                    return 2.0;
                else if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.SLACK_BUS))
                    return 3.0;
                else 
                    logger.debug("Problem converting to Matpower Bus Type. May not work properly.");
            case "PD":
                return (Double)node.getProperty(PowerNodeState.POWER_DEMAND_REAL);
            case "QD":
                return (Double)node.getProperty(PowerNodeState.POWER_DEMAND_REACTIVE);
            case "GS":
                return (Double)node.getProperty(PowerNodeState.SHUNT_CONDUCT);
            case "BS":
                return (Double)node.getProperty(PowerNodeState.SHUNT_SUSCEPT);
            case "BUS_AREA":
                return (Double)node.getProperty(PowerNodeState.AREA);
            case "VM":
                return (Double)node.getProperty(PowerNodeState.VOLTAGE_MAGNITUDE);
            case "VA":
                return (Double)node.getProperty(PowerNodeState.VOLTAGE_ANGLE);
            case "BASE_KV":
                return (Double)node.getProperty(PowerNodeState.BASE_VOLTAGE);
            case "ZONE":
                return (Double)node.getProperty(PowerNodeState.ZONE);
            case "VMAX":
                return (Double)node.getProperty(PowerNodeState.VOLTAGE_MAX);
            case "VMIN":
                return (Double)node.getProperty(PowerNodeState.VOLTAGE_MIN);
            default:
                logger.debug("Problem converting to Matpower Bus Data. May not work properly.");
                return 0.0;
        }
    }

    private double[][] getGenerators(ArrayList<Node> nodes){
        ArrayList MatpowerGenData = new ArrayList();
        ArrayList<String> neededGenData = new ArrayList();
        neededGenData.addAll(Arrays.asList(
            "GEN_BUS",
            "PG",
            "QG",
            "QMAX",
            "QMIN",
            "VG",
            "MBASE",
            "GEN_STATUS",
            "PMAX",
            "PMIN",
            "PC1",
            "PC2",
            "QC1MIN",
            "QC1MAX",
            "QC2MIN",
            "QC2MAX",
            "RAMP_AGC",
            "RAMP_10",
            "RAMP_30",
            "RAMP_Q",
            "APF"
        ));
        for (Node node : nodes){
            if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.GENERATOR) || node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.SLACK_BUS)){
                ArrayList<Double> row = new ArrayList<>();
                for (String what : neededGenData){
                    row.add(getNeededGenData(node, what));
                }
                MatpowerGenData.add(row);
            }
        }
        return convertToDoubleArray(MatpowerGenData);
    }
    
    private double getNeededGenData(Node node, String what){
        switch(what){
            case "GEN_BUS":
                return Double.parseDouble(node.getIndex());
            case "PG":
                return (Double)node.getProperty(PowerNodeState.POWER_GENERATION_REAL);
            case "QG":
                return (Double)node.getProperty(PowerNodeState.POWER_GENERATION_REACTIVE);
            case "QMAX":
                return (Double)node.getProperty(PowerNodeState.POWER_MAX_REACTIVE);
            case "QMIN":
                return (Double)node.getProperty(PowerNodeState.POWER_MIN_REACTIVE);
            case "VG":
                return (Double)node.getProperty(PowerNodeState.VOLTAGE_SETPOINT);
            case "MBASE":
                return (Double)node.getProperty(PowerNodeState.MVA_BASE_TOTAL);
            case "GEN_STATUS":
                if (node.isActivated() == true)
                    return 1.0;
                else
                    return 0.0;
            case "PMAX":
                return (Double)node.getProperty(PowerNodeState.POWER_MAX_REAL);
            case "PMIN":
                return (Double)node.getProperty(PowerNodeState.POWER_MIN_REAL);
            case "PC1":
                return (Double)node.getProperty(PowerNodeState.PC1);
            case "PC2":
                return (Double)node.getProperty(PowerNodeState.PC2);
            case "QC1MIN":
                return (Double)node.getProperty(PowerNodeState.QC1_MIN);
            case "QC1MAX":
                return (Double)node.getProperty(PowerNodeState.QC1_MAX);
            case "QC2MIN":
                return (Double)node.getProperty(PowerNodeState.QC2_MIN);
            case "QC2MAX":
                return (Double)node.getProperty(PowerNodeState.QC2_MAX);
            case "RAMP_AGC":
                return (Double)node.getProperty(PowerNodeState.RAMP_AGC);
            case "RAMP_10":
                return (Double)node.getProperty(PowerNodeState.RAMP_10);
            case "RAMP_30":
                return (Double)node.getProperty(PowerNodeState.RAMP_30);
            case "RAMP_Q":
                return (Double)node.getProperty(PowerNodeState.RAMP_REACTIVE_POWER);
            case "APF":
                return (Double)node.getProperty(PowerNodeState.AREA_PART_FACTOR);
            default:
                logger.debug("Problem converting to Matpower Generator Data. May not work properly.");
                return 0.0;
        }
    }
    
//    private enum NeededGenData{
//        ID,
//        POWER_GENERATION_REAL,
//        POWER_GENERATION_REACTIVE,
//        POWER_MAX_REACTIVE,
//        POWER_MIN_REACTIVE,
//        VOLTAGE_SETPOINT,
//        MVA_BASE_TOTAL,
//        STATUS,             
//        POWER_MAX_REAL,
//        POWER_MIN_REAL,
//        PC1,        
//        PC2,
//        QC1_MIN,    
//        QC1_MAX,
//        QC2_MIN,
//        QC2_MAX,
//        RAMP_AGC,   
//        RAMP_10,    
//        RAMP_30,
//        RAMP_REACTIVE_POWER,
//        AREA_PART_FACTOR,
//}

    private double[][] getBranches(ArrayList<Link> links){
        ArrayList MatpowerBranchData = new ArrayList();
        ArrayList<String> neededBranchData = new ArrayList();
        neededBranchData.addAll(Arrays.asList(
            "F_BUS",
            "T_BUS",
            "BR_R",
            "BR_X",
            "BR_B",
            "RATE_A",
            "RATE_B",
            "RATE_C",
            "TAP",
            "SHIFT",
            "BR_STATUS",
            "ANGMIN",
            "ANGMAX"
        ));
        for(Link link : links){
            ArrayList<Double> row = new ArrayList<>();
            for(String what : neededBranchData){
                row.add(getNeededBranchData(link, what));
            }
            MatpowerBranchData.add(row);
        }
        return convertToDoubleArray(MatpowerBranchData);
    }
    
    private double getNeededBranchData(Link link, String what){
        switch(what){
            case "F_BUS":
                return Double.parseDouble(link.getStartNode().getIndex());
            case "T_BUS":
                return Double.parseDouble(link.getEndNode().getIndex());
            case "BR_R":
                return (Double)link.getProperty(PowerLinkState.RESISTANCE);
            case "BR_X":
                return (Double)link.getProperty(PowerLinkState.REACTANCE);
            case "BR_B":
                return (Double)link.getProperty(PowerLinkState.SUSCEPTANCE);
            case "RATE_A":
                return (Double)link.getProperty(PowerLinkState.RATE_A);
            case "RATE_B":
                return (Double)link.getProperty(PowerLinkState.RATE_B);
            case "RATE_C":
                return (Double)link.getProperty(PowerLinkState.RATE_C);
            case "TAP":
                return (Double)link.getProperty(PowerLinkState.TAP_RATIO);
            case "SHIFT":
                return (Double)link.getProperty(PowerLinkState.ANGLE_SHIFT);
            case "BR_STATUS":
                if (link.isActivated() == true)
                    return 1.0;
                else
                    return 0.0;
            case "ANGMIN":
                return (Double)link.getProperty(PowerLinkState.ANGLE_DIFFERENCE_MIN);
            case "ANGMAX":
                return (Double)link.getProperty(PowerLinkState.ANGLE_DIFFERENCE_MAX);
            default:
                logger.debug("Problem converting to Matpower Branch Data. May not work properly.");
                return 0.0;
        }
    }

    private double[][] getGenerationCosts(ArrayList<Node> nodes){
        ArrayList MatpowerGenCostData = new ArrayList();
        ArrayList<String> neededGenCostData = new ArrayList();
        neededGenCostData.addAll(Arrays.asList(
            "MODEL",
            "STARTUP",
            "SHUTDOWN",
            "NCOST",
            "COST1",
            "COST2",
            "COST3"
        ));
        for (Node node : nodes){
            if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.GENERATOR) || node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.SLACK_BUS)){
                ArrayList<Double> row = new ArrayList<>();
                for (String what : neededGenCostData){
                    row.add(getNeededGenCostData(node, what));       
                }
                MatpowerGenCostData.add(row);
            }
        }
        return convertToDoubleArray(MatpowerGenCostData);
    }
    
    private double getNeededGenCostData(Node node, String what){
        switch(what){
            case "MODEL":
                return (Double)node.getProperty(PowerNodeState.MODEL);
            case "STARTUP":
                return (Double)node.getProperty(PowerNodeState.STARTUP);
            case "SHUTDOWN":
                return (Double)node.getProperty(PowerNodeState.SHUTDOWN);
            case "NCOST":
                return (Double)node.getProperty(PowerNodeState.N_COST);
            case "COST1":
                return (Double)node.getProperty(PowerNodeState.COST_PARAM_1);
            case "COST2":
                return (Double)node.getProperty(PowerNodeState.COST_PARAM_2);
            case "COST3":
                return (Double)node.getProperty(PowerNodeState.COST_PARAM_3);
            default:
                logger.debug("Problem converting to Matpower Generator Cost Data. May not work properly.");
                return 0.0;
        }
    }
    
    private double[][] convertToDoubleArray(ArrayList<ArrayList<Double>> list){
        int rows = list.size();
        int cols = list.get(0).size();
        double[][] doubleArray = new double[rows][cols];
        for (int i=0; i < rows; i++){
            for (int j=0; j < cols; j++){
                if (list.get(i).size() != cols){
                    System.out.println("Problem with array dimension! Rows don't have same length! May not work properly.");
                }
                doubleArray[i][j] = list.get(i).get(j);
            }
        }
        return doubleArray;
    }    
    
    private void updateNodes(ArrayList<Node> nodes){
        //update with buses, generators and costs power flow info
        for (int i=0; i<this.busesPowerFlowInfo.length; i++){
            nodes.get(i).replacePropertyElement(PowerNodeState.VOLTAGE_MAGNITUDE, busesPowerFlowInfo[i][7]);
            nodes.get(i).replacePropertyElement(PowerNodeState.VOLTAGE_ANGLE, busesPowerFlowInfo[i][8]);
            if(powerFlowType.equals(PowerFlowType.DC))
                nodes.get(i).replacePropertyElement(PowerNodeState.POWER_DEMAND_REACTIVE, 0.0);

        }
        for (int i=0; i<this.generatorsPowerFlowInfo.length; i++){
            String busIndex = String.valueOf((int)generatorsPowerFlowInfo[i][0]);
            net.getNode(busIndex).replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, generatorsPowerFlowInfo[i][1]);
            if(powerFlowType.equals(PowerFlowType.AC))
                net.getNode(busIndex).replacePropertyElement(PowerNodeState.POWER_GENERATION_REACTIVE, generatorsPowerFlowInfo[i][2]);
            else {
                net.getNode(busIndex).replacePropertyElement(PowerNodeState.POWER_GENERATION_REACTIVE, 0.0);
                net.getNode(busIndex).replacePropertyElement(PowerNodeState.POWER_DEMAND_REACTIVE, 0.0);
            }
        }
    }
    
    private void updateLinks(ArrayList<Link> links){
        //update with branches power flow info
        for (int i=0; i<this.branchesPowerFlowInfo.length; i++){
            links.get(i).replacePropertyElement(PowerLinkState.POWER_FLOW_FROM_REAL, branchesPowerFlowInfo[i][13]);
            links.get(i).replacePropertyElement(PowerLinkState.POWER_FLOW_FROM_REACTIVE, branchesPowerFlowInfo[i][14]);
            links.get(i).replacePropertyElement(PowerLinkState.POWER_FLOW_TO_REAL, branchesPowerFlowInfo[i][15]);
            links.get(i).replacePropertyElement(PowerLinkState.POWER_FLOW_TO_REACTIVE, branchesPowerFlowInfo[i][16]);
            // Loss
            double lossReal = Math.abs(Math.abs((Double)links.get(i).getProperty(PowerLinkState.POWER_FLOW_TO_REAL)) - Math.abs((Double)links.get(i).getProperty(PowerLinkState.POWER_FLOW_FROM_REAL)));
            double lossReactive = (Double)links.get(i).getProperty(PowerLinkState.POWER_FLOW_TO_REACTIVE) + (Double)links.get(i).getProperty(PowerLinkState.POWER_FLOW_FROM_REACTIVE);
            lossReactive = 0.0;
            links.get(i).addProperty(PowerLinkState.LOSS_REAL, lossReal);
            links.get(i).addProperty(PowerLinkState.LOSS_REACTIVE, lossReactive);
            // Current to be calculated
            links.get(i).replacePropertyElement(PowerLinkState.CURRENT, 0.0); // To be calculated
        }
    }
    

    /**
     * @return the powerFlowType
     */
    public PowerFlowType getPowerFlowType() {
        return powerFlowType;
    }

    /**
     * @param powerFlowType the powerFlowType to set
     */
    public void setPowerFlowType(PowerFlowType powerFlowType) {
        this.powerFlowType = powerFlowType;
    }

    /**
     * @return the busesPowerFlowInfo
     */
    public double[][] getBusesPowerFlowInfo() {
        return busesPowerFlowInfo;
    }

    /**
     * @return the generatorsPowerFlowInfo
     */
    public double[][] getGeneratorsPowerFlowInfo() {
        return generatorsPowerFlowInfo;
    }

    /**
     * @return the branchesPowerFlowInfo
     */
    public double[][] getBranchesPowerFlowInfo() {
        return branchesPowerFlowInfo;
    }

    /**
     * @return the costsPowerFlowInfo
     */
    public double[][] getCostsPowerFlowInfo() {
        return costsPowerFlowInfo;
    }
}
