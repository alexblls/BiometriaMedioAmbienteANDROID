package com.example.biometriamedioambienteandroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

//Librerias para la fecha
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ------------------------------------------------------------------
// ------------------------------------------------------------------

public class MainActivity extends AppCompatActivity {

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private static final String ETIQUETA_LOG = ">>>>";

    // Estos son los códigos de petición de permisos (no significan gran cosa)
    private static final int CODIGO_PETICION_PERMISOS = 11223344;
    private static final int REQUEST_CODE_BLUETOOTH_SCAN = 1;
    private static final int REQUEST_CODE_BLUETOOTH_CONNECT = 2;

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private BluetoothLeScanner elEscanner;

    private ScanCallback callbackDelEscaneo = null;

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void buscarTodosLosDispositivosBTLE() {
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empieza ");

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): instalamos scan callback ");

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanResult() ");

                mostrarInformacionDispositivoBTLE(resultado);
                // Find the TextView by its id
                TextView myTextView = findViewById(R.id.scaneoStatus);

                // Set the text programmatically
                myTextView.setText("Escaneo conseguido!");
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onBatchScanResults() ");

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanFailed() ");
                // Find the TextView by its id
                TextView myTextView = findViewById(R.id.scaneoStatus);

                // Set the text programmatically
                myTextView.setText("Escaneo fallido");
            }
        };

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.d(ETIQUETA_LOG, "TENEMOS PERMISOS DE BLUETOOTH SCAN???: " + ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN));
            pedirPermisoBluetoothScan();
            return;
        }
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empezamos a escanear ");

        this.elEscanner.startScan(this.callbackDelEscaneo);

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------

    private void mostrarInformacionDispositivoBTLE(ScanResult resultado) {

        BluetoothDevice bluetoothDevice = resultado.getDevice();
        byte[] bytes;
        // una sencilla comprobacion para ver que bytes no está vacío...
        if (resultado.getScanRecord() == null) {
            // no hay bytes!!!
            Log.d(ETIQUETA_LOG, "mostrarInformacionDispositivoBTLE: Los bytes están vacios!!! ");
            return;
        } else {
            // lo que deberia ocurrir...
            bytes = resultado.getScanRecord().getBytes();
        }
        int rssi = resultado.getRssi();

        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " ****** DISPOSITIVO DETECTADO BTLE ****************** ");
        Log.d(ETIQUETA_LOG, " ****************************************************");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            pedirPermisoBluetoothConnect();
            return;
        }
        Log.d(ETIQUETA_LOG, " nombre = " + bluetoothDevice.getName());
        Log.d(ETIQUETA_LOG, " toString = " + bluetoothDevice.toString());

        /*
        ParcelUuid[] puuids = bluetoothDevice.getUuids();
        if ( puuids.length >= 1 ) {
            //Log.d(ETIQUETA_LOG, " uuid = " + puuids[0].getUuid());
           // Log.d(ETIQUETA_LOG, " uuid = " + puuids[0].toString());
        }*/

        Log.d(ETIQUETA_LOG, " dirección = " + bluetoothDevice.getAddress());
        Log.d(ETIQUETA_LOG, " rssi = " + rssi);

        Log.d(ETIQUETA_LOG, " bytes = " + new String(bytes));
        Log.d(ETIQUETA_LOG, " bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));

        TramaIBeacon tib = new TramaIBeacon(bytes);

        Log.d(ETIQUETA_LOG, " ----------------------------------------------------");
        Log.d(ETIQUETA_LOG, " prefijo  = " + Utilidades.bytesToHexString(tib.getPrefijo()));
        Log.d(ETIQUETA_LOG, "          advFlags = " + Utilidades.bytesToHexString(tib.getAdvFlags()));
        Log.d(ETIQUETA_LOG, "          advHeader = " + Utilidades.bytesToHexString(tib.getAdvHeader()));
        Log.d(ETIQUETA_LOG, "          companyID = " + Utilidades.bytesToHexString(tib.getCompanyID()));
        Log.d(ETIQUETA_LOG, "          iBeacon type = " + Integer.toHexString(tib.getiBeaconType()));
        Log.d(ETIQUETA_LOG, "          iBeacon length 0x = " + Integer.toHexString(tib.getiBeaconLength()) + " ( "
                + tib.getiBeaconLength() + " ) ");
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToHexString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " major  = " + Utilidades.bytesToHexString(tib.getMajor()) + "( "
                + Utilidades.bytesToInt(tib.getMajor()) + " ) ");
        Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToHexString(tib.getMinor()) + "( "
                + Utilidades.bytesToInt(tib.getMinor()) + " ) ");
        Log.d(ETIQUETA_LOG, " txPower  = " + Integer.toHexString(tib.getTxPower()) + " ( " + tib.getTxPower() + " )");
        Log.d(ETIQUETA_LOG, " ****************************************************");

        // AQUI SACAMOS EL TEXTO POR PANTALLA

        // Bytes de major
        TextView majorTextView = findViewById(R.id.datosBluetoothMajor);
        String majorString = "Major: " + Utilidades.bytesToInt(tib.getMajor());
        majorTextView.setText(majorString);

        // Bytes de minor
        TextView minorTextView = findViewById(R.id.datosBluetoothMinor);
        String minorString = "Minor: " + Utilidades.bytesToInt(tib.getMinor());
        minorTextView.setText(minorString);

        LocalDateTime fechaHoraActual = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fechaHoraFormateada = fechaHoraActual.format(formatter);

        String dataToSend = "{" +
                "\"fecha\": \""+fechaHoraFormateada+"\", " +
                "\"ppm\": "+Utilidades.bytesToInt(tib.getMajor())+", " +
                "\"latitud\": 123.456, " +
                "\"longitud\": 789.123" +
                "}";
        Log.d(ETIQUETA_LOG, " datos json "+dataToSend);
        ApiHandler.sendDataToDatabase(dataToSend);
        Log.d(ETIQUETA_LOG, " despues de api handler");

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void buscarEsteDispositivoBTLE(String dispositivoBuscado) {
        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empieza ");

        TextView dispositivoBuscadoTextView = findViewById(R.id.dispositivoBuscadoTextView);
        dispositivoBuscadoTextView.setText(dispositivoBuscado);
        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): instalamos scan callback ");

        this.callbackDelEscaneo = new ScanCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): onScanResult() ");
                TextView scanTextView = findViewById(R.id.scaneoStatus);
                BluetoothDevice bluetoothDevice = resultado.getDevice();
                Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE():  ¿dispositivoBuscado = " + dispositivoBuscado + " equivale a bluetoothDevice.getName() = " + bluetoothDevice.getName() + " ?");
                // Este mostrarInformacion es para hacer debugging
                //mostrarInformacionDispositivoBTLE(resultado);

                // AQUI ES CUANDO FILTRAMOS Y ENCONTRAMOS NUESTRO DISPOSITIVO
                if (Objects.equals(bluetoothDevice.getAddress(), dispositivoBuscado)) {
                    scanTextView.setText("Escaneo con éxito");
                    Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): dispositivo " + dispositivoBuscado + " encontrado");
                    mostrarInformacionDispositivoBTLE(resultado);

                } else {
                    Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): dispositivo no encontrado :( ");

                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): onBatchScanResults() ");
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): onScanFailed() ");
                // Find the TextView by its id
                TextView myTextView = findViewById(R.id.scaneoStatus);
                // Set the text programmatically
                myTextView.setText("Escaneo fallido");
            }
        };

        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + dispositivoBuscado);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            pedirPermisoBluetoothScan();
            return;
        }

        // Start the scan with the specified scan settings and filter
        this.elEscanner.startScan(this.callbackDelEscaneo);
        Log.d(ETIQUETA_LOG, "buscarEsteDispositivoBTLE: BUSCANDO CON EL FILTRO");
    }

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void detenerBusquedaDispositivosBTLE() {

        if (this.callbackDelEscaneo == null) {
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            pedirPermisoBluetoothScan();
            return;
        }
        this.elEscanner.stopScan(this.callbackDelEscaneo);
        this.callbackDelEscaneo = null;

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonBuscarDispositivosBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton buscar dispositivos BTLE Pulsado");
        this.buscarTodosLosDispositivosBTLE();
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonBuscarNuestroDispositivoBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado");
        //this.buscarEsteDispositivoBTLE( Utilidades.stringToUUID( "PROY3A-FONDO-SUR" ) );


        // En vez de buscar directamente el dispositivo, se podria filtrar lo que llega a la base de datos
        // es decir, recibir todos los datos de todos los dispositivos, y filtramos por nombre hasta que tengamos el nuestro
        // y cuando lo tengamos, enviamos los datos de nuestro dispositivo a la base de datos
        //this.buscarEsteDispositivoBTLE( "PROY3A-FONDO-SUR" );

        this.buscarEsteDispositivoBTLE("D2:5C:EB:10:7A:80");

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    public void botonDetenerBusquedaDispositivosBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton detener busqueda dispositivos BTLE Pulsado");
        this.detenerBusquedaDispositivosBTLE();
    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void inicializarBlueTooth() {
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos adaptador BT ");

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitamos adaptador BT ");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            pedirPermisoBluetoothConnect();
            return;
        }
        bta.enable();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitado =  " + bta.isEnabled());

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): estado =  " + bta.getState());

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos escaner btle ");

        this.elEscanner = bta.getBluetoothLeScanner();

        if (this.elEscanner == null) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): Socorro: NO hemos obtenido escaner btle  !!!!");

        }

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): voy a perdir permisos (si no los tuviera) !!!!");

        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_PETICION_PERMISOS);
        } else {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): parece que YA tengo los permisos necesarios !!!!");

        }
    } // ()


    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(ETIQUETA_LOG, " onCreate(): empieza ");

        inicializarBlueTooth();

        // No queremos de que busque bluetooth nada más abrirse la app, o al menos no de momento
        //buscarTodosLosDispositivosBTLE();
        Log.d(ETIQUETA_LOG, " onCreate(): termina ");



    } // onCreate()

    // --------------------------------------------------------------
    // --------------------------------------------------------------

    private void pedirPermisoBluetoothScan() {
        String[] permissions = new String[0];
        // Se supone que de 31 para abajo, no hay que pedir permisos para el bluetooth, si no, oopsies!
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions = new String[]{Manifest.permission.BLUETOOTH_SCAN};
            requestPermissions(permissions, REQUEST_CODE_BLUETOOTH_SCAN);
        } else {
            // muere en un incendio!
            Log.d(ETIQUETA_LOG, " im gonna kys bluetooth scan you!!! ");
        }
    }

    private void pedirPermisoBluetoothConnect() {
        String[] permissions = new String[0];
        // Se supone que de 31 para abajo, no hay que pedir permisos para el bluetooth, si no, oopsies!
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions = new String[]{Manifest.permission.BLUETOOTH_CONNECT};
            requestPermissions(permissions, REQUEST_CODE_BLUETOOTH_CONNECT);

        } else {
            // muere en un incendio!
            Log.d(ETIQUETA_LOG, " im gonna kys bluetooth connect you!!! ");
        }
    }

    private void alertaPermisosConcedidos() {
        AlertDialog.Builder alertaPermisos = new AlertDialog.Builder(MainActivity.this);
        alertaPermisos.setMessage("Permisos concedidos, porfavor, pulse el botón de nuevo")
                .setCancelable(false)
                .setNeutralButton("Entendido", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
    }

    private void alertaPermisosDenegados() {
        AlertDialog.Builder alertaPermisos = new AlertDialog.Builder(MainActivity.this);
        alertaPermisos.setMessage("Perminos denegados. La aplicación necesita permisos bluetooth para funcionar. Por favor, vaya a los Ajustes de su teléfono y conceda los permisos a la aplicación")
                .setCancelable(false)
                .setNeutralButton("Entendido", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Este switch determina que permisos se han pedido
        switch (requestCode) {
            case CODIGO_PETICION_PERMISOS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): permisos Jordi concedidos  !!!!");
                    alertaPermisosConcedidos();

                    // Permission is granted. Continue the action or workflow
                    // in your app.
                } else {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): Socorro: permisos Jordi NO concedidos  !!!!");
                    alertaPermisosDenegados();

                }
                break;
            case REQUEST_CODE_BLUETOOTH_SCAN:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): permisos Bluetooth Scan concedidos  !!!!");
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    alertaPermisosConcedidos();

                } else {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): Socorro: permisos Bluetooth Scan NO concedidos  !!!!");
                    alertaPermisosDenegados();
                }
                break;
            case REQUEST_CODE_BLUETOOTH_CONNECT:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): permisos Bluetooth Connect concedidos  !!!!");
                    alertaPermisosConcedidos();

                    // Permission is granted. Continue the action or workflow
                    // in your app.
                } else {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): Socorro: permisos Bluetooth Connect NO concedidos  !!!!");
                    alertaPermisosDenegados();

                }
                break;

        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    } // ()

} // class
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------


