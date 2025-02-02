package com.example.romero_andresimdbappp;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.romero_andresimdbappp.api.IMDBApiService;
import com.example.romero_andresimdbappp.models.Movie;
import com.example.romero_andresimdbappp.models.MovieOverviewResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieDetailsActivity extends AppCompatActivity {

    private Button btnSms;
    private ImageView movieImageView;
    private TextView movieTitleTextView, moviePlotTextView, movieRatingTextView, movieReleaseDateTextView;
    private static final String API_KEY = "c1cce6d145msh19937f212457748p163fd5jsnb262629a6f45";
    private static final String API_HOST = "imdb-com.p.rapidapi.com";
    private Movie selectedMovie;
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean granted : result.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }

                if (allGranted) {
                    openContactPicker();
                } else {
                    Toast.makeText(this, "Permisos denegados. No se puede enviar SMS.", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> contactPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri contactUri = result.getData().getData();
                    if (contactUri != null) {
                        retrievePhoneNumber(contactUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        movieImageView = findViewById(R.id.movie_image);
        movieTitleTextView = findViewById(R.id.movie_title);
        moviePlotTextView = findViewById(R.id.movie_plot);
        movieRatingTextView = findViewById(R.id.movie_rating);
        movieReleaseDateTextView = findViewById(R.id.movie_release_date);
        btnSms = findViewById(R.id.btnSms);
        selectedMovie = getIntent().getParcelableExtra("MOVIE_DATA");
        if (selectedMovie != null) {
            displayMovieDetails(selectedMovie);
            fetchMovieDetailsFromAPI(selectedMovie.getId());
        }
        btnSms.setOnClickListener(v -> checkPermissionsAndSendSms());
    }

    private void displayMovieDetails(Movie movie) {
        Glide.with(this)
                .load(movie.getImageUrl())
                .placeholder(R.drawable.default_user_image)
                .into(movieImageView);

        movieTitleTextView.setText(movie.getTitle() != null ? movie.getTitle() : "Título desconocido");
        moviePlotTextView.setText(movie.getOverview() != null ? movie.getOverview() : "Sin descripción");
        movieReleaseDateTextView.setText(movie.getReleaseYear() != null ? "Release Date: " + movie.getReleaseYear() : "Fecha desconocida");
        movieRatingTextView.setText(movie.getRating() != null ? "Rating: " + movie.getRating() : "Sin rating");
    }

    private void fetchMovieDetailsFromAPI(String movieId) {
        IMDBApiService apiService = new Retrofit.Builder()
                .baseUrl("https://imdb-com.p.rapidapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(IMDBApiService.class);

        Call<MovieOverviewResponse> call = apiService.getOverview(API_KEY, API_HOST, movieId);

        call.enqueue(new Callback<MovieOverviewResponse>() {
            @Override
            public void onResponse(Call<MovieOverviewResponse> call, Response<MovieOverviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieOverviewResponse.Data data = response.body().getData();
                    updateUIWithDetails(data);

                    if (selectedMovie != null) {
                        if (data.getTitle() != null) {
                            MovieOverviewResponse.RatingsSummary ratings = data.getTitle().getRatingsSummary();
                            if (ratings != null) {
                                selectedMovie.setRating(String.valueOf(ratings.getAggregateRating()));
                            }

                            MovieOverviewResponse.Plot plot = data.getTitle().getPlot();
                            if (plot != null && plot.getPlotText() != null) {

                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<MovieOverviewResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void updateUIWithDetails(MovieOverviewResponse.Data data) {
        if (data.getTitle() != null && data.getTitle().getTitleText() != null) {
            movieTitleTextView.setText(data.getTitle().getTitleText().getText());
        }

        if (data.getTitle() != null && data.getTitle().getReleaseDate() != null) {
            MovieOverviewResponse.ReleaseDate releaseDate = data.getTitle().getReleaseDate();
            String formattedDate = releaseDate.getDay() + "/" + releaseDate.getMonth() + "/" + releaseDate.getYear();
            movieReleaseDateTextView.setText("Release Date: " + formattedDate);
        }

        if (data.getTitle() != null && data.getTitle().getRatingsSummary() != null) {
            movieRatingTextView.setText("Rating: " + data.getTitle().getRatingsSummary().getAggregateRating());
        }

        if (data.getTitle() != null && data.getTitle().getPlot() != null && data.getTitle().getPlot().getPlotText() != null) {
            moviePlotTextView.setText(data.getTitle().getPlot().getPlotText().getPlainText());
        }
    }

    private void checkPermissionsAndSendSms() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS});
        } else {
            openContactPicker();
        }
    }

    private void openContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        contactPickerLauncher.launch(intent);
    }

    private void retrievePhoneNumber(Uri contactUri) {
        String phoneNumber = null;

        try (Cursor cursor = getContentResolver().query(contactUri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                phoneNumber = cursor.getString(numberIndex);
            }
        }

        if (phoneNumber != null) {
            sendSms(phoneNumber, selectedMovie);
        } else {
            Toast.makeText(this, "No se encontró un número de teléfono válido.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSms(String phoneNumber, Movie movie) {
        if (movie == null) {
            Toast.makeText(this, "No se pudo obtener la información de la película.", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = movie.getTitle() != null ? movie.getTitle() : "Título desconocido";
        String rating = movie.getRating() != null ? movie.getRating() : "Sin rating";
        String overview = movie.getOverview() != null ? movie.getOverview() : "Sin descripción";
        String releaseDate = movie.getReleaseYear() != null ? movie.getReleaseYear() : "Fecha desconocida";

        String message = "Esta película te gustará: " + title +
                "\nRating: " + rating +
                "\n" + overview +
                "\nRelease Date: " + releaseDate;

        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + phoneNumber));
        smsIntent.putExtra("sms_body", message);

        try {
            startActivity(smsIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Error al abrir la aplicación de mensajería.", Toast.LENGTH_SHORT).show();
        }
    }
}
