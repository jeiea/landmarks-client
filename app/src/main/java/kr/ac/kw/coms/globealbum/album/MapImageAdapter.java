package kr.ac.kw.coms.globealbum.album;
/* 작성자: 이상훈 */
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;

class MapImageAdapter extends RecyclerView.Adapter<MapImageAdapter.ViewHolder> {
    ArrayList<String> folderList;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView mName;
        RecyclerView mImgList;
        public ViewHolder(View view)
        {
            super(view);
            mName = (TextView)((LinearLayout)view).getChildAt(0);
            mImgList = (RecyclerView)((LinearLayout)view).getChildAt(1);
        }
    }

    public MapImageAdapter(ArrayList<String> folderList) {
        //this.folderList = folderList;
        this.folderList.add("0");
        this.folderList.add("1");
        this.folderList.add("2");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.mapimgrow_view, parent, false);
        ViewHolder vh = new ViewHolder(layout);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mName.setText("#" + position);
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }
}
