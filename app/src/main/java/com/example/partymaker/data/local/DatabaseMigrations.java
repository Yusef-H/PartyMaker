package com.example.partymaker.data.local;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Database migration strategies for Room database. Handles schema changes while preserving user
 * data. Each migration is carefully designed to maintain data integrity during upgrades.
 *
 * @noinspection ALL
 */
public class DatabaseMigrations {
  private static final String TAG = "DatabaseMigrations";

  // Database version constants
  private static final int VERSION_1 = 1;
  private static final int VERSION_2 = 2;
  private static final int VERSION_3 = 3;
  private static final int VERSION_4 = 4;
  private static final int VERSION_5 = 5;
  private static final int VERSION_6 = 6;

  // Default values for new columns
  private static final String DEFAULT_INTEGER_FALSE = "0";
  private static final String DEFAULT_INTEGER_TRUE = "1";
  private static final String DEFAULT_TEXT_EMPTY = "''";
  private static final String DEFAULT_MESSAGE_TYPE = "'TEXT'";
  private static final String DEFAULT_GROUP_TYPE = "'GENERAL'";
  private static final String DEFAULT_CATEGORY = "'OTHER'";
  private static final int DEFAULT_MAX_PARTICIPANTS = -1;
  private static final double DEFAULT_GROUP_PRICE = 0.0;

  // Table names
  private static final String TABLE_GROUPS = "groups";
  private static final String TABLE_USERS = "users";
  private static final String TABLE_CHAT_MESSAGES = "chat_messages";
  private static final String TABLE_USER_PREFERENCES = "user_preferences";

  // Prevent instantiation
  private DatabaseMigrations() {
    // Utility class for database migrations
  }

