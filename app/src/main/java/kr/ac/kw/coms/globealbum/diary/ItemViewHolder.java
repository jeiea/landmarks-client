package kr.ac.kw.coms.globealbum.diary;

import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import kr.ac.kw.coms.globealbum.R;

public class ItemViewHolder extends RecyclerView.ViewHolder {
    ConstraintLayout rootLayout;
    ImageView imageView;
    TextView text_Title;
    ImageButton btn_Delete;
    Button btn_New;
    Button btn_MoveUp;
    Button btn_MoveDown;

    public ItemViewHolder(AppCompatActivity RootActivity, View itemView) {
        super(itemView);
        rootLayout = (ConstraintLayout) itemView;
        imageView = (ImageView) rootLayout.getViewById(R.id.verticalList_Image);
        text_Title = (TextView) rootLayout.getViewById(R.id.verticalList_Title);
        if (rootLayout.getId() == R.id.verticalList_Root)
            btn_Delete = (ImageButton) rootLayout.getViewById(R.id.verticalList_Delete);
        else
            btn_New = (Button) rootLayout.getViewById(R.id.verticalList_New);
        btn_MoveUp = (Button) rootLayout.getViewById(R.id.verticalList_MoveUp);
        btn_MoveDown = (Button) rootLayout.getViewById(R.id.verticalList_MoveDown);
    }
}
