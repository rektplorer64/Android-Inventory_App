package tanawinwichitcom.android.inventoryapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.bumptech.glide.Glide;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Item;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.ItemViewModel;

import static tanawinwichitcom.android.inventoryapp.ColorUtility.darkenColor;

public class AddItemActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback{

    private LinearLayout linearLayoutEdit;

    private enum ActionCode{ADD_ITEM, UPDATE_ITEM}
    private int PICK_IMAGE_REQUEST = 1;
    private int REQUEST_PERMISSION = 1;

    private File originalImageFile;
    private boolean isInEditMode;

    private Window window;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private RelativeLayout imageHeaderRelativeLayout;
    private FloatingActionButton floatingActionButton;
    private TextInputLayout nameEditWrapper, quantityEditWrapper, descriptionEditWrapper;
    private EditText nameEditText, quantityEditText, descriptionEditText;
    private LinearLayout selectColorButton;
    private ImageButton circleImageView;
    private Toolbar toolbar;
    private ImageView itemImageView, nameIconImageView, descriptionIconImageView;

    static ArrayList<SelectableColor> integerArrayList;

    private ItemViewModel itemViewModel;

    @ColorInt
    private Integer selectedColorInt;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        initializeViews();

        Bundle bundle = getIntent().getExtras();
        // Persistence data source
        itemViewModel = ViewModelProviders.of(this).get(ItemViewModel.class);

        // If the bundle is not null, that means it is the edit mode
        isInEditMode = bundle != null;

