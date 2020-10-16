package hu.bme.aut.bestchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.arch.core.executor.DefaultTaskExecutor;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import hu.bme.aut.bestchat.Adapter.UserAdapter;
import hu.bme.aut.bestchat.Model.ChatList;
import hu.bme.aut.bestchat.Model.FrirendsList;
import hu.bme.aut.bestchat.Model.User;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView tvFullname, tvUsername, tvEmail, tvPhoneNumber, tvHomeTown, tvBirthPlace, tvBirthDate;
    EditText edFullname, edUsername, edEmail, edPhoneNumber, edHomeTown, edBirthPlace, edBirthDate;
    TextView tv1Fullname, tv1Username, tv1Email, tv1PhoneNumber, tv1HomeTown, tv1BirthPlace, tv1BirthDate;
    TextView tvSeeMyProfile;
    RadioGroup radioGroup;
    RadioButton rbEveryone, rbFriends;

    Button btnSave;

    FirebaseUser fbUser;
    DatabaseReference reference;

    RelativeLayout relativeLayout;

    Uri selectedPhotoUri;
    Uri takenPhotoUri;
    UploadTask uploadTask;
    String dUri;

    String userUID, username;


    private List<User> allUsers;
    private List<User> friendUsers;
    private List<FrirendsList> friendsList;

    final Calendar myCalendar = Calendar.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fbUser = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        userUID = intent.getStringExtra("user");
        username = intent.getStringExtra("username");

        friendsList = new ArrayList<>();
        allUsers = new ArrayList<>();
        friendUsers = new ArrayList<>();

        relativeLayout = findViewById(R.id.relativelayout);
        profile_image = findViewById(R.id.profile_image);
        tvFullname = findViewById(R.id.tvFullname);
        tvUsername = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhoneNumber = findViewById(R.id.tvNumber);
        tvHomeTown = findViewById(R.id.tvHomeTown);
        tvBirthPlace = findViewById(R.id.tvBornTown);
        tvBirthDate = findViewById(R.id.tvBornTime);
        tvSeeMyProfile = findViewById(R.id.seemyprofile);

        tv1Fullname = findViewById(R.id.tv1fullname);
        tv1Username = findViewById(R.id.tv1username);
        tv1Email = findViewById(R.id.tv1email);
        tv1PhoneNumber = findViewById(R.id.tv1number);
        tv1HomeTown = findViewById(R.id.tv1hometown);
        tv1BirthPlace = findViewById(R.id.tv1borntown);
        tv1BirthDate = findViewById(R.id.tv1borntime);

        edFullname = findViewById(R.id.fullname);
        edUsername = findViewById(R.id.username);
        edEmail = findViewById(R.id.email);
        edPhoneNumber = findViewById(R.id.number);
        edHomeTown = findViewById(R.id.hometown);
        edBirthPlace = findViewById(R.id.borntown);
        edBirthDate = findViewById(R.id.borntime);

        radioGroup = findViewById(R.id.radiogroup);
        rbEveryone = findViewById(R.id.everyone);
        rbFriends = findViewById(R.id.friends);

        btnSave = findViewById(R.id.btnSave);

        if(userUID.equals(fbUser.getUid())){
            userUID = fbUser.getUid();
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(userUID.equals(fbUser.getUid())) {
            getSupportActionBar().setTitle("             Your profile");
        } else {
            getSupportActionBar().setTitle(username + "'s profile");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        if(userUID==fbUser.getUid()){
            showMyProfile();
        } else {
            showOtherProfile();
            checkFriendship();
        }


        if(userUID.equals(fbUser.getUid()))
            profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyCustomAlertDialog();
            }
        });
        if(!tv1BirthPlace.equals("") || !tv1BirthPlace.equals("default")) {
            tv1BirthPlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LatLng location = getLocationFromAddress(ProfileActivity.this,tv1BirthPlace.getText().toString());
                    if(location!=null) {
                        Intent intent = new Intent(ProfileActivity.this, MapsActivity.class);
                        intent.putExtra("longitude", String.valueOf(location.longitude));
                        intent.putExtra("latitude", String.valueOf(location.latitude));
                        intent.putExtra("username","");
                        intent.putExtra("message",tv1Username.getText().toString() + " was born in " + tv1BirthPlace.getText().toString());
                        startActivity(intent);
                    }
                }
            });
        }

        if(!tv1HomeTown.equals("") || !tv1HomeTown.equals("default")) {
            tv1HomeTown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LatLng location = getLocationFromAddress(ProfileActivity.this,tv1HomeTown.getText().toString());
                    if(location!=null) {
                        Intent intent = new Intent(ProfileActivity.this, MapsActivity.class);
                        intent.putExtra("longitude", String.valueOf(location.longitude));
                        intent.putExtra("latitude", String.valueOf(location.latitude));
                        intent.putExtra("username","");
                        intent.putExtra("message",tv1Username.getText().toString() + "'s home town is " + tv1HomeTown.getText().toString());
                        startActivity(intent);
                    }
                }
            });
        }

        profile_image.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(fbUser.getUid() == userUID){
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.edit: {
                startEditing();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void checkFriendship(){
        final FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference allUsersReference = FirebaseDatabase.getInstance().getReference("Users");

        allUsersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    assert fbUser != null;

                    if (!user.getId().equals(fbUser.getUid())) {
                        allUsers.add(user);
                    }
                }
                getFriendslist();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getFriendslist(){
        final FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference friendsListReference;
        friendsListReference = FirebaseDatabase.getInstance().getReference("Friendslist").child(fbUser.getUid());
        friendsListReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    FrirendsList frList = snapshot.getValue(FrirendsList.class);
                    friendsList.add(frList);
                }

                getFriends();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFriends(){
        friendUsers = new ArrayList<>();
        final DatabaseReference friendReference;
        friendReference = FirebaseDatabase.getInstance().getReference("Users");
        friendReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendUsers.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for(FrirendsList frList : friendsList){
                        if(user.getId().equals(frList.getId())){
                            friendUsers.add(user);
                        }
                    }
                }
                for(int i = 0; i < allUsers.size();i++) {
                    if(allUsers.get(i).getId().equals(userUID)){
                        if(allUsers.get(i).getWhocanseemyprofile().equals("Everyone")){
                            showProfileforEveryone();
                        } else if (allUsers.get(i).getWhocanseemyprofile().equals("Only friends")){
                            for(int j = 0; j < friendUsers.size(); j++){
                                if(friendUsers.get(j).getId().equals(userUID)){
                                    showProfileforEveryone();
                                } else {
                                    showProfileforNotFriends();
                                }
                            }
                        }
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void showMyProfile(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(fbUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User myUser = dataSnapshot.getValue(User.class);

                tv1Username.setText(myUser.getUsername());
                tv1Email.setText(myUser.getEmail());

                tv1Fullname.setText(myUser.getFullname());
                tv1BirthDate.setText(myUser.getBornTime());
                tv1BirthPlace.setText(myUser.getBornTown());
                tv1HomeTown.setText(myUser.getHometown());
                tv1PhoneNumber.setText(myUser.getNumber());

                edUsername.setText(myUser.getUsername());
                edFullname.setText(myUser.getFullname());
                edBirthDate.setText(myUser.getBornTime());
                edBirthPlace.setText(myUser.getBornTown());
                edHomeTown.setText(myUser.getHometown());
                edPhoneNumber.setText(myUser.getNumber());

                if(myUser.getFullname().equals("default") || myUser.getFullname().equals("")){
                    tvFullname.setVisibility(View.INVISIBLE);
                    tv1Fullname.setVisibility(View.INVISIBLE);
                }

                if(myUser.getNumber().equals("default") || myUser.getNumber().isEmpty()){
                    tvPhoneNumber.setVisibility(View.INVISIBLE);
                    tv1PhoneNumber.setVisibility(View.INVISIBLE);
                }

                if(myUser.getBornTime().equals("default") || myUser.getBornTime().isEmpty()){
                    tvBirthDate.setVisibility(View.INVISIBLE);
                    tv1BirthDate.setVisibility(View.INVISIBLE);
                }

                if(myUser.getHometown().equals("default") || myUser.getHometown().isEmpty()){
                    tvHomeTown.setVisibility(View.INVISIBLE);
                    tv1HomeTown.setVisibility(View.INVISIBLE);
                }

                if(myUser.getBornTown().equals("default") || myUser.getBornTown().isEmpty()){
                    tvBirthPlace.setVisibility(View.INVISIBLE);
                    tv1BirthPlace.setVisibility(View.INVISIBLE);
                }

                if(myUser.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(ProfileActivity.this).load(myUser.getImageURL()).into(profile_image);
                }

                if(myUser.getWhocanseemyprofile().equals("Everyone")){
                    rbEveryone.setChecked(true);
                }

                if(myUser.getWhocanseemyprofile().equals("Only friends")){
                    rbFriends.setChecked(true);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void showOtherProfile(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userUID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                tv1Username.setText(user.getUsername());
                tv1Email.setText(user.getEmail());

                tv1Fullname.setText(user.getFullname());
                tv1BirthDate.setText(user.getBornTime());
                tv1BirthPlace.setText(user.getBornTown());
                tv1HomeTown.setText(user.getHometown());
                tv1PhoneNumber.setText(user.getNumber());

                edUsername.setText(user.getUsername());
                edFullname.setText(user.getFullname());
                edBirthDate.setText(user.getBornTime());
                edBirthPlace.setText(user.getBornTown());
                edHomeTown.setText(user.getHometown());
                edPhoneNumber.setText(user.getNumber());

                if(user.getUsername().equals("default") || user.getUsername().isEmpty()){
                    tvUsername.setVisibility(View.INVISIBLE);
                    tv1Username.setVisibility(View.INVISIBLE);
                }

                if(user.getFullname().equals("default") || user.getFullname().isEmpty()){
                    tvFullname.setVisibility(View.INVISIBLE);
                    tv1Fullname.setVisibility(View.INVISIBLE);
                }

                if(user.getNumber().equals("default") || user.getNumber().isEmpty()){
                    tvPhoneNumber.setVisibility(View.INVISIBLE);
                    tv1PhoneNumber.setVisibility(View.INVISIBLE);
                }

                if(user.getBornTime().equals("default") || user.getBornTime().isEmpty()){
                    tvBirthDate.setVisibility(View.INVISIBLE);
                    tv1BirthDate.setVisibility(View.INVISIBLE);
                }

                if(user.getHometown().equals("default") || user.getHometown().isEmpty()){
                    tvHomeTown.setVisibility(View.INVISIBLE);
                    tv1HomeTown.setVisibility(View.INVISIBLE);
                }

                if(user.getBornTown().equals("default") || user.getBornTown().isEmpty()){
                    tvBirthPlace.setVisibility(View.INVISIBLE);
                    tv1BirthPlace.setVisibility(View.INVISIBLE);
                }

                if(user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(ProfileActivity.this).load(user.getImageURL()).into(profile_image);
                }

                tvSeeMyProfile.setVisibility(View.INVISIBLE);
                radioGroup.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateLabel(){
        String myFormat = "yyyy.MM.dd EEEE";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat,Locale.ENGLISH);

        edBirthDate.setText(sdf.format(myCalendar.getTime()));
    }


    public void checkButton(View v){
        int radiobuttonid = radioGroup.getCheckedRadioButtonId();
        RadioButton rb = findViewById(radiobuttonid);
    }


    public void startEditing(){
        edFullname.setBackgroundResource(R.drawable.edittext_backg);
        edUsername.setBackgroundResource(R.drawable.edittext_backg);
        edPhoneNumber.setBackgroundResource(R.drawable.edittext_backg);
        edHomeTown.setBackgroundResource(R.drawable.edittext_backg);
        edBirthPlace.setBackgroundResource(R.drawable.edittext_backg);
        edBirthDate.setBackgroundResource(R.drawable.edittext_backg);

        profile_image.setEnabled(true);

        tvFullname.setVisibility(View.VISIBLE);
        tvUsername.setVisibility(View.VISIBLE);
        tvPhoneNumber.setVisibility(View.VISIBLE);
        tvHomeTown.setVisibility(View.VISIBLE);
        tvBirthPlace.setVisibility(View.VISIBLE);
        tvBirthDate.setVisibility(View.VISIBLE);
        tvSeeMyProfile.setVisibility(View.VISIBLE);

        tv1Fullname.setVisibility(View.INVISIBLE);
        tv1Username.setVisibility(View.INVISIBLE);
        tv1PhoneNumber.setVisibility(View.INVISIBLE);
        tv1HomeTown.setVisibility(View.INVISIBLE);
        tv1BirthPlace.setVisibility(View.INVISIBLE);
        tv1BirthDate.setVisibility(View.INVISIBLE);

        edFullname.setVisibility(View.VISIBLE);
        edUsername.setVisibility(View.VISIBLE);
        edPhoneNumber.setVisibility(View.VISIBLE);
        edHomeTown.setVisibility(View.VISIBLE);
        edBirthPlace.setVisibility(View.VISIBLE);
        edBirthDate.setVisibility(View.VISIBLE);

        rbFriends.setEnabled(true);
        rbEveryone.setEnabled(true);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR,year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                updateLabel();
            }
        };

        edBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(ProfileActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()-315569520000L);
                datePickerDialog.show();
            }
        });


        btnSave.setVisibility(View.VISIBLE);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edFullname.setVisibility(View.INVISIBLE);
                edUsername.setVisibility(View.INVISIBLE);
                edPhoneNumber.setVisibility(View.INVISIBLE);
                edHomeTown.setVisibility(View.INVISIBLE);
                edBirthPlace.setVisibility(View.INVISIBLE);
                edBirthDate.setVisibility(View.INVISIBLE);
                btnSave.setVisibility(View.INVISIBLE);

                tv1Fullname.setText(edFullname.getText().toString());
                tv1Username.setText(edUsername.getText().toString());
                tv1PhoneNumber.setText(edPhoneNumber.getText().toString());
                tv1HomeTown.setText(edHomeTown.getText().toString());
                tv1BirthPlace.setText(edBirthPlace.getText().toString());
                tv1BirthDate.setText(edBirthDate.getText().toString());

                tv1Fullname.setVisibility(View.VISIBLE);
                tv1Username.setVisibility(View.VISIBLE);
                tv1PhoneNumber.setVisibility(View.VISIBLE);
                tv1HomeTown.setVisibility(View.VISIBLE);
                tv1BirthPlace.setVisibility(View.VISIBLE);
                tv1BirthDate.setVisibility(View.VISIBLE);

                int radioID = radioGroup.getCheckedRadioButtonId();
                RadioButton rb = findViewById(radioID);

                String whocansee = rb.getText().toString();

                rbEveryone.setEnabled(false);
                rbFriends.setEnabled(false);

                reference = FirebaseDatabase.getInstance().getReference("Users").child(userUID);
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("username",tv1Username.getText().toString());
                hashMap.put("fullname",tv1Fullname.getText().toString());
                hashMap.put("number",tv1PhoneNumber.getText().toString());
                hashMap.put("hometown",tv1HomeTown.getText().toString());
                hashMap.put("borntown",tv1BirthPlace.getText().toString());
                hashMap.put("borntime",tv1BirthDate.getText().toString());
                hashMap.put("whocanseemyprofile",whocansee);
                reference.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(relativeLayout,"Profile updated successfully!",2500).show();
                    }
                });
            }
        });
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

    public void MyCustomAlertDialog(){
        final Dialog dialog = new Dialog(ProfileActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.profile_picture_change_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        CircleImageView btnGallery = dialog.findViewById(R.id.gallery);
        CircleImageView btnCamera = dialog.findViewById(R.id.camera);
        Button btnBack = dialog.findViewById(R.id.btnback);

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
                dialog.cancel();
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    openCamera();
                } catch (IOException e){
                    e.printStackTrace();
                }
                dialog.cancel();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        dialog.show();
    }



    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, MessageActivity.IMAGE_REQUEST);
    }

    private void openCamera() throws IOException {
        File imageFile = null;
        try{
            imageFile = createImageFile();
        } catch (IOException e){
            e.printStackTrace();
        }
        takenPhotoUri = FileProvider.getUriForFile(ProfileActivity.this, "hu.bme.aut.bestchat.provider", imageFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,takenPhotoUri);
        startActivityForResult(intent,MessageActivity.REQUEST_CAMERA);
    }

    String imageFilePath;

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
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
        ContentResolver contentResolver = ProfileActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return  mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(Uri selectedPhotoUri) {
        final ProgressDialog pd = new ProgressDialog(ProfileActivity.this);
        pd.setMessage("Uploading");
        pd.show();

        if(selectedPhotoUri != null){
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("Uploads");
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

                        fbUser = FirebaseAuth.getInstance().getCurrentUser();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(fbUser.getUid());
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageURL",dUri);
                        reference.updateChildren(hashMap);
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Snackbar.make(relativeLayout,e.getMessage(),2500).show();
                    pd.dismiss();
                }
            });
        } else {
            Snackbar.make(relativeLayout,"There's no choosen picture!", 2500).show();
            pd.dismiss();
        }
    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address.size() == 0) {
                return null;
            } else {
                Address location = address.get(0);
                p1 = new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return p1;
    }

    public void showProfileforEveryone(){
        tvEmail.setVisibility(View.VISIBLE);
        tv1Email.setVisibility(View.VISIBLE);
        tvHomeTown.setVisibility(View.VISIBLE);
        tv1HomeTown.setVisibility(View.VISIBLE);
        tv1BirthPlace.setVisibility(View.VISIBLE);
        tvBirthPlace.setVisibility(View.VISIBLE);
        tvBirthDate.setVisibility(View.VISIBLE);
        tv1BirthDate.setVisibility(View.VISIBLE);
        tvPhoneNumber.setVisibility(View.VISIBLE);
        tv1PhoneNumber.setVisibility(View.VISIBLE);
        edBirthDate.setVisibility(View.VISIBLE);
        edBirthPlace.setVisibility(View.VISIBLE);
        edHomeTown.setVisibility(View.VISIBLE);
        edPhoneNumber.setVisibility(View.VISIBLE);
        edUsername.setVisibility(View.VISIBLE);
        edFullname.setVisibility(View.VISIBLE);
        edEmail.setVisibility(View.VISIBLE);
    }

    public void showProfileforNotFriends(){
        tvEmail.setVisibility(View.INVISIBLE);
        tv1Email.setVisibility(View.INVISIBLE);
        tvHomeTown.setVisibility(View.INVISIBLE);
        tv1HomeTown.setVisibility(View.INVISIBLE);
        tv1BirthPlace.setVisibility(View.INVISIBLE);
        tvBirthPlace.setVisibility(View.INVISIBLE);
        tvBirthDate.setVisibility(View.INVISIBLE);
        tv1BirthDate.setVisibility(View.INVISIBLE);
        tvPhoneNumber.setVisibility(View.INVISIBLE);
        tv1PhoneNumber.setVisibility(View.INVISIBLE);
        edBirthDate.setVisibility(View.INVISIBLE);
        edBirthPlace.setVisibility(View.INVISIBLE);
        edHomeTown.setVisibility(View.INVISIBLE);
        edPhoneNumber.setVisibility(View.INVISIBLE);
        edUsername.setVisibility(View.INVISIBLE);
        edFullname.setVisibility(View.INVISIBLE);
        edEmail.setVisibility(View.INVISIBLE);
    }

}
