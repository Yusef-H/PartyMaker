package com.example.partymaker.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.model.User;
import com.example.partymaker.data.model.ChatMessage;

/**
 * Room database for local caching of data.
 * This improves performance by reducing network calls and allowing offline access.
 */
@Database(
    entities = {Group.class, User.class, ChatMessage.class},
    version = 1,
    exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    // DAOs (Data Access Objects)
    public abstract GroupDao groupDao();
    public abstract UserDao userDao();
    public abstract ChatMessageDao chatMessageDao();

    /**
     * Gets the singleton instance of the database.
     *
     * @param context The application context
     * @return The database instance
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "partymaker_database")
                            .fallbackToDestructiveMigration() // For simplicity in development
                            .build();
                }
            }
        }
        return INSTANCE;
    }
} 