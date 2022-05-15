package com.example.petdiary.activity;


import android.content.Context;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.petdiary.data.Data;
import com.example.petdiary.R;
import com.example.petdiary.util.callBackListener;
import com.example.petdiary.util.Expand_ImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Kon_MypageAdapter extends RecyclerView.Adapter<Kon_MypageAdapter.MypageViewHolder> implements callBackListener {

    private ArrayList<Data> arrayList ;
    private ArrayList<Data> arrayList2;
    private Context context;
    private LayoutInflater inf;
    private int squareSize;
    private int columnNum;
    callBackListener calbacklistener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabase;

    public Kon_MypageAdapter(ArrayList<Data> arrayList, int columnNum, Context context , callBackListener calbacklistener) {
        this.arrayList = arrayList;
        this.arrayList2 = arrayList;
        this.context = context;
        this.columnNum = columnNum;
        this.calbacklistener = calbacklistener;
    }

    public void setArray(ArrayList<Data> arrayList)
    {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MypageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.kon_mypage_item, parent, false);
        MypageViewHolder holder = new MypageViewHolder(view);

        int layout_width = parent.getMeasuredWidth();
        //int layout_height = parent.getMeasuredHeight();
        int itemSize = layout_width / columnNum;
        squareSize = itemSize - (itemSize / 32);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MypageViewHolder holder, int position) {

        holder.itemView.getLayoutParams().width = squareSize;  // 아이템 뷰의 세로 길이를 구한 길이로 변경
        holder.itemView.getLayoutParams().height = squareSize;  // 아이템 뷰의 세로 길이를 구한 길이로 변경
        holder.itemView.requestLayout(); // 변경 사항 적용

        String url = arrayList.get(position).getImageUrl1();


        if(!url.equals("https://firebasestorage.googleapis.com/v0/b/petdiary-794c6.appspot.com/o/images%2Fempty.png?alt=media&token=c41b1cc0-d610-4964-b00c-2638d4bfd8bd")) {
            holder.cardView.setVisibility(View.VISIBLE);
            holder.textView.setVisibility(View.GONE);
            Glide.with(context).load(url).centerCrop().override(500).into(holder.postImage);

        }
        else{
            holder.cardView.setVisibility(View.GONE);
            holder.textView.setVisibility(View.VISIBLE);
            holder.textView.setText(arrayList.get(position).getContent());

        }

    }

    @Override
    public int getItemCount() {
        // 삼항 연산자
        return arrayList.size();
    }

    @Override
    public void refresh(boolean check) {


    }

    public class MypageViewHolder extends RecyclerView.ViewHolder {
        ImageView postImage;
        TextView textView;
        CardView cardView;

        public MypageViewHolder(@NonNull final View itemView) {
            super(itemView);
            this.postImage = itemView.findViewById(R.id.mypage_image);
            this.textView = itemView.findViewById(R.id.my_textview);
            this.cardView = itemView.findViewById(R.id.my_cardView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(arrayList.size() > 0) {
                        goPostData(arrayList.get(getAdapterPosition()));
                    }

                    }
            });
        }
    }

    private void goPostData(final Data arrayList) {
        final Intent intent = new Intent(context, Expand_ImageView.class);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        final ArrayList<String> mainSource = new ArrayList<>();

        mainSource.clear();

        mDatabase = FirebaseDatabase.getInstance().getReference("friend/"+arrayList.getUid());
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    mainSource.add(postSnapshot.getKey());
                }
                db.collection("user-checked/"+arrayList.getUid()+"/bookmark")
                        .whereEqualTo("postID", arrayList.getPostID())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    intent.putExtra("bookmark", "unchecked");
                                    for (final QueryDocumentSnapshot document : task.getResult()) {
                                        if(arrayList.getPostID().equals(document.getData().get("postID").toString())){
                                            intent.putExtra("bookmark", "checked");
                                            break;
                                        }
                                    }
                                    db.collection("user-checked/"+arrayList.getUid()+"/like")
                                            .whereEqualTo("postID", arrayList.getPostID())
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        intent.putExtra("postLike", "unchecked");
                                                        for (final QueryDocumentSnapshot document : task.getResult()) {
                                                            if(arrayList.getPostID().equals(document.getData().get("postID").toString())){
                                                                intent.putExtra("postLike", "checked");
                                                                break;
                                                            }
                                                        }
                                                        boolean chkFriend = false;
                                                        for (int i=0; i<mainSource.size(); i++) {
                                                            if (arrayList.getUid().equals(mainSource.get(i))) {
                                                                chkFriend = true;
                                                                break;
                                                            }
                                                        }
                                                        if (chkFriend) {
                                                            intent.putExtra("friend", "checked");
                                                        } else {
                                                            intent.putExtra("friend", "unchecked");
                                                        }
                                                        intent.putExtra("postID", arrayList.getPostID());
                                                        intent.putExtra("nickName", arrayList.getNickName());
                                                        intent.putExtra("uid", arrayList.getUid());
                                                        intent.putExtra("imageUrl1", arrayList.getImageUrl1());
                                                        intent.putExtra("imageUrl2", arrayList.getImageUrl2());
                                                        intent.putExtra("imageUrl3", arrayList.getImageUrl3());
                                                        intent.putExtra("imageUrl4", arrayList.getImageUrl4());
                                                        intent.putExtra("imageUrl5", arrayList.getImageUrl5());
                                                        intent.putExtra("favoriteCount", arrayList.getFavoriteCount());
                                                        intent.putExtra("date", arrayList.getDate());
                                                        intent.putExtra("content", arrayList.getContent());
                                                        intent.putExtra("postID", arrayList.getPostID());
                                                        intent.putExtra("category", arrayList.getCategory());
                                                        intent.putExtra("favoriteCount", arrayList.getFavoriteCount());
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        context.startActivity(intent);
                                                        Expand_ImageView.setListener(calbacklistener);
                                                    } else {
                                                        Log.d("###", "Error getting documents: ", task.getException());
                                                    }
                                                }
                                            });
                                } else {
                                    Log.d("###", "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}