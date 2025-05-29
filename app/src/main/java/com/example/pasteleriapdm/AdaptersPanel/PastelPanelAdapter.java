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

public class PastelPanelAdapter extends RecyclerView.Adapter<PastelPanelAdapter.ViewHolderPastelPanelAdapter> {

    // Para identificar en logcat
    private static final String TAG = "PastelPanelAdapter";
    private Context context;
    private FragmentManager fragmentManager;
    private int totalPasteles = 0;

    public PastelPanelAdapter(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        loadPastelesCount();
    }

    // Cargar el total de pasteles desde la base de datos
    private void loadPastelesCount() {
        DatabaseHelper.getInstance().obtenerTotalPasteles(new DatabaseHelper.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer total) {
                totalPasteles = total;
                Log.d(TAG, "Total de pasteles cargado: " + total);
                // Notifica que los datos han cambiado para actualizar la UI
                notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error cargando total de pasteles: " + error);
                totalPasteles = 0;
                notifyDataSetChanged();
            }
        });
    }

    // Metodo publico para refrescar el contador de pasteles cuando se borre o agregue
    public void refreshPastelesCount() {
        loadPastelesCount();
    }

    @NonNull
    @Override
    public PastelPanelAdapter.ViewHolderPastelPanelAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_panel_pastel, parent, false);
        return new ViewHolderPastelPanelAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PastelPanelAdapter.ViewHolderPastelPanelAdapter holder, int position) {
        holder.imgPastel.setImageResource(R.drawable.img_pastel1);
        holder.txtNombrePastel.setText("Pasteles");
        holder.txtCantidadPastelesPanel.setText(String.valueOf(totalPasteles));

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "Click en panel de pasteles. Total: " + totalPasteles);
        });
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class ViewHolderPastelPanelAdapter extends RecyclerView.ViewHolder {
        ImageView imgPastel;
        TextView txtNombrePastel, txtCantidadPastelesPanel;

        public ViewHolderPastelPanelAdapter(@NonNull View itemView) {
            super(itemView);
            imgPastel = itemView.findViewById(R.id.imgPastel);
            txtNombrePastel = itemView.findViewById(R.id.txtNombrePastel);
            txtCantidadPastelesPanel = itemView.findViewById(R.id.txtCantidadPastelesPanel);
        }
    }
}