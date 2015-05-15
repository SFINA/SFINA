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
    
    private double[][] matpf_bus;
    private double[][] matpf_gen;
    private double[][] matpf_bra;
    private double[][] matpf_gencost;
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
    
    /**
    * Returns bus 2-dim double array.
    * @return double 2-dim array.
    */
    public double[][] getBus() {
           return matpf_bus;
    }
    /**
    * Returns Generator 2-dim double array.
    * @return double 2-dim array.
    */	
    public double[][] getGen() {
           return matpf_gen;
    }
    /**
    * Returns Branch 2-dim double array.
    * @return double 2-dim array.
    */
    public double[][] getBranch() {
           return matpf_bra;
    }
    /**
    * Returns Generator Cost 2-dim double array.
    * @return double 2-dim array.
    */
    public double[][] getGencost() {
           return matpf_gencost;
    }
    
    @Override
    public void flowAnalysis(List<Node> nodes, List<Link> links){
        // Initialize local variables
        matpf_bus = this.getBuses(nodes);
        matpf_gen = this.getGenerators(nodes);
        matpf_bra = this.getBranches(links);
        matpf_gencost = this.getGenerationCosts(nodes);
        
        try{

            // Connect to Matlab
            MatlabProxy proxy = factory.getProxy();

            // Conversion object to send and retrieve arrays to and from matlab
            MatlabTypeConverter processor = new MatlabTypeConverter(proxy);

            // Send data to matlab to create matpower struct
            proxy.eval(caseFile + ".version = '2'");
            proxy.eval(caseFile + ".baseMVA = [100]");
            processor.setNumericArray(caseFile + ".bus", new MatlabNumericArray(matpf_bus, null));
            processor.setNumericArray(caseFile + ".gen", new MatlabNumericArray(matpf_gen, null));
            processor.setNumericArray(caseFile + ".branch", new MatlabNumericArray(matpf_bra, null));
            processor.setNumericArray(caseFile + ".gencost", new MatlabNumericArray(matpf_gencost, null));

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
            matpf_bra = processor.getNumericArray("result.branch").getRealArray2D();
            matpf_bus = processor.getNumericArray("result.bus").getRealArray2D();
            matpf_gen = processor.getNumericArray("result.gen").getRealArray2D();
            matpf_gencost = processor.getNumericArray("result.gencost").getRealArray2D();
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
    
    
}
