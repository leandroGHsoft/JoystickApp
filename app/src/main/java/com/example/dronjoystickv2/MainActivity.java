package com.example.dronjoystickv2;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.rorschach.library.ShaderSeekArc;

public class MainActivity extends AppCompatActivity {
    /***********************************************************************************************
     * Declaración de objetos
     **********************************************************************************************/
    // Declaro los joysticks
    JoystickClass js2;
    // Declaro los layouts
    LinearLayout jder_layout;
    // Declaro el Log multiline
    LinearLayout linearLayout_Log;
    // Declaro el scrollview
    ScrollView scrollview;
    // Declaro el botón
    Button btnConnect;
    // Declaro los textview para los bytes
    TextView tv_byte2_b7, tv_byte2_b6, tv_byte2_b5, tv_byte2_b4, tv_byte2_b3, tv_byte2_b2, tv_byte2_b1, tv_byte2_b0;
    // Declaro los objetos para el bluetooth
    BluetoothAdapter bluetoothAdapter;
    // Declaro el Dialog bluetooth
    Dialog dialogBluetooth;
    // Throtle textview
    TextView throttle_value;
    // Switch bluetooth
    Switch switch_bluetooth;
    /***********************************************************************************************
     * Declaración de variables
     **********************************************************************************************/
    // Variable para marcar la última dirección de los sticks
    public static int LAST_EVENT_J1, LAST_EVENT_J2;
    // Declaro los bytes que serán enviados
    //public static byte Byte3 = 0, Byte2 = 0, Byte1 = 0, Byte0 = 0;
    // Stream de datos enviados
    public static byte[] msgBuffer = {42, 42, 42, 35, 0, 0, 47, 43, 43, 43};
    public static int throttle = 0, joystick = 0;
    // UUID para el bluetooth
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // Socket para el bluetooth
    private BluetoothSocket btSocket = null;
    // Output stream para el bluetooth
    private OutputStream outStream = null;
    // Flag para conexin establecida con módulo bluetooth
    private int flag_conect = 0;
    // Dirección del sispositivo bluetooth conectado
    private String DeviceAddress;
    /**********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Orientación de pantalla landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /*******************************************************************************************
         * Registro de los objetos
         ******************************************************************************************/
        // Registro los joysticks
        jder_layout = (LinearLayout) findViewById(R.id.jder_layout);
        // Registro el linearLayout del scrollview
        linearLayout_Log = (LinearLayout) findViewById(R.id.scrollView_layout);
        // Registro el scrollview
        scrollview = (ScrollView) findViewById(R.id.scrollView);
        // Registro el botón para realizar la conexión
        btnConnect = (Button) findViewById(R.id.btn_connect);
        // Registro los textview para mostrar las posiciones de los joysticks
        tv_byte2_b7 = (TextView) findViewById(R.id.tv_byte2_b7);
        tv_byte2_b6 = (TextView) findViewById(R.id.tv_byte2_b6);
        tv_byte2_b5 = (TextView) findViewById(R.id.tv_byte2_b5);
        tv_byte2_b4 = (TextView) findViewById(R.id.tv_byte2_b4);
        tv_byte2_b3 = (TextView) findViewById(R.id.tv_byte2_b3);
        tv_byte2_b2 = (TextView) findViewById(R.id.tv_byte2_b2);
        tv_byte2_b1 = (TextView) findViewById(R.id.tv_byte2_b1);
        tv_byte2_b0 = (TextView) findViewById(R.id.tv_byte2_b0);
        // Registro los elementos para la conexión bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Throttle value
        throttle_value = (TextView) findViewById(R.id.throttle_value);
        // Switch bluetooth
        switch_bluetooth = (Switch) findViewById(R.id.switchBT);
        /******************************************************************************************/
        js2 = new JoystickClass(getApplicationContext(), jder_layout, R.drawable.joystick_lever);
        js2.setStickSize(200, 200);
        js2.setLayoutAlpha(250);
        js2.setStickAlpha(250);
        js2.setOffset(120);
        js2.setMinimumDistance(20);
        jder_layout.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js2.drawStick(arg1);
                if (arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    int direction = js2.get8Direction();
                    joystick_der_functions(direction);
                } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    msgBuffer[5] = 0;
                    tv_byte2_b0.setText("0");
                    tv_byte2_b1.setText("0");
                    tv_byte2_b2.setText("0");
                    tv_byte2_b3.setText("0");
                    tv_byte2_b4.setText("0");
                    tv_byte2_b5.setText("0");
                    tv_byte2_b6.setText("0");
                    tv_byte2_b7.setText("0");
                    LAST_EVENT_J2 = JoystickClass.STICK_NONE;
                    if (flag_conect == 1)
                        sendData(msgBuffer, DeviceAddress);

                }
                return true;
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothConnection();
            }
        });

        switch_bluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Boolean btflagswitch = switch_bluetooth.isChecked();
                if (btflagswitch == true) {
                    checkOnOffBluetooth();
                }
                else {
                    //btSocket.close();
                    bluetoothAdapter.disable();
                }
            }
        });
        /******************************************************************************************/
        ShaderSeekArc seekArc = (ShaderSeekArc) findViewById(R.id.seek_arc);
        int[] colors = new int[]{0xFF2C3EFF, 0xFF53FF65, 0xFF000000};
        seekArc.setColors(colors);
        float[] positions = new float[]{0, 1f / 2, 1};
        seekArc.setPositions(positions);
        seekArc.setStartColor(0xFF1636FF);
        seekArc.setEndColor(0xFFFF2D0C);
        seekArc.setStartValue(0);
        seekArc.setEndValue(100);
        seekArc.setProgress(0);
        seekArc.setStartAngle(-225);
        seekArc.setEndAngle(45);
        seekArc.setOnSeekArcChangeListener(new ShaderSeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(ShaderSeekArc seekArc, float progress) {
                SetThrottle(progress);
            }
            @Override
            public void onStartTrackingTouch(ShaderSeekArc seekArc) {
                //Log.d(TAG, "onStartTrackingTouch");
            }
            @Override
            public void onStopTrackingTouch(ShaderSeekArc seekArc) {
                //Log.d(TAG, "onStopTrackingTouch");
            }
        });

    } // Fin de onCreate()


    /***********************************************************************************************
     * Funciones para los joysticks
     **********************************************************************************************/
    public void joystick_der_functions(int direction){
        if (direction == JoystickClass.STICK_UP) {
            if (LAST_EVENT_J2 != JoystickClass.STICK_UP) {
                msgBuffer[5] = 1;
                tv_byte2_b0.setText("1");
                tv_byte2_b1.setText("0");
                tv_byte2_b2.setText("0");
                tv_byte2_b3.setText("0");
                tv_byte2_b4.setText("0");
                tv_byte2_b5.setText("0");
                tv_byte2_b6.setText("0");
                tv_byte2_b7.setText("0");
                if (flag_conect == 1)
                    sendData(msgBuffer, DeviceAddress);
            }
            LAST_EVENT_J2 = JoystickClass.STICK_UP;
        } else if (direction == JoystickClass.STICK_UPRIGHT) {
            if (LAST_EVENT_J2 != JoystickClass.STICK_UPRIGHT) {
                msgBuffer[5] = 2;
                tv_byte2_b0.setText("0");
                tv_byte2_b1.setText("1");
                tv_byte2_b2.setText("0");
                tv_byte2_b3.setText("0");
                tv_byte2_b4.setText("0");
                tv_byte2_b5.setText("0");
                tv_byte2_b6.setText("0");
                tv_byte2_b7.setText("0");
                if (flag_conect == 1)
                    sendData(msgBuffer, DeviceAddress);
            }
            LAST_EVENT_J2 = JoystickClass.STICK_UPRIGHT;
        } else if (direction == JoystickClass.STICK_RIGHT) {
            if (LAST_EVENT_J2 != JoystickClass.STICK_RIGHT) {
                msgBuffer[5] = 4;
                tv_byte2_b0.setText("0");
                tv_byte2_b1.setText("0");
                tv_byte2_b2.setText("1");
                tv_byte2_b3.setText("0");
                tv_byte2_b4.setText("0");
                tv_byte2_b5.setText("0");
                tv_byte2_b6.setText("0");
                tv_byte2_b7.setText("0");
                if (flag_conect == 1)
                    sendData(msgBuffer, DeviceAddress);
            }
            LAST_EVENT_J2 = JoystickClass.STICK_RIGHT;
        } else if (direction == JoystickClass.STICK_DOWNRIGHT){
            if (LAST_EVENT_J2 != JoystickClass.STICK_DOWNRIGHT) {
                msgBuffer[5] = 8;
                tv_byte2_b0.setText("0");
                tv_byte2_b1.setText("0");
                tv_byte2_b2.setText("0");
                tv_byte2_b3.setText("1");
                tv_byte2_b4.setText("0");
                tv_byte2_b5.setText("0");
                tv_byte2_b6.setText("0");
                tv_byte2_b7.setText("0");
                if (flag_conect == 1)
                    sendData(msgBuffer, DeviceAddress);
            }
            LAST_EVENT_J2 = JoystickClass.STICK_DOWNRIGHT;
        } else if (direction == JoystickClass.STICK_DOWN){
            if (LAST_EVENT_J2 != JoystickClass.STICK_DOWN) {
                msgBuffer[5] = 16;
                tv_byte2_b0.setText("0");
                tv_byte2_b1.setText("0");
                tv_byte2_b2.setText("0");
                tv_byte2_b3.setText("0");
                tv_byte2_b4.setText("1");
                tv_byte2_b5.setText("0");
                tv_byte2_b6.setText("0");
                tv_byte2_b7.setText("0");
                if (flag_conect == 1)
                    sendData(msgBuffer, DeviceAddress);
            }
            LAST_EVENT_J2 = JoystickClass.STICK_DOWN;
        } else if (direction == JoystickClass.STICK_DOWNLEFT){
            if (LAST_EVENT_J2 != JoystickClass.STICK_DOWNLEFT) {
                msgBuffer[5] = 32;
                tv_byte2_b0.setText("0");
                tv_byte2_b1.setText("0");
                tv_byte2_b2.setText("0");
                tv_byte2_b3.setText("0");
                tv_byte2_b4.setText("0");
                tv_byte2_b5.setText("1");
                tv_byte2_b6.setText("0");
                tv_byte2_b7.setText("0");
                if (flag_conect == 1)
                    sendData(msgBuffer, DeviceAddress);
            }
            LAST_EVENT_J2 = JoystickClass.STICK_DOWNLEFT;
        } else if (direction == JoystickClass.STICK_LEFT){
            if (LAST_EVENT_J2 != JoystickClass.STICK_LEFT) {
                msgBuffer[5] = 64;
                tv_byte2_b0.setText("0");
                tv_byte2_b1.setText("0");
                tv_byte2_b2.setText("0");
                tv_byte2_b3.setText("0");
                tv_byte2_b4.setText("0");
                tv_byte2_b5.setText("0");
                tv_byte2_b6.setText("1");
                tv_byte2_b7.setText("0");
                if (flag_conect == 1)
                    sendData(msgBuffer, DeviceAddress);
            }
            LAST_EVENT_J2 = JoystickClass.STICK_LEFT;
        } else if (direction == JoystickClass.STICK_UPLEFT){
            if (LAST_EVENT_J2 != JoystickClass.STICK_UPLEFT) {
                msgBuffer[5] = -128;
                tv_byte2_b0.setText("0");
                tv_byte2_b1.setText("0");
                tv_byte2_b2.setText("0");
                tv_byte2_b3.setText("0");
                tv_byte2_b4.setText("0");
                tv_byte2_b5.setText("0");
                tv_byte2_b6.setText("0");
                tv_byte2_b7.setText("1");
                if (flag_conect == 1)
                    sendData(msgBuffer, DeviceAddress);
            }
            LAST_EVENT_J2 = JoystickClass.STICK_UPLEFT;
        } else if (direction == JoystickClass.STICK_NONE){
            if (LAST_EVENT_J2 != JoystickClass.STICK_NONE) {
                msgBuffer[5] = 0;
                tv_byte2_b0.setText("0");
                tv_byte2_b1.setText("0");
                tv_byte2_b2.setText("0");
                tv_byte2_b3.setText("0");
                tv_byte2_b4.setText("0");
                tv_byte2_b5.setText("0");
                tv_byte2_b6.setText("0");
                tv_byte2_b7.setText("0");
                if (flag_conect == 1)
                    sendData(msgBuffer, DeviceAddress);
            }
            LAST_EVENT_J2 = JoystickClass.STICK_NONE;
        }
    } // Fin de función joystick_der_functions

    /***********************************************************************************************
     * Función para el throttle
     **********************************************************************************************/
    public void SetThrottle(float progress){
        byte throttle = (byte)Math.round(progress);
        throttle_value.setText(Integer.toString(throttle));
        msgBuffer[4] = throttle;
        if (flag_conect == 1)
            sendData(msgBuffer, DeviceAddress);
    }

    /***********************************************************************************************
     * Función para escribir en el Log Textview
     **********************************************************************************************/
    public void textLog(String txt, String colorflag){
        TextView textView = new TextView(this);
        // Obtengo la hora
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm:ss");
        String strTime = mdformat.format(calendar.getTime());
        // Muestro el resultado
        textView.setText(strTime + " --> " + txt);
        switch(colorflag){
            case "alert":
                // Red text
                textView.setTextColor(getResources().getColor(R.color.colorRed));
                break;
            case "info":
                // White text
                textView.setTextColor(getResources().getColor(R.color.colorWhite));
                break;
            case "ok":
                // Green text
                textView.setTextColor(getResources().getColor(R.color.colorGreen));
                break;
        }
        linearLayout_Log.addView(textView);
        scrollview.post(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    /***********************************************************************************************
     * Funciones para el bluetooth
     **********************************************************************************************/
    // Función para crear el Dialog y mostrar los dispositivos Bluetooth apareados
    public void customDialogBluetooth(){
        // custom dialog
        dialogBluetooth = new Dialog(this);

        View view = getLayoutInflater().inflate(R.layout.bluetooth_popup, null);

        ListView MyList = (ListView) view.findViewById(R.id.listView);

        // Change MyActivity.this and myListOfItems to your own values
        int index = 0;
        Set<BluetoothDevice> bluetoothSet = bluetoothAdapter.getBondedDevices();
        final String[] name = new String[bluetoothSet.size()];
        final String[] address = new String[bluetoothSet.size()];
        Integer[] imgid = new Integer[bluetoothSet.size()];

        if (bluetoothSet.size() > 0) {
            for (BluetoothDevice device : bluetoothSet) {
                name[index] = device.getName();
                address[index] = device.getAddress();
                imgid[index] = R.drawable.bluetooth;
                index++;
            }
        }

        MyListAdapter adapter = new MyListAdapter(this, name, address, imgid);
        MyList.setAdapter(adapter);
        MyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    DeviceAddress = address[0];
                    startBluetoothConnection(address[0]);
                } else if(position == 1) {
                    DeviceAddress = address[0];
                    startBluetoothConnection(address[1]);
                } else if(position == 2) {
                    DeviceAddress = address[0];
                    startBluetoothConnection(address[2]);
                } else if(position == 3) {
                    DeviceAddress = address[0];
                    startBluetoothConnection(address[3]);
                } else if(position == 4) {
                    DeviceAddress = address[0];
                    startBluetoothConnection(address[4]);
                }
            }
        });

        dialogBluetooth.setContentView(view);
        dialogBluetooth.setTitle("Dispositivos Bluetooth vinculados");

        dialogBluetooth.show();
    }

    // Función para realizar la conexión Bluetooth
    public void BluetoothConnection(){
        // Mostrar dispositivos apareados en un Dialog
        customDialogBluetooth();
    }

    // Chequear si el bluetooth está encendido, si no lo está pedir permisos y encenderlo
    public void checkOnOffBluetooth(){
        if (bluetoothAdapter == null){
            Toast.makeText(getApplicationContext(), "Bluetooth no está soportado en este dispositivo", Toast.LENGTH_SHORT).show();
        } else {
            if(!bluetoothAdapter.isEnabled()){
                // Pido permisos al usuario para encender el bluetooth
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(i,1);
                textLog("Bluetooth encendido","ok");
            }
        }
    }

    private void startBluetoothConnection(String address) {
        if (bluetoothAdapter.isEnabled()) {
            // Hago un puntero al nodo remoto usando su dirección
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

            /** Para realizar una conexión hacen falta dos cosas:
             * _Una dirección MAC
             * _ Un identificador UUID. En este caso utilizo el UUID para SPP**/

            try {
                btSocket = createBluetoothSocket(device);
            } catch (IOException e1) {
                errorExit("Fatal Error", "Error al crear el socket: " + e1.getMessage() + ".");
            }

            // Discovery is resource intensive.  Make sure it isn't going on
            // when you attempt to connect and pass your message.
            bluetoothAdapter.cancelDiscovery();

            // Establish the connection.  This will block until it connects.
            try {
                btSocket.connect();
                Toast.makeText(getApplicationContext(), "El socket está conectado", Toast.LENGTH_SHORT).show();
                Log.e("Evento: ","El socket está conectado");
            } catch (IOException e) {
                try {
                    btSocket.close();
                    Toast.makeText(getApplicationContext(), "El socket está desconectado", Toast.LENGTH_SHORT).show();
                    Log.e("Evento: ","El socket está desconectado!!!!!!");
                } catch (IOException e2) {
                    errorExit("Fatal Error", "No se puede cerrar el socket en el fallo de conexión" + e2.getMessage() + ".");
                }
            }

            // Create a data stream so we can talk to server.
            try {
                outStream = btSocket.getOutputStream();
            } catch (IOException e) {
                errorExit("Fatal Error", "Fallo en crear el OutputStream:" + e.getMessage() + ".");
            }
            flag_conect = 1;
        } else {
            flag_conect = 0;
            Toast.makeText(getApplicationContext(), "Active el Bluetooth primero", Toast.LENGTH_LONG).show();
        }
        textLog("Conexión bluetooth establecida con " + address,"ok");
        dialogBluetooth.cancel();
    }

    // Función para crear un Socket para la comunicación Bluetooth
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "El socket no pudo crearse", Toast.LENGTH_SHORT).show();
            }
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    private void sendData(byte[] message, String address) {
        //byte[] sendBuff = message.getBytes();
        try {
            //outStream.write(message);
            outStream.write(message[3]);
            outStream.write(message[4]);
            outStream.write(message[5]);
            outStream.write(message[6]);
            //outStream.write(12);
        } catch (IOException e) {
            String msg = "An exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
            msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            errorExit("Fatal Error", msg);
        }
    }

    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

/**************************************************************************************************/
} // Fin de la clase principal de la aplicación
