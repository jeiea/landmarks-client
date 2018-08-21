package kr.ac.kw.coms.globealbum.game;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;


public class AfterGameAdapter extends RecyclerView.Adapter<AfterGameAdapter.ViewHolder>{

    Context context;
    ArrayList<GamePictureInfo> gamePictureInfos;
    GameActivity gameActivity;

    public AfterGameAdapter(Context context,ArrayList<GamePictureInfo> gamePictureInfos) {
        this.context = context;
        this.gamePictureInfos=gamePictureInfos;
        gameActivity =(GameActivity)GameActivity.GActivity;

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
        Button buttonQuit;

        public ViewHolder(final View itemView) {
            super(itemView);
            buttonQuit = itemView.findViewById(R.id.after_game_quit_button);
            buttonQuit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gameActivity.finish();      //GameActivity 종료
                }
            });
            imageViewPicture = itemView.findViewById(R.id.after_game_place_imageview);
            imageViewPicture.setOnClickListener( gameActivity.new PictureClickListenerTypeA());      //사진 클릭 시 사진 확대
            textViewPlace = itemView.findViewById(R.id.after_game_show_place_name);
        }
    }
}
