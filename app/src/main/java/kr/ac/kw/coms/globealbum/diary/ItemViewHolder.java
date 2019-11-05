package kr.ac.kw.coms.globealbum.diary;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import kr.ac.kw.coms.globealbum.R;

public class ItemViewHolder extends RecyclerView.ViewHolder {
    ConstraintLayout rootLayout;
    ImageView imageView;
    TextView text_Title;
    ImageButton btn_Delete;
    ImageButton btn_New;
    ImageButton btn_MoveUp;
    ImageButton btn_MoveDown;

    public ItemViewHolder(AppCompatActivity RootActivity, View itemView) {
        super(itemView);
        rootLayout = (ConstraintLayout) itemView;
        imageView = (ImageView) rootLayout.getViewById(R.id.verticalList_Image);
        text_Title = (TextView) rootLayout.getViewById(R.id.verticalList_Title);
        if (rootLayout.getId() == R.id.verticalList_Root) {
            btn_Delete = (ImageButton) rootLayout.getViewById(R.id.verticalList_Delete);
            btn_MoveUp = (ImageButton) rootLayout.getViewById(R.id.verticalList_MoveUp);
            btn_MoveDown = (ImageButton) rootLayout.getViewById(R.id.verticalList_MoveDown);
        } else
            btn_New = (ImageButton) rootLayout.getViewById(R.id.verticalList_New);

    }
}
