package tanawinwichitcom.android.inventoryapp.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;
import tanawinwichitcom.android.inventoryapp.ItemEditingContainerActivity;
import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.roomdatabase.DataRepository;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;
import tanawinwichitcom.android.inventoryapp.roomdatabase.ItemViewModel;
import tanawinwichitcom.android.inventoryapp.utility.ColorUtility;
import tanawinwichitcom.android.inventoryapp.utility.ImageUtility;

import static android.app.Activity.RESULT_OK;
import static tanawinwichitcom.android.inventoryapp.utility.ColorUtility.darkenColor;

public class ItemEditingFragment extends Fragment implements ColorChooserDialog.ColorCallback{

    private int PICK_IMAGE_REQUEST = 1;
    private int REQUEST_PERMISSION = 1;

    private File originalImageFile;

    private boolean isInEditMode;

    private Window window;

    private LinearLayout linearLayoutEdit;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    private TextInputLayout nameEditWrapper, quantityEditWrapper, descriptionEditWrapper;
    private TextInputEditText nameEditText;
    private EditText quantityEditText, descriptionEditText;
    private LinearLayout selectColorButton;
    private ImageButton circleImageView, deleteImageButton;
    private Toolbar toolbar;
    private ImageView itemImageView, nameIconImageView, descriptionIconImageView;
    private ItemViewModel itemViewModel;

    private ChipGroup tagChipGroup;
    private TextInputLayout tagEditWrapper;
    private AutoCompleteTextView tagEditText;

    private OnConfirmListener onConfirmListener;

    @ColorInt
    private Integer selectedColorInt;

    private ArrayAdapter<String> suggestionAdapter;

    public ItemEditingFragment(){
        setHasOptionsMenu(true);
    }

    public static ItemEditingFragment newInstance(int itemId, boolean isInEditMode){
        Bundle args = new Bundle();
        args.putInt("itemId", itemId);
        args.putBoolean("inEditMode", isInEditMode);
        ItemEditingFragment fragment = new ItemEditingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.activity_add_item, container, false);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        // super.onCreate(savedInstanceState);

        initializeViews(view);

        Bundle bundle = getArguments();
        // Persistence data source
        itemViewModel = ViewModelProviders.of(this).get(ItemViewModel.class);

        // If the bundle is not null, that means it is the edit mode
        isInEditMode = bundle.getBoolean("inEditMode");

