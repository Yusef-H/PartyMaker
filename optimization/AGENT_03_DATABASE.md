# AGENT 03 - Database Optimization

## 🎯 Mission: Room Database Performance & Caching
**Estimated Time: 4-5 hours**
**Priority: HIGH**

---

## 📋 Tasks Overview

### Task 1: Room Database Indexing
**Time: 2 hours | Priority: CRITICAL**

#### Files to Modify:
- `app/src/main/java/com/example/partymaker/data/local/AppDatabase.java`
- All Entity classes in `app/src/main/java/com/example/partymaker/data/model/`
- All DAO classes in `app/src/main/java/com/example/partymaker/data/local/`

#### 1. Update Entity Classes with Indexes:

**Update Group Entity:**
```java
@Entity(tableName = "groups",
        indices = {
            @Index(value = "createdAt", name = "idx_group_created"),
            @Index(value = "lastMessageTime", name = "idx_group_last_message"),
            @Index(value = {"isPrivate", "createdAt"}, name = "idx_group_private_created"),
            @Index(value = "userId", name = "idx_group_user"),
            @Index(value = {"userId", "isPrivate"}, name = "idx_group_user_private"),
            @Index(value = "groupName", name = "idx_group_name") // For search
        })
public class GroupEntity {
    @PrimaryKey
    @NonNull
    private String id;
    
    @ColumnInfo(name = "groupName")
    private String groupName;
    
    @ColumnInfo(name = "userId")
    private String userId;
    
    @ColumnInfo(name = "isPrivate")
    private boolean isPrivate;
    
    @ColumnInfo(name = "createdAt")
    private long createdAt;
    
    @ColumnInfo(name = "lastMessageTime")
    private long lastMessageTime;
    
    // Add more indexes for commonly queried fields
    @ColumnInfo(name = "location")
    private String location;
    
    @ColumnInfo(name = "eventDate")
    private long eventDate;
    
    // Existing getters/setters...
}
```

**Update User Entity:**
```java
@Entity(tableName = "users",
        indices = {
            @Index(value = "email", name = "idx_user_email", unique = true),
            @Index(value = "userName", name = "idx_user_name"),
            @Index(value = "lastActive", name = "idx_user_last_active"),
            @Index(value = {"isOnline", "lastActive"}, name = "idx_user_online_active")
        })
public class UserEntity {
    @PrimaryKey
    @NonNull
    private String userId;
    
    @ColumnInfo(name = "email")
    private String email;
    
    @ColumnInfo(name = "userName")
    private String userName;
    
    @ColumnInfo(name = "isOnline")
    private boolean isOnline;
    
    @ColumnInfo(name = "lastActive")
    private long lastActive;
    
    // Existing getters/setters...
}
```

**Create ChatMessage Entity (if not exists):**
```java
@Entity(tableName = "chat_messages",
        indices = {
            @Index(value = "groupId", name = "idx_message_group"),
            @Index(value = {"groupId", "timestamp"}, name = "idx_message_group_time"),
            @Index(value = "senderId", name = "idx_message_sender"),
            @Index(value = "timestamp", name = "idx_message_time"),
            @Index(value = {"groupId", "messageType"}, name = "idx_message_group_type")
        })
public class ChatMessageEntity {
    @PrimaryKey
    @NonNull
    private String messageId;
    
    @ColumnInfo(name = "groupId")
    private String groupId;
    
    @ColumnInfo(name = "senderId")
    private String senderId;
    
    @ColumnInfo(name = "messageText")
    private String messageText;
    
    @ColumnInfo(name = "timestamp")
    private long timestamp;
    
    @ColumnInfo(name = "messageType")
    private String messageType; // TEXT, IMAGE, FILE
    
    @ColumnInfo(name = "isRead")
    private boolean isRead;
    
    // Constructors, getters, setters...
}
```

#### 2. Update AppDatabase Configuration:

