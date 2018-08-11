package io.rektplorer.inventoryapp.fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import es.dmoral.toasty.Toasty;
import io.rektplorer.inventoryapp.ItemEditingContainerActivity;
import io.rektplorer.inventoryapp.R;
import io.rektplorer.inventoryapp.databinding.FragBodyItemEditBinding;
import io.rektplorer.inventoryapp.databinding.FragItemEditBinding;
import io.rektplorer.inventoryapp.roomdatabase.DataRepository;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Image;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Item;
import io.rektplorer.inventoryapp.roomdatabase.ItemViewModel;
import io.rektplorer.inventoryapp.rvadapters.ItemImageAdapter;
import io.rektplorer.inventoryapp.rvadapters.item.multiselectutil.MyItemDetailsLookup;
import io.rektplorer.inventoryapp.utility.ColorUtility;
import io.rektplorer.inventoryapp.utility.ImageUtility;

import static android.app.Activity.RESULT_OK;
import static io.rektplorer.inventoryapp.utility.ColorUtility.darkenColor;

public class ItemEditingFragment extends Fragment implements ColorChooserDialog.ColorCallback, Observer<Item>{

    private int PICK_IMAGE_REQUEST = 1;
    private int REQUEST_PERMISSION = 1;

    private boolean isInEditMode;

    private FragItemEditBinding binding;
    private FragBodyItemEditBinding editBinding;

    private Window window;

    private ItemViewModel itemViewModel;

    private OnConfirmListener onConfirmListener;

    private ItemImageAdapter itemImageAdapter;

    @ColorInt
    private Integer selectedColorInt;

    private ArrayAdapter<String> suggestionAdapter;
    private SelectionTracker selectionTracker;

    private MutableLiveData<List<Image>> tempImagesLiveData;
    private MediatorLiveData<List<Image>> displayingImagesLiveData;
    private Item item;

    public ItemEditingFragment(){
        setHasOptionsMenu(true);
    }

    /**
     * The instantiate ItemEditingFragment
     *
     * @param itemId Item Id
     *
     * @return a new ItemEditingFragment instance
     */
    public static ItemEditingFragment newInstance(int itemId){
        Bundle args = new Bundle();
        args.putInt("itemId", itemId);
        ItemEditingFragment fragment = new ItemEditingFragment();
        fragment.setArguments(args);
        return fragment;
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
        setupSystemUiColor(selectedColorInt);
        setupTextFieldOnFocusAppearances(selectedColorInt);
    }

    /**
     * Sets system's status bar color and navigation bar color
     *
     * @param colorInteger color integer
     */
    private void setupSystemUiColor(@ColorInt int colorInteger){
        int frontColorInt = ColorUtility.getSuitableFrontColor(getContext(), colorInteger, true);
        if(getActivity() instanceof ItemEditingContainerActivity){
            window.setStatusBarColor(darkenColor(colorInteger));
            window.setNavigationBarColor(colorInteger);
        }

        // Changes Toolbar's color according to the selected color
        binding.toolbar.setBackgroundColor(colorInteger);
        binding.toolbar.setTitleTextColor(frontColorInt);
        binding.toolbar.getNavigationIcon().setTint(frontColorInt);

        // If collapsing toolbar is in layout
        if(binding.collapsingToolbar != null){
            binding.collapsingToolbar.setContentScrimColor(colorInteger);
        }

        // Changes Toolbar Icon (Back Arrow Icon and others)'s color
        binding.toolbar.getMenu().clear();
        if(frontColorInt != Color.rgb(0, 0, 0)){    // If front color is white
            binding.toolbar.inflateMenu(R.menu.menu_item_editor);       // Use white icons
        }else{
            binding.toolbar.inflateMenu(R.menu.menu_item_editor_dark);      // Use dark icons
        }

        // If ScrollView is in layout
        if(binding.editNestedScrollView != null){
            // Darken nested scroll color
            binding.editNestedScrollView
                    .setBackgroundColor(ColorUtility.darkenColor(colorInteger, 0.75f));
        }

        // Changes Square Color Icon's color according to the selected color
        editBinding.colorCircle.setBackgroundColor(colorInteger);
    }

