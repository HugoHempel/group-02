package com.example.app

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.media.MediaPlayer
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_connect.*
import org.jetbrains.anko.toast
import java.io.IOException
import java.util.*
import android.app.Activity


private const val TAG = "Group 2 - Debug:"

class ConnectActivity : AppCompatActivity() {

    // Creates a companion object with values
    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        var m_bluetoothAdapter: BluetoothAdapter? = null
        var m_isConnected: Boolean = false
        var m_address: String? = null
    }

    var automaticDriving: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)
        // Set m_address to the car's MAC-address
        m_address = "FC:F5:C4:0F:87:62"
        // Run the ConnectToDevice method
        ConnectToDevice(this).execute()
      
        if(m_isConnected){
            toast("Connected to car")
        } else {
            toast("Not connected to car")
        }

        buttonForward.setOnClickListener { sendMessage("f")
            var drivingForward = MediaPlayer.create(this, R.raw.driving_forward)
            drivingForward!!.start()}
        buttonBackward.setOnClickListener { sendMessage("b")
            var drivingBackwards = MediaPlayer.create(this, R.raw.driving_backwards)
            drivingBackwards!!.start()
        }
        buttonLeft.setOnClickListener { sendMessage("l")
            var turningLeft = MediaPlayer.create(this, R.raw.turning_left)
            turningLeft!!.start()
        }
        buttonRight.setOnClickListener { sendMessage("r")
            var turningRight = MediaPlayer.create(this, R.raw.turning_right)
            turningRight!!.start()
        }

        buttonStop.setOnClickListener { sendMessage("§")                                        }
        buttonExit.setOnClickListener { disconnect() }

        toggleDriveMode.setOnClickListener{

            if (toggleDriveMode.isChecked) {
                    sendMessage("a")
                    automaticDriving = true
                    toast("Automatic driving is active.")
            } else {
                    sendMessage("m")
                    automaticDriving = false
                    toast("Manual driving is active.")
                }
            }


        while(m_isConnected){
            var input = readMessage()


        }
    }

    private fun sendMessage(message: String) {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.outputStream.write(message.toByteArray())
            } catch (e: IOException) {
                Log.e(TAG, "Error writing message")
            }
        }
    }

    private fun readMessage(): Char {
        var input: Char = 'q'
        if (m_bluetoothSocket != null) {
            try {
                input = m_bluetoothSocket!!.inputStream.read().toChar()
            } catch (e: IOException) {
                Log.e(TAG, "Error reading message")
            }
        }
        return input;
    }

    private fun disconnect() {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.close()
                m_bluetoothSocket = null
                m_isConnected = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        finish()
    }

    //Class in charge of connecting the device with the car
    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>(){

        private var connectSuccess: Boolean = true
        private val context: Context = c

        override fun onPreExecute() {
            super.onPreExecute()
        }
        //Connect device to car
        override fun doInBackground(vararg params: Void?): String? {
            try {
                if (m_bluetoothSocket == null || !m_isConnected){
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

                    // Creates a object representing the Bluetooth device with matching MAC Address
                    val device: BluetoothDevice = m_bluetoothAdapter!!.getRemoteDevice(m_address)

                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    // Stop looking for other devices to save battery
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    // Connect to the found Bluetooth socket
                    m_bluetoothSocket!!.connect()
                }
            } catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }
      
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(!connectSuccess){
                Log.i(TAG, "could not connect")
            } else {
                m_isConnected = true
            }
        }
    }
}