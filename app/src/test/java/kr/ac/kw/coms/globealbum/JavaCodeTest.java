package kr.ac.kw.coms.globealbum;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kotlin.Pair;
import kr.ac.kw.coms.globealbum.provider.Promise;
import kr.ac.kw.coms.globealbum.provider.RemoteJava;

import static org.junit.Assert.assertEquals;

public class JavaCodeTest {
    @org.junit.Test
    public void clientTest() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);
        RemoteJava remote = new RemoteJava();
        Promise<Pair<String, String>> prom = new Promise<Pair<String, String>>() {
            @Override
            public void success(Pair<String, String> result) {
                lock.countDown();
            }

            @Override
            public void failure(@NotNull Throwable cause) {
                lock.countDown();
            }
        };
        remote.reverseGeocode(37.54567, 126.9944, prom);
        lock.await(10, TimeUnit.SECONDS);
        assertEquals(Objects.requireNonNull(prom.getAns()).getFirst(), "대한민국");
        assertEquals(prom.getAns().getSecond(), "서울특별시");
    }
}
