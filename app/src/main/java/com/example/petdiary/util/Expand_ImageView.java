package com.example.petdiary.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.petdiary.R;
import com.example.petdiary.activity.CommentActivity;
import com.example.petdiary.activity.ContentEditActivity;
import com.example.petdiary.activity.SettingCustomerActivity;
import com.example.petdiary.activity.UserPageActivity;
import com.example.petdiary.adapter.ViewPageAdapterDetail;
import com.example.petdiary.info.BlockFriendInfo;
import com.example.petdiary.info.BookmarkInfo;
import com.example.petdiary.info.FriendInfo;
import com.example.petdiary.info.PostLikeInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.Hashtable;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class Expand_ImageView extends AppCompatActivity implements callBackListener {

    private static callBackListener calbacklistener;
    private String friendChecked;
    private String bookmarkChecked;
    private String likeChecked;
    private String postID;
    private String uid;
    private String imageUrl1;
    private String imageUrl2;
    private String imageUrl3;
    private String imageUrl4;
    private String imageUrl5;
    private String content;
    private ArrayList<String> hashTag = new ArrayList<String>();
    private String date = "";
    private String nickName;
    private String Category;
    private int favoriteCount;
    Activity activity;


    ViewPageAdapterDetail viewPageAdapter;
    ViewPager viewPager;
    WormDotsIndicator wormDotsIndicator;

    TextView post_nickName;
    TextView post_content;
    TextView LikeText;

    private Button Comment_btn;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String myUidServer = user.getUid();

    private CheckBox bookmark_button;
    private CheckBox Like_button;
    private FirebaseDatabase firebaseDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expand__image_view);
        overridePendingTransition(R.anim.fade_in, R.anim.none_animation);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        activity = this;

        Intent intent = getIntent();
        friendChecked = intent.getStringExtra("friend");
        Log.d("dsd", "friendCheck: " + friendChecked);
        likeChecked = intent.getStringExtra("postLike");
        bookmarkChecked = intent.getStringExtra("bookmark");
        postID = intent.getStringExtra("postID");
        Log.d("DSD", "onCreate: postID값" + postID);
        uid = intent.getStringExtra("uid");
        nickName = intent.getStringExtra("nickName");
        date = intent.getStringExtra("date");
        content = intent.getStringExtra("content");
        imageUrl1 = intent.getStringExtra("imageUrl1");
        imageUrl2 = intent.getStringExtra("imageUrl2");
        imageUrl3 = intent.getStringExtra("imageUrl3");
        imageUrl4 = intent.getStringExtra("imageUrl4");
        imageUrl5 = intent.getStringExtra("imageUrl5");
        favoriteCount = intent.getIntExtra("favoriteCount", 0);

        Log.d("dsd", "onCreate: favori" + favoriteCount);
        Category = intent.getStringExtra("category");


        Comment_btn = (Button) findViewById(R.id.Comment_btn);

        Comment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CommentActivity.class);

                intent.putExtra("postID", postID);
                intent.putExtra("nickName", nickName);
                intent.putExtra("uid", uid);
                intent.putExtra("content", content);
                intent.putExtra("image", imageUrl1);
                intent.putExtra("image2", imageUrl2);
                intent.putExtra("image3", imageUrl3);
                intent.putExtra("image4", imageUrl4);
                intent.putExtra("image5", imageUrl5);

                getApplicationContext().startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK));

            }
        });
        final ImageView profileImage = (ImageView) findViewById(R.id.Profile_image);
        final String[] profileImg = new String[1];
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(uid);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        if (document.exists()) {
                            profileImg[0] = document.getData().get("profileImg").toString();
                            if (profileImg[0].length() > 0) {
                                Glide.with(getApplicationContext()).load(profileImg[0]).centerCrop().override(500).into(profileImage);
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

        wormDotsIndicator = (WormDotsIndicator) findViewById(R.id.worm_dots_indicator);
        wormDotsIndicator.setVisibility(View.GONE);
        viewPager = (ViewPager) findViewById(R.id.main_image);
        post_content = findViewById(R.id.main_textView);

        if (!imageUrl1.equals("https://firebasestorage.googleapis.com/v0/b/petdiary-794c6.appspot.com/o/images%2Fempty.png?alt=media&token=c41b1cc0-d610-4964-b00c-2638d4bfd8bd")) {
            Log.e("###111", viewPager.getCurrentItem() + " ");
            viewPageAdapter = new ViewPageAdapterDetail(true, imageUrl1, imageUrl2, imageUrl3, imageUrl4, imageUrl5, getApplicationContext());
            viewPager.setAdapter(viewPageAdapter);

            viewPager.setVisibility(View.VISIBLE);
            wormDotsIndicator.setViewPager(viewPager);
            wormDotsIndicator.setVisibility(View.VISIBLE);

        } else {
            viewPager.setVisibility(View.GONE);
            RelativeLayout.LayoutParams param2 = new RelativeLayout.LayoutParams
                    (RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            param2.topMargin = 200;
            param2.bottomMargin = 200;
            param2.leftMargin = 10;
            param2.rightMargin = 10;
            post_content.setLayoutParams(param2);

            ScrollView scrollView = findViewById(R.id.scroll);

            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            scrollView.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            scrollView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;

        }


        post_nickName = findViewById(R.id.Profile_Name);
        LikeText = findViewById(R.id.Like_button_text_Count);

        LikeText.setText(String.valueOf(favoriteCount));


        post_nickName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), UserPageActivity.class);
                intent.putExtra("userID", uid);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), UserPageActivity.class);
                intent.putExtra("userID", uid);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        post_nickName.setText(nickName);
        post_content.setText(content);
        LikeText.setText(Integer.toString(favoriteCount));
        if (content.length() == 0) {
            post_content.setVisibility(View.INVISIBLE);
        }

        findViewById(R.id.onPopupButton).setOnClickListener(new View.OnClickListener() {
            //내 uid
            String myuid = myUidServer;
            //게시글 uid
            String content_uid = uid;

            @Override

            public void onClick(final View view) {

                if (myuid.equals(content_uid)) {
                    CharSequence info[] = new CharSequence[]{"Edit", "Delete", "Share"};

                    final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                    builder.setTitle("");

                    builder.setItems(info, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:

                                    Intent intent = new Intent(getApplicationContext(), ContentEditActivity.class);

                                    intent.putExtra("imageUrl1", imageUrl1);
                                    intent.putExtra("imageUrl2", imageUrl2);
                                    intent.putExtra("imageUrl3", imageUrl3);
                                    intent.putExtra("imageUrl4", imageUrl4);
                                    intent.putExtra("imageUrl5", imageUrl5);
                                    intent.putExtra("postID", postID);
                                    intent.putExtra("content", content);
                                    intent.putExtra("category", Category);

                                    //getApplicationContext().startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK));
                                    startActivityForResult(intent, 0);

                                    ContentEditActivity.setListener(calbacklistener);

                                    // 내정보
                                    Toast.makeText(view.getContext(), "Edit", Toast.LENGTH_SHORT).show();
                                    break;
                                case 1:
                                    EXpandPostDelete(view);
                                    // 로그아웃
                                    break;
                                case 2:
                                    /*딥링크 공유 하는 코드*/
                                    FirebaseDynamicLinks.getInstance().createDynamicLink()
                                            .setLink(Uri.parse("https://www.jun.com/" + postID))
                                            .setDomainUriPrefix("https://MyPetStory.page.link")
                                            .setAndroidParameters(
                                                    new DynamicLink.AndroidParameters.Builder("com.example.petdiary")
                                                            .setFallbackUrl(Uri.parse("https://daengjundl.tistory.com/12"))
                                                            .build())
                                            .setGoogleAnalyticsParameters(
                                                    new DynamicLink.GoogleAnalyticsParameters.Builder()
                                                            .setSource("orkut")
                                                            .setMedium("social")
                                                            .setCampaign("example-promo")
                                                            .build())
                                            .setItunesConnectAnalyticsParameters(
                                                    new DynamicLink.ItunesConnectAnalyticsParameters.Builder()
                                                            .setProviderToken("123456")
                                                            .setCampaignToken("example-promo")
                                                            .build())
                                            .setSocialMetaTagParameters(
                                                    new DynamicLink.SocialMetaTagParameters.Builder()
                                                            .setTitle(date)
                                                            .setDescription(content)
                                                            .setImageUrl(Uri.parse(imageUrl1))
                                                            .build())

                                            .buildShortDynamicLink()
                                            .addOnCompleteListener(activity, new OnCompleteListener<ShortDynamicLink>() {
                                                @Override
                                                public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                                    if (task.isSuccessful()) {
                                                        Uri shortLink = task.getResult().getShortLink();
                                                        try {
                                                            Intent sendIntent = new Intent();
                                                            sendIntent.setAction(Intent.ACTION_SEND);
                                                            sendIntent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                                                            sendIntent.setType("text/plain");
                                                            startActivity(Intent.createChooser(sendIntent, "Share"));
                                                        } catch (ActivityNotFoundException ignored) {
                                                        }
                                                    } else {
                                                    }
                                                }
                                            });
                                    break;
                            }
                            dialog.dismiss();
                        }
                    });

                    builder.show();

                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                    if (friendChecked.equals("checked")) {
                        final CharSequence info[] = new CharSequence[]{"친구삭제", "신고하기", "사용자 차단", "게시물 공유"};
                        builder.setTitle("");
                        builder.setItems(info, new DialogInterface.OnClickListener() {
                            @Override

                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        ExpandFriendsDelete(view);
                                        break;
                                    case 1:
                                        // 내정보

                                        Intent intent = new Intent(getApplicationContext(), SettingCustomerActivity.class);

                                        intent.putExtra("email", user.getEmail());
                                        intent.putExtra("declaration", "신고");

                                        getApplicationContext().startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK));

//                                        Toast.makeText(view.getContext(), "신고하기", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 2:
                                        // 로그아웃
                                        BlockFriendInfo blockFriendInfo = new BlockFriendInfo();
                                        blockFriendInfo.setFriendUid(uid);
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                                        db.collection("blockFriends/" + user.getUid() + "/friends").document(uid).set(blockFriendInfo)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        DatabaseReference friend = firebaseDatabase.getReference("friend").child(user.getUid() + "/" + uid);
                                                        FriendInfo friendInfo = new FriendInfo();
                                                        friend.setValue(friendInfo);
                                                        calbacklistener.refresh(true);
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                    }
                                                });
                                        Toast.makeText(view.getContext(), "사용자 차단", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 3:
                                        FirebaseDynamicLinks.getInstance().createDynamicLink()
                                                .setLink(Uri.parse("https://www.jun.com/" + postID))
                                                .setDomainUriPrefix("https://MyPetStory.page.link")
                                                .setAndroidParameters(
                                                        new DynamicLink.AndroidParameters.Builder("com.example.petdiary")
                                                                .setFallbackUrl(Uri.parse("https://daengjundl.tistory.com/12"))
                                                                .build())
                                                .setGoogleAnalyticsParameters(
                                                        new DynamicLink.GoogleAnalyticsParameters.Builder()
                                                                .setSource("orkut")
                                                                .setMedium("social")
                                                                .setCampaign("example-promo")
                                                                .build())
                                                .setItunesConnectAnalyticsParameters(
                                                        new DynamicLink.ItunesConnectAnalyticsParameters.Builder()
                                                                .setProviderToken("123456")
                                                                .setCampaignToken("example-promo")
                                                                .build())
                                                .setSocialMetaTagParameters(
                                                        new DynamicLink.SocialMetaTagParameters.Builder()
                                                                .setTitle(date)
                                                                .setDescription(content)
                                                                .setImageUrl(Uri.parse(imageUrl1))
                                                                .build())

                                                .buildShortDynamicLink()
                                                .addOnCompleteListener(activity, new OnCompleteListener<ShortDynamicLink>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                                        if (task.isSuccessful()) {
                                                            Uri shortLink = task.getResult().getShortLink();
                                                            try {
                                                                Intent sendIntent = new Intent();
                                                                sendIntent.setAction(Intent.ACTION_SEND);
                                                                sendIntent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                                                                sendIntent.setType("text/plain");
                                                                startActivity(Intent.createChooser(sendIntent, "Share"));
                                                            } catch (ActivityNotFoundException ignored) {
                                                            }
                                                        } else {
                                                        }
                                                    }
                                                });
                                        break;
                                }
                                dialog.dismiss();
                            }
                        });
                    } else {
                        final CharSequence info[] = new CharSequence[]{"친구추가", "신고하기", "사용자 차단", "게시물 공유"};
                        builder.setTitle("");
                        builder.setItems(info, new DialogInterface.OnClickListener() {
                            @Override

                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        Toast.makeText(view.getContext(), "addFriend", Toast.LENGTH_SHORT).show();
                                        friendChecked = "checked";
                                        firebaseDatabase = FirebaseDatabase.getInstance();
                                        DatabaseReference friend = firebaseDatabase.getReference("friend").child(user.getUid() + "/" + uid);
//                                        FriendInfo friendInfo = new FriendInfo();
//                                        friendInfo.setFriendUid(uid);
//                                        friend.setValue(friendInfo);

                                        Hashtable<String, String> numbers = new Hashtable<String, String>();
                                        numbers.put("message", "없음");
                                        friend.setValue(numbers);

                                        Toast.makeText(getApplicationContext(), "친구를 추가하였습니다.", Toast.LENGTH_SHORT).show();
                                        calbacklistener.refresh(true);
                                        break;
                                    case 1:

                                        Log.d("Email", "dangjun my_Email : " + user.getEmail());

                                        Intent intent = new Intent(getApplicationContext(), SettingCustomerActivity.class);

                                        intent.putExtra("email", user.getEmail());
                                        intent.putExtra("declaration", "신고");

                                        getApplicationContext().startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK));

//                                        Toast.makeText(view.getContext(), "신고하기", Toast.LENGTH_SHORT).show();

                                        break;
                                    case 2:
                                        BlockFriendInfo blockFriendInfo = new BlockFriendInfo();
                                        blockFriendInfo.setFriendUid(uid);
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                                        db.collection("blockFriends/" + user.getUid() + "/friends").document(uid).set(blockFriendInfo)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        DatabaseReference friend = firebaseDatabase.getReference("friend").child(user.getUid() + "/" + uid);
                                                        FriendInfo friendInfo = new FriendInfo();
                                                        friend.setValue(friendInfo);
                                                        Log.d("ㅇㄴㅇㄴ", "onSuccess: 사용자 차단");
                                                        calbacklistener.refresh(true);
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                    }
                                                });
                                        Toast.makeText(view.getContext(), "사용자 차단", Toast.LENGTH_SHORT).show();
                                        break;

                                    case 3:
                                        FirebaseDynamicLinks.getInstance().createDynamicLink()
                                                .setLink(Uri.parse("https://www.jun.com/" + postID))
                                                .setDomainUriPrefix("https://MyPetStory.page.link")
                                                .setAndroidParameters(
                                                        new DynamicLink.AndroidParameters.Builder("com.example.petdiary")
                                                                .setFallbackUrl(Uri.parse("https://daengjundl.tistory.com/12"))
                                                                .build())
                                                .setGoogleAnalyticsParameters(
                                                        new DynamicLink.GoogleAnalyticsParameters.Builder()
                                                                .setSource("orkut")
                                                                .setMedium("social")
                                                                .setCampaign("example-promo")
                                                                .build())
                                                .setItunesConnectAnalyticsParameters(
                                                        new DynamicLink.ItunesConnectAnalyticsParameters.Builder()
                                                                .setProviderToken("123456")
                                                                .setCampaignToken("example-promo")
                                                                .build())
                                                .setSocialMetaTagParameters(
                                                        new DynamicLink.SocialMetaTagParameters.Builder()
                                                                .setTitle(date)
                                                                .setDescription(content)
                                                                .setImageUrl(Uri.parse(imageUrl1))
                                                                .build())

                                                .buildShortDynamicLink()
                                                .addOnCompleteListener(activity, new OnCompleteListener<ShortDynamicLink>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                                        if (task.isSuccessful()) {
                                                            Uri shortLink = task.getResult().getShortLink();
                                                            try {
                                                                Intent sendIntent = new Intent();
                                                                sendIntent.setAction(Intent.ACTION_SEND);
                                                                sendIntent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                                                                sendIntent.setType("text/plain");
                                                                startActivity(Intent.createChooser(sendIntent, "Share"));
                                                            } catch (ActivityNotFoundException ignored) {
                                                            }
                                                        } else {
                                                        }
                                                    }
                                                });
                                        break;
                                }
                                dialog.dismiss();
                            }
                        });
                    }
                    builder.show();
                }
            }
        });

        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        bookmark_button = (CheckBox) findViewById(R.id.bookmark_button);
        if (bookmarkChecked.equals("checked")) {
            bookmark_button.setChecked(true);
        }
        firebaseDatabase = FirebaseDatabase.getInstance();
        bookmark_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                DatabaseReference bookmark = firebaseDatabase.getReference("bookmark").child(user.getUid() + "/" + postID);
                BookmarkInfo bookmarkInfo = new BookmarkInfo();
                if (b) {
                    bookmarkInfo.setPostID(postID);
                    db.collection("user-checked/" + user.getUid() + "/bookmark").document(postID).set(bookmarkInfo)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    calbacklistener.refresh(true);

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                } else {
                    db.collection("user-checked/" + user.getUid() + "/bookmark").document(postID)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    calbacklistener.refresh(true);
                                    Log.d("CustomAdapter", "DocumentSnapshot successfully deleted!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("CustomAdapter", "Error deleting document", e);
                                }
                            });
                }
            }
        });

        Like_button = (CheckBox) findViewById(R.id.Like_button);
        if (likeChecked.equals("checked")) {

            Like_button.setChecked(true);
        }
        Like_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PostLikeInfo postLikeInfo = new PostLikeInfo();
                if (b) {
                    postLikeInfo.setPostID(postID);
                    db.collection("user-checked/" + user.getUid() + "/like").document(postID).set(postLikeInfo)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    DocumentReference washingtonRef = db.collection("post").document(postID);


                                    int favoritePlus = favoriteCount;
                                    Log.d("ddsdsds", "onSuccess: favoriteCount" + favoriteCount);
                                    Log.d("121", "onSuccess: 값좀알자!!!!" + "//" + favoritePlus);

                                    favoritePlus = favoritePlus + 1;
                                    favoriteCount = favoritePlus;
                                    LikeText.setText(String.valueOf(favoriteCount));

// Set the "isCapital" field of the city 'DC'
                                    washingtonRef
                                            .update("favoriteCount", favoriteCount)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    calbacklistener.refresh(true);
                                                    Log.d("성공", "DocumentSnapshot successfully updated!");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w("실패", "Error updating document", e);
                                                }
                                            });

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                } else {
                    db.collection("user-checked/" + user.getUid() + "/like").document(postID)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    // if(favoriteCount!=0) {
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    DocumentReference washingtonRef = db.collection("post").document(postID);

                                    int favoriteMinus = favoriteCount;

                                    favoriteMinus = favoriteMinus - 1;
                                    favoriteCount = favoriteMinus;
                                    LikeText.setText(String.valueOf(favoriteCount));

// Set the "isCapital" field of the city 'DC'
                                    washingtonRef
                                            .update("favoriteCount", favoriteCount)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    calbacklistener.refresh(true);
                                                    Log.d("성공", "DocumentSnapshot successfully updated!");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w("실패", "Error updating document", e);
                                                }
                                            });
                                    //    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    content = data.getStringExtra("content");
                    imageUrl1 = data.getStringExtra("imageUrl1");
                    imageUrl2 = data.getStringExtra("imageUrl2");
                    imageUrl3 = data.getStringExtra("imageUrl3");
                    imageUrl4 = data.getStringExtra("imageUrl4");
                    imageUrl5 = data.getStringExtra("imageUrl5");

                    post_content = findViewById(R.id.main_textView);
                    post_content.setText(content);

                    viewPager = (ViewPager) findViewById(R.id.main_image);
                    viewPageAdapter = new ViewPageAdapterDetail(true, imageUrl1, imageUrl2, imageUrl3, imageUrl4, imageUrl5, getApplicationContext());
                    viewPager.setAdapter(viewPageAdapter);

                    viewPageAdapter.notifyDataSetChanged();

                    wormDotsIndicator = (WormDotsIndicator) findViewById(R.id.worm_dots_indicator);
                    wormDotsIndicator.setViewPager(viewPager);
                } else {
                }
                break;
        }
    }


    //친구삭제
    public void ExpandFriendsDelete(final View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());


        builder.setTitle("정말 삭제하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        //Toast.makeText(view.getContext(), "deleteFriend", Toast.LENGTH_SHORT).show();
                        friendChecked = "unchecked";
                        firebaseDatabase = FirebaseDatabase.getInstance();
                        DatabaseReference friend = firebaseDatabase.getReference("friend").child(user.getUid() + "/" + uid);
                        FriendInfo friendInfo = new FriendInfo();
                        friend.setValue(friendInfo);
                        Toast.makeText(getApplicationContext(), "친구를 삭제하였습니다.", Toast.LENGTH_SHORT).show();
                        calbacklistener.refresh(true);
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    //게시글 삭제
    public void EXpandPostDelete(final View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());


        builder.setTitle("정말 삭제하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        final StorageReference storageRef = storage.getReference();
// Create a reference to the file to delete

                        String[] splitText = postID.split("_");

                        String image[] = new String[5];

                        image[0] = imageUrl1;
                        image[1] = imageUrl1;
                        image[2] = imageUrl1;
                        image[3] = imageUrl1;
                        image[4] = imageUrl1;

                        for (int i = 0; i < 5; i++) {

                            if (image[i] != null) {
                                StorageReference desertRef = storageRef.child("images/" + splitText[0] + "_" + splitText[1] + "_postImg_" + i);

// Delete the file
                                desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // File deleted successfully

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Uh-oh, an error occurred!
                                    }
                                });

                            }
                        }
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("post").document(postID)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {

                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        calbacklistener.refresh(true);
                                        onBackPressed();

                                    }
//                                                    finish();
//                                                    overridePendingTransition(0, 0);
//                                                    startActivity(getIntent());
//                                                    overridePendingTransition(0, 0);

//                                                arrayList.remove(position);
//                                                notifyItemRemoved(position);
//                                                //this line below gives you the animation and also updates the
//                                                //list items after the deleted item
//                                                notifyItemRangeChanged(position, getItemCount());
                                    // }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });

                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void refresh(boolean check) {

    }


    public static void setListener(callBackListener listener) {

        calbacklistener = listener;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("dsd", "onResume: " + imageUrl1);
        Log.d("dsd", "onResume: " + viewPageAdapter);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        Log.d("ㅇㄴㅇ", "onUserInteraction: 왜왜왜왜");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.none_animation, R.anim.fade_out);
    }
}