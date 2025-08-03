package com.example.partymaker.data.local;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.partymaker.data.model.ChatMessage;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.model.User;

/**
 * Room database for local caching of data. This improves performance by reducing network calls and
 * allowing offline access.
 */
@Database(
    entities = {Group.class, User.class, ChatMessage.class},
    version = 6, // Updated for encryption field schema fix in ChatMessage
    exportSchema = false) // Disable schema export to avoid build warnings
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

  private static final String TAG = "AppDatabase";
  private static volatile AppDatabase INSTANCE;

  /**
   * Gets the singleton instance of the database with proper migration support.
   *
   * @param context The application context
   * @return The database instance
   */
  public static AppDatabase getInstance(Context context) {
    if (INSTANCE == null) {
      synchronized (AppDatabase.class) {
        if (INSTANCE == null) {
          INSTANCE = createDatabase(context);
        }
      }
    }
    return INSTANCE;
  }

  /** Creates the database instance with comprehensive migration strategy */
  private static AppDatabase createDatabase(Context context) {
    return Room.databaseBuilder(
            context.getApplicationContext(), AppDatabase.class, "partymaker_database")
        // Add callback for database events
        .addCallback(databaseCallback)
        // Enable WAL mode for better performance
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        // Use destructive migration - this will recreate DB with correct schema
        .fallbackToDestructiveMigration()
        .fallbackToDestructiveMigrationOnDowngrade()
        // Force destructive migration from any version
        .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5)
        .build();
  }

  /** Database callback for monitoring and initialization */
  private static final RoomDatabase.Callback databaseCallback =
      new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
          super.onCreate(db);
          Log.i(TAG, "Database created for the first time");
          // Add any initial data setup here if needed
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
          super.onOpen(db);
          Log.d(TAG, "Database opened, version: " + db.getVersion());

          // Enable foreign keys
          db.execSQL("PRAGMA foreign_keys=ON");

          // Optimize performance
          db.execSQL("PRAGMA synchronous=NORMAL");
          db.execSQL("PRAGMA cache_size=10000");
          db.execSQL("PRAGMA temp_store=MEMORY");
        }

        @Override
        public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
          super.onDestructiveMigration(db);
          Log.w(TAG, "Destructive migration occurred - all data lost");
          DatabaseMigrations.MigrationCallback.onFallbackToDestructive(db.getVersion(), 6);
        }
      };

  /** Closes the database instance (for testing or app shutdown) */
  public static void closeDatabase() {
    if (INSTANCE != null) {
      INSTANCE.close();
      INSTANCE = null;
      Log.d(TAG, "Database closed");
    }
  }

  /** Forces database recreation (for testing purposes) */
  public static void recreateDatabase(Context context) {
    closeDatabase();
    context.deleteDatabase("partymaker_database");
    // Also delete any related files
    String[] filesToDelete = {
      "partymaker_database-shm", "partymaker_database-wal", "partymaker_database-journal"
    };
    for (String fileName : filesToDelete) {
      context.deleteDatabase(fileName);
    }
    Log.i(TAG, "Database and related files deleted");
  }

  // DAOs (Data Access Objects)
  public abstract GroupDao groupDao();

  public abstract UserDao userDao();

  public abstract ChatMessageDao chatMessageDao();
}
