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


//MovieAdapter para mostrar las peliculas mediante un RecyclerView y un arraylist.
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private List<Movie> movieList;
    private Context context;
    private boolean Favorites;
    public MovieAdapter(List<Movie> movieList, boolean isFavoritesMode) {
        this.movieList = movieList;
        this.Favorites = isFavoritesMode;
    }
    //Creamos y devolvemos un nuevo ViewHolder para representar un elemento de la lista.
    @Override
    public MovieViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }
    //Asociamos los datos de una película con un ViewHolder.
    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        // Cargar la imagen de la película
        Glide.with(holder.itemView.getContext())
                .load(movie.getImageUrl())
                .into(holder.imageView);

        // Manejar el evento de clic corto para abrir detalles
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailsActivity.class);
            intent.putExtra("MOVIE_DATA", movie); // Pasar el objeto Movie
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            FavoritesManager favoritesManager = new FavoritesManager(context);
            String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if (Favorites) {
                // Modo favoritos: Eliminar de la lista
                favoritesManager.removeFavorite(movie.getId(), userEmail);
                movieList.remove(position); // Eliminar de la lista local
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, movieList.size());
                Toast.makeText(context, "Eliminada de favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
            } else {
                // Modo principal: Agregar a favoritos
                favoritesManager.addFavorite(movie, userEmail);
                Toast.makeText(context, "Agregada a favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
            }
            return true;
        });

    }


    //Número de películas en la lista.
    @Override
    public int getItemCount() {
        return movieList.size();
    }

    //ViewHolder para representar una película en el RecyclerView.
    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public MovieViewHolder( View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.movie_image);
        }
    }
}
