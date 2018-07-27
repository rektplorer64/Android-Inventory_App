package tanawinwichitcom.android.inventoryapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import tanawinwichitcom.android.inventoryapp.fragments.dialogfragment.CircularRevealFragment;
import tanawinwichitcom.android.inventoryapp.fragments.ItemProfileFragment;

public class ItemProfileContainerActivity extends AppCompatActivity{

    private int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_profile_container);

        Bundle receivedBundle = getIntent().getExtras();
        itemId = receivedBundle.getInt("itemId");

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        CircularRevealFragment itemProfileFragment
                = ItemProfileFragment.newInstance(R.layout.fragment_profile_item, itemId, 0, 0);

        fragmentTransaction.replace(R.id.itemProfileFragmentFrame, itemProfileFragment);
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_item_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_edit:{
                Intent intent = new Intent(this, ItemEditingContainerActivity.class);
                intent.putExtra("itemId", itemId);
                startActivity(intent);
            }
        }
        return true;
    }
}
