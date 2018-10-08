package kr.ac.kw.coms.globealbum.diary;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.album.GalleryActivity;
import kr.ac.kw.coms.globealbum.common.RequestCodes;
import kr.ac.kw.coms.globealbum.provider.IPicture;

public class EditImageListAdapter extends RecyclerView.Adapter<ItemViewHolder> {
    private AppCompatActivity RootActivity;
    final ArrayList<IPicture> mItems = new ArrayList<>();

    public EditImageListAdapter(AppCompatActivity RootActivity, ArrayList<IPicture> Items) {
        this.RootActivity = RootActivity;
        mItems.addAll(Items);
    }

    public void AddNewPicture(IPicture newPicture) {
        mItems.add(newPicture);
        notifyDataSetChanged();
    }

    public ArrayList<IPicture> getItems()
    {
        return mItems;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < getItemCount() - 1) {
            return 0;
        } else {
            return 1;
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == 0) {
            view = LayoutInflater.from(RootActivity.getBaseContext()).inflate(R.layout.layout_map_n_pictures_verticallist, parent, false);
        } else {
            view = LayoutInflater.from(RootActivity.getBaseContext()).inflate(R.layout.layout_map_n_pictures_verticallist_new, parent, false);
        }
        return new ItemViewHolder(this.RootActivity, view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, final int position) {
        //목록의 내용 추가
        if (position < getItemCount() - 1) {
            Glide.with(holder.imageView).load(mItems.get(position)).into(holder.imageView);
            holder.text_Title.setText(mItems.get(position).getMeta().getAddress() + "\n" + mItems.get(position).getMeta().getGeo().toIntString());
            holder.btn_Delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(RootActivity);
                    alert.setTitle("삭제 확인");
                    alert.setMessage("사진을 삭제합니다.");
                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mItems.remove(position);
                            notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    });
                    alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.show();
                }
            });
            holder.btn_MoveUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position == 0)
                        return;
                    IPicture swap = mItems.get(position);
                    mItems.set(position, mItems.get(position - 1));
                    mItems.set(position - 1, swap);
                    notifyDataSetChanged();
                }
            });
            holder.btn_MoveDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position > getItemCount() - 2)
                        return;
                    IPicture swap = mItems.get(position);
                    mItems.set(position, mItems.get(position + 1));
                    mItems.set(position + 1, swap);
                    notifyDataSetChanged();
                }
            });
        } else {
            holder.btn_New.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(RootActivity, "New Image!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RootActivity.getBaseContext(), GalleryActivity.class);
                    RootActivity.startActivityForResult(intent, RequestCodes.SelectNewPhoto);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size() + 1;
    }
}
