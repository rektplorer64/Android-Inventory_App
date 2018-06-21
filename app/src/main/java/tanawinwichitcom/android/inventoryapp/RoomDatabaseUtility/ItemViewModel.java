package tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Item;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Review;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.User;

public class ItemViewModel extends AndroidViewModel{

    private DataRepository dataRepository;

    private LiveData<List<Item>> allItems;
    private LiveData<List<Review>> allReviews;
    private LiveData<List<User>> allUsers;

    public ItemViewModel(Application application){
        super(application);
        dataRepository = new DataRepository(application);

        allItems = dataRepository.getAllItems();
        allReviews = dataRepository.getAllReviews();
        allUsers = dataRepository.getAllUsers();
    }

    public LiveData<List<Item>> getAllItems(){
        return allItems;
    }

    public LiveData<List<Review>> getAllReviews(){
        return allReviews;
    }

    public LiveData<List<User>> getAllUsers(){
        return allUsers;
    }

    public LiveData<Item> getItemById(int itemId){
        return dataRepository.getItemById(itemId);
    }

    public void insert(Object o){
        dataRepository.insert(o);
    }

    public void delete(Object o){
        dataRepository.delete(o);
    }

    public void update(Object o){
        dataRepository.update(o);
    }

    public static SparseArray<ArrayList<Review>> convertReviewListToSparseArray(List<Review> reviewList){
        SparseArray<ArrayList<Review>> reviewSparseArray = new SparseArray<>();
        for(Review review : reviewList){
            if(reviewSparseArray.get(review.getItemId()) == null){
                reviewSparseArray.put(review.getItemId(), new ArrayList<Review>());
            }
            reviewSparseArray.get(review.getItemId()).add(review);
        }
        return reviewSparseArray;
    }

    public static HashMap<Integer, ArrayList<Review>> convertReviewListToHashMap(List<Review> reviewList){
        @SuppressLint("UseSparseArrays") HashMap<Integer, ArrayList<Review>> reviewHashMap = new HashMap<>();
        for(Review review : reviewList){
            if(reviewHashMap.get(review.getItemId()) == null){
                reviewHashMap.put(review.getItemId(), new ArrayList<Review>());
            }
            reviewHashMap.get(review.getItemId()).add(review);
        }
        return reviewHashMap;
    }
}
