package tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import main.java.com.maximeroussy.invitrode.RandomWord;
import main.java.com.maximeroussy.invitrode.WordLengthException;
import tanawinwichitcom.android.inventoryapp.AddItemActivity;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.DAOs.ItemDAO;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.DAOs.ReviewDAO;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.DAOs.UserDAO;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Item;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Review;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.User;

@Database(entities = {Item.class, User.class, Review.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase{

    public enum DatabaseInstanceType{REVIEWS, USERS, ITEMS}

    private static AppDatabase INSTANCE_REVIEWS;
    private static AppDatabase INSTANCE_USERS;
    private static AppDatabase INSTANCE_ITEMS;

    public abstract ItemDAO itemDao();

    public abstract UserDAO userDao();

    public abstract ReviewDAO reviewDao();

    private static RoomDatabase.Callback callback;

    public static AppDatabase getDatabase(final Context context, DatabaseInstanceType databaseInstanceType){

        // The Callback is used to assign an operation for database (Populate the database) when it is created
        callback = new RoomDatabase.Callback(){
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db){
                super.onCreate(db);
                new PopulateDatabaseAsync(INSTANCE_ITEMS, context).execute();
            }
        };

        if(INSTANCE_REVIEWS == null){
            synchronized(AppDatabase.class){
                if(INSTANCE_REVIEWS == null){
                    INSTANCE_REVIEWS = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "reviews")
                            .addCallback(callback).fallbackToDestructiveMigration().build();
                }
            }
        }
        if(INSTANCE_USERS == null){
            synchronized(AppDatabase.class){
                if(INSTANCE_USERS == null){
                    INSTANCE_USERS = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "users")
                            .addCallback(callback).fallbackToDestructiveMigration().build();
                }
            }
        }
        if(INSTANCE_ITEMS == null){
            synchronized(AppDatabase.class){
                if(INSTANCE_ITEMS == null){
                    INSTANCE_ITEMS = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "items")
                            .addCallback(callback).fallbackToDestructiveMigration().build();
                }
            }
        }

        switch(databaseInstanceType){
            case REVIEWS:
                return INSTANCE_REVIEWS;
            case USERS:
                return INSTANCE_USERS;
            case ITEMS:
                return INSTANCE_ITEMS;
        }
        return null;
    }

    /**
     * This class will populate the database in an background thread when it is called.
     * It is required that database operations must be done asynchronously.
     */
    private static class PopulateDatabaseAsync extends AsyncTask<Void, Void, Void>{

        private final ItemDAO itemDAO;
        private final ReviewDAO reviewDAO;
        private final UserDAO userDAO;

        private final Context context;

        /**
         * Default Constructor for PopulateDataAsync class
         *
         * @param appDatabase the custom RoomDatabase class
         * @param context     the context of an activity which is required in order to assign randomized color
         */
        public PopulateDatabaseAsync(AppDatabase appDatabase, Context context){
            itemDAO = appDatabase.itemDao();
            reviewDAO = appDatabase.reviewDao();
            userDAO = appDatabase.userDao();

            this.context = context;
        }

        private void populateItems(){
            Item[] items = new Item[100];
            for(int i = 0; i < 100; i++){
                int timeStamp = (new Random()).nextInt(1000000) + 100000;
                int colorArrPosition = (new Random()).nextInt(AddItemActivity.predefinedColorsResourceIDs.length);
                int colorValue = Color.parseColor(context.getResources().getString(AddItemActivity.predefinedColorsResourceIDs[colorArrPosition]));

                try{
                    StringBuilder descriptionStringBuilder = new StringBuilder();
                    for(int wordCount = 0; wordCount < 100; wordCount++){
                        descriptionStringBuilder.append(RandomWord.getNewWord(4) + " ");
                    }
                    descriptionStringBuilder.append(".");

                    StringBuilder tagStringBuilder = new StringBuilder();
                    for(int wordCount = 0; wordCount < 5; wordCount++){
                        descriptionStringBuilder.append(RandomWord.getNewWord(5) + " ");
                    }

                    String itemName = RandomWord.getNewWord(10);
                    int quantity = (new Random()).nextInt(100);
                    items[i] = new Item(itemName, quantity, descriptionStringBuilder.toString(), colorValue, tagStringBuilder.toString().trim(), null,
                            new Date(timeStamp), null);

                    itemDAO.insertAll(items[i]);
                }catch(WordLengthException e){
                    e.printStackTrace();
                    Log.e(e.getMessage(), "Invalid Word length");
                }
            }
        }

        private void populateUsers(){
            ArrayList<User> userArrayList = new ArrayList<>();
            userArrayList.add(new User("rektplorer64", "Tanawin"
                    , "Wichit", "913680000", "tanawin46840"));
            userDAO.insertAll(userArrayList.get(0));
        }

        private void populateReviews(){
            Review[] reviews = new Review[100];
            for(int i = 0; i < 100; i++){
                int randomizedTime = (new Random()).nextInt(1000000) + 100000;
                reviews[i] = new Review(String.valueOf(randomizedTime), 1, 1
                        , RandomStringUtils.random(200)
                        , ThreadLocalRandom.current().nextDouble(0, 5));
                reviewDAO.insertAll(reviews[i]);
            }
        }

        /**
         * The driver function of AsyncTask
         *
         * @param voids void
         *
         * @return void
         */
        @Override
        protected Void doInBackground(Void... voids){
            populateItems();
            populateUsers();
            populateReviews();
            return null;
        }
    }
}
