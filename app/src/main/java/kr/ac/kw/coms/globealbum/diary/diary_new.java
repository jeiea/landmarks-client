package kr.ac.kw.coms.globealbum.diary;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

import kr.ac.kw.coms.globealbum.R;
import kr.ac.kw.coms.globealbum.common.RequestCodes;
import kr.ac.kw.coms.globealbum.provider.Diary;
import kr.ac.kw.coms.globealbum.provider.IPicture;
import kr.ac.kw.coms.globealbum.provider.Promise;
import kr.ac.kw.coms.globealbum.provider.RemoteJava;
import kr.ac.kw.coms.globealbum.provider.RemotePicture;
import kr.ac.kw.coms.globealbum.provider.UIPromise;
import kr.ac.kw.coms.landmarks.client.CollectionInfo;

//새로 작성하는 화면. 여행지 목록에 플로팅 버튼을 통해 진입할 예정.
public class diary_new extends AppCompatActivity {
    CollectionInfo diary;
    NewImageListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_new);

        diary = new CollectionInfo();
        RecyclerView Edit_ImageList = findViewById(R.id.diary_edit_ImageList);
        adapter = new NewImageListAdapter(this, new ArrayList<IPicture>());
        Edit_ImageList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        Edit_ImageList.setAdapter(adapter);
    }

    public void diary_edit_onClick(View view) {
        //새로 쓰기 화면에서 뒤로/저장 버튼 클릭 시
        switch (view.getId()) {
            case R.id.diary_edit_btnBack:
                Toast.makeText(this, "작성 취소", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.diary_edit_btnSave:
                if (((TextView)findViewById(R.id.diary_edit_TitleText)).getText().toString().isEmpty())
                {
                    Toast.makeText(this, "제목을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (((TextView)findViewById(R.id.diary_edit_DescriptionText)).getText().toString().isEmpty())
                {
                    Toast.makeText(this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (adapter.getItemCount() == 0)
                {
                    Toast.makeText(this, "사진을 추가해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                diary.setTitle(((TextView) findViewById(R.id.diary_edit_TitleText)).getText().toString());
                diary.setText(((TextView) findViewById(R.id.diary_edit_DescriptionText)).getText().toString());
                ArrayList<Integer> imgIds = new ArrayList<>();
                for (IPicture p : adapter.getItems()) {
                    imgIds.add(((RemotePicture) p).getInfo().getId());
                }
                diary.setImages(imgIds);
                diary.setLiking(false);

                //서버 통신 필요
                RemoteJava.INSTANCE.uploadCollection(diary, afterUpload);
                break;
        }
    }

    Promise<Diary> afterUpload = new UIPromise<Diary>() {
        @Override
        public void success(Diary result) {
            Toast.makeText(diary_new.this, "작성 완료", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void failure(@NotNull Throwable cause) {
            super.failure(cause);
        }
    };

    public class NewImageListAdapter extends RecyclerView.Adapter<ItemViewHolder> {
        final ArrayList<IPicture> mItems = new ArrayList<>();

        public NewImageListAdapter(AppCompatActivity RootActivity, ArrayList<IPicture> Items) {
            mItems.addAll(Items);
        }

        public void AddNewPicture(IPicture newPicture) {
            mItems.add(newPicture);
            notifyDataSetChanged();
        }

        public ArrayList<IPicture> getItems() {
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_map_n_pictures_verticallist, parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_map_n_pictures_verticallist_new, parent, false);
            }
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, final int position) {
            //목록의 내용 추가
            if (position < getItemCount() - 1) {
                Glide.with(holder.imageView).load(mItems.get(position)).into(holder.imageView);
                GeoPoint point = mItems.get(position).getMeta().getGeo();
                holder.text_Title.setText(mItems.get(position).getMeta().getAddress() + "\n위도 " + Math.round(point.getLatitude()) + ", 경도 " + Math.round(point.getLongitude()));
                holder.btn_Delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(diary_new.this);
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
                        Toast.makeText(diary_new.this, "New Image!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size() + 1;
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout rootLayout;
        ConstraintLayout updownBox;
        ImageView imageView;
        TextView text_Title;
        ImageButton btn_Delete;
        ImageButton btn_New;
        ImageButton btn_MoveUp;
        ImageButton btn_MoveDown;

        public ItemViewHolder(View itemView) {
            super(itemView);
            rootLayout = (ConstraintLayout) itemView;
            imageView = (ImageView) rootLayout.getViewById(R.id.verticalList_Image);
            text_Title = (TextView) rootLayout.getViewById(R.id.verticalList_Title);
            if (rootLayout.getId() == R.id.verticalList_Root) {
                btn_Delete = (ImageButton) rootLayout.getViewById(R.id.verticalList_Delete);
                updownBox = (ConstraintLayout) rootLayout.getViewById(R.id.verticalList_UpDownBox);
                btn_MoveUp = (ImageButton) updownBox.getViewById(R.id.verticalList_MoveUp);
                btn_MoveDown = (ImageButton) updownBox.getViewById(R.id.verticalList_MoveDown);
            } else
                btn_New = (ImageButton) rootLayout.getViewById(R.id.verticalList_New);

        }
    }
}
