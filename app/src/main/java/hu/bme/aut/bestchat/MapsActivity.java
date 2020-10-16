package hu.bme.aut.bestchat;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TEXT = "text";

    private GoogleMap mMap;
    Double longitude = 0.0;
    Double latitude = 0.0;
    String username = "";
    String message = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Intent intent = getIntent();
        longitude = Double.valueOf(intent.getStringExtra("longitude"));
        latitude = Double.valueOf(intent.getStringExtra("latitude"));
        username = intent.getStringExtra("username");
        message = intent.getStringExtra("message");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.isMyLocationEnabled();
        mMap.isIndoorEnabled();
        mMap.isBuildingsEnabled();
        mMap.isTrafficEnabled();

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        LatLng location = new LatLng(latitude,longitude);
        StringBuilder result = new StringBuilder();

        try{
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> address = geocoder.getFromLocation(latitude,longitude,1);
            if(address == null){
                throw new RuntimeException("No address found");
            }

            for(int i = 0 ; i <= address.get(0).getMaxAddressLineIndex(); i++){
                result.append(address.get(0).getAddressLine(i));

                if(i != address.get(0).getMaxAddressLineIndex()){
                    result.append("\n");
                }
            }

        } catch (Exception e){
            result.append("No address: ");
            result.append(e.getMessage());
        }


        String myAddress = result.toString();
        Log.d("asd",myAddress);

        MarkerOptions markerOptions = new MarkerOptions().position(location);

        CameraPosition cameraPosition;
        if(!username.equals("")) {
            markerOptions.title(username + "'s position: ");
            markerOptions.snippet(myAddress);

            mMap.addMarker(markerOptions);

            cameraPosition =  CameraPosition.builder()
                    .target(location)
                    .zoom(17.0f).build();
        } else {
            markerOptions.title(message);
            mMap.addMarker(markerOptions);

            cameraPosition =  CameraPosition.builder()
                    .target(location)
                    .zoom(13.0f).build();
        }


        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

}
