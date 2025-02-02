package com.example.romero_andresimdbappp.database;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.romero_andresimdbappp.models.Movie;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestor de favoritos para manejar la base de datos SQLite.
 * Permite agregar, eliminar y obtener películas favoritas de un usuario.
 */
public class FavoritesManager {
    private final FavoritesDatabaseHelper dbHelper;

    public FavoritesManager(Context context) {
        dbHelper = new FavoritesDatabaseHelper(context);
    }

    /**
     * Agrega una película a la lista de favoritos del usuario.
     * Si la película ya existe, evita duplicados usando CONFLICT_IGNORE.
     *
     * @param movie Objeto Movie con la información de la película.
     * @param userEmail Correo del usuario asociado a la película favorita.
     */
    public void addFavorite(Movie movie, String userEmail) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FavoritesDatabaseHelper.COLUMN_ID, movie.getId());
        values.put(FavoritesDatabaseHelper.COLUMN_USER_EMAIL, userEmail);
        values.put(FavoritesDatabaseHelper.COLUMN_TITLE, movie.getTitle());
        values.put(FavoritesDatabaseHelper.COLUMN_IMAGE_URL, movie.getImageUrl());
        values.put(FavoritesDatabaseHelper.COLUMN_RELEASE_DATE, movie.getReleaseYear());
        values.put(FavoritesDatabaseHelper.COLUMN_RATING, movie.getRating());

        // Inserta sin duplicados
        db.insertWithOnConflict(
                FavoritesDatabaseHelper.TABLE_FAVORITES,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
        db.close();
    }

    /**
     * Elimina una película de los favoritos del usuario.
     *
     * @param movieId ID de la película a eliminar.
     * @param userEmail Correo del usuario propietario del favorito.
     */
    public void removeFavorite(String movieId, String userEmail) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(
                FavoritesDatabaseHelper.TABLE_FAVORITES,
                FavoritesDatabaseHelper.COLUMN_ID + "=? AND " +
                        FavoritesDatabaseHelper.COLUMN_USER_EMAIL + "=?",
                new String[]{movieId, userEmail}
        );
        db.close();
    }

    /**
     * Obtiene todas las películas favoritas de un usuario.
     *
     * @param userEmail Correo del usuario para recuperar sus películas favoritas.
     * @return Lista de películas favoritas.
     */
    public List<Movie> getFavorites(String userEmail) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Movie> favoriteMovies = new ArrayList<>();

        Cursor cursor = db.query(
                FavoritesDatabaseHelper.TABLE_FAVORITES,
                null,
                FavoritesDatabaseHelper.COLUMN_USER_EMAIL + "=?",
                new String[]{userEmail},
                null,
                null,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Movie movie = new Movie();
                movie.setId(cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_ID)));
                movie.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_TITLE)));
                movie.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_IMAGE_URL)));
                movie.setReleaseYear(cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_RELEASE_DATE)));
                movie.setRating(cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_RATING)));
                favoriteMovies.add(movie);
            }
            cursor.close();
        }

        db.close();
        return favoriteMovies;
    }
}
