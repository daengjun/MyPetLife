package com.example.petdiary.adapter;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.petdiary.activity.SettingCustomerActivity;
import com.example.petdiary.activity.UserPageActivity;
import com.example.petdiary.util.callBackListener;
import com.example.petdiary.info.BlockFriendInfo;
import com.example.petdiary.info.BookmarkInfo;
import com.example.petdiary.activity.CommentActivity;
import com.example.petdiary.data.Data;
import com.example.petdiary.info.FriendInfo;
import com.example.petdiary.info.PostLikeInfo;
import com.example.petdiary.R;
import com.example.petdiary.activity.ContentEditActivity;
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

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> implements callBackListener {

    private ArrayList<Data> arrayList;
    private Context context;
    private Button Comment_btn;
    private Button onPopupButton;
    Activity activity;
    ViewPageAdapterDetail viewPageAdapter;
    ViewPager viewPager;
    WormDotsIndicator wormDotsIndicator;
    callBackListener listener;
    private FirebaseDatabase firebaseDatabase;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid = user.getUid();


    private boolean checkFriend = true;

    public CustomAdapter(ArrayList<Data> arrayList, Context context, callBackListener listener, Activity activity) {
        this.arrayList = arrayList;
        this.context = context;
        this.listener = listener;
        this.activity = activity;
    }

    public void setListener(callBackListener listener) {
        this.listener = listener;
    }

    public void clear() {
        arrayList.clear();
    }

    @NonNull
    @Override
    //실제 리스트뷰가 어댑터에 연결된 다음에 뷰 홀더를 최초로 만들어낸다.
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        CustomViewHolder holder = new CustomViewHolder(view);

        return holder;
    }

    // 최근 수정
    @Override
    public void onBindViewHolder(@NonNull final CustomViewHolder holder, @SuppressLint("RecyclerView") final int position) {


        String empty_Url = "https://firebasestorage.googleapis.com/v0/b/petdiary-794c6.appspot.com/o/images%2Fempty.png?alt=media&token=c41b1cc0-d610-4964-b00c-2638d4bfd8bd";
        String first_imageData = arrayList.get(position).getImageUrl1();
        View first_border = (View) holder.itemView.findViewById(R.id.first_Square);
        View second_border = (View) holder.itemView.findViewById(R.id.second_Square);
        View hidden_border = (View) holder.itemView.findViewById(R.id.hidden_Square);
        wormDotsIndicator = (WormDotsIndicator) holder.itemView.findViewById(R.id.worm_dots_indicator);

        viewPager = (ViewPager) holder.itemView.findViewById(R.id.main_image);

        viewPageAdapter = new ViewPageAdapterDetail(true, arrayList.get(position).getImageUrl1(), arrayList.get(position).getImageUrl2(),
                arrayList.get(position).getImageUrl3(), arrayList.get(position).getImageUrl4(), arrayList.get(position).getImageUrl5(), context);


        viewPager.setAdapter(viewPageAdapter);
        holder.content.setText(arrayList.get(position).getContent());
        holder.nickName.setText(arrayList.get(position).getNickName());
        holder.LikeText.setText(String.valueOf(arrayList.get(position).getFavoriteCount()));

        if (first_imageData.equals(empty_Url)) {
            viewPager.setVisibility(View.GONE);
            wormDotsIndicator.setViewPager(viewPager);
            wormDotsIndicator.setVisibility(View.INVISIBLE);

            first_border.setVisibility(View.GONE);
            second_border.setVisibility(View.GONE);
            hidden_border.setVisibility(View.VISIBLE);

        } else {

            viewPager.setVisibility(View.VISIBLE);
            wormDotsIndicator.setViewPager(viewPager);
            wormDotsIndicator.setVisibility(View.VISIBLE);

            first_border.setVisibility(View.VISIBLE);
            second_border.setVisibility(View.VISIBLE);
            hidden_border.setVisibility(View.GONE);

        }

        holder.nickName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UserPageActivity.class);
                intent.putExtra("userID", arrayList.get(position).getUid());
                UserPageActivity.setlistener(listener);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        holder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UserPageActivity.class);
                intent.putExtra("userID", arrayList.get(position).getUid().toString());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        final String[] profileImg = new String[1];
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(arrayList.get(position).getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        if (document.exists()) {
                            profileImg[0] = document.getData().get("profileImg").toString();
                            if (profileImg[0].length() > 0) {
                                Glide.with(context).load(profileImg[0]).centerCrop().override(500).into(holder.profileImage);
                            } else {
                                holder.profileImage.setImageResource(R.drawable.icon_person);
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

        if (arrayList.get(position).getBookmark()) {
            holder.bookmark_button.setChecked(true);
        } else {
            holder.bookmark_button.setChecked(false);
        }
        if (arrayList.get(position).getLike()) {
            holder.Like_button.setChecked(true);
        } else {
            holder.Like_button.setChecked(false);
        }

        Comment_btn = (Button) holder.itemView.findViewById(R.id.Comment_btn);
        Comment_btn.findViewById(R.id.Comment_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent intent = new Intent(context, CommentActivity.class);

                intent.putExtra("postID", arrayList.get(position).getPostID());
                intent.putExtra("image", arrayList.get(position).getImageUrl1());
                intent.putExtra("image2", arrayList.get(position).getImageUrl2());
                intent.putExtra("image3", arrayList.get(position).getImageUrl3());
                intent.putExtra("image4", arrayList.get(position).getImageUrl4());
                intent.putExtra("image5", arrayList.get(position).getImageUrl5());

                intent.putExtra("nickName", arrayList.get(position).getNickName());
                intent.putExtra("uid", arrayList.get(position).getUid());
                intent.putExtra("content", arrayList.get(position).getContent());

                context.startActivity(intent);
            }
        });

        onPopupButton = (Button) holder.itemView.findViewById(R.id.onPopupButton);
        onPopupButton.setOnClickListener(new View.OnClickListener() {

            String uids = uid;
            //내 uid
            String uids2 = arrayList.get(position).getUid();

            //게시물 정보 uid
            @Override
            public void onClick(final View view) {
                if (uids.equals(uids2)) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                    CharSequence info[] = new CharSequence[]{"Edit", "Delete", "Share"};
                    builder.setTitle("");
                    builder.setItems(info, new DialogInterface.OnClickListener() {
                        @Override

                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    // 내정보
                                    Intent intent = new Intent(context, ContentEditActivity.class);

                                    //intent.putExtra("nickName", arrayList.get(position).getNickName());
                                    // intent.putExtra("uid", arrayList.get(position).getUid());
                                    // intent.putExtra("content", arrayList.get(position).getContent());
                                    intent.putExtra("imageUrl1", arrayList.get(position).getImageUrl1());
                                    intent.putExtra("imageUrl2", arrayList.get(position).getImageUrl2());
                                    intent.putExtra("imageUrl3", arrayList.get(position).getImageUrl3());
                                    intent.putExtra("imageUrl4", arrayList.get(position).getImageUrl4());
                                    intent.putExtra("imageUrl5", arrayList.get(position).getImageUrl5());
                                    intent.putExtra("category", arrayList.get(position).getCategory());
                                    intent.putExtra("content", arrayList.get(position).getContent());
                                    intent.putExtra("postID", arrayList.get(position).getPostID());

                                    context.startActivity(intent);
                                    ContentEditActivity.setListener(listener);

                                    Toast.makeText(view.getContext(), "Edit", Toast.LENGTH_SHORT).show();
                                    break;
                                case 1:
                                    PostDelete(view, position);
                                    break;
                                case 2:


                                    FirebaseDynamicLinks.getInstance().createDynamicLink()
                                            .setLink(Uri.parse("https://www.jun.com/" + arrayList.get(position).getPostID()))
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
                                                            .setTitle(arrayList.get(position).getDate())
                                                            .setDescription(arrayList.get(position).getContent())
                                                            .setImageUrl(Uri.parse(arrayList.get(position).getImageUrl1()))
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
                                                            context.startActivity(Intent.createChooser(sendIntent, "Share"));
                                                        } catch (ActivityNotFoundException ignored) {
                                                        }
                                                    } else {
//                            Uri shortLink = task.getResult().getShortLink();

                                                        Log.d("dsd", "오류");
//                            Intent sendIntent = new Intent();
//                            sendIntent.setAction(Intent.ACTION_SEND);
//                            sendIntent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
//                            sendIntent.setType("text/plain");
//                            context.startActivity(Intent.createChooser(sendIntent, "Share"));
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
                    firebaseDatabase = FirebaseDatabase.getInstance();
                    final CharSequence[][] info = new CharSequence[1][1];
                    if (checkFriend) {
                        info[0] = new CharSequence[]{"친구삭제", "신고하기", "사용자 차단"};
                    } else {
                        info[0] = new CharSequence[]{"친구추가", "신고하기", "사용자 차단"};
                    }
                    builder.setTitle("");
                    builder.setItems(info[0], new DialogInterface.OnClickListener() {
                        @Override

                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    if (checkFriend) {
                                        FriendsDelete(view, position);
                                    } else {
                                        DatabaseReference friend = firebaseDatabase.getReference("friend").child(user.getUid() + "/" + arrayList.get(position).getUid());
                                        Hashtable<String, String> numbers = new Hashtable<String, String>();
                                        numbers.put("message", "없음");
                                        friend.setValue(numbers);
                                        checkFriend = true;
                                        Toast.makeText(context, "친구를 추가하였습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case 1:
                                    Intent intent = new Intent(context, SettingCustomerActivity.class);

                                    intent.putExtra("email", user.getEmail());
                                    intent.putExtra("declaration", "신고");

                                    context.startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK));                                    break;
                                case 2:
                                    BlockFriendInfo blockFriendInfo = new BlockFriendInfo();
                                    blockFriendInfo.setFriendUid(arrayList.get(position).getUid());
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    db.collection("blockFriends/" + user.getUid() + "/friends").document(arrayList.get(position).getUid()).set(blockFriendInfo)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    DatabaseReference friend = firebaseDatabase.getReference("friend").child(user.getUid() + "/" + arrayList.get(position).getUid());
                                                    FriendInfo friendInfo = new FriendInfo();
                                                    friend.setValue(friendInfo);
                                                    checkFriend = false;
                                                    arrayList.remove(position);
                                                    notifyItemRemoved(position);
                                                    notifyItemRangeChanged(position, getItemCount());
                                                    listener.refresh(false);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                }
                                            });
                                    Toast.makeText(view.getContext(), "사용자를 차단하였습니다.", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                            dialog.dismiss();
                        }
                    });

                    builder.show();

                }
            }

        });

        wormDotsIndicator = (WormDotsIndicator) holder.itemView.findViewById(R.id.worm_dots_indicator);
        wormDotsIndicator.setViewPager(viewPager);
    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }

    @Override
    public void refresh(boolean check) {
    }


    public class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView content;
        TextView nickName;
        TextView LikeText;
        ImageView profileImage;
        CheckBox bookmark_button;
        CheckBox Like_button;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.content = itemView.findViewById(R.id.main_textView);
            this.nickName = itemView.findViewById(R.id.Profile_Name);
            this.profileImage = itemView.findViewById(R.id.Profile_image);
            this.bookmark_button = itemView.findViewById(R.id.bookmark_button);
            this.Like_button = itemView.findViewById(R.id.Like_button);
            this.LikeText = itemView.findViewById(R.id.Like_button_text_Count);


            itemView.findViewById(R.id.bookmark_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    BookmarkInfo bookmarkInfo = new BookmarkInfo();
                    if (((CheckBox) view).isChecked()) {
                        bookmarkInfo.setPostID(arrayList.get(pos).getPostID());
                        db.collection("user-checked/" + user.getUid() + "/bookmark").document(arrayList.get(pos).getPostID()).set(bookmarkInfo)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        listener.refresh(false);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                    } else {
                        db.collection("user-checked/" + user.getUid() + "/bookmark").document(arrayList.get(pos).getPostID())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
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

            itemView.findViewById(R.id.Like_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int pos = getAdapterPosition();
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    PostLikeInfo postLikeInfo = new PostLikeInfo();
                    if (((CheckBox) view).isChecked()) {
                        postLikeInfo.setPostID(arrayList.get(pos).getPostID());
                        db.collection("user-checked/" + user.getUid() + "/like").document(arrayList.get(pos).getPostID()).set(postLikeInfo)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        DocumentReference washingtonRef = db.collection("post").document(arrayList.get(pos).getPostID());


                                        int favoritePlus = arrayList.get(pos).getFavoriteCount();

                                        favoritePlus = favoritePlus + 1;

                                        arrayList.get(pos).setFavoriteCount(favoritePlus);


                                        LikeText.setText(String.valueOf(favoritePlus));

// Set the "isCapital" field of the city 'DC'
                                        washingtonRef
                                                .update("favoriteCount", favoritePlus)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        listener.refresh(false);
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
                        db.collection("user-checked/" + user.getUid() + "/like").document(arrayList.get(pos).getPostID())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        //    if(arrayList.get(pos).getFavoriteCount()!=0) {
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        DocumentReference washingtonRef = db.collection("post").document(arrayList.get(pos).getPostID());


                                        int favoriteMinos = arrayList.get(pos).getFavoriteCount();

                                        favoriteMinos = favoriteMinos - 1;


// Set the "isCapital" field of the city 'DC'
                                        arrayList.get(pos).setFavoriteCount(favoriteMinos);
                                        LikeText.setText(String.valueOf(favoriteMinos));

                                        washingtonRef
                                                .update("favoriteCount", favoriteMinos)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        listener.refresh(false);
                                                        Log.d("성공", "DocumentSnapshot successfully updated!");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w("실패", "Error updating document", e);
                                                    }
                                                });


                                        Log.d("CustomAdapter", "DocumentSnapshot successfully deleted!");
                                        //      }
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
        }
    }


    //친구 삭제
    public void FriendsDelete(final View view, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

        builder.setTitle("정말 삭제하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        DatabaseReference friend = firebaseDatabase.getReference("friend").child(user.getUid() + "/" + arrayList.get(position).getUid());
                        FriendInfo friendInfo = new FriendInfo();
                        friend.setValue(friendInfo);
                        checkFriend = false;
                        arrayList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, getItemCount());
                        Toast.makeText(context, "친구를 삭제하였습니다.", Toast.LENGTH_SHORT).show();
                        listener.refresh(false);
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
    public void PostDelete(final View view, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());


        builder.setTitle("정말 삭제하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        final StorageReference storageRef = storage.getReference();

                        String[] splitText = arrayList.get(position).getPostID().split("_");

                        Log.d("splitText", "onClick: splitText의값은" + splitText[0] + "_" + splitText[1]);


                        String image[] = new String[5];

                        image[0] = arrayList.get(position).getImageUrl1();
                        image[1] = arrayList.get(position).getImageUrl1();
                        image[2] = arrayList.get(position).getImageUrl1();
                        image[3] = arrayList.get(position).getImageUrl1();
                        image[4] = arrayList.get(position).getImageUrl1();

                        for (int i = 0; i < 5; i++) {

                            if (image[i] != null) {
                                StorageReference desertRef = storageRef.child("images/" + splitText[0] + "_" + splitText[1] + "_postImg_" + i);

                                Log.d("날짜정보", "onClick: 날짜 정보" + arrayList.get(position).getDate());
                                Log.d("날짜정보", "onClick: 날짜 정보" + arrayList.get(position).getDate() + "_postImg_0");

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

                        // 로그아웃
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("post").document(arrayList.get(position).getPostID())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {

                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("@@@", "DocumentSnapshot successfully deleted!");

                                        arrayList.remove(position);
                                        notifyItemRemoved(position);
                                        //this line below gives you the animation and also updates the
                                        //list items after the deleted item
                                        notifyItemRangeChanged(position, getItemCount());

                                        listener.refresh(false);
                                        Log.d("확인", "리스너 작동");


                                    }
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
}

