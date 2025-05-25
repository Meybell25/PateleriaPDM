package com.example.pasteleriapdm.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pasteleriapdm.Dialogs.PastelesDialog;
import com.example.pasteleriapdm.Dialogs.UsuariosDialog;
import com.example.pasteleriapdm.R;

public class GestionarUsuariosAdapter extends RecyclerView.Adapter<GestionarUsuariosAdapter.ViewHolderGestionarUsuariosAdapter> {
    private Context context;
    private FragmentManager fragmentManager;

    public GestionarUsuariosAdapter(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public GestionarUsuariosAdapter.ViewHolderGestionarUsuariosAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_usuarios, parent, false);
        return new ViewHolderGestionarUsuariosAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GestionarUsuariosAdapter.ViewHolderGestionarUsuariosAdapter holder, int position) {
        // Simular datos de ejemplo para el usuario
        holder.lblNombreCompleto.setText("Carlos López");
        holder.lblRol.setText("Rol: Administrador");
        holder.lblNotasAdicionales.setText("Notas: Tiene permisos completos");
        holder.lblCreatedAt.setText("Creado: 2024-01-01 12:00");
        holder.lblLastLogin.setText("Último acceso: 2025-05-24 08:30");
        holder.lblEstadoUsuario.setText("Activo");
        holder.lblEstadoUsuario.setTextColor(Color.parseColor("#388E3C"));


        //EVENTO DEL BOTON EDITAR
        holder.btnEditarUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsuariosDialog usuariosDialog = new UsuariosDialog();
                usuariosDialog.show(fragmentManager, "editar");
            }
        });

        //EVENTO DEL BOTON ELIMINAR (PERO SOLO EL DIALOGO)
        holder.btnEliminarUsuario.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("¿Eliminar Pastel?")
                    .setMessage("¿Estás seguro de que deseas eliminar este usuario ")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        Toast.makeText(context, "Elimindo", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public class ViewHolderGestionarUsuariosAdapter extends RecyclerView.ViewHolder {
        ImageView imgUsuario;
        TextView lblNombreCompleto;
        TextView lblRol;
        TextView lblNotasAdicionales;
        TextView lblCreatedAt;
        TextView lblLastLogin;
        TextView lblEstadoUsuario;

        ImageButton btnEditarUsuario;
        ImageButton btnEliminarUsuario;
        public ViewHolderGestionarUsuariosAdapter(@NonNull View itemView) {
            super(itemView);
            // Asociación con el layout
            imgUsuario = itemView.findViewById(R.id.imgUsuario);
            lblNombreCompleto = itemView.findViewById(R.id.lblNombreCompleto);
            lblRol = itemView.findViewById(R.id.lblRol);
            lblNotasAdicionales = itemView.findViewById(R.id.lblNotasAdicionales);
            lblCreatedAt = itemView.findViewById(R.id.lblCreatedAt);
            lblLastLogin = itemView.findViewById(R.id.lblLastLogin);
            lblEstadoUsuario = itemView.findViewById(R.id.lblEstadoUsuario);
            btnEditarUsuario = itemView.findViewById(R.id.btnEditarUsuario);
            btnEliminarUsuario = itemView.findViewById(R.id.btnEliminarUsuario);
        }

    }
}
