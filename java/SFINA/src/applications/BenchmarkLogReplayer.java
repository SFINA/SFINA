/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package applications;

import dsutil.protopeer.services.aggregation.AggregationFunction;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import protopeer.measurement.LogReplayer;
import protopeer.measurement.MeasurementLog;
import protopeer.measurement.MeasurementLoggerListener;

/**
 *
 * @author Evangelos
 */
public class BenchmarkLogReplayer {

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
    private PrintWriter flowTimeOut;
    private PrintWriter totalTimeOut;
    private PrintWriter iterations;

    public BenchmarkLogReplayer(String experimentSequenceNumber, int minLoad, int maxLoad){
        this.expSeqNum=experimentSequenceNumber;
        this.expID="experiment-"+expSeqNum+"/";
        this.resultID="results/"+expSeqNum+"/";
        this.replayer=new LogReplayer();
        System.out.println(expID);
        this.loadLogs("peerlets-log/"+expID, minLoad, maxLoad);
        this.prepareResultOutput();
        this.replayResults();
        this.closeFiles();
    }

    public static void main(String args[]){
        BenchmarkLogReplayer replayer=new BenchmarkLogReplayer("RateReductionReloadCase30", 0, 1000);
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
            flowTimeOut = new PrintWriter(new BufferedWriter(new FileWriter(resultID+"flowTime.txt", true)));
            totalTimeOut = new PrintWriter(new BufferedWriter(new FileWriter(resultID+"totalTime.txt", true)));
            iterations  = new PrintWriter(new BufferedWriter(new FileWriter(resultID+"iterations.txt", true)));
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
        flowTimeOut.print("\n");
        totalTimeOut.print("\n");
        iterations.print("\n");
        
        lineLossOut.close();
        flowOut.close();
        utilizationOut.close();
        epochPowerLossOut.close();
        totalPowerLossOut.close();
        flowTimeOut.close();
        totalTimeOut.close();
        iterations.close();
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
        double avgflowSimuTimePerEpoch = log.getAggregateByEpochNumber(epochNumber, Metrics.SYSTEM_FLOW_SIMU_TIME).getAverage();
        double simuTimePerEpoch = log.getAggregateByEpochNumber(epochNumber, Metrics.SYSTEM_TOT_SIMU_TIME).getMax();
        double neededIterations = log.getAggregateByEpochNumber(epochNumber, Metrics.NEEDED_ITERATIONS).getMax();
        
        lineLossOut.print(avgLineLossesPerEpoch + coma);
        flowOut.print(avgFlowPerEpoch + coma);
        utilizationOut.print(avgUtilizationPerEpoch + coma);
        epochPowerLossOut.print(relPowerLossBetweenEpochs + coma);
        totalPowerLossOut.print(relPowerLossSinceEpoch1 + coma);
        flowTimeOut.print(avgflowSimuTimePerEpoch + coma);
        totalTimeOut.print(simuTimePerEpoch + coma);
        iterations.print(neededIterations + coma);
        
        System.out.format("%20.0f%20.2f%20.2f%20.2f%20.4f%20.4f%20.0f%20.0f%20.0f\n",epochNum, avgLineLossesPerEpoch, avgFlowPerEpoch, avgUtilizationPerEpoch, relPowerLossBetweenEpochs, relPowerLossSinceEpoch1, avgflowSimuTimePerEpoch, simuTimePerEpoch, neededIterations);
        
    }

    private MeasurementLog getMemorySupportedLog(MeasurementLog log, int minLoad, int maxLoad){
        return log.getSubLog(minLoad, maxLoad);
    }

    public void printGlobalMetricsTags(){
       System.out.println("*** RESULTS PER PEER ***\n");
    }

    public void printLocalMetricsTags(){
        System.out.println("*** RESULTS PER EPOCH ***\n");
        System.out.format("%20s%20s%20s%20s%20s%20s%20s%20s%20s\n", "# of Epoch","AVG lines failed","AVG Flow","AVG Utilization", "Pow Loss this epoch", "Pow Loss since ep1", "Avg Flow Simu Time", "Total Simu Time", "Nr of iterations");
    }

    public double roundDecimals(double decimal, int decimalPlace) {
        BigDecimal bd = new BigDecimal(decimal);
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_UP);
        return bd.doubleValue();
    }

}
