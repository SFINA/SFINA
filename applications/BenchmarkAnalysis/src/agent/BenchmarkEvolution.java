/*
 * Copyright (C) 2015 SFINA Team
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
package agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import utilities.Metrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import network.FlowNetwork;
import network.Link;
import network.Node;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.Logger;
import power.input.PowerNodeType;
import power.backend.PowerFlowType;
import power.backend.PowerBackendParameter;
import power.input.PowerNodeState;
import protopeer.measurement.MeasurementFileDumper;
import protopeer.measurement.MeasurementLog;
import protopeer.measurement.MeasurementLoggerListener;
import protopeer.util.quantities.Time;

/**
 * Strategy to make power load flow analysis converge. Balancing generation
 * limits and shedding load.
 *
 * @author Ben
 */
public class BenchmarkEvolution extends BenchmarkAnalysis {

    private static final Logger logger = Logger.getLogger(BenchmarkEvolution.class);
    public ArrayList<Integer> linktoIterations = new ArrayList<Integer>();

    public BenchmarkEvolution(String experimentID,
            Time bootstrapTime,
            Time runTime) {
        super(experimentID,
                bootstrapTime,
                runTime);
    }

    @Override
    public void runInitialOperations() {
        // inherited from BenchmarkSFINAAgent
        this.initMeasurementVariables();
        this.saveStartTime();

    }

    @Override
    public void runFinalOperations() {

        //storeCorrelationCoefficient();
        //storeCorrelationCoefficientAvgFlow();
        //storeCorrelationCoefficientFlowIncrease();

        //registers link-removed corresponding to iterations (In the replayer table, it is named as "Link Removed")
        for (int i = 0; i < getFlowNetwork().getLinks().size(); i++) {
            for (int j = 0; j < macroCount.get(i); j++) {
                linktoIterations.add(i + 1);
            }
        }
        // inherited from BenchmarkSFINAAgent
        this.calculateActivationStatus();
        this.calculateFlow();
        this.calculateUtilization();
        this.calculateTotalLines();
        this.saveSimuTime();
        this.saveIterationNumber();
        
        //rough output link
        try (
                PrintStream outPearson = new PrintStream(new File("output_link_status.txt"));) {

            //for (int m = 0; m < linkPerIteration.size(); m++) {
            for (int m = 0; m < linkStatusPerContingency.size(); m++) {
                String sc = "";
                for (int j = 0; j < linkStatusPerContingency.get(m).size(); j++) {
                    sc += linkStatusPerContingency.get(m).get(j) + " ";
                }

                outPearson.println(sc);
            }
            outPearson.close();

        } catch (FileNotFoundException p) {

            p.printStackTrace();
        }
//        
//        //rough output power
//        try (
//                PrintStream outPearson = new PrintStream(new File("output_power.txt"));) {
//
//            for (int m = 0; m < powerPerIteration.size(); m++) {
//                String sc = "";
//                for (int j = 0; j < powerPerIteration.get(m).size(); j++) {
//                    sc += powerPerIteration.get(m).get(j) + " ";
//                }
//
//                outPearson.println(sc);
//            }
//            outPearson.close();
//
//        } catch (FileNotFoundException p) {
//
//            p.printStackTrace();
//        }
        

    }

    private void storeCorrelationCoefficient() {
        //calculates correlation coefficient and stores in a file
        double[][] ei = new double[linkStatusPerContingency.size()][];

        for (int i = 0; i < linkStatusPerContingency.size(); i++) {
            ArrayList<Double> row = linkStatusPerContingency.get(i);

            double[] copy = new double[row.size()];
            for (int j = 0; j < row.size(); j++) {

                copy[j] = row.get(j);
            }

            ei[i] = copy;
        }

        PearsonsCorrelation pearson = new PearsonsCorrelation();
        RealMatrix corrDouble = pearson.computeCorrelationMatrix(ei);

        try (
                PrintStream outPearson = new PrintStream(new File("output_pearson.txt"));) {

            for (int m = 0; m < corrDouble.getRowDimension(); m++) {
                String sc = "";
                for (int j = 0; j < corrDouble.getRow(m).length; j++) {
                    sc += corrDouble.getEntry(m, j) + " ";
                }

                outPearson.println(sc);
            }
            outPearson.close();

        } catch (FileNotFoundException p) {

            p.printStackTrace();
        }
    }
    
    
    
