package sg.edu.rp.c346.id19020125.p09_getting_my_location;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button btn1, btn2, btn3;
    private GoogleMap map;
    TextView tv;
    private FusedLocationProviderClient client;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    String folderLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        tv = findViewById(R.id.tv);

        client = LocationServices.getFusedLocationProviderClient(this);

        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment)fm.findFragmentById(R.id.map);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                String msg;
                if (locationResult != null) {
                    Location data = locationResult.getLastLocation();
                    double lat = data.getLatitude();
                    double lng = data.getLongitude();
                    msg = "Last known location: \nLatititude: " + lat + "\nLongtitude: " + lng;
                    map.clear();
                    LatLng newLocation = new LatLng(lat, lng);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 11));

                    map.addMarker(new MarkerOptions().position(newLocation));
                    try {
                        String folder = getFilesDir().getAbsolutePath() + "/Locations";
                        File file = new File(folder, "locationData.txt");
                        FileWriter writer = new FileWriter(file, true);
                        writer.write(lat + " " + lng + "\n");
                        writer.flush();
                        writer.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    msg = "";
                }
                tv.setText(msg);
            }
        };

        folderLocation = getFilesDir().getAbsolutePath() + "/Locations";
        File folder = new File(folderLocation);
        if(folder.exists() == false) {
            boolean result = folder.mkdir();
            if(result) {
                Log.i("Folder", "Folder created");
            }
            else {
                Log.i("Folder", "Folder not created");
            }
        }

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;

                boolean permissionResult = checkPermission();
                if(permissionResult) {
                    client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            String msg;
                            if(location != null) {
                                msg = "Last known location: \nLatititude: " + location.getLatitude() + "\nLongtitude: " + location.getLongitude();
                                LatLng lastKnownLocation = new LatLng(location.getLatitude(),location.getLongitude());
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 11));

                                map.addMarker(new MarkerOptions().position(lastKnownLocation));
                                try {
                                    String folder = getFilesDir().getAbsolutePath() + "/Locations";
                                    File file = new File(folder, "locationData.txt");
                                    FileWriter writer = new FileWriter(file, true);
                                    writer.write(location.getLatitude() + " " + location.getLongitude() + "\n");
                                    writer.flush();
                                    writer.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                msg = "No last location found";
                            }
                            tv.setText(msg);
                        }
                    });
                }
                else {
                    Toast.makeText(MainActivity.this, "Permission denied",Toast.LENGTH_LONG).show();
                }

            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("btn1", "click");
                boolean permissionResult = checkPermission();
                if (permissionResult) {
                    mLocationRequest = LocationRequest.create();
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    mLocationRequest.setInterval(30);
                    mLocationRequest.setSmallestDisplacement(500);
                    client.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                }
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.removeLocationUpdates(mLocationCallback);
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MainActivity.this, CheckRecords.class);
                startActivity(i);
            }
        });
    }

    private boolean checkPermission() {
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            return false;
        }
    }
}