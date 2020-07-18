package com.bigyao.account;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHandler extends SQLiteOpenHelper {
    public DBOpenHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int dbVersion){
        super(context, name, factory, dbVersion);
    }

    /**
     * 首次创建数据库时调用
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // id, 标题，日期，明细，收支
        db.execSQL("CREATE TABLE account(id integer primary key autoincrement, title text, date text, detail text, income integer)");
    }

    /**
     * 更新数据库版本时调用
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE account");
        db.execSQL("CREATE TABLE account(id integer primary key autoincrement, title text, date text, detail text, income integer)");
    }
}

