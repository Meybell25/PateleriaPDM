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

public class ReservaPanelAdapter extends RecyclerView.Adapter<ReservaPanelAdapter.ViewHolderReservaPanelAdapter> {

    // Para identificar en logcat
    private static final String TAG = "ReservaPanelAdapter";
    private Context context;
    private FragmentManager fragmentManager;
    private int totalReservas = 0;

    public ReservaPanelAdapter(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        loadReservasCount();
    }

    // Cargar el total de reservas desde la base de datos
    private void loadReservasCount() {
        DatabaseHelper.getInstance().obtenerTotalReservas(new DatabaseHelper.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer total) {
                totalReservas = total;
                Log.d(TAG, "Total de reservas cargado: " + total);
                // Notifica que los datos han cambiado para actualizar la UI
                notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error cargando total de reservas: " + error);
                totalReservas = 0;
                notifyDataSetChanged();
            }
        });
    }

    // Metodo pablico para refrescar el contador de reservas cuando se borre o agregue
    public void refreshReservasCount() {
        loadReservasCount();
    }

    @NonNull
    @Override
    public ReservaPanelAdapter.ViewHolderReservaPanelAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_panel_reservas, parent, false);
        return new ViewHolderReservaPanelAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaPanelAdapter.ViewHolderReservaPanelAdapter holder, int position) {
        holder.image.setImageResource(R.drawable.img_reserva164);
        holder.lblReserva.setText("Reservas");
        holder.lblCantidaReseservasPanel.setText(String.valueOf(totalReservas));

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "Click en panel de reservas. Total: " + totalReservas);
        });
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class ViewHolderReservaPanelAdapter extends RecyclerView.ViewHolder {
        ImageView image;
        TextView lblReserva, lblCantidaReseservasPanel;

        public ViewHolderReservaPanelAdapter(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            lblReserva = itemView.findViewById(R.id.lblReserva);
            lblCantidaReseservasPanel = itemView.findViewById(R.id.lblCantidaReseservasPanel);
        }
    }
}