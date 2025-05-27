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
    private OnUsuarioActionListener listener;

    // Interface para comunicación con el Fragment
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
        this.listaUsuarios.addAll(nuevaLista);
        notifyDataSetChanged();
        Log.d(TAG, "Lista actualizada con " + nuevaLista.size() + " usuarios");
    }

    // Metodo para agregar un usuario
    public void agregarUsuario(User usuario) {
        this.listaUsuarios.add(usuario);
        notifyItemInserted(listaUsuarios.size() - 1);
        Log.d(TAG, "Usuario agregado: " + usuario.getName());
    }

    // Metodo para actualizar un usuario específico
    public void actualizarUsuario(User usuarioActualizado) {
        for (int i = 0; i < listaUsuarios.size(); i++) {
            if (listaUsuarios.get(i).getUid().equals(usuarioActualizado.getUid())) {
                listaUsuarios.set(i, usuarioActualizado);
                notifyItemChanged(i);
                Log.d(TAG, "Usuario actualizado: " + usuarioActualizado.getName());
                break;
            }
        }
    }

    // Metodo para eliminar un usuario
    public void eliminarUsuario(String uid) {
        for (int i = 0; i < listaUsuarios.size(); i++) {
            if (listaUsuarios.get(i).getUid().equals(uid)) {
                listaUsuarios.remove(i);
                notifyItemRemoved(i);
                Log.d(TAG, "Usuario eliminado del adapter: " + uid);
                break;
            }
        }
    }

    // Metodo para filtrar usuarios por rol
    public void filtrarPorRol(String rol) {
        List<User> listaFiltrada = new ArrayList<>();

        if ("TODOS".equals(rol)) {
            listaFiltrada.addAll(listaUsuarios);
        } else {
            String rolFiltro = convertirFiltroARol(rol);
            for (User usuario : listaUsuarios) {
                if (rolFiltro.equals(usuario.getRole())) {
                    listaFiltrada.add(usuario);
                }
            }
        }

        actualizarLista(listaFiltrada);
        Log.d(TAG, "Filtro aplicado: " + rol + " - Usuarios encontrados: " + listaFiltrada.size());
    }

    private String convertirFiltroARol(String filtro) {
        switch (filtro) {
            case "ADMINISTRADOR":
                return User.ROLE_ADMIN;
            case "VENDEDOR":
                return User.ROLE_SELLER;
            case "PRODUCCIÓN":
                return User.ROLE_PRODUCTION;
            default:
                return "";
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

        // Configurar eventos
        configurarEventos(holder, usuario, position);
    }

    private void configurarColorRol(ViewHolderGestionarUsuariosAdapter holder, String rol) {
        switch (rol) {
            case User.ROLE_ADMIN:
                holder.lblRol.setTextColor(Color.parseColor("#D32F2F")); // Rojo para admin
                break;
            case User.ROLE_SELLER:
                holder.lblRol.setTextColor(Color.parseColor("#1976D2")); // Azul para vendedor
                break;
            case User.ROLE_PRODUCTION:
                holder.lblRol.setTextColor(Color.parseColor("#388E3C")); // Verde para producción
                break;
            default:
                holder.lblRol.setTextColor(Color.parseColor("#666666")); // Gris por defecto
                break;
        }
    }

    private void configurarEventos(ViewHolderGestionarUsuariosAdapter holder, User usuario, int position) {
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
                    // Notificar al Fragment
                    if (listener != null) {
                        listener.onUsuarioActualizado(usuarioActualizado);
                    }
                    Toast.makeText(context, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show();
                }
            });
            usuariosDialog.show(fragmentManager, "editarUsuario");
        });

        // Evento del botón eliminar
        holder.btnEliminarUsuario.setOnClickListener(v -> {
            mostrarDialogoEliminar(usuario, position);
        });

        // Evento para mostrar/ocultar contraseña
        holder.lblOlvidePassword.setOnClickListener(v -> {
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                boolean esVisible = (Boolean) holder.lblOlvidePassword.getTag();
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

    private void configurarImagenUsuario(ViewHolderGestionarUsuariosAdapter holder, User usuario) {
        switch (usuario.getRole()) {
            case User.ROLE_ADMIN:
                holder.imgUsuario.setImageResource(R.drawable.ic_admin);
                holder.imgUsuario.setColorFilter(Color.parseColor("#D32F2F")); // Rojo
                break;
            case User.ROLE_SELLER:
                holder.imgUsuario.setImageResource(R.drawable.ic_seller);
                holder.imgUsuario.setColorFilter(Color.parseColor("#1976D2")); // Azul
                break;
            case User.ROLE_PRODUCTION:
                holder.imgUsuario.setImageResource(R.drawable.ic_production);
                holder.imgUsuario.setColorFilter(Color.parseColor("#388E3C")); // Verde
                break;
            default:
                holder.imgUsuario.setImageResource(R.drawable.ic_usuario);
                holder.imgUsuario.setColorFilter(Color.parseColor("#666666")); // Gris
                break;
        }
    }

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

    private void mostrarDialogoEliminar(User usuario, int position) {
        // Verificar si es el último administrador
        if (User.ROLE_ADMIN.equals(usuario.getRole()) && esUltimoAdmin()) {
            new AlertDialog.Builder(context)
                    .setTitle("❌ No se puede eliminar")
                    .setMessage("No puedes eliminar el último administrador del sistema.\n\nDebe existir al menos un administrador activo.")
                    .setPositiveButton("Entendido", null)
                    .setIcon(R.drawable.ic_warning)
                    .show();
            return;
        }

        String mensajeExtra = User.ROLE_ADMIN.equals(usuario.getRole())
                ? "\n\n⚠️ Este es un usuario ADMINISTRADOR"
                : "";

        new AlertDialog.Builder(context)
                .setTitle("🗑️ ¿Eliminar Usuario?")
                .setMessage("¿Estás seguro de que deseas bloquear al usuario " + usuario.getName() + "?" + mensajeExtra + "\n\nEl usuario será bloqueado y no podrá acceder al sistema.")
                .setPositiveButton("Sí, Bloquear", (dialog, which) -> {
                    bloquearUsuario(usuario, position);
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(R.drawable.ic_warning)
                .show();
    }

    private boolean esUltimoAdmin() {
        int contadorAdmins = 0;
        for (User user : listaUsuarios) {
            if (User.ROLE_ADMIN.equals(user.getRole()) &&
                    !User.STATUS_BLOCKED.equals(user.getStatus())) {
                contadorAdmins++;
            }
        }
        return contadorAdmins <= 1;
    }

    private void bloquearUsuario(User usuario, int position) {
        Log.d(TAG, "Bloqueando usuario: " + usuario.getName());

        // Cambiar estado a bloqueado
        usuario.setStatus(User.STATUS_BLOCKED);

        databaseHelper.updateUser(usuario, new DatabaseHelper.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User usuarioActualizado) {
                Log.d(TAG, "Usuario bloqueado exitosamente");
                actualizarUsuario(usuarioActualizado);

                // Notificar al Fragment
                if (listener != null) {
                    listener.onUsuarioActualizado(usuarioActualizado);
                }

                Toast.makeText(context, "✅ Usuario " + usuarioActualizado.getName() + " ha sido bloqueado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error bloqueando usuario: " + error);
                Toast.makeText(context, "❌ Error: " + error, Toast.LENGTH_LONG).show();
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