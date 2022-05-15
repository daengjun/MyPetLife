package com.example.petdiary.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.petdiary.data.Chat;
import com.example.petdiary.R;
import com.example.petdiary.adapter.CommentAdapter;
import com.example.petdiary.adapter.ViewPageAdapterDetail;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

public class CommentActivity extends AppCompatActivity {

    private static final String TAG = "Comment2";
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    FirebaseDatabase database;
    EditText etText;
    Button btnSend;
    String stEmail;

    ArrayList<Chat> commentArrayList;
    CommentAdapter cAdapter;
    private RecyclerView.LayoutManager layoutManager;

    ImageView user_profileImage_ImageView;

    private String postID;
    private String uid;
    private String content;
    private String nickName;
    private String Image = "";
    private String Image2 = "";
    private String Image3 = "";
    private String Image4 = "";
    private String Image5 = "";
    TextView post_nickName;
    TextView post_content;
    ViewPageAdapterDetail viewPageAdapter;
    ViewPager viewPager;
    WormDotsIndicator wormDotsIndicator;
    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Intent intent = getIntent();
        postID = intent.getStringExtra("postID");
        uid = intent.getStringExtra("uid");
        nickName = intent.getStringExtra("nickName");
        content = intent.getStringExtra("content");
        Image = intent.getStringExtra("image");
        Image2 = intent.getStringExtra("image2");
        Image3 = intent.getStringExtra("image3");
        Image4 = intent.getStringExtra("image4");
        Image5 = intent.getStringExtra("image5");

        final String[] profileImg = new String[1];
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(uid);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        if (document.exists()) {
                            ImageView profileImage = (ImageView) findViewById(R.id.Profile_image);
                            profileImg[0] = document.getData().get("profileImg").toString();
                            if (profileImg[0].length() > 0) {
                                Glide.with(getApplicationContext()).load(document.getData().get("profileImg").toString()).centerCrop().override(500).into(profileImage);
                            } else {
                                profileImage.setImageResource(R.drawable.icon_person);
                            }
                        } else {
                            //Log.d("###", "No such document");
                        }
                    }
                } else {
                    //Log.d("###", "get failed with ", task.getException());
                }
            }
        });

        post_nickName = findViewById(R.id.Profile_Name);
        post_content = findViewById(R.id.PostText_view);
        post_nickName.setText(nickName);
        post_content.setText(content);
        viewPager = (ViewPager) findViewById(R.id.comment_imageView);
        viewPager.setVisibility(View.GONE);
        relativeLayout = findViewById(R.id.relatives);
        wormDotsIndicator = (WormDotsIndicator) findViewById(R.id.comment_worm_dots_indicator);
        wormDotsIndicator.setVisibility(View.GONE);
        Log.d(TAG, "onCreate: Image" + Image);
        if (!Image.equals("https://firebasestorage.googleapis.com/v0/b/petdiary-794c6.appspot.com/o/images%2Fempty.png?alt=media&token=c41b1cc0-d610-4964-b00c-2638d4bfd8bd")) {
            Log.e("###111", viewPager.getCurrentItem() + " ");
            viewPageAdapter = new ViewPageAdapterDetail(true, Image, Image2, Image3, Image4, Image5, getApplicationContext());
            viewPager.setAdapter(viewPageAdapter);

            wormDotsIndicator.setViewPager(viewPager);
            wormDotsIndicator.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
        } else {
            relativeLayout.setVisibility(View.VISIBLE);
        }
        if (content.length() == 0) {
            post_content.setVisibility(View.INVISIBLE);
        }

        user_profileImage_ImageView = findViewById(R.id.bottom_Profile_Image);
        setImg();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        commentArrayList = new ArrayList<>();
        FirebaseUser user = mAuth.getCurrentUser();
        stEmail = user.getEmail();

        btnSend = findViewById(R.id.input_button);
        etText = findViewById(R.id.text_input);

        recyclerView = findViewById(R.id.comment_recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        cAdapter = new CommentAdapter(commentArrayList, stEmail, postID, getApplicationContext());
        recyclerView.setAdapter(cAdapter);


        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                // A new comment has been added, add it to the displayed list
                Chat chat = dataSnapshot.getValue(Chat.class);
                commentArrayList.add(chat);
                cAdapter.notifyDataSetChanged();
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                String commentKey = dataSnapshot.getKey();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(getApplicationContext(), "Failed to load comments.", Toast.LENGTH_SHORT).show();
            }
        };
        DatabaseReference ref = database.getReference("comment/" + postID);
        ref.addChildEventListener(childEventListener);


        /////////////////////////////////////////입력
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etText.getText().toString().length() > 0) {
                    String stText = etText.getText().toString();
                    //Toast.makeText(getApplicationContext(), "MSG : " + stText, Toast.LENGTH_SHORT).show();
                    etText.getText().clear();
                    database = FirebaseDatabase.getInstance();
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                    String datetime = dateformat.format(c.getTime());
                    DatabaseReference myRef = database.getReference("comment/" + postID).child(datetime);
                    Hashtable<String, String> numbers
                            = new Hashtable<String, String>();
                    numbers.put("email", stEmail);
                    numbers.put("text", stText);
                    numbers.put("date", datetime);
                    myRef.setValue(numbers);

                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                        }
                    });

                }
            }
        });

    }

    private void setImg() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        storageRef.child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "_profileImage.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String profileImg = uri.toString();
                setProfileImg(profileImg);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    private void setProfileImg(String profileImg) {
        Glide.with(this).load(profileImg).centerCrop().override(500).into(user_profileImage_ImageView);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}