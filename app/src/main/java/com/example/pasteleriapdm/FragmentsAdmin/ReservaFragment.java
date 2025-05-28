package com.example.pasteleriapdm.FragmentsAdmin;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import com.example.pasteleriapdm.Adapters.GestionarClientesAdapter;
import com.example.pasteleriapdm.Adapters.ReservaAdapter;
import com.example.pasteleriapdm.Dialogs.ClientesDialog;
import com.example.pasteleriapdm.Dialogs.ReservaDialogo;
import com.example.pasteleriapdm.R;


public class ReservaFragment extends Fragment {

    private Button btnAbrirDialogoReserva;
    private RecyclerView rvcReserva;
    private Spinner spinnerFiltroRservaEstado;

    public ReservaFragment() {
        // Required empty public constructor
    }

    public static ReservaFragment newInstance(String param1, String param2) {
        ReservaFragment fragment = new ReservaFragment();
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
        View view = inflater.inflate(R.layout.fragment_reserva, container, false);
        AsociarElementoXML(view);

        ReservaAdapter reservaAdapter= new ReservaAdapter(getContext(), getParentFragmentManager());

        rvcReserva.setLayoutManager(new LinearLayoutManager(getContext()));
        rvcReserva.setAdapter(reservaAdapter);
        btnAbrirDialogoReserva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReservaDialogo dialog = new ReservaDialogo();
                // dialog.setProductoListener(this::actualizar); // this debe implementar ProductoListener
                dialog.show(getParentFragmentManager(), "reservaDialogo");
            }
        });
        return view;
    }
    private  void AsociarElementoXML(View view){
        btnAbrirDialogoReserva = view.findViewById(R.id.btnAbrirDialogoReservas);
        rvcReserva = view.findViewById(R.id.rvcReservas);
        spinnerFiltroRservaEstado = view.findViewById(R.id.spinnerFiltroRservaEstado);
    }
}