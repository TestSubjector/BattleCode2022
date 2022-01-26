package OQualBot;

import battlecode.common.*;

public class Anomaly extends Util{
    private static AnomalyScheduleEntry[] anomalies;
    private static int anomalyHead;

    
    public static void initAnomaly(){
        anomalies = rc.getAnomalySchedule();
        anomalyHead = 0;
    }


    public static AnomalyScheduleEntry getNextAnomaly(){
        try{
            if (anomalyHead == anomalies.length) return null;
            AnomalyScheduleEntry anomaly = anomalies[anomalyHead];
            while(anomaly.roundNumber < rc.getRoundNum()){
                anomalyHead++;
                if (anomalyHead == anomalies.length){
                    anomaly = null;
                    break;
                }
                anomaly = anomalies[anomalyHead];
            }
            return anomaly;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public static void printAnomaly(AnomalyScheduleEntry anomaly){
        System.out.println("Anomaly Type: " + anomaly.anomalyType);
        System.out.println("Anomaly Round Number: " + anomaly.roundNumber);
    }


    public static void printAllAnomalies(){
        for (int i = 0; i < anomalies.length; ++i){
            printAnomaly(anomalies[i]);
        }
    }
}