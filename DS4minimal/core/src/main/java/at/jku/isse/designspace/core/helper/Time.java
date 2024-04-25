package at.jku.isse.designspace.core.helper;

public class Time {
    private static long TIME=1;
    private static long PRESCRIBED_TIME=1;
    private static long nanoTimeOffset=System.nanoTime();
    private static boolean prescribedTime=false;
    private static boolean majorStepTime=false;
    private static boolean realTime = false;

    public static long currentTime() {
        if (prescribedTime)
            return PRESCRIBED_TIME;
        else if (majorStepTime)
            return TIME;
        else if (realTime)
            return System.nanoTime()-nanoTimeOffset;
        else
            return TIME++;
    }
    public static long nextMajorStepTime() {
        return TIME++;
    }
    public static void prescribeTime(long time) {
        PRESCRIBED_TIME=time;
        prescribedTime=true;
    }
    public static void unprescribeTime() {
        TIME=PRESCRIBED_TIME+1;
        prescribedTime=false;
    }
}
