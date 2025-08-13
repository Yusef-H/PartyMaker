package com.example.partymaker.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.partymaker.data.model.Group;
import java.util.List;

/**
 * Data Access Object for Group entities. Provides methods to query, insert, and update groups in
 * the local database. Following Room DAO patterns with proper query optimization.
 */
@Dao
public interface GroupDao {

  // Table and column constants
  String TABLE_NAME = "groups";
  String COLUMN_GROUP_KEY = "groupKey";

  // SQL Query constants
  String SELECT_ALL_COLUMNS = "SELECT * FROM `" + TABLE_NAME + "`";
  String WHERE_GROUP_KEY = " WHERE " + COLUMN_GROUP_KEY + " = :groupKey";
  String DELETE_FROM_TABLE = "DELETE FROM `" + TABLE_NAME + "`";

  /**
   * Gets a group by its key
   *
   * @param groupKey The group key
   * @return The group
   */
  @Query(SELECT_ALL_COLUMNS + WHERE_GROUP_KEY)
  Group getGroupByKey(String groupKey);

  /**
   * Gets a group by its key as LiveData
   *
   * @param groupKey The group key
   * @return LiveData containing the group
   */
  @Query(SELECT_ALL_COLUMNS + WHERE_GROUP_KEY)
  LiveData<Group> observeGroupByKey(String groupKey);

  /**
   * Gets all groups
   *
   * @return List of all groups
   */
  @Query(SELECT_ALL_COLUMNS)
  List<Group> getAllGroups();

  /**
   * Gets all groups as LiveData
   *
   * @return LiveData containing list of all groups
   */
  @Query(SELECT_ALL_COLUMNS)
  LiveData<List<Group>> observeAllGroups();

  /**
   * Gets groups for a specific user with pagination
   *
   * @param adminKey The admin key to filter by
   * @param limit The number of groups to return
   * @return LiveData containing list of user's groups
   */
  @Query(SELECT_ALL_COLUMNS + " WHERE admin_key = :adminKey ORDER BY created_at DESC LIMIT :limit")
  LiveData<List<Group>> getUserGroupsPaginated(String adminKey, int limit);
  
  /**
   * Gets public groups with pagination
   *
   * @param limit The number of groups to return
   * @param offset The offset for pagination
   * @return LiveData containing list of public groups
   */
  @Query(SELECT_ALL_COLUMNS + " WHERE group_type = 0 ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
  LiveData<List<Group>> getPublicGroupsPaginated(int limit, int offset);
  
  /**
   * Count queries for pagination
   */
  @Query("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE admin_key = :adminKey AND group_type = 1")
  int getUserPrivateGroupsCount(String adminKey);
  
  @Query("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE group_type = 0")
  int getPublicGroupsCount();
  
  /**
   * Search groups with FTS simulation
   *
   * @param searchTerm The term to search for
   * @param limit Maximum results to return
   * @return LiveData containing search results
   */
  @Query(SELECT_ALL_COLUMNS + " WHERE group_name LIKE '%' || :searchTerm || '%' OR group_location LIKE '%' || :searchTerm || '%' ORDER BY created_at DESC LIMIT :limit")
  LiveData<List<Group>> searchGroups(String searchTerm, int limit);
  
  /**
   * Recent groups for quick access
   *
   * @return LiveData containing recent groups
   */
  @Query(SELECT_ALL_COLUMNS + " ORDER BY created_at DESC LIMIT 10")
  LiveData<List<Group>> getRecentGroups();

  /**
   * Inserts a group
   *
   * @param group The group to insert
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertGroup(Group group);

  /**
   * Inserts multiple groups
   *
   * @param groups The groups to insert
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertGroups(List<Group> groups);

  /**
   * Updates a group
   *
   * @param group The group to update
   */
  @Update
  void updateGroup(Group group);
  
  /**
   * Updates multiple groups
   *
   * @param groups The groups to update
   */
  @Update
  void updateGroups(List<Group> groups);

  /**
   * Deletes a group by its key
   *
   * @param groupKey The group key
   */
  @Query(DELETE_FROM_TABLE + WHERE_GROUP_KEY)
  void deleteGroupByKey(String groupKey);

  /** Deletes all groups */
  @Query(DELETE_FROM_TABLE)
  void deleteAllGroups();
  
  /**
   * Cleanup old groups based on creation time
   *
   * @param cutoffTime The cutoff timestamp (older groups will be deleted)
   * @return Number of deleted groups
   */
  @Query(DELETE_FROM_TABLE + " WHERE CAST(created_at AS INTEGER) < :cutoffTime")
  int deleteOldGroups(long cutoffTime);
  
  /**
   * Get groups with location for map view
   *
   * @return LiveData containing groups with location
   */
  @Query(SELECT_ALL_COLUMNS + " WHERE group_location IS NOT NULL AND group_location != '' AND group_type = 0 ORDER BY created_at DESC")
  LiveData<List<Group>> getGroupsWithLocation();
  
  /**
   * Get group by key synchronously
   *
   * @param groupKey The group key
   * @return The group
   */
  @Query(SELECT_ALL_COLUMNS + WHERE_GROUP_KEY)
  Group getGroupByKeySync(String groupKey);
}
