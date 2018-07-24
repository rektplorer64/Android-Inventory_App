package tanawinwichitcom.android.inventoryapp.roomdatabase;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.util.List;
import java.util.concurrent.ExecutionException;

import tanawinwichitcom.android.inventoryapp.roomdatabase.DAOs.ItemDAO;
import tanawinwichitcom.android.inventoryapp.roomdatabase.DAOs.ReviewDAO;
import tanawinwichitcom.android.inventoryapp.roomdatabase.DAOs.UserDAO;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Review;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.User;

import static java.lang.annotation.RetentionPolicy.CLASS;

public class DataRepository{

    @Retention(CLASS)
    @IntDef({ENTITY_ITEM, ENTITY_REVIEW, ENTITY_USER})
    public @interface EntityType{
    }

    public static final int ENTITY_ITEM = 0;
    public static final int ENTITY_REVIEW = 1;
    public static final int ENTITY_USER = 2;

    @Retention(CLASS)
    @IntDef({MAX_VALUE, MIN_VALUE})
    public @interface DomainType{
    }

    public static final int MAX_VALUE = 0;
    public static final int MIN_VALUE = 1;

    @Retention(CLASS)
    @IntDef({ITEM_FIELD_NAME, ITEM_FIELD_QUANTITY, ITEM_FIELD_ID})
    public @interface ItemFieldType{
    }

    public static final int ITEM_FIELD_ID = 0;
    public static final int ITEM_FIELD_NAME = 1;
    public static final int ITEM_FIELD_QUANTITY = 2;

    private ItemDAO itemDAO;
    private ReviewDAO reviewDAO;
    private UserDAO userDAO;

    private LiveData<List<Item>> allItems;
    private LiveData<List<Review>> allReviews;
    private LiveData<List<User>> allUsers;

    DataRepository(Application application){
        AppDatabase database = AppDatabase.getDatabase(application, AppDatabase.DatabaseInstanceType.ITEMS);
        itemDAO = database.itemDao();
        reviewDAO = database.reviewDao();
        userDAO = database.userDao();

        allItems = itemDAO.getAll();
        allReviews = reviewDAO.getAll();
        allUsers = userDAO.getAll();
    }



    public LiveData<Review> getReviewByItemAndUserId(int itemId, int userId){
        return reviewDAO.getReviewByItemAndUserId(itemId, userId);
    }

    public LiveData<User> getUserById(int id){
        return userDAO.findUserById(id);
    }

    public int getItemDomain(@EntityType int entityType, @DomainType int domainType, @ItemFieldType int itemFieldType){
        try{
            return new FindDomainItemAsyncTask(itemDAO, entityType, domainType, itemFieldType).execute().get();
        }catch(InterruptedException e){
            e.printStackTrace();
        }catch(ExecutionException e){
            e.printStackTrace();
        }
        return 0;
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
        return itemDAO.getItemById(itemId);
    }

    public LiveData<List<Review>> getReviewsByItemId(int itemId){
        return reviewDAO.findByItemId(itemId);
    }

    public int[] getBothNearestIds(int itemId){
        try{
            return new FindBothNearestIds(itemDAO).execute(itemId).get();
        }catch(InterruptedException e){
            e.printStackTrace();
        }catch(ExecutionException e){
            e.printStackTrace();
        }
        return new int[]{0};
    }

    public void insert(Object o){
        new InsertAsyncTask(itemDAO, reviewDAO, userDAO).execute(o);
    }

    public void delete(Object o){
        new DeleteAsyncTask(itemDAO, reviewDAO, userDAO).execute(o);
    }

    public void update(Object o){
        new UpdateAsyncTask(itemDAO, reviewDAO, userDAO).execute(o);
    }

    private static class InsertAsyncTask extends AsyncTask<Object, Void, Void>{

        private ItemDAO itemDAO;
        private ReviewDAO reviewDAO;
        private UserDAO userDAO;

        public InsertAsyncTask(ItemDAO itemDAO, ReviewDAO reviewDAO, UserDAO userDAO){
            this.itemDAO = itemDAO;
            this.reviewDAO = reviewDAO;
            this.userDAO = userDAO;
        }

        @Override
        protected Void doInBackground(Object... objects){
            if(objects[0] instanceof Item){
                itemDAO.insertAll((Item) objects[0]);
            }else if(objects[0] instanceof Review){
                reviewDAO.insertAll((Review) objects[0]);
            }else if(objects[0] instanceof User){
                userDAO.insertAll((User) objects[0]);
            }
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Object, Void, Void>{

        private ItemDAO itemDAO;
        private ReviewDAO reviewDAO;
        private UserDAO userDAO;

        public DeleteAsyncTask(ItemDAO itemDAO, ReviewDAO reviewDAO, UserDAO userDAO){
            this.itemDAO = itemDAO;
            this.reviewDAO = reviewDAO;
            this.userDAO = userDAO;
        }

        @Override
        protected Void doInBackground(Object... objects){
            for(int i = 0; i < objects.length; i++){
                if(objects[i] instanceof Item){
                    itemDAO.delete((Item) objects[i]);
                }else if(objects[i] instanceof Review){
                    reviewDAO.delete((Review) objects[i]);
                }else if(objects[i] instanceof User){
                    userDAO.delete((User) objects[i]);
                }
            }
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Object, Void, Void>{

        private ItemDAO itemDAO;
        private ReviewDAO reviewDAO;
        private UserDAO userDAO;

        public UpdateAsyncTask(ItemDAO itemDAO, ReviewDAO reviewDAO, UserDAO userDAO){
            this.itemDAO = itemDAO;
            this.reviewDAO = reviewDAO;
            this.userDAO = userDAO;
        }

        @Override
        protected Void doInBackground(Object... objects){
            if(objects[0] instanceof Item){
                itemDAO.update((Item) objects[0]);
            }else if(objects[0] instanceof Review){
                reviewDAO.update((Review) objects[0]);
            }else if(objects[0] instanceof User){
                userDAO.update((User) objects[0]);
            }
            return null;
        }
    }

    private static class FindDomainItemAsyncTask extends AsyncTask<Void, Void, Integer>{

        private ItemDAO itemDAO;

        private final int entityType;
        private final int domainType;
        private final int itemFieldType;

        public FindDomainItemAsyncTask(ItemDAO itemDAO, @EntityType int entityType, @DomainType int domainType, @ItemFieldType int itemFieldType){
            this.itemDAO = itemDAO;
            this.entityType = entityType;
            this.domainType = domainType;
            this.itemFieldType = itemFieldType;
        }

        @Override
        protected Integer doInBackground(Void... voids){
            if(domainType == MIN_VALUE){
                if(itemFieldType == ITEM_FIELD_ID){
                    return itemDAO.getMinItemId();
                }else if(itemFieldType == ITEM_FIELD_QUANTITY){
                    return itemDAO.getMinItemQuantity();
                }
            }else{
                return itemDAO.getMaxItemQuantity();
            }
            return 0;
        }
    }

    private static class FindBothNearestIds extends AsyncTask<Integer, Void, int[]>{

        private final ItemDAO itemDAO;

        public FindBothNearestIds(ItemDAO itemDAO){
            this.itemDAO = itemDAO;
        }

        @Override
        protected int[] doInBackground(Integer... integers){
            return itemDAO.getBothNearestIds(integers[0]);
        }
    }
}
