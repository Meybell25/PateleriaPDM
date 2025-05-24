package com.example.pasteleriapdm;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {


    private ProgressBar progressBar;
    private TextView tvLoading;
    private Handler handler;
    private int progressStatus = 0;

    // Textos de carga que van cambiando
    private String[] loadingTexts = {
            "Preparando deliciosos sabores...",
            "Horneando los mejores pasteles...",
            "Decorando con amor...",
            "Agregando el toque final...",
            "¡Listo para endulzar tu día!"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        progressBar = findViewById(R.id.progress_bar);
        tvLoading = findViewById(R.id.tv_loading);
        handler = new Handler(Looper.getMainLooper());

        // Iniciar animación del splash
        startSplashAnimation();
    }

    private void startSplashAnimation() {
        // Animación suave del ProgressBar
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        progressAnimator.setDuration(3000); // 3 segundos
        progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        progressAnimator.start();

        // Cambiar textos durante la carga
        changeLoadingTexts();

        // Navegar a la siguiente pantalla después de 3.5 segundos
        handler.postDelayed(() -> {
            navigateToNextScreen();
        }, 3500);
    }

    private void changeLoadingTexts() {
        // Cambiar texto cada 700ms
        for (int i = 0; i < loadingTexts.length; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                tvLoading.setText(loadingTexts[index]);

                // Pequeña animación de fade para el texto
                tvLoading.setAlpha(0.5f);
                tvLoading.animate()
                        .alpha(1.0f)
                        .setDuration(300)
                        .start();

            }, i * 700);
        }
    }

    private void navigateToNextScreen() {
        // Aquí puedes decidir a qué pantalla ir
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);

        // Animación de transición suave
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        // Finalizar esta actividad
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar handlers para evitar memory leaks
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}