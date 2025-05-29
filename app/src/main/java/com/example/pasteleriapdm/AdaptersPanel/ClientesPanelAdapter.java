
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

public class ClientesPanelAdapter extends RecyclerView.Adapter<ClientesPanelAdapter.ViewHolderClientesPanelAdapter> {

    //para identificar en locat
    private static final String TAG = "ClientesPanelAdapter";
    private Context context;
    private FragmentManager fragmentManager;
    private int totalClientes = 0;
    public ClientesPanelAdapter(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        loadClientesCount();
    }

     //Cargar el total de clientes desde la base de datos
    private void loadClientesCount() {
        DatabaseHelper.getInstance().obtenerTotalClientes(new DatabaseHelper.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer total) {
                totalClientes = total;
                Log.d(TAG, "Total de clientes cargado: " + total);
                // Notifica q los datos han cambiado para actualizar la UI
                notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error cargando total de clientes: " + error);
                totalClientes = 0;
                notifyDataSetChanged();
            }
        });
    }

    //Metodo publico para refrescar el contador de clientes cunaod se borre o agrge
    public void refreshClientesCount() {
        loadClientesCount();
    }

    @NonNull
    @Override
    public ClientesPanelAdapter.ViewHolderClientesPanelAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_panel_clientes, parent, false);
        return new ViewHolderClientesPanelAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientesPanelAdapter.ViewHolderClientesPanelAdapter holder, int position) {
        holder.imege.setImageResource(R.drawable.img_clientes); // Asegúrate de tener este drawable
        holder.lblClientePanel.setText("Clientes");
        holder.lblCantidadClientesPanel.setText(String.valueOf(totalClientes));
        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "Click en panel de clientes. Total: " + totalClientes);
        });
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class ViewHolderClientesPanelAdapter extends RecyclerView.ViewHolder {
        ImageView imege;
        TextView lblCantidadClientesPanel, lblClientePanel;
        public ViewHolderClientesPanelAdapter(@NonNull View itemView) {
            super(itemView);
            imege = itemView.findViewById(R.id.imageClientes);
            lblClientePanel = itemView.findViewById(R.id.lblClientePanel);
            lblCantidadClientesPanel = itemView.findViewById(R.id.lblCantidadClientesPanel);
        }
    }
}