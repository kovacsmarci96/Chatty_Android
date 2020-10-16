package hu.bme.aut.bestchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Array;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import hu.bme.aut.bestchat.Adapter.MessageAdapter;
import hu.bme.aut.bestchat.Fragments.APIService;
import hu.bme.aut.bestchat.Model.Chat;
import hu.bme.aut.bestchat.Model.ChatList;
import hu.bme.aut.bestchat.Model.FrirendsList;
import hu.bme.aut.bestchat.Model.User;
import hu.bme.aut.bestchat.Notification.Client;
import hu.bme.aut.bestchat.Notification.Data;
import hu.bme.aut.bestchat.Notification.MyResponse;
import hu.bme.aut.bestchat.Notification.Sender;
import hu.bme.aut.bestchat.Notification.Token;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    public static final String TEXT = "text";
    public static final int REQUEST_CAMERA = 0;
    public static final int IMAGE_REQUEST = 1;

    CircleImageView profile_image;
    TextView tvUsername, tvStatus;
    FirebaseUser fbUser;
    DatabaseReference reference;
    ImageButton btnSend, btnLocation, btnChoosePicture, btnCamera;
    EditText etSend;
    RelativeLayout relativeLayout;
    String userID, userName;
    ValueEventListener seenListener;
    MessageAdapter messageAdapter;
    List<Chat> mchat;
    RecyclerView recyclerView;

    APIService apiService;
    boolean notify = false;

    Uri selectedPhotoUri;
    Uri takenPhotoUri;
    UploadTask uploadTask;
    String dUri;

    User user;

    Intent intent;

    private List<FrirendsList> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        usersList = new ArrayList<>();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(   new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        profile_image = findViewById(R.id.profile_image);
        tvUsername = findViewById(R.id.username);
        tvStatus = findViewById(R.id.tvStatus);
        btnSend = findViewById(R.id.btnSend);
        btnLocation = findViewById(R.id.btn_location);
        btnChoosePicture = findViewById(R.id.btn_picture);
        btnCamera = findViewById(R.id.btn_camera);
        etSend = findViewById(R.id.textsend);
        relativeLayout = findViewById(R.id.relativelayout);
        recyclerView = findViewById(R.id.recyclerview);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        intent = getIntent();


        userID = intent.getStringExtra("userid");
        userName = intent.getStringExtra("username");

        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                tvUsername.setText(user.getUsername());

                final Long then = Long.parseLong(user.getLastTimeOnline());

                if(user.getStatus().equals("Online"))
                    tvStatus.setText("Online");

                final Handler handler = new Handler();
                final Runnable offRunnable = new Runnable() {
                    @Override
                    public void run() {

                        if (user.getStatus().equals("Online")) {
                            handler.removeCallbacks(this);
                        } else {
                            Long now = (LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                            Long diff = now - then;
                            long minute = (diff / (1000 * 60)) % 60;
                            long hour = (diff / (1000 * 60 * 60)) % 24;
                            long day = (diff / (1000 * 60 * 60 * 24)) % 365;

                            String hours = String.valueOf(hour);
                            String minutes = String.valueOf(minute);
                            String days = String.valueOf(day);

                            if (user.getStatus().equals("Offline")) {
                                if (hour > 0) {
                                    if (hour == 1) {
                                        tvStatus.setText("Offline for " + hours + " hour.");
                                    } else {
                                        tvStatus.setText("Offline for " + hours + " hours.");
                                    }
                                } else if (minute < 60) {
                                    if (minute <= 1) {
                                        tvStatus.setText("Offline for " + 1 + " minute.");
                                    } else {
                                        tvStatus.setText("Offline for " + minutes + " minutes.");
                                    }
                                } else if (hour >= 24){
                                    if(day == 1) {
                                        tvStatus.setText("Offline for " + 1 + " day.");
                                    } else {
                                        tvStatus.setText("Offline for " + days + " days.");
                                    }
                                }
                            }
                            handler.postDelayed(this, 1000);
                        }
                    }
                };

                Thread off = new Thread() {
                    public void run() {
                        try {
                            while (!isInterrupted()) {
                                Thread.sleep(1000);
                                Log.d("Még futok", "running");
                                runOnUiThread(offRunnable);
                            }
                        } catch (InterruptedException e) {
                        }
                    }
                };


                if(!user.getStatus().equals("Online")) {
                    off.start();
                } else {
                    tvStatus.setText("Online");
                    handler.removeCallbacksAndMessages(off);
                    handler.postDelayed(offRunnable,5000);
                }

                if(user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }
                readMessages(fbUser.getUid(),userID,user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userID);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = etSend.getText().toString();
                if(!message.equals("")){
                    sendMessage(fbUser.getUid(),userID,message,"default","default","default");

                } else {
                    Snackbar.make(relativeLayout,"You can't send empty message!",2500).show();
                }
                etSend.setText("");
            }
        });

        btnChoosePicture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                notify = true;
                openGallery();
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                try{
                    openCamera();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                requestPermission();
                FusedLocationProviderClient client1 = LocationServices.getFusedLocationProviderClient(MessageActivity.this);
                if(ActivityCompat.checkSelfPermission(MessageActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    client1.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if(location != null){
                                Double longitude = location.getLongitude();
                                Double latitude = location.getLatitude();

                                StringBuilder result = new StringBuilder();

                                try{
                                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                                    List<Address> address = geocoder.getFromLocation(latitude,longitude,1);
                                    if(address == null){
                                        throw new RuntimeException("No address found");
                                    }

                                    for(int i = 0 ; i <= address.get(0).getMaxAddressLineIndex(); i++){
                                        result.append(address.get(0).getLocality());

                                        if(i != address.get(0).getMaxAddressLineIndex()){
                                            result.append("\n");
                                        }
                                    }


                                } catch (Exception e){
                                    result.append("No address: ");
                                    result.append(e.getMessage());
                                }

                                String myAddress = result.toString();

                                sendMessage(fbUser.getUid(),userID,"Sent a location!","default",longitude.toString(),latitude.toString());

                            }
                        }
                    });
                }

            }
        });
    }

    private void seenMessage(final String userID){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(fbUser.getUid()) && chat.getSender().equals(userID)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("seen",true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage(String sender, final String receiver, String message,String photoUri, String longitude, String latitude){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        String username = "";
        SharedPreferences sharedPreferences = getSharedPreferences(MapsActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        username = sharedPreferences.getString(TEXT,"");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("seen",false);
        hashMap.put("imageURL",photoUri);
        hashMap.put("longitude",longitude);
        hashMap.put("latitude",latitude);
        hashMap.put("username",username);

        reference.child("Chats").push().setValue(hashMap);

        final DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fbUser.getUid())
                .child(userID);

        if(fbUser!= null) {
            chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        chatReference.child("id").setValue(userID);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        final DatabaseReference chatReference1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userID)
                .child(fbUser.getUid());
        chatReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatReference1.child("id").setValue(fbUser.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final String msg = message;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fbUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(notify) {
                    sendNotification(receiver, user.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendNotification(String receiver, final  String username, final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data;
                    if(message.contains("sent")) {
                        data = new Data(fbUser.getUid(), R.mipmap.facebook_profile_image, message, "New message!!", userID);
                    } else {
                        data = new Data(fbUser.getUid(), R.mipmap.facebook_profile_image,  username + ": " +message, "New message!!", userID);
                    }

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Snackbar.make(relativeLayout, "Failed!", 2500).show();
                                        }
                                    }
                                }
                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                    }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages(final String myID, final String uID, final String imageURL){
        mchat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(myID) && chat.getSender().equals(uID) || chat.getReceiver().equals(uID) && chat.getSender().equals(myID)){
                        mchat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this,mchat,imageURL);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void currentUser(String userID){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
        editor.putString("currentuser",userID);
        editor.apply();
    }

    private void requestPermission(){
        String[] s;
        ActivityCompat.requestPermissions(MessageActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MessageActivity.IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null){
            selectedPhotoUri = data.getData();
            if(uploadTask != null && uploadTask.isInProgress()){
                Snackbar.make(relativeLayout,"Sending is in procces!",2500).show();
            } else {
                uploadImage(selectedPhotoUri);
            }
        }

        if(requestCode == MessageActivity.REQUEST_CAMERA && resultCode == Activity.RESULT_OK){
            if (uploadTask != null && uploadTask.isInProgress()) {
                Snackbar.make(relativeLayout,"Sending is in process!",2500).show();
            } else {
                uploadImage(takenPhotoUri);
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, MessageActivity.IMAGE_REQUEST);
    }

    private void openCamera() throws IOException{
        File imageFile = null;
        try{
            imageFile = createImageFile();
        } catch (IOException e){
            e.printStackTrace();
        }
        takenPhotoUri = FileProvider.getUriForFile(MessageActivity.this, "hu.bme.aut.bestchat.provider", imageFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,takenPhotoUri);
        startActivityForResult(intent,MessageActivity.REQUEST_CAMERA);
    }

    String imageFilePath;

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(imageFileName, ".jpg", storageDir);
        }catch (IOException e){
            e.printStackTrace();
        }
        imageFilePath = image.getAbsolutePath();

        return image;
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = MessageActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return  mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(Uri selectedPhotoUri) {
        final ProgressDialog pd = new ProgressDialog(MessageActivity.this);
        pd.setMessage("Sending");
        pd.show();

        if(selectedPhotoUri != null){
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("ChatUploads");
            final StorageReference fileReference = storageReference.child(String.valueOf(System.currentTimeMillis()) + "." + getFileExtension(selectedPhotoUri));

            uploadTask = fileReference.putFile(selectedPhotoUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){}
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        dUri = downloadUri.toString();
                        sendMessage(fbUser.getUid(),userID,"Picture message",dUri,"default","default");
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Snackbar.make(recyclerView,e.getMessage(),2500).show();
                    pd.dismiss();
                }
            });
        } else {
            Snackbar.make(relativeLayout,"There's no choosen picture!", 2500).show();
            pd.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_m, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.showprofile: {
                Intent intent = new Intent(MessageActivity.this,ProfileActivity.class);
                intent.putExtra("user",userID);
                intent.putExtra("username",userName);
                startActivity(intent);
                return true;
            }
            case R.id.addfriend: {
                addFriendDialog();
                return true;
            }
            case R.id.removefrind: {
                deleteFriendDialog();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addFriendDialog(){
        final Dialog dialog = new Dialog(MessageActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_friend_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvUsername = dialog.findViewById(R.id.tvUserName);
        CircleImageView profile_picture = dialog.findViewById(R.id.profile_image);
        Button btnYes = dialog.findViewById(R.id.btnYes);
        Button btnNo = dialog.findViewById(R.id.btnNo);

        tvUsername.setText(user.getUsername());
        if(user.getImageURL().equals("default")){
            profile_picture.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(MessageActivity.this).load(user.getImageURL()).into(profile_picture);
        }

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend();
                dialog.cancel();
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    public void deleteFriendDialog(){
        final Dialog dialog = new Dialog(MessageActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.delete_friend_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvUsername = dialog.findViewById(R.id.tvUserName);
        CircleImageView profile_picture = dialog.findViewById(R.id.profile_image);
        Button btnYes = dialog.findViewById(R.id.btnYes);
        Button btnNo = dialog.findViewById(R.id.btnNo);

        tvUsername.setText(user.getUsername());
        if(user.getImageURL().equals("default")){
            profile_picture.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(MessageActivity.this).load(user.getImageURL()).into(profile_picture);
        }

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFriend();
                dialog.cancel();
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }


    public void addFriend(){
        DatabaseReference reference;
        DatabaseReference reference1;
        final FirebaseUser fbUser;

        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference friendsReference = FirebaseDatabase.getInstance().getReference("Friendslist")
                .child(fbUser.getUid())
                .child(userID);

        final DatabaseReference friendsReference1 = FirebaseDatabase.getInstance().getReference("Friendslist")
                .child(userID)
                .child(fbUser.getUid());


        if(fbUser!= null) {
            friendsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        friendsReference.child("id").setValue(userID)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Snackbar.make(relativeLayout, user.getUsername() + " has been added as a friend!", 2500).show();
                            }
                        });
                    } else {
                        Snackbar.make(relativeLayout,user.getUsername() + " is already your friend",2500).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

            friendsReference1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()){
                        friendsReference1.child("id").setValue(fbUser.getUid());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }


    public void deleteFriend(){
        DatabaseReference reference;
        DatabaseReference reference1;
        final FirebaseUser fbUser;

        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference friendsReference = FirebaseDatabase.getInstance().getReference("Friendslist")
                .child(fbUser.getUid())
                .child(userID);

        final DatabaseReference friendsReference1 = FirebaseDatabase.getInstance().getReference("Friendslist")
                .child(userID)
                .child(fbUser.getUid());


        if(fbUser!= null) {
            friendsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        friendsReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Snackbar.make(relativeLayout,user.getUsername() + " has been deleted from your friends.",2500).show();
                            }
                        });
                    } else {
                        Snackbar.make(relativeLayout,user.getUsername() + " is not your friend",2500).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

            friendsReference1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        friendsReference1.removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        currentUser(userID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentUser("none");
        reference.removeEventListener(seenListener);
    }
}