    private void storeCorrelationCoefficientAvgFlow() {
        //calculates correlation coefficient and stores in a file
        double[][] ei_avg_flow = new double[avgflowStatusPerContingency.size()][];

        for (int i = 0; i < avgflowStatusPerContingency.size(); i++) {
            ArrayList<Double> row_flow = avgflowStatusPerContingency.get(i);

            double[] copy_flow = new double[row_flow.size()];
            for (int j = 0; j < row_flow.size(); j++) {

                copy_flow[j] = row_flow.get(j);
            }

            ei_avg_flow[i] = copy_flow;
        }

        PearsonsCorrelation pearson_avg_flow = new PearsonsCorrelation();
        RealMatrix corrDouble_avg_flow = pearson_avg_flow.computeCorrelationMatrix(ei_avg_flow);

        try (
                PrintStream outPearson_avg_flow = new PrintStream(new File("output_pearson_avg_flow.txt"));) {

            for (int m = 0; m < corrDouble_avg_flow.getRowDimension(); m++) {
                String sc_avg_flow = "";
                for (int j = 0; j < corrDouble_avg_flow.getRow(m).length; j++) {
                    sc_avg_flow += corrDouble_avg_flow.getEntry(m, j) + " ";
                }

                outPearson_avg_flow.println(sc_avg_flow);
            }
            outPearson_avg_flow.close();

        } catch (FileNotFoundException q) {

            q.printStackTrace();
        }
    }
    
        private void storeCorrelationCoefficientFlowIncrease() {
        //calculates correlation coefficient and stores in a file
        double[][] ei_avg_flow_increase = new double[avgflowIncreaseStatusPerContingency.size()][];

        for (int i = 0; i < avgflowIncreaseStatusPerContingency.size(); i++) {
            ArrayList<Double> row_increase = avgflowIncreaseStatusPerContingency.get(i);

            double[] copy_increase = new double[row_increase.size()];
            for (int j = 0; j < row_increase.size(); j++) {

                copy_increase[j] = row_increase.get(j);
            }

            ei_avg_flow_increase[i] = copy_increase;
        }

        PearsonsCorrelation pearson_avg_flow_increase = new PearsonsCorrelation();
        RealMatrix corrDouble_avg_flow_increase = pearson_avg_flow_increase.computeCorrelationMatrix(ei_avg_flow_increase);

        try (
                PrintStream outPearson_avg_flow_increase = new PrintStream(new File("output_pearson_avg_flow.txt"));) {

            for (int m = 0; m < corrDouble_avg_flow_increase.getRowDimension(); m++) {
                String sc_avg_flow_increase = "";
                for (int j = 0; j < corrDouble_avg_flow_increase.getRow(m).length; j++) {
                    sc_avg_flow_increase += corrDouble_avg_flow_increase.getEntry(m, j) + " ";
                }

                outPearson_avg_flow_increase.println(sc_avg_flow_increase);
            }
            outPearson_avg_flow_increase.close();

        } catch (FileNotFoundException r) {

            r.printStackTrace();
        }
    }

    @Override
    public boolean flowConvergenceStrategy(FlowNetwork flowNetwork) {
        boolean converged = false;
        switch (getDomain()) {
            case POWER:
                // blackout if isolated node
                if (flowNetwork.getNodes().size() == 1) {
                    logger.info("....not enough nodes");
                    return converged;
                }

                // or for example to get all generators and the slack bus if it exists
                ArrayList<Node> generators = new ArrayList();
                Node slack = null;
                for (Node node : flowNetwork.getNodes()) {
                    if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.GENERATOR)) {
                        generators.add(node);
                    }
                    if (node.getProperty(PowerNodeState.TYPE).equals(PowerNodeType.SLACK_BUS)) {
                        slack = node;
                    }
                }

                // To sort generators by max power output
                Collections.sort(generators, new Comparator<Node>() {
                    public int compare(Node node1, Node node2) {
                        return Double.compare((Double) node1.getProperty(PowerNodeState.POWER_MAX_REAL), (Double) node2.getProperty(PowerNodeState.POWER_MAX_REAL));
                    }
                }.reversed());

