package io.rektplorer.inventoryapp.rvadapters;


import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import io.rektplorer.inventoryapp.R;
import io.rektplorer.inventoryapp.databinding.CardEditImageBinding;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Image;

public class ItemImageAdapter extends ListAdapter<Image, ItemImageAdapter.ViewHolder>{

    private static final String LOG_TAG = ItemImageAdapter.class.getSimpleName();

    private Context context;
    private final boolean editMode;
    private final boolean dynamicHeight;
    private SelectionTracker<Long> selectionTracker;

    private static DiffUtil.ItemCallback<Image> IMAGE_FILE_DIFF_UTIL = new DiffUtil.ItemCallback<Image>(){
        @Override
        public boolean areItemsTheSame(@NonNull Image oldItem, @NonNull Image newItem){
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Image oldItem, @NonNull Image newItem){
            return oldItem.equals(newItem);
        }
    };

    private DeleteClickListener deleteClickListener;

    public ItemImageAdapter(Context context, boolean editMode, boolean dynamicHeight){
        super(IMAGE_FILE_DIFF_UTIL);
        this.context = context;
        this.editMode = editMode;
        this.dynamicHeight = dynamicHeight;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        CardEditImageBinding binding = CardEditImageBinding
                .inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        holder.bindToView(position);
    }

    @Override
    public long getItemId(int position){
        return getItem(position).getDateAdded().getTime();
    }

    public void applyDataChanges(List<Image> imageFileList){
        submitList(imageFileList);
        notifyDataSetChanged();
        Log.d(this.getClass().getName(),
              "Submitted " + imageFileList.size() + " image(s) to adapter");
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker){
        this.selectionTracker = selectionTracker;
    }

    public void setDeleteClickListener(DeleteClickListener deleteClickListener){
        this.deleteClickListener = deleteClickListener;
    }

    public interface DeleteClickListener{
        void onDelete(Image imageFile, int position);
    }

    public final class ViewHolder extends RecyclerView.ViewHolder implements Detailable{
        private CardEditImageBinding binding;

        private ViewHolder(@NonNull CardEditImageBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }

        void bindToView(final int position){
            final Image image = getItem(position);
            final File imageFile = image.getImageFile();
            final long key = image.getDateAdded().getTime();

            if(selectionTracker == null && editMode){
                return;
            }

            RequestBuilder requestBuilder = Glide.with(context)
                                                 .load(imageFile)
                                                 .transition(
                                                         DrawableTransitionOptions.withCrossFade())
                                                 .thumbnail(0.01f);

            // TODO: Preload Images
            // Asynchronously Calculate image aspect ratio
            if(dynamicHeight){
                String aspectRatio = image.getAspectRatio();
                if(aspectRatio == null || aspectRatio.isEmpty()){
                    Log.d(LOG_TAG, "Calculating image aspect ratio " + image.getImageFile().getName());
                    new Image.ImageViewSizeAsyncCalculator(binding.imageConstraintLayout, binding.imageView, image)
                            .execute(imageFile);
                }else{
                    ConstraintSet cs = new ConstraintSet();
                    cs.clone(binding.imageConstraintLayout);
                    cs.setDimensionRatio(
                            binding.imageConstraintLayout.getViewById(binding.imageView.getId())
                                                         .getId(), aspectRatio);
                    cs.applyTo(binding.imageConstraintLayout);
                    binding.imageConstraintLayout.setConstraintSet(cs);
                    requestBuilder.into(binding.imageView);
                }
            }else{
                requestBuilder.apply(RequestOptions.centerCropTransform()).into(binding.imageView);
            }
            // binding.heroImageSelector.setVisibility((editMode)? View.VISIBLE : View.GONE);
            // binding.imageTitle.setText(String.valueOf(position + 1));

            if(editMode){
                binding.heroImageSelector.setVisibility(View.VISIBLE);
                binding.heroImageSelector.setImageResource((selectionTracker.isSelected(key)) ?
                                                                   R.drawable.ic_check_circle_24dp : R.drawable.ic_radio_button_unchecked_white_24dp);

                // binding.heroImageSelector.setOnClickListener(new View.OnClickListener(){
                //     @Override
                //     public void onClick(View view){
                //         if(!selectionTracker.isSelected(key)){
                //             selectionTracker.select(key);
                //         }
                //     }
                // });

                binding.removeImageButton.setVisibility(View.VISIBLE);
                // binding.removeImageButton.setOnClickListener(new View.OnClickListener(){
                //     @Override
                //     public void onClick(View view){
                //         Toasty.success(view.getContext(), "Removed image").show();
                //         if(deleteClickListener != null){
                //             deleteClickListener.onDelete(getItem(position), position);
                //         }
                //     }
                // });
            }else{
                binding.heroImageSelector.setVisibility(View.GONE);
                binding.removeImageButton.setVisibility(View.GONE);
            }
        }

        @Override
        public ItemDetailsLookup.ItemDetails<Long> getItemDetails(){
            return new ItemDetailsLookup.ItemDetails<Long>(){
                @Override
                public int getPosition(){
                    return getAdapterPosition();
                }

                @Nullable
                @Override
                public Long getSelectionKey(){
                    return getItem(getAdapterPosition()).getDateAdded().getTime();
                }
            };
        }
    }

    public static class ItemImageKeyProvider extends ItemKeyProvider<Long>{

        private ItemImageAdapter adapter;

        /**
         * Creates a new provider with the given scope.
         *
         * @param adapter image adapter
         */
        public ItemImageKeyProvider(ItemImageAdapter adapter){
            super(ItemKeyProvider.SCOPE_CACHED);
            this.adapter = adapter;
        }

        @Nullable
        @Override
        public Long getKey(int position){
            return adapter.getItem(position).getDateAdded().getTime();
        }

        @Override
        public int getPosition(@NonNull Long key){
            for(int i = 0; i < adapter.getItemCount(); i++){
                Image image = adapter.getItem(i);
                if(image.getDateAdded().getTime() == key){
                    return i;
                }
            }
            return 0;
        }
    }
}
