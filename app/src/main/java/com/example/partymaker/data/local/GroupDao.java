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
   * Gets groups for a specific user. This is a simplified version that just returns all groups for
   * now. In a real implementation, this would filter groups by user.
   *
   * @return List of groups (currently returns all groups)
   */
  @Query(SELECT_ALL_COLUMNS)
  List<Group> getGroupsForUser();

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
   * Deletes a group by its key
   *
   * @param groupKey The group key
   */
  @Query(DELETE_FROM_TABLE + WHERE_GROUP_KEY)
  void deleteGroupByKey(String groupKey);

  /** Deletes all groups */
  @Query(DELETE_FROM_TABLE)
  void deleteAllGroups();
}
