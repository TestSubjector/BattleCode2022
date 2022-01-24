package OPreQualBot;

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
            AnomalyScheduleEntry anomaly = anomalies[anomalyHead];
            while(anomaly.roundNumber < rc.getRoundNum()){
                anomalyHead++;
                if (anomalyHead == anomalies.length){
                    anomaly = null;
                }
                anomaly = anomalies[anomalyHead];
            }
            return anomaly;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}