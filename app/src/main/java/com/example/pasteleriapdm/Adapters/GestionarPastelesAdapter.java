package com.example.pasteleriapdm.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pasteleriapdm.Models.Cake;
import com.example.pasteleriapdm.Models.User;
import com.example.pasteleriapdm.R;
import com.example.pasteleriapdm.Utils.DatabaseHelper;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GestionarPastelesAdapter extends RecyclerView.Adapter<GestionarPastelesAdapter.ViewHolderGestionarPastelesAdapter> {

    private static final String TAG = "GestionarPastelesAdapter";

    private Context context;
    private FragmentManager fragmentManager;
    private List<Cake> listasPasteles;
    private List<Cake> listasPastelesFiltrada;
    private OnPastelActionListener actionListener;

    // Interface para manejar acciones de editar y eliminar
    public interface OnPastelActionListener {
        void onEditarPastel(Cake cake);
        void onEliminarPastel(Cake cake);
    }

    public GestionarPastelesAdapter(Context context, FragmentManager fragmentManager,
                                    List<Cake> listasPasteles, OnPastelActionListener actionListener) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.listasPasteles = listasPasteles != null ? listasPasteles : new ArrayList<>();
        this.listasPastelesFiltrada = new ArrayList<>(this.listasPasteles);
        this.actionListener = actionListener;

        // Debug: Log para verificar la inicialización
        Log.d(TAG, "Adapter inicializado con " + this.listasPasteles.size() + " pasteles");
        for (Cake cake : this.listasPasteles) {
            Log.d(TAG, "Pastel: " + cake.getName());
        }
    }

    // Constructor alternativo para compatibilidad
    public GestionarPastelesAdapter(Context context, FragmentManager fragmentManager,
                                    List<Cake> listasPasteles,
                                    OnEditarPastelListener editListener,
                                    OnEliminarPastelListener deleteListener) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.listasPasteles = listasPasteles != null ? listasPasteles : new ArrayList<>();
        this.listasPastelesFiltrada = new ArrayList<>(this.listasPasteles);
        this.actionListener = new OnPastelActionListener() {
            @Override
            public void onEditarPastel(Cake cake) {
                if (editListener != null) {
                    editListener.onEditarPastel(cake);
                }
            }

            @Override
            public void onEliminarPastel(Cake cake) {
                if (deleteListener != null) {
                    deleteListener.onEliminarPastel(cake);
                }
            }
        };

        Log.d(TAG, "Adapter inicializado (constructor alternativo) con " + this.listasPasteles.size() + " pasteles");
    }

    // Interfaces separadas para compatibilidad
    public interface OnEditarPastelListener {
        void onEditarPastel(Cake cake);
    }

    public interface OnEliminarPastelListener {
        void onEliminarPastel(Cake cake);
    }

    @NonNull
    @Override
    public ViewHolderGestionarPastelesAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pasteles, parent, false);
        return new ViewHolderGestionarPastelesAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderGestionarPastelesAdapter holder, int position) {
        // Verificar si la posición es válida
        if (position < 0 || position >= listasPastelesFiltrada.size()) {
            Log.e(TAG, "Posición inválida: " + position + ", tamaño: " + listasPastelesFiltrada.size());
            return;
        }

        Cake cake = listasPastelesFiltrada.get(position);
        if (cake == null) {
            Log.e(TAG, "Cake es null en posición: " + position);
            return;
        }

        Log.d(TAG, "Mostrando pastel: " + cake.getName() + " en posición: " + position);

        // Configurar datos del pastel con validaciones null
        holder.lblNombrePastel.setText(cake.getName() != null ? cake.getName() : "Sin nombre");
        holder.lblDescripcionPastel.setText(cake.getDescription() != null ? cake.getDescription() : "Sin descripción");
        holder.lblCategoriaPastel.setText(convertirCategoriaParaDisplay(cake.getCategory()));
        holder.lblTanoPaste.setText(convertirTamanoParaDisplay(cake.getSize()));
        holder.lblPorcionesPastel.setText("Porciones: " + cake.getServings());

        // Configurar estado del pastel
        configurarEstadoPastel(holder, cake);

        // Configurar fechas
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        if (cake.getCreatedAt() > 0) {
            String fechaCreacion = dateFormat.format(new Date(cake.getCreatedAt()));
            holder.lblCreatedAt.setText("Creado: " + fechaCreacion);
        } else {
            holder.lblCreatedAt.setText("Creado: --");
        }

        if (cake.getUpdatedAt() > 0) {
            String fechaActualizacion = dateFormat.format(new Date(cake.getUpdatedAt()));
            holder.lblUpdatedAt.setText("Actualizado: " + fechaActualizacion);
        } else {
            holder.lblUpdatedAt.setText("Actualizado: --");
        }

        // Configurar creado por
        if (cake.getCreatedBy() != null && !cake.getCreatedBy().isEmpty()) {
            // Si createdBy contiene un ID (más de 20 caracteres), buscar el nombre
            if (cake.getCreatedBy().length() > 20) {
                // Es un ID, buscar el nombre del usuario
                holder.lblCreatedBy.setText("Creado por: Cargando...");
                DatabaseHelper.getInstance().getUserById(cake.getCreatedBy(), new DatabaseHelper.DatabaseCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (user != null && user.getName() != null) {
                            holder.lblCreatedBy.setText("Creado por: " + user.getName());
                        } else {
                            holder.lblCreatedBy.setText("Creado por: Usuario desconocido");
                        }
                    }

                    @Override
                    public void onError(String error) {
                        holder.lblCreatedBy.setText("Creado por: Usuario desconocido");
                    }
                });
            } else {
                // Ya es un nombre, mostrarlo directamente
                holder.lblCreatedBy.setText("Creado por: " + cake.getCreatedBy());
            }
        } else {
            holder.lblCreatedBy.setText("Creado por: --");
        }

        // Formatear precio
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        holder.lblPrecioPastel.setText(formatter.format(cake.getPrice()));

        // Configurar imagen
        configurarImagenPastel(holder.imgImagenPastel, cake);

        // Configurar eventos de botones con validación de estado
        configurarBotones(holder, cake);
    }

    // ========== MÉTODOS DE BÚSQUEDA MEJORADOS ==========

    /**
     * Filtrar pasteles por nombre - VERSION MEJORADA
     */
    public void filtrarPorNombre(String query) {
        Log.d(TAG, "Filtrando por: '" + query + "'");
        Log.d(TAG, "Lista original tiene: " + listasPasteles.size() + " pasteles");

        listasPastelesFiltrada.clear();

        if (query == null || query.trim().isEmpty()) {
            // Si no hay búsqueda, mostrar todos los pasteles
            listasPastelesFiltrada.addAll(listasPasteles);
            Log.d(TAG, "Filtro vacío - mostrando todos los pasteles: " + listasPastelesFiltrada.size());
        } else {
            // Filtrar por nombre (case insensitive)
            String queryLowerCase = query.toLowerCase().trim();
            Log.d(TAG, "Buscando: '" + queryLowerCase + "'");

            for (Cake cake : listasPasteles) {
                if (cake != null && cake.getName() != null) {
                    String nombreCake = cake.getName().toLowerCase();
                    Log.d(TAG, "Comparando '" + nombreCake + "' con '" + queryLowerCase + "'");

                    if (nombreCake.contains(queryLowerCase)) {
                        listasPastelesFiltrada.add(cake);
                        Log.d(TAG, "Encontrado: " + cake.getName());
                    }
                }
            }
        }

        Log.d(TAG, "Resultados filtrados: " + listasPastelesFiltrada.size());
        notifyDataSetChanged();
    }

    /**
     * Limpiar filtro de búsqueda
     */
    public void limpiarFiltro() {
        Log.d(TAG, "Limpiando filtro");
        listasPastelesFiltrada.clear();
        listasPastelesFiltrada.addAll(listasPasteles);
        Log.d(TAG, "Después de limpiar filtro: " + listasPastelesFiltrada.size() + " pasteles");
        notifyDataSetChanged();
    }

    /**
     * Obtener cantidad de resultados filtrados
     */
    public int getCantidadResultados() {
        return listasPastelesFiltrada.size();
    }

    /**
     * Verificar si hay resultados
     */
    public boolean hayResultados() {
        return !listasPastelesFiltrada.isEmpty();
    }

    @Override
    public int getItemCount() {
        int count = listasPastelesFiltrada != null ? listasPastelesFiltrada.size() : 0;
        Log.d(TAG, "getItemCount() retorna: " + count);
        return count;
    }

    // ========== MÉTODOS DE ACTUALIZACIÓN MEJORADOS ==========

    /**
     * Método para actualizar la lista - VERSION MEJORADA
     */
    public void actualizarLista(List<Cake> nuevaLista) {
        Log.d(TAG, "Actualizando lista. Nueva lista tiene: " + (nuevaLista != null ? nuevaLista.size() : 0) + " pasteles");

        this.listasPasteles = nuevaLista != null ? nuevaLista : new ArrayList<>();
        this.listasPastelesFiltrada = new ArrayList<>(this.listasPasteles);

        Log.d(TAG, "Lista actualizada. Total pasteles: " + this.listasPasteles.size());
        for (Cake cake : this.listasPasteles) {
            if (cake != null) {
                Log.d(TAG, "Pastel en lista: " + cake.getName());
            }
        }

        notifyDataSetChanged();
    }

    /**
     * Método para agregar un pastel
     */
    public void agregarPastel(Cake cake) {
        if (cake != null) {
            Log.d(TAG, "Agregando pastel: " + cake.getName());
            listasPasteles.add(cake);
            listasPastelesFiltrada.add(cake);
            notifyItemInserted(listasPastelesFiltrada.size() - 1);
        }
    }

    /**
     * Método para actualizar un pastel específico
     */
    public void actualizarPastel(Cake cake) {
        if (cake == null || cake.getId() == null) {
            Log.e(TAG, "No se puede actualizar pastel null o sin ID");
            return;
        }

        Log.d(TAG, "Actualizando pastel: " + cake.getName());

        // Actualizar en la lista original
        for (int i = 0; i < listasPasteles.size(); i++) {
            if (listasPasteles.get(i).getId().equals(cake.getId())) {
                listasPasteles.set(i, cake);
                break;
            }
        }

        // Actualizar en la lista filtrada
        for (int i = 0; i < listasPastelesFiltrada.size(); i++) {
            if (listasPastelesFiltrada.get(i).getId().equals(cake.getId())) {
                listasPastelesFiltrada.set(i, cake);
                notifyItemChanged(i);
                break;
            }
        }
    }

    /**
     * Método para eliminar un pastel
     */
    public void eliminarPastel(String cakeId) {
        if (cakeId == null) {
            Log.e(TAG, "No se puede eliminar pastel con ID null");
            return;
        }

        Log.d(TAG, "Eliminando pastel con ID: " + cakeId);

        // Eliminar de la lista original
        for (int i = 0; i < listasPasteles.size(); i++) {
            if (listasPasteles.get(i).getId().equals(cakeId)) {
                listasPasteles.remove(i);
                break;
            }
        }

        // Eliminar de la lista filtrada
        for (int i = 0; i < listasPastelesFiltrada.size(); i++) {
            if (listasPastelesFiltrada.get(i).getId().equals(cakeId)) {
                listasPastelesFiltrada.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    private void configurarEstadoPastel(ViewHolderGestionarPastelesAdapter holder, Cake cake) {
        String status = cake.getStatus();
        if (status == null) {
            status = Cake.STATUS_ACTIVE;
        }

        switch (status) {
            case Cake.STATUS_ACTIVE:
                holder.lblEstado.setText("ACTIVO");
                holder.lblEstado.setTextColor(ContextCompat.getColor(context, R.color.verde_activo));
                holder.lblEstado.setBackground(ContextCompat.getDrawable(context, R.drawable.background_estado_activo));
                break;
            case Cake.STATUS_INACTIVE:
                holder.lblEstado.setText("INACTIVO");
                holder.lblEstado.setTextColor(ContextCompat.getColor(context, R.color.rojo_inactivo));
                holder.lblEstado.setBackground(ContextCompat.getDrawable(context, R.drawable.background_estado_inactivo));
                break;
            default:
                holder.lblEstado.setText("ACTIVO");
                holder.lblEstado.setTextColor(ContextCompat.getColor(context, R.color.verde_activo));
                holder.lblEstado.setBackground(ContextCompat.getDrawable(context, R.drawable.background_estado_activo));
                break;
        }
    }

    private void configurarBotones(ViewHolderGestionarPastelesAdapter holder, Cake cake) {
        // El botón de editar siempre está disponible
        holder.btnEditarPastel.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEditarPastel(cake);
            }
        });

        // El botón de eliminar solo está disponible para pasteles inactivos
        String status = cake.getStatus();
        boolean esInactivo = Cake.STATUS_INACTIVE.equals(status);

        if (esInactivo) {
            // Pastel inactivo - permitir eliminación
            holder.btnEliminarPastel.setEnabled(true);
            holder.btnEliminarPastel.setAlpha(1.0f);
            holder.btnEliminarPastel.setOnClickListener(v -> mostrarDialogoEliminar(cake));
        } else {
            // Pastel activo - no permitir eliminación
            holder.btnEliminarPastel.setEnabled(false);
            holder.btnEliminarPastel.setAlpha(0.5f);
            holder.btnEliminarPastel.setOnClickListener(v ->
                    mostrarMensajeNoSePuedeEliminar(cake));
        }
    }

    private void mostrarMensajeNoSePuedeEliminar(Cake cake) {
        new AlertDialog.Builder(context)
                .setTitle("No se puede eliminar")
                .setMessage("El pastel '" + cake.getName() + "' está activo.\n\n" +
                        "Para eliminarlo, primero debe cambiar su estado a 'Inactivo' " +
                        "editando el pastel.")
                .setPositiveButton("Entendido", null)
                .setNeutralButton("Editar ahora", (dialog, which) -> {
                    if (actionListener != null) {
                        actionListener.onEditarPastel(cake);
                    }
                })
                .show();
    }

    private void configurarImagenPastel(ImageView imageView, Cake cake) {
        // Si hay imageUrl y es una URL válida
        if (cake.getImageUrl() != null && !cake.getImageUrl().isEmpty()) {
            // Verificar si es una URL válida
            if (cake.getImageUrl().startsWith("http://") || cake.getImageUrl().startsWith("https://")) {
                // Cargar imagen desde URL usando Glide
                Glide.with(context)
                        .load(cake.getImageUrl())
                        .placeholder(obtenerImagenPorCategoria(cake.getCategory()))
                        .error(obtenerImagenPorCategoria(cake.getCategory()))
                        .into(imageView);
                return;
            } else {
                Toast.makeText(context.getApplicationContext(), "tipo de imageninvalida tiene que ser http:// o https://", Toast.LENGTH_SHORT).show();
                // Si no es URL, intentar como resource ID (compatibilidad hacia atrás)
                try {
                    int resourceId = Integer.parseInt(cake.getImageUrl());
                    imageView.setImageResource(resourceId);
                    return;
                } catch (NumberFormatException e) {
                    // Si no es resource ID válido, usar imagen por categoría
                }
            }
        }

        // Usar imagen por defecto basada en categoría
        int imageResource = obtenerImagenPorCategoria(cake.getCategory());
        imageView.setImageResource(imageResource);
    }

    private int obtenerImagenPorCategoria(String categoria) {
        if (categoria == null) {
            return R.drawable.decoracion_pastel;
        }

        switch (categoria) {
            case Cake.CATEGORY_CHOCOLATE:
                return R.drawable.ic_pastel_chocolate;
            case Cake.CATEGORY_VANILLA:
                return R.drawable.ic_pastel_vainilla;
            case Cake.CATEGORY_FRUIT:
                return R.drawable.ic_pastel_frutas;
            case Cake.CATEGORY_CHEESECAKE:
                return R.drawable.ic_cheesecake;
            case Cake.CATEGORY_BIRTHDAY:
                return R.drawable.ic_pastel_cumpleanos;
            case Cake.CATEGORY_WEDDING:
                return R.drawable.ic_pastel_boda;
            case Cake.CATEGORY_SPECIAL:
            default:
                return R.drawable.decoracion_pastel;
        }
    }

    private void mostrarDialogoEliminar(Cake cake) {
        String mensaje = "¿Está seguro de que desea eliminar PERMANENTEMENTE el pastel '" +
                cake.getName() + "'?\n\nEsta acción no se puede deshacer.";

        new AlertDialog.Builder(context)
                .setTitle("⚠️ Eliminar Permanentemente")
                .setMessage(mensaje)
                .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                    if (actionListener != null) {
                        actionListener.onEliminarPastel(cake);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String convertirCategoriaParaDisplay(String dbCategory) {
        if (dbCategory == null) {
            return "Especial";
        }

        switch (dbCategory) {
            case Cake.CATEGORY_CHOCOLATE: return "Chocolate";
            case Cake.CATEGORY_VANILLA: return "Vainilla";
            case Cake.CATEGORY_FRUIT: return "Frutas";
            case Cake.CATEGORY_CHEESECAKE: return "Cheesecake";
            case Cake.CATEGORY_SPECIAL: return "Especial";
            case Cake.CATEGORY_BIRTHDAY: return "Cumpleaños";
            case Cake.CATEGORY_WEDDING: return "Bodas";
            default: return "Especial";
        }
    }

    private String convertirTamanoParaDisplay(String dbSize) {
        if (dbSize == null) {
            return "Mediano";
        }

        switch (dbSize) {
            case Cake.SIZE_SMALL: return "Pequeño";
            case Cake.SIZE_MEDIUM: return "Mediano";
            case Cake.SIZE_LARGE: return "Grande";
            case Cake.SIZE_XLARGE: return "Extra Grande";
            default: return "Mediano";
        }
    }

    public class ViewHolderGestionarPastelesAdapter extends RecyclerView.ViewHolder {
        TextView lblNombrePastel, lblCategoriaPastel, lblPorcionesPastel,
                lblPrecioPastel, lblTanoPaste, lblDescripcionPastel,
                lblCreatedAt, lblUpdatedAt, lblCreatedBy, lblEstado;
        ImageButton btnEditarPastel, btnEliminarPastel;
        ImageView imgImagenPastel;

        public ViewHolderGestionarPastelesAdapter(@NonNull View itemView) {
            super(itemView);
            imgImagenPastel = itemView.findViewById(R.id.imgImagenPastel);
            lblNombrePastel = itemView.findViewById(R.id.lblNombrePastel);
            lblCategoriaPastel = itemView.findViewById(R.id.lblCategoriaPastel);
            lblPorcionesPastel = itemView.findViewById(R.id.lblPorcionesPastel);
            lblPrecioPastel = itemView.findViewById(R.id.lblPrecioPastel);
            lblTanoPaste = itemView.findViewById(R.id.lblTanoPaste);
            lblDescripcionPastel = itemView.findViewById(R.id.lblDescripcionPastel);
            lblCreatedAt = itemView.findViewById(R.id.lblCreatedAt);
            lblUpdatedAt = itemView.findViewById(R.id.lblupdatedAt);
            lblCreatedBy = itemView.findViewById(R.id.lblcreatedBy);
            lblEstado = itemView.findViewById(R.id.lblEstado);
            btnEditarPastel = itemView.findViewById(R.id.btnEditarPastel);
            btnEliminarPastel = itemView.findViewById(R.id.btnEliminarPastel);
        }
    }
}