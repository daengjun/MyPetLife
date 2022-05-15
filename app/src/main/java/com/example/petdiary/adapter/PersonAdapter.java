package com.example.petdiary.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petdiary.data.Chat;
import com.example.petdiary.util.ItemTouchHelperListener;
import com.example.petdiary.R;
import com.example.petdiary.activity.ChatActivity;
import com.example.petdiary.util.callBackListener;
import com.example.petdiary.data.Person;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.ViewHolder> implements ItemTouchHelperListener, callBackListener {
    private ArrayList<Chat> mDataset;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String stMyEmail;
    FirebaseDatabase database;
    callBackListener callBackListener;

    private DatabaseReference mDatabase;

    ArrayList<Person> items = new ArrayList<Person>();
    private OnItemClickListener mListener = null;
    private Context mContext;

    public PersonAdapter(Context mContext, callBackListener callBackListener){
        this.mContext = mContext;
        this.callBackListener = callBackListener;
    }
    public PersonAdapter(ArrayList<Chat> myDataset, String stEmail) {
        mDataset = myDataset;
        this.stMyEmail = stEmail;
    }

    public void setOnitemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }

    @Override
    public void refresh(boolean check) {

    }

    public interface OnItemClickListener{
        void onItemClick(View v, int position);

    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        Button chat;
        TextView nick;
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        public ViewHolder(final View itemView){
            super(itemView);

            nick = itemView.findViewById(R.id.textView);
            textView = itemView.findViewById(R.id.tvChat);
            chat = itemView.findViewById(R.id.btn_chat);

            final String[] nn = new String[1];
            db.collection("users")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if(document.get("email").toString().equals(user.getEmail())){
                                        nn[0] = document.get("nickName").toString();
                                    }
                                }
                            } else {
                            }
                        }
                    });


            chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(itemView.getContext(), ChatActivity.class);
                    //intent.putExtra("email",stMyEmail );
                    intent.putExtra("nickName",nick.getText());
                    intent.putExtra("my",nn[0]);
                    mContext.startActivity(intent);
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION){
                        Person item = items.get(pos);
                        if(mListener != null){
                            mListener.onItemClick(v, pos);
                        }
                    }
                }
            });
        }
        public void setItem(final Person item){
            nick.setText(item.getNickname());
            db.collection("users")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String s = document.get("profileImg").toString();
                                    String ss = document.get("nickName").toString();
                                    if(ss.equals(item.getNickname())) {
                                        if (s.length() > 0) {
                                            ImageView profileImage = (ImageView) itemView.findViewById(R.id.imageView);
                                            Glide.with(itemView.getContext()).load(s).centerCrop().into(profileImage);
                                            //Glide.with(holder.profile.getContext()).load(s).centerCrop().into(profileImage);
                                        }
                                    }
                                }
                            } else {

                            }
                        }
                    });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType){
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.person_item, viewGroup, false);

        return new ViewHolder(itemView);

    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder , int position){

        Person item = items.get(position);
        viewHolder.setItem(item);
    }
    @Override
    public int getItemCount(){
        return items.size();
    }

    public void addItem(Person item){
        items.add(item);
    }
    public void setItems(ArrayList<Person> items){
        this.items  = items;
    }
    public Person getItem(int position){
        return items.get(position);
    }
    public void setItem(int position, Person item){
        items.set(position,item);
    }
    public int getItemViewType(int position) {
        return super.getItemViewType(position);

    }

    public boolean onItemMove(int from_position, int to_position) {
        Person person = items.get(from_position);
        items.remove(from_position);
        items.add(to_position,person);
        notifyItemMoved(from_position,to_position);
        return true;
    }

    public void onItemSwipe(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onRightClick(int position, RecyclerView.ViewHolder viewHolder) {
        mDatabase = FirebaseDatabase.getInstance().getReference("friend/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+items.get(position).getUid());
        mDatabase.removeValue();
        items.remove(position);
        notifyItemRemoved(position);
        callBackListener.refresh(true);
    }

}
