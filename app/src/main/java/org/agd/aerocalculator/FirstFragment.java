package org.agd.aerocalculator;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;

import org.agd.aerocalculator.databinding.FragmentFirstBinding;

import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private RxBleClient rxBleClient = null;
    private UUID hrCharacteristicUUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    String hrMonitorMacAddress = "E0:AA:2D:C0:5E:56";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rxBleClient == null) {
                    rxBleClient = RxBleClient.create(getContext());
                }
                RxBleDevice device = rxBleClient.getBleDevice(hrMonitorMacAddress);
                device.establishConnection(false)
                        .flatMap(rxBleConnection -> rxBleConnection.setupNotification(hrCharacteristicUUID))
                        .doOnNext(notificationObservable -> {
                            // Notification has been set up
                            Log.i("test_ble", "Notification set up");

                        })
                        .flatMap(notificationObservable -> notificationObservable) // <-- Notification has been set up, now observe value changes.
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                bytes -> {
                                    // Given characteristic has been changes, here is the value.
                                    byte in = bytes[1];
                                    int place1 = bytes[1] & 0xFF; //heart rate
                                    int place2 = bytes[2] & 0xFF;
                                    int place3 = bytes[3] & 0xFF;

                                    String result = String.format("%s-%s-%s", place1, place2, place3);
                                    Log.i("test_ble", "VALUE::" + result);
                                    binding.textviewFirst.setText(result);
                                },
                                throwable -> {
                                    // Handle an error here.
                                    throwable.printStackTrace();
                                }
                        );
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}