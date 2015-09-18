/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package power.flow_analysis;

import flow_analysis.FlowBackendInterface;
import java.util.ArrayList;
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
import power.PowerFlowType;
import power.PowerNodeType;
import power.input.PowerLinkState;
import power.input.PowerNodeState;

/**
 *
 * @author evangelospournaras
 */
public class MATPOWERFlowBackend implements FlowBackendInterface{
    
    private FlowNetwork net;
    private double[][] busesPowerFlowInfo;
    private double[][] generatorsPowerFlowInfo;
    private double[][] branchesPowerFlowInfo;
    private double[][] costsPowerFlowInfo;
    private MatlabProxyFactory factory;
    private MatlabProxy proxy;
    private PowerFlowType powerFlowType;
    private boolean converged;
    private final String caseFile="DumpCase";
    private static final Logger logger = Logger.getLogger(MATPOWERFlowBackend.class);
    
    
    public MATPOWERFlowBackend(PowerFlowType powerFlowType){
        MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder().setUsePreviouslyControlledSession(true).build();
        factory = new MatlabProxyFactory(options);
        this.powerFlowType=powerFlowType;
        this.converged=false;
    }
    
    @Override
    public void flowAnalysis(FlowNetwork net){
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
    }
        
    private double[][] getBuses(List<Node> nodes){
        ArrayList MatpowerBusData = new ArrayList();
        for (Node node : nodes){
            ArrayList<Double> row = new ArrayList<>();
            for (NeededBusData BusState : NeededBusData.values()){
                switch (BusState){
                    case ID: 
                        row.add(Double.parseDouble(node.getIndex()));
                        break;
                    case TYPE:
                        if (!node.isConnected())
                            row.add(4.0);
                        else if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.BUS))
                            row.add(1.0);
                        else if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.GENERATOR))
                            row.add(2.0);
                        else if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.SLACK_BUS))
                            row.add(3.0);
                        else 
                            System.out.println("Problem converting to Matpower Bus Type! May not work properly.");
                        break;
                    default:
                        row.add((Double)node.getProperty(PowerNodeState.valueOf(BusState.toString())));
                }
            }
            MatpowerBusData.add(row);
        }
        return convertToDoubleArray(MatpowerBusData);
    }
    
    private enum NeededBusData {
        ID,
        TYPE,
        REAL_POWER_DEMAND,
        REACTIVE_POWER_DEMAND,
        SHUNT_CONDUCT,
        SHUNT_SUSCEPT,
        AREA,
        VOLTAGE_MAGNITUDE,
        VOLTAGE_ANGLE,
        BASE_VOLTAGE,
        ZONE,
        VOLTAGE_MAX,
        VOLTAGE_MIN,
    }
    
    private double[][] getGenerators(ArrayList<Node> nodes){
        ArrayList MatpowerGenData = new ArrayList();
        for (Node node : nodes){
            if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.GENERATOR) || node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.SLACK_BUS)){
                ArrayList<Double> row = new ArrayList<>();
                for (NeededGenData GenState : NeededGenData.values()){
                    switch (GenState){
                        case ID: 
                            row.add(Double.parseDouble(node.getIndex()));
                            break;
                        case STATUS:
                            if (node.isActivated() == true)
                                row.add(1.0);
                            else
                                row.add(0.0);
                            break;
                        default:
                            row.add((Double)node.getProperty(PowerNodeState.valueOf(GenState.toString())));       
                    }
                }
                MatpowerGenData.add(row);
            }
        }
        return convertToDoubleArray(MatpowerGenData);
    }
    
    private enum NeededGenData{
        ID,
        REAL_POWER_GENERATION,
        REACTIVE_POWER_GENERATION,
        REACTIVE_POWER_MAX,
        REACTIVE_POWER_MIN,
        VOLTAGE_SETPOINT,
        TOTAL_MVA_BASE,
        STATUS,             
        REAL_POWER_MAX,
        REAL_POWER_MIN,
        PC1,        
        PC2,
        QC1_MIN,    
        QC1_MAX,
        QC2_MIN,
        QC2_MAX,
        RAMP_AGC,   
        RAMP_10,    
        RAMP_30,
        RAMP_REACTIVE_POWER,
        AREA_PART_FACTOR,
}
    
    private double[][] getBranches(ArrayList<Link> links){
        ArrayList MatpowerBranchData = new ArrayList();
        for(Link link : links){
            ArrayList<Double> row = new ArrayList<>();
            for(NeededBranchData BranchState : NeededBranchData.values()){
                switch (BranchState){
                    case FROM_BUS: 
                        row.add(Double.parseDouble(link.getStartNode().getIndex()));
                        break;
                    case TO_BUS:
                        row.add(Double.parseDouble(link.getEndNode().getIndex()));
                        break;
                    case STATUS:
                        if (link.isActivated() == true)
                            row.add(1.0);
                        else
                            row.add(0.0);
                        break;
                    default:
                        row.add((Double)link.getProperty(PowerLinkState.valueOf(BranchState.toString())));
                }
            }
            MatpowerBranchData.add(row);
        }
        return convertToDoubleArray(MatpowerBranchData);
    }
    
    private enum NeededBranchData {
        FROM_BUS,   // !
        TO_BUS,     // !
        RESISTANCE,
        REACTANCE,
        SUSCEPTANCE,
        RATE_A, 
        RATE_B, 
        RATE_C, 
        TAP_RATIO,
        ANGLE_SHIFT,
        STATUS,     // !
        ANGLE_DIFFERENCE_MIN,
        ANGLE_DIFFERENCE_MAX,
    }
    
    private double[][] getGenerationCosts(ArrayList<Node> nodes){
        ArrayList MatpowerGenCostData = new ArrayList();
        for (Node node : nodes){
            if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.GENERATOR) || node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.SLACK_BUS)){
                ArrayList<Double> row = new ArrayList<>();
                for (NeededGenCostData GenCostState : NeededGenCostData.values()){
                    row.add((Double)node.getProperty(PowerNodeState.valueOf(GenCostState.toString())));       
                }
                MatpowerGenCostData.add(row);
            }
        }
        return convertToDoubleArray(MatpowerGenCostData);
    }
    
    private enum NeededGenCostData {
        MODEL,      
        STARTUP,    
        SHUTDOWN,
        N_COST,     
        COST_PARAM_1,
        COST_PARAM_2,
        COST_PARAM_3,
    }
    
    private double[][] convertToDoubleArray(ArrayList<ArrayList<Double>> list){
        int rows = list.size();
        int cols = list.get(1).size();
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
        }
        for (int i=0; i<this.generatorsPowerFlowInfo.length; i++){
            String busIndex = String.valueOf((int)generatorsPowerFlowInfo[i][0]);
            net.getNode(busIndex).replacePropertyElement(PowerNodeState.REAL_POWER_GENERATION, generatorsPowerFlowInfo[i][1]);
            net.getNode(busIndex).replacePropertyElement(PowerNodeState.REACTIVE_POWER_GENERATION, generatorsPowerFlowInfo[i][2]);
        }
    }
    
    private void updateLinks(ArrayList<Link> links){
        //update with branches power flow info
        for (int i=0; i<this.branchesPowerFlowInfo.length; i++){
            links.get(i).replacePropertyElement(PowerLinkState.REAL_POWER_FLOW_FROM, branchesPowerFlowInfo[i][13]);
            links.get(i).replacePropertyElement(PowerLinkState.REACTIVE_POWER_FLOW_FROM, branchesPowerFlowInfo[i][14]);
            links.get(i).replacePropertyElement(PowerLinkState.REAL_POWER_FLOW_TO, branchesPowerFlowInfo[i][15]);
            links.get(i).replacePropertyElement(PowerLinkState.REACTIVE_POWER_FLOW_TO, branchesPowerFlowInfo[i][16]);
            // Loss
            double lossReal = Math.abs(Math.abs((Double)links.get(i).getProperty(PowerLinkState.REAL_POWER_FLOW_TO)) - Math.abs((Double)links.get(i).getProperty(PowerLinkState.REAL_POWER_FLOW_FROM)));
            double lossReactive = (Double)links.get(i).getProperty(PowerLinkState.REACTIVE_POWER_FLOW_TO) + (Double)links.get(i).getProperty(PowerLinkState.REACTIVE_POWER_FLOW_FROM);
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
     * @return the converged
     */
    public boolean isConverged() {
        return converged;
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
