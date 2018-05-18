package kr.ac.kw.coms.globealbum;
/* 작성자: 이상훈 */
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

class MapImageAdapter extends RecyclerView.Adapter<MapImageAdapter.ViewHolder> {
    ArrayList<String> folderList;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public ViewHolder(View view)
        {
            super(view);
        }
    }

    public MapImageAdapter(ArrayList<String> folderList) {
        this.folderList = folderList;
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

    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }
}
