package in.mobiux.android.orca50scanner.sgul.util;

import androidx.room.TypeConverter;

import java.sql.Date;

/**
 * Created by SUJEET KUMAR on 10-Mar-21.
 */
public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
