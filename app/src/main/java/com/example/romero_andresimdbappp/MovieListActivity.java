package com.example.romero_andresimdbappp;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.romero_andresimdbappp.adapters.MovieAdapter;
import com.example.romero_andresimdbappp.api.TMDBApiService;
import com.example.romero_andresimdbappp.models.Movie;
import com.example.romero_andresimdbappp.models.TMDBMovie;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Muestra películas por género y año usando TMDB API.
 */
public class MovieListActivity extends AppCompatActivity {

    private RecyclerView movieRecyclerView;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList = new ArrayList<>();
    private static final String TMDB_API_KEY = "baf7f3ad547bc44a7060a2183991b271"; // API Key TMDB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        movieRecyclerView = findViewById(R.id.recyclerView);
        movieRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        movieAdapter = new MovieAdapter(movieList, false);
        movieRecyclerView.setAdapter(movieAdapter);

        int genreId = getIntent().getIntExtra("GENRE_ID", -1);
        String year = getIntent().getStringExtra("YEAR");

        if (genreId != -1 && year != null) {
            fetchMovies(genreId, year);
        } else {
            Toast.makeText(this, "Faltan parámetros de búsqueda", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Llama a la API de TMDB para buscar películas.
     */
    private void fetchMovies(int genreId, String year) {
        TMDBApiService apiService = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TMDBApiService.class);

        Call<TMDBMovie.MovieSearchResponse> call = apiService.discoverMovies(
                TMDB_API_KEY, "es-ES", "popularity.desc", 1, year, genreId
        );

        call.enqueue(new Callback<TMDBMovie.MovieSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<TMDBMovie.MovieSearchResponse> call,
                                   @NonNull Response<TMDBMovie.MovieSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateMovieList(response.body().getResults());
                } else {
                    Toast.makeText(MovieListActivity.this, "No se encontraron películas.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TMDBMovie.MovieSearchResponse> call, @NonNull Throwable t) {
                Toast.makeText(MovieListActivity.this, "Error al cargar películas.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Actualiza la lista de películas.
     */
    private void updateMovieList(List<TMDBMovie.MovieResult> results) {
        movieList.clear();
        for (TMDBMovie.MovieResult result : results) {
            Movie movie = new Movie();
            movie.setId(String.valueOf(result.getId()));
            movie.setTitle(result.getTitle());
            movie.setImageUrl("https://image.tmdb.org/t/p/w500" + result.getPosterPath());
            movie.setReleaseYear(result.getReleaseDate() != null ? result.getReleaseDate() : "Fecha desconocida");

            // ✅ Verifica si `overview` es null o vacío
            if (result.getOverview() != null && !result.getOverview().isEmpty()) {
                movie.setOverview(result.getOverview());
            } else {
                movie.setOverview("Sin descripción disponible.");
            }

            movie.setRating(String.format("%.1f", result.getVoteAverage()));
            movieList.add(movie);

            // 🔍 Depuración en Logcat
            System.out.println("🎬 " + result.getTitle() + " | 📜 Overview: " + result.getOverview());
        }
        movieAdapter.notifyDataSetChanged();
    }
}
