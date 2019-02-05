package io.rektplorer.inventoryapp.fragments;


import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
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
import com.kennyc.view.MultiStateView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
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
import androidx.recyclerview.selection.BandPredicate;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
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
import io.rektplorer.inventoryapp.utility.ScreenUtility;
import io.rektplorer.inventoryapp.utility.ImageUtility;

import static android.app.Activity.RESULT_OK;
import static io.rektplorer.inventoryapp.utility.ColorUtility.darkenColor;

public class ItemEditingFragment extends Fragment implements ColorChooserDialog.ColorCallback{

    private static final String LOG_TAG = ItemEditingFragment.class.getSimpleName();

    private static final String TEXT_FIELD_ITEM_NAME = "TEXT_FIELD_ITEM_NAME";
    private static final String TEXT_FIELD_ITEM_QUANTITY = "TEXT_FIELD_ITEM_QUANTITY";
    private static final String TEXT_FIELD_ITEM_DESCRIPTION = "TEXT_FIELD_ITEM_DESCRIPTION";
    private static final String TEMP_FIELD_ITEM_TAGS = "TEMP_FIELD_ITEM_TAGS";
    private static final String TEMP_IMAGES = "TEMP_IMAGES";

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_PERMISSION = 1;

    ///n Field Variables
    private boolean isInEditMode;

    private FragItemEditBinding binding;
    private FragBodyItemEditBinding editBinding;

    private Window window;

    private ItemViewModel itemViewModel;

    private OnConfirmListener onConfirmListener;

    private ItemImageAdapter itemImageAdapter;

    @ColorInt
    private int selectedColorInt;

    private ArrayAdapter<String> suggestionAdapter;
    private SelectionTracker selectionTracker;

    private LiveData<List<Image>> databaseImageLiveData;
    private MutableLiveData<List<Image>> tempImagesLiveData;
    private MediatorLiveData<List<Image>> displayingImagesLiveData;

    private Item item;

    public ItemEditingFragment(){
        setHasOptionsMenu(true);
        setRetainInstance(true);
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
        // FIXME: Do this with XML and ObjectAnimator
        ObjectAnimator animator = ObjectAnimator.ofArgb(editBinding.nameIconImageView, "tint", colorInt);
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
                editBinding.descriptionIconImageView
                        .setColorFilter((hasFocus) ? colorInt : Color.BLACK);
                editBinding.descriptionIconImageView.setPressed(hasFocus);
            }
        });
        binding.editFields.linearLayoutEdit.requestFocus();
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog){ }

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

                editBinding.imageRecyclerMultiState.setViewState(MultiStateView.VIEW_STATE_LOADING);

                int itemId = (item != null) ? item.getId() : -1;

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
                                , itemId
                                , date);

                        tempImagesLiveData.getValue().add(image);
                        Log.d(LOG_TAG, "Added image #" + i + " to ArrayList");
                    }
                }else{
                    if(data.getData() != null){
                        Image image = new Image(
                                new File(ImageUtility.getPathFromUri(getContext(), data.getData()))
                                , null
                                , false
                                , 1
                                , itemId
                                , calendar.getTime());

                        tempImagesLiveData.getValue().add(image);
                    }
                }
                Toasty.info(getContext(), "New images = " + tempImagesLiveData.getValue().size())
                      .show();
                tempImagesLiveData.setValue(tempImagesLiveData.getValue());

                if(!selectionTracker.hasSelection() && !Objects
                        .requireNonNull(displayingImagesLiveData.getValue()).isEmpty()){
                    selectionTracker
                            .select(displayingImagesLiveData.getValue().get(0).getDateAdded()
                                                            .getTime());
                }

                // FileUtils.copyDirectory(originalFile, new File(this.getFilesDir().toURI().getPath() + ""));
                // System.out.println();
                //TODO: Don't copy the selected file yet. Wait until user press the FAB.
            }
        }catch(SecurityException e){
            Toast.makeText(getContext(), "Exception throwed: Storage Permission Denied",
                           Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        Log.d(LOG_TAG, "onAttach()");
        tempImagesLiveData = new MutableLiveData<>();
        displayingImagesLiveData = new MediatorLiveData<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        Log.d(LOG_TAG, "onCreateView()");
        binding = DataBindingUtil.inflate(inflater, R.layout.frag_item_edit, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ResourceType")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        Log.d(LOG_TAG, "onViewCreated()");
        initialize();

        int itemId = getArguments().getInt("itemId");
        setupImageRecyclerSelection();

        // Persistence data source
        itemViewModel = ViewModelProviders.of(getActivity()).get(ItemViewModel.class);

        itemViewModel.getItemById(itemId).observe(getActivity(), new Observer<Item>(){
            @Override
            public void onChanged(final Item item){
                isInEditMode = item != null;
                ItemEditingFragment.this.item = item;

                if(item != null){
                    selectedColorInt = item.getItemColorAccent();
                }else{
                    selectedColorInt = Color
                            .parseColor(getResources().getString(R.color.md_red_400));
                }

                editBinding.colorCircle.setBackgroundColor(selectedColorInt);
                setupTextFieldOnFocusAppearances(selectedColorInt);
                setupSystemUiColor(selectedColorInt);
                setupColorDialogButton();
                setupTagEditor(item);

                if(savedInstanceState != null){
                    editBinding.nameEditText
                            .setText(savedInstanceState.getString(TEXT_FIELD_ITEM_NAME));
                    editBinding.quantityEditText
                            .setText(savedInstanceState.getString(TEXT_FIELD_ITEM_QUANTITY));
                    editBinding.descriptionEditText
                            .setText(savedInstanceState.getString(TEXT_FIELD_ITEM_DESCRIPTION));

                    new Handler().post(new Runnable(){
                        @Override
                        public void run(){
                            ArrayList<String> tagList = savedInstanceState
                                    .getStringArrayList(TEMP_FIELD_ITEM_TAGS);
                            if(tagList != null){
                                for(int i = 0; i < tagList.size(); i++){
                                    Log.d(LOG_TAG, "Adding chip #" + i + " of " + tagList
                                            .size() + " " + tagList.get(i));
                                    createNewChip(tagList.get(i), true);
                                }
                            }
                        }
                    });
                }else{
                    if(item != null){
                        editBinding.nameEditText.setText(item.getName());
                        editBinding.quantityEditText
                                .setText(String.valueOf(item.getQuantity()));
                        editBinding.descriptionEditText.setText(item.getDescription());

                        new Handler().post(new Runnable(){
                            @Override
                            public void run(){
                                for(String tag : item.getTags()){
                                    createNewChip(tag, true);
                                }
                            }
                        });
                    }
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
        });
        databaseImageLiveData = itemViewModel.getImagesByItemId(itemId);

        displayingImagesLiveData.observe(getActivity(), new Observer<List<Image>>(){
            @Override
            public void onChanged(List<Image> imageList){
                // Collections.sort(imageList, new Comparator<Image>(){
                //     @Override
                //     public int compare(Image image1, Image image2){
                //         return Integer.compare(image1.getId(), image2.getId());
                //     }
                // });
                Log.d(LOG_TAG, "Populating image RecyclerView (" + imageList.size() + ")");
                itemImageAdapter.applyDataChanges(imageList);
                for(int i = 0; i < imageList.size(); i++){
                    if(imageList.get(i).isHeroImage()){
                        selectionTracker.select(imageList.get(i).getDateAdded().getTime());
                        Glide.with(getContext()).load(imageList.get(i).getImageFile())
                             .into(binding.itemImageView);
                        break;
                    }
                }
                if(!imageList.isEmpty()){
                    editBinding.imageRecyclerMultiState
                            .setViewState(MultiStateView.VIEW_STATE_CONTENT);
                }else{
                    editBinding.imageRecyclerMultiState
                            .setViewState(MultiStateView.VIEW_STATE_EMPTY);
                }
                String imageHeadText = "Item Images" + ((imageList
                        .size() > 0) ? " (Total " + NumberFormat
                        .getNumberInstance(ScreenUtility.getCurrentLocale(getContext()))
                        .format(imageList.size()) + ")" : "");
                editBinding.itemImageTextView.setText(imageHeadText);
            }
        });

        displayingImagesLiveData.addSource(databaseImageLiveData, new Observer<List<Image>>(){
            @Override
            public void onChanged(List<Image> imageList){
                // If tempImageLiveData is called before this
                if(tempImagesLiveData.getValue() != null){
                    if(!tempImagesLiveData.getValue().isEmpty()){
                        for(int i = 0; i < tempImagesLiveData.getValue().size(); i++){
                            if(!imageList.contains(tempImagesLiveData.getValue().get(i))){
                                imageList.add(tempImagesLiveData.getValue().get(i));
                            }
                        }
                    }
                }
                displayingImagesLiveData.setValue(imageList);
                displayingImagesLiveData.removeSource(databaseImageLiveData);
            }
        });

        displayingImagesLiveData.addSource(tempImagesLiveData, new Observer<List<Image>>(){
            @Override
            public void onChanged(List<Image> imageList){
                if(databaseImageLiveData.getValue() != null){
                    if(!databaseImageLiveData.getValue().isEmpty()){
                        for(int i = 0; i < databaseImageLiveData.getValue().size(); i++){
                            if(!imageList.contains(databaseImageLiveData.getValue().get(i))){
                                imageList.add(databaseImageLiveData.getValue().get(i));
                            }
                        }
                    }
                    // imageList.addAll(databaseImageLiveData.getValue());
                    if(!selectionTracker.hasSelection()){
                        selectionTracker.select(imageList.get(0).getDateAdded().getTime());
                    }
                }
                Log.d(LOG_TAG, "Posting displayImagesLiveData: " + imageList.size());
                displayingImagesLiveData.setValue(imageList);
            }
        });

        tempImagesLiveData.setValue(new ArrayList<Image>());
        if(savedInstanceState != null){
            List<Image> restoredList = savedInstanceState.getParcelableArrayList(TEMP_IMAGES);
            if(restoredList == null){
                restoredList = new ArrayList<>();
            }
            Log.d(LOG_TAG, "Restoring temp image list (" + restoredList.size() + ")");
            tempImagesLiveData.setValue(restoredList);
            selectionTracker.onRestoreInstanceState(savedInstanceState);
        }

        binding.itemImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                validatePermissionRequests(getActivity());
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select images"),
                                       PICK_IMAGE_REQUEST);
            }
        });

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState){
        Log.d(LOG_TAG, "onSaveInstanceState()");

        outState.putString(TEXT_FIELD_ITEM_NAME, editBinding.nameEditText.getText().toString());

        outState.putString(TEXT_FIELD_ITEM_QUANTITY,
                           editBinding.quantityEditText.getText().toString());
        outState.putString(TEXT_FIELD_ITEM_DESCRIPTION,
                           editBinding.descriptionEditText.getText().toString());

        ArrayList<String> chipsFromChipGroup = new ArrayList<>();
        for(int i = 0; i < editBinding.tagChipGroup.getChildCount(); i++){
            chipsFromChipGroup
                    .add(((Chip) editBinding.tagChipGroup.getChildAt(i)).getText().toString());
        }
        outState.putStringArrayList(TEMP_FIELD_ITEM_TAGS, chipsFromChipGroup);

        // FIXED: Fix bug when resizing screen from half to full in stock Android cause tempImagesLiveData to return null when calling getValue(), causing image list to lost its state.
        // SOLUTION: It was my mistake to call postValue() on MutableLiveData instead of setValue() in the ui thread
        Log.d(LOG_TAG, "TempImagesLiveData != null: " + (tempImagesLiveData.getValue() != null));
        tempImagesLiveData.removeObservers(this);
        if(tempImagesLiveData.getValue() != null){
            outState.putParcelableArrayList(TEMP_IMAGES, new ArrayList<Parcelable>(
                    tempImagesLiveData.getValue()));
        }else{
            Log.e(LOG_TAG, "TempImagesData cannot be saved!");
        }

        selectionTracker.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        setupSystemUiColor(selectedColorInt);
    }

    /**
     * Setups DataBinding, Image Adapter and RecyclerView
     */
    private void initialize(){
        // Gets the Window in order to change Status Bar's Color
        window = getActivity().getWindow();
        editBinding = binding.editFields;

        itemImageAdapter = new ItemImageAdapter(getContext(), true, ScreenUtility
                .getScreenOrientation(getContext()) != ScreenUtility.SCREENORIENTATION_PORTRAIT);
        // itemImageAdapter.setHasStableIds(true);
        itemImageAdapter.setDeleteClickListener(new ItemImageAdapter.DeleteClickListener(){
            @Override
            public void onDelete(Image imageFile, int position){
                if(displayingImagesLiveData != null && !Objects
                        .requireNonNull(displayingImagesLiveData.getValue()).isEmpty()){
                    displayingImagesLiveData.getValue().remove(imageFile);
                    displayingImagesLiveData.setValue(displayingImagesLiveData.getValue());
                }
            }
        });
        editBinding.imageRecyclerView.setHasFixedSize(false);

        editBinding.imageRecyclerView.setAdapter(itemImageAdapter);
        editBinding.imageRecyclerView.setItemViewCacheSize(20);

        editBinding.imageRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL));



        // editBinding.imageRecyclerView.addItemDecoration(new MarginItemDecoration(getContext(), 4, 2));
        editBinding.addMultiImageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                validatePermissionRequests(getActivity());
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select images"),
                                       PICK_IMAGE_REQUEST);
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
        setupSystemUiElements();
    }

    private void setupImageRecyclerSelection(){
        ItemDetailsLookup itemDetailsLookup = new MyItemDetailsLookup(
                editBinding.imageRecyclerView);
        selectionTracker = new SelectionTracker.Builder<>("IMAGE_SELECTION"
                , editBinding.imageRecyclerView
                , new ItemImageAdapter.ItemImageKeyProvider(itemImageAdapter)
                , itemDetailsLookup
                , StorageStrategy.createLongStorage())
                .withSelectionPredicate(new SelectionTracker.SelectionPredicate<Long>(){
                    @Override
                    public boolean canSetStateForKey(@NonNull Long key, boolean nextState){
                        return !selectionTracker.hasSelection() || !selectionTracker.getSelection()
                                                                                    .contains(key);
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
                .withBandPredicate(new BandPredicate.NonDraggableArea(editBinding.imageRecyclerView,
                                                                      itemDetailsLookup))
                .build();

        selectionTracker.addObserver(new SelectionTracker.SelectionObserver(){
            @Override
            public void onSelectionChanged(){
                super.onSelectionChanged();
                if(selectionTracker.hasSelection() && displayingImagesLiveData.getValue() != null){
                    for(Image imageFile : displayingImagesLiveData.getValue()){
                        if(selectionTracker.getSelection()
                                           .contains(imageFile.getDateAdded().getTime())){
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
     * Setups ChipGroup child views, it will fill data if there is.
     *
     * @param item target item
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
                    if(!tagEditText.getText().toString().trim()
                                   .isEmpty()){         // If trimmed string is not empty
                        createNewChip(tagEditText.getText().toString(),
                                      false);     // Add a new chip
                        tagEditText.setText("");        // Empty Tag AutoCompleteTextView
                    }
                }
                return true;
            }
        });
        editBinding.tagEditText.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l){
                createNewChip(adapterView.getAdapter().getItem(position).toString(),
                              false);         // Add a new chip
                editBinding.tagEditText.setText("");        // Empty Tag AutoCompleteTextView
            }
        });
    }

    /**
     * Creates a new Chip to ChipGroup
     *
     * @param newTag      string tag for a new chip
     * @param fillingData is current adding data to ChipGroup (Adding tag Chips when open an Item Profile with tags) or not
     */
    private void createNewChip(String newTag, boolean fillingData){
        final ChipGroup tagChipGroup = editBinding.tagChipGroup;

        if(getContext() == null){
            return;
        }

        if(tagChipGroup != null && !fillingData){
            for(int i = 0; i < tagChipGroup.getChildCount(); i++){
                if(((Chip) tagChipGroup.getChildAt(i)).getText().toString()
                                                      .equalsIgnoreCase(newTag)){
                    // TODO: Change to editTextWrapper error
                    editBinding.tagEditTextWrapper.setError("This tag is already added!");
                    return;
                }
            }
        }

        final Chip newChip = new Chip(getContext());
        newChip.setText(newTag);
        newChip.setCloseIconVisible(true);      // Add a close button to Chip
        newChip.setOnCloseIconClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                tagChipGroup.removeView(
                        newChip);       // When click the close button, chip will be removed from ChipGroup
            }
        });
        tagChipGroup.addView(newChip);
    }

    /**
     * Handles item interaction (Insertion or Updating)
     *
     * @param actionMode    a mode, either Insert or Update
     * @param itemViewModel to providing data
     * @param item          is null, if adding a new item. Otherwise, update it.
     */
    private void takeAction(ActionMode actionMode, ItemViewModel itemViewModel, Item item){
        Date currentTime = Calendar.getInstance().getTime();

        if(editBinding.quantityEditText.getText().toString().isEmpty() || editBinding.nameEditText
                .getText().toString().isEmpty()
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

            if(!displayingImagesLiveData.getValue().isEmpty()){
                getSelectedImageUrl(displayingImagesLiveData.getValue()
                        , itemViewModel
                        , itemViewModel.getItemDomainValue(DataRepository.ENTITY_ITEM
                                , DataRepository.MAX_VALUE
                                , DataRepository.ITEM_FIELD_ID));
            }
        }else if(actionMode == ActionMode.UPDATE_ITEM){
            item.setName(itemName);
            item.setQuantity(quantity);
            item.setDescription(description);
            item.setItemColorAccent(selectedColorInt);

            item.setDateModified(currentTime);
            item.setTags(tagSet);
            itemViewModel.update(item);

            if(!displayingImagesLiveData.getValue().isEmpty()){
                getSelectedImageUrl(displayingImagesLiveData.getValue()
                        , itemViewModel
                        , item.getId());
            }
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
                onConfirmListener.onConfirm(itemViewModel
                                                    .getItemDomainValue(DataRepository.ENTITY_ITEM,
                                                                        DataRepository.MAX_VALUE,
                                                                        DataRepository.ITEM_FIELD_ID));
            }
        }

        if(getActivity() instanceof ItemEditingContainerActivity){
            getActivity().finish();
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
                                                  new String[]{
                                                          Manifest.permission.READ_EXTERNAL_STORAGE},
                                                  REQUEST_PERMISSION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    /**
     * Defines system ui such as Status bar and navigation bar
     * Toolbar back button behavior is also initialized here.
     */
    private void setupSystemUiElements(){
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

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                MaterialDialog.SingleButtonCallback positive = new MaterialDialog.SingleButtonCallback(){
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which){
                        if(getActivity() instanceof ItemEditingContainerActivity){
                            getActivity()
                                    .finish();
                        }else{
                            getActivity()
                                    .getSupportFragmentManager()
                                    .beginTransaction()
                                    .remove(getParentFragment())
                                    .commit();
                        }
                    }
                };
                new MaterialDialog.Builder(v.getContext()).title("Cancel editing?")
                                                          .positiveText(android.R.string.yes)
                                                          .negativeText(android.R.string.no)
                                                          .theme(Theme.LIGHT)
                                                          .onPositive(positive)
                                                          .show();
            }
        });
    }

    /**
     * Copies Selected Image into Internal storage (The new image from copying will be named by unix time)
     *
     * @param imageList all images which are being displayed in the RecyclerView
     * @param viewModel for image database access
     */
    private void getSelectedImageUrl(List<Image> imageList, ItemViewModel viewModel, int itemId){
        String internalStoragePath = getActivity().getFilesDir().toURI().getPath() + "/" + itemId;
        new ImageFilesAsyncManipulator(viewModel, selectionTracker, databaseImageLiveData,
                                       internalStoragePath, getContext(), itemId)
                .execute(imageList.toArray(new Image[0]));
    }

    public void setOnConfirmListener(OnConfirmListener onConfirmListener){
        this.onConfirmListener = onConfirmListener;
    }

    private enum ActionMode{ADD_ITEM, UPDATE_ITEM}

    public interface OnConfirmListener{
        void onConfirm(int itemId);
    }

    private static class ImageFilesAsyncManipulator extends AsyncTask<Image, Integer, Void>{

        private final ItemViewModel itemViewModel;
        private final SelectionTracker selectionTracker;
        private final LiveData<List<Image>> databaseImageLiveData;
        private final String internalStoragePath;
        private final WeakReference<Context> contextWeakReference;
        private final int itemId;
        ///n Field Variables
        private MaterialDialog dialog;

        ImageFilesAsyncManipulator(ItemViewModel itemViewModel,
                                   SelectionTracker selectionTracker,
                                   LiveData<List<Image>> databaseImageLiveData,
                                   String internalStoragePath, Context context, int itemId){
            this.itemViewModel = itemViewModel;
            this.selectionTracker = selectionTracker;
            this.databaseImageLiveData = databaseImageLiveData;
            this.internalStoragePath = internalStoragePath;
            contextWeakReference = new WeakReference<>(context);
            this.itemId = itemId;
        }

        @Override
        protected Void doInBackground(Image... images){
            // Toasty.info(getContext(), "tempImageSize=" + tempImagesLiveData.getValue().size()).show();
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

            for(int i = 0; i < images.length; i++){
                publishProgress(i + 1);
                // New file's name using unix time stamp with .jpg extension
                Image tempImage = images[i];
                tempImage.setHeroImage(
                        selectionTracker.getSelection()
                                        .contains(tempImage.getDateAdded().getTime()));
                tempImage.setItemId(itemId);
                if(!databaseImageLiveData.getValue().contains(
                        tempImage)){      // If the image is not in the database
                    String fileNameWithExtension = "id" + images[i].getDateAdded()
                                                                   .getTime() + ".jpg";
                    // Creates new instance of file with index.jpg located in internal storage
                    String newFileDirectory = internalStoragePath + "/" + fileNameWithExtension;

                    File newFile = new File(newFileDirectory);
                    try{
                        FileUtils.copyFile(tempImage.getImageFile(),
                                           newFile);      // Copy the image to the internal storage
                        tempImage.setImageFile(newFile);
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    itemViewModel.insert(tempImage);
                }else{
                    // Toast.makeText(getContext(), "Updating " + tempImage.getId() + " heroImage="+ tempImage.isHeroImage(), Toast.LENGTH_SHORT).show();
                    itemViewModel.update(tempImage);
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            // TODO: update dialog code when there is a new version to support androidx
            // dialog = new MaterialDialog.Builder(contextWeakReference.get())
            //         .title("Processing image files...")
            //         .customView(R.layout.dialog_intermidiate_progress, false)
            //         .show();
        }

        @Override
        protected void onPostExecute(Void aVoid){
            super.onPostExecute(aVoid);
            // dialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(Integer... values){
            super.onProgressUpdate(values);
            System.out.println("progress " + values[0]);
            // dialog.setProgress(values[0]);
        }
    }
}
