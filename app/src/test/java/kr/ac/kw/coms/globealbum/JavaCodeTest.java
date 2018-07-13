package kr.ac.kw.coms.globealbum;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kotlin.Pair;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlinx.coroutines.experimental.Deferred;
import kr.ac.kw.coms.globealbum.provider.LandmarksClient;

import static org.junit.Assert.assertEquals;

public class JavaCodeTest {
    @org.junit.Test
    public void clientTest() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);
        Deferred<Pair<String, String>> d = new LandmarksClient()
                .reverseGeoJava(37.54567, 126.9944);
        d.invokeOnCompletion(new Function1<Throwable, Unit>() {
            @Override
            public Unit invoke(Throwable throwable) {
                lock.countDown();
                return null;
            }
        });
        lock.await(100, TimeUnit.SECONDS);
        Pair<String, String> p = d.getCompleted();
        assertEquals(p.getFirst(), "대한민국");
        assertEquals(p.getSecond(), "서울특별시");
    }
}
