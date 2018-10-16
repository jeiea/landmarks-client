package kr.ac.kw.coms.globealbum.game;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.GlideApp;
import kr.ac.kw.coms.globealbum.provider.IPicture;


public class AfterGameAdapter extends RecyclerView.Adapter<AfterGameAdapter.ViewHolder> {

    private List<IPicture> gamePictureInfos;
    private Activity gameActivity;

    public AfterGameAdapter(Activity activity, List<IPicture> gamePictureInfos) {
        gameActivity = activity;
        this.gamePictureInfos = gamePictureInfos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(gameActivity).inflate(R.layout.layout_after_game_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IPicture gamePictureInfo = gamePictureInfos.get(position);
        GlideApp.with(gameActivity).load(gamePictureInfo).into(holder.imageViewPicture);
        String str = gamePictureInfo.getMeta().getAddress();
        int start = 0;
        int end = str.length();
        int firstSpace =  str.indexOf(" ");
        if (firstSpace == -1) {
            holder.textViewPlace.setText(str);
        }
        else{
            holder.textViewCountry.setText(str.substring(start,firstSpace));
            holder.textViewPlace.setText(str.substring(firstSpace+1,end));
        }
    }

    @Override
    public int getItemCount() {
        return gamePictureInfos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPicture;
        TextView textViewPlace,textViewCountry;

        ViewHolder(final View itemView) {
            super(itemView);
            imageViewPicture = itemView.findViewById(R.id.after_game_place_imageview);
            //imageViewPicture.setOnClickListener(new GameUI.PictureClickZoomingListener());      //사진 클릭 시 사진 확대
            textViewCountry = itemView.findViewById(R.id.after_game_show_country_name);
            textViewPlace = itemView.findViewById(R.id.after_game_show_place_name);
        }
    }
}
