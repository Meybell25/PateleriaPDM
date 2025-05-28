package com.example.pasteleriapdm.FragmentsAdmin;

import android.os.Bundle;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import com.example.pasteleriapdm.R;

import java.util.ArrayList;
import java.util.List;


public class ReportesFragment extends Fragment {
    private View view;
    private PieChart graficoCircular;
    private BarChart graficoBarras;
    private TextView lblTotalReservas, lblVentasHoy, lblError;
    private Button btnExportarPdf, btnActualizar;
    private RecyclerView recyclerReportes;

    public ReportesFragment() {
        // Required empty public constructor
    }

    public static ReportesFragment newInstance(String param1, String param2) {
        ReportesFragment fragment = new ReportesFragment();
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
        view = inflater.inflate(R.layout.fragment_reportes, container, false);

        inicializarVistas();
        configurarGraficos();
        actualizarEstadisticas();
        configurarBotones();

        return view;
    }
    private void inicializarVistas() {
        graficoCircular = view.findViewById(R.id.graficoCircular);
        graficoBarras = view.findViewById(R.id.graficoBarras);
        lblTotalReservas = view.findViewById(R.id.lblTotalReservas);
        lblVentasHoy = view.findViewById(R.id.lblVentasHoy);
        lblError = view.findViewById(R.id.lblerror);
        btnExportarPdf = view.findViewById(R.id.btnExportarPdf);
        btnActualizar = view.findViewById(R.id.btnActualizarDatos);
        recyclerReportes = view.findViewById(R.id.rvcReportes);
    }
    private void configurarGraficos() {
        configurarGraficoCircular();
        configurarGraficoBarras();
    }
    private void configurarGraficoCircular() {
        try {
            List<PieEntry> entradas = new ArrayList<>();
            entradas.add(new PieEntry(45f, "Pendiente"));
            entradas.add(new PieEntry(25f, "En preparacion"));
            entradas.add(new PieEntry(8f, "Entregado"));
            entradas.add(new PieEntry(22f, "Cancelado"));

            PieDataSet conjuntoDatos = new PieDataSet(entradas, "Estados de Reservas");

            List<Integer> colores = new ArrayList<>();
            colores.add(Color.rgb(76, 175, 80));   // Verde
            colores.add(Color.rgb(255, 152, 0));   // Naranja
            colores.add(Color.rgb(244, 67, 54));   // Rojo
            colores.add(Color.rgb(156, 39, 176));  // Morado

            conjuntoDatos.setColors(colores);
            conjuntoDatos.setValueTextSize(13f);
            conjuntoDatos.setValueTextColor(Color.WHITE);
            conjuntoDatos.setSliceSpace(4f);
            conjuntoDatos.setSelectionShift(10f);

            PieData datos = new PieData(conjuntoDatos);
            datos.setValueFormatter(new PercentFormatter());

            graficoCircular.setData(datos);
            graficoCircular.setUsePercentValues(true);
            graficoCircular.getDescription().setEnabled(false);
            graficoCircular.setExtraOffsets(10, 10, 10, 10);
            graficoCircular.setDragDecelerationFrictionCoef(0.95f);
            graficoCircular.setDrawHoleEnabled(true);
            graficoCircular.setHoleColor(Color.WHITE);
            graficoCircular.setHoleRadius(45f);
            graficoCircular.setTransparentCircleRadius(50f);
            graficoCircular.setRotationAngle(0);
            graficoCircular.setRotationEnabled(true);
            graficoCircular.setHighlightPerTapEnabled(true);

            Legend leyenda = graficoCircular.getLegend();
            leyenda.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            leyenda.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            leyenda.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            leyenda.setDrawInside(false);
            leyenda.setXEntrySpace(7f);
            leyenda.setYEntrySpace(5f);
            leyenda.setTextSize(5f);

            graficoCircular.animateY(1200);
            graficoCircular.invalidate();

        } catch (Exception e) {
            mostrarError("Error al cargar gráfico circular: " + e.getMessage());
        }
    }