        setupTagEditor();
        if(!isInEditMode){
            selectedColorInt = Color.parseColor(getResources().getString(R.color.md_red_400));
            circleImageView.setBackgroundColor(selectedColorInt);

            setupStatusAndToolbar(selectedColorInt);
            setupDialogButton();
            setEditTextOnFocus(selectedColorInt);

            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener(){
                @Override
                public boolean onMenuItemClick(MenuItem menuItem){
                    switch(menuItem.getItemId()){
                        case R.id.action_confirm_edit:
                            takeAction(ActionMode.ADD_ITEM, itemViewModel, null);
                            break;
                    }
                    return true;
                }
            });
        }else{
            itemViewModel.getItemById(bundle.getInt("itemId")).observe(this, new Observer<Item>(){
                @Override
                public void onChanged(@Nullable final Item item){
                    originalImageFile = item.getImageFile();
                    if(originalImageFile != null){
                        deleteImageButton.setVisibility(View.VISIBLE);
                    }
                    selectedColorInt = item.getItemColorAccent();
                    circleImageView.setBackgroundColor(selectedColorInt);

                    setupStatusAndToolbar(selectedColorInt);
                    setupDialogButton();
                    setEditTextOnFocus(selectedColorInt);
                    fillDataToForms(item);
                    for(int i = 0; i < suggestionAdapter.getCount(); i++){
                        if(item.getTags().contains(suggestionAdapter.getItem(i))){
                            suggestionAdapter.remove(suggestionAdapter.getItem(i));
                        }
                    }

                    toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener(){
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem){
                            switch(menuItem.getItemId()){
                                case R.id.action_confirm_edit:
                                    takeAction(ActionMode.UPDATE_ITEM, itemViewModel, item);
                                    break;
                            }
                            return true;
                        }
                    });
                }
            });
        }

        final Activity activity = getActivity();
        itemImageView.setOnClickListener(new View.OnClickListener(){
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

    private void initializeViews(View view){
        // Gets the Window in order to change Status Bar's Color
        window = getActivity().getWindow();
        linearLayoutEdit = view.findViewById(R.id.linearLayoutEdit);
        collapsingToolbarLayout = view.findViewById(R.id.collapsing_toolbar);

        deleteImageButton = view.findViewById(R.id.deleteImageButton);
        deleteImageButton.setVisibility(View.GONE);
        deleteImageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View view){
                new MaterialDialog.Builder(view.getContext()).title("Clear item image?")
                        .positiveText("yes")
                        .negativeText("no")
                        .theme(Theme.LIGHT)
                        .onPositive(new MaterialDialog.SingleButtonCallback(){
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which){
                                deleteImageButton.setVisibility(View.GONE);
                                originalImageFile = null;
                                Glide.with(view.getContext())
                                        .load(R.drawable.ic_launcher_background)
                                        .into(itemImageView);
                            }
                        }).show();
            }
        });

        nameEditText = view.findViewById(R.id.nameEditText);
        nameEditWrapper = view.findViewById(R.id.nameEditWrapper);

        quantityEditText = view.findViewById(R.id.quantityEditText);
        quantityEditWrapper = view.findViewById(R.id.quantityEditWrapper);

        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        descriptionEditWrapper = view.findViewById(R.id.descriptionEditWrapper);

        selectColorButton = view.findViewById(R.id.colorEditButton);

        circleImageView = view.findViewById(R.id.colorCircle);

        // Setting up the toolbar
        toolbar = view.findViewById(R.id.toolbar);

        itemImageView = view.findViewById(R.id.itemImageView);

        nameIconImageView = view.findViewById(R.id.nameIconImageView);
        descriptionIconImageView = view.findViewById(R.id.descriptionIconImageView);

        tagChipGroup = view.findViewById(R.id.tagChipGroup);
        // tagEditWrapper = view.findViewById(R.id.tagEditWrapper);
        tagEditText = view.findViewById(R.id.tagEditText);
    }

    private void setupTagEditor(){
        suggestionAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        suggestionAdapter.addAll(itemViewModel.getAllTags());
        suggestionAdapter.setNotifyOnChange(true);

        tagEditText.setAdapter(suggestionAdapter);
        tagEditText.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent){
                if(actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                                keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                    Toasty.info(getContext(), "Enter pressed").show();
                    if(!textView.getText().toString().isEmpty()){
                        createNewChip(textView.getText().toString(), false);
                        textView.setText("");
                        return true;
                    }
                }
                return false;
            }
        });
        tagEditText.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l){
                createNewChip(adapterView.getAdapter().getItem(position).toString(), false);
                tagEditText.setText("");
            }
        });
    }

    private void createNewChip(String newTag, boolean fillingData){
        if(tagChipGroup != null && !fillingData){
            for(int i = 0; i < tagChipGroup.getChildCount(); i++){
                if(((Chip) tagChipGroup.getChildAt(i)).getText().toString().equalsIgnoreCase(newTag)){
                    Toasty.warning(getContext(), "This tag is already added!").show();
                    return;
                }
            }
        }

        final Chip newChip = new Chip(getContext());
        newChip.setText(newTag);
        newChip.setCloseIconEnabled(true);
        newChip.setOnCloseIconClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                tagChipGroup.removeView(newChip);
            }
        });
        tagChipGroup.addView(newChip);
    }

    private void setupDialogButton(){
        selectColorButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new ColorChooserDialog.Builder(v.getContext(), R.string.color_palette)
                        .titleSub(R.string.colors)
                        .preselect(selectedColorInt)
                        .allowUserColorInputAlpha(false)
                        .dynamicButtonColor(false)
                        .show(getChildFragmentManager());
            }
        });
    }

    private void setSystemBarsColor(int selectedColorInt){
        int frontColorInt = ColorUtility.getSuitableFrontColor(getContext(), selectedColorInt, true);
        if(getActivity() instanceof ItemEditingContainerActivity){
            window.setStatusBarColor(darkenColor(selectedColorInt));
            window.setNavigationBarColor(selectedColorInt);
        }

        // Changes Toolbar's color according to the selected color
        toolbar.setBackgroundColor(selectedColorInt);
        toolbar.setTitleTextColor(frontColorInt);
        toolbar.getNavigationIcon().setTint(frontColorInt);

        collapsingToolbarLayout.setContentScrimColor(selectedColorInt);

        // Changes Navigation Icon (Back Arrow Icon)'s color
        toolbar.getMenu().clear();
        if(frontColorInt != Color.rgb(0, 0, 0)){
            toolbar.inflateMenu(R.menu.menu_item_editor);
        }else{
            toolbar.inflateMenu(R.menu.menu_item_editor_dark);
        }

        // Changes Square Color Icon's color according to the selected color
        circleImageView.setBackgroundColor(selectedColorInt);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        if(getActivity() instanceof ItemEditingContainerActivity){
            inflater.inflate(R.menu.menu_item_editor, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setupStatusAndToolbar(@ColorInt int backColorInt){
        if(getActivity() instanceof ItemEditingContainerActivity){
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            activity.setTitle((!isInEditMode) ? "Add an item" : "Edit an item");

            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);

            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }else{
            // TODO: Define toolbar behavior outside container activity
            toolbar.setTitle((!isInEditMode) ? "Add an item" : "Edit an item");
            toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        }
        setSystemBarsColor(backColorInt);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new MaterialDialog.Builder(v.getContext()).title("Cancel editing?")
                        .positiveText("Yes")
                        .negativeText("no")
                        .theme(Theme.LIGHT)
                        .onPositive(new MaterialDialog.SingleButtonCallback(){
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which){
                                if(getActivity() instanceof ItemEditingContainerActivity){
                                    getActivity().finish();
                                }else{
                                    getActivity().getSupportFragmentManager().beginTransaction()
                                            .remove(getParentFragment()).commit();
                                }
                            }
                        }).show();
            }
        });

    }

    private void fillDataToForms(Item item){
        nameEditText.setText(item.getName());
        quantityEditText.setText(String.valueOf(item.getQuantity()));
        descriptionEditText.setText(item.getDescription());
        if(item.getImageFile() != null){
            Glide.with(this).load(item.getImageFile()).into(itemImageView);
        }

        ArrayList<String> sortedTagList = new ArrayList<>(item.getTags());
        Collections.sort(sortedTagList, new Comparator<String>(){
            @Override
            public int compare(String s1, String s2){
                return s1.compareToIgnoreCase(s2);
            }
        });
        for(String tag : sortedTagList){
            createNewChip(tag, true);
        }
    }

    /**
     * Handles item interaction (Insertion or Updating)
     *
     * @param actionMode    a mode, either Insert or Update
     * @param itemViewModel to providing data
     * @param item          is null, if adding a new item. Otherwise, update it.
     */
    public void takeAction(ActionMode actionMode, ItemViewModel itemViewModel, Item item){
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
        long quantity;
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
                s = getSelectedImageUrl(originalImageFile, currentTime.getTime());
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        File imageFile = null;
        if(s != null){
            imageFile = new File(s);
        }

        Set<String> tagSet = new HashSet<>();
        for(int i = 0; i < tagChipGroup.getChildCount(); i++){
            tagSet.add(((Chip) tagChipGroup.getChildAt(i)).getText().toString());
        }

        if(actionMode == ActionMode.ADD_ITEM){
            itemViewModel.insert(new Item(itemName, quantity, description, selectedColorInt, tagSet, imageFile, currentTime, null));
        }else if(actionMode == ActionMode.UPDATE_ITEM){
            item.setName(itemName);
            item.setQuantity(quantity);
            item.setDescription(description);
            item.setItemColorAccent(selectedColorInt);

            item.setImageFile(imageFile);
            item.setDateModified(currentTime);
            item.setTags(tagSet);
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
        //     if(actionMode == ActionMode.ADD_ITEM){
        //         // Stores all fields into a row in the database (Color is stored as a hex value)
        //         itemViewModel.insert(new Item(itemName, quantity, description, selectedColorInt, "asdasd asdasd", imageFile, currentTime, null));
        //     }else if(actionMode == ActionMode.UPDATE_ITEM){
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
        //     if(actionMode == ActionMode.ADD_ITEM){
        //         // Stores all fields into a row in the database (Color is stored as a hex value)
        //         itemViewModel.insert(new Item(itemName, quantity, description, selectedColorInt, "asdasd asdasd", null, currentTime, null));
        //     }else if(actionMode == ActionMode.UPDATE_ITEM){
        //
        //     }
        // }

        //startActivity(new Intent(mContext, MainActivity.class));

        if(onConfirmListener != null){
            if(isInEditMode){
                onConfirmListener.onConfirm(item.getId());
            }else{
                onConfirmListener.onConfirm(itemViewModel.getItemDomainValue(DataRepository.ENTITY_ITEM, DataRepository.MAX_VALUE, DataRepository.ITEM_FIELD_ID));
            }
        }

        if(getActivity() instanceof ItemEditingContainerActivity){
            getActivity().finish();
        }
    }

    /**
     * This method specifies what the activity should do when the DialogFragment is dismissed.
     *
     * @param dialog           color chooser dialog
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

    /**
     * Receives selected file from image intent
     *
     * @param requestCode intent request code
     * @param resultCode  intent result code
     * @param data        the image that user has selected
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                    && data != null && data.getData() != null){

                // If received image is not null

                // If currently editing, and there is image.
                if(isInEditMode && originalImageFile != null){
                    try{
                        // Delete current image
                        FileUtils.forceDelete(originalImageFile);
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }

                // Gets select image
                originalImageFile = new File(ImageUtility.getPathFromUri(getContext(), data.getData()));
                // System.out.println("getPathFromUri() : " + ImageUtility.getPathFromUri(getContext(), data.getData()));

                // If new image has been selected, show delete image button
                deleteImageButton.setVisibility(View.VISIBLE);

                // Shows image to item image
                Glide.with(getContext()).load(originalImageFile).into(itemImageView);

                // String fileName =
                // FileUtils.copyDirectory(originalFile, new File(this.getFilesDir().toURI().getPath() + ""));
                // System.out.println();
                //TODO: Don't copy the selected file yet. Wait until user press the FAB.
            }
        }catch(SecurityException e){
            Toast.makeText(getContext(), "Exception throwed: Storage Permission Denied", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
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

    /**
     * Copies Selected Image into Internal storage (The new image from copying will be named by unix time)
     *
     * @param originalImageFile selected image
     * @param timestamp         for file naming
     *
     * @return path of the new file
     *
     * @throws IOException in case of file error
     */
    private String getSelectedImageUrl(File originalImageFile, long timestamp) throws IOException{
        // Internal storage path
        String internalStoragePath = getActivity().getFilesDir().toURI().getPath();

        // New file's name using unix time stamp with .jpg extension
        String fileName = String.valueOf(timestamp) + ".jpg";

        // Creates new instance of file with TIMESTAMP.jpg located in internal storage
        String newFileDirectory = internalStoragePath + fileName;
        File newFile = new File(newFileDirectory);
        // Copies file
        FileUtils.copyFile(originalImageFile, newFile);
        return newFileDirectory;
    }

    private enum ActionMode{ADD_ITEM, UPDATE_ITEM}

    public void setOnConfirmListener(OnConfirmListener onConfirmListener){
        this.onConfirmListener = onConfirmListener;
    }

    public interface OnConfirmListener{
        void onConfirm(int itemId);
    }
}
