package tanawinwichitcom.android.inventoryapp.DialogFragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.RecycleViewAdapters.UserReviewAdapter;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.User;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.ItemViewModel;

import static android.widget.ListPopupWindow.WRAP_CONTENT;

public class AllReviewDialogFragment extends DialogFragment{

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.dialog_allreviews, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        final ItemViewModel itemViewModel = ViewModelProviders.of(getActivity()).get(ItemViewModel.class);
        getDialog().getWindow().setLayout(400, WRAP_CONTENT);

        final RecyclerView allReviewRecycleView = view.findViewById(R.id.allReviewRecycleView);

        final int itemId = getArguments().getParcelable("itemId");
        final UserReviewAdapter userReviewAdapter = new UserReviewAdapter(itemViewModel.getAllReviews().getValue(), itemId);

        allReviewRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        allReviewRecycleView.setAdapter(userReviewAdapter);

        itemViewModel.getAllUsers().observe(getActivity(), new Observer<List<User>>(){
            @Override
            public void onChanged(@Nullable List<User> users){
                userReviewAdapter.applyUserDataChange(users);
            }
        });


        TextView titleTextView = view.findViewById(R.id.titleTextView);
        titleTextView.setText("All Reviews (" + itemViewModel.getAllReviews().getValue().size() + ")");

        allReviewRecycleView.setHasFixedSize(true);

        Button reviewCloseButton = view.findViewById(R.id.reviewCloseButton);
        reviewCloseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dismiss();
            }
        });

    }
}
