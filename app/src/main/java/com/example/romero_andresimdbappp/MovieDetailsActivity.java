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
    // Declaración de vistas y variables
    private TextView titulopelicula, resumenpelicula, calificacionpelicula, fechalanzamiento;
    private ImageView imagenpelicula;
    private Button btnSms;
    private static final String API_CLAVE = "c1cce6d145msh19937f212457748p163fd5jsnb262629a6f45";
    private static final String API_HOST = "imdb-com.p.rapidapi.com";
    private Movie pelicula;

    // Launcher para pedir permisos
    private final ActivityResultLauncher<String[]> lanzador =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), resultado -> {
                boolean Permisos = true;
                for (Boolean concedido : resultado.values()) {
                    if (!concedido) {
                        Permisos = false;
                        break;
                    }
                }
                if (Permisos) {
                    abrirSelectorDeContactos(); // Si tiene permisos, abre contactos
                } else {
                    Toast.makeText(this, "Permisos denegados. No se puede enviar SMS.", Toast.LENGTH_SHORT).show();
                }
            });

    // Launcher para seleccionar un contacto
    private final ActivityResultLauncher<Intent> SelectorContactos =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == Activity.RESULT_OK && resultado.getData() != null) {
                    Uri uriContacto = resultado.getData().getData();
                    if (uriContacto != null) {
                        recuperarNumeroTelefono(uriContacto);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // Inicialización de vistas
        imagenpelicula = findViewById(R.id.movie_image);
        titulopelicula = findViewById(R.id.movie_title);
        resumenpelicula = findViewById(R.id.movie_plot);
        calificacionpelicula = findViewById(R.id.movie_rating);
        fechalanzamiento = findViewById(R.id.movie_release_date);
        btnSms = findViewById(R.id.btnSms);

        // Obtener película desde Parcelable
        pelicula = getIntent().getParcelableExtra("MOVIE_DATA");
        if (pelicula != null) {
            mostrarDetallesPelicula(pelicula);
            obtenerDetallesPeliculaDeApi(pelicula.getId());
        }

        // Botón para enviar SMS
        btnSms.setOnClickListener(v -> verificarPermisosYEnviarSms());
    }

    // Muestra detalles básicos de la película
    private void mostrarDetallesPelicula(Movie pelicula) {
        Glide.with(this)
                .load(pelicula.getImageUrl())
                .placeholder(R.drawable.default_user_image)
                .into(imagenpelicula);
        titulopelicula.setText(pelicula.getTitle() != null ? pelicula.getTitle() : "Título desconocido");
        resumenpelicula.setText(pelicula.getOverview() != null ? pelicula.getOverview() : "Sin descripción");
        fechalanzamiento.setText(pelicula.getReleaseYear() != null ? "Fecha de lanzamiento: " + pelicula.getReleaseYear() : "Fecha desconocida");
        calificacionpelicula.setText(pelicula.getRating() != null ? "Calificación: " + pelicula.getRating() : "Sin calificación");
    }

    // Llama a la API para obtener detalles adicionales de la película
    private void obtenerDetallesPeliculaDeApi(String idPelicula) {
        IMDBApiService apiService = new Retrofit.Builder()
                .baseUrl("https://imdb-com.p.rapidapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(IMDBApiService.class);

        Call<MovieOverviewResponse> llamada = apiService.getOverview(API_CLAVE, API_HOST, idPelicula);

        llamada.enqueue(new Callback<MovieOverviewResponse>() {
            @Override
            public void onResponse(Call<MovieOverviewResponse> call, Response<MovieOverviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieOverviewResponse.Data datos = response.body().getData();
                    actualizarInterfazConDetalles(datos);
                }
            }

            @Override
            public void onFailure(Call<MovieOverviewResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // Actualiza la interfaz con los datos obtenidos de la API
    private void actualizarInterfazConDetalles(MovieOverviewResponse.Data datos) {
        if (datos.getTitle() != null && datos.getTitle().getTitleText() != null) {
            titulopelicula.setText(datos.getTitle().getTitleText().getText());
        }

        if (datos.getTitle() != null && datos.getTitle().getReleaseDate() != null) {
            MovieOverviewResponse.ReleaseDate fechaLanzamiento = datos.getTitle().getReleaseDate();
            String fechaFormateada = fechaLanzamiento.getDay() + "/" + fechaLanzamiento.getMonth() + "/" + fechaLanzamiento.getYear();
            fechalanzamiento.setText("Fecha de lanzamiento: " + fechaFormateada);
        }

        if (datos.getTitle() != null && datos.getTitle().getRatingsSummary() != null) {
            calificacionpelicula.setText("Calificación: " + datos.getTitle().getRatingsSummary().getAggregateRating());
        }

        if (datos.getTitle() != null && datos.getTitle().getPlot() != null && datos.getTitle().getPlot().getPlotText() != null) {
            resumenpelicula.setText(datos.getTitle().getPlot().getPlotText().getPlainText());
        }
    }

    // Verifica permisos antes de enviar SMS
    private void verificarPermisosYEnviarSms() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            lanzador.launch(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS});
        } else {
            abrirSelectorDeContactos();
        }
    }

    // Abre la agenda de contactos
    private void abrirSelectorDeContactos() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        SelectorContactos.launch(intent);
    }

    // Recupera el número de teléfono del contacto seleccionado
    private void recuperarNumeroTelefono(Uri uriContacto) {
        String numeroTelefono = null;
        try (Cursor cursor = getContentResolver().query(uriContacto, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int indiceNumero = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                numeroTelefono = cursor.getString(indiceNumero);
            }
        }
        if (numeroTelefono != null) {
            enviarSms(numeroTelefono, pelicula);
        } else {
            Toast.makeText(this, "No se encontró un número de teléfono válido.", Toast.LENGTH_SHORT).show();
        }
    }

    // Envía un SMS con la información de la película
    private void enviarSms(String numeroTelefono, Movie pelicula) {
        String mensaje = "Esta película te gustará: " + pelicula.getTitle() + "\nCalificación: " + pelicula.getRating() +
                "\n" + pelicula.getOverview() + "\nFecha de lanzamiento: " + pelicula.getReleaseYear();
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + numeroTelefono));
        smsIntent.putExtra("sms_body", mensaje);
        try {
            startActivity(smsIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Error al abrir la aplicación de mensajería.", Toast.LENGTH_SHORT).show();
        }
    }
}
