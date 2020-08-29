package com.fouadbahari.lellafood.Services;

import androidx.annotation.NonNull;

import com.fouadbahari.lellafood.Common.Common;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyFCMServices extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String,String> dataRecv=remoteMessage.getData();
        if (dataRecv!=null)
        {
            Common.ShowNotification(this,new Random().nextInt(),
                    dataRecv.get(Common.NOTIF_TITLE),
                    dataRecv.get(Common.NOTIF_CONTENT),
                    null);
        }

    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Common.updateToken(this,s);

    }
}
