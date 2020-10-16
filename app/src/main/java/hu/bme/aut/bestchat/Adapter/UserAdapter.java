package hu.bme.aut.bestchat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hu.bme.aut.bestchat.MessageActivity;
import hu.bme.aut.bestchat.Model.Chat;
import hu.bme.aut.bestchat.Model.User;
import hu.bme.aut.bestchat.R;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    public static final String SHARED_PREFS1= "sharedPrefs1";
    public static final String TEXT1 = "text1";


    private Context mContext;
    private List<User> mUsers;
    private boolean isOnline;
    private String lastMSG;


    public UserAdapter(Context mContext, List<User> mUsers, boolean isOnline){
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isOnline = isOnline;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false);
        return new UserAdapter.ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = mUsers.get(position);
        holder.tvUsername.setText(user.getUsername());
        if(user.getImageURL().equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid",user.getId());
                intent.putExtra("username",user.getUsername());
                mContext.startActivity(intent);
            }
        });


        if(isOnline){
            getLastMessage(user.getId(),holder.lastMessage);
        } else {
            holder.lastMessage.setVisibility(View.GONE);
        }

        if(isOnline){
            if(user.getStatus().equals("Online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_off.setVisibility(View.VISIBLE);
                holder.img_on.setVisibility(View.GONE);
            }
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        private int id;
        private TextView tvUsername;
        private CircleImageView profile_image;
        private CircleImageView img_on;
        private CircleImageView img_off;
        private TextView lastMessage;
        private ViewHolder(View itemView){
            super(itemView);

            tvUsername = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            lastMessage = itemView.findViewById(R.id.lastmsg);
        }
    }

    private void getLastMessage(final String uID, final TextView tvLastMsg){
        lastMSG = "default";

        final FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        if(fbUser != null) {
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);
                        if (chat.getReceiver().equals(fbUser.getUid()) && chat.getSender().equals(uID)) {
                            lastMSG = "He/She: " + chat.getMessage();
                        } else if (chat.getReceiver().equals(uID) && chat.getSender().equals(fbUser.getUid())) {
                            lastMSG = "You: " + chat.getMessage();
                        }

                        if (chat.getReceiver().equals(fbUser.getUid()) && chat.getSender().equals(uID) && chat.getImageURL().equals("default")){
                            lastMSG = "He/She: " + chat.getMessage();
                        } else if(chat.getReceiver().equals(uID) && chat.getSender().equals(fbUser.getUid()) && chat.getImageURL().equals("default")){
                            lastMSG = "You: " + chat.getMessage();
                        } else if(chat.getReceiver().equals(fbUser.getUid()) && chat.getSender().equals(uID) && !chat.getImageURL().equals("default")){
                            lastMSG = "He/She: Picture message";
                        } else if(chat.getReceiver().equals(uID) && chat.getSender().equals(fbUser.getUid()) && !chat.getImageURL().equals("default")) {
                            lastMSG = "You: Picture message";
                        }
                    }

                    if (lastMSG.equals("default")) {
                        tvLastMsg.setText("");
                    } else {
                        tvLastMsg.setText(lastMSG);
                    }

                    lastMSG = "default";
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

}
