package com.example.petdiary.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petdiary.R;
import com.example.petdiary.activity.kon_AnimalProfileActivity;
import com.example.petdiary.data.Data;
import com.example.petdiary.data.PetData;
import com.example.petdiary.fragment.FragmentMy;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;


public class Kon_Mypage_petAdapter extends RecyclerView.Adapter<Kon_Mypage_petAdapter.myPagePetViewHolder> {

     private ArrayList<PetData> arrayList;
    Activity activity;
    private Context context;
     int prePosition = -1;
    static SparseBooleanArray selectedItems = new SparseBooleanArray() ;

   static FragmentMy.StringCallback stringCallback ;


    public Kon_Mypage_petAdapter(ArrayList<PetData> arrayList, Context context, Activity activity, FragmentMy.StringCallback callback) {
        this.arrayList = arrayList;
        this.context = context;
        this.activity= activity;
        this.stringCallback = callback;

    }


    @NonNull
    @Override
    public myPagePetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.kon_mypage_pet_item, parent, false);
        myPagePetViewHolder holder = new myPagePetViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull myPagePetViewHolder holder, int position) {

        String url = arrayList.get(position).getImageUrl();

        if(!url.equals("")) {
            Glide.with(context).load(url).centerCrop().override(500).into(holder.postImage);
        }
        else{
            Glide.with(context).load("https://t1.daumcdn.net/cfile/tistory/9971E63E5BDAF4A809").centerCrop().override(500).into(holder.postImage);
        }

        holder.changeFrameState(selectedItems.get(position));

        Log.d("dsd", "onBindViewHolder: " + arrayList.size());

//        holder.onBind(arrayList.get(position), position);


    }

    @Override
    public int getItemCount() {
        // 삼항 연산자
        return arrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public class myPagePetViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView frame;
        ImageView postImage;
        int frameWidth;
        private PetData data;
        private int position;


        public myPagePetViewHolder(@NonNull final View itemView) {
            super(itemView);
            frame = itemView.findViewById(R.id.mypage_pet_imageView);
            this.postImage = itemView.findViewById(R.id.mypage_pet_image);
            frameWidth = itemView.getLayoutParams().width / 10;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int position = getAdapterPosition();

                    if (selectedItems.get(position)) {
                        selectedItems.delete(position);
                        stringCallback.callback("");
                        Log.d("dangJunDebug", "setOnClickListener IF: " + selectedItems.get(position));

                    } else {
                        selectedItems.delete(prePosition);
                        selectedItems.put(position, true);
                        stringCallback.callback(arrayList.get(position).getPetId());
                        Log.d("dangJunDebug", "setOnClickListener ELSE " + position);
                        Log.d("dangJunDebug", "setOnClickListener ELSE " + selectedItems.size());
                        Log.d("dangJunDebug", "setOnClickListener ELSE " + selectedItems.get(position));

                    }

                    // 해당 포지션의 변화를 알림
                    if (prePosition != -1) notifyItemChanged(prePosition);
                    notifyItemChanged(position);
                    Log.d("dangJunDebug", "notifyItemChanged");

                    // 클릭된 position 저장
                    prePosition = position;
                    Log.d("dangJunDebug", "setOnClickListener END " + selectedItems.size());



                }
            });

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final String uid = user.getUid();

            /* 길게 눌렀을때 수정모드로 진입 */
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();

                    Intent intent = new Intent(context, kon_AnimalProfileActivity.class);
                    PetData pet = arrayList.get(position);

                    intent.putExtra("isAddMode", false);
                    intent.putExtra("isEditMode", false);
                    intent.putExtra("petId", pet.getPetId());
                    intent.putExtra("petMaster", pet.getPetMaster());

                    intent.putExtra("userId",uid);

                    intent.putExtra("petImage", pet.getImageUrl());
                    intent.putExtra("name",pet.getName());
                    intent.putExtra("memo", pet.getMemo());


                    activity.startActivityForResult(intent,1);

                    return false;
                }
            });
        }

        void onBind(PetData data, int position){

                this.data = data;
                this.position = position;



        }

        private void changeFrameState(final boolean isOpen) {
            Log.d("dangJunDebug", "changeFrameState " + isOpen);

            if(isOpen) {
                frame.setStrokeColor(ContextCompat.getColor(context, R.color.colorAccent));
            }
            else {
                frame.setStrokeColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            }
        }
    }
}