```java
@Database(
    entities = {GroupEntity.class, UserEntity.class, ChatMessageEntity.class},
    version = 3, // Increment version
    exportSchema = true
)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;
    
    // Abstract DAOs
    public abstract GroupDao groupDao();
    public abstract UserDao userDao();
    public abstract ChatMessageDao chatMessageDao();
    
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = buildDatabase(context);
                }
            }
        }
        return INSTANCE;
    }
    
    private static AppDatabase buildDatabase(Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "partymaker_database")
            .addCallback(new RoomDatabase.Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);
                    // Create additional custom indexes
                    createCustomIndexes(db);
                }
                
                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    // Enable WAL mode for better performance
                    db.execSQL("PRAGMA journal_mode = WAL");
                    db.execSQL("PRAGMA synchronous = NORMAL");
                    db.execSQL("PRAGMA cache_size = 10000"); // 10MB cache
                    db.execSQL("PRAGMA temp_store = MEMORY");
                }
            })
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .addMigrations(MIGRATION_2_3) // Add migration
            .build();
    }
    
    private static void createCustomIndexes(SupportSQLiteDatabase db) {
        // Create composite indexes for complex queries
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_group_search ON groups(groupName COLLATE NOCASE, location COLLATE NOCASE)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_message_unread ON chat_messages(groupId, isRead, timestamp)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_user_search ON users(userName COLLATE NOCASE, email COLLATE NOCASE)");
    }
    
    // Database migration
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add new columns if needed
            database.execSQL("ALTER TABLE groups ADD COLUMN eventDate INTEGER DEFAULT 0");
            // Create new indexes
            createCustomIndexes(database);
        }
    };
}
```

---

### Task 2: Optimize DAO Queries
**Time: 1.5 hours | Priority: HIGH**

#### Update GroupDao.java:
```java
@Dao
public interface GroupDao {
    
    // Optimized queries with LIMIT and proper ORDER BY
    @Query("SELECT * FROM groups WHERE userId = :userId ORDER BY lastMessageTime DESC LIMIT :limit")
    LiveData<List<GroupEntity>> getUserGroupsPaginated(String userId, int limit);
    
    @Query("SELECT * FROM groups WHERE isPrivate = 0 ORDER BY lastMessageTime DESC LIMIT :limit OFFSET :offset")
    LiveData<List<GroupEntity>> getPublicGroupsPaginated(int limit, int offset);
    
    // Count queries for pagination
    @Query("SELECT COUNT(*) FROM groups WHERE userId = :userId AND isPrivate = 1")
    int getUserPrivateGroupsCount(String userId);
    
    @Query("SELECT COUNT(*) FROM groups WHERE isPrivate = 0")
    int getPublicGroupsCount();
    
    // Search queries with FTS (Full Text Search) simulation
    @Query("SELECT * FROM groups WHERE groupName LIKE '%' || :searchTerm || '%' OR location LIKE '%' || :searchTerm || '%' ORDER BY lastMessageTime DESC LIMIT :limit")
    LiveData<List<GroupEntity>> searchGroups(String searchTerm, int limit);
    
    // Recent groups for quick access
    @Query("SELECT * FROM groups ORDER BY lastMessageTime DESC LIMIT 10")
    LiveData<List<GroupEntity>> getRecentGroups();
    
    // Batch operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGroups(List<GroupEntity> groups);
    
    @Update
    void updateGroups(List<GroupEntity> groups);
    
    // Cleanup old data
    @Query("DELETE FROM groups WHERE lastMessageTime < :cutoffTime")
    int deleteOldGroups(long cutoffTime);
    
    // Get groups by location for map view
    @Query("SELECT * FROM groups WHERE location IS NOT NULL AND location != '' AND isPrivate = 0 ORDER BY createdAt DESC")
    LiveData<List<GroupEntity>> getGroupsWithLocation();
    
    // Specific group queries
    @Query("SELECT * FROM groups WHERE id = :groupId")
    LiveData<GroupEntity> getGroupById(String groupId);
    
    @Query("SELECT * FROM groups WHERE id = :groupId")
    GroupEntity getGroupByIdSync(String groupId);
}
```

