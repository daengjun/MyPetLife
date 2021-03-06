package com.example.petdiary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petdiary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kakao.auth.ApiErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private SessionCallback sessionCallback;

    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionCallback = new SessionCallback();
        Session.getCurrentSession().addCallback(sessionCallback);
        Session.getCurrentSession().checkAndImplicitOpen();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.loginButton).setOnClickListener(onClickListener);
        findViewById(R.id.signUpButton).setOnClickListener(onClickListener);
        findViewById(R.id.findPasswordButton).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.loginButton:
                    login();
                    break;
                case R.id.signUpButton:
                    startSignUpActivity();
                    break;
                case R.id.findPasswordButton:
                    startFindPasswordActivity();
                    break;
            }
        }
    };

    public static boolean isValidPassword(String password){
        boolean err = false;
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*\\W)(?=\\S+$).{8,20}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(password);
        if(m.matches()){
            err = true;
        }
        return err;
    }

    private void login(){
        String email = ((EditText)findViewById(R.id.emailEditText)).getText().toString();
        password = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();

        if(email.length() > 0 && password.length() > 0){
            // ???????????? ??????????????? ????????? ??????????????? ?????????????????? ???????????? ?????? ???????????? ????????????
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();

                                if(isValidPassword(password)){
                                    startToast("???????????? ?????????????????????.");
                                    startMainActivity();
                                    finish();
                                } else {
                                    startToast("??????????????? ????????? ???????????????. ??????????????????.");
                                    startSetPasswordActivity();
                                    ((EditText) findViewById(R.id.emailEditText)).setText("");
                                    ((EditText) findViewById(R.id.passwordEditText)).setText("");
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                //Log.w(TAG, "signInWithEmail:failure", task.getException());
                                if(task.getException() != null){
                                    Log.d(TAG, task.getException().toString());
                                    startToast("???????????? ??????????????? ????????? ?????????.");
                                }
                            }
                        }
                    });
        } else {
            startToast("????????? ?????? ??????????????? ????????? ?????????.");
        }
    }

    private void startToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void startMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void startSetPasswordActivity(){
        Intent intent = new Intent(this, SetPasswordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void startSignUpActivity(){
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    private void startFindPasswordActivity(){
        Intent intent = new Intent(this, FindPasswordActivity.class);
        startActivity(intent);
    }

    private long backKeyPressedTime = 0;
    private Toast toast;

    public void onBackPressed(){
        //super.onBackPressed();
        // ??????????????? ???????????? ????????? ????????? ????????? 2?????? ?????? ??????????????? ?????? ???
        // ??????????????? ???????????? ????????? ????????? ????????? 2?????? ???????????? Toast Show
        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'??????\' ????????? ?????? ??? ???????????? ???????????????.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // ??????????????? ???????????? ????????? ????????? ????????? 2?????? ?????? ??????????????? ?????? ???
        // ??????????????? ???????????? ????????? ????????? ????????? 2?????? ????????? ???????????? ??????
        // ?????? ????????? Toast ??????
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
            toast.cancel();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    private class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            UserManagement.getInstance().me(new MeV2ResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    int result = errorResult.getErrorCode();
                    //Log.e("###123", errorResult.getErrorMessage());
                    if(result == ApiErrorCode.CLIENT_ERROR_CODE) {
                        Toast.makeText(getApplicationContext(), "???????????? ????????? ??????????????????. ?????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(),"????????? ?????? ????????? ??????????????????: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    //Toast.makeText(getApplicationContext(),"????????? ???????????????. ?????? ????????? ?????????: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(MeV2Response result) {
                    String needsScopeAutority = ""; // ?????? ????????? ???????????? ?????? ????????? ????????? ???????????? ??????
                    if(result.getKakaoAccount().needsScopeAccountEmail()) {
                        needsScopeAutority = needsScopeAutority + "?????????";
                    }

                    Intent intent = new Intent(getApplicationContext(), KakaoSignUpActivity.class);
                    intent.putExtra("nickName", result.getNickname());
                    intent.putExtra("profile", result.getProfileImagePath());
                    //intent.putExtra("email", result.getKakaoAccount().getEmail());

                    if (result.getKakaoAccount().hasEmail() == OptionalBoolean.TRUE){
                        intent.putExtra("email", result.getKakaoAccount().getEmail());
                        Log.e("###", result.getKakaoAccount().getEmail());
                    }
                    startActivity(intent);
                }
            });
        }

        @Override
        public void onSessionOpenFailed(KakaoException e) {
            Toast.makeText(getApplicationContext(), "????????? ?????? ????????? ??????????????????. ????????? ????????? ??????????????????: "+e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
