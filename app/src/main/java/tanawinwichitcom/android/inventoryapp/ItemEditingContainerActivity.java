package tanawinwichitcom.android.inventoryapp;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.FrameLayout;

public class ItemEditingContainerActivity extends AppCompatActivity{

    private FrameLayout itemEditingFrameLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_editing_container);
        itemEditingFrameLayout = findViewById(R.id.itemEditingFrameLayout);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        Bundle bundle = getIntent().getExtras();
        int itemId = 0;
        boolean inEditMode = false;
        if(bundle != null){
            itemId = bundle.getInt("itemId");
            inEditMode = bundle.getBoolean("inEditMode", false);
        }

        ItemEditingFragment itemEditingFragment = ItemEditingFragment.newInstance(itemId, inEditMode);
        ft.replace(itemEditingFrameLayout.getId(), itemEditingFragment).commit();
    }
}
