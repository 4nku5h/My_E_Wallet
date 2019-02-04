package com.example.hackermr.docsholder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String DatabaseName="databasename";
    //public static String TableName="projectsTable";
    public static final String TableName="tabalea";
    public static final String Col1="ID";
    public static final String Col2="date";
    public static final String Col3="item";
    public static final String Col4="cost";
    public static final String Col5="day";
    Context context;

    public DataBaseHelper(Context context,String TableName){
        super(context, TableName, null, 1);
        this.context=context;
    }




    @Override
    public void onCreate(SQLiteDatabase db){

    db.execSQL("create table " + TableName +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,date TEXT,item TEXT,cost INTEGER,day TEXT )");
       // db.execSQL("create table " + TableName +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,projects TEXT,Uri TEXT,MaxLimit INTEGER )");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    db.execSQL("DROP TABLE IF EXISTS "+ TableName);
    onCreate(db);

    }


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
                                            //+++Insert+++//

    public boolean InsertToDataBase(String date,String item,Long cost,String day){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(Col2,date);
        contentValues.put(Col3,item);
        contentValues.put(Col4,cost.toString());
        contentValues.put(Col5,day);
        long result=db.insert(TableName,null,contentValues);
        if(result==-1)
            return false;
        else
        return true;
    }



    public Cursor getDataFrom_DataBase(){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cursor=db.rawQuery("select * from "+TableName,null);
        return cursor;
    }
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
                                          //+++Delete+++//

    public boolean deleteFromDataBase(Integer id){
        SQLiteDatabase db=this.getWritableDatabase();
        String [] dta={id.toString()};
        long status=db.delete(TableName,"ID=?",dta);
        if (status==-1)
            return false;
        else
            return true;

    }

    public boolean Update(Integer id,String date,String item,Long cost,String day){
        SQLiteDatabase db=this.getWritableDatabase();
        String [] data={id.toString()};
        ContentValues cv=new ContentValues();
        cv.put(Col2,date);
        cv.put(Col3,item);
        cv.put(Col4,cost.toString());
        cv.put(Col5,day);
        long status=db.update(TableName,cv,"ID=?",data);
        if(status==-1) {
            return false;
        }
            return true;

    }
    public Cursor Query(String query){
        String[] projection={Col1,Col2,Col3,Col4,Col5};
        String[] selectonArgs={query};
        SQLiteDatabase mydb=this.getWritableDatabase();
        Cursor cursor=mydb.query(TableName,projection,Col3 +" like ?",selectonArgs,null,null,Col3);
        //Cursor cursor=mydb.query(TableName,projection,Col3 +" like ?",selectonArgs,null,null,Col3);
        //Cursor cursor=mydb.query(TableName,projection,Col3 +"= ?",selectonArgs,null,null,Col3);

        return cursor;

    }
}