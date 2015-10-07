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
                    System.err.println(loadedLog.toString());
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
        double avgActivationStatusPerEpoch=log.getAggregateByEpochNumber(epochNumber, Metrics.ACTIVATION_STATUS).getAverage();
        double avgFlowPerEpoch=log.getAggregateByEpochNumber(epochNumber, Metrics.FLOW).getAverage();
        double avgUtilizationPerEpoch=log.getAggregateByEpochNumber(epochNumber, Metrics.UTILIZATION).getAverage();
        
        System.out.println(epochNum+coma+avgActivationStatusPerEpoch+coma+avgFlowPerEpoch+coma+avgUtilizationPerEpoch);
        
    }

    private MeasurementLog getMemorySupportedLog(MeasurementLog log, int minLoad, int maxLoad){
        return log.getSubLog(minLoad, maxLoad);
    }

    public void printGlobalMetricsTags(){
       System.out.println("*** RESULTS PER PEER ***\n");
    }

    public void printLocalMetricsTags(){
        System.out.println("*** RESULTS PER EPOCH ***\n");
        System.out.println("# of Epoch,AVG Activation Status,AVG Flow,AVG Utilization");
    }

    public double roundDecimals(double decimal, int decimalPlace) {
        BigDecimal bd = new BigDecimal(decimal);
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_UP);
        return bd.doubleValue();
    }

}
