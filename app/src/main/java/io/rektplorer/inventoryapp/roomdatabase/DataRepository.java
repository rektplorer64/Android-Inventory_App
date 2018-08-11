package io.rektplorer.inventoryapp.roomdatabase;

import android.app.Application;
import androidx.lifecycle.LiveData;
import android.os.AsyncTask;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import io.rektplorer.inventoryapp.roomdatabase.DAOs.ImageDAO;
import io.rektplorer.inventoryapp.roomdatabase.DAOs.ItemDAO;
import io.rektplorer.inventoryapp.roomdatabase.DAOs.ReviewDAO;
import io.rektplorer.inventoryapp.roomdatabase.DAOs.UserDAO;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Image;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Item;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Review;
import io.rektplorer.inventoryapp.roomdatabase.Entities.User;

import static java.lang.annotation.RetentionPolicy.CLASS;

public class DataRepository{

    public LiveData<List<Image>> getAllImage(){
        return allImage;
    }

    @Retention(CLASS)
    @IntDef({ENTITY_ITEM, ENTITY_REVIEW, ENTITY_USER, ENTITY_IMAGE})
    public @interface EntityType{
    }

    public static final int ENTITY_ITEM = 0;
    public static final int ENTITY_REVIEW = 1;
    public static final int ENTITY_USER = 2;
    public static final int ENTITY_IMAGE = 3;

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
    private ImageDAO imageDAO;

    private LiveData<List<Item>> allItems;
    private LiveData<List<Review>> allReviews;
    private LiveData<List<User>> allUsers;
    private LiveData<List<Image>> allImage;

    DataRepository(Application application){
        AppDatabase database = AppDatabase.getDatabase(application, AppDatabase.DatabaseInstanceType.ITEMS);
        itemDAO = database.itemDao();
        reviewDAO = database.reviewDao();
        userDAO = database.userDao();
        imageDAO = database.imageDAO();

        allItems = itemDAO.getAll();
        allReviews = reviewDAO.getAll();
        allUsers = userDAO.getAll();
        allImage = imageDAO.getAll();
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
        return reviewDAO.getReviewsByItemId(itemId);
    }

    public LiveData<Image> getHeroImageByItemId(int itemId){
        return imageDAO.getHeroImageByItemId(itemId);
    }

    public LiveData<List<Image>> getImagesByItemId(int itemId){
        return imageDAO.getImagesByItemId(itemId);
    }

    public Image getImageByTimeStamp(long time){
        try{
            return (Image) new FindEntryByColumn(itemDAO, reviewDAO, userDAO, imageDAO).execute(time).get();
        }catch(ExecutionException e){
            e.printStackTrace();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        return null;
    }

    public LiveData<Image> getImageById(int imageId){
        return imageDAO.getImageById(imageId);
    }

    public LiveData<List<Image>> getImagesByUserId(int userId){
        return imageDAO.getImagesByUserId(userId);
    }

    public LiveData<List<Image>> getAllHeroImage(){
        return imageDAO.getAllHeroImage();
    }

    public Set<String> getAllTags(){
        try{
            return new BasicAsyncOperation(itemDAO).execute().get();
        }catch(InterruptedException e){
            e.printStackTrace();
        }catch(ExecutionException e){
            e.printStackTrace();
        }
        return new HashSet<>();
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
        new GeneralDbOperatorAsync(itemDAO, reviewDAO, userDAO, imageDAO, GeneralDbOperatorAsync.INSERT).execute(o);
    }

    public void delete(Object o){
        new GeneralDbOperatorAsync(itemDAO, reviewDAO, userDAO, imageDAO, GeneralDbOperatorAsync.DELETE).execute(o);
    }

    public void update(Object o){
        new GeneralDbOperatorAsync(itemDAO, reviewDAO, userDAO, imageDAO, GeneralDbOperatorAsync.UPDATE).execute(o);
    }

    private static class GeneralDbOperatorAsync extends AsyncTask<Object, Void, Void>{

        private ItemDAO itemDAO;
        private ReviewDAO reviewDAO;
        private UserDAO userDAO;
        private ImageDAO imageDao;

        private final int type;

        @Retention(CLASS)
        @IntDef({INSERT, DELETE, UPDATE})
        public @interface DbOperationType{
        }

        public static final int INSERT = 0;
        public static final int DELETE = 1;
        public static final int UPDATE = 2;

        public GeneralDbOperatorAsync(ItemDAO itemDAO
                , ReviewDAO reviewDAO, UserDAO userDAO, ImageDAO imageDao, @DbOperationType int type){
            this.itemDAO = itemDAO;
            this.reviewDAO = reviewDAO;
            this.userDAO = userDAO;
            this.imageDao = imageDao;
            this.type = type;
        }

        @Override
        protected Void doInBackground(Object... objects){
            for(Object object : objects){
                if(type == INSERT){
                    if(object instanceof Item){
                        itemDAO.insertAll((Item) object);
                    }else if(object instanceof Review){
                        reviewDAO.insertAll((Review) object);
                    }else if(object instanceof User){
                        userDAO.insertAll((User) object);
                    }else if(object instanceof Image){
                        imageDao.insertAll((Image) object);
                    }
                }else if(type == DELETE){
                    if(object instanceof Item){
                        itemDAO.delete((Item) object);
                    }else if(object instanceof Review){
                        reviewDAO.delete((Review) object);
                    }else if(object instanceof User){
                        userDAO.delete((User) object);
                    }else if(object instanceof Image){
                        imageDao.delete((Image) object);
                    }
                }else if(type == UPDATE){
                    if(object instanceof Item){
                        itemDAO.update((Item) object);
                    }else if(object instanceof Review){
                        reviewDAO.update((Review) object);
                    }else if(object instanceof User){
                        userDAO.update((User) object);
                    }else if(object instanceof Image){
                        imageDao.update((Image) object);
                    }
                }
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

    private static class BasicAsyncOperation extends AsyncTask<Void, Void, Set<String>>{
        private final ItemDAO itemDAO;

        BasicAsyncOperation(ItemDAO itemDAO){
            this.itemDAO = itemDAO;
        }

        @Override
        protected Set<String> doInBackground(Void... voids){
            Set<String> allTags = new TreeSet<>(itemDAO.getAllTags());
            Set<String> individualTagSet = new TreeSet<>();
            for(String s: allTags){
                String tagsString[] = s.split(" ");
                individualTagSet.addAll(Arrays.asList(tagsString));
            }
            return individualTagSet;
        }
    }

    private static class FindEntryByColumn extends AsyncTask<Long, Void, Object>{

        private final ItemDAO itemDAO;
        private final ReviewDAO reviewDAO;
        private final UserDAO userDAO;
        private final ImageDAO imageDao;

        FindEntryByColumn(ItemDAO itemDAO, ReviewDAO reviewDAO, UserDAO userDAO, ImageDAO imageDao){
            this.itemDAO = itemDAO;
            this.reviewDAO = reviewDAO;
            this.userDAO = userDAO;
            this.imageDao = imageDao;
        }

        @Override
        protected Object doInBackground(Long... longs){
            //TODO: Expand this functionality
            return imageDao.getImageByTimeStamp(longs[0]);
        }
    }
}
