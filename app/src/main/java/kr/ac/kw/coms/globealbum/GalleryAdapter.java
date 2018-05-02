package kr.ac.kw.coms.globealbum;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private int[] mDataset;
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView mImageView;
        public int resId;
        public ViewHolder(ImageView v)
        {
            super(v);
            mImageView = v;
        }
    }
    public GalleryAdapter(int[] Dataset)
    {
        mDataset = Dataset;
    }

    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType)
    {
        ImageView v = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.inner_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
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
                Toast.makeText(holder.itemView.getContext(), "Clicked " + holder.resId, Toast.LENGTH_SHORT).show();
            }
        });
        holder.mImageView.setImageResource(mDataset[position]);
        holder.resId = mDataset[position];
    }

    @Override
    public int getItemCount()
    {
        return mDataset.length;
    }
}
