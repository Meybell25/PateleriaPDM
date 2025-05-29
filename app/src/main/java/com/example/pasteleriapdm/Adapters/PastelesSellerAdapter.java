package com.example.pasteleriapdm.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pasteleriapdm.Models.Cake;
import com.example.pasteleriapdm.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PastelesSellerAdapter extends RecyclerView.Adapter<PastelesSellerAdapter.ViewHolderPastelesSellerAdapter> {

    private static final String TAG = "PastelesSellerAdapter";

    private Context context;
    private List<Cake> listasPasteles;
    private List<Cake> listasPastelesFiltrada;

    public PastelesSellerAdapter(Context context, List<Cake> listasPasteles) {
        this.context = context;
        this.listasPasteles = listasPasteles != null ? listasPasteles : new ArrayList<>();
        this.listasPastelesFiltrada = new ArrayList<>(this.listasPasteles);

        // Debug: Log para verificar la inicialización
        Log.d(TAG, "Adapter Seller inicializado con " + this.listasPasteles.size() + " pasteles activos");
        for (Cake cake : this.listasPasteles) {
            Log.d(TAG, "Pastel activo: " + cake.getName());
        }
    }

    @NonNull
    @Override
    public ViewHolderPastelesSellerAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pasteles_seller, parent, false);
        return new ViewHolderPastelesSellerAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderPastelesSellerAdapter holder, int position) {
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

        // Configurar datos básicos del pastel
        holder.lblNombrePastel.setText(cake.getName() != null ? cake.getName() : "Sin nombre");
        holder.lblDescripcionPastel.setText(cake.getDescription() != null ? cake.getDescription() : "Sin descripción");
        holder.lblCategoriaPastel.setText(convertirCategoriaParaDisplay(cake.getCategory()));
        holder.lblTanoPaste.setText(convertirTamanoParaDisplay(cake.getSize()));
        holder.lblPorcionesPastel.setText("Porciones: " + cake.getServings());

        // Configurar estado del pastel (siempre será ACTIVO para sellers)
        holder.lblEstado.setText("ACTIVO");
        holder.lblEstado.setTextColor(context.getResources().getColor(R.color.verde_activo));

        // Formatear precio
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        holder.lblPrecioPastel.setText(formatter.format(cake.getPrice()));

        // Configurar imagen
        configurarImagenPastel(holder.imgImagenPastel, cake);

    }

    // ========== METODOS DE BÚSQUEDA ==========

    /**
     * Filtrar pasteles por nombre
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
            // Filtrar por nombre
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

    // ========== MÉTODOS DE ACTUALIZACIÓN ==========

    /**
     * Método para actualizar la lista de pasteles activos
     */
    public void actualizarLista(List<Cake> nuevaLista) {
        Log.d(TAG, "Actualizando lista. Nueva lista tiene: " + (nuevaLista != null ? nuevaLista.size() : 0) + " pasteles");

        this.listasPasteles = nuevaLista != null ? nuevaLista : new ArrayList<>();
        this.listasPastelesFiltrada = new ArrayList<>(this.listasPasteles);

        Log.d(TAG, "Lista actualizada. Total pasteles activos: " + this.listasPasteles.size());
        for (Cake cake : this.listasPasteles) {
            if (cake != null) {
                Log.d(TAG, "Pastel activo en lista: " + cake.getName());
            }
        }

        notifyDataSetChanged();
    }

    // ========== MÉTODOS AUXILIARES ==========

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

    public class ViewHolderPastelesSellerAdapter extends RecyclerView.ViewHolder {
        TextView lblNombrePastel, lblCategoriaPastel, lblPorcionesPastel,
                lblPrecioPastel, lblTanoPaste, lblDescripcionPastel, lblEstado;
        ImageView imgImagenPastel;

        public ViewHolderPastelesSellerAdapter(@NonNull View itemView) {
            super(itemView);
            imgImagenPastel = itemView.findViewById(R.id.imgImagenPastel);
            lblNombrePastel = itemView.findViewById(R.id.lblNombrePastel);
            lblCategoriaPastel = itemView.findViewById(R.id.lblCategoriaPastel);
            lblPorcionesPastel = itemView.findViewById(R.id.lblPorcionesPastel);
            lblPrecioPastel = itemView.findViewById(R.id.lblPrecioPastel);
            lblTanoPaste = itemView.findViewById(R.id.lblTanoPaste);
            lblDescripcionPastel = itemView.findViewById(R.id.lblDescripcionPastel);
            lblEstado = itemView.findViewById(R.id.lblEstado);
        }
    }
}