package com.example.hackermr.docsholder.expenses;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hackermr.docsholder.DataBaseHelper;
import com.example.hackermr.docsholder.Generate_Pdf;
import com.example.hackermr.docsholder.Main.Aux_Function;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.shrivastava.myewallet.R;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Expenture_Activity extends AppCompatActivity {
    static RecyclerView recyclerView;
    DataBaseHelper myDataBase;
    String TableName="expentureTable";
    String normalOperation="Normal";
    String editOperation="Edit";
    Cursor cursor;
    static ArrayList<Data> listOFExp;
    static ArrayList<Integer> selected_ElementList;
    Button et_from;
    Button et_to;

    Integer from_year;
    Integer from_month;
    Integer from_day;

    Integer to_year;
    Integer to_month;
    Integer to_day;
    Dialog curr_dialog;
    MenuInflater menuInflater;
    static Menu menu;
    FloatingActionButton btn_plus_minus;
    TextView tv_SortDetail;

    String fromDate=null;
    String toDate=null;
    static Boolean selectAll_Element=false;
    static Boolean selectAll_enabled=false;
    static Boolean isAnyElement_selected=false;
    static RecyclerAdapter myAdapter;
    Aux_Function date_Functions;
    static Integer totalElment_in_recyclerView;
    static Context context;
    Integer maxItemString_Length=0;
    SearchView searchView;
    Boolean add_new_data_Dialog_Open=false;
    Boolean sortDialog_open=false;
    String operation="";
    String temp_cost_savedInstance ="";
    String temp_item_savedInstance ="";
    Long temp_cost;
    String dialog_current_date;
    public Boolean is_minus_state=true;
    Long total_amount=0l;
    Long loss= 0l;
    Long profit= 0l;
    FloatingActionButton btn_AddNewData;
    static Integer scroll_position=null;
    private InterstitialAd interstitialAd;
    Boolean external_storage_permission=false;

    //DatePickerDialog datePickerDialog;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expences);
        //getSupportActionBar().setTitle("My Wallet");
        btn_AddNewData=findViewById(R.id.btn_add_new_data);
        checkPermissions("location");
        date_Functions=new Aux_Function();
        MobileAds.initialize(this,"ca-app-pub-1668615702809607~1546671143");

        try {
            setColor_of_Day();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        menuInflater = getMenuInflater();
        context=getApplicationContext();
        myDataBase = new DataBaseHelper(this, TableName);
        recyclerView = findViewById(R.id.listview_exp);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),DividerItemDecoration.VERTICAL));
        listOFExp = new ArrayList<Data>();
        selected_ElementList = new ArrayList<Integer>();
        tv_SortDetail = findViewById(R.id.tv_SortDetail);
        getDatabaseCursor();
        if (cursor.getCount() != 0) {
            try {
                getDataFromDataBase();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            setAdapterOrUpdate();
        }

    }

    public void checkPermissions(String permission){

        switch (permission){
            case "externalStorage":
                if(ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                     external_storage_permission=true;
                    return;
                }else
                    requestPermission(permission);
                break;

            case "location":
                if(ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                      return;
                }  else{
                    requestPermission(permission);
                }
                break;
        }
    }

    public void requestPermission(String permission){
        switch (permission){
            case "externalStorage":
                ActivityCompat.requestPermissions(this
                        ,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0000);
            case "location":
                ActivityCompat.requestPermissions(this
                        ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1111);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0000: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    external_storage_permission = true;
                }
                else {
                    Toast.makeText(getApplicationContext(),"WRITE EXTERNAL STORAGE Permission not granted",Toast.LENGTH_SHORT).show();
                }
            }
            //not checking location permission its not necessary;


                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        menuInflater.inflate(R.menu.main_menu_expenture,menu);
        this.menu=menu;
        if(selected_ElementList.size()>0){
            ToggleMenu(0);
        }

        if(selected_ElementList.size()==0 || selected_ElementList.size()>1){
            MenuItem item_edit = menu.findItem(R.id.menu_edit);
            item_edit.setVisible(false);
        }

         searchView=(SearchView) menu.findItem(R.id.menu_search_bar).getActionView();
        SearchManager searchManager=(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.trim().length()<=15){
                    cursor=myDataBase.Query("%"+query+"%");
                    try {
                        getDataFromDataBase();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    setAdapterOrUpdate();

                    if(query.length()==0) {
                        Boolean is_Sorting_needed=split_Date();
                        try {
                            if(is_Sorting_needed)
                                sortData();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.trim().length()<=15){
                    cursor=myDataBase.Query("%"+newText+"%");
                    try {
                        getDataFromDataBase();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if(newText.length()==0) {
                        Boolean is_Sorting_needed=split_Date();
                        try {
                            if(is_Sorting_needed) {
                                sortData();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace(  );
                        }
                    }
                    setAdapterOrUpdate();
                }
                return false;
            }

        });

        return super.onCreateOptionsMenu(menu);
    }

    public  void prepareAd_and_Show(){
        interstitialAd=new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-1668615702809607/6670951419");
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                if(interstitialAd.isLoaded())
                    interstitialAd.show();
            }
        });
        if(interstitialAd.isLoaded()){
            interstitialAd.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_generate_Pdf:
                try {
                    checkPermissions("externalStorage");
                    if(external_storage_permission.equals(true)){
                        generatePdfRecord();
                    }
                   prepareAd_and_Show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.menu_sort:
                if(listOFExp.size()>0) {
                    prepareAd_and_Show();
                    show_Dialog_sort();
                }else
                    Toast.makeText(getApplicationContext(),"Transaction not found",Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_refresh:
                fromDate=null;
                toDate=null;
                total_amount=0l;
                loss= 0l;
                profit= 0l;
                selectAll_enabled=false;
                isAnyElement_selected =false;
                tv_SortDetail.setText("");
                tv_SortDetail.setVisibility(View.INVISIBLE);
                getDatabaseCursor();
                if(cursor.getCount()!=0) {
                    try {
                        getDataFromDataBase();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    setAdapterOrUpdate();
                }else
                    Toast.makeText(getApplicationContext(),"Transaction not found",Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_calculate:
                if(listOFExp.size()>0){
                    try{
                        calculate();
                        prepareAd_and_Show();
                    }catch(Exception ex){}
                }else
                    Toast.makeText(getApplicationContext(),"Transaction not found",Toast.LENGTH_SHORT).show();
                break;

            case R.id.delete:
                if(selected_ElementList.size()>0){
                    show_delete_alert();
                }
                break;
            case R.id.menu_edit:
                show_Dialog_add_new_data(editOperation);
                break;
            case R.id.menu_selectAll:
                myAdapter.SelectAll();
                break;

            case R.id.menu_info:
                Dialog dialog=new Dialog(this);
                dialog.setContentView(R.layout.dialog_info);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setCancelable(true);
                dialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void ToggleMenu(int menu_Type){
        MenuItem item_delete =menu.findItem(R.id.delete);
        MenuItem item_refersh =menu.findItem(R.id.menu_refresh);
        MenuItem item_calculate =menu.findItem(R.id.menu_calculate);
        MenuItem item_gen_pdf =menu.findItem(R.id.menu_generate_Pdf);
        MenuItem item_sort =menu.findItem(R.id.menu_sort);
        MenuItem item_selectAll =menu.findItem(R.id.menu_selectAll);
        MenuItem item_edit =menu.findItem(R.id.menu_edit);
        MenuItem item_search=menu.findItem(R.id.menu_search_bar);

        if(menu_Type==0){
            // delete, select all , edit option visble
            item_delete.setVisible(true);
            item_selectAll.setVisible(true);
            item_edit.setVisible(true);
            item_refersh.setVisible(false);
            item_calculate.setVisible(false);
            item_gen_pdf.setVisible(false);
            item_sort.setVisible(false);
            item_search.setVisible(false);

            return;
        }
        else if(menu_Type==1){
            // refresh, calculate , pdf , sort visible
            item_delete.setVisible(false);
            item_selectAll.setVisible(false);
            item_edit.setVisible(false);
            item_refersh.setVisible(true);
            item_calculate.setVisible(true);
            item_gen_pdf.setVisible(true);
            item_sort.setVisible(true);
            item_search.setVisible(true);
            return;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setColor_of_Day() throws ParseException {
        String day=date_Functions.get_Day(date_Functions.get_Date());
        int colorid_actionbar;
        int colorid_statusbar;
        int colorid_button;
        switch (day){
            case "Sunday":
                colorid_actionbar=getResources().getColor(R.color.sunday);
                colorid_statusbar=getResources().getColor(R.color.sunday_accent);
                colorid_button=getResources().getColor(R.color.sunday_button);
                break;

            case "Monday":
                colorid_actionbar=getResources().getColor(R.color.monday);
                colorid_statusbar=getResources().getColor(R.color.monday_accent);
                colorid_button=getResources().getColor(R.color.monday_button);
                break;

            case "Tuesday":
                colorid_actionbar=getResources().getColor(R.color.tuesday);
                colorid_statusbar=getResources().getColor(R.color.tuesday_accent);
                colorid_button=getResources().getColor(R.color.tuesday_button);
                break;

            case "Wednesday":
                colorid_actionbar=getResources().getColor(R.color.wednesday);
                colorid_statusbar=getResources().getColor(R.color.wednesday_accent);
                colorid_button=getResources().getColor(R.color.wednesday_button);
                break;

            case "Thursday":
                colorid_actionbar=getResources().getColor(R.color.thursday);
                colorid_statusbar=getResources().getColor(R.color.thursday_accent);
                colorid_button=getResources().getColor(R.color.thursday_button);
                break;

            case "Friday":
                colorid_actionbar=getResources().getColor(R.color.friday);
                colorid_statusbar=getResources().getColor(R.color.friday_accent);
                colorid_button=getResources().getColor(R.color.friday_button);
                break;

            case "Saturday":
                colorid_actionbar=getResources().getColor(R.color.saturday);
                colorid_statusbar=getResources().getColor(R.color.saturday_accent);
                colorid_button=getResources().getColor(R.color.saturday_button);
                break;

            default:
                colorid_actionbar=getResources().getColor(R.color.sunday);
                colorid_statusbar=getResources().getColor(R.color.sunday_accent);
                colorid_button=getResources().getColor(R.color.sunday_button);
                break;
        }
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colorid_actionbar));
        getWindow().setStatusBarColor(colorid_statusbar);
        btn_AddNewData.setBackgroundTintList(ColorStateList.valueOf(colorid_button));
       //changing color when pressed
        btn_AddNewData.setRippleColor(colorid_statusbar);
    }
    public void show_delete_alert(){
        String count=String.valueOf(selected_ElementList.size());
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        if(selected_ElementList.size()==1){
            builder.setMessage("Delete Transaction?");
        }else{
            builder.setMessage("Delete "+ count+" Transactions?");

        }
        builder
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Delete_item();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }
    public  void btn_new_data(View v){
        show_Dialog_add_new_data(normalOperation);
    }

    public void calculate(){
        if(listOFExp.size()==0){
            return;
        }
        show_calculate_dialog(total_amount,profit,loss);
        return;
    }
    public void show_calculate_dialog(Long wallet,Long creadit,Long debit){
        Dialog dialog=new Dialog(this);
        dialog.setContentView(R.layout.dialog_calculation_result);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        TextView tv_creadit=dialog.findViewById(R.id.tv_creadit);
        TextView tv_debit=dialog.findViewById(R.id.tv_debit);
        TextView tv_wallet=dialog.findViewById(R.id.tv_wallet);
        tv_creadit.setText(creadit.toString());
        tv_debit.setText(debit.toString());
        tv_wallet.setText(wallet.toString());

    }


    public void getDatabaseCursor(){
        cursor=myDataBase.getDataFrom_DataBase();
    }
    public void getDataFromDataBase() throws ParseException {
        if(cursor.getCount()==0){
            listOFExp.clear();
            return;
        }
        listOFExp.clear();
         total_amount=0l;
         loss= 0l;
         profit= 0l;
        cursor.moveToFirst();
        do{
            Integer index=cursor.getInt(0);
            String date=cursor.getString(1);
            String item=cursor.getString(2);
            Long money=cursor.getLong(3);
            String day=cursor.getString(4);
            Data data=new Data(index,date,item,money,day);
            if(fromDate!=null && toDate !=null){
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy");
                Date sdf_from=simpleDateFormat.parse(fromDate);
                Date sdf_to=simpleDateFormat.parse(toDate);
                Date sdf_date=simpleDateFormat.parse(date);
                if((sdf_date.after(sdf_from) && sdf_date.before(sdf_to) || sdf_date.equals(sdf_from) || sdf_date.equals(sdf_to))){
                    listOFExp.add(data);
                    total_amount+=data.money;
                    if(data.money<0){
                        loss+=data.money;
                    }
                    if (data.money>=0){
                        profit+=data.money;
                    }
                    if(data.item.length()>maxItemString_Length){
                        maxItemString_Length=data.item.length();
                    }

                }
            }
            else {
                listOFExp.add(data);
                total_amount += data.money;
                if (data.money < 0) {
                    loss += data.money;
                }
                if (data.money >= 0) {
                    profit += data.money;
                }
                if (data.item.length() > maxItemString_Length) {
                    maxItemString_Length = data.item.length();
                }
            }
        }while(cursor.moveToNext());

    }
    public static void setAdapterOrUpdate(){
         myAdapter=new RecyclerAdapter(context,listOFExp);
        recyclerView.setAdapter(myAdapter);
         if(myAdapter!=null){
             totalElment_in_recyclerView=myAdapter.getItemCount();
         }
         if(scroll_position!=null){
             recyclerView.scrollToPosition(scroll_position);
         }else
        recyclerView.scrollToPosition(listOFExp.size()-1);
    }

    public void Delete_item() throws ParseException {
        if(selected_ElementList.size()== recyclerView.getChildCount()){
            tv_SortDetail.setText("");
            tv_SortDetail.setVisibility(View.INVISIBLE);
        }
        for (int i = 0; i< selected_ElementList.size(); i++){
            myDataBase.deleteFromDataBase(listOFExp.get(selected_ElementList.get(i)).index);
        }
        try {
            getDatabaseCursor();
            getDataFromDataBase();
        }catch (Exception e){};

        ToggleMenu(1);
        reaset_variable_State();

        if(fromDate !=null && toDate !=null){
            split_Date();
            sortData();
            return;

        }


    }

    public void generatePdfRecord() throws IOException{
        //if(cursor.getCount()!=0)
        if(listOFExp.size()>0){
            Generate_Pdf generate_pdf_object=new Generate_Pdf();
            String outPath = Environment.getExternalStorageDirectory().toString();
            outPath=outPath+"/My E-Wallet/";

               String pdfName=getPdfName();

            generate_pdf_object.generate(this,pdfName,null, DataBuilder(),outPath);
        }else
            Toast.makeText(this,"Transaction not Found",Toast.LENGTH_LONG).show();
    }
    public String getPdfName(){

        if(fromDate==null&&toDate==null){
            return "My E-Wallet (All)";
        }

        String[] spl_f_date=fromDate.split("/");
        String[] spl_to_date=toDate.split("/");
        if(fromDate.equals(toDate)){
            return "My E-Wallet"+" ("+spl_f_date[0]+"-"+spl_f_date[1]+"-"+spl_f_date[2]+")";
        }
        return "My E-Wallet"+" ("+spl_f_date[0]+"-"+spl_f_date[1]+"-"+spl_f_date[2]+" To "+spl_to_date[0]+"-"+spl_to_date[1]+"-"+spl_to_date[2]+")";
    }
    public String DataBuilder(){
        String text="\n  My E-Wallet \n\n";

        if(fromDate!=null&&toDate!=null){
            if(fromDate.equals(toDate)){
                text=text+"   Date : "+fromDate+"\n\n";
            }
            else {
                text = text + "   Date : " + fromDate + " To " + toDate + "\n\n";
            }
        }else{
            text=text+"   Date : All"+"\n\n";
        }
        text=text+"     Creadited :   "+profit.toString()+" \n";
        text=text+"     Debited    :   "+loss.toString()+" \n";
        text+="    "+"+________________________\n";
        text=text+"      Total       :  ( "+total_amount.toString()+" )\n\n";

        text=text+"  **************************************************************************************************************************  \n";
        text=text+"                   SRNO                DATE                    ITEM                               COST";
        text=text+"\n  **************************************************************************************************************************  \n";

        for (int i = 0; i <= listOFExp.size()-1; i++){
            Integer srno=i+1;
            Data data=listOFExp.get(i);
            text=text+"\n\n"+""+get_N_spaces_srNo(srno.toString().trim().length())+"[ "+srno.toString().trim()+" ]"+get_N_spaces_srNo(srno.toString().trim().length())+data.date.trim()+"            "+data.item+get_N_spaces(data.item.length())+""+data.money.toString().trim()+" /-";


            //text=text+"\n\n"+get_N_spaces(srno.toString().trim().length())+srno.toString()+"            "+data.date.trim()+"            "+data.item.trim()+" "+get_N_spaces(data.item.trim().length())+""+data.money.toString().trim()+"$";

        }
        text=text+"\n\n\n\n";

        return text;
    }

    public String get_N_spaces(int length){
        String spaces = new String(new char[(maxItemString_Length-length)+20]).replace('\0', ' ');
        return spaces;
    }

    public String get_N_spaces_srNo(int length) {
        String spaces = new String(new char[(16-length)+4]).replace('\0', ' ');
        return spaces;
    }

    public void show_Dialog_sort(){
        final Dialog mdialog =new Dialog(this);
        sortDialog_open=true;
        mdialog.setContentView(R.layout.dialog_sort);
        mdialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mdialog.setCancelable(false);
        mdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mdialog.show();

        Button btn_cancel=mdialog.findViewById(R.id.sort_btn_cancel);
        Button btn_sort=mdialog.findViewById(R.id.btn_sort);
        et_from=mdialog.findViewById(R.id.btn_from);
        et_to=mdialog.findViewById(R.id.btn_to);

        et_from.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                OpenDatePicker("FROM");
            }
        });
        et_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenDatePicker("TO");
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                reaset_Sort_Selection();
                mdialog.dismiss();
                sortDialog_open=false;
            }
        });
        btn_sort.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!et_from.getText().toString().isEmpty() && !et_to.getText().toString().isEmpty()){
                    fromDate=et_from.getText().toString();
                    toDate=et_to.getText().toString();
                    Boolean splitStatus = split_Date();
                    if (splitStatus==true){
                        Boolean status= null;
                        try {
                            status = sortData();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (status==true){
                            mdialog.dismiss();
                            sortDialog_open=false;
                            if(fromDate.equals(toDate)){
                                tv_SortDetail.setText(fromDate);
                            }else
                            tv_SortDetail.setText(fromDate +" To "+ toDate);

                            tv_SortDetail.setVisibility(View.VISIBLE);
                        }
                    }

                }

            }
        });
    }

    public  void reaset_Sort_Selection(){
        fromDate=null;
        toDate=null;
        return;
    }

    public Boolean split_Date(){
        if(fromDate==null||toDate==null)
            return false;
        try{
            String[] fromSpilitted = fromDate.split("/");
            String[] toSpilitted = toDate.split("/");
            from_day=Integer.parseInt(fromSpilitted[0].trim());
            from_month=Integer.parseInt(fromSpilitted[1].trim());
            from_year=Integer.parseInt(fromSpilitted[2].trim());

            to_day=Integer.parseInt(toSpilitted[0].trim());
            to_month=Integer.parseInt(toSpilitted[1].trim());
            to_year=Integer.parseInt(toSpilitted[2].trim());
            return true;
        }
        catch (Exception e){
            return false;
        }

    }

    public void setText_Et_Item(Dialog mdialog, String text){
        TextInputLayout textInputLayout_item = mdialog.findViewById(R.id.text_input_item);
         EditText editText_item = textInputLayout_item.getEditText();
        editText_item.setText(text);
        editText_item.setSelection(editText_item.getText().toString().length());
    }


    public void show_Dialog_add_new_data(final String operation){
        this.operation=operation;
        final Dialog mdialog =new Dialog(this);
        curr_dialog=mdialog;
        add_new_data_Dialog_Open=true;
        mdialog.setContentView(R.layout.dialog_new_expenses);
        mdialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mdialog.setCancelable(false);
        mdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mdialog.show();
        TextInputLayout te_cost=mdialog.findViewById(R.id.text_input_cost);
        EditText editText_cost=te_cost.getEditText();
        editText_cost.setSelection(editText_cost.getText().length());
        btn_plus_minus = mdialog.findViewById(R.id.btn_increment_decrement);

        Spinner spinner=mdialog.findViewById(R.id.item_spinner);
        String[] shortcuts={"","Fast Food","Shopping","Fuel","Travel","Bill","Salary","Fee","Hotel"};

        if (is_minus_state.equals(true)){
            btn_plus_minus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.decrement_red)));
            btn_plus_minus.setImageResource(R.drawable.minus);

        }else
        if (is_minus_state.equals(false)){
            btn_plus_minus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
            btn_plus_minus.setImageResource(R.drawable.add_round);
        }
        TextView tv_date=mdialog.findViewById(R.id.textview_date);
        tv_date.setText(date_Functions.get_Date());

        if(operation.equals(editOperation)){
            Long cost=listOFExp.get(selected_ElementList.get(0)).money;
            if(cost<0){
                is_minus_state=true;
            }else is_minus_state=false;
            if(is_minus_state.equals(true)){
                btn_plus_minus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.decrement_red)));
                btn_plus_minus.setImageResource(R.drawable.minus);
            }else if(is_minus_state.equals(false)){
                btn_plus_minus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                btn_plus_minus.setImageResource(R.drawable.add_round);
            }

            if(temp_cost_savedInstance!=null && temp_item_savedInstance.length()>0){
                   shortcuts[0]=temp_item_savedInstance;
                   Long temp_cost=Long.parseLong(temp_cost_savedInstance);
                   if(temp_cost<0){
                       btn_plus_minus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.decrement_red)));
                       btn_plus_minus.setImageResource(R.drawable.minus);
                   }else {
                       btn_plus_minus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                       btn_plus_minus.setImageResource(R.drawable.add_round);
                   }

            }
            else {
                shortcuts[0]=listOFExp.get(selected_ElementList.get(0)).item.toString();
            }



            setText_Et_Item(mdialog,temp_item_savedInstance);

            cost=cost>=0?cost:-cost;
            editText_cost.setText(cost.toString());
            //cost_invertor(mdialog);
            TextView tv_header=mdialog.findViewById(R.id.textView_header);
            tv_header.setText("Edit");
            tv_date.setText(listOFExp.get(selected_ElementList.get(0)).date);
        }
        else if(operation.equals(normalOperation)){
            if(temp_item_savedInstance!=null && temp_cost_savedInstance.length()>0)
            shortcuts[0]=temp_item_savedInstance;
            else shortcuts[0]="";

        }

        ArrayAdapter<String> adapter=(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,shortcuts));
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setText_Et_Item(mdialog,parent.getItemAtPosition(position).toString());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if(operation.equals(editOperation)){
                    setText_Et_Item(mdialog,listOFExp.get(selected_ElementList.get(0)).item.toString());
                }
            }
        });
        editText_cost.setSelection(editText_cost.getText().length());
        Button btn_done=mdialog.findViewById(R.id.done);
        FloatingActionButton btn_datePick=mdialog.findViewById(R.id.dialog_new_DatePicker);
        Button btn_cancel=mdialog.findViewById(R.id.dia_new_cancel);

        btn_done.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                try {
                    insert(mdialog,operation);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!selected_ElementList.isEmpty()){
                    reaset_variable_State();
                    ToggleMenu(1);
                }

                temp_item_savedInstance="";
                temp_cost_savedInstance="";

                mdialog.dismiss();
                is_minus_state=true;
                temp_cost_savedInstance="";
                temp_item_savedInstance="";
                add_new_data_Dialog_Open=false;
            }
        });

        btn_plus_minus.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                cost_invertor(mdialog);
            }
        });

        btn_datePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenDatePicker("Add_New_Date");
                curr_dialog=mdialog;
            }
        });

    }

    public  static void reaset_variable_State(){
        selected_ElementList.clear();
        isAnyElement_selected=false;
        setAdapterOrUpdate();

     }

    public void insert(final Dialog mdialog,String operation) throws ParseException {
        Integer previous_element_count = null;
        if (myAdapter != null) {
            previous_element_count = myAdapter.getItemCount();
        }
        Boolean state = true;
        TextView tv_date = mdialog.findViewById(R.id.textview_date);
        TextInputLayout textInputLayout_item = mdialog.findViewById(R.id.text_input_item);
        TextInputLayout textInputLayout_cost = mdialog.findViewById(R.id.text_input_cost);
        EditText et_item = textInputLayout_item.getEditText();
        EditText et_cost = textInputLayout_cost.getEditText();

        if (et_item.getText().toString().trim().length() > 15) {
            textInputLayout_item.setError("Exceed Limit");
            state = false;
        } else if (et_item.getText().toString().trim().length() == 0) {
            textInputLayout_item.setError("Enter Item");
            state = false;
        } else textInputLayout_item.setError("");


        if (et_cost.getText().toString().trim().length() > 8) {
            textInputLayout_cost.setError("Exceed Limit");
            state = false;
        } else if (et_cost.getText().toString().length() == 0 || et_cost.getText().toString().trim().equals("-")) {
            textInputLayout_cost.setError("Enter Amount");
            state = false;
        } else textInputLayout_cost.setError("");

        if (state == true){
            String day = date_Functions.get_Day(tv_date.getText().toString());
            String date = tv_date.getText().toString();
            String item = et_item.getText().toString();
            Long cost=Long.parseLong(et_cost.getText().toString());
            if(is_minus_state==true){
                cost=cost<0?cost:-cost;
            }else{
                cost=cost>=0?cost:-cost;
            }
            if (operation.equals(normalOperation)) {
                boolean status = myDataBase.InsertToDataBase(date, item, cost, day);
                if (status == true) {
                    selectAll_enabled = true;
                    listOFExp.add(new Data(-1, date, item, cost, day));
                    tv_date.setText(date_Functions.get_Date());
                    getDatabaseCursor();
                    getDataFromDataBase();
                    setAdapterOrUpdate();
                    scroll_position=listOFExp.size()-1;
                }

            }

            if (operation.equals(editOperation)) {
                Integer id = listOFExp.get(selected_ElementList.get(0)).index;
                boolean edit_status = myDataBase.Update(id, date, item, cost, day);
                if (edit_status == true) {
                    getDatabaseCursor();
                    getDataFromDataBase();
                    ToggleMenu(1);
                    scroll_position=listOFExp.indexOf(selected_ElementList.get(0));
                    reaset_variable_State();
                }
            }

            try {
                Boolean is_sorting_needed=split_Date();
                if (is_sorting_needed) {
                    sortData();
                    prepareAd_and_Show();
                    if (myAdapter.getItemCount() == previous_element_count && myAdapter != null && !operation.equals(editOperation)) {
                        Toast.makeText(getApplicationContext(), "REFRESH to get Frequent Data", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
            }
            mdialog.dismiss();
            is_minus_state=true;
            temp_cost_savedInstance="";
            temp_item_savedInstance="";
            add_new_data_Dialog_Open = false;
        }





        return;
    }

    public void cost_invertor(Dialog mdialog){

        TextInputLayout textInputLayout_cost = mdialog.findViewById(R.id.text_input_cost);
        EditText et_cost = textInputLayout_cost.getEditText();
        temp_cost_savedInstance=et_cost.getText().toString();
        if (is_minus_state.equals(true)){
            btn_plus_minus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
            btn_plus_minus.setImageResource(R.drawable.add_round);
            is_minus_state=false;
            return;
        }else
        if (is_minus_state.equals(false)){
            btn_plus_minus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.decrement_red)));
            btn_plus_minus.setImageResource(R.drawable.minus);
            is_minus_state=true;
            return;
        }
    }

    public void OpenDatePicker(final String Id){
        String date=date_Functions.get_Date();
        String[] spliitted=date.split("/");
        Integer day=Integer.parseInt(spliitted[0].trim());
        Integer month=Integer.parseInt(spliitted[1].trim())+1;
        Integer year=Integer.parseInt(spliitted[2].trim());

        DatePickerDialog datePickerDialog =new DatePickerDialog(Expenture_Activity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth){

                int correct_month=month+1;
                String day_of_month=String.valueOf(dayOfMonth);
                String month_of_year=String.valueOf(correct_month);
                if(day_of_month.length()==1){
                    day_of_month="0"+day_of_month;
                }
                if(month_of_year.length()==1){
                    month_of_year="0"+month_of_year;
                }


                switch (Id){
                    case "FROM":
                        et_from.setText((day_of_month+"/"+month_of_year+"/"+year));
                        break;
                    case "TO":
                        et_to.setText((day_of_month+"/"+month_of_year+"/"+year));
                        break;

                    case "Add_New_Date":
                        TextView tv_Date=curr_dialog.findViewById(R.id.textview_date);
                        tv_Date.setText((day_of_month+"/"+month_of_year+"/"+year));
                        break;
                }

            }
        },day,month,year);
        datePickerDialog.setCancelable(false);
        datePickerDialog.show();
        datePickerDialog.updateDate(year,month-2,day);
        //Toast.makeText(getApplicationContext(),day+" "+month+" "+year,Toast.LENGTH_SHORT).show();
    }

    /////////////////////////////+++++++++++++++++++Sorting++++++++++++++++++++++/////////////////////////////
    public Boolean sortData() throws ParseException {
        getDatabaseCursor();
        if(cursor.getCount()==0){
            tv_SortDetail.setText("");
            tv_SortDetail.setVisibility(View.INVISIBLE);
            reaset_Sort_Selection();
            return false;
        }
        ArrayList<Data> temp=new ArrayList<Data>();
        cursor.moveToFirst();
        do{
            Integer index=cursor.getInt(0);
            String date=cursor.getString(1);
            String item=cursor.getString(2);
            Long money=cursor.getLong(3);
            String day=cursor.getString(4);
            Data data=new Data(index,date,item,money,day);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date sdf_from=sdf.parse(fromDate);
            Date sdf_to=sdf.parse(toDate);
            Date sdf_curr=sdf.parse(date);

            if((sdf_curr.after(sdf_from) && sdf_curr.before(sdf_to) || sdf_curr.equals(sdf_from)) || sdf_curr.equals(sdf_to)){
                temp.add(data);
            }

        }while(cursor.moveToNext());

        if(!temp.isEmpty()){
            listOFExp=temp;
            setAdapterOrUpdate();
            return true;
        }
        else
            Toast.makeText(this,"Transaction Not Found",Toast.LENGTH_SHORT).show();

        return false;
    }



    Boolean double_press=false;

    @Override

    public void onBackPressed() {
        if(double_press){
            super.onBackPressed();
            return;
        }
        ToggleMenu(1);
        if( !selected_ElementList.isEmpty()){
            selected_ElementList.clear();
            reaset_variable_State();
            return;
        }
        fromDate=null;
        toDate=null;
        selectAll_enabled=false;
        isAnyElement_selected =false;
        tv_SortDetail.setText("");
        tv_SortDetail.setVisibility(View.INVISIBLE);
        getDatabaseCursor();
        if(!searchView.isIconified()){
            searchView.setIconified(true);
        }
        if(cursor.getCount()!=0) {
            try {
                getDataFromDataBase();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            setAdapterOrUpdate();
        }


        Toast.makeText(getApplicationContext(),"press BACK again to exit",Toast.LENGTH_SHORT).show();
        double_press=true;
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                double_press=false;
            }
        },2000);

    }

    public void onDestroy(){
        super.onDestroy();
        if ( curr_dialog!=null && curr_dialog.isShowing() ){
            curr_dialog.cancel();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("fromDate",fromDate);
        outState.putString("toDate",toDate);
        outState.putBoolean("isAnyElement_selected",isAnyElement_selected);
        outState.putBoolean("selectAll_enabled",selectAll_enabled);
        outState.putIntegerArrayList("selected_ElementList",selected_ElementList);
        outState.putBoolean("add_new_data_Dialog_Open",add_new_data_Dialog_Open);
        outState.putBoolean("sortDialog_open",sortDialog_open);
        outState.putString("operation",operation);
        outState.putBoolean("is_minus_state",is_minus_state);
        if(add_new_data_Dialog_Open==true){
            TextInputLayout te_cost=curr_dialog.findViewById(R.id.text_input_cost);
            TextInputLayout te_item=curr_dialog.findViewById(R.id.text_input_item);
            TextView tv=curr_dialog.findViewById(R.id.textview_date);
            dialog_current_date =tv.getText().toString();
            EditText editText_item=te_item.getEditText();
            EditText editText_cost=te_cost.getEditText();
            outState.putString("cost",editText_cost.getText().toString());
            outState.putString("item",editText_item.getText().toString());
            outState.putString("dialog_current_date",dialog_current_date);
        }

        if(selected_ElementList.size()== recyclerView.getChildCount()){
            outState.putBoolean("SelectAll_Element",true);
        }else {
            outState.putBoolean("SelectAll_Element",false);
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fromDate=savedInstanceState.getString("fromDate",null);
        toDate=savedInstanceState.getString("toDate",null);
        isAnyElement_selected=savedInstanceState.getBoolean("isAnyElement_selected");
        selectAll_enabled=savedInstanceState.getBoolean("selectAll_enabled");
        selectAll_Element=savedInstanceState.getBoolean("SelectAll_Element");
        selected_ElementList=savedInstanceState.getIntegerArrayList("selected_ElementList");
        add_new_data_Dialog_Open=savedInstanceState.getBoolean("add_new_data_Dialog_Open");
        sortDialog_open=savedInstanceState.getBoolean("sortDialog_open");
        operation=savedInstanceState.getString("operation");
        temp_cost_savedInstance =savedInstanceState.getString("cost");
        temp_item_savedInstance =savedInstanceState.getString("item");
        is_minus_state=savedInstanceState.getBoolean("is_minus_state");
        dialog_current_date=savedInstanceState.getString("dialog_current_date");
        if(fromDate!=null &&toDate!=null){
            split_Date();
            try {
                sortData();
                tv_SortDetail.setVisibility(View.VISIBLE);
                if(fromDate.equals(toDate)){
                    tv_SortDetail.setText(fromDate);
                }else
                    tv_SortDetail.setText(fromDate +" To "+ toDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(sortDialog_open==true){
            show_Dialog_sort();
        }
        if(add_new_data_Dialog_Open==true && (operation.equals(editOperation) ||operation.equals(normalOperation))) {
            show_Dialog_add_new_data(operation);
            //if(!(temp_cost_savedInstance.equals("")&& temp_item_savedInstance.equals(""))){
            TextInputLayout te_cost = curr_dialog.findViewById(R.id.text_input_cost);
            TextInputLayout te_item = curr_dialog.findViewById(R.id.text_input_item);
            TextView tv = curr_dialog.findViewById(R.id.textview_date);
            EditText editText_item = te_item.getEditText();
            EditText editText_cost = te_cost.getEditText();
            editText_cost.setText(temp_cost_savedInstance);
            editText_item.setText(temp_item_savedInstance);
            tv.setText(dialog_current_date);
            if (operation.equals(editOperation)) {
                if (is_minus_state.equals(true)) {
                    btn_plus_minus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.decrement_red)));
                    btn_plus_minus.setImageResource(R.drawable.minus);
                } else {

                    btn_plus_minus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                    btn_plus_minus.setImageResource(R.drawable.add_round);
                }
            }
        }
            }
        }

