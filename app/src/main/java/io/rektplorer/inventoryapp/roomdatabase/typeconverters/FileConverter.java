package io.rektplorer.inventoryapp.roomdatabase.typeconverters;


import java.io.File;
import java.io.IOException;

import androidx.room.TypeConverter;

public class FileConverter{
    @TypeConverter
    public static File stringPathToFile(String filePath){
        if(filePath != null && !filePath.isEmpty()){
            return new File(filePath);
        }
        return null;
    }

    @TypeConverter
    public static String fileToStringPath(File imageFile){
        return (imageFile != null) ? imageFile.getPath() : "";
    }

    private static void renameFile(File file, String newName) throws IOException{
        // File (or directory) with new name
        File file2 = new File(newName);
        if(file2.exists()){
            throw new java.io.IOException("File exists");
        }
        // Rename file (or directory)
        file.renameTo(file2);
    }
}
