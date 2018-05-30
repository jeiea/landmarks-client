package kr.ac.kw.coms.globealbum.album;
/* 작성자: 이상훈 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;

class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private ArrayList<Model> mDataset;
    private Context context;
    private boolean MultiSelectMode = false;
    private ArrayList<ViewHolder> Elements = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public String ImagePath;
        public int DisplayWidth;
        public int index;

        public ViewHolder(ImageView v) {
            super(v);
            mImageView = v;
            DisplayWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        }
    }

    public GalleryAdapter(ArrayList<Model> Dataset) {
        mDataset = Dataset;
    }

    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        ImageView v = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.inner_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
        context = parent.getContext();
        Elements.add(vh);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Model model = mDataset.get(position);
        Bitmap mBitmap = BitmapFactory.decodeFile(model.getImage());
        holder.mImageView.setImageBitmap(mBitmap);
        ViewGroup.LayoutParams params = holder.mImageView.getLayoutParams();
        params.height = holder.DisplayWidth / 3;
        holder.mImageView.setLayoutParams(params);
        holder.ImagePath = model.getImage();
        holder.index = position;
        holder.mImageView.setBackgroundColor(model.isSelected() ? Color.CYAN : Color.WHITE);

        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MultiSelectMode) {
                    model.setSelected(!model.isSelected());
                    holder.mImageView.setBackgroundColor(model.isSelected() ? Color.CYAN : Color.WHITE);
                    Toast.makeText(context, model.isSelected() ? "SELECTED" : "UNSELECTED", Toast.LENGTH_SHORT).show();
                } else { //이미지 한 개 선택 시 이벤트
                    /*
                    Intent intent = new Intent(Intent.ACTION_ANSWER);
                    intent.putExtra("FILEPATH", holder.ImagePath);
                    ((Activity) context).setResult(Activity.RESULT_OK, intent);
                    ((Activity) context).finish();*/
                    Intent intent = new Intent(context, GalleryDetail.class);
                    intent.putExtra("index", holder.index);
                    String[] ImageList = new String[mDataset.size()];
                    for (int i = 0; i < mDataset.size(); i++) {
                        ImageList[i] = mDataset.get(i).getImage();
                    }
                    intent.putExtra("Dataset", ImageList);
                    ((FragmentActivity) context).startActivityForResult(intent, 1);
                }
            }
        });
        holder.mImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MultiSelectMode = true;
                ((AppCompatImageView) ((Activity) context).findViewById(R.id.btn_detail)).setVisibility(View.VISIBLE);
                model.setSelected(!model.isSelected());
                holder.mImageView.setBackgroundColor(model.isSelected() ? Color.CYAN : Color.WHITE);
                Toast.makeText(context, model.isSelected() ? "SELECTED" : "UNSELECTED", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }

    public void UnSelectAll() {
        for (Model i : mDataset) {
            i.setSelected(false);
        }
        for (ViewHolder i : Elements) {
            i.mImageView.setBackgroundColor(Color.WHITE);
        }
        MultiSelectMode = false;
        ((AppCompatImageView) ((Activity) context).findViewById(R.id.btn_detail)).setVisibility(View.GONE);
    }

    public boolean isMultiSelectMode() {
        return MultiSelectMode;
    }
}
