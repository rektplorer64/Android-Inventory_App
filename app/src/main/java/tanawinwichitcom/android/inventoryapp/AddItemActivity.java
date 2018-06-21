package tanawinwichitcom.android.inventoryapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import tanawinwichitcom.android.inventoryapp.DialogFragments.ColorSelectorDialogFragment;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Item;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.ItemViewModel;

import static tanawinwichitcom.android.inventoryapp.DialogFragments.ColorSelectorDialogFragment.darkenColor;

public class AddItemActivity extends AppCompatActivity implements DialogInterface.OnDismissListener{

    private enum ActionCode{ADD_ITEM, UPDATE_ITEM}
    private int PICK_IMAGE_REQUEST = 1;
    private int REQUEST_PERMISSION = 1;

    private File originalImageFile;
    private boolean isInEditMode;

    private Window window;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FloatingActionButton floatingActionButton;
    private TextInputLayout nameEditWrapper, quantityEditWrapper, descriptionEditWrapper;
    private EditText nameEditText, quantityEditText, descriptionEditText;
    private Button selectColorButton;
    private ImageButton circleImageView, selectImageButton;
    private Toolbar toolbar;
    private ImageView itemImageView;

    static ArrayList<SelectableColor> integerArrayList;

    private ItemViewModel itemViewModel;

    final Context mContext = this;

    @ColorInt
    private Integer starterColorInt;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        initializeViews();

        Bundle bundle = getIntent().getExtras();
        // Persistence data source
        itemViewModel = ViewModelProviders.of(this).get(ItemViewModel.class);

        isInEditMode = bundle != null;

        if(!isInEditMode){
            starterColorInt = Color.parseColor(getResources().getString(R.color.md_red_400));
            circleImageView.setBackgroundColor(starterColorInt);

            int frontColorInteger = (Color.red(starterColorInt) + Color.green(starterColorInt)
                    + Color.blue(starterColorInt) >= 383) ? Color.BLACK : Color.WHITE;

            setUpStatusAndToolbar(starterColorInt, frontColorInteger);
            setupDialogButton(null, this);

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
                    starterColorInt = item.getItemColorAccent();
                    circleImageView.setBackgroundColor(starterColorInt);
                    int frontColorInteger = (Color.red(starterColorInt) + Color.green(starterColorInt)
                            + Color.blue(starterColorInt) >= 383) ? Color.BLACK : Color.WHITE;

                    setUpStatusAndToolbar(starterColorInt, frontColorInteger);
                    setupDialogButton(item, getApplicationContext());

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
        selectImageButton.setOnClickListener(new View.OnClickListener(){
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

    private void setupDialogButton(Item item, Context context){
        setSelectableColor(item, context);
        selectColorButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ColorSelectorDialogFragment colorSelectorDialog = new ColorSelectorDialogFragment();
                // Send the ArrayList to DialogFragment
                colorSelectorDialog.putArguments(integerArrayList, circleImageView);

                // Shows the Dialogs
                colorSelectorDialog.show(getSupportFragmentManager(), "Color Selector");
            }
        });
    }

    private void initializeViews(){
        // Gets the Window in order to change Status Bar's Color
        window = getWindow();
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        floatingActionButton = findViewById(R.id.fabConfirmAddItem);

        nameEditText = findViewById(R.id.nameEditText);
        nameEditWrapper = findViewById(R.id.nameEditWrapper);

        quantityEditText = findViewById(R.id.quantityEditText);
        quantityEditWrapper = findViewById(R.id.quantityEditWrapper);

        descriptionEditText = findViewById(R.id.descriptionEditText);
        descriptionEditWrapper = findViewById(R.id.descriptionEditWrapper);

        selectColorButton = findViewById(R.id.colorEditButton);

        circleImageView = findViewById(R.id.colorCircle);
        selectImageButton = findViewById(R.id.selectImageButton);

        // Setting up the toolbar
        toolbar = findViewById(R.id.toolbar);

        itemImageView = findViewById(R.id.itemImageView);
    }

    private void fillTextEditForm(Item item){
        nameEditText.setText(item.getName());
        quantityEditText.setText(String.valueOf(item.getQuantity()));
        descriptionEditText.setText(item.getDescription());
        Glide.with(this).load(item.getImageFile()).into(itemImageView);
    }

