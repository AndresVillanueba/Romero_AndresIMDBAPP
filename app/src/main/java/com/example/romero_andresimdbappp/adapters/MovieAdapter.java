package com.example.romero_andresimdbappp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.romero_andresimdbappp.MovieDetailsActivity;
import com.example.romero_andresimdbappp.R;
import com.example.romero_andresimdbappp.database.FavoritesManager;
import com.example.romero_andresimdbappp.models.Movie;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * Adaptador para mostrar películas en un RecyclerView.
 * Soporta clic largo para agregar/eliminar favoritos.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private List<Movie> movieList;
    private Context context;
    private boolean isFavoritesMode; // Indica si se usa en la lista de favoritos

    public MovieAdapter(List<Movie> movieList, boolean isFavoritesMode) {
        this.movieList = movieList;
        this.isFavoritesMode = isFavoritesMode;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        // Cargar imagen con Glide
        Glide.with(holder.itemView.getContext())
                .load(movie.getImageUrl())
                .into(holder.imageView);

        // Clic corto: abrir detalles
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailsActivity.class);
            intent.putExtra("MOVIE_DATA", movie);
            context.startActivity(intent);
        });

        // 🔹 Clic largo: Agregar o eliminar de favoritos
        holder.itemView.setOnLongClickListener(v -> {
            FavoritesManager favoritesManager = new FavoritesManager(context);
            String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            if (isFavoritesMode) {
                // Modo favoritos: Eliminar de favoritos
                favoritesManager.removeFavorite(movie.getId(), userEmail);
                movieList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, movieList.size());
                Toast.makeText(context, "Eliminada de favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
            } else {
                // Modo normal: Agregar a favoritos
                favoritesManager.addFavorite(movie, userEmail);
                Toast.makeText(context, "Agregada a favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
            }
            return true; // 🔹 IMPORTANTE: Devuelve `true` para que OnLongClick funcione
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    // ViewHolder para representar una película en la lista
    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.movie_image);
        }
    }
}
