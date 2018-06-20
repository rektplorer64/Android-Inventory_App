package tanawinwichitcom.android.inventoryapp.RecycleViewAdapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import tanawinwichitcom.android.inventoryapp.AddItemActivity;
import tanawinwichitcom.android.inventoryapp.R;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder>{
    private Context mContext;
    private ArrayList<AddItemActivity.SelectableColor> selectableColorArrayList;

    public ColorAdapter(Context context, ArrayList<AddItemActivity.SelectableColor> selectableColorArrayList){
        mContext = context;
        this.selectableColorArrayList = selectableColorArrayList;
    }

    // @Override
    // public View getView(int position, View convertView, ViewGroup parent){
    //     Button colorButton;
    //     if(convertView == null){
    //         colorButton = new Button(mContext);
    //         colorButton.setLayoutParams(new ViewGroup.LayoutParams(150, 150));
    //         colorButton.setGravity(Gravity.CENTER);
    //         //colorButton.setPadding(0, 0, 32, 0);
    //         colorButton.setBackground(mContext.getDrawable(R.drawable.circle_button));
    //     }else{
    //         colorButton = (Button) convertView;
    //     }
    //     colorButton.setBackgroundColor(mContext.getResources().getColor(selectableColorArrayList.get(position).getColorId()));
    //     return colorButton;
    // }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.circle_color, parent, false);
        return new ColorAdapter.ViewHolder(itemView);
    }

    AddItemActivity.SelectableColor lastSelectedColor;

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position){
        final AddItemActivity.SelectableColor selectableColor = selectableColorArrayList.get(position);

        Drawable drawable = null;
        try{
            drawable = Drawable.createFromXml(mContext.getResources(), mContext.getResources().getXml(R.drawable.circle_button));
        }catch(XmlPullParserException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        drawable.setColorFilter(mContext.getResources().getColor(selectableColorArrayList.get(position).getColorId()), PorterDuff.Mode.SRC_OVER);
        holder.colorCircleButton.setBackground(drawable);
        holder.colorCheckIcon.setImageResource(R.drawable.ic_check_white_color);
        //holder.colorCircleButton.setBackgroundColor(mContext.getResources().getColor(selectableColorArrayList.get(position).getColorId()));
        if(!selectableColor.getSelected()){
            holder.colorCheckIcon.setVisibility(View.GONE);
        }else{
            holder.colorCheckIcon.setVisibility(View.VISIBLE);
            holder.colorCheckIcon.setColorFilter(getSuitableFrontColor(mContext, selectableColor.getColorId()));
        }
        holder.colorCircleButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //Toast.makeText(mContext, "Clicked " + " #" + position + ", lol!", Toast.LENGTH_SHORT).show();
                if(selectableColor.getSelected()){
                    System.out.println("Statement 1: ");
                    holder.colorCheckIcon.setVisibility(View.GONE);
                    selectableColor.setSelected(false);
                }else{
                    System.out.println("Statement 2: ");
                    holder.colorCheckIcon.setColorFilter(getSuitableFrontColor(mContext, selectableColor.getColorId()));
                    holder.colorCheckIcon.setVisibility(View.VISIBLE);
                    selectableColor.setSelected(true);

                    lastSelectedColor = selectableColor;
                    clearOtherSelection(lastSelectedColor);
                }
                System.out.println("Toogle #" + position + ", Current: " + selectableColor.getSelected());

            }
        });
    }

    @Override
    public int getItemCount(){
        return selectableColorArrayList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        ImageButton colorCircleButton;
        ImageView colorCheckIcon;

        ViewHolder(View itemView){
            super(itemView);
            colorCircleButton = itemView.findViewById(R.id.colorCircleButton);
            colorCheckIcon = itemView.findViewById(R.id.colorCheckIcon);
        }
    }

    public static int getSuitableFrontColor(Context context, Integer backgroundColorInt){
        String colorString = context.getResources().getString(backgroundColorInt);     /* Gets Hex Color from given resource id*/
        int color = Color.parseColor(colorString);      /* Decode Hex String into a Color integer */

        int red = Color.red(color);     /* Extracts Red Channel Value from the Color integer */
        int green = Color.green(color);     /* Extracts Green Channel Value from the Color integer */
        int blue = Color.blue(color);       /* Extracts Blue Channel Value from the Color integer */

        System.out.println("Param:" + backgroundColorInt);
        System.out.println("Param Value: " + color);
        System.out.println("RGB: " + red + " " + green + " " + blue);

        if(red + green + blue < 383){       /* Simple Algorithm for determining suitable foreground color */
            // White
            red = 255;
            green = 255;
            blue = 255;
        }else{
            // Black
            red = 0;
            green = 0;
            blue = 0;
        }
        System.out.println("Returns: " + Color.rgb(red, green, blue));
        return Color.rgb(red, green, blue);     /* Converts RGB to an Integer color value*/
    }

    private void clearOtherSelection(AddItemActivity.SelectableColor lastSelectedColor){
        for(int i = 0; i < selectableColorArrayList.size(); i++){
            AddItemActivity.SelectableColor selectableColor = selectableColorArrayList.get(i);
            if(selectableColor != lastSelectedColor && selectableColor.getSelected()){
                selectableColor.setSelected(false);
                notifyItemChanged(i);
            }
        }
    }
}
