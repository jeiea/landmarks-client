package kr.ac.kw.coms.globealbum.game;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;

public class AfterGameAdapter extends RecyclerView.Adapter<AfterGameAdapter.ViewHolder>{

    Context context;
    ArrayList<GamePictureInfo> gamePictureInfos;

    public AfterGameAdapter(Context context,ArrayList<GamePictureInfo> gamePictureInfos) {
        this.context = context;
        this.gamePictureInfos=gamePictureInfos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_after_game_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GamePictureInfo gamePictureInfo = gamePictureInfos.get(position);
            Glide.with(context).load(gamePictureInfo.id).into(holder.imageViewPicture);
            holder.textViewPlace.setText(gamePictureInfo.name);
    }
    @Override
    public int getItemCount() {
        return gamePictureInfos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageViewPicture;
        TextView textViewPlace;


        public ViewHolder(View itemView) {
            super(itemView);
            imageViewPicture = itemView.findViewById(R.id.after_game_place_imageview);
            textViewPlace = itemView.findViewById(R.id.after_game_show_place_name);
        }
    }
}
