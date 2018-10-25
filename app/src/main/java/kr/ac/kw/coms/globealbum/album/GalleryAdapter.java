package kr.ac.kw.coms.globealbum.album;
/* 작성자: 이상훈 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.RequestCodes;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import kr.ac.kw.coms.globealbum.provider.LocalPicture;
import kr.ac.kw.coms.globealbum.provider.ResourcePicture;

//사용 안함
class _GalleryAdapter extends RecyclerView.Adapter<_GalleryAdapter.ViewHolder> {
    private ArrayList<Model> mDataset;
    private static Context context;
    private boolean MultiSelectMode = false;
    private ArrayList<ViewHolder> Elements = new ArrayList<>();
    String Mode;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public String ImagePath;
        public int DisplayWidth;
        public int index;
        public Bitmap Image_original;
        public Bitmap Image_checked;
        public Bitmap Image_unchecked;
        public Bitmap checkedbox;
        public Bitmap uncheckedbox;

        public ViewHolder(ImageView v) {
            super(v);
            mImageView = v;
            DisplayWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
            checkedbox = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.checked), DisplayWidth / 15, DisplayWidth / 15, false);
            uncheckedbox = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.unchecked), DisplayWidth / 15, DisplayWidth / 15, false);
        }
    }

    public _GalleryAdapter(ArrayList<Model> Dataset, String Action) {
        mDataset = Dataset;
        if (Action.equals(RequestCodes.ACTION_SELECT_PHOTO) || Action.equals(RequestCodes.ACTION_VIEW_PHOTO))
            Mode = Action;
    }

    @Override
    public _GalleryAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        context = parent.getContext();
        ImageView v = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.inner_view, parent, false);
        ViewHolder vh = new ViewHolder(v);

        Elements.add(vh);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Model model = mDataset.get(position);
        Glide.with(holder.mImageView).load(model.getImage()).into(holder.mImageView);
/*
        Bitmap overlay = Bitmap.createBitmap(holder.Image_original.getWidth(), holder.Image_original.getHeight(), holder.Image_original.getConfig());
        Canvas canvas = new Canvas(overlay);
        canvas.drawBitmap(holder.Image_original, new Matrix(), null);
        canvas.drawBitmap(holder.checkedbox, new Matrix(), null);
        holder.Image_checked = overlay.copy(holder.Image_original.getConfig(), false);
        overlay = Bitmap.createBitmap(holder.Image_original.getWidth(), holder.Image_original.getHeight(), holder.Image_original.getConfig());
        canvas = new Canvas(overlay);
        canvas.drawBitmap(holder.Image_original, new Matrix(), null);
        canvas.drawBitmap(holder.uncheckedbox, new Matrix(), null);
        holder.Image_unchecked = overlay.copy(holder.Image_original.getConfig(), false);
*/
        ViewGroup.LayoutParams params = holder.mImageView.getLayoutParams();
        params.height = holder.DisplayWidth / 3;
        holder.mImageView.setLayoutParams(params);
        holder.ImagePath = model.getImage();
        holder.index = position;

        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (MultiSelectMode) {
                    model.setSelected(!model.isSelected());
                    holder.mImageView.setImageBitmap(model.isSelected() ? holder.Image_checked : holder.Image_unchecked);

                } else { //이미지 한 개 선택 시 이벤트*/
                if (Mode.equals(RequestCodes.ACTION_VIEW_PHOTO)) {
                } else if (Mode.equals(RequestCodes.ACTION_SELECT_PHOTO)) {
                }
                //}
            }
        });/*
        holder.mImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MultiSelectMode = true;
                ((AppCompatImageView) ((Activity) context).findViewById(R.id.btn_detail)).setVisibility(View.VISIBLE);
                model.setSelected(!model.isSelected());
                for (ViewHolder i : Elements) {
                    i.mImageView.setImageBitmap(mDataset.get(i.index).isSelected() ? i.Image_checked : i.Image_unchecked);
                }
                return true;
            }
        });*/
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
            i.mImageView.setImageBitmap(i.Image_original);
        }
        MultiSelectMode = false;
    }

    public boolean isMultiSelectMode() {
        return MultiSelectMode;
    }

}
