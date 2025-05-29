package com.example.pasteleriapdm.AdaptersPanel;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pasteleriapdm.R;
import com.example.pasteleriapdm.Utils.DatabaseHelper;

public class UsuarioPaneleAdapter extends RecyclerView.Adapter<UsuarioPaneleAdapter.ViewHolderUsuarioPaneleAdapter> {
    private static final String TAG = "UsuarioPaneleAdapter";
    private Context context;
    private FragmentManager fragmentManager;
    private DatabaseHelper databaseHelper;
    private int totalUsuarios = 0;

    public UsuarioPaneleAdapter(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.databaseHelper = DatabaseHelper.getInstance();

        // Cargar el total de usuarios
        obtenerTotalUsuarios();
    }

    @NonNull
    @Override
    public UsuarioPaneleAdapter.ViewHolderUsuarioPaneleAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_panel_usuario, parent, false);
        return new ViewHolderUsuarioPaneleAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioPaneleAdapter.ViewHolderUsuarioPaneleAdapter holder, int position) {
        // Mostrar el total de usuarios
        holder.lblNombre.setText("Usuarios");
        holder.lblCantidadUsuariosPanel.setText(String.valueOf(totalUsuarios));

        // Opcional: cambiar icono
        holder.imgPastel.setImageResource(R.drawable.img_usuarios);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    /**
     * Obtiene el total de usuarios desde Firebase
     */
    private void obtenerTotalUsuarios() {
        databaseHelper.obtenerTotalUsuarios(new DatabaseHelper.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer total) {
                Log.d(TAG, "Usuarios");
                totalUsuarios = total;
                notifyDataSetChanged(); // Actualiza la vista
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error obteniendo total usuarios: " + error);
                totalUsuarios = 0;
                notifyDataSetChanged();
            }
        });
    }

    /**
     * Metodo publico para actualizar el contador cuando se cree un usuario
     */
    public void actualizarTotal() {
        obtenerTotalUsuarios();
    }

    public class ViewHolderUsuarioPaneleAdapter extends RecyclerView.ViewHolder {
        ImageView imgPastel;
        TextView lblNombre, lblCantidadUsuariosPanel;

        public ViewHolderUsuarioPaneleAdapter(@NonNull View itemView) {
            super(itemView);
            imgPastel = itemView.findViewById(R.id.imagen);
            lblNombre = itemView.findViewById(R.id.lblNombre);
            lblCantidadUsuariosPanel = itemView.findViewById(R.id.lblCantidadUsiariosPanel);
        }
    }
}