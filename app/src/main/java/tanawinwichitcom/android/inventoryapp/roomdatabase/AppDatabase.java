package tanawinwichitcom.android.inventoryapp.roomdatabase;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import main.java.com.maximeroussy.invitrode.RandomWord;
import main.java.com.maximeroussy.invitrode.WordLengthException;
import tanawinwichitcom.android.inventoryapp.roomdatabase.DAOs.ItemDAO;
import tanawinwichitcom.android.inventoryapp.roomdatabase.DAOs.ReviewDAO;
import tanawinwichitcom.android.inventoryapp.roomdatabase.DAOs.UserDAO;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Review;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.User;

@Database(entities = {Item.class, User.class, Review.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase{

    private static AppDatabase INSTANCE_REVIEWS;
    private static AppDatabase INSTANCE_USERS;
    private static AppDatabase INSTANCE_ITEMS;
    private static RoomDatabase.Callback callback;

    public static AppDatabase getDatabase(final Context context, DatabaseInstanceType databaseInstanceType){

        // The Callback is used to assign an operation for database (Populate the database) when it is created
        callback = new RoomDatabase.Callback(){
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db){
                super.onCreate(db);
                new PopulateDatabaseAsync(INSTANCE_ITEMS).execute();
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

    public abstract ItemDAO itemDao();

    public abstract UserDAO userDao();

    public abstract ReviewDAO reviewDao();

    public enum DatabaseInstanceType{REVIEWS, USERS, ITEMS}

    /**
     * This class will populate the database in an background thread when it is called.
     * It is required that database operations must be done asynchronously.
     */
    private static class PopulateDatabaseAsync extends AsyncTask<Void, Integer, Void>{

        private final ItemDAO itemDAO;
        private final ReviewDAO reviewDAO;
        private final UserDAO userDAO;

        public static final int MAXIMUM_ITEMS = 100;
        public static final int MAXIMUM_REVIEWS = 10;

        /**
         * Default Constructor for PopulateDataAsync class
         *
         * @param appDatabase the custom RoomDatabase class
         */
        public PopulateDatabaseAsync(AppDatabase appDatabase){
            itemDAO = appDatabase.itemDao();
            reviewDAO = appDatabase.reviewDao();
            userDAO = appDatabase.userDao();
        }

        private void populateItems(){
            Item[] items = new Item[MAXIMUM_ITEMS];
            for(int i = 0; i < MAXIMUM_ITEMS; i++){
                publishProgress(i);
                long timeStamp = (Calendar.getInstance().getTimeInMillis() + (new Random()).nextInt(1000000));

                // Random Color
                Random rand = new Random();
                int r = rand.nextInt(255);
                int g = rand.nextInt(255);
                int b = rand.nextInt(255);
                int colorValue = Color.rgb(r, g, b);

                try{
                    StringBuilder descriptionStringBuilder = new StringBuilder();
                    for(int wordCount = 0; wordCount < 100; wordCount++){
                        descriptionStringBuilder.append(RandomWord.getNewWord(4)).append(" ");
                    }
                    descriptionStringBuilder.append(".");

                    for(int wordCount = 0; wordCount < 5; wordCount++){
                        descriptionStringBuilder.append(RandomWord.getNewWord(5)).append(" ");
                    }

                    String itemName = RandomWord.getNewWord(10);
                    int quantity = (new Random()).nextInt(1000000);

                    HashSet<String> stringHashSet = new HashSet<>();
                    for(int j = 0; j < 10; j++){
                        String randomizedWord = RandomWord.getNewWord(10).toLowerCase();
                        stringHashSet.add(StringUtils.capitalize(randomizedWord));
                    }

                    items[i] = new Item(itemName, quantity, descriptionStringBuilder.toString(), colorValue, stringHashSet, null,
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
            Review[] reviews = new Review[MAXIMUM_REVIEWS];
            for(int itemIndex = 0; itemIndex < MAXIMUM_ITEMS; itemIndex++){
                for(int reviewIndex = 0; reviewIndex < MAXIMUM_REVIEWS; reviewIndex++){
                    //int randomizedTime = (new Random()).nextInt(1000000) + 100000;
                    reviews[reviewIndex] = new Review(Calendar.getInstance().getTime(), 1, itemIndex
                            , RandomStringUtils.random(200)
                            , ThreadLocalRandom.current().nextDouble(0.5, 5));
                    reviewDAO.insertAll(reviews[reviewIndex]);
                }
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
            //Toast.makeText(context, "Populating Database", Toast.LENGTH_SHORT).show();
            populateItems();
            populateUsers();
            populateReviews();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values){
            super.onProgressUpdate(values);
        }
    }
}
