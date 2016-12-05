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
package backend;

import core.SimulationAgent;
import core.SimulationAgentInterface;
import java.util.HashMap;
import network.FlowNetwork;
import org.apache.log4j.Logger;
import protopeer.BasePeerlet;
import protopeer.Peer;
import protopeer.measurement.MeasurementFileDumper;

/**
 *
 * @author evangelospournaras
 */
public abstract class FlowDomainAgent extends BasePeerlet{

    private static final Logger logger = Logger.getLogger(FlowDomainAgent.class);
    
    private FlowNetworkDataTypesInterface flowNetworkDataTypes;
    
    private MeasurementFileDumper measurementDumper;
    
    private HashMap<Enum,Object> domainParameters;
    
    public FlowDomainAgent(){
    }
    
    /**
    * Inititializes the flow domain agent
    *
    * @param peer the local peer
    */
    @Override
    public void init(Peer peer){
        super.init(peer);
    }
    
    /**
     * Calculate the flow in the network.
     * @param net
     * @return if calculation converged.
     */
    public abstract boolean flowAnalysis(FlowNetwork net);
    
    /**
     * Calculate the flow in the network. With new backend parameters.
     * @param net
     * @param backendParameters
     * @return if calculation converged.
     */
    public boolean flowAnalysis(FlowNetwork net, HashMap<Enum,Object> backendParameters){
        setDomainParameters(backendParameters);
        return flowAnalysis(net);
    }
            
    /**
     * Set which domain specific quantity is flow and capacity for nodes and links.
     * @param flowNetwork 
     */
    public abstract void setFlowParameters(FlowNetwork flowNetwork);
    
    /**
     * Call the domain specific backend parameter loader.
     * Should call method setDomainParameters at the end.
     * @param backendParamLocation 
     */
    public abstract void loadDomainParameters(String backendParamLocation);

    /**
     * Cast parameters from backend parameter HashMap to domain specific variables.
     */
    public abstract void extractDomainParameters();

    /**
     * @return the domainParameters
     */
    public HashMap<Enum,Object> getDomainParameters() {
        return domainParameters;
    }
    
    /**
     * Set the domain parameters in the current backend.
     * @param backendParameters
     */
    public void setDomainParameters(HashMap<Enum,Object> backendParameters){
        this.domainParameters=backendParameters;
        this.extractDomainParameters();
    }
    
    /**
     * @return the flowNetworkDataTypes
     */
    public FlowNetworkDataTypesInterface getFlowNetworkDataTypes() {
        if(flowNetworkDataTypes == null)
            logger.debug("Domain backend has to call setFlowNetworkDataTypes method, but probably didn't.");
        return flowNetworkDataTypes;
    }

    /**
     * @param flowNetworkDataTypes the flowNetworkDataTypes to set
     */
    public void setFlowNetworkDataTypes(FlowNetworkDataTypesInterface flowNetworkDataTypes) {
        this.flowNetworkDataTypes = flowNetworkDataTypes;
    }
    
    public String getParameterColumnSeparator(){
        return ((SimulationAgentInterface)this.getPeer().getPeerletOfType(SimulationAgentInterface.class)).getParameterColumnSeparator();
    }
    
}
