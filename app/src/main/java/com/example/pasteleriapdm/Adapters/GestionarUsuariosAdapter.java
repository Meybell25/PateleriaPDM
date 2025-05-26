package com.example.pasteleriapdm.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pasteleriapdm.Dialogs.UsuariosDialog;
import com.example.pasteleriapdm.Models.User;
import com.example.pasteleriapdm.R;
import com.example.pasteleriapdm.Utils.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GestionarUsuariosAdapter extends RecyclerView.Adapter<GestionarUsuariosAdapter.ViewHolderGestionarUsuariosAdapter> {
    private static final String TAG = "GestionarUsuariosAdapter";

    private Context context;
    private FragmentManager fragmentManager;
    private List<User> listaUsuarios;
    private DatabaseHelper databaseHelper;
    private SimpleDateFormat dateFormat;

    public GestionarUsuariosAdapter(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.listaUsuarios = new ArrayList<>();
        this.databaseHelper = DatabaseHelper.getInstance();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    // Método para actualizar la lista de usuarios
    public void actualizarLista(List<User> nuevaLista) {
        this.listaUsuarios.clear();
        this.listaUsuarios.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    // Método para agregar un usuario
    public void agregarUsuario(User usuario) {
        this.listaUsuarios.add(usuario);
        notifyItemInserted(listaUsuarios.size() - 1);
    }

    // Método para actualizar un usuario específico
    public void actualizarUsuario(User usuarioActualizado) {
        for (int i = 0; i < listaUsuarios.size(); i++) {
            if (listaUsuarios.get(i).getUid().equals(usuarioActualizado.getUid())) {
                listaUsuarios.set(i, usuarioActualizado);
                notifyItemChanged(i);
                break;
            }
        }
    }

    // Método para eliminar un usuario
    public void eliminarUsuario(String uid) {
        for (int i = 0; i < listaUsuarios.size(); i++) {
            if (listaUsuarios.get(i).getUid().equals(uid)) {
                listaUsuarios.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolderGestionarUsuariosAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_usuarios, parent, false);
        return new ViewHolderGestionarUsuariosAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderGestionarUsuariosAdapter holder, int position) {
        User usuario = listaUsuarios.get(position);

        // Asignar datos del usuario
        holder.lblNombreCompleto.setText(usuario.getName());

        // Formatear rol
        String rolFormateado = formatearRol(usuario.getRole());
        holder.lblRol.setText("Rol: " + rolFormateado);

        // Mostrar contraseña (puede ser sensible, considera ocultarla o mostrar solo parte)
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            holder.lblOlvidePassword.setText("Contraseña: " + usuario.getPassword());
        } else {
            holder.lblOlvidePassword.setText("Contraseña: No disponible");
        }

        // Formatear fecha de creación
        String fechaCreacion = dateFormat.format(new Date(usuario.getCreatedAt()));
        holder.lblCreatedAt.setText("Creado: " + fechaCreacion);

        // Formatear último login
        if (usuario.getLastLogin() > 0) {
            String ultimoLogin = dateFormat.format(new Date(usuario.getLastLogin()));
            holder.lblLastLogin.setText("Último acceso: " + ultimoLogin);
        } else {
            holder.lblLastLogin.setText("Último acceso: Nunca");
        }

        // Configurar estado del usuario
        configurarEstadoUsuario(holder, usuario);

        // Configurar imagen según el rol
        configurarImagenUsuario(holder, usuario);

        // Evento del botón editar
        holder.btnEditarUsuario.setOnClickListener(v -> {
            UsuariosDialog usuariosDialog = new UsuariosDialog(usuario);
            usuariosDialog.setUsuarioDialogListener(new UsuariosDialog.UsuarioDialogListener() {
                @Override
                public void onUsuarioCreado(User usuario) {
                    // No se usa en edición
                }

                @Override
                public void onUsuarioActualizado(User usuarioActualizado) {
                    actualizarUsuario(usuarioActualizado);
                    Toast.makeText(context, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show();
                }
            });
            usuariosDialog.show(fragmentManager, "editarUsuario");
        });

        // Evento del botón eliminar
        holder.btnEliminarUsuario.setOnClickListener(v -> {
            mostrarDialogoEliminar(usuario, position);
        });

        // Evento para mostrar/ocultar contraseña al hacer clic
        holder.lblOlvidePassword.setOnClickListener(v -> {
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                if (holder.lblOlvidePassword.getText().toString().contains("****")) {
                    // Mostrar contraseña real
                    holder.lblOlvidePassword.setText("Contraseña: " + usuario.getPassword());
                } else {
                    // Ocultar contraseña
                    holder.lblOlvidePassword.setText("Contraseña: ****");
                }
            }
        });
    }

    private void configurarEstadoUsuario(ViewHolderGestionarUsuariosAdapter holder, User usuario) {
        switch (usuario.getStatus()) {
            case User.STATUS_ACTIVE:
                holder.lblEstadoUsuario.setText("Activo");
                holder.lblEstadoUsuario.setTextColor(Color.parseColor("#388E3C")); // Verde
                break;
            case User.STATUS_INACTIVE:
                holder.lblEstadoUsuario.setText("Inactivo");
                holder.lblEstadoUsuario.setTextColor(Color.parseColor("#FF9800")); // Naranja
                break;
            case User.STATUS_BLOCKED:
                holder.lblEstadoUsuario.setText("Bloqueado");
                holder.lblEstadoUsuario.setTextColor(Color.parseColor("#F44336")); // Rojo
                break;
            default:
                holder.lblEstadoUsuario.setText("Desconocido");
                holder.lblEstadoUsuario.setTextColor(Color.parseColor("#666666")); // Gris
                break;
        }
    }

    private void configurarImagenUsuario(ViewHolderGestionarUsuariosAdapter holder, User usuario) {
        // Puedes cambiar las imágenes según el rol
        switch (usuario.getRole()) {
            case User.ROLE_ADMIN:
                holder.imgUsuario.setImageResource(R.drawable.ic_admin);
                break;
            case User.ROLE_SELLER:
                holder.imgUsuario.setImageResource(R.drawable.ic_seller);
                break;
            case User.ROLE_PRODUCTION:
                holder.imgUsuario.setImageResource(R.drawable.ic_production);
                break;
            default:
                holder.imgUsuario.setImageResource(R.drawable.ic_usuario);
                break;
        }
    }

    private String formatearRol(String rol) {
        switch (rol) {
            case User.ROLE_ADMIN:
                return "Administrador";
            case User.ROLE_SELLER:
                return "Vendedor";
            case User.ROLE_PRODUCTION:
                return "Producción";
            default:
                return rol;
        }
    }

    private void mostrarDialogoEliminar(User usuario, int position) {
        new AlertDialog.Builder(context)
                .setTitle("¿Eliminar Usuario?")
                .setMessage("¿Estás seguro de que deseas eliminar al usuario " + usuario.getName() + "?\n\nEsta acción no se puede deshacer.")
                .setPositiveButton("Sí, Eliminar", (dialog, which) -> {
                    eliminarUsuarioFirebase(usuario, position);
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(R.drawable.ic_warning)
                .show();
    }

    private void eliminarUsuarioFirebase(User usuario, int position) {
        // Nota: En un sistema real, normalmente no se elimina completamente el usuario
        // sino que se marca como "eliminado" o se desactiva para mantener la integridad referencial

        // Cambiar estado a bloqueado en lugar de eliminar
        usuario.setStatus(User.STATUS_BLOCKED);

        databaseHelper.updateUser(usuario, new DatabaseHelper.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User usuarioActualizado) {
                Log.d(TAG, "Usuario bloqueado exitosamente");
                actualizarUsuario(usuarioActualizado);
                Toast.makeText(context, "Usuario bloqueado correctamente", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error bloqueando usuario: " + error);
                Toast.makeText(context, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public class ViewHolderGestionarUsuariosAdapter extends RecyclerView.ViewHolder {
        ImageView imgUsuario;
        TextView lblNombreCompleto;
        TextView lblRol;
        TextView lblOlvidePassword;
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
            lblOlvidePassword = itemView.findViewById(R.id.lblOlvidePassword);
            lblCreatedAt = itemView.findViewById(R.id.lblCreatedAt);
            lblLastLogin = itemView.findViewById(R.id.lblLastLogin);
            lblEstadoUsuario = itemView.findViewById(R.id.lblEstadoUsuario);
            btnEditarUsuario = itemView.findViewById(R.id.btnEditarUsuario);
            btnEliminarUsuario = itemView.findViewById(R.id.btnEliminarUsuario);
        }
    }
}