package util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** This class handles dates and time.
 * @author Nicholas Fallico
 */
public class DateTime {

    public static java.sql.Timestamp getTimeStamp() {
        ZoneId currentUserID = ZoneId.of("UTC");
        LocalDateTime currentUserDate = LocalDateTime.now(currentUserID);
        java.sql.Timestamp timeStamp = Timestamp.valueOf(currentUserDate);
        return timeStamp;
    }

    public static java.sql.Date getDate() {
        java.sql.Date date = java.sql.Date.valueOf(LocalDate.now());
        return date;
    }
}