    private void configurarGraficoBarras() {
        try {
            List<BarEntry> entradas = new ArrayList<>();
            entradas.add(new BarEntry(0f, 65f));  // Chocolate
            entradas.add(new BarEntry(1f, 45f));  // Vainilla
            entradas.add(new BarEntry(2f, 30f));  // Fresa
            entradas.add(new BarEntry(3f, 25f));  // Red Velvet
            entradas.add(new BarEntry(4f, 40f));  // Tres Leches
            entradas.add(new BarEntry(5f, 35f));  // Cheesecake

            BarDataSet conjuntoDatos = new BarDataSet(entradas, "Unidades Vendidas");

            List<Integer> colores = new ArrayList<>();
            colores.add(Color.rgb(139, 69, 19));
            colores.add(Color.rgb(255, 228, 181));
            colores.add(Color.rgb(255, 182, 193));
            colores.add(Color.rgb(220, 20, 60));
            colores.add(Color.rgb(255, 250, 205));
            colores.add(Color.rgb(240, 230, 140));

            conjuntoDatos.setColors(colores);
            conjuntoDatos.setValueTextSize(12f);
            conjuntoDatos.setValueTextColor(Color.BLACK);

            BarData datos = new BarData(conjuntoDatos);
            datos.setBarWidth(0.7f);

            graficoBarras.setData(datos);
            graficoBarras.getDescription().setEnabled(false);
            graficoBarras.setFitBars(true);
            graficoBarras.setDrawGridBackground(false);

            XAxis ejeX = graficoBarras.getXAxis();
            ejeX.setPosition(XAxis.XAxisPosition.BOTTOM);
            ejeX.setDrawGridLines(false);
            ejeX.setGranularity(1f);
            ejeX.setLabelCount(6);
            ejeX.setTextSize(4f);
            String[] etiquetas = {"Chocolate", "Vainilla", "Fresa", "Cheesecake", "Especiales", "Cumpleaños", "Bodas"};
            ejeX.setValueFormatter(new IndexAxisValueFormatter(etiquetas));

            YAxis ejeYIzquierdo = graficoBarras.getAxisLeft();
            ejeYIzquierdo.setDrawGridLines(true);
            ejeYIzquierdo.setAxisMinimum(0f);
            ejeYIzquierdo.setTextSize(10f);

            YAxis ejeYDerecho = graficoBarras.getAxisRight();
            ejeYDerecho.setEnabled(false);

            Legend leyenda = graficoBarras.getLegend();
            leyenda.setEnabled(false);

            graficoBarras.animateY(1200);
            graficoBarras.invalidate();

        } catch (Exception e) {
            mostrarError("Error al cargar gráfico de barras: " + e.getMessage());
        }
    }
    private void actualizarEstadisticas() {
        try {
            int totalReservas = 45 + 25 + 8 + 22;
            int ventasHoy = 65 + 45 + 30 + 25 + 40 + 35;

            animarContador(lblTotalReservas, totalReservas);
            animarContador(lblVentasHoy, ventasHoy);

            lblError.setVisibility(View.GONE);

        } catch (Exception e) {
            mostrarError("Error al actualizar estadísticas: " + e.getMessage());
        }
    }
    private void animarContador(TextView texto, int valorFinal) {
        Thread hiloAnimacion = new Thread(() -> {
            for (int i = 0; i <= valorFinal; i += Math.max(1, valorFinal / 20)) {
                final int valorActual = Math.min(i, valorFinal);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> texto.setText(String.valueOf(valorActual)));
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> texto.setText(String.valueOf(valorFinal)));
            }
        });
        hiloAnimacion.start();
    }

    private void configurarBotones() {
        btnExportarPdf.setOnClickListener(v -> exportarPDF());
        btnActualizar.setOnClickListener(v -> actualizarDatos());
    }

    private void exportarPDF() {
        try {
            Toast.makeText(getContext(), "Generando reporte PDF...", Toast.LENGTH_SHORT).show();

            // Simular proceso de exportación
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Reporte generado exitosamente", Toast.LENGTH_SHORT).show()
                        );
                    }
                } catch (InterruptedException ignored) {}
            }).start();

        } catch (Exception e) {
            mostrarError("Error al exportar PDF: " + e.getMessage());
        }
    }

    private void actualizarDatos() {
        Toast.makeText(getContext(), "Actualizando datos...", Toast.LENGTH_SHORT).show();
        actualizarEstadisticas();
    }

    private void mostrarError(String mensaje) {
        lblError.setVisibility(View.VISIBLE);
        lblError.setText(mensaje);
    }


}