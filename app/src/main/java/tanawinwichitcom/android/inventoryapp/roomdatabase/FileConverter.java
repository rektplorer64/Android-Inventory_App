package tanawinwichitcom.android.inventoryapp.roomdatabase;

import androidx.room.TypeConverter;

import java.io.File;

public class FileConverter{
    @TypeConverter
    public static File urlToFile(String fileUrl){
        return (fileUrl == null) ? null : new File(fileUrl);
    }

    @TypeConverter
    public static String fileToUrl(File file){
        if(file != null){
            return file.getPath();
        }else{
            return null;
        }
    }
}