#### Create ChatMessageDao.java:
```java
@Dao
public interface ChatMessageDao {
    
    @Query("SELECT * FROM chat_messages WHERE groupId = :groupId ORDER BY timestamp DESC LIMIT :limit")
    LiveData<List<ChatMessageEntity>> getGroupMessages(String groupId, int limit);
    
    @Query("SELECT * FROM chat_messages WHERE groupId = :groupId AND timestamp > :lastTimestamp ORDER BY timestamp DESC LIMIT :limit")
    LiveData<List<ChatMessageEntity>> getNewMessages(String groupId, long lastTimestamp, int limit);
    
    @Query("SELECT COUNT(*) FROM chat_messages WHERE groupId = :groupId AND isRead = 0 AND senderId != :currentUserId")
    LiveData<Integer> getUnreadMessageCount(String groupId, String currentUserId);
    
    @Query("UPDATE chat_messages SET isRead = 1 WHERE groupId = :groupId AND senderId != :currentUserId")
    int markAllMessagesAsRead(String groupId, String currentUserId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessages(List<ChatMessageEntity> messages);
    
    @Query("DELETE FROM chat_messages WHERE timestamp < :cutoffTime")
    int deleteOldMessages(long cutoffTime);
    
    // Get latest message for group list
    @Query("SELECT * FROM chat_messages WHERE groupId = :groupId ORDER BY timestamp DESC LIMIT 1")
    ChatMessageEntity getLatestMessage(String groupId);
}
```

---

### Task 3: Repository Pattern with Caching
**Time: 1-2 hours | Priority: HIGH**

#### Create Enhanced GroupRepository:
```java
public class GroupRepository {
    private final GroupDao localDataSource;
    private final FirebaseServerClient remoteDataSource;
    private final NetworkManager networkManager;
    
    // In-memory cache with expiration
    private final Map<String, CachedData<List<Group>>> cache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
    private static final int DEFAULT_PAGE_SIZE = 20;
    
    public GroupRepository() {
        localDataSource = AppDatabase.getInstance(PartyApplication.getInstance()).groupDao();
        remoteDataSource = FirebaseServerClient.getInstance();
        networkManager = new NetworkManager();
    }
    
    // Paginated groups with caching
    public LiveData<List<Group>> getUserGroups(String userId, int page) {
        String cacheKey = "user_groups_" + userId + "_" + page;
        
        // Check cache first
        CachedData<List<Group>> cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.data;
        }
        
        // Get from local database
        LiveData<List<GroupEntity>> localData = localDataSource.getUserGroupsPaginated(
            userId, DEFAULT_PAGE_SIZE);
        
        // Transform to domain objects
        MediatorLiveData<List<Group>> result = new MediatorLiveData<>();
        result.addSource(localData, entities -> {
            if (entities != null) {
                List<Group> groups = entities.stream()
                    .map(this::mapEntityToDomain)
                    .collect(Collectors.toList());
                result.setValue(groups);
            }
        });
        
        // Fetch from remote if online
        if (networkManager.isOnline()) {
            fetchFromRemoteAndCache(userId, page, cacheKey);
        }
        
        return result;
    }
    
    public LiveData<List<Group>> searchGroups(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new MutableLiveData<>(new ArrayList<>());
        }
        
        String cacheKey = "search_" + query.hashCode();
        
        // Check cache
        CachedData<List<Group>> cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.data;
        }
        
        // Search locally first
        LiveData<List<GroupEntity>> localData = localDataSource.searchGroups(query, 50);
        
        MediatorLiveData<List<Group>> result = new MediatorLiveData<>();
        result.addSource(localData, entities -> {
            if (entities != null) {
                List<Group> groups = entities.stream()
                    .map(this::mapEntityToDomain)
                    .collect(Collectors.toList());
                result.setValue(groups);
                
                // Cache search results
                cache.put(cacheKey, new CachedData<>(new MutableLiveData<>(groups), System.currentTimeMillis()));
            }
        });
        
        return result;
    }
    
    private void fetchFromRemoteAndCache(String userId, int page, String cacheKey) {
        ThreadUtils.executeDatabaseTask(() -> {
            try {
                List<Group> remoteGroups = remoteDataSource.fetchUserGroups(userId, page);
                
                // Convert to entities and save to local database
                List<GroupEntity> entities = remoteGroups.stream()
                    .map(this::mapDomainToEntity)
                    .collect(Collectors.toList());
                    
                localDataSource.insertGroups(entities);
                
                // Update cache
                MutableLiveData<List<Group>> liveData = new MutableLiveData<>(remoteGroups);
                cache.put(cacheKey, new CachedData<>(liveData, System.currentTimeMillis()));
                
            } catch (Exception e) {
                Log.e("GroupRepository", "Failed to fetch from remote", e);
            }
        });
    }
    
    // Cache cleanup
    public void clearExpiredCache() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    // Cleanup old data
    public void cleanupOldData() {
        ThreadUtils.executeDatabaseTask(() -> {
            long cutoffTime = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000); // 30 days
            int deletedGroups = localDataSource.deleteOldGroups(cutoffTime);
            Log.d("GroupRepository", "Cleaned up " + deletedGroups + " old groups");
        });
    }
    
    // Mapping methods
    private Group mapEntityToDomain(GroupEntity entity) {
        // Convert entity to domain object
        return new Group(entity.getId(), entity.getGroupName(), /* other fields */);
    }
    
    private GroupEntity mapDomainToEntity(Group domain) {
        // Convert domain object to entity
        GroupEntity entity = new GroupEntity();
        entity.setId(domain.getId());
        entity.setGroupName(domain.getName());
        // Set other fields
        return entity;
    }
    
    private static class CachedData<T> {
        final LiveData<T> data;
        final long timestamp;
        
        CachedData(LiveData<T> data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION;
        }
    }
}
```

