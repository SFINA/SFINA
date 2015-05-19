/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package power.flow_analysis;

import flow_analysis.FlowAnalysisInterface;
import java.util.List;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;
import network.Link;
import network.Node;
import org.apache.log4j.Logger;
import power.PowerFlowType;
import power.PowerNodeType;
import power.input.PowerNodeState;

/**
 *
 * @author evangelospournaras
 */
public class MATPOWERPowerFlowAnalysis implements FlowAnalysisInterface{
    
    private double[][] busesPowerFlowInfo;
    private double[][] generatorsPowerFlowInfo;
    private double[][] branchesPowerFlowInfo;
    private double[][] costsPowerFlowInfo;
    private MatlabProxyFactory factory;
    private MatlabProxy proxy;
    private PowerFlowType powerFlowType;
    private boolean converged;
    private final String caseFile="dump-case";
    private static final Logger logger = Logger.getLogger(MATPOWERPowerFlowAnalysis.class);
    
    
    public MATPOWERPowerFlowAnalysis(PowerFlowType powerFlowType){
        MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder().setUsePreviouslyControlledSession(true).build();
        factory = new MatlabProxyFactory(options);
        this.powerFlowType=powerFlowType;
        this.converged=false;
    }
    
    @Override
    public void flowAnalysis(List<Node> nodes, List<Link> links){
        // Initialize local variables
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
                case DC:
                    proxy.eval("result = rundcpf(" + caseFile + ");");
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
        for(Node node:nodes){
            if(node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.BUS)){
                
            }
        }
        return null;
    }
    
    private double[][] getGenerators(List<Node> nodes){
        return null;
    }
    
    private double[][] getBranches(List<Link> links){
        return null;
    }
    
    private double[][] getGenerationCosts(List<Node> nodes){
        return null;
    }
    
    private void updateNodes(List<Node> nodes){
        //update with buses, generators and costs power flow info
    }
    
    private void updateLinks(List<Link> links){
        //update with branches power flow info
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
