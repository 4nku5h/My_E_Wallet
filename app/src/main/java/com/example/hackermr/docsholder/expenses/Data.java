package com.example.hackermr.docsholder.expenses;

public class Data {
    Integer status=0;
    Integer index;
    String date;
    String item;
    Long money;
    String day;

    public Data(Integer index,String date,String item,Long money,String day){
        this.index=index;
        this.date=date;
        this.item=item;
        this.money=money;
        this.day=day;
    }
}

