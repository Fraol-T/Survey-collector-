package com.example.surveycollector.util;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class NetworkErrorManager {
    private static final MutableLiveData<String> errorEvent = new MutableLiveData<>();

    public static LiveData<String> getErrorEvent() {
        return errorEvent;
    }

    public static void postError(String message) {
        errorEvent.postValue(message);
    }
}
