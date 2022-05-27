package com.example.weatherapp.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class MyDbManager {
    private Context context;
    private MyDbHelper myDbHelper;
    private SQLiteDatabase db;

    public MyDbManager(Context context) {
        this.context = context;
        myDbHelper = new MyDbHelper(context);
    }

    public void openDb() {
        db = myDbHelper.getWritableDatabase();
    }

    public void insertToDb(String datatime, String cityname, String temp, String condition) {
        ContentValues cv = new ContentValues();
        cv.put(MyConstants.DATATIME, datatime);
        cv.put(MyConstants.CITYNAME, cityname);
        cv.put(MyConstants.TEMP, temp);
        cv.put(MyConstants.CONDITION, condition);
        db.insert(MyConstants.TABLE_NAME, null, cv);
    }

    public List<String> getFromDb() {
        List<String> datatimeList = new ArrayList<>();
        List<String> citynameList = new ArrayList<>();
        List<String> tempList = new ArrayList<>();
        List<String> conditionList = new ArrayList<>();

        Cursor cursor = db.query(MyConstants.TABLE_NAME, null, null, null,
                null, null, null);

        while (cursor.moveToNext()) {
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!
            @SuppressLint("Range") String datatime = cursor.getString(cursor.getColumnIndex(MyConstants.DATATIME));
            datatimeList.add(datatime);
            @SuppressLint("Range") String cityname = cursor.getString(cursor.getColumnIndex(MyConstants.CITYNAME));
            citynameList.add(cityname);
            @SuppressLint("Range") String temp = cursor.getString(cursor.getColumnIndex(MyConstants.TEMP));
            tempList.add(temp);
            @SuppressLint("Range") String condition = cursor.getString(cursor.getColumnIndex(MyConstants.CONDITION));
            conditionList.add(condition);
        }

        cursor.close();
        return tempList;
    }

    public void closeDb() {
        myDbHelper.close();
    }

}
