package com.example.lence.bird_hunter.dateBase


import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.lence.bird_hunter.R
import com.example.lence.bird_hunter.ui.MVPDB
import com.example.lence.bird_hunter.ui.MainActivity
import kotlinx.android.synthetic.main.activity_main.view.*
import java.util.*

class DBManager(activity: MainActivity) : MVPDB {
    private var db: DB = DB(activity)
    private var database: SQLiteDatabase? = null

    //    @Override
    //    public void insert(String bird) {
    //        database = db.getWritableDatabase();
    //        ContentValues contentValues = new ContentValues();
    //        contentValues.put(DB.KEY_NAME, bird);
    //        database.insert(DB.TABLE_BIRDS, null, contentValues);
    //        //mvpUpDate.showNewUser();
    //    }

    override val birds: List<String>
        get() {
            database = db.readableDatabase
            val rez = ArrayList<String>()
            val cursor = database!!.query(DB.TABLE_BIRDS, null, null, null, null, null, null)
            if (cursor.moveToFirst()) {
                val key_id = cursor.getColumnIndex(DB.KEY_BIRD_ID)
                val key_name = cursor.getColumnIndex(DB.KEY_NAME)
                do {

                    rez.add(cursor.getString(key_name))
                } while (cursor.moveToNext())
            } else {
                return rez
            }
            return rez
        }

    //    @Override
    //    public void delete(String id) {
    //        database = db.getReadableDatabase();
    //        database.delete(DB.TABLE_USER, DB.KEY_USER_ID + " =?", new String[]{id});
    //        mvpUpDate.showNewUser();
    //    }

    override fun upDate(birds: List<String>) {
        database = db.readableDatabase
        database!!.delete(DB.TABLE_BIRDS, null, null)

        for (i in birds) {
            val contentValues = ContentValues()
            contentValues.put(DB.KEY_NAME, i)
            database!!.insert(DB.TABLE_BIRDS, null, contentValues)
        }
    }
}