                // check if there's a slack in the island, if not make the generator with biggest power output to a slack bus
                if (slack == null) {
                    if (generators.size() == 0) {
                        logger.info("....no generator");
                        return converged; // blackout if no generator in island
                    } else {
                        slack = generators.get(0);
                        // this is how one changes node/link properties
                        slack.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.SLACK_BUS);
                        generators.remove(0);
                    }
                }

                boolean limViolation = true;
                while (limViolation) {
                    converged = callBackend(flowNetwork);
                    logger.info("....converged " + converged);
                    if (converged) {
                        limViolation = GenerationBalancing(flowNetwork, slack);

                        // Without the following line big cases (like polish) even DC doesn't converge..
                        PowerFlowType flowType = (PowerFlowType) getBackendParameters().get(PowerBackendParameter.FLOW_TYPE);
                        if (flowType.equals(PowerFlowType.DC)) {
                            limViolation = false;
                        }

                        if (limViolation) {
                            converged = false;
                            if (generators.size() > 0) { // make next bus a slack
                                slack = generators.get(0);
                                slack.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.SLACK_BUS);
                                generators.remove(0);
                            } else {
                                logger.info("....no more generators");
                                return false; // all generator limits were hit -> blackout
                            }
                        }
                    } else {
                        converged = loadShedding(flowNetwork);
                        if (!converged) {
                            return false; // blackout if no convergence after load shedding
                        }
                    }
                }

                break;
            case GAS:
                logger.debug("This domain is not supported at this moment");
                break;
            case WATER:
                logger.debug("This domain is not supported at this moment");
                break;
            case TRANSPORTATION:
                logger.debug("This domain is not supported at this moment");
                break;
            default:
                logger.debug("This domain is not supported at this moment");
        }
        return converged;
    }

    private boolean GenerationBalancing(FlowNetwork flowNetwork, Node slack) {
        boolean limViolation = false;
        if ((Double) slack.getProperty(PowerNodeState.POWER_GENERATION_REAL) > (Double) slack.getProperty(PowerNodeState.POWER_MAX_REAL)) {
            slack.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, slack.getProperty(PowerNodeState.POWER_MAX_REAL));
            limViolation = true;
        }
        if ((Double) slack.getProperty(PowerNodeState.POWER_GENERATION_REAL) < (Double) slack.getProperty(PowerNodeState.POWER_MIN_REAL)) {
            slack.replacePropertyElement(PowerNodeState.POWER_GENERATION_REAL, slack.getProperty(PowerNodeState.POWER_MIN_REAL));
            limViolation = true;
        }
        slack.replacePropertyElement(PowerNodeState.TYPE, PowerNodeType.GENERATOR);
        if (limViolation) {
            logger.info("....generator limit violated at node " + slack.getIndex());
        } else {
            logger.info("....no generator limit violated");
        }
        return limViolation;
    }

    private boolean loadShedding(FlowNetwork flowNetwork) {
        boolean converged = false;
        int loadIter = 0;
        int maxLoadShedIterations = 15; // according to paper
        double loadReductionFactor = 0.05; // 5%, according to paper
        while (!converged && loadIter < maxLoadShedIterations) {
            logger.info("....Doing load shedding at iteration " + loadIter);
            for (Node node : flowNetwork.getNodes()) {
                node.replacePropertyElement(PowerNodeState.POWER_DEMAND_REAL, (Double) node.getProperty(PowerNodeState.POWER_DEMAND_REAL) * (1.0 - loadReductionFactor));
                node.replacePropertyElement(PowerNodeState.POWER_DEMAND_REACTIVE, (Double) node.getProperty(PowerNodeState.POWER_DEMAND_REACTIVE) * (1.0 - loadReductionFactor));
            }
            converged = callBackend(flowNetwork);
            loadIter++;
        }
        return converged;
    }

    @Override
    public void scheduleMeasurements() {
        setMeasurementDumper(new MeasurementFileDumper(getPeersLogDirectory() + this.getExperimentID() + "/peer-" + getPeer().getIndexNumber()));
        getPeer().getMeasurementLogger().addMeasurementLoggerListener(new MeasurementLoggerListener() {
            public void measurementEpochEnded(MeasurementLog log, int epochNumber) {
                int simulationTime = getSimulationTime();
                if (simulationTime >= 1) {
                    log.logTagSet(simulationTime, new HashSet(getFlowNetwork().getLinks()), simulationTime);
                    //before 0 to 42 or 114
                    for (int i = 0; i < 203; i++) { //hardcoded because there is problem in time step for logreplayer
                        for (Link link : getFlowNetwork().getLinks()) {
                            HashMap<Metrics, Object> linkMetrics = getTemporalLinkMetrics().get(simulationTime).get(link.getIndex());
                            log.log(simulationTime, "utilization" + Integer.toString(i), ((Double) powerPerIteration.get(i).get(Integer.parseInt(link.getIndex()) - 1)) / link.getCapacity());
                            log.log(simulationTime, "power" + Integer.toString(i), ((Double) powerPerIteration.get(i).get(Integer.parseInt(link.getIndex()) - 1)));
                            log.log(simulationTime, "powerincrease" + Integer.toString(i), ((Double) powerIncreasePerIteration.get(i).get(Integer.parseInt(link.getIndex()) - 1)));
                            log.log(simulationTime, "link" + Integer.toString(i), ((Double) linkPerIteration.get(i).get(Integer.parseInt(link.getIndex()) - 1)));
                            log.log(simulationTime, Metrics.TOTAL_LINES, ((Double) linkMetrics.get(Metrics.TOTAL_LINES)));
                        }
                        log.log(simulationTime, "linkremoved" + Integer.toString(i), ((Integer) linktoIterations.get(i)));
                        log.log(simulationTime, "spectralRadius" + Integer.toString(i), ((Double) spectralRadius.get(i)));
                    }
                    HashMap<Metrics, Object> sysMetrics = getTemporalSystemMetrics().get(simulationTime);
                    log.log(simulationTime, Metrics.TOT_SIMU_TIME, ((Double) sysMetrics.get(Metrics.TOT_SIMU_TIME)));
                    log.log(simulationTime, Metrics.NEEDED_ITERATIONS, ((Integer) sysMetrics.get(Metrics.NEEDED_ITERATIONS)));
                }
                getMeasurementDumper().measurementEpochEnded(log, simulationTime);
                log.shrink(simulationTime, simulationTime + 1);

            }
        });
    }
}
