package tanawinwichitcom.android.inventoryapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import tanawinwichitcom.android.inventoryapp.RecycleViewAdapters.ItemAdapter;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Item;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Review;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.User;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.ItemViewModel;

public class MainActivity extends AppCompatActivity{

    private ItemViewModel itemViewModel;

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private FloatingActionButton fab;
    private Toolbar toolbar;

    // KEY for SHARED_PREFERENCE (ID of the User who currently login)
    public static final String SharedPref_LOGIN_SESSION_DATA = "SharedPref_LOGIN_SESSION_DATA";
    public static final String SharedPrefKey_LOGIN_SESSION_USER_ID = "SharedPrefKey_LOGIN_SESSION_USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startActivity(new Intent(MainActivity.this, AddItemActivity.class));
            }
        });

        itemViewModel = ViewModelProviders.of(this).get(ItemViewModel.class);

        recyclerView = findViewById(R.id.itemsList);
        itemAdapter = new ItemAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(itemAdapter);

        itemViewModel.getAllItems().observe(MainActivity.this, new Observer<List<Item>>(){
            @Override
            public void onChanged(@Nullable List<Item> items){
                itemAdapter.applyItemDataChanges(items);
            }
        });

        itemViewModel.getAllReviews().observe(MainActivity.this, new Observer<List<Review>>(){
            @Override
            public void onChanged(@Nullable List<Review> reviews){
                if(reviews != null){
                    itemAdapter.applyReviewDataChanges(ItemViewModel.convertReviewListToSparseArray(reviews));
                }
            }
        });

        //loadDatabase(itemViewModel);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_settings){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void simulateLoginSession(User user){
        SharedPreferences loginData = getSharedPreferences(SharedPref_LOGIN_SESSION_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = loginData.edit();
        prefEditor.putInt(SharedPrefKey_LOGIN_SESSION_USER_ID, user.getId());
        prefEditor.apply();

        int loggedID = getSharedPreferences(MainActivity.SharedPref_LOGIN_SESSION_DATA, Context.MODE_PRIVATE)
                .getInt(SharedPrefKey_LOGIN_SESSION_USER_ID, -1);
        String toastMsg = "Logged in as: ID#" + loggedID;
        Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
    }

}
