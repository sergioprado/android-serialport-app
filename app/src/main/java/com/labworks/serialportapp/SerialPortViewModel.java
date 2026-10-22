package com.labworks.serialportapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import vendor.labworks.serialportmanager.SerialPortManager;

public class SerialPortViewModel extends ViewModel {

    private final MutableLiveData<String> rxData = new MutableLiveData<>("");
    private final MutableLiveData<String> rxError = new MutableLiveData<>();
    private final StringBuilder rxBuffer = new StringBuilder();
    private volatile boolean running = true;

    public SerialPortViewModel() {
        // rx() blocks until a byte is received, so it runs in a dedicated thread
        Thread rxThread = new Thread(this::rxLoop, "SerialPortRx");
        rxThread.setDaemon(true);
        rxThread.start();
    }

    private void rxLoop() {
        try {
            SerialPortManager sp = SerialPortManager.getInstance();
            while (running) {
                byte b = sp.rx();
                synchronized (rxBuffer) {
                    rxBuffer.append((char) b);
                    rxData.postValue(rxBuffer.toString());
                }
            }
        } catch (Exception e) {
            rxError.postValue(e.getMessage());
        }
    }

    public void clearRxData() {
        synchronized (rxBuffer) {
            rxBuffer.setLength(0);
            rxData.postValue("");
        }
    }

    public LiveData<String> getRxData() {
        return rxData;
    }

    public LiveData<String> getRxError() {
        return rxError;
    }

    // the thread is blocked in rx(), so it only exits after the next byte
    @Override
    protected void onCleared() {
        running = false;
    }
}
