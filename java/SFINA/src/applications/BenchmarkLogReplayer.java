/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package applications;

import dsutil.protopeer.services.aggregation.AggregationFunction;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import protopeer.measurement.LogReplayer;
import protopeer.measurement.MeasurementLog;
import protopeer.measurement.MeasurementLoggerListener;

/**
 *
 * @author Evangelos
 */
public class BenchmarkLogReplayer {

    private final static String expSeqNum="01";
    private final static String expID="experiment-"+expSeqNum+"/";

    private LogReplayer replayer;
    private final String coma=",";


    public BenchmarkLogReplayer(String logsDir, int minLoad, int maxLoad){
        this.replayer=new LogReplayer();
        this.loadLogs(logsDir, minLoad, maxLoad);
        this.replayResults();
    }

    public static void main(String args[]){
        BenchmarkLogReplayer replayer=new BenchmarkLogReplayer("peerlets-log/"+expID, 0, 1000);
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
        double relLoadReductionBetweenEpochs = log.getAggregateByEpochNumber(epochNumber, Metrics.NODE_FINAL_LOADING).getSum()/log.getAggregateByEpochNumber(epochNumber, Metrics.NODE_INIT_LOADING).getSum();
        double avgflowSimuTimePerEpoch = log.getAggregateByEpochNumber(epochNumber, Metrics.SYSTEM_FLOW_SIMU_TIME).getAverage();
        
        System.out.format("%20.0f%20.2f%20.2f%20.2f%20.4f%20.0f\n",epochNum, avgLineLossesPerEpoch, avgFlowPerEpoch, avgUtilizationPerEpoch, relLoadReductionBetweenEpochs, avgflowSimuTimePerEpoch);
        
    }

    private MeasurementLog getMemorySupportedLog(MeasurementLog log, int minLoad, int maxLoad){
        return log.getSubLog(minLoad, maxLoad);
    }

    public void printGlobalMetricsTags(){
       System.out.println("*** RESULTS PER PEER ***\n");
    }

    public void printLocalMetricsTags(){
        System.out.println("*** RESULTS PER EPOCH ***\n");
        System.out.format("%20s%20s%20s%20s%20s%20s\n", "# of Epoch","AVG lines failed","AVG Flow","AVG Utilization", "Rel Load Loss", "Flow Simu Time (ms)");
    }

    public double roundDecimals(double decimal, int decimalPlace) {
        BigDecimal bd = new BigDecimal(decimal);
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_UP);
        return bd.doubleValue();
    }

}
