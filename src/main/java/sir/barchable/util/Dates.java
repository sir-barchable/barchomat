package sir.barchable.util;

/**
 * @author Sir Barchable
 *         Date: 2/05/15
 */
public class Dates {
    /**
     * Format an interval
     * @param s the interval in seconds
     * @return the interval, formatted "d day(s) hh:mm:ss"
     */
    public static String formatInterval(int s) {
        boolean neg = s < 0;
        s = Math.abs(s);
        int d = s / 86400;
        int h = s % 86400 / 3600;
        int m = s % 3600/ 60;
        s = s % 60;
        StringBuilder buffer = new StringBuilder();
        if (neg) {
            buffer.append('-');
        }
        if (d > 0) {
            buffer.append(d).append(d == 1 ? " day " : " days ");
        }
        buffer.append(String.format("%02d:%02d:%02d", h, m, s));
        return buffer.toString();
    }
}
