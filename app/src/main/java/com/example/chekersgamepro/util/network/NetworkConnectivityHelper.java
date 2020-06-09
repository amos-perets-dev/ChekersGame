package com.example.chekersgamepro.util.network;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;


public class NetworkConnectivityHelper implements Disposable {


    public interface NetworkConnectivityListener {

        void onStatusChange(boolean isConnected);
    }

    private final Application application;

    private final ConnectivityManager connectivityManager;

    private boolean isConnected;

    private final ListenersHolder<NetworkConnectivityListener> listeners = new ListenersHolder<>();

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(isConnected != getConnectivityStatus()) {
                isConnected =!isConnected;
                listeners.notifyListeners(listener -> listener.onStatusChange(isConnected));
            }
        }
    };

    private boolean isDisposed = false;


    public NetworkConnectivityHelper(Application application) {
        this.connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.isConnected = getConnectivityStatus();
        this.application = application;
        this.application.registerReceiver(broadcastReceiver,new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    public boolean getConnectivityStatus() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        if (activeNetwork != null) {
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                case ConnectivityManager.TYPE_MOBILE:
                    return true;
            }
        }
        return false;
    }


    @Override
    public void dispose() {
        try {
            this.application.unregisterReceiver(broadcastReceiver);
            isDisposed = true;
        }catch (Exception ex) {
            isDisposed = false;
        }
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }

    public Observable<Boolean> asObservable() {
        return Observable.create(emitter -> {
            emitter.onNext(isConnected);
            listeners.addListener(emitter::onNext);
            emitter.setDisposable(Disposables.fromAction(() -> listeners.removeListener(emitter::onNext)));
        });
    }


    public static class Builder {
        private Application application;
        private NetworkConnectivityListener listener;

        public Builder setApplication(Application application) {
            this.application = application;
            return this;
        }

        public Builder setListener(NetworkConnectivityListener listener) {
            this.listener = listener;
            return this;
        }

        public NetworkConnectivityHelper create() {
            NetworkConnectivityHelper networkConnectivityHelper = new NetworkConnectivityHelper(application);
            networkConnectivityHelper.listeners.addListener(listener);
            return new NetworkConnectivityHelper(application);
        }
    }
}