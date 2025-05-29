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
    //VARIBALES ESTAICAS
    private static final String TAG = "GestionarUsuariosAdapter";
    //VARIBALES NORMALES
    private Context context;
    private FragmentManager fragmentManager;
    private List<User> listaUsuarios;
    private DatabaseHelper databaseHelper;
    private SimpleDateFormat dateFormat;
    private OnUsuarioActionListener listener;

    // Interface para comunicacion con el Fragment
    public interface OnUsuarioActionListener {
        void onUsuarioEliminado(String uid);
        void onUsuarioActualizado(User usuario);
    }
    public GestionarUsuariosAdapter(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.listaUsuarios = new ArrayList<>();
        this.databaseHelper = DatabaseHelper.getInstance();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    // Setter para el listener
    public void setOnUsuarioActionListener(OnUsuarioActionListener listener) {
        this.listener = listener;
    }

    // Metodo para actualizar la lista de usuarios
    public void actualizarLista(List<User> nuevaLista) {
        this.listaUsuarios.clear();
        if (nuevaLista != null) {
            this.listaUsuarios.addAll(nuevaLista);
        }
        notifyDataSetChanged();
        Log.d(TAG, "Lista actualizada con " + this.listaUsuarios.size() + " usuarios");
    }

    // Metodo para agregar un usuario
    public void agregarUsuario(User usuario) {
        if (usuario != null) {
            this.listaUsuarios.add(usuario);
            notifyItemInserted(listaUsuarios.size() - 1);
            Log.d(TAG, "Usuario agregado: " + usuario.getName());
        }
    }

    // Metodo para actualizar un usuario específico
    public void actualizarUsuario(User usuarioActualizado) {
        if (usuarioActualizado == null) return;

        for (int i = 0; i < listaUsuarios.size(); i++) {
            if (listaUsuarios.get(i).getUid().equals(usuarioActualizado.getUid())) {
                listaUsuarios.set(i, usuarioActualizado);
                notifyItemChanged(i);
                Log.d(TAG, "Usuario actualizado: " + usuarioActualizado.getName());
                break;
            }
        }
    }

    // Metodo para eliminar  usuario
    public void eliminarUsuario(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            Log.w(TAG, "UID nulo o vacío para eliminar");
            return;
        }

        for (int i = 0; i < listaUsuarios.size(); i++) {
            if (listaUsuarios.get(i).getUid().equals(uid)) {
                User usuarioEliminado = listaUsuarios.get(i);
                listaUsuarios.remove(i);
                notifyItemRemoved(i);
                // Notificar cambios en los elementos posteriores
                if (i < listaUsuarios.size()) {
                    notifyItemRangeChanged(i, listaUsuarios.size() - i);
                }
                Log.d(TAG, "Usuario eliminado del adapter: " + usuarioEliminado.getName());
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
        if (position >= listaUsuarios.size()) {
            Log.w(TAG, "Posición inválida: " + position + ", tamaño lista: " + listaUsuarios.size());
            return;
        }

        User usuario = listaUsuarios.get(position);
        if (usuario == null) {
            Log.w(TAG, "Usuario nulo en posición: " + position);
            return;
        }

        // Asignar datos del usuario
        holder.lblNombreCompleto.setText(usuario.getName());

        // Formatear rol con color distintivo
        String rolFormateado = formatearRol(usuario.getRole());
        holder.lblRol.setText("Rol: " + rolFormateado);
        configurarColorRol(holder, usuario.getRole());

        // Mostrar contraseña
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            holder.lblOlvidePassword.setText("Contraseña: ****");
            holder.lblOlvidePassword.setTag(false); // false = oculta, true = visible
        } else {
            holder.lblOlvidePassword.setText("Contraseña: No disponible");
        }

        // Formatear fecha de creacion
        if (usuario.getCreatedAt() > 0) {
            String fechaCreacion = dateFormat.format(new Date(usuario.getCreatedAt()));
            holder.lblCreatedAt.setText("Creado: " + fechaCreacion);
        } else {
            holder.lblCreatedAt.setText("Creado: Fecha no disponible");
        }

        // Formatear ultimo login
        if (usuario.getLastLogin() > 0) {
            String ultimoLogin = dateFormat.format(new Date(usuario.getLastLogin()));
            holder.lblLastLogin.setText("Último acceso: " + ultimoLogin);
        } else {
            holder.lblLastLogin.setText("Último acceso: Nunca");
        }

        // Configurar estado del usuario
        configurarEstadoUsuario(holder, usuario);

        // Configurar imagen segun el rol
        configurarImagenUsuario(holder, usuario);

        // Configurar eventos
        configurarEventos(holder, usuario, position);
    }

    //Metodo para configurar el Rol
    private void configurarColorRol(ViewHolderGestionarUsuariosAdapter holder, String rol) {
        switch (rol) {
            case User.ROLE_ADMIN:
                holder.lblRol.setTextColor(Color.parseColor("#FF69B4")); //  Fusia  para admin
                break;
            case User.ROLE_SELLER:
                holder.lblRol.setTextColor(Color.parseColor("#C68EFD")); // Purpura  para vendedor
                break;
            case User.ROLE_PRODUCTION:
                holder.lblRol.setTextColor(Color.parseColor("#8B4513")); // Marron para producción
                break;
            default:
                holder.lblRol.setTextColor(Color.parseColor("#666666")); // Gris por defecto
                break;
        }
    }

    //Metodo para configurar el evento
    private void configurarEventos(ViewHolderGestionarUsuariosAdapter holder, User usuario, int position) {
        // Limpiar listeners anteriores
        holder.btnEditarUsuario.setOnClickListener(null);
        holder.btnEliminarUsuario.setOnClickListener(null);
        holder.lblOlvidePassword.setOnClickListener(null);

        // Evento del boton editar
        holder.btnEditarUsuario.setOnClickListener(v -> {
            UsuariosDialog usuariosDialog = new UsuariosDialog(usuario);
            usuariosDialog.setUsuarioDialogListener(new UsuariosDialog.UsuarioDialogListener() {
                @Override
                public void onUsuarioCreado(User usuario) {
                    // No se usa en edicion
                }

                @Override
                public void onUsuarioActualizado(User usuarioActualizado) {
                    actualizarUsuario(usuarioActualizado);
                    // Notificar al Fragment
                    if (listener != null) {
                        listener.onUsuarioActualizado(usuarioActualizado);
                    }
                    //Toast.makeText(context, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show();
                }
            });
            usuariosDialog.show(fragmentManager, "editarUsuario");
        });

        // CONFIGURAR BOTON ELIMINAR
        String status = usuario.getStatus();
        boolean esInactivo = User.STATUS_INACTIVE.equals(status);

        if (esInactivo) {
            // Usuario inactivo - permitir eliminación
            holder.btnEliminarUsuario.setEnabled(true);
            holder.btnEliminarUsuario.setAlpha(1.0f);
            holder.btnEliminarUsuario.setOnClickListener(v -> {
                // Obtener posicion actual
                int posicionActual = holder.getAdapterPosition();
                if (posicionActual != RecyclerView.NO_POSITION && posicionActual < listaUsuarios.size()) {
                    User usuarioActual = listaUsuarios.get(posicionActual);
                    mostrarDialogoEliminar(usuarioActual, posicionActual);
                }
            });
        } else {
            // Usuario activo o bloqueado no permitir eliminación
            holder.btnEliminarUsuario.setEnabled(false);
            holder.btnEliminarUsuario.setAlpha(0.5f);
            holder.btnEliminarUsuario.setOnClickListener(v -> mostrarMensajeNoSePuedeEliminar(usuario));
        }

        // Evento para mostrar/ocultar contraseña
        holder.lblOlvidePassword.setOnClickListener(v -> {
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                boolean esVisible = holder.lblOlvidePassword.getTag() != null && (Boolean) holder.lblOlvidePassword.getTag();
                if (esVisible) {
                    // Ocultar contraseña
                    holder.lblOlvidePassword.setText("Contraseña: ****");
                    holder.lblOlvidePassword.setTag(false);
                } else {
                    // Mostrar contraseña real
                    holder.lblOlvidePassword.setText("Contraseña: " + usuario.getPassword());
                    holder.lblOlvidePassword.setTag(true);
                }
            }
        });
    }

    private void mostrarMensajeNoSePuedeEliminar(User usuario) {
        String estadoActual = usuario.getStatus().equals(User.STATUS_ACTIVE) ? "Activo" : "Bloqueado";

        new AlertDialog.Builder(context)
                .setTitle("No se puede eliminar")
                .setMessage("El usuario '" + usuario.getName() + "' tiene estado '" + estadoActual + "'.\n\n" +
                        "Para eliminarlo, primero debe cambiar su estado a 'Inactivo' " +
                        "editando el usuario.")
                .setPositiveButton("Entendido", null)
                .setNeutralButton("Editar ahora", (dialog, which) -> {
                    UsuariosDialog usuariosDialog = new UsuariosDialog(usuario);
                    usuariosDialog.setUsuarioDialogListener(new UsuariosDialog.UsuarioDialogListener() {
                        @Override
                        public void onUsuarioCreado(User usuario) {
                            // No se usa en edicion
                        }
                        @Override
                        public void onUsuarioActualizado(User usuarioActualizado) {
                            actualizarUsuario(usuarioActualizado);
                            if (listener != null) {
                                listener.onUsuarioActualizado(usuarioActualizado);
                            }
                            Toast.makeText(context, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show();
                        }
                    });
                    usuariosDialog.show(fragmentManager, "editarUsuario");
                })
                .show();
    }
    private void configurarEstadoUsuario(ViewHolderGestionarUsuariosAdapter holder, User usuario) {
        switch (usuario.getStatus()) {
            case User.STATUS_ACTIVE:
                holder.lblEstadoUsuario.setText("✓ Activo");
                holder.lblEstadoUsuario.setTextColor(Color.parseColor("#388E3C")); // Verde
                break;
            case User.STATUS_INACTIVE:
                holder.lblEstadoUsuario.setText("⏸ Inactivo");
                holder.lblEstadoUsuario.setTextColor(Color.parseColor("#FF9800")); // Naranja
                break;
            case User.STATUS_BLOCKED:
                holder.lblEstadoUsuario.setText("🚫 Bloqueado");
                holder.lblEstadoUsuario.setTextColor(Color.parseColor("#F44336")); // Rojo
                break;
            default:
                holder.lblEstadoUsuario.setText("? Desconocido");
                holder.lblEstadoUsuario.setTextColor(Color.parseColor("#666666")); // Gris
                break;
        }
    }

    //Metodo para configurar la imagen de usuario la q le corresponde segun su rol
    private void configurarImagenUsuario(ViewHolderGestionarUsuariosAdapter holder, User usuario) {
        switch (usuario.getRole()) {
            case User.ROLE_ADMIN:
                holder.imgUsuario.setImageResource(R.drawable.ic_admin);
                holder.imgUsuario.setColorFilter(Color.parseColor("#FF69B4")); // Fusia Suave
                break;
            case User.ROLE_SELLER:
                holder.imgUsuario.setImageResource(R.drawable.ic_seller);
                holder.imgUsuario.setColorFilter(Color.parseColor("#C68EFD")); // Purpura suvae
                break;
            case User.ROLE_PRODUCTION:
                holder.imgUsuario.setImageResource(R.drawable.ic_production);
                holder.imgUsuario.setColorFilter(Color.parseColor("#8B4513")); //  marron
                break;
            default:
                holder.imgUsuario.setImageResource(R.drawable.ic_usuario);
                holder.imgUsuario.setColorFilter(Color.parseColor("#666666")); // Gris
                break;
        }
    }

    //Metodo para formatear el rol
    private String formatearRol(String rol) {
        switch (rol) {
            case User.ROLE_ADMIN:
                return "👑 Administrador";
            case User.ROLE_SELLER:
                return "🛒 Vendedor";
            case User.ROLE_PRODUCTION:
                return "🏭 Producción";
            default:
                return rol;
        }
    }

    //Metodo para mostar el dialogo Eliminar
    private void mostrarDialogoEliminar(User usuario, int position) {
        String mensajeExtra = User.ROLE_ADMIN.equals(usuario.getRole())
                ? "\n\n⚠️ Este es un usuario ADMINISTRADOR INACTIVO"
                : "";

        new AlertDialog.Builder(context)
                .setTitle("🗑️ ¿Eliminar Usuario Permanentemente?")
                .setMessage("¿Estás seguro de que deseas ELIMINAR PERMANENTEMENTE al usuario " + usuario.getName() + "?" + mensajeExtra + "\n\n⚠️ ESTA ACCIÓN NO SE PUEDE DESHACER\n\nEl usuario será eliminado completamente del sistema.")
                .setPositiveButton("Sí, Eliminar", (dialog, which) -> {
                    eliminarUsuarioPermanentemente(usuario, position);
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(R.drawable.ic_warning)
                .show();
    }

    //Metodo para eliminar usuario permanetemente
    private void eliminarUsuarioPermanentemente(User usuario, int position) {
        Log.d(TAG, "Eliminando usuario permanentemente: " + usuario.getName() + " (UID: " + usuario.getUid() + ")");

        // Validaciones previas
        if (usuario.getUid() == null || usuario.getUid().trim().isEmpty()) {
            Toast.makeText(context, "Error: UID del usuario no valido", Toast.LENGTH_LONG).show();
            return;
        }

        if (!User.STATUS_INACTIVE.equals(usuario.getStatus())) {
            Toast.makeText(context, "Error: Solo se pueden eliminar usuarios inactivos", Toast.LENGTH_LONG).show();
            return;
        }

        databaseHelper.deleteUser(usuario.getUid(), new DatabaseHelper.DatabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean eliminado) {
                if (eliminado) {
                    Log.d(TAG, "Usuario eliminado exitosamente de la base de datos");

                    // Notificar al Fragment primero
                    if (listener != null) {
                        listener.onUsuarioEliminado(usuario.getUid());
                    }
                    Toast.makeText(context, "Usuario " + usuario.getName() + " eliminado permanentemente", Toast.LENGTH_SHORT).show();
                } else {
                    Log.w(TAG, "La eliminación devolvió false");
                    Toast.makeText(context, " Error: No se pudo eliminar el usuario", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error eliminando usuario: " + error);
                Toast.makeText(context, "Error eliminando usuario: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public int getItemCount() {
        return listaUsuarios != null ? listaUsuarios.size() : 0;
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