    /**
     * Setups Field icons behavior when corresponding TextField is focused
     *
     * @param colorInt color integer
     */
    private void setupTextFieldOnFocusAppearances(@ColorInt final int colorInt){
        editBinding.nameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                editBinding.nameIconImageView.setColorFilter((hasFocus) ? colorInt : Color.BLACK);
                editBinding.nameIconImageView.setPressed(hasFocus);
            }
        });
        editBinding.descriptionEditText.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                editBinding.descriptionIconImageView.setColorFilter((hasFocus) ? colorInt : Color.BLACK);
                editBinding.descriptionIconImageView.setPressed(hasFocus);
            }
        });
        binding.editFields.linearLayoutEdit.requestFocus();
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
        // Toasty.info(getContext(), "Fragment's onActivityResult()").show();
        // Toasty.info(getContext(), "requestCode=" + (requestCode == PICK_IMAGE_REQUEST)
        //         + " resultCode=" + (resultCode == RESULT_OK)
        //         + " data=" + (data != null)
        //         + " tempImagesLiveData=" + (tempImagesLiveData.getValue() != null)).show();

        try{
            if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                    && data != null){

                Calendar calendar = Calendar.getInstance();
                if(data.getClipData() != null){
                    ClipData clip = data.getClipData();
                    for(int i = 0; i < clip.getItemCount(); i++){
                        ClipData.Item item = clip.getItemAt(i);
                        Uri uri = item.getUri();

                        Date date = new Date();
                        date.setTime(calendar.getTimeInMillis() + i);

                        Image image = new Image(
                                new File(ImageUtility.getPathFromUri(getContext(), uri))
                                , null
                                , false
                                , 1
                                , this.item.getId()
                                , date);

                        tempImagesLiveData.getValue().add(image);
                        Log.d(ItemEditingFragment.class.getName(), "Added image #" + i + " to ArrayList");
                    }
                }else{
                    if(data.getData() != null){
                        Image image = new Image(
                                new File(ImageUtility.getPathFromUri(getContext(), data.getData()))
                                , null
                                , false
                                , 1
                                , this.item.getId()
                                , calendar.getTime());

                        tempImagesLiveData.getValue().add(image);
                    }
                }
                Toasty.info(getContext(), "New images = " + tempImagesLiveData.getValue().size()).show();
                tempImagesLiveData.postValue(tempImagesLiveData.getValue());

                if(!selectionTracker.hasSelection() && !Objects.requireNonNull(displayingImagesLiveData.getValue()).isEmpty()){
                    selectionTracker.select(displayingImagesLiveData.getValue().get(0).getDateAdded().getTime());
                }

                // FileUtils.copyDirectory(originalFile, new File(this.getFilesDir().toURI().getPath() + ""));
                // System.out.println();
                //TODO: Don't copy the selected file yet. Wait until user press the FAB.
            }
        }catch(SecurityException e){
            Toast.makeText(getContext(), "Exception throwed: Storage Permission Denied", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Setups ChipGroup child views, it will fill data if there is.
     *
     * @param item
     */
    private void setupTagEditor(Item item){
        // Instantiate AutoCompleteTextView's adapter
        suggestionAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);

        // Fetch all tag from the database (From all row)
        suggestionAdapter.addAll(itemViewModel.getAllTags());
        suggestionAdapter.setNotifyOnChange(true);

        if(item != null){
            // Remove tags which are included with the current Item object
            // , so user won't see any tag which included with Item.
            for(int i = 0; i < suggestionAdapter.getCount(); i++){
                if(item.getTags().contains(suggestionAdapter.getItem(i))){
                    suggestionAdapter.remove(suggestionAdapter.getItem(i));
                }
            }
        }

        // Assigns the Adapter to Tag AutoCompleteTextView
        editBinding.tagEditText.setAdapter(suggestionAdapter);

        // Setup Tag AutoCompleteTextView behavior on Text Change
        editBinding.tagEditText.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView tagEditText, int actionId, KeyEvent keyEvent){
                // If enter is pressed
                if(actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                                keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                    if(!tagEditText.getText().toString().trim().isEmpty()){         // If trimmed string is not empty
                        createNewChip(tagEditText.getText().toString(), false);     // Add a new chip
                        tagEditText.setText("");        // Empty Tag AutoCompleteTextView
                    }
                }
                return true;
            }
        });
        editBinding.tagEditText.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l){
                createNewChip(adapterView.getAdapter().getItem(position).toString(), false);         // Add a new chip
                editBinding.tagEditText.setText("");        // Empty Tag AutoCompleteTextView
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_item_edit, container, false);
        return binding.getRoot();
    }

    /**
     * Creates a new Chip to ChipGroup
     *
     * @param newTag      string tag for a new chip
     * @param fillingData is current adding data to ChipGroup (Adding tag Chips when open an Item Profile with tags) or not
     */
    private void createNewChip(String newTag, boolean fillingData){
        final ChipGroup tagChipGroup = editBinding.tagChipGroup;
        if(tagChipGroup != null && !fillingData){
            for(int i = 0; i < tagChipGroup.getChildCount(); i++){
                if(((Chip) tagChipGroup.getChildAt(i)).getText().toString().equalsIgnoreCase(newTag)){
                    // TODO: Change to editTextWrapper error
                    editBinding.tagEditTextWrapper.setError("This tag is already added!");
                    return;
                }
            }
        }

        final Chip newChip = new Chip(getContext());
        newChip.setText(newTag);
        newChip.setCloseIconEnabled(true);      // Add a close button to Chip
        newChip.setOnCloseIconClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                tagChipGroup.removeView(newChip);       // When click the close button, chip will be removed from ChipGroup
            }
        });
        tagChipGroup.addView(newChip);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        // super.onCreate(savedInstanceState);
        initialize();
        int itemId = getArguments().getInt("itemId");
        setupImageRecyclerSelection();

        // Persistence data source
        itemViewModel = ViewModelProviders.of(this).get(ItemViewModel.class);
        itemViewModel.getItemById(itemId).observe(this, this);

        tempImagesLiveData = new MutableLiveData<>();
        tempImagesLiveData.postValue(new ArrayList<Image>());

        displayingImagesLiveData = new MediatorLiveData<>();

        // tempImagesLiveData.observe(this, new Observer<List<Image>>(){
        //     @Override
        //     public void onChanged(List<Image> imageList){
        //         // Toasty.info(getContext(), "tempImagesLiveData: " + imageList.size()).show();
        //         displayingImagesLiveData.setValue(imageList);
        //     }
        // });


        final LiveData<List<Image>> imageLiveData = itemViewModel.getImagesByItemId(itemId);
        displayingImagesLiveData.addSource(tempImagesLiveData, new Observer<List<Image>>(){
            @Override
            public void onChanged(List<Image> imageList){
                // Toasty.info(getContext(), "displayingImagesLiveData: " + imageList.size()).show();
                if(imageLiveData.getValue() != null){
                    imageList.addAll(imageLiveData.getValue());
                }
                displayingImagesLiveData.postValue(imageList);
            }
        });
        displayingImagesLiveData.addSource(imageLiveData, new Observer<List<Image>>(){
            @Override
            public void onChanged(List<Image> imageList){
                displayingImagesLiveData.postValue(imageList);
                displayingImagesLiveData.removeSource(imageLiveData);
            }
        });

        displayingImagesLiveData.observe(this, new Observer<List<Image>>(){
            @Override
            public void onChanged(List<Image> imageList){
                Toasty.info(getContext(), "displayingImagesLiveData: " + imageList.size()).show();
                itemImageAdapter.applyDataChanges(imageList);
                for(int i = 0; i < imageList.size(); i++){
                    if(imageList.get(i).isHeroImage()){
                        selectionTracker.select(imageList.get(i).getDateAdded().getTime());
                        Glide.with(getContext()).load(imageList.get(i).getImageFile()).into(binding.itemImageView);
                        break;
                    }
                }
            }
        });

        binding.itemImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                validatePermissionRequests(getActivity());
                //Toast.makeText(mContext, "I need Medic!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select a picture"), PICK_IMAGE_REQUEST);
            }
        });
    }

    /**
     * Create a Color Chooser Dialog
     */
    private void setupColorDialogButton(){
        editBinding.colorEditButton.setOnClickListener(new View.OnClickListener(){
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

    /**
     * Setups DataBinding, Image Adapter and RecyclerView
     */
    private void initialize(){
        // Gets the Window in order to change Status Bar's Color
        window = getActivity().getWindow();
        editBinding = binding.editFields;

        itemImageAdapter = new ItemImageAdapter(getContext(), true);
        itemImageAdapter.setDeleteClickListener(new ItemImageAdapter.DeleteClickListener(){
            @Override
            public void onDelete(File imageFile, int position){
                if(displayingImagesLiveData != null && !Objects.requireNonNull(displayingImagesLiveData.getValue()).isEmpty()){
                    displayingImagesLiveData.getValue().remove(position);
                }
            }
        });
        editBinding.imageRecyclerView.setAdapter(itemImageAdapter);

        editBinding.imageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        editBinding.addMultiImageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                validatePermissionRequests(getActivity());
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select images"), PICK_IMAGE_REQUEST);
            }
        });

        editBinding.quantityEditText.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){
                try{
                    Long.valueOf(charSequence.toString());
                }catch(NumberFormatException e){
                    editBinding.quantityEditWrapper.setError("The number is too large");
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable){

            }
        });
    }

    private void setupImageRecyclerSelection(){
        selectionTracker = new SelectionTracker.Builder<>("IMAGE_SELECTION"
                , editBinding.imageRecyclerView
                , new ItemImageAdapter.ItemImageKeyProvider(itemImageAdapter)
                , new MyItemDetailsLookup(editBinding.imageRecyclerView)
                , StorageStrategy.createLongStorage())
                .withSelectionPredicate(new SelectionTracker.SelectionPredicate<Long>(){
                    @Override
                    public boolean canSetStateForKey(@NonNull Long key, boolean nextState){
                        return !selectionTracker.hasSelection() || !selectionTracker.getSelection().contains(key);
                    }

                    @Override
                    public boolean canSetStateAtPosition(int position, boolean nextState){
                        return true;
                    }

                    @Override
                    public boolean canSelectMultiple(){
                        return false;
                    }
                })
                .build();
        selectionTracker.addObserver(new SelectionTracker.SelectionObserver(){
            @Override
            public void onSelectionChanged(){
                super.onSelectionChanged();
                if(selectionTracker.hasSelection() && displayingImagesLiveData.getValue() != null){
                    for(Image imageFile : displayingImagesLiveData.getValue()){
                        if(selectionTracker.getSelection().contains(imageFile.getDateAdded().getTime())){
                            imageFile.setHeroImage(true);
                            Glide.with(getContext()).load(imageFile.getImageFile())
                                    .thumbnail(0.025f)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(binding.itemImageView);
                        }else{
                            imageFile.setHeroImage(false);
                        }
                    }
                }
            }
        });
        itemImageAdapter.setSelectionTracker(selectionTracker);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        if(getActivity() instanceof ItemEditingContainerActivity){
            inflater.inflate(R.menu.menu_item_editor, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Defines system ui such as Status bar and navigation bar
     * Toolbar back button behavior is also initialized here.
     *
     * @param backColorInt bold color integer
     */
    private void setupSystemUiElements(@ColorInt int backColorInt){
        String toolbarTitle = (!isInEditMode) ? "Add an item" : "Edit an item";

        // If current activity is an instance of ItemEditContainerActivity
        if(getActivity() instanceof ItemEditingContainerActivity){
            AppCompatActivity activity = (AppCompatActivity) getActivity();

            // Set this fragment's toolbar to activity's
            activity.setSupportActionBar(binding.toolbar);

            // Change toolbar title
            activity.setTitle(toolbarTitle);

            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);

            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }else{
            // TODO: Define toolbar behavior outside container activity
            binding.toolbar.setTitle(toolbarTitle);
            binding.toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        }
        setupSystemUiColor(backColorInt);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new MaterialDialog.Builder(v.getContext()).title("Cancel editing?")
                        .positiveText(android.R.string.yes)
                        .negativeText(android.R.string.no)
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

    public void setOnConfirmListener(OnConfirmListener onConfirmListener){
        this.onConfirmListener = onConfirmListener;
    }

    private void fillExistingDataToFields(Item item){
        editBinding.nameEditText.setText(item.getName());
        editBinding.quantityEditText.setText(String.valueOf(item.getQuantity()));
        editBinding.descriptionEditText.setText(item.getDescription());

        for(String tag : item.getTags()){
            createNewChip(tag, true);
        }
    }

    private enum ActionMode{ADD_ITEM, UPDATE_ITEM}

    /**
     * Handles item interaction (Insertion or Updating)
     *
     * @param actionMode    a mode, either Insert or Update
     * @param itemViewModel to providing data
     * @param item          is null, if adding a new item. Otherwise, update it.
     */
    public void takeAction(ActionMode actionMode, ItemViewModel itemViewModel, Item item){
        Date currentTime = Calendar.getInstance().getTime();

        if(editBinding.quantityEditText.getText().toString().isEmpty() || editBinding.nameEditText.getText().toString().isEmpty()
                || editBinding.descriptionEditText.getText().toString().isEmpty()){
            if(editBinding.quantityEditText.getText().toString().isEmpty()){
                editBinding.nameEditWrapper.setError("Give it a name");
            }
            if(editBinding.quantityEditText.getText().toString().isEmpty()){
                editBinding.quantityEditWrapper.setError("Enter a number");
            }
            if(editBinding.descriptionEditText.getText().toString().isEmpty()){
                editBinding.descriptionEditWrapper.setError("Give it a description or something");
            }
            return;
        }

        long quantity;
        try{
            quantity = Long.valueOf(editBinding.quantityEditText.getText().toString());
        }catch(NumberFormatException e){
            editBinding.quantityEditWrapper.setError("That number is too large.");
            e.printStackTrace();
            return;
        }

        String itemName = editBinding.nameEditText.getText().toString().trim();
        String description = editBinding.descriptionEditText.getText().toString().trim();

        Set<String> tagSet = null;
        if(editBinding.tagChipGroup.getChildCount() > 0){
            tagSet = new HashSet<>();
            for(int i = 0; i < editBinding.tagChipGroup.getChildCount(); i++){
                tagSet.add(((Chip) editBinding.tagChipGroup.getChildAt(i)).getText().toString());
            }
        }

        if(actionMode == ActionMode.ADD_ITEM){
            itemViewModel
                    .insert(new Item(itemName, quantity, description, selectedColorInt
                            , tagSet, currentTime, null));

            if(!tempImagesLiveData.getValue().isEmpty()){
                getSelectedImageUrl(tempImagesLiveData.getValue()
                        , itemViewModel
                        , itemViewModel
                                .getItemDomainValue(DataRepository.ENTITY_ITEM
                                        , DataRepository.MAX_VALUE
                                        , DataRepository.ITEM_FIELD_ID));
            }
            applyHeroImageStatus(itemViewModel);
        }else if(actionMode == ActionMode.UPDATE_ITEM){
            item.setName(itemName);
            item.setQuantity(quantity);
            item.setDescription(description);
            item.setItemColorAccent(selectedColorInt);

            item.setDateModified(currentTime);
            item.setTags(tagSet);
            itemViewModel.update(item);

            if(!tempImagesLiveData.getValue().isEmpty()){
                getSelectedImageUrl(tempImagesLiveData.getValue()
                        , itemViewModel
                        , item.getId());
            }
            applyHeroImageStatus(itemViewModel);
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
        //         item.setImageFiles(imageFile);
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

        //startActivity(new Intent(mContext, CollectionActivity.class));

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

    public interface OnConfirmListener{
        void onConfirm(int itemId);
    }

    private void applyHeroImageStatus(ItemViewModel itemViewModel){
        for(int i = 0; i < displayingImagesLiveData.getValue().size(); i++){
            Image image = displayingImagesLiveData.getValue().get(i);
            Image imageInDb = itemViewModel.getImageByTimeStamp(image.getDateAdded().getTime());
            imageInDb.setHeroImage(image.isHeroImage());
            itemViewModel.update(imageInDb);
        }
    }

    /**
     * Copies Selected Image into Internal storage (The new image from copying will be named by unix time)
     *
     * @param imageList all images which are being displayed in the RecyclerView
     * @param viewModel for image database access
     *
     * @return path of the new file
     */
    private void getSelectedImageUrl(List<Image> imageList, ItemViewModel viewModel, int itemId){
        Toasty.info(getContext(), "getSelectedImageUrl()=" + imageList.size()).show();
        //TODO: Fix bug when editing image
        // Internal storage path
        String internalStoragePath = getActivity().getFilesDir().toURI().getPath() + "/" + itemId;

        File internalStorage = new File(internalStoragePath);
        if(internalStorage.exists()){
            // try{
            //     FileUtils.deleteDirectory(internalStorage);
            // }catch(IOException e){
            //     e.printStackTrace();
            // }
        }else{
            internalStorage.mkdir();
        }

        for(int i = 0; i < imageList.size(); i++){
            // New file's name using unix time stamp with .jpg extension
            String fileNameWithExtension = "id" + imageList.get(i).getDateAdded().getTime() + ".jpg";

            Image tempImage = imageList.get(i);

            // Creates new instance of file with index.jpg located in internal storage
            String newFileDirectory = internalStoragePath + "/" + fileNameWithExtension;

            File newFile = new File(newFileDirectory);

            // Copies file
            try{
                FileUtils.copyFile(tempImage.getImageFile(), newFile);
                tempImage.setImageFile(newFile);
            }catch(IOException e){
                e.printStackTrace();
            }
            viewModel.insert(tempImage);
        }
    }

    @SuppressLint("ResourceType")
    @Override
    public void onChanged(@Nullable final Item item){
        isInEditMode = item != null;
        this.item = item;

        if(item != null){
            selectedColorInt = item.getItemColorAccent();
        }else{
            selectedColorInt = Color.parseColor(getResources().getString(R.color.md_red_400));
        }

        editBinding.colorCircle.setBackgroundColor(selectedColorInt);

        setupSystemUiElements(selectedColorInt);
        setupTextFieldOnFocusAppearances(selectedColorInt);
        setupColorDialogButton();
        setupTagEditor(item);

        if(item != null){
            fillExistingDataToFields(item);
        }

        binding.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener(){
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
}