  /** Migration from version 1 to 2 Example: Adding a new column to existing tables */
  public static final Migration MIGRATION_1_2 =
      new Migration(VERSION_1, VERSION_2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
          try {
            Log.d(TAG, "Starting migration from version 1 to 2");

            // Add new columns with proper defaults
            addColumnToTable(
                database,
                TABLE_GROUPS,
                "isPrivate",
                "INTEGER NOT NULL DEFAULT " + DEFAULT_INTEGER_FALSE);
            addColumnToTable(
                database,
                TABLE_USERS,
                "lastActiveTime",
                "INTEGER NOT NULL DEFAULT " + DEFAULT_INTEGER_FALSE);
            addColumnToTable(
                database,
                TABLE_CHAT_MESSAGES,
                "messageType",
                "TEXT NOT NULL DEFAULT " + DEFAULT_MESSAGE_TYPE);

            // Create indexes for better performance
            createIndexIfNotExists(database, "index_groups_isPrivate", TABLE_GROUPS, "isPrivate");
            createIndexIfNotExists(
                database, "index_users_lastActiveTime", TABLE_USERS, "lastActiveTime");
            createIndexIfNotExists(
                database, "index_chat_messages_messageType", TABLE_CHAT_MESSAGES, "messageType");

            Log.d(TAG, "Successfully migrated from version 1 to 2");

          } catch (Exception e) {
            Log.e(TAG, "Error during migration 1->2", e);
            throw e; // Re-throw to trigger fallback
          }
        }
      };

  /** Migration from version 4 to 5: Adding encryption field to ChatMessage */
  public static final Migration MIGRATION_4_5 =
      new Migration(VERSION_4, VERSION_5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
          try {
            Log.d(TAG, "Starting migration from version 4 to 5 - Adding encryption support");

            // Add encrypted column to chat_messages table - without default to match Room
            // expectations
            database.execSQL(
                "ALTER TABLE chat_messages ADD COLUMN encrypted INTEGER NOT NULL DEFAULT 0");

            // Update the table schema to match Room expectations by recreating without explicit
            // default
            database.execSQL(
                "CREATE TABLE chat_messages_new ("
                    + "messageKey TEXT PRIMARY KEY NOT NULL, "
                    + "groupKey TEXT, "
                    + "senderKey TEXT, "
                    + "senderName TEXT, "
                    + "message TEXT, "
                    + "timestamp INTEGER NOT NULL, "
                    + "imageUrl TEXT, "
                    + "encrypted INTEGER NOT NULL, "
                    + "metadata TEXT, "
                    + "groupId TEXT, "
                    + "messageContent TEXT, "
                    + "messageText TEXT, "
                    + "messageTime TEXT, "
                    + "messageUser TEXT)");

            // Copy data from old table to new, setting encrypted = 0 for existing messages
            database.execSQL(
                "INSERT INTO chat_messages_new SELECT "
                    + "messageKey, groupKey, senderKey, senderName, message, timestamp, imageUrl, "
                    + "COALESCE(encrypted, 0) as encrypted, metadata, groupId, messageContent, messageText, messageTime, messageUser "
                    + "FROM chat_messages");

            // Drop old table and rename new one
            database.execSQL("DROP TABLE chat_messages");
            database.execSQL("ALTER TABLE chat_messages_new RENAME TO chat_messages");

            // Create index for encrypted field for better query performance
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_chat_messages_encrypted ON chat_messages(encrypted)");

            Log.d(TAG, "Successfully migrated from version 4 to 5 - Encryption field added");

          } catch (Exception e) {
            Log.e(TAG, "Error during migration 4->5", e);
            throw e; // Re-throw to trigger fallback
          }
        }
      };

  /** Migration from version 5 to 6: Fix schema validation for encrypted field */
  public static final Migration MIGRATION_5_6 =
      new Migration(VERSION_5, VERSION_6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
          try {
            Log.d(TAG, "Starting migration from version 5 to 6 - Fixing encrypted field schema");

            // No actual schema changes needed - this is just to fix Room validation
            // The encrypted field should already exist from migration 4->5

            Log.d(TAG, "Successfully migrated from version 5 to 6 - Schema validation fixed");

          } catch (Exception e) {
            Log.e(TAG, "Error during migration 5->6", e);
            throw e; // Re-throw to trigger fallback
          }
        }
      };

  /** Migration from version 2 to 3 Example: Adding new tables and relationships */
  public static final Migration MIGRATION_2_3 =
      new Migration(VERSION_2, VERSION_3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
          try {
            Log.d(TAG, "Starting migration from version 2 to 3");

            // Example: Create new table for user preferences
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS user_preferences ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "userId TEXT NOT NULL, "
                    + "preferenceKey TEXT NOT NULL, "
                    + "preferenceValue TEXT, "
                    + "createdAt INTEGER NOT NULL, "
                    + "updatedAt INTEGER NOT NULL, "
                    + "FOREIGN KEY(userId) REFERENCES users(userKey) ON DELETE CASCADE"
                    + ")");

            // Create unique index for user preferences
            database.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_user_preferences_user_key "
                    + "ON user_preferences(userId, preferenceKey)");

            // Example: Add new columns for group management
            database.execSQL("ALTER TABLE groups ADD COLUMN maxParticipants INTEGER DEFAULT -1");
            database.execSQL(
                "ALTER TABLE groups ADD COLUMN requiresApproval INTEGER NOT NULL DEFAULT 0");

            Log.d(TAG, "Successfully migrated from version 2 to 3");

          } catch (Exception e) {
            Log.e(TAG, "Error during migration 2->3", e);
            throw e;
          }
        }
      };

  /** Migration from version 3 to 4 Example: Data transformation and cleanup */
  public static final Migration MIGRATION_3_4 =
      new Migration(VERSION_3, VERSION_4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
          try {
            Log.d(TAG, "Starting migration from version 3 to 4");

            // Example: Create temporary table for data transformation
            database.execSQL(
                "CREATE TABLE groups_new ("
                    + "groupKey TEXT PRIMARY KEY NOT NULL, "
                    + "groupName TEXT NOT NULL, "
                    + "adminKey TEXT NOT NULL, "
                    + "groupLocation TEXT, "
                    + "groupDays TEXT, "
                    + "groupMonths TEXT, "
                    + "groupYears TEXT, "
                    + "groupHours TEXT, "
                    + "createdAt INTEGER NOT NULL, "
                    + "groupPrice REAL NOT NULL DEFAULT 0.0, "
                    + "groupType TEXT NOT NULL DEFAULT 'GENERAL', "
                    + "canAdd INTEGER NOT NULL DEFAULT 1, "
                    + "friendKeys TEXT, "
                    + "comingKeys TEXT, "
                    + "messageKeys TEXT, "
                    + "isPrivate INTEGER NOT NULL DEFAULT 0, "
                    + "maxParticipants INTEGER DEFAULT -1, "
                    + "requiresApproval INTEGER NOT NULL DEFAULT 0, "
                    + "description TEXT, "
                    + "category TEXT NOT NULL DEFAULT 'OTHER'"
                    + ")");

            // Copy data with transformation
            database.execSQL(
                "INSERT INTO groups_new "
                    + "SELECT groupKey, groupName, adminKey, groupLocation, groupDays, groupMonths, "
                    + "groupYears, groupHours, createdAt, groupPrice, groupType, canAdd, "
                    + "friendKeys, comingKeys, messageKeys, isPrivate, maxParticipants, "
                    + "requiresApproval, NULL as description, 'OTHER' as category "
                    + "FROM groups");

            // Drop old table and rename new one
            database.execSQL("DROP TABLE groups");
            database.execSQL("ALTER TABLE groups_new RENAME TO groups");

            // Recreate indexes
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_groups_adminKey ON groups(adminKey)");
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_groups_category ON groups(category)");
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_groups_createdAt ON groups(createdAt)");

            Log.d(TAG, "Successfully migrated from version 3 to 4");

          } catch (Exception e) {
            Log.e(TAG, "Error during migration 3->4", e);
            throw e;
          }
        }
      };

  /**
   * Emergency migration - clears all data if migration fails Use this as a last resort when data
   * integrity is compromised
   */
  public static final Migration EMERGENCY_CLEAR_ALL =
      new Migration(VERSION_1, VERSION_4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
          Log.w(TAG, "Performing emergency migration - all data will be lost");

          try {
            // Drop all tables
            database.execSQL("DROP TABLE IF EXISTS groups");
            database.execSQL("DROP TABLE IF EXISTS users");
            database.execSQL("DROP TABLE IF EXISTS chat_messages");
            database.execSQL("DROP TABLE IF EXISTS user_preferences");

            // Recreate tables with latest schema
            createLatestSchema(database);

            Log.w(TAG, "Emergency migration completed - database recreated");

          } catch (Exception e) {
            Log.e(TAG, "Emergency migration failed", e);
            throw e;
          }
        }

        private void createLatestSchema(@NonNull SupportSQLiteDatabase database) {
          // Create groups table matching Entity expectations exactly
          database.execSQL(
              "CREATE TABLE IF NOT EXISTS `groups` ("
                  + "`groupKey` TEXT PRIMARY KEY NOT NULL, "
                  + "`group_name` TEXT, "
                  + "`admin_key` TEXT, "
                  + "`group_location` TEXT, "
                  + "`group_days` TEXT, "
                  + "`group_months` TEXT, "
                  + "`group_years` TEXT, "
                  + "`group_hours` TEXT, "
                  + "`created_at` TEXT, "
                  + "`group_price` TEXT, "
                  + "`group_type` INTEGER NOT NULL, "
                  + "`can_add` INTEGER NOT NULL, "
                  + "`friend_keys` TEXT, "
                  + "`coming_keys` TEXT, "
                  + "`message_keys` TEXT, "
                  + "`group_description` TEXT"
                  + ")");

          // Create users table matching Entity expectations exactly
          database.execSQL(
              "CREATE TABLE IF NOT EXISTS `users` ("
                  + "`userKey` TEXT PRIMARY KEY NOT NULL, "
                  + "`username` TEXT, "
                  + "`email` TEXT, "
                  + "`profile_image_url` TEXT, "
                  + "`friend_keys` TEXT"
                  + ")");

          // Create chat_messages table matching Entity expectations exactly
          database.execSQL(
              "CREATE TABLE IF NOT EXISTS `chat_messages` ("
                  + "`messageKey` TEXT PRIMARY KEY NOT NULL, "
                  + "`groupKey` TEXT, "
                  + "`senderKey` TEXT, "
                  + "`senderName` TEXT, "
                  + "`message` TEXT, "
                  + "`timestamp` INTEGER, "
                  + "`imageUrl` TEXT, "
                  + "`metadata` TEXT"
                  + ")");

          // Skip user_preferences table for now as it's not part of core entities

          // Create basic indexes matching the entities
          database.execSQL(
              "CREATE INDEX IF NOT EXISTS `index_groups_admin_key` ON `groups`(`admin_key`)");
          database.execSQL(
              "CREATE INDEX IF NOT EXISTS `index_chat_messages_groupKey` ON `chat_messages`(`groupKey`)");
          database.execSQL("CREATE INDEX IF NOT EXISTS `index_users_email` ON `users`(`email`)");
        }
      };

  // Migration helper methods

  /**
   * Helper method to add a column to a table
   *
   * @param database The database instance
   * @param tableName The table name
   * @param columnName The column name
   * @param columnDefinition The column definition
   */
  private static void addColumnToTable(
      SupportSQLiteDatabase database,
      String tableName,
      String columnName,
      String columnDefinition) {
    String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition;
    database.execSQL(sql);
  }

  /**
   * Helper method to create an index if it doesn't exist
   *
   * @param database The database instance
   * @param indexName The index name
   * @param tableName The table name
   * @param columnName The column name
   */
  private static void createIndexIfNotExists(
      SupportSQLiteDatabase database, String indexName, String tableName, String columnName) {
    String sql =
        "CREATE INDEX IF NOT EXISTS " + indexName + " ON " + tableName + "(" + columnName + ")";
    database.execSQL(sql);
  }

  /** Get all available migrations in order */
  public static Migration[] getAllMigrations() {
    return new Migration[] {
      MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6
    };
  }

  /** Get emergency migration for data recovery scenarios */
  public static Migration getEmergencyMigration(int fromVersion, int toVersion) {
    return new Migration(fromVersion, toVersion) {
      @Override
      public void migrate(@NonNull SupportSQLiteDatabase database) {
        Log.w(TAG, String.format("Emergency migration from %d to %d", fromVersion, toVersion));
        EMERGENCY_CLEAR_ALL.migrate(database);
      }
    };
  }

  /** Database migration callback for monitoring migration success/failure */
  public static class MigrationCallback {

    public static void onMigrationStart(int fromVersion, int toVersion) {
      Log.i(
          TAG,
          String.format(
              "Starting database migration from version %d to %d", fromVersion, toVersion));
    }

    public static void onMigrationSuccess(int fromVersion, int toVersion) {
      Log.i(
          TAG,
          String.format(
              "Database migration completed successfully from version %d to %d",
              fromVersion, toVersion));
    }

    public static void onMigrationFailure(int fromVersion, int toVersion, Exception error) {
      Log.e(
          TAG,
          String.format("Database migration failed from version %d to %d", fromVersion, toVersion),
          error);
    }

    public static void onFallbackToDestructive(int fromVersion, int toVersion) {
      Log.w(
          TAG,
          String.format(
              "Falling back to destructive migration from version %d to %d - DATA WILL BE LOST",
              fromVersion, toVersion));
    }
  }
}
