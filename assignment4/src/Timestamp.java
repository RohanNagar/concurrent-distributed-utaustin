
public class Timestamp {
    int logicalClock;
    int pid;
    public Timestamp(int logicalClock, int pid) {
        this.logicalClock = logicalClock;
        this.pid = pid;
    }
    public static int compare(Timestamp a, Timestamp b) {

        if (a.logicalClock > b.logicalClock)
            return 1;
        if (a.logicalClock <  b.logicalClock)
            return -1;
        if (a.pid > b.pid) return 1;
        if (a.pid < b.pid)
            return -1;

        return 0;
    }
    public int getLogicalClock() {
        return logicalClock;
    }
    public int getPid() {
        return pid;
    }

}