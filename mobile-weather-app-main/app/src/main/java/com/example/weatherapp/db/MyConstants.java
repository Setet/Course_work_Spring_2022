package com.example.weatherapp.db;

public class MyConstants {
    public static final String TABLE_NAME = "weather";//имя таблицы

    public static final String _ID = "_id";//идентификатор

    public static final String DATATIME = "datatime";//дата время
    public static final String CITYNAME = "cityname";//имя города
    public static final String TEMP = "temp";//температура в градусах цельсия
    public static final String CONDITION = "condition";//описание погоды

    public static final String DB_NAME = "Weather.db";//имя бд
    public static final int DB_VERSION = 1;//версия бв

    public static final String TABLE_STRUCTURE = "CREATE TABLE IF NOT EXISTS " +
            TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY," + DATATIME + " TEXT," + CITYNAME +
            " TEXT," + TEMP + " TEXT," + CONDITION + " TEXT)";//структура табл

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;//делит табл
}
