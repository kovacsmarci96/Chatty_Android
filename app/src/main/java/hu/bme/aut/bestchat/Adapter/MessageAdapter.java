package hu.bme.aut.bestchat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Camera;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;



import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import hu.bme.aut.bestchat.MapsActivity;
import hu.bme.aut.bestchat.MessageActivity;
import hu.bme.aut.bestchat.Model.Chat;
import hu.bme.aut.bestchat.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MESSAGE_TYPE_LEFT = 0;
    public static final int MESSAGE_TYPE_RIGHT = 1;
    public static final int MESSAGE_TYPE_LEFT_PICTURE = 2;
    public static final int MESSAGE_TYPE_RIGHT_PICTURE = 3;
    public static final int MESSAGE_TYPE_LEFT_LOCATION = 4;
    public static final int MESSAGE_TYPE_RIGHT_LOCATION = 5;

    private Context mContext;
    private List<Chat> mChat;
    private String imageURL;
    private GoogleMap googleMap;




    FirebaseUser fbUser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageURL){
        this.imageURL = imageURL;
        this.mChat = mChat;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MESSAGE_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return  new MessageAdapter.ViewHolder(view);
        } else if (viewType == MESSAGE_TYPE_LEFT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);;
            return  new MessageAdapter.ViewHolder(view);
        } else if(viewType == MESSAGE_TYPE_RIGHT_PICTURE){
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right_picutre,parent,false);
            return  new MessageAdapter.ViewHolder(view);
        } else if(viewType == MESSAGE_TYPE_LEFT_PICTURE) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left_picture,parent,false);
            return new MessageAdapter.ViewHolder(view);
        } else if(viewType == MESSAGE_TYPE_RIGHT_LOCATION){
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right_location,parent,false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left_location,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.ViewHolder holder, int position) {
        final Chat chat = mChat.get(position);

        holder.show_message.setText(chat.getMessage());


        if(holder.mapView != null){
            holder.mapView.onCreate(null);
            holder.mapView.onResume();
            holder.mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    MapsInitializer.initialize(mContext.getApplicationContext());
                    if(!chat.getLatitude().equals("default") && !chat.getLongitude().equals("default")) {
                        LatLng location = new LatLng(Double.parseDouble(chat.getLatitude()), Double.parseDouble(chat.getLongitude()));
                        googleMap.addMarker(new MarkerOptions().position(location));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,14.0f));
                        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        googleMap.getUiSettings().setZoomGesturesEnabled(false);
                        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {
                                Intent intent = new Intent(mContext, MapsActivity.class);
                                intent.putExtra("longitude",chat.getLongitude());
                                intent.putExtra("latitude",chat.getLatitude());
                                intent.putExtra("username",chat.getUsername());
                                mContext.startActivity(intent);
                            }
                        });
                }
            }
            });
        }

        if(!chat.getLatitude().equals("default") && !chat.getLongitude().equals("default") && !chat.getUsername().equals("default")){
            holder.users_location.setText(chat.getUsername() + "'s location");
        }


        if(!chat.getImageURL().equals("default")){
            Glide.with(mContext).load(chat.getImageURL()).into(holder.text_picture);
        }

        if(imageURL.equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imageURL).into(holder.profile_image);
        }

        if(position == mChat.size()-1){
            if(chat.getSeen()){
                holder.seen.setText("Seen");
            } else {
                holder.seen.setText("Delivered");
            }
        } else {
            holder.seen.setVisibility(View.GONE);
        }
    }




    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView show_message;
        public CircleImageView profile_image;
        public TextView seen;
        public RoundedImageView text_picture;
        public MapView mapView;
        public GoogleMap gMap;
        public TextView users_location;

        public ViewHolder(View itemView){
            super(itemView);

            mapView = (MapView) itemView.findViewById(R.id.map_location);
            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            seen = itemView.findViewById(R.id.txt_seen);
            text_picture = itemView.findViewById(R.id.text_image);
            users_location = itemView.findViewById(R.id.users_location);
        }
    }


    @Override
    public int getItemViewType(int position) {
        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getSender().equals(fbUser.getUid()) && (mChat.get(position).getImageURL().equals("default")) && (mChat.get(position).getLongitude().equals("default")) && (mChat.get(position).getLatitude().equals("default"))){
            return MESSAGE_TYPE_RIGHT;
        } else if (mChat.get(position).getReceiver().equals(fbUser.getUid()) && (mChat.get(position).getImageURL().equals("default")) && (mChat.get(position).getLongitude().equals("default")) && (mChat.get(position).getLatitude().equals("default"))){
            return MESSAGE_TYPE_LEFT;
        } else if (mChat.get(position).getSender().equals(fbUser.getUid()) && (!mChat.get(position).getImageURL().equals("default")) && (mChat.get(position).getLongitude().equals("default")) && (mChat.get(position).getLatitude().equals("default"))){
            return MESSAGE_TYPE_RIGHT_PICTURE;
        } else if (mChat.get(position).getReceiver().equals(fbUser.getUid()) && (!mChat.get(position).getImageURL().equals("default")) && (mChat.get(position).getLongitude().equals("default")) && (mChat.get(position).getLatitude().equals("default"))) {
            return MESSAGE_TYPE_LEFT_PICTURE;
        } else if (mChat.get(position).getSender().equals(fbUser.getUid()) && (mChat.get(position).getImageURL().equals("default")) && (!mChat.get(position).getLongitude().equals("default")) && (!mChat.get(position).getLatitude().equals("default"))) {
            return MESSAGE_TYPE_RIGHT_LOCATION;
        } else {
            return MESSAGE_TYPE_LEFT_LOCATION;
        }
    }
}
