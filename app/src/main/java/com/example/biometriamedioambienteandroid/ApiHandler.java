package com.example.biometriamedioambienteandroid;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

public class ApiHandler {
    private static final String ETIQUETA_LOG = ">>>>";

    public static void sendDataToDatabase(String dataToSend) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // URL de tu API
                    Log.d(ETIQUETA_LOG, " Dentro de Apihandler");
                    String apiUrl = "http://192.168.1.133:3000/datos";

                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    // Configura la conexión para una solicitud POST
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    // Escribe los datos que deseas enviar
                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    outputStream.writeBytes(dataToSend);
                    outputStream.flush();
                    outputStream.close();

                    // Obtiene la respuesta de la API (código de estado)
                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.d(ETIQUETA_LOG, " Envio correcto");
                    } else {
                        Log.d(ETIQUETA_LOG, " Envio fallido");
                    }

                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

