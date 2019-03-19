package se.ju.ralu18pz.petfinder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    // Public static variables
    public static String FOUND_COLLECTION = "Found";
    public static String LOST_COLLECTION = "Lost";
    public static String USER_CLASS = "Users";
    public static String PET_CLASS = "Pets";
    public static FirebaseAuth auth;
    public static FirebaseUser currentUser;


    private static final int REQUEST_LOCATION = 123;
    public static BottomNavigationView mainNav;
    private FrameLayout mainFrame;

    private HomeFragment homeFragment;
    private MapFragment mapFragment;
    private PetsFragment petsFragment;
    private ProfileFragment profileFragment;
    private DenyLocationPermissionFragment denyLocationPermissionFragment;
    private NoAuthorizationFragment noAuthorizationFragment;
    private UserFragment userFragment;
    private HomeAfterLoginFragment homeAfterLoginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        mainFrame = (FrameLayout) findViewById(R.id.main_frame);
        mainNav = (BottomNavigationView) findViewById(R.id.main_nav);

        homeFragment = new HomeFragment();
        mapFragment = new MapFragment();
        petsFragment = new PetsFragment();
        profileFragment = new ProfileFragment();
        noAuthorizationFragment = new NoAuthorizationFragment();
        userFragment = new UserFragment();
        homeAfterLoginFragment = new HomeAfterLoginFragment();


        if(currentUser == null) {
            setFragment(homeFragment);
        }
        else {
            setFragment(homeAfterLoginFragment);
        }

        mainNav.getMenu().getItem(0).setChecked(true);

        mainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                clearBackStack();
                switch (menuItem.getItemId()) {
                    case R.id.home_button:
                        if(currentUser == null) {
                            setFragment(homeFragment);
                        }
                        else {
                            setFragment(homeAfterLoginFragment);
                        }
                        return true;
                    case R.id.map_button:
                        verifyLocationPermission();
                        return true;
                    case R.id.pets_button:
                        if(currentUser == null) {
                            setFragment(noAuthorizationFragment);
                        }
                        else {
                            setFragment(petsFragment);
                        }
                        return true;
                    case R.id.profile_button:
                        if(currentUser == null) {
                            setFragment(profileFragment);
                        }
                        else {
                            setFragment(userFragment);
                        }
                        return true;
                        default:
                            return false;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    public void verifyLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        else {
            setFragment(mapFragment);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_LOCATION) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setFragment(mapFragment);
            }
            else {
                denyLocationPermissionFragment = new DenyLocationPermissionFragment();
                setFragment(denyLocationPermissionFragment);
            }
        }
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }

    private void clearBackStack() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = fragmentManager.getBackStackEntryAt(0);
            fragmentManager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }
}
