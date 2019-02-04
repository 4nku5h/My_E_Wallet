package com.example.hackermr.docsholder.expenses;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.nfc.cardemulation.HostApduService;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hackermr.docsholder.Main.Aux_Function;
import com.shrivastava.myewallet.R;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.HashMap;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>{
    private ArrayList<Data> listofData;
    Context context;
    Aux_Function date_function=new Aux_Function();
    HashMap<Integer,Integer> selected_element_map;

    public  RecyclerAdapter(Context context,ArrayList<Data> listofData){
        this.listofData=listofData;
        this.context=context;

    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());
        View view=layoutInflater.inflate(R.layout.expences_ticket,parent,false);
        selected_element_map=new HashMap<Integer, Integer>();
        return new MyViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        Boolean selected_flag=false;

        for(int i=0;i<Expenture_Activity.selected_ElementList.size();i++){
            if(Expenture_Activity.selected_ElementList.get(i)==position){
                holder.linearLayout.setBackgroundColor(context.getResources().getColor(R.color.selected));
                selected_flag=true;
                break;
            }
        }


        if(selected_flag==false){
            holder.linearLayout.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        }

        Data data=listofData.get(position);
        String[] date_spilit =date_function.get_date_day_year(data.date);
        String date=date_spilit[0];
        String month=date_spilit[1];
        String year=date_spilit[2];
        holder.tv_date.setText(date);
        holder.tv_day.setText(data.day);
        holder.tv_year.setText(month+"-"+year);

        Drawable drawable=(context.getResources().getDrawable(R.drawable.background_gray_dark));
        drawable.setColorFilter(get_Day_Color(data.day), PorterDuff.Mode.SRC_IN);
        holder.tv_day.setBackground(drawable);
        //holder.tv_day.setBackgroundColor(get_Day_Color(data.day.trim()));

        holder.etitem.setText(data.item);
        if(data.money<0){
            holder.etcost.setTextColor(context.getResources().getColor(R.color.background_minus));
        }else{
            holder.etcost.setTextColor(context.getResources().getColor(R.color.background_plus));
        }
        holder.etcost.setText(data.money.toString());

        holder.linearLayout.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {

                if (Expenture_Activity.isAnyElement_selected == false && Expenture_Activity.selected_ElementList.isEmpty()) {
                    Expenture_Activity.selectAll_enabled = true;
                    Expenture_Activity.isAnyElement_selected = true;
                    Expenture_Activity.ToggleMenu(0);
                    v.setBackgroundColor(context.getResources().getColor(R.color.selected));
                    Expenture_Activity.selected_ElementList.add(position);

                }
                return true;
            }
        });


        holder.linearLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (Expenture_Activity.isAnyElement_selected == true) {
                    MenuItem edit = Expenture_Activity.menu.findItem(R.id.menu_edit);
                    edit.setVisible(false);
                    Boolean status = true;

                    for (int i = 0; i < Expenture_Activity.selected_ElementList.size(); i++) {
                        if (position == Expenture_Activity.selected_ElementList.get(i)) {

                            if (Expenture_Activity.selected_ElementList.size() == 1) {
                                Expenture_Activity.isAnyElement_selected = false;
                                Expenture_Activity.ToggleMenu(1);
                                reaset_variable_State();
                                edit.setVisible(false);
                                return;
////
                            }
                            Expenture_Activity.selected_ElementList.remove(i);
                            Expenture_Activity.selectAll_enabled = true;
                            if (Expenture_Activity.selected_ElementList.size() == 1) {
                                edit.setVisible(true);

                            }
                            v.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                            status = false;
                        }

                    }
                    if (status != false){
                        v.setBackgroundColor(context.getResources().getColor(R.color.selected));
                        Expenture_Activity.selected_ElementList.add(position);
                        if (Expenture_Activity.totalElment_in_recyclerView == Expenture_Activity.selected_ElementList.size()) {
                            Expenture_Activity.selectAll_enabled = false;
                            if (Expenture_Activity.selected_ElementList.size() == 1) {
                                edit.setVisible(true);
                            } else {
                                edit.setVisible(false);
                            }
                        }
                        return;
                    }
                }
                return;
            }
        });

    }

    @Override
    public int getItemCount() {
        return listofData.size();
    }

    public  void reaset_variable_State(){
        Expenture_Activity.reaset_variable_State();

        }

        public void SelectAll(){
            if(Expenture_Activity.selected_ElementList.size()==Expenture_Activity.recyclerView.getAdapter().getItemCount()){
                Toast.makeText(context,"Already Selected",Toast.LENGTH_SHORT).show();
                return;
            }
            if(Expenture_Activity.selectAll_enabled==true){
                Expenture_Activity.selectAll_enabled=false;
                MenuItem edit=Expenture_Activity.menu.findItem(R.id.menu_edit);
                edit.setVisible(false);
                Expenture_Activity.selected_ElementList.clear();
                Expenture_Activity.isAnyElement_selected =true;
                Integer count=0;
                for(int i = 0; i<Expenture_Activity.recyclerView.getAdapter().getItemCount(); i++){
                    count++;
                    Expenture_Activity.selected_ElementList.add(i);
                }
                Toast.makeText(context,count.toString()+" Transaction Selected",Toast.LENGTH_SHORT).show();
                Expenture_Activity.setAdapterOrUpdate();
            }
        }


    public Integer get_Day_Color(String day){
        Integer colorid;
        switch (day){
            case "Sunday":
                 colorid=context.getResources().getColor(R.color.sunday);
                break;

            case "Monday":
                colorid=context.getResources().getColor(R.color.monday);
                break;

            case "Tuesday":
                colorid=context.getResources().getColor(R.color.tuesday);
                break;

            case "Wednesday":
                colorid=context.getResources().getColor(R.color.wednesday);
                break;

            case "Thursday":
                colorid=context.getResources().getColor(R.color.thursday);
                break;

            case "Friday":
                colorid=context.getResources().getColor(R.color.friday);
                break;

            case "Saturday":
                colorid=context.getResources().getColor(R.color.saturday);
                break;

            default:
                colorid=context.getResources().getColor(R.color.gray_dark);
                break;
        }
        return colorid;


    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_date;
        TextView tv_day;
        TextView tv_year;
        TextView etitem;
        TextView etcost;
        LinearLayout linearLayout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
             tv_date=itemView.findViewById(R.id.tv_date);
             tv_day=itemView.findViewById(R.id.tv_day);
             tv_year=itemView.findViewById(R.id.tv_year);
             etitem=itemView.findViewById(R.id.et_item);
             etcost=itemView.findViewById(R.id.et_money);
             linearLayout=itemView.findViewById(R.id.expenses_ticket_linear_lay);
        }
    }


}
