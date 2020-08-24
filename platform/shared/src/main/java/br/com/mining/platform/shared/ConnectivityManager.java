package br.com.mining.platform.shared;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public interface ConnectivityManager {

    MqttStatus getStatus();

    boolean isMqttOnline();

    boolean isNetwork();

    Disposable addSubscriber(Consumer<Connectivity> subscriber);

}
