package com.example.pasteleriapdm.FragmentsAdmin;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pasteleriapdm.AdaptersPanel.ClientesPanelAdapter;
import com.example.pasteleriapdm.AdaptersPanel.PastelPanelAdapter;
import com.example.pasteleriapdm.AdaptersPanel.ReservaPanelAdapter;
import com.example.pasteleriapdm.AdaptersPanel.UsuarioPaneleAdapter;
import com.example.pasteleriapdm.R;


public class PanelAdministrativoFragment extends Fragment {
    private RecyclerView rvcItemPastenesPanel;
    private RecyclerView rvcItemClientesPanel;
    private RecyclerView rvcItemUsauriosPanel;
    private RecyclerView rvcItemReservaPanel;



    public PanelAdministrativoFragment() {
        // Required empty public constructor
    }


    public static PanelAdministrativoFragment newInstance(String param1, String param2) {
        PanelAdministrativoFragment fragment = new PanelAdministrativoFragment();
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
        View view = inflater.inflate(R.layout.fragment_panel_administrativo, container, false);
        AsociarElementoXML(view);

        //PARA EL RVCPASTELES
        PastelPanelAdapter pastelPanelAdapter= new PastelPanelAdapter(getContext(), getParentFragmentManager());
        rvcItemPastenesPanel.setLayoutManager(new LinearLayoutManager(getContext()));
        rvcItemPastenesPanel.setAdapter(pastelPanelAdapter);

        //RVC PARA USUARIOS EN EL PANEL
        UsuarioPaneleAdapter usuarioPaneleAdapter= new UsuarioPaneleAdapter(getContext(), getParentFragmentManager());
        rvcItemUsauriosPanel.setLayoutManager(new LinearLayoutManager(getContext()));
        rvcItemUsauriosPanel.setAdapter(usuarioPaneleAdapter);


        //RVC PARA CLIENTE EN EL PANEL
        ClientesPanelAdapter clientesPanelAdapter = new ClientesPanelAdapter(getContext(), getParentFragmentManager());
        rvcItemClientesPanel.setLayoutManager(new LinearLayoutManager(getContext()));
        rvcItemClientesPanel.setAdapter(clientesPanelAdapter);

        //RVC PARA CLIENTE EN EL PANEL
        ReservaPanelAdapter reservaPanelAdapter = new ReservaPanelAdapter(getContext(), getParentFragmentManager());
        rvcItemReservaPanel.setLayoutManager(new LinearLayoutManager(getContext()));
        rvcItemReservaPanel.setAdapter(reservaPanelAdapter);

        return view;
    }

    private void AsociarElementoXML(View view){
        // Asociación de vistas
        rvcItemClientesPanel = view.findViewById(R.id.rvcItemClientesPanel);
        rvcItemPastenesPanel = view.findViewById(R.id.rvcItemPastenesPanel);
        rvcItemUsauriosPanel = view.findViewById(R.id.rvcItemUsauriosPanel);
        rvcItemReservaPanel = view.findViewById(R.id.rvcItemReservaPanel);
    }

}