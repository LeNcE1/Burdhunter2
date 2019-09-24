package com.example.lence.bird_hunter.dateBase


import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import okhttp3.MultipartBody

class DB(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL(
            "create table " + TABLE_BIRDS + "("
                    + KEY_BIRD_ID + " integer primary key autoincrement,"
                    + KEY_NAME + " text"
                    + ");"
        )
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        onCreate(sqLiteDatabase)
    }

    companion object {
        val DATABASE_VERSION = 1
        val DATABASE_NAME = "MyDb"

        val TABLE_BIRDS = "birds"

        val KEY_BIRD_ID = "id"
        val KEY_NAME = "name"
    }
}

@Entity(tableName = "birds")
data class Bird(
    @PrimaryKey val birdName: String = ""
)

@Dao
interface BirdDao {

    @Query("SELECT birdName FROM birds")
    fun getBirds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(birds: List<Bird>)

}


@Entity(tableName = "posts")
data class PostBird(
    @PrimaryKey(autoGenerate = true) val id:Int = 0,
    @ColumnInfo val user_id: String = "",
    @ColumnInfo val x: String = "",
    @ColumnInfo val y: String = "",
    @ColumnInfo val bird_name: String = "",
    @ColumnInfo val description: String = "",
    @ColumnInfo val files: String = ""
)

@Dao
interface PostBirdDao {

    @Query("SELECT * FROM posts")
    fun getPosts(): List<PostBird>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(post:PostBird)

    @Delete
    fun delete(post:PostBird)

}



@Database(entities = [Bird::class,PostBird::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun birdDao(): BirdDao
    abstract fun postBirdDao(): PostBirdDao
}
