package com.antif.browser.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profiles ORDER BY lastUsedAt DESC")
    fun getAllProfiles(): Flow<List<BrowserProfile>>

    @Query("SELECT * FROM profiles ORDER BY lastUsedAt DESC")
    suspend fun getAllProfilesList(): List<BrowserProfile>

    @Query("SELECT * FROM profiles ORDER BY lastUsedAt DESC")
    suspend fun getAllProfilesSync(): List<BrowserProfile>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): BrowserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: BrowserProfile): Long

    @Update
    suspend fun updateProfile(profile: BrowserProfile)

    @Delete
    suspend fun deleteProfile(profile: BrowserProfile)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Long)

    @Query("UPDATE profiles SET lastUsedAt = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: Long, timestamp: Long)

    @Query("SELECT COUNT(*) FROM profiles")
    suspend fun getProfileCount(): Int

    @Query("DELETE FROM profiles")
    suspend fun deleteAllProfiles()
}