    private void setUpStatusAndToolbar(@ColorInt int backColorInt, @ColorInt int frontColorInt){
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(darkenColor(backColorInt));

        setSupportActionBar(toolbar);
        setTitle("Add an item");
        toolbar.setBackgroundColor(backColorInt);
        toolbar.setTitleTextColor(frontColorInt);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.getNavigationIcon().setTint(frontColorInt);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });

        collapsingToolbarLayout.setContentScrimColor(backColorInt);
    }

    public static int getSelectedColor(Context context){
        SelectableColor selectedColor = null;
        for(SelectableColor color : integerArrayList){
            if(color.getSelected()){
                selectedColor = color;
                break;
            }
        }

        String colorString;
        System.out.println("getSelectedColor(); selectedColor = " + selectedColor);
        if(selectedColor == null){
            colorString = context.getString(integerArrayList.get(0).getColorId());     /* Gets Hex Color from the first color in the pre-defined ArrayList */
        }else{
            colorString = context.getResources().getString(selectedColor.getColorId());     /* Gets Hex Color from given resource id */
        }

        return Color.parseColor(colorString);      /* Decode Hex String into a Color integer */
    }

    public void takeAction(ActionCode actionCode, ItemViewModel itemViewModel, Item item){
        Date dateCreated = Calendar.getInstance().getTime();
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
        }else{
            String itemName = nameEditText.getText().toString().trim();
            int quantity = Integer.valueOf(quantityEditText.getText().toString());
            String description = descriptionEditText.getText().toString().trim();

            if(originalImageFile != null){
                try{
                    String s = getSelectedImageUrl(originalImageFile, dateCreated.getTime(), item);
                    System.out.println(s);

                    File imageFile = new File(s);

                    if(actionCode == ActionCode.ADD_ITEM){
                        // Stores all fields into a row in the database (Color is stored as a hex value)
                        System.out.println("Inserting a new item");
                        itemViewModel.insert(new Item(itemName, quantity, description, getSelectedColor(mContext), "asdasd asdasd", imageFile, dateCreated, null));
                    }else if(actionCode == ActionCode.UPDATE_ITEM){
                        System.out.println("Updating a new item");

                        item.setName(itemName);
                        item.setQuantity(quantity);
                        item.setDescription(description);
                        item.setItemColorAccent(getSelectedColor(mContext));
                        item.setImageFile(imageFile);
                        item.setDateModified(dateCreated);

                        itemViewModel.update(item);
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }else{
                if(actionCode == ActionCode.ADD_ITEM){
                    // Stores all fields into a row in the database (Color is stored as a hex value)
                    itemViewModel.insert(new Item(itemName, quantity, description, getSelectedColor(mContext), "asdasd asdasd", null, dateCreated, null));
                }
            }

            //startActivity(new Intent(mContext, MainActivity.class));
            finish();
        }
    }

    public void setSelectableColor(Item item, Context context){
        // Prepares Color IDs to be ready by converting them into an ArrayList of SelectableColor
        integerArrayList = new ArrayList<>();
        for(Integer colorId : predefinedColorsResourceIDs){
            integerArrayList.add(new SelectableColor(colorId));
        }
        if(item == null){
            integerArrayList.get(0).setSelected(true);
        }else{
            for(SelectableColor s : integerArrayList){
                if(Color.parseColor(context.getString(s.getColorId())) == item.getItemColorAccent()){
                    s.setSelected(true);
                    break;
                }
            }
        }
    }

    /**
     * This method specifies what the activity should do when the DialogFragment is dismissed.
     *
     * @param dialog dialog
     */
    @Override
    public void onDismiss(DialogInterface dialog){
        System.out.println("ConfirmButton Triggered: Changing Color to " + AddItemActivity.getSelectedColor(mContext));
        int backgroundColor = AddItemActivity.getSelectedColor(mContext);
        int frontColor = (Color.red(backgroundColor) + Color.green(backgroundColor)
                + Color.blue(backgroundColor) >= 383) ? Color.BLACK : Color.WHITE;

        collapsingToolbarLayout.setContentScrimColor(backgroundColor);

        // Changes Square Color Icon's color according to the selected color
        circleImageView.setBackgroundColor(backgroundColor);

        // Changes Toolbar's color according to the selected color
        toolbar.setBackgroundColor(backgroundColor);
        toolbar.setTitleTextColor(frontColor);

        // Changes Navigation Icon (Back Arrow Icon)'s color
        toolbar.getNavigationIcon().setTint(frontColor);

        // Changes Status bar's color according to the selected color
        window.setStatusBarColor(darkenColor(backgroundColor));
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

            if(isInEditMode){
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
