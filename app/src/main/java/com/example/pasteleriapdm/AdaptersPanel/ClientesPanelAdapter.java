package com.example.pasteleriapdm.AdaptersPanel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pasteleriapdm.R;

public class ClientesPanelAdapter extends RecyclerView.Adapter<ClientesPanelAdapter.ViewHolderClientesPanelAdapter> {
    private Context context;
    private FragmentManager fragmentManager;

    public ClientesPanelAdapter(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ClientesPanelAdapter.ViewHolderClientesPanelAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_panel_clientes, parent, false);
        return new ViewHolderClientesPanelAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientesPanelAdapter.ViewHolderClientesPanelAdapter holder, int position) {

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
            imege = itemView.findViewById(R.id.imagen);
            lblClientePanel = itemView.findViewById(R.id.lblCliente);
            lblCantidadClientesPanel = itemView.findViewById(R.id.lblCantidadClientesPanel);
        }
    }
}
