package com.example.romero_andresimdbappp.database;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Administrador de base de datos para gestionar películas favoritas.
 * Maneja la creación, actualización y degradación de la base de datos.
 */
public class FavoritesDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favoritas_db"; // Cambio de nombre a "favoritas_db"
    private static final int DATABASE_VERSION = 2;
    public static final String TABLE_FAVORITES = "favorites";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_EMAIL = "user_email";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_IMAGE_URL = "image_url";
    public static final String COLUMN_RELEASE_DATE = "release_date";
    public static final String COLUMN_RATING = "rating";

    /**
     * Sentencia SQL para la creación de la tabla de favoritos.
     * Almacena ID de película, usuario, título, imagen, fecha y calificación.
     */
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_FAVORITES + " (" +
                    COLUMN_ID + " TEXT, " +
                    COLUMN_USER_EMAIL + " TEXT, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_IMAGE_URL + " TEXT, " +
                    COLUMN_RELEASE_DATE + " TEXT, " +
                    COLUMN_RATING + " TEXT, " +
                    "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_USER_EMAIL + "));";

    public FavoritesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Se ejecuta al crear la base de datos por primera vez.
     * Crea la tabla de películas favoritas.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    /**
     * Se activa cuando la versión de la base de datos cambia.
     * Se elimina la tabla y se recrea para reflejar cambios en la estructura.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
            onCreate(db);
        }
    }

    /**
     * Se ejecuta si la versión de la base de datos se intenta degradar.
     * Borra y vuelve a crear la tabla para garantizar compatibilidad.
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }
}
