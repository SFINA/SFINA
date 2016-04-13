/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package replayer;

import utilities.Metrics;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import org.apache.log4j.Logger;
import protopeer.measurement.LogReplayer;
import protopeer.measurement.MeasurementLog;
import protopeer.measurement.MeasurementLoggerListener;

/**
 * Loads logs, calculates and prints measurement results.
 * @author Evangelos
 */
public class BenchmarkLogReplayer {

    private static final Logger logger = Logger.getLogger(BenchmarkLogReplayer.class);
    
    private String expSeqNum;
    private String expID;
    private String resultID;

    private LogReplayer replayer;
    private final String coma=",";

    private PrintWriter lineLossOut;
    private PrintWriter flowOut;
    private PrintWriter utilizationOut;
    private PrintWriter epochPowerLossOut;
    private PrintWriter totalPowerLossOut;
    private PrintWriter totalTimeOut;
    private PrintWriter iterations;
    private PrintWriter islandNum;
    private PrintWriter isolatedNodes;
    
    static boolean writeToFile=false;

    public BenchmarkLogReplayer(String experimentSequenceNumber, int minLoad, int maxLoad){
        this.expSeqNum=experimentSequenceNumber;
        this.expID="experiment-"+expSeqNum+"/";
        this.resultID="results/"+expSeqNum+"/";
        this.replayer=new LogReplayer();
        logger.info(expID);
        this.loadLogs("peerlets-log/"+expID, minLoad, maxLoad);
        if(writeToFile)
            this.prepareResultOutput();
        this.replayResults();
        if(writeToFile)
            this.closeFiles();
    }

    public static void main(String args[]){
        //BenchmarkLogReplayer replayer=new BenchmarkLogReplayer("Case30LineRemovalRandomConsistent", 0, 1000);
    }

