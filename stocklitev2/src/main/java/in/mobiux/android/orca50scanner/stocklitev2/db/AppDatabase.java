package in.mobiux.android.orca50scanner.stocklitev2.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import in.mobiux.android.orca50scanner.stocklitev2.db.dao.RFIDTagDao;
import in.mobiux.android.orca50scanner.stocklitev2.db.model.RFIDTag;
import in.mobiux.android.orca50scanner.stocklitev2.utils.Converters;

@Database(entities = {RFIDTag.class}, version = 32, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    public static AppDatabase instance;

    public abstract RFIDTagDao rfidTagDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "rfidtags.db").fallbackToDestructiveMigration().addCallback(roomCallback).build();
        }

        return instance;
    }


    private static Callback roomCallback = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
        }
    };
}
