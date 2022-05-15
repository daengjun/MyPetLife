package com.example.petdiary.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.petdiary.util.Expand_ImageView;
import com.example.petdiary.activity.MainActivity;
import com.example.petdiary.activity.SettingBlockFriendsActivity;
import com.example.petdiary.activity.SettingBookMarkActivity;
import com.example.petdiary.adapter.CustomAdapter;
import com.example.petdiary.data.Data;
import com.example.petdiary.R;
import com.example.petdiary.util.callBackListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class FragmentMain extends Fragment implements callBackListener {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Data> arrayList;
    private View view;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    BottomNavigationView bottomNavigationView;
    boolean contentCheck ;
    private FirebaseDatabase firebaseDatabase;

    private ArrayList<String> bookmark = new ArrayList<String>();
    private ArrayList<String> like = new ArrayList<String>();
    private int firstCount;
    private DatabaseReference mDatabase;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_main, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_layout);
        moveTop();
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화


        layoutManager = new LinearLayoutManager(getContext());

        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>(); // User 객체를 담을 어레이 리스트 (어댑터쪽으로)

        setInfo();

        adapter = new CustomAdapter(arrayList, getContext(),this,(MainActivity)getActivity());
        recyclerView.setAdapter(adapter); // 리사이클러뷰에 어댑터 연결
        SettingBookMarkActivity.setListener(this);
        SettingBlockFriendsActivity.setlistener(this);


        //새로고침
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setInfo();
                // 동글동글 도는거 사라짐
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){

        } else {
            moveTop();
        }
    }




    private void moveTop(){
        bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.tab1){
                    recyclerView.smoothScrollToPosition(0);
                }
            }
        });
    }



    public void setInfo(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final ArrayList<String> mainSource = new ArrayList<>();
        firstCount = arrayList.size();
            arrayList.clear();
            bookmark.clear();
            like.clear();
            mainSource.clear();
            mainSource.add(uid);

        mDatabase = FirebaseDatabase.getInstance().getReference("friend/"+uid);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    mainSource.add(postSnapshot.getKey());
                }
                db.collection("user-checked/"+uid+"/bookmark")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (final QueryDocumentSnapshot document : task.getResult()) {
                                        bookmark.add(document.getData().get("postID").toString());
                                    }
                                    db.collection("user-checked/"+uid+"/like")
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (final QueryDocumentSnapshot document : task.getResult()) {
                                                            like.add(document.getData().get("postID").toString());
                                                        }
                                                        db.collection("post")
                                                                .get()
                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                        if (task.isSuccessful()) {
                                                                            for (final QueryDocumentSnapshot document : task.getResult()) {
                                                                                final Data dataList = new Data();
                                                                                for(int j=0; j<mainSource.size(); j++){
                                                                                    if(mainSource.get(j).equals(document.getData().get("uid").toString())){
                                                                                        dataList.setBookmark(false);
                                                                                        for(int i=0; i<bookmark.size(); i++){
                                                                                            if(bookmark.get(i).equals(document.getId())){
                                                                                                dataList.setBookmark(true);
                                                                                                break;
                                                                                            }
                                                                                        }
                                                                                        dataList.setLike(false);
                                                                                        for(int i=0; i<like.size(); i++){
                                                                                            if(like.get(i).equals(document.getId())){
                                                                                                dataList.setLike(true);
                                                                                                break;
                                                                                            }
                                                                                        }

                                                                                            dataList.setPostID(document.getId());
                                                                                            dataList.setUid(document.getData().get("uid").toString());
                                                                                            dataList.setContent(document.getData().get("content").toString());
                                                                                            dataList.setImageUrl1(document.getData().get("imageUrl1").toString());
                                                                                            dataList.setImageUrl2(document.getData().get("imageUrl2").toString());
                                                                                            dataList.setImageUrl3(document.getData().get("imageUrl3").toString());
                                                                                            dataList.setImageUrl4(document.getData().get("imageUrl4").toString());
                                                                                            dataList.setImageUrl5(document.getData().get("imageUrl5").toString());
                                                                                            dataList.setNickName(document.getData().get("nickName").toString());
                                                                                            dataList.setCategory(document.getData().get("category").toString());
                                                                                            dataList.setDate(document.getData().get("date").toString());
                                                                                            dataList.setFavoriteCount(Integer.parseInt(document.getData().get("favoriteCount").toString()));
                                                                                            arrayList.add(0, dataList);

                                                                                        }
                                                                                }
                                                                            }
                                                                            adapter.notifyDataSetChanged();

                                                                        } else {
                                                                            Log.d("###", "Error getting documents: ", task.getException());
                                                                        }
                                                                    }
                                                                });
                                                            adapter.notifyDataSetChanged();

                                                    } else {
                                                        Log.d("###", "Error getting documents: ", task.getException());
                                                    }
                                                }
                                            });
                                        adapter.notifyDataSetChanged();
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


    public void getPostID(String postID){

        for(int i=0; i<arrayList.size(); i++){
            if (arrayList.get(i).getPostID().equals(postID)){

                Log.d("sads", "getPostID: " + i);
                Log.d("ㄴㅇㄴㅇ", "getPostID: 여기타는거 맞느?>");
                final Intent intent = new Intent(getContext(), Expand_ImageView.class);
                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                firebaseDatabase = FirebaseDatabase.getInstance();

                final ArrayList<String> mainSource = new ArrayList<>();

                mainSource.clear();

                mDatabase = FirebaseDatabase.getInstance().getReference("friend/"+arrayList.get(i).getUid());
                final int finalI = i;
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                            mainSource.add(postSnapshot.getKey());
                        }
                        db.collection("user-checked/"+arrayList.get(finalI).getUid()+"/bookmark")
                                .whereEqualTo("postID", arrayList.get(finalI).getPostID())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            intent.putExtra("bookmark", "unchecked");
                                            for (final QueryDocumentSnapshot document : task.getResult()) {
                                                if(arrayList.get(finalI).getPostID().equals(document.getData().get("postID").toString())){
                                                    intent.putExtra("bookmark", "checked");
                                                    break;
                                                }
                                            }
                                            db.collection("user-checked/"+arrayList.get(finalI).getUid()+"/like")
                                                    .whereEqualTo("postID", arrayList.get(finalI).getPostID())
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                intent.putExtra("postLike", "unchecked");
                                                                for (final QueryDocumentSnapshot document : task.getResult()) {
                                                                    if(arrayList.get(finalI).getPostID().equals(document.getData().get("postID").toString())){
                                                                        intent.putExtra("postLike", "checked");
                                                                        break;
                                                                    }
                                                                }
                                                                boolean chkFriend = false;
                                                                for (int i=0; i<mainSource.size(); i++) {
                                                                    if (arrayList.get(finalI).getUid().equals(mainSource.get(i))) {
                                                                        chkFriend = true;
                                                                        break;
                                                                    }
                                                                }
                                                                if (chkFriend) {
                                                                    intent.putExtra("friend", "checked");
                                                                } else {
                                                                    intent.putExtra("friend", "unchecked");
                                                                }
                                                                intent.putExtra("postID", arrayList.get(finalI).getPostID());
                                                                intent.putExtra("nickName", arrayList.get(finalI).getNickName());
                                                                intent.putExtra("uid", arrayList.get(finalI).getUid());
                                                                intent.putExtra("imageUrl1", arrayList.get(finalI).getImageUrl1());
                                                                intent.putExtra("imageUrl2", arrayList.get(finalI).getImageUrl2());
                                                                intent.putExtra("imageUrl3", arrayList.get(finalI).getImageUrl3());
                                                                intent.putExtra("imageUrl4", arrayList.get(finalI).getImageUrl4());
                                                                intent.putExtra("imageUrl5", arrayList.get(finalI).getImageUrl5());
                                                                intent.putExtra("favoriteCount", arrayList.get(finalI).getFavoriteCount());
                                                                intent.putExtra("date", arrayList.get(finalI).getDate());
                                                                intent.putExtra("content", arrayList.get(finalI).getContent());
                                                                intent.putExtra("postID", arrayList.get(finalI).getPostID());
                                                                intent.putExtra("category", arrayList.get(finalI).getCategory());
                                                                intent.putExtra("favoriteCount", arrayList.get(finalI).getFavoriteCount());
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                getContext().startActivity(intent);
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
    }


    public void refresh(boolean check){

        /*메인에서만 check가 false면 게시글 삭제 , 북마크 최신업데이트 true면 게시글 수정 업데이트 */
    if(check==false){
        ((MainActivity)getActivity()).refresh(check);
    }
    contentCheck = check;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mSwipeRefreshLayout != null){
            if(contentCheck){
                ((MainActivity)getActivity()).refresh(contentCheck);
                contentCheck = false;
            }
        }
    }
}



