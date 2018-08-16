package io.rektplorer.inventoryapp;


import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import io.rektplorer.inventoryapp.fragments.ItemEditingFragment;

public class ItemEditingContainerActivity extends AppCompatActivity{

    private FrameLayout itemEditingFrameLayout;
    private ItemEditingFragment itemEditingFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_editing_container);
        itemEditingFrameLayout = findViewById(R.id.itemEditingFrameLayout);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        Bundle bundle = getIntent().getExtras();
        int itemId = 0;

        if(bundle != null){
            itemId = bundle.getInt("itemId");
        }

        if(savedInstanceState == null){
            itemEditingFragment = ItemEditingFragment.newInstance(itemId);
        }else{
            itemEditingFragment = (ItemEditingFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, ConstantsHolder.FRAGMENT_ITEM_EDIT);
        }
        ft.replace(itemEditingFrameLayout.getId(), itemEditingFragment,
                   ConstantsHolder.FRAGMENT_ITEM_EDIT).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        getSupportFragmentManager()
                .putFragment(outState, ConstantsHolder.FRAGMENT_ITEM_EDIT, itemEditingFragment);
    }
}
