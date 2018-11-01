package kr.ac.kw.coms.globealbum.game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.GlideApp;
import kr.ac.kw.coms.globealbum.common.RequestCodes;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import kr.ac.kw.coms.globealbum.provider.RemoteJava;
import kr.ac.kw.coms.globealbum.provider.RemotePicture;
import kr.ac.kw.coms.globealbum.provider.UIPromise;


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
        holder.gamePictureInfo = gamePictureInfos.get(position);
        GlideApp.with(gameActivity).load(holder.gamePictureInfo).into(holder.afterGamePictureImageView);
        String str = holder.gamePictureInfo.getMeta().getAddress();
        int start = 0;
        int end = str.length();
        int firstSpace =  str.indexOf(" ");
        if (firstSpace == -1) {
            holder.afterGamePlactTextView.setText(str);
        }
        else{
            holder.afterGameCountryTextView.setText(str.substring(start,firstSpace));
            holder.afterGamePlactTextView.setText(str.substring(firstSpace+1,end));
        }
    }

    @Override
    public int getItemCount() {
        return gamePictureInfos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView afterGamePictureImageView,afterGameSeeRelatedDiary,afterGameSeeRelatedPicture;
        TextView afterGamePlactTextView, afterGameCountryTextView;
        IPicture gamePictureInfo;

        ViewHolder(final View itemView) {
            super(itemView);
            afterGamePictureImageView = itemView.findViewById(R.id.after_game_place_imageview);
            afterGameSeeRelatedDiary = itemView.findViewById(R.id.after_game_see_diary_btn);
            afterGameSeeRelatedPicture = itemView.findViewById(R.id.after_game_see_picture_btn);
            afterGameCountryTextView = itemView.findViewById(R.id.after_game_show_country_name);
            afterGamePlactTextView = itemView.findViewById(R.id.after_game_show_place_name);

            afterGameSeeRelatedDiary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 사진을 찍은 나라를 방문한 다이어리 보여주기
                    ShowRelatedInformation(afterGamePictureImageView.getContext(), gamePictureInfo, 1);
                }
            });
            afterGameSeeRelatedPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 사진을 찍은 나라를 같은 사진들 보여주기
                    ShowRelatedInformation(afterGamePictureImageView.getContext(), gamePictureInfo, 0);
                }
            });
        }

        public void ShowRelatedInformation(Context context, IPicture picture, int WHAT_TO_SHOW)
        {
            Intent intent = new Intent(context, kr.ac.kw.coms.globealbum.diary.Diary_main.class);
            if (WHAT_TO_SHOW == 0)
            {
                //이미지 목록 먼저 보여주기
                intent.setAction(RequestCodes.ACTION_DIARY_RELATED_IMAGE_FIRST);
            }
            else
            {
                //다이어리 먼저 보여주기
                intent.setAction(RequestCodes.ACTION_DIARY_RELATED_DIARY_FIRST);
            }
            intent.putExtra("Query", picture);


            context.startActivity(intent);
        }
    }
}
