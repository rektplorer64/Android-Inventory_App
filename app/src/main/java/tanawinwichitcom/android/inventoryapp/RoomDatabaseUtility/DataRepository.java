package tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.DAOs.ItemDAO;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.DAOs.ReviewDAO;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.DAOs.UserDAO;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Item;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Review;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.User;

public class DataRepository{

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

    public LiveData<List<Item>> getAllItems(){
        return allItems;
    }

    public LiveData<List<Review>> getAllReviews(){
        return allReviews;
    }

    public LiveData<List<User>> getAllUsers(){
        return allUsers;
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
            if(objects[0] instanceof Item){
                itemDAO.delete((Item) objects[0]);
            }else if(objects[0] instanceof Review){
                reviewDAO.delete((Review) objects[0]);
            }else if(objects[0] instanceof User){
                userDAO.delete((User) objects[0]);
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
}
