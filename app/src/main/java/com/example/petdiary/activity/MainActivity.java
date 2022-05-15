package com.example.petdiary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.petdiary.R;
import com.example.petdiary.fragment.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private String password = "";

    TextView toolbarNickName;
    ImageView genter_icon;
    BottomNavigationView bottomNavigationView;
    FragmentMain fragmentMain;
    FragmentSub fragmentSub;
    FragmentNewPost fragmentNewPost;
    FragmentMy fragmentMy;
    FragmentContentMain fragmentContentMain;
    String fullString;
    private FragmentManager fragmentManager;
    Menu menu;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ProgressBar progressBar;
    private DrawerLayout drawerLayout;
    private View drawerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getAppKeyHash();

            /* 어플 실행 할때 애니메이션 할당 */
//        findViewById(R.id.splish).animate().scaleX(1.2f).scaleY(1.2f).setDuration(3500).start();

       Intent fcm = new Intent(getApplicationContext(), FirebaseMessagingService.class);
        startService(fcm);

        getSupportActionBar().hide();
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.start_progressBar).setVisibility(View.GONE);
                findViewById(R.id.splish).setVisibility(View.GONE);
                getSupportActionBar().show();
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);
            }
        }, 3500);

        Intent intent = getIntent();
        if(intent != null) {
            String notificationData = intent.getStringExtra("FCM_PetDiary");
            if(notificationData != null)
                Log.d("FCM_PetDiary", notificationData);
            Log.d("FCM_PetDiary", FirebaseMessaging.getInstance().toString());
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerView = (View) findViewById(R.id.drawerView);
        drawerLayout.setDrawerListener(listener);
        toolbarNickName = findViewById(R.id.toolbar_nickName);
        genter_icon = findViewById(R.id.genter_icon);

        Uri data = intent.getData();

        if (data != null)
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        Log.d(TAG, "onComplete: " + token);
                    }
                });


        /*딥링크로 받아오는 부분 */
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            Log.d(TAG, "dangjun deeplink " + deepLink.toString());

                            fullString  = deepLink.toString();
                            Log.d(TAG, "dagnjun uid : " + fullString);


                            Handler mHandler = new Handler();
                                        mHandler.postDelayed(new Runnable() {
                                            public void run() {
                                                if(fullString != null) {
                                                    String[] splitText = fullString.split("/");
                                                    fragmentSub.getPostID(splitText[3]);
                                                }
                                            }
                                        }, 3000);

                        }

                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...

                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });

        if (user == null) {
            /* 로그인 페이지로 이동 */
            myStartActivityFlag(LoginActivity.class);
            finish();
        } else {
            /* 자동로그인 확인 */
            checkPassword();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 햄버거메뉴
    public void blank(View view) {
    }

    public void bookMarkOnClick(View view){
        myStartActivity(SettingBookMarkActivity.class);
    }

    public void blockFriendOnClick(View view) {
        myStartActivity(SettingBlockFriendsActivity.class);
    }

    public void noticeOnClick(View view){
        myStartActivity(SettingNotificationActivity.class);
    }

    public void passwordSetOnClick(View view){
        myStartActivity(LoginConfirmActivity.class, "setPassword");
    }

    public void customerCenterOnClick(View view){
        myStartActivity(SettingCustomerActivity.class);
    }

    public void logOutOnClick(View view){
        startPopupActivity();
    }

    public void unRegisterOnClick(View view){
        myStartActivity(LoginConfirmActivity.class, "out");
        //startToast("회원탈퇴");
    }

    public void AppInfoOnClick(View view){
        myStartActivity(SettingAppInfoActivity.class);
        //startToast("앱 정보");
    }

    private void startPopupActivity(){
        Intent intent = new Intent(getApplicationContext(), LogoutPopupActivity.class);
        startActivityForResult(intent, 0);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                if(drawerLayout.isDrawerOpen(drawerView)){
                    drawerLayout.closeDrawer(drawerView);
                } else {
                    drawerLayout.openDrawer(drawerView);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);
        }

        @Override
        public void onDrawerStateChanged(int newState) {
        }
    };

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setFirst() {
        fragmentManager = getSupportFragmentManager();

        fragmentMain = new FragmentMain();
        fragmentManager.beginTransaction().replace(R.id.main_layout, fragmentMain).commit();

        if(fragmentSub == null){
            fragmentSub = new FragmentSub();
            fragmentManager.beginTransaction().add(R.id.main_layout, fragmentSub).commit();
        }


        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        menu = bottomNavigationView.getMenu();
        menu.findItem(R.id.tab2).setChecked(false);
        menu.findItem(R.id.tab1).setChecked(true);

        fragmentManager.beginTransaction().hide(fragmentSub).commit();



        /* 하단 네비게이션바 클릭할때마다 동작 */
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.tab1:
                        if(fragmentMain == null){
                            fragmentMain = new FragmentMain();
                            fragmentManager.beginTransaction().add(R.id.main_layout, fragmentMain).commit();

                        }
                        if(fragmentMain != null){
                            fragmentManager.beginTransaction().show(fragmentMain).commit();
                        }
                        if(fragmentSub != null){
                            fragmentManager.beginTransaction().hide(fragmentSub).commit();
                        }
                        if(fragmentNewPost != null){
                            fragmentManager.beginTransaction().hide(fragmentNewPost).commit();
                        }
                        if(fragmentMy != null){
                            fragmentManager.beginTransaction().hide(fragmentMy).commit();
                        }
                        if(fragmentContentMain != null){
                            fragmentManager.beginTransaction().hide(fragmentContentMain).commit();
                        }
                        return true;
                    case R.id.tab2:
                        if(fragmentSub == null){
                            fragmentSub = new FragmentSub();
                            fragmentManager.beginTransaction().add(R.id.main_layout, fragmentSub).commit();
                        }
                        if(fragmentSub != null){
                            fragmentManager.beginTransaction().show(fragmentSub).commit();
                        }
                        if(fragmentMain != null){
                            fragmentManager.beginTransaction().hide(fragmentMain).commit();
                        }
                        if(fragmentNewPost != null){
                            fragmentManager.beginTransaction().hide(fragmentNewPost).commit();
                        }
                        if(fragmentMy != null){
                            fragmentManager.beginTransaction().hide(fragmentMy).commit();
                        }
                        if(fragmentContentMain != null){
                            fragmentManager.beginTransaction().hide(fragmentContentMain).commit();
                        }
                        return true;
                    case R.id.tab3:
                        if(fragmentNewPost == null){
                            fragmentNewPost = new FragmentNewPost();
                            fragmentManager.beginTransaction().add(R.id.main_layout, fragmentNewPost).commit();
                        }
                        if(fragmentNewPost != null){
                            fragmentManager.beginTransaction().show(fragmentNewPost).commit();
                        }
                        if(fragmentMain != null){
                            fragmentManager.beginTransaction().hide(fragmentMain).commit();
                        }
                        if(fragmentSub != null){
                            fragmentManager.beginTransaction().hide(fragmentSub).commit();
                        }
                        if(fragmentMy != null){
                            fragmentManager.beginTransaction().hide(fragmentMy).commit();
                        }
                        if(fragmentContentMain != null){
                            fragmentManager.beginTransaction().hide(fragmentContentMain).commit();
                        }
                        return true;
                    case R.id.tab4:
                        if(fragmentMy == null){
                            fragmentMy = new FragmentMy();
                            fragmentManager.beginTransaction().add(R.id.main_layout, fragmentMy).commit();
                        }
                        if(fragmentMy != null){
                            fragmentManager.beginTransaction().show(fragmentMy).commit();
                        }
                        if(fragmentMain != null){
                            fragmentManager.beginTransaction().hide(fragmentMain).commit();
                        }
                        if(fragmentSub != null){
                            fragmentManager.beginTransaction().hide(fragmentSub).commit();
                        }
                        if(fragmentNewPost != null){
                            fragmentManager.beginTransaction().hide(fragmentNewPost).commit();
                        }
                        if(fragmentContentMain != null){
                            fragmentManager.beginTransaction().hide(fragmentContentMain).commit();
                        }
                        return true;
                    case R.id.tab5:
                        if(fragmentContentMain == null){
                            fragmentContentMain = new FragmentContentMain();
                            fragmentManager.beginTransaction().add(R.id.main_layout, fragmentContentMain).commit();
                        }
                        if(fragmentContentMain != null){
                            fragmentManager.beginTransaction().show(fragmentContentMain).commit();
                        }
                        if(fragmentMain != null){
                            fragmentManager.beginTransaction().hide(fragmentMain).commit();
                        }
                        if(fragmentSub != null){
                            fragmentManager.beginTransaction().hide(fragmentSub).commit();
                        }
                        if(fragmentNewPost != null){
                            fragmentManager.beginTransaction().hide(fragmentNewPost).commit();
                        }
                        if(fragmentMy != null){
                            fragmentManager.beginTransaction().hide(fragmentMy).commit();
                        }
                        return true;
                    default: return false;

                }
            }
        });
    }

    /********** 새로고침 메서드 ***********/

    public void mainReplaceFragment(boolean check){
        if(fragmentNewPost!=null) {
            fragmentManager.beginTransaction().remove(fragmentNewPost).commit();
            fragmentNewPost = null;
        }
        if(fragmentMain != null&&check){
            fragmentMain.setInfo();
            fragmentManager.beginTransaction().show(fragmentMain).commit();
        }
        if(fragmentSub != null){
            fragmentManager.beginTransaction().hide(fragmentSub).commit();
            fragmentSub.refresh();
        }
        if(fragmentNewPost != null){
            fragmentManager.beginTransaction().hide(fragmentNewPost).commit();
        }
        if(fragmentMy != null){
            fragmentManager.beginTransaction().hide(fragmentMy).commit();
            fragmentMy.MypageRefresh();
        }
        if(fragmentContentMain != null){
            fragmentManager.beginTransaction().hide(fragmentContentMain).commit();
        }
    }

    public void uploadReplaceFragment(boolean check){
        if(fragmentNewPost != null){
            fragmentManager.beginTransaction().show(fragmentNewPost).commit();
        }
        if(fragmentMain != null&&check){
            fragmentMain.setInfo();
            fragmentManager.beginTransaction().hide(fragmentMain).commit();
        }
        if(fragmentSub != null){
            fragmentManager.beginTransaction().hide(fragmentSub).commit();
            fragmentSub.refresh();
        }
        if(fragmentMy != null){
            fragmentManager.beginTransaction().hide(fragmentMy).commit();
            fragmentMy.MypageRefresh();
        }
        if(fragmentContentMain != null){
            fragmentManager.beginTransaction().hide(fragmentContentMain).commit();
        }
    }



    public void containReplaceFragment(boolean check){
        if(fragmentContentMain != null){
            fragmentManager.beginTransaction().show(fragmentContentMain).commit();
        }
        if(fragmentMain != null&&check){
            fragmentMain.setInfo();
            fragmentManager.beginTransaction().hide(fragmentMain).commit();
        }
        if(fragmentSub != null){
            fragmentManager.beginTransaction().hide(fragmentSub).commit();
            fragmentSub.refresh();

        }
        if(fragmentNewPost != null){
            fragmentManager.beginTransaction().hide(fragmentNewPost).commit();

        }
        if(fragmentMy != null){
            fragmentManager.beginTransaction().hide(fragmentMy).commit();
            fragmentMy.MypageRefresh();
        }
    }

    public void subReplaceFragment(boolean check){
        if(fragmentSub != null){
            fragmentManager.beginTransaction().show(fragmentSub).commit();
            fragmentSub.refresh();
        }
        if(fragmentMain != null&&check){
            fragmentMain.setInfo();
            fragmentManager.beginTransaction().hide(fragmentMain).commit();
        }

        if(fragmentNewPost != null){
            fragmentManager.beginTransaction().hide(fragmentNewPost).commit();

        }
        if(fragmentMy != null){
            fragmentManager.beginTransaction().hide(fragmentMy).commit();
            fragmentMy.MypageRefresh();

        }
        if(fragmentContentMain != null){
            fragmentManager.beginTransaction().hide(fragmentContentMain).commit();

        }
    }

    public void myReplaceFragment(boolean check){
        if(fragmentNewPost!=null) {
            fragmentManager.beginTransaction().remove(fragmentNewPost).commit();
            fragmentNewPost = null;
        }
        if(fragmentMain != null&&check){
            fragmentMain.setInfo();
            fragmentManager.beginTransaction().hide(fragmentMain).commit();
        }
        if(fragmentSub != null){
            fragmentManager.beginTransaction().hide(fragmentSub).commit();
            fragmentSub.refresh();
        }
        if(fragmentNewPost != null){
            fragmentManager.beginTransaction().hide(fragmentNewPost).commit();
        }
        if(fragmentMy != null){
            fragmentManager.beginTransaction().show(fragmentMy).commit();
            fragmentMy.MypageRefresh();

        }
        if(fragmentContentMain != null){
            fragmentManager.beginTransaction().hide(fragmentContentMain).commit();

        }
    }

    public static boolean isValidPassword(String password) {
        boolean err = false;
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*\\W)(?=\\S+$).{8,20}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(password);
        if (m.matches()) {
            err = true;
        }
        return err;
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }
    private void myStartActivity(Class c, String s) {
        Intent intent = new Intent(this, c);
        intent.putExtra("setting", s);
        startActivity(intent);
    }

    private void myStartActivityFlag(Class c) {
        Intent intent = new Intent(this, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void checkPassword() {
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    //Log.d("@@@", FirebaseAuth.getInstance().getCurrentUser().getUid()+"");
                    if (document != null) {
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            password = document.getData().get("password").toString();
                            toolbarNickName.setText(document.getData().get("nickName").toString() + " 님");
                            if(document.getData().get("profileImg").toString().length() > 0 ){
                                setProfileImg(document.getData().get("profileImg").toString());
                            }
                            if (isValidPassword(password)) {
                                setFirst();
                            } else {
                                myStartActivityFlag(SetPasswordActivity.class);
                                finish();
                            }
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void setProfileImg(String profileImg) {
        Glide.with(this).load(profileImg).centerCrop().override(500).into(genter_icon);
    }

    private long backKeyPressedTime = 0;
    private Toast toast;

    @Override
    public void onBackPressed(){
        if(drawerLayout.isDrawerOpen(drawerView)){
            drawerLayout.closeDrawer(drawerView);
        } else {

            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
                toast.cancel();
            }
        }
    }

    public void updateMainContent() {
        fragmentMain.setInfo();

    }

    public void updateContent() {
        if (fragmentNewPost != null) {
            fragmentManager.beginTransaction().remove(fragmentNewPost).commit();
            fragmentNewPost = null;
        }
        if (fragmentMain != null) {
            fragmentManager.beginTransaction().show(fragmentMain).commit();
        }
        if(fragmentSub != null){
            fragmentManager.beginTransaction().hide(fragmentSub).commit();
            fragmentSub.refresh();
        }
        if(fragmentNewPost != null){
            fragmentManager.beginTransaction().hide(fragmentNewPost).commit();
        }
        if(fragmentMy != null){
            fragmentManager.beginTransaction().hide(fragmentMy).commit();
            fragmentMy.MypageRefresh();
        }
        if(fragmentContentMain != null){
            fragmentManager.beginTransaction().hide(fragmentContentMain).commit();
        }
    }



    public void refresh(boolean check) {
        Log.d(TAG, "refresh: " + menu.findItem(R.id.tab2).isChecked());

        if (menu.findItem(R.id.tab1).isChecked()){
        mainReplaceFragment(check);

        }
        else if(menu.findItem(R.id.tab2).isChecked()){
            subReplaceFragment(check);
        }
        else if(menu.findItem(R.id.tab3).isChecked()){
             uploadReplaceFragment(check);
        }
        else if(menu.findItem(R.id.tab4).isChecked()){
            myReplaceFragment(check);
        }

        else if(menu.findItem(R.id.tab5).isChecked()){
            containReplaceFragment(check);
        }

    }

}