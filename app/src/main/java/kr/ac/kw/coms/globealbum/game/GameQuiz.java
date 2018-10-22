package kr.ac.kw.coms.globealbum.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import kr.ac.kw.coms.globealbum.provider.IPicture;


interface IGameQuiz {
    int getMsTimeLimit();

    String getDescription();

    Collection<IPicture> getUsedPictures();
}

class PositionQuiz implements IGameQuiz {
    IPicture picture;

    PositionQuiz(IPicture picture) {
        this.picture = picture;
    }

    @Override
    public int getMsTimeLimit() {
        return 0;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Collection<IPicture> getUsedPictures() {
        ArrayList<IPicture> ret = new ArrayList<>();
        ret.add(picture);
        return ret;
    }
}

class PicChoiceQuiz implements IGameQuiz {
    ArrayList<IPicture> pictures;
    private int correctIdx;

    PicChoiceQuiz(List<? extends IPicture> pictures, Random random) {
        this.pictures = new ArrayList<>(pictures);
        correctIdx = random.nextInt(4);
    }

    @Override
    public int getMsTimeLimit() {
        return 0;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Collection<IPicture> getUsedPictures() {
        return pictures;
    }

    IPicture getCorrectPicture() {
        return pictures.get(correctIdx);
    }
}
