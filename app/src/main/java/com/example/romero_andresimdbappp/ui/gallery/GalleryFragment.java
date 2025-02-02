package com.example.romero_andresimdbappp.ui.gallery;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.romero_andresimdbappp.R;
import com.example.romero_andresimdbappp.adapters.MovieAdapter;
import com.example.romero_andresimdbappp.database.FavoritesManager;
import com.example.romero_andresimdbappp.models.Movie;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

//Fragmento para ver y compartir películas favoritas vía Bluetooth.
public class GalleryFragment extends Fragment {
    private RecyclerView recyclerFavorites;
    private MovieAdapter movieAdapter;
    private List<Movie> favoritas = new ArrayList<>();
    private FavoritesManager favoritesManager;
    private TextView vacio;
    private Button btncompartirfav;
    private String jsonFavoritas; // Almacena el JSON al compartir
    // Permisos Bluetooth
    private final ActivityResultLauncher<String[]> bluetoothPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean granted : result.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    shareFavoritesViaBluetooth(jsonFavoritas);
                } else {
                    Toast.makeText(requireContext(), "Permiso Bluetooth necesario.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        recyclerFavorites = root.findViewById(R.id.recyclerView);
        vacio = root.findViewById(R.id.emptyView);
        btncompartirfav = root.findViewById(R.id.btnCompartir);
        recyclerFavorites.setLayoutManager(new GridLayoutManager(getContext(), 2));
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        favoritesManager = new FavoritesManager(getContext());
        // Cargar favoritos
        favoritas = favoritesManager.getFavorites(userEmail);
        updateEmptyViewVisibility();
        // Configurar adaptador
        movieAdapter = new MovieAdapter(favoritas, true);
        recyclerFavorites.setAdapter(movieAdapter);
        btncompartirfav.setOnClickListener(v -> {
            jsonFavoritas = new Gson().toJson(favoritas);
            if (favoritas.isEmpty()) {
                Toast.makeText(requireContext(), "No hay favoritos para compartir.", Toast.LENGTH_SHORT).show();
            } else {
                requestBluetoothPermissions();
            }
        });
        return root;
    }

    //Solicita permisos Bluetooth antes de compartir.
    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothPermissionLauncher.launch(new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            bluetoothPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    //Comparte películas favoritas en formato JSON por Bluetooth.
    private void shareFavoritesViaBluetooth(String jsonFavorites) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Compartir favoritos");
        builder.setMessage(jsonFavorites);
        builder.setPositiveButton("Compartir", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, jsonFavorites);
            intent.setType("text/plain");
            // Buscar apps Bluetooth
            PackageManager packageManager = requireActivity().getPackageManager();
            List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            boolean bluetoothAppFound = false;
            for (ResolveInfo resolveInfo : resolveInfos) {
                String packageName = resolveInfo.activityInfo.packageName;
                if (packageName.toLowerCase().contains("bluetooth")) {
                    intent.setPackage(packageName);
                    bluetoothAppFound = true;
                    break;
                }
            }

            if (bluetoothAppFound) {
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "No se encontró una app Bluetooth.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cerrar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    //Muestra u oculta el mensaje de lista vacía.
    private void updateEmptyViewVisibility() {
        if (favoritas.isEmpty()) {
            vacio.setVisibility(View.VISIBLE);
            recyclerFavorites.setVisibility(View.GONE);
        } else {
            vacio.setVisibility(View.GONE);
            recyclerFavorites.setVisibility(View.VISIBLE);
        }
    }

    //Recarga la lista de favoritos al reanudar el fragmento.
    @Override
    public void onResume() {
        super.onResume();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        favoritas.clear();
        favoritas.addAll(favoritesManager.getFavorites(userEmail));
        movieAdapter.notifyDataSetChanged();

        updateEmptyViewVisibility();
    }

    //Limpia referencias.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerFavorites = null;
        vacio = null;
    }
}
