package kr.ac.kw.coms.globealbum.game;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
        holder.textViewPlace.setText(gamePictureInfo.getMeta().getAddress());
    }

    @Override
    public int getItemCount() {
        return gamePictureInfos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPicture;
        TextView textViewPlace;
        Button buttonQuit;

        ViewHolder(final View itemView) {
            super(itemView);
            buttonQuit = itemView.findViewById(R.id.after_game_quit_button);
            buttonQuit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gameActivity.finish();      //GameActivity 종료
                }
            });
            imageViewPicture = itemView.findViewById(R.id.after_game_place_imageview);
            imageViewPicture.setOnClickListener(new GameUI.PictureClickZoomingListener());      //사진 클릭 시 사진 확대
            textViewPlace = itemView.findViewById(R.id.after_game_show_place_name);
        }
    }
}
