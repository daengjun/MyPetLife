package com.example.petdiary.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.petdiary.R;
import com.example.petdiary.activity.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.BufferedInputStream;
import java.io.InputStream;


public class MyFireBaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private Bitmap bmp = null;
    OkHttpClient client = new OkHttpClient();

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d("dangjun", "Token : " + s);
    }

    /**
     * @내용 : 포그라운드 상태인 앱에서 알림 메시지 또는 데이터 메시지를 수신하려면
     * onMessageReceived 콜백을 처리하는 코드를 작성해야 함
     **/
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
            sendNotification(remoteMessage.getNotification());

        }
    }
    /**
     * @내용 : 파이어베이스 remoteMessage를 통해 받은 url로 이미지 값 가져와 비트맵으로 변환하는 함수
     *         okhttp 라이브러리를 사용해서 Get 요청 동기처리
     *         이미지를 받아오는데 실패했을 경우 false 반환
     **/
    public boolean getImage(Uri string){

        try {
            // GET 요청을 Request.Builder 를 통해 만듦 (get 요청임을 명시)
            Request request = new Request.Builder().url(String.valueOf(string)).get().build();

            // client 객체의 newCall() 메소드에 만들어진 Request를 전달하고, execute() 메소드를 실행
            Response response = client.newCall(request).execute();

            // execute() 메소드는 요청에 대한 응답이 올때까지 기다렸다가 반환
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (body != null) {
                    ResponseBody in = response.body();
                    InputStream inputStream = in.byteStream();
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                    bmp= BitmapFactory.decodeStream(bufferedInputStream);
                    Log.d("jjslee","bitmap value = "+bmp.toString());
                }
            }
            return true;
        } catch(Exception e) {
            return false;
        }

    }
    private void sendNotification(RemoteMessage.Notification notification) {

        // 0. Pending Intent
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) ;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT);

        // 1. 알림 메시지를 관리하는 notificationManager 객체 추출
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = getNotificationBuilder(notificationManager, "chennal id", "dangjun chennal");

        builder.setContentTitle(notification.getTitle())       // 콘솔에서 설정한 타이틀
                .setContentText(notification.getBody())         // 콘솔에서 설정한 내용
                .setSmallIcon(R.mipmap.ic_main)
//                .setColor(getResources().getColor(R.color.))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)// 사용자가 노티피케이션을 탭시 ResultActivity로 이동하도록 설정
                .setAutoCancel(true);             // 메시지를 터치하면 메시지가 자동으로 제거됨

        // 이미지를 성공적으로 받아와졌을때만 수행
        if(getImage(notification.getImageUrl())){
            builder.setLargeIcon(bmp)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(bmp)
                            .bigLargeIcon(null));
        }

        notificationManager.notify(1234, builder.build()); // 고유숫자로 노티피케이션 동작시킴

        if (bmp != null) {
            bmp.recycle();
            bmp = null;
        }

    }

    /**
     * @내용 : 안드로이드 8.0 이상부터 Notification은 채널별로 관리해야만 함
     * (channel별이라함은 Notification을 보낼때마다 막쌓이는것이 아니고 앱별로 그룹지어서 쌓이도록 하는 것 )
     **/
    protected NotificationCompat.Builder getNotificationBuilder(NotificationManager notificationManager, String channelId, CharSequence channelName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // 2. NotificationChannel채널 객체 생성 (첫번재 인자: 관리id, 두번째 인자: 사용자에게 보여줄 채널 이름)
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);

            // 3. 알림 메시지를 관리하는 객체에 노티피케이션 채널을 등록
            notificationManager.createNotificationChannel(channel);
            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            return builder;

        } else { // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남
            builder.setSmallIcon(R.mipmap.ic_launcher);
            return builder;

        }
    }
}


//public class MyFireBaseMessagingService extends FirebaseMessagingService{
//
//    @Override
//    public void onNewToken(String s) {
//        super.onNewToken(s);
//        Log.d("FCM_PetDiary", s);
//    }
//
//    @Override
//    public void onMessageReceived(RemoteMessage remoteMessage) {
//        super.onMessageReceived(remoteMessage);
//        Log.d("dangjun", "dangjun fcm signal");
//        String messageBody = remoteMessage.getNotification().getBody();
//        String messageTitle = remoteMessage.getNotification().getTitle();
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//        String channelId = "Pet Diary";
//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//
//        NotificationCompat.Builder notificationBuilder =
//                new NotificationCompat.Builder(this, channelId)
//                        .setSmallIcon(R.drawable.chat_icon)
//                        .setContentTitle(messageTitle)
//                        .setContentText(messageBody)
//                        .setVibrate(new long[]{3000,3000,3000})
//                        .setSound(defaultSoundUri)
//                        .setAutoCancel(true)
//                        .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            String channelName = "Pet Diary_Android App";
//            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
//            notificationManager.createNotificationChannel(channel);
//        }
//        notificationManager.notify(0, notificationBuilder.build());
//    }
//}
