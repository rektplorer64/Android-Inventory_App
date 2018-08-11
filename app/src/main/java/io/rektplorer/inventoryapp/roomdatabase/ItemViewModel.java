package io.rektplorer.inventoryapp.roomdatabase;

import android.annotation.SuppressLint;
import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import io.rektplorer.inventoryapp.roomdatabase.Entities.Image;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Item;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Review;
import io.rektplorer.inventoryapp.roomdatabase.Entities.User;

public class ItemViewModel extends AndroidViewModel{

    //TODO: Reorganize this class

    private DataRepository dataRepository;

    private LiveData<List<Item>> allItems;
    private LiveData<List<Review>> allReviews;
    private LiveData<List<User>> allUsers;
    private LiveData<List<Image>> allImage;

    public ItemViewModel(Application application){
        super(application);
        dataRepository = new DataRepository(application);

        allItems = dataRepository.getAllItems();
        allReviews = dataRepository.getAllReviews();
        allUsers = dataRepository.getAllUsers();
        allImage = dataRepository.getAllImage();
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

    public LiveData<Review> getReviewByItemAndUserId(int itemId, int userId){
        return dataRepository.getReviewByItemAndUserId(itemId, userId);
    }

    public LiveData<User> getUserById(int id){
        return dataRepository.getUserById(id);
    }

    public int getItemDomainValue(@DataRepository.EntityType int entityType
            , @DataRepository.DomainType int domainType
            , @DataRepository.ItemFieldType int itemFieldType){
        return dataRepository.getItemDomain(entityType, domainType, itemFieldType);
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

    public LiveData<List<Image>> getAllImage(){
        return allImage;
    }

    public LiveData<Item> getItemById(int itemId){
        return dataRepository.getItemById(itemId);
    }

    public LiveData<Image> getImageById(int imageId){
        return dataRepository.getImageById(imageId);
    }

    public Image getImageByTimeStamp(long time){
        return dataRepository.getImageByTimeStamp(time);
    }

    public LiveData<List<Review>> getReviewsByItemId(int itemId){
        return dataRepository.getReviewsByItemId(itemId);
    }

    public LiveData<List<Image>> getImagesByItemId(int itemId){
        return dataRepository.getImagesByItemId(itemId);
    }

    public LiveData<List<Image>> getImagesByUserId(int userId){
        return dataRepository.getImagesByUserId(userId);
    }

    public LiveData<Image> getHeroImageByItemId(int itemId){
        return dataRepository.getHeroImageByItemId(itemId);
    }

    public LiveData<List<Image>> getAllHeroImage(){
        return dataRepository.getAllHeroImage();
    }

    public Set<String> getAllTags(){
        return dataRepository.getAllTags();
    }

    public int[] getBothNearestIds(int itemId){
        return dataRepository.getBothNearestIds(itemId);
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
}
