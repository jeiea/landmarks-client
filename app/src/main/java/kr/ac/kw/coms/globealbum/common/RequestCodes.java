package kr.ac.kw.coms.globealbum.common;

public final class RequestCodes {
    //startActivityForResult()의 RequestCode 지정
    public final static int OpenFromMainActivity = 1;//초기 화면에서 사용하는 코드
    public final static int MakeNewDiary = 1001;//새 다이어리 작성
    public final static int SelectNewPhoto = 1002;//새로 등록할 사진 선택
    public final static String ACTION_SELECT_PHOTO = "select_photo";
    public final static String ACTION_VIEW_PHOTO = "view_photo";
    public final static String ACTION_EDIT_DIARY = "edit_diary";
    public final static String ACTION_DIARY_OTHERS = "diary_others";
    public final static String ACTION_DIARY_MINE = "diary_mine";
}
