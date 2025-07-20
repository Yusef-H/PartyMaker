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
 * the local database.
 */
@Dao
public interface GroupDao {

  /**
   * Gets a group by its key
   *
   * @param groupKey The group key
   * @return The group
   */
  @Query("SELECT * FROM `Group` WHERE groupKey = :groupKey")
  Group getGroupByKey(String groupKey);

  /**
   * Gets a group by its key as LiveData
   *
   * @param groupKey The group key
   * @return LiveData containing the group
   */
  @Query("SELECT * FROM `Group` WHERE groupKey = :groupKey")
  LiveData<Group> observeGroupByKey(String groupKey);

  /**
   * Gets all groups
   *
   * @return List of all groups
   */
  @Query("SELECT * FROM `Group`")
  List<Group> getAllGroups();

  /**
   * Gets all groups as LiveData
   *
   * @return LiveData containing list of all groups
   */
  @Query("SELECT * FROM `Group`")
  LiveData<List<Group>> observeAllGroups();

  /**
   * Gets groups for a specific user This is a simplified version that just returns all groups for
   * now In a real implementation, this would filter groups by user
   *
   * @return List of groups
   */
  @Query("SELECT * FROM `Group`")
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
  @Query("DELETE FROM `Group` WHERE groupKey = :groupKey")
  void deleteGroupByKey(String groupKey);

  /** Deletes all groups */
  @Query("DELETE FROM `Group`")
  void deleteAllGroups();
}
