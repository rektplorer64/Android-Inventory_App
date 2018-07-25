package tanawinwichitcom.android.inventoryapp.roomdatabase.Entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User{

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "username")
    private String username;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "surname")
    private String surname;

    @ColumnInfo(name = "birthday")
    private String birthday_timestamp;

    @ColumnInfo(name = "password")
    private String password;

    public User(String username, String name, String surname, String birthday_timestamp, String password){
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.birthday_timestamp = birthday_timestamp;
        this.password = password;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getSurname(){
        return surname;
    }

    public void setSurname(String surname){
        this.surname = surname;
    }

    public String getBirthday_timestamp(){
        return birthday_timestamp;
    }

    public void setBirthday_timestamp(String birthday_timestamp){
        this.birthday_timestamp = birthday_timestamp;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }
}
