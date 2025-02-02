package com.example.romero_andresimdbappp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//Clase que maneja la base de datos SQLite para almacenar películas favoritas.
public class FavoritesDatabaseHelper extends SQLiteOpenHelper {
    // Nombre y versión de la base de datos
    private static final String DATABASE_NAME = "favorites_db";
    private static final int DATABASE_VERSION = 2;
    // Nombre de la tabla y columnas
    public static final String TABLE_FAVORITES = "favorites";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_EMAIL = "user_email";  // Usuario dueño del favorito
    public static final String COLUMN_TITLE = "title";  // Título de la película
    public static final String COLUMN_IMAGE_URL = "image_url";  // Imagen de la película
    public static final String COLUMN_RELEASE_DATE = "release_date";  // Fecha de estreno
    public static final String COLUMN_RATING = "rating";  // Puntuación de la película

    // Sentencia SQL para crear la tabla favoritos
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_FAVORITES + " (" +
                    COLUMN_ID + " TEXT, " +  // ID de la película (String)
                    COLUMN_USER_EMAIL + " TEXT, " +  // Email del usuario que la guardó
                    COLUMN_TITLE + " TEXT, " +  // Título de la película
                    COLUMN_IMAGE_URL + " TEXT, " +  // URL de la imagen de la película
                    COLUMN_RELEASE_DATE + " TEXT, " +  // Fecha de estreno
                    COLUMN_RATING + " TEXT, " +  // Puntuación de la película
                    "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_USER_EMAIL + "));";  // Clave primaria combinada (película + usuario)

    //Constructor del helper de base de datos.
    public FavoritesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Método que se ejecuta cuando se crea la base de datos por primera vez.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);  // Ejecuta la creación de la tabla
    }

    //Método que se ejecuta cuando se actualiza la versión de la base de datos.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);  // Elimina la tabla existente
            onCreate(db);  // Vuelve a crear la tabla
        }
    }

    //Método que se ejecuta cuando se reduce la versión de la base de datos.
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);  // Elimina la tabla
        onCreate(db);  // Vuelve a crearla
    }
}