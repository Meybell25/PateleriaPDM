package com.example.pasteleriapdm;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pasteleriapdm.FragmentsAdmin.ReservaFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MunuProductionActivity extends AppCompatActivity {

    private BottomNavigationView bottomnavigationProduction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_munu_production);

        // orden con los padig
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        bottomnavigationProduction = findViewById(R.id.bottomnavigationProduction);

        // Fragmento por defecto
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmnetContainer, new ReservaFragment())
                    .commit();
            bottomnavigationProduction.setSelectedItemId(R.id.navPanelAdministrativo);
        }

        // Navegación inferior
        bottomnavigationProduction.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navReservas) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmnetContainer, new ReservaFragment())
                        .commit();
                return true;
            } else if (id == R.id.navSalir) {
                mostrarDialogoSalirApp();
                return true;
            } else if (id == R.id.navCerrarSesion) {
                mostrarDialogoCerrarSesion();
                return true;
            }
            return false;
        });
    }

    private void mostrarDialogoCerrarSesion() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesion")
                .setMessage("¿Está seguro que desea cerrar sesion?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Si, cerrar sesion", (dialog, which) -> cerrarSesion())
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    private void cerrarSesion() {
        try {
            Log.d(TAG, "Iniciando proceso de cierre de sesion");
            Toast.makeText(this, "Cerrando sesion...", Toast.LENGTH_SHORT).show();
            LoginActivity.cerrarSesionYRedireccionar(this);
        } catch (Exception e) {
            Log.e(TAG, "Error cerrando sesion", e);
            Toast.makeText(this, "Error cerrando sesion: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarDialogoSalirApp() {
        new AlertDialog.Builder(this)
                .setTitle("Salir de la Aplicacion")
                .setMessage("¿Desea salir de la aplicacion? Su sesion permanecera activa.")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Salir", (dialog, which) -> {
                    moveTaskToBack(true);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }
}
