package kr.ac.kw.coms.globealbum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private ArrayList<String> mDataset;
    private Context context;
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView mImageView;
        public String ImagePath;
        public ViewHolder(ImageView v)
        {
            super(v);
            mImageView = v;
        }
    }
    public GalleryAdapter(ArrayList<String> Dataset)
    {
        mDataset = Dataset;
    }

    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType)
    {
        ImageView v = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.inner_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
        context = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position)
    {
        //데이터 가져오기 작업
        holder.mImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(Intent.ACTION_ANSWER);
                intent.putExtra("FILEPATH", holder.ImagePath);
                ((Activity)context).setResult(Activity.RESULT_OK, intent);
                ((Activity)context).finish();
            }
        });
        Bitmap mBitmap = BitmapFactory.decodeFile(mDataset.get(position));
        holder.mImageView.setImageBitmap(mBitmap);
        holder.ImagePath = mDataset.get(position);
    }

    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }
}