        if(!isInEditMode){
            selectedColorInt = Color.parseColor(getResources().getString(R.color.md_red_400));
            circleImageView.setBackgroundColor(selectedColorInt);

            setUpStatusAndToolbar(selectedColorInt);
            setupDialogButton();
            setEditTextOnFocus(selectedColorInt);
            floatingActionButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    takeAction(ActionCode.ADD_ITEM, itemViewModel, null);
                }
            });
        }else{
            itemViewModel.getItemById(bundle.getInt("itemId")).observe(this, new Observer<Item>(){
                @Override
                public void onChanged(@Nullable final Item item){
                    originalImageFile = item.getImageFile();
                    selectedColorInt = item.getItemColorAccent();
                    circleImageView.setBackgroundColor(selectedColorInt);

                    setUpStatusAndToolbar(selectedColorInt);
                    setupDialogButton();
                    setEditTextOnFocus(selectedColorInt);
                    fillTextEditForm(item);
                    floatingActionButton.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            takeAction(ActionCode.UPDATE_ITEM, itemViewModel, item);
                        }
                    });
                }
            });
        }

        final Activity activity = this;
        imageHeaderRelativeLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                validatePermissionRequests(activity);
                //Toast.makeText(mContext, "I need Medic!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select a picture"), PICK_IMAGE_REQUEST);
            }
        });

    }

    private void setupDialogButton(){
        selectColorButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // ColorSelectorDialogFragment colorSelectorDialog = new ColorSelectorDialogFragment();
                // // Send the ArrayList to DialogFragment
                // colorSelectorDialog.putArguments(integerArrayList, circleImageView);
                //
                // // Shows the Dialogs
                // colorSelectorDialog.show(getSupportFragmentManager(), "Color Selector");

                new ColorChooserDialog.Builder(v.getContext(), R.string.color_palette)
                        .titleSub(R.string.colors)
                        .preselect(selectedColorInt)
                        .allowUserColorInputAlpha(false)
                        .dynamicButtonColor(false)
                        .show(getSupportFragmentManager());
            }
        });
    }

    private void initializeViews(){
        // Gets the Window in order to change Status Bar's Color
        window = getWindow();
        linearLayoutEdit = findViewById(R.id.linearLayoutEdit);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        imageHeaderRelativeLayout = findViewById(R.id.imageHeaderRelativeLayout);
        floatingActionButton = findViewById(R.id.fabConfirmAddItem);

        nameEditText = findViewById(R.id.nameEditText);
        nameEditWrapper = findViewById(R.id.nameEditWrapper);

        quantityEditText = findViewById(R.id.quantityEditText);
        quantityEditWrapper = findViewById(R.id.quantityEditWrapper);

        descriptionEditText = findViewById(R.id.descriptionEditText);
        descriptionEditWrapper = findViewById(R.id.descriptionEditWrapper);

        selectColorButton = findViewById(R.id.colorEditButton);

        circleImageView = findViewById(R.id.colorCircle);

        // Setting up the toolbar
        toolbar = findViewById(R.id.toolbar);

        itemImageView = findViewById(R.id.itemImageView);

        nameIconImageView = findViewById(R.id.nameIconImageView);
        descriptionIconImageView = findViewById(R.id.descriptionIconImageView);
    }

    private void setEditTextOnFocus(@ColorInt final int colorInt){
        final int darkerColorInt = colorInt;
        nameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                if(hasFocus){
                    nameIconImageView.setColorFilter(darkerColorInt);
                    nameIconImageView.setPressed(true);
                }else{
                    nameIconImageView.setColorFilter(Color.BLACK);
                    nameIconImageView.setPressed(false);
                }
            }
        });
        descriptionEditText.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                if(hasFocus){
                    descriptionIconImageView.setColorFilter(darkerColorInt);
                    descriptionIconImageView.setPressed(true);
                }else{
                    descriptionIconImageView.setColorFilter(Color.BLACK);
                    descriptionIconImageView.setPressed(false);
                }
            }
        });
        linearLayoutEdit.requestFocus();
    }

    private void fillTextEditForm(Item item){
        nameEditText.setText(item.getName());
        quantityEditText.setText(String.valueOf(item.getQuantity()));
        descriptionEditText.setText(item.getDescription());
        if(item.getImageFile() != null){
            Glide.with(this).load(item.getImageFile()).into(itemImageView);
        }
    }

    private void setUpStatusAndToolbar(@ColorInt int backColorInt){
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setSupportActionBar(toolbar);
        setTitle("Add an item");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });
        setSystemBarsColor(backColorInt);
    }

    public void takeAction(ActionCode actionCode, ItemViewModel itemViewModel, Item item){
        Date currentTime = Calendar.getInstance().getTime();

        if(quantityEditText.getText().toString().isEmpty() || nameEditText.getText().toString().isEmpty()
                || descriptionEditText.getText().toString().isEmpty()){
            if(quantityEditText.getText().toString().isEmpty()){
                nameEditWrapper.setError("Give it a name");
            }
            if(nameEditText.getText().toString().isEmpty()){
                quantityEditWrapper.setError("Enter a number");
            }
            if(descriptionEditText.getText().toString().isEmpty()){
                descriptionEditWrapper.setError("Give it a description or something");
            }
            return;
        }

        String itemName = nameEditText.getText().toString().trim();
        int quantity = 0;
        try{
            quantity = Integer.valueOf(quantityEditText.getText().toString());
        }catch(NumberFormatException e){
            quantityEditWrapper.setError("That number is too large.");
            e.printStackTrace();
            return;
        }
        String description = descriptionEditText.getText().toString().trim();

        String s = null;
        try{
            if(originalImageFile != null){
                s = getSelectedImageUrl(originalImageFile, currentTime.getTime(), item);
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        File imageFile = null;
        if(s != null){
            imageFile = new File(s);
        }

        if(actionCode == ActionCode.ADD_ITEM){
            itemViewModel.insert(new Item(itemName, quantity, description, selectedColorInt, "asdasd asdasd", imageFile, currentTime, null));
        }else if(actionCode == ActionCode.UPDATE_ITEM){
            item.setName(itemName);
            item.setQuantity(quantity);
            item.setDescription(description);
            item.setItemColorAccent(selectedColorInt);
            item.setImageFile(imageFile);
            item.setDateModified(currentTime);

            itemViewModel.update(item);
        }
        // if(originalImageFile != null){
        //     String s = null;
        //     try{
        //         s = getSelectedImageUrl(originalImageFile, currentTime.getTime(), item);
        //     }catch(IOException e){
        //         e.printStackTrace();
        //     }
        //     File imageFile = new File(s);
        //     if(actionCode == ActionCode.ADD_ITEM){
        //         // Stores all fields into a row in the database (Color is stored as a hex value)
        //         itemViewModel.insert(new Item(itemName, quantity, description, selectedColorInt, "asdasd asdasd", imageFile, currentTime, null));
        //     }else if(actionCode == ActionCode.UPDATE_ITEM){
        //         item.setName(itemName);
        //         item.setQuantity(quantity);
        //         item.setDescription(description);
        //         item.setItemColorAccent(selectedColorInt);
        //         item.setImageFile(imageFile);
        //         item.setDateModified(currentTime);
        //
        //         itemViewModel.update(item);
        //     }
        // }else{
        //     if(actionCode == ActionCode.ADD_ITEM){
        //         // Stores all fields into a row in the database (Color is stored as a hex value)
        //         itemViewModel.insert(new Item(itemName, quantity, description, selectedColorInt, "asdasd asdasd", null, currentTime, null));
        //     }else if(actionCode == ActionCode.UPDATE_ITEM){
        //
        //     }
        // }

        //startActivity(new Intent(mContext, MainActivity.class));
        finish();

    }

    /**
     * This method specifies what the activity should do when the DialogFragment is dismissed.
     * @param dialog color chooser dialog
     * @param selectedColorInt color integer
     */
    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int selectedColorInt){
        this.selectedColorInt = selectedColorInt;
        setSystemBarsColor(selectedColorInt);
        setEditTextOnFocus(selectedColorInt);
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog){

    }

    private void setSystemBarsColor(int selectedColorInt){
        int frontColorInt = ColorUtility.getSuitableFrontColor(this, selectedColorInt, true);
        window.setStatusBarColor(darkenColor(selectedColorInt));

        window.setNavigationBarColor(selectedColorInt);

        // Changes Toolbar's color according to the selected color
        toolbar.setBackgroundColor(selectedColorInt);
        toolbar.setTitleTextColor(frontColorInt);
        toolbar.getNavigationIcon().setTint(frontColorInt);
        collapsingToolbarLayout.setContentScrimColor(selectedColorInt);

        // Changes Navigation Icon (Back Arrow Icon)'s color
        toolbar.getNavigationIcon().setTint(frontColorInt);

        // Changes Square Color Icon's color according to the selected color
        circleImageView.setBackgroundColor(selectedColorInt);
    }

    private void validatePermissionRequests(Activity activity){
        // Here, thisActivity is the current activity
        if(ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED){

            // Permission is not granted
            // Should we show an explanation?
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)){
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            }else{
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        this.REQUEST_PERMISSION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){

            if(isInEditMode && originalImageFile != null){
                try{
                    FileUtils.forceDelete(originalImageFile);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }

            originalImageFile = new File(ImageUtil.getPathFromUri(getApplicationContext(), data.getData()));
            System.out.println("getPathFromUri() : " + ImageUtil.getPathFromUri(getApplicationContext(), data.getData()));
            Glide.with(getApplicationContext()).load(originalImageFile).into(itemImageView);

            // String fileName =
            // FileUtils.copyDirectory(originalFile, new File(this.getFilesDir().toURI().getPath() + ""));
            // System.out.println();
            //TODO: Don't copy the selected file yet. Wait until user press the FAB.
        }
    }

    private String getSelectedImageUrl(File originalImageFile, long timestamp, Item item) throws IOException{
        String internalStoragePath = this.getFilesDir().toURI().getPath();
        String fileName = String.valueOf(timestamp) + ".jpg";

        File newFile = new File(internalStoragePath + fileName);
        FileUtils.copyFile(originalImageFile, newFile);
        return internalStoragePath + fileName;
    }

    public static class SelectableColor{
        private Integer colorId;
        private Boolean isSelected;

        public SelectableColor(Integer colorId){
            this.colorId = colorId;
            this.isSelected = false;
        }

        public SelectableColor(Integer colorId, Boolean b){
            this.colorId = colorId;
            this.isSelected = b;
        }

        public Integer getColorId(){
            return colorId;
        }

        public void setColorId(Integer colorId){
            this.colorId = colorId;
        }

        public Boolean getSelected(){
            return isSelected;
        }

        public void setSelected(Boolean selected){
            isSelected = selected;
        }

        @Override
        public boolean equals(Object o){
            if(this == o){
                return true;
            }
            if(o == null || getClass() != o.getClass()){
                return false;
            }
            SelectableColor that = (SelectableColor) o;
            return Objects.equals(colorId, that.colorId);
        }

        @Override
        public int hashCode(){
            return Objects.hash(colorId);
        }

        public static SelectableColor copy(SelectableColor s){
            return new SelectableColor(s.getColorId(), s.getSelected());
        }

        public static ArrayList<SelectableColor> copyArrayList(ArrayList<SelectableColor> colorArrayList){
            ArrayList<SelectableColor> selectableColorArrayList = new ArrayList<>();
            for(SelectableColor s : colorArrayList){
                selectableColorArrayList.add(copy(s));
            }
            return selectableColorArrayList;
        }
    }

    public static Integer[] predefinedColorsResourceIDs = {R.color.md_red_400, R.color.md_pink_400,
                                                           R.color.md_purple_400,
                                                           R.color.md_deeppurple_400,
                                                           R.color.md_indigo_400,
                                                           R.color.md_blue_400,
                                                           R.color.md_lightblue_400,
                                                           R.color.md_cyan_400, R.color.md_teal_400,
                                                           R.color.md_green_400,
                                                           R.color.md_lightgreen_400,
                                                           R.color.md_lime_400,
                                                           R.color.md_yellow_400,
                                                           R.color.md_amber_400,
                                                           R.color.md_orange_400,
                                                           R.color.md_deeporange_400,
                                                           R.color.md_brown_400,
                                                           R.color.md_gray_400,
                                                           R.color.md_bluegray_400};
}
