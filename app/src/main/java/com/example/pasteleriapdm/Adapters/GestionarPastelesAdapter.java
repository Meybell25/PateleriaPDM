package com.example.pasteleriapdm.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pasteleriapdm.Dialogs.PastelesDialog;
import com.example.pasteleriapdm.R;
import com.google.android.material.button.MaterialButton;

public class GestionarPastelesAdapter extends RecyclerView.Adapter<GestionarPastelesAdapter.ViewHolderGestionarPastelesAdapter> {
   private Context context;
   private FragmentManager fragmentManager;

    public GestionarPastelesAdapter(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public GestionarPastelesAdapter.ViewHolderGestionarPastelesAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pasteles, parent, false);
        return new ViewHolderGestionarPastelesAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GestionarPastelesAdapter.ViewHolderGestionarPastelesAdapter holder, int position) {
        holder.imgImagenPastel.setImageResource(R.drawable.decoracion_pastel);
        holder.lblNombrePastel.setText("Pastel de cholate");
        holder.lblDescripcionPastel.setText("Pastel con sabor a chocolate y frezas");
        holder.lblTanoPaste.setText("Mediano");
        holder.lblPorcionesPastel.setText("Porciones: 23");
        holder.lblPrecioPastel.setText("$ 20");

        holder.imgImagenPastel.setImageResource(R.drawable.decoracion_pastel);
        holder.lblNombrePastel.setText("Pastel de cholate");
        holder.lblDescripcionPastel.setText("Pastel con sabor a chocolate y frezas");
        holder.lblTanoPaste.setText("Mediano");
        holder.lblPorcionesPastel.setText("Porciones: 23");
        holder.lblPrecioPastel.setText("$ 20");

        //EVENTO DEL BOTON EDITAR
        holder.btnEditarPastel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PastelesDialog pastelesDialog = new PastelesDialog();
                pastelesDialog.show(fragmentManager, "editar");
            }
        });

        //EVENTO DEL BOTON ELIMINAR (PERO SOLO EL DIALOGO)
        holder.btnEliminarPastel.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("¿Eliminar Pastel?")
                    .setMessage("¿Estás seguro de que deseas eliminar este pastel ")
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

    public class ViewHolderGestionarPastelesAdapter extends RecyclerView.ViewHolder {
        TextView lblNombrePastel, lblCategoriaPastel, lblPorcionesPastel, lblPrecioPastel, lblTanoPaste, lblDescripcionPastel;
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
            btnEditarPastel = itemView.findViewById(R.id.btnEditarPastel);
            btnEliminarPastel = itemView.findViewById(R.id.btnEliminarPastel);
        }
    }

}