---

### Task 4: Database Performance Monitoring
**Time: 0.5 hour | Priority: MEDIUM**

#### Create DatabaseMonitor.java:
```java
public class DatabaseMonitor {
    private static final String TAG = "DatabaseMonitor";
    
    public static void logQueryPerformance(String queryName, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        
        if (duration > 500) { // Slow query threshold
            Log.w(TAG, "SLOW QUERY: " + queryName + " took " + duration + "ms");
        } else {
            Log.d(TAG, queryName + " took " + duration + "ms");
        }
    }
    
    public static void logDatabaseStats(Context context) {
        ThreadUtils.executeDatabaseTask(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            
            // Log table sizes
            int groupCount = db.groupDao().getUserPrivateGroupsCount("sample");
            Log.i(TAG, "Database stats - Groups: " + groupCount);
            
            // Log database file size
            File dbFile = context.getDatabasePath("partymaker_database");
            if (dbFile.exists()) {
                long sizeInMB = dbFile.length() / (1024 * 1024);
                Log.i(TAG, "Database file size: " + sizeInMB + " MB");
                
                if (sizeInMB > 50) { // Alert if database is too large
                    Log.w(TAG, "Database is getting large, consider cleanup");
                }
            }
        });
    }
}
```

---

## ✅ Testing Instructions

### Database Performance Tests:

1. **Query Performance Test:**
```bash
# Enable SQL logging
adb shell setprop log.tag.SQLiteDatabase VERBOSE
adb shell setprop log.tag.SQLiteStatements VERBOSE

# Monitor logs
adb logcat -s SQLiteDatabase SQLiteStatements
```

1. **Database Size Test:**
```bash
# Check database file size
adb shell ls -la /data/data/com.example.partymaker/databases/
```

1. **Index Usage Test:**
```sql
-- Test queries manually in database
EXPLAIN QUERY PLAN SELECT * FROM groups WHERE userId = 'test' ORDER BY lastMessageTime DESC;
```

### Expected Results:
- Query execution under 100ms for most operations
- Search queries under 200ms
- Database file under 50MB
- Indexes being used (check EXPLAIN QUERY PLAN)

---

## 🚨 Critical Points

1. **Test with Large Dataset**: Insert 1000+ groups to test performance
2. **Check Index Usage**: Use EXPLAIN QUERY PLAN to verify indexes
3. **Monitor Database Size**: Track growth over time
4. **Test Offline Scenarios**: Ensure local queries work without network

---

## 📊 Success Criteria

- [ ] All queries under 100ms execution time
- [ ] Search functionality under 200ms
- [ ] Database indexes properly used
- [ ] Caching working correctly
- [ ] Old data cleanup functioning
- [ ] Migration working without data loss

---

**Agent 03 Priority:** Focus on Indexing first - biggest database performance impact!
**Time Allocation:** Indexes (40%) → DAO Optimization (30%) → Repository Caching (30%)