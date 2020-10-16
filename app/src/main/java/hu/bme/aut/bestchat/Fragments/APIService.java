package hu.bme.aut.bestchat.Fragments;

import hu.bme.aut.bestchat.Notification.MyResponse;
import hu.bme.aut.bestchat.Notification.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization: key=AAAAcsGO7yw:APA91bEalsvv3oL24YTiSmXXvVg-ENyEc-apGEzgUHY5CRC9lc1txNsXZ9aSusS-BbqpQunkSQSNowHtkSGJbjxex5qXl_fQZxUZyVGQ2gQDMn2efEkQyQmwtoSpS_ZzldZNDaXDP5F0"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
