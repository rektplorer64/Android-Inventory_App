package io.rektplorer.inventoryapp.rvadapters;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    private Context context;
    private boolean editMode;
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

    public ItemImageAdapter(Context context, boolean editMode){
        super(IMAGE_FILE_DIFF_UTIL);
        this.context = context;
        this.editMode = editMode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        CardEditImageBinding binding = CardEditImageBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        holder.bindToView(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    public void applyDataChanges(List<Image> imageFileList){
        submitList(imageFileList);
        notifyDataSetChanged();
        Log.d(this.getClass().getName(), "Submitted " + imageFileList.size() + " image(s) to adapter");
    }

    public void setSelectionTracker(SelectionTracker selectionTracker){
        this.selectionTracker = selectionTracker;
    }

    public void setDeleteClickListener(DeleteClickListener deleteClickListener){
        this.deleteClickListener = deleteClickListener;
    }

    public interface DeleteClickListener{
        void onDelete(File imageFile, int position);
    }

    public final class ViewHolder extends RecyclerView.ViewHolder implements Detailable{
        private CardEditImageBinding binding;

        private ViewHolder(@NonNull CardEditImageBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindToView(final int position){
            final File imageFile = getItem(position).getImageFile();
            final long key = getItem(position).getDateAdded().getTime();

            if(selectionTracker == null && editMode){
                return;
            }

            Glide.with(context)
                    .load(imageFile)
                    .apply(RequestOptions.centerCropTransform())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .thumbnail(0.01f)
                    .into(binding.imageView);

            binding.imageTitle.setText(String.valueOf(position + 1));

            if(editMode){
                binding.heroImageSelector.setVisibility(View.VISIBLE);
                binding.heroImageSelector.setImageResource((selectionTracker.isSelected(key)) ?
                        R.drawable.ic_check_circle_24dp : R.drawable.ic_radio_button_unchecked_black_24dp);

                binding.heroImageSelector.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        if(!selectionTracker.isSelected(key)){
                            selectionTracker.select(key);
                        }
                    }
                });

                binding.removeImageButton.setVisibility(View.VISIBLE);
                binding.removeImageButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        if(deleteClickListener != null){
                            deleteClickListener.onDelete(imageFile, position);
                        }
                    }
                });
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
                    return (long) getItem(getAdapterPosition()).getDateAdded().getTime();
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
            super(ItemKeyProvider.SCOPE_MAPPED);
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
