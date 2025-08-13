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
    version = 7,
    exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

  private static final String TAG = "AppDatabase";
  private static final String DATABASE_NAME = "partymaker_database";
  private static final int DATABASE_VERSION = 7;
  private static final int CACHE_SIZE = 10000;
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
    return Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
        .addCallback(databaseCallback)
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        .fallbackToDestructiveMigration()
        .fallbackToDestructiveMigrationOnDowngrade()
        .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5, 6)
        .build();
  }

  /** Database callback for monitoring and initialization */
  private static final RoomDatabase.Callback databaseCallback =
      new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
          super.onCreate(db);
          Log.i(TAG, "Database created for the first time");
          createCustomIndexes(db);
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
          super.onOpen(db);
          Log.d(TAG, "Database opened, version: " + db.getVersion());

          enableForeignKeys(db);
          optimizeDatabasePerformance(db);
        }

        @Override
        public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
          super.onDestructiveMigration(db);
          Log.w(TAG, "Destructive migration occurred - all data lost");
          DatabaseMigrations.MigrationCallback.onFallbackToDestructive(
              db.getVersion(), DATABASE_VERSION);
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
    deleteDatabaseFiles(context);
    Log.i(TAG, "Database and related files deleted");
  }

  private static void createCustomIndexes(SupportSQLiteDatabase db) {
    // Create composite indexes for complex queries
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_group_search ON groups(group_name COLLATE NOCASE, group_location COLLATE NOCASE)");
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_message_unread ON chat_messages(groupKey, encrypted, timestamp)");
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_user_search ON users(username COLLATE NOCASE, email COLLATE NOCASE)");
    Log.d(TAG, "Custom indexes created");
  }

  private static void enableForeignKeys(SupportSQLiteDatabase db) {
    db.execSQL("PRAGMA foreign_keys=ON");
  }

  private static void optimizeDatabasePerformance(SupportSQLiteDatabase db) {
    db.execSQL("PRAGMA synchronous=NORMAL");
    db.execSQL("PRAGMA cache_size=" + CACHE_SIZE);
    db.execSQL("PRAGMA temp_store=MEMORY");
  }

  private static void deleteDatabaseFiles(Context context) {
    context.deleteDatabase(DATABASE_NAME);
    String[] filesToDelete = {
      DATABASE_NAME + "-shm", DATABASE_NAME + "-wal", DATABASE_NAME + "-journal"
    };
    for (String fileName : filesToDelete) {
      context.deleteDatabase(fileName);
    }
  }

  // DAOs (Data Access Objects)
  public abstract GroupDao groupDao();

  public abstract UserDao userDao();

  public abstract ChatMessageDao chatMessageDao();
}