    public void loadLogs(String directory, int minLoad, int maxLoad){
        try{
            File folder = new File(directory);
            File[] listOfFiles = folder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()&&!listOfFiles[i].isHidden()) {
                    MeasurementLog loadedLog=replayer.loadLogFromFile(directory+listOfFiles[i].getName());
                    MeasurementLog replayedLog=this.getMemorySupportedLog(loadedLog, minLoad, maxLoad);
                    replayer.mergeLog(replayedLog);
                }
                else
                    if (listOfFiles[i].isDirectory()) {
                        //do sth else
                    }
            }
        }
        catch(IOException io){

        }
        catch(ClassNotFoundException ex){

        }
    }
    
    private void prepareResultOutput(){
        try{
            File resultLocation = new File(resultID);
            resultLocation.mkdirs();
            
            lineLossOut = new PrintWriter(new BufferedWriter(new FileWriter(resultID+"lineLoss.txt", true)));
            flowOut = new PrintWriter(new BufferedWriter(new FileWriter(resultID+"flow.txt", true)));
            utilizationOut = new PrintWriter(new BufferedWriter(new FileWriter(resultID+"utilization.txt", true)));
            epochPowerLossOut = new PrintWriter(new BufferedWriter(new FileWriter(resultID+"epochPowLoss.txt", true)));
            totalPowerLossOut = new PrintWriter(new BufferedWriter(new FileWriter(resultID+"totPowLoss.txt", true)));
            totalTimeOut = new PrintWriter(new BufferedWriter(new FileWriter(resultID+"totalTime.txt", true)));
            iterations  = new PrintWriter(new BufferedWriter(new FileWriter(resultID+"iterations.txt", true)));
            islandNum  = new PrintWriter(new BufferedWriter(new FileWriter(resultID+"islands.txt", true)));
            isolatedNodes  = new PrintWriter(new BufferedWriter(new FileWriter(resultID+"isolatedNodes.txt", true)));
        }
        catch (IOException e) {
                //exception handling left as an exercise for the reader
        }
    }
    
    
    private void closeFiles(){
        lineLossOut.print("\n");
        flowOut.print("\n");
        utilizationOut.print("\n");
        epochPowerLossOut.print("\n");
        totalPowerLossOut.print("\n");
        totalTimeOut.print("\n");
        iterations.print("\n");
        islandNum.print("\n");
        isolatedNodes.print("\n");
        
        lineLossOut.close();
        flowOut.close();
        utilizationOut.close();
        epochPowerLossOut.close();
        totalPowerLossOut.close();
        totalTimeOut.close();
        iterations.close();
        islandNum.close();
        isolatedNodes.close();
    }

    public void replayResults(){
//        this.printGlobalMetricsTags();
//        this.calculatePeerResults(replayer.getCompleteLog());
        this.printLocalMetricsTags();
        replayer.replayTo(new MeasurementLoggerListener(){
            public void measurementEpochEnded(MeasurementLog log, int epochNumber){
                calculateEpochResults(log, epochNumber);
            }
        });
    }

    private void calculatePeerResults(MeasurementLog globalLog){
        
    }

    private void calculateEpochResults(MeasurementLog log, int epochNumber){
        double epochNum=epochNumber;
        double avgLineLossesPerEpoch=1-(log.getAggregateByEpochNumber(epochNumber, Metrics.ACTIVATED_LINES).getSum()/log.getAggregateByEpochNumber(epochNumber, Metrics.TOTAL_LINES).getSum());
        double avgFlowPerEpoch=log.getAggregateByEpochNumber(epochNumber, Metrics.LINE_FLOW).getAverage();
        double avgUtilizationPerEpoch=log.getAggregateByEpochNumber(epochNumber, Metrics.LINE_UTILIZATION).getAverage();
        double relPowerLossBetweenEpochs = 1.0-log.getAggregateByEpochNumber(epochNumber, Metrics.NODE_FINAL_LOADING).getSum()/log.getAggregateByEpochNumber(epochNumber, Metrics.NODE_INIT_LOADING).getSum();
        double relPowerLossSinceEpoch1 = 1.0-log.getAggregateByEpochNumber(epochNumber, Metrics.NODE_FINAL_LOADING).getSum()/log.getAggregateByEpochNumber(1, Metrics.NODE_INIT_LOADING).getSum();
        double simuTimePerEpoch = log.getAggregateByEpochNumber(epochNumber, Metrics.TOT_SIMU_TIME).getMax();
        double neededIterations = log.getAggregateByEpochNumber(epochNumber, Metrics.NEEDED_ITERATIONS).getMax();
        double islands = log.getAggregateByEpochNumber(epochNumber, Metrics.ISLANDS).getMax();
        double isolNodes = log.getAggregateByEpochNumber(epochNumber, Metrics.ISOLATED_NODES).getMax();
        
        if(writeToFile){
            lineLossOut.print(avgLineLossesPerEpoch + coma);
            flowOut.print(avgFlowPerEpoch + coma);
            utilizationOut.print(avgUtilizationPerEpoch + coma);
            epochPowerLossOut.print(relPowerLossBetweenEpochs + coma);
            totalPowerLossOut.print(relPowerLossSinceEpoch1 + coma);
            totalTimeOut.print(simuTimePerEpoch + coma);
            iterations.print(neededIterations + coma);
            islandNum.print(islands + coma);
            isolatedNodes.print(isolNodes + coma);
        }
        logger.info(String.format("%20.0f%20.2f%20.2f%20.2f%20.0f%20.0f%20.4f%20.4f%20.0f%20.0f\n",epochNum, avgLineLossesPerEpoch, avgFlowPerEpoch, avgUtilizationPerEpoch, simuTimePerEpoch, neededIterations, relPowerLossBetweenEpochs, relPowerLossSinceEpoch1, islands, isolNodes));
    }

    private MeasurementLog getMemorySupportedLog(MeasurementLog log, int minLoad, int maxLoad){
        return log.getSubLog(minLoad, maxLoad);
    }

    public void printGlobalMetricsTags(){
        logger.info("*** RESULTS PER PEER ***\n");
    }

    public void printLocalMetricsTags(){
        logger.info("*** RESULTS PER EPOCH ***\n");
        logger.info(String.format("%20s%20s%20s%20s%20s%20s%20s%20s%20s%20s\n", "# of Epoch","AVG lines failed","AVG Flow","AVG Utilization","Simu Time [ms]", "Nr of iterations", "Pow Loss this epoch", "Pow Loss since ep1", "Nr of islands", "Nr of isol. nodes"));
    }

    public double roundDecimals(double decimal, int decimalPlace) {
        BigDecimal bd = new BigDecimal(decimal);
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_UP);
        return bd.doubleValue();
    }

}
