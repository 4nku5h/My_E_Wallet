package com.example.hackermr.docsholder.Main;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Aux_Function {

    public String get_Date(){
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date data= Calendar.getInstance().getTime();
        String date=dateFormat.format(data);
        String formatted_Date=FormatDate(date);
        return formatted_Date;
    }

    public String get_Day(String date) throws ParseException {
        SimpleDateFormat format1=new SimpleDateFormat("dd/MM/yyyy");
        Date dt1=format1.parse(date);
        DateFormat format2=new SimpleDateFormat("EEEE");
        String day=format2.format(dt1);
        return day;
    }

    public String FormatDate(String date_month_year){
        String[] date_spillitted=date_month_year.split("/");
        String date=date_spillitted[0];
        String month=date_spillitted[1];
        String year=date_spillitted[2];

        if(date.trim().length()==1)
            date="0"+date;
        if(month.trim().length()==1)
            month="0"+month;
        return date+"/"+month+"/"+year;

    }

    public String[] get_date_day_year(String string){
        string=string.trim();
        String[] date_spilit=string.split("/");
        return date_spilit;
    }



}
