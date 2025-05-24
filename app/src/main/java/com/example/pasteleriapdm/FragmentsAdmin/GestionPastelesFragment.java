package com.example.pasteleriapdm.FragmentsAdmin;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.pasteleriapdm.Adapters.GestionarPastelesAdapter;
import com.example.pasteleriapdm.Dialogs.PastelesDialog;
import com.example.pasteleriapdm.R;


public class GestionPastelesFragment extends Fragment {

    private Button btnAbrirDialogoPasteles;
    private RecyclerView rvcPasteles;

    public GestionPastelesFragment() {
        // Required empty public constructor
    }

    public static GestionPastelesFragment newInstance(String param1, String param2) {
        GestionPastelesFragment fragment = new GestionPastelesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gestion_pasteles, container, false);

        AsociarElementoXML(view);
        GestionarPastelesAdapter  gestionarPastelesAdapter= new GestionarPastelesAdapter(getContext(), getParentFragmentManager());
        rvcPasteles.setLayoutManager(new LinearLayoutManager(getContext()));
        rvcPasteles.setAdapter(gestionarPastelesAdapter);
        btnAbrirDialogoPasteles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PastelesDialog dialog = new PastelesDialog();
               // dialog.setProductoListener(this::actualizar); // this debe implementar ProductoListener
                dialog.show(getParentFragmentManager(), "pstelesDialogo");
            }
        });
        return view;
    }

    private  void AsociarElementoXML(View view){
        btnAbrirDialogoPasteles = view.findViewById(R.id.btnAbrirDialogoPasteles);
        rvcPasteles = view.findViewById(R.id.rvcPasteles);
    }


}