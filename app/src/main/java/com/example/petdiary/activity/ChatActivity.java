package com.example.petdiary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petdiary.adapter.MyAdapter;
import com.example.petdiary.R;
import com.example.petdiary.data.Chat;
import com.example.petdiary.util.sub_ItemDecoration;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatMain";
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    FirebaseDatabase database;
    EditText etText;
    Button btnSend, picture;
    String stEmail, nickName, my;
    String nn[];
    ArrayList<Chat> chatArrayList;
    MyAdapter mAdapter;
    TextView topNick;
    private RecyclerView.LayoutManager layoutManager;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatroom);

        Intent intent = getIntent();
        nickName = intent.getStringExtra("nickName");
        my = intent.getStringExtra("my");
        topNick = findViewById(R.id.guest);
        topNick.setText(nickName);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        chatArrayList = new ArrayList<>();
        stEmail = user.getEmail();
        nn = new String[2];

        btnSend = findViewById(R.id.btn_send);
        etText = findViewById(R.id.chat);

        recyclerView = findViewById(R.id.room_recyclerview);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new MyAdapter(chatArrayList, stEmail, nickName);
        recyclerView.addItemDecoration(new sub_ItemDecoration(getApplicationContext(),10));
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();

        recyclerView.setAdapter(mAdapter);

        etText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEND:
                        // 검색 동작
                        break;
                    default:
                        // 기본 엔터키 동작
                        return false;
                }
                return true;
            }
        });

        final FirebaseFirestore db = FirebaseFirestore.getInstance();



        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                if(document.get("nickName").toString().equals(nickName)) {

                                    ChildEventListener childEventListener = new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                                            Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                                            Chat chat = dataSnapshot.getValue(Chat.class);

                                            chatArrayList.add(chat);
                                            mAdapter.notifyDataSetChanged();
                                            recyclerView.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                                                }
                                            });
                                        }

                                        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) { }
                                        public void onChildRemoved(DataSnapshot dataSnapshot) { }
                                        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}
                                        public void onCancelled(DatabaseError databaseError) {}
                                    };

                                    nn[0] = user.getUid();
                                    nn[1] = document.getId();
                                    Arrays.sort(nn);

//                                    DatabaseReference ref = database.getReference("friend").child(user.getUid()).child(document.getId()).child("message");
//                                    ref.addChildEventListener(childEventListener);
                                    DatabaseReference ref = database.getReference("chat").child(user.getUid() + "&" + document.getId()).child("message");
                                    ref.addChildEventListener(childEventListener);

                                }
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

        //보내기Send

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    if (etText.getText().toString().length() > 0) {
                        final String stText = etText.getText().toString();

                        Toast.makeText(ChatActivity.this, "MSG : " + stText, Toast.LENGTH_SHORT).show();
                    etText.getText().clear();
                    database = FirebaseDatabase.getInstance();

                    db.collection("users")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                            if(document.get("nickName").toString().equals(nickName)) {

                                                Calendar c = Calendar.getInstance();
                                                SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                                                String datetime = dateformat.format(c.getTime());

                                                nn[0] = user.getUid();
                                                nn[1] = document.getId();
                                                Arrays.sort(nn);

                                                //DatabaseReference myRef = database.getReference("friend").child(user.getUid()).child(document.getId()).child("message").child(datetime);
                                                DatabaseReference myRef = database.getReference("chat").child(user.getUid() + "&" + document.getId()).child("message").child(datetime);
                                                DatabaseReference dr = database.getReference("chat").child(document.getId() + "&" + user.getUid()).child("message").child(datetime);
                                                Hashtable<String, String> numbers
                                                        = new Hashtable<String, String>();
                                                numbers.put("email", stEmail);
                                                numbers.put("text", stText);
                                                myRef.setValue(numbers);
                                                dr.setValue(numbers);


                                            }
                                        }
                                    } else {
                                        Log.w(TAG, "Error getting documents.", task.getException());
                                    }
                                }
                            });


                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                        }
                    });

                }
            }
        });


        picture = findViewById(R.id.picture);
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* 다중 선택 오류나서 기능 보류 */
//                Intent intent = new Intent(ChatActivity.this, ImageChoicePopupActivity2.class);
           Intent intent = new Intent(ChatActivity.this, ImageChoicePopupActivity.class);

                startActivityForResult(intent, 0);
            }
        });
    }

    String[] sImg;
    String[] uri;
    ImageView iv;
    String ca;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: " + requestCode);
        sImg = new String[9];
        uri = new String[9];
        ca = new String();
        iv = findViewById(R.id.ivChat);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {

                for (
                        int i = 0; i < 9; i++) {
                    sImg[i] = data.getStringExtra("postImgPath" + i + "");
                    uri[i] = data.getStringExtra("uri" + i + "");

                    if (uri[i] != null) {
                        final int j = i;
                        database = FirebaseDatabase.getInstance();

                        db.collection("users")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                Log.d(TAG, document.getId() + " => " + document.getData());
                                                if(document.get("nickName").toString().equals(nickName)) {

                                                    Calendar c = Calendar.getInstance();
                                                    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                                                    String datetime = dateformat.format(c.getTime());

                                                    nn[0] = user.getUid();
                                                    nn[1] = document.getId();
                                                    Arrays.sort(nn);

                                                    //DatabaseReference myRef = database.getReference("friend").child(user.getUid()).child(document.getId()).child("message").child(datetime);
                                                    DatabaseReference myRef = database.getReference("chat").child(user.getUid() + "&" + document.getId()).child("message").child(datetime);
                                                    DatabaseReference dr = database.getReference("chat").child(document.getId() + "&" + user.getUid()).child("message").child(datetime);
                                                    Hashtable<String, String> numbers
                                                            = new Hashtable<String, String>();
                                                    numbers.put("email", stEmail);
                                                    numbers.put("image", sImg[j] + "");
                                                    myRef.setValue(numbers);
                                                    dr.setValue(numbers);

                                                }
                                            }
                                        } else {
                                            Log.w(TAG, "Error getting documents.", task.getException());
                                        }
                                    }
                                });


                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                            }
                        });
                    }

                }

            } else if (resultCode == 2) {
                ca = data.getStringExtra("camera");
                Log.d(TAG, "onActivityResult: ca" + ca);
                FirebaseStorage storage = FirebaseStorage.getInstance("gs://petstory-f9843.appspot.com");
                final StorageReference storageRef = storage.getReference();

                database = FirebaseDatabase.getInstance();

                Calendar c = Calendar.getInstance();
                SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd k:mm:ss");
                String datetime = dateformat.format(c.getTime());

                db.collection("users")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        if(document.get("nickName").toString().equals(nickName)) {

                                            Calendar c = Calendar.getInstance();
                                            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                                            String datetime = dateformat.format(c.getTime());

                                            nn[0] = user.getUid();
                                            nn[1] = document.getId();
                                            Arrays.sort(nn);

                                            //DatabaseReference myRef = database.getReference("friend").child(user.getUid()).child(document.getId()).child("message").child(datetime);
                                            DatabaseReference myRef = database.getReference("chat").child(user.getUid() + "&" + document.getId()).child("message").child(datetime);
                                            DatabaseReference dr = database.getReference("chat").child(document.getId() + "&" + user.getUid()).child("message").child(datetime);
                                            Hashtable<String, String> numbers
                                                    = new Hashtable<String, String>();
                                            numbers.put("email", stEmail);
                                            numbers.put("image", ca);
                                            myRef.setValue(numbers);
                                            dr.setValue(numbers);

                                        }
                                    }
                                } else {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                }
                            }
                        });

                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {

                        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                    }
                });

            }

        }
    }

}