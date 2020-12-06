package com.fouadbahari.lellafood.Remote;

import com.fouadbahari.lellafood.Model.FCMResponse;
import com.fouadbahari.lellafood.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAXSSaJbI:APA91bGqGYIhDTFnlY8x6gFKizhv9N02uoRolajYbuN-ngEbt_vB8YjLm81mRQx5oMF9gYdZLqTXa6fZ6k60Ft17ykXZbWI2LbcaqfQHHFvZaqMKTNez8w5SIw05SrF5Q-HpwkAQulGw"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);


}
