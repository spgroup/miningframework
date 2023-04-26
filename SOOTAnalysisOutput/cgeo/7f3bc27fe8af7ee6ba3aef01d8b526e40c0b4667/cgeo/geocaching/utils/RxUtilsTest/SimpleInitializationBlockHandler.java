package cgeo.geocaching.utils;

import static org.assertj.core.api.Assertions.assertThat;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import android.test.AndroidTestCase;

public class RxUtilsTest extends AndroidTestCase {

    private static final ReplaySubject<Integer> range = ReplaySubject.createWithSize(10);

    static {
        for (int i = 1; i <= 10; i++) {
            range.onNext(i);
        }
        range.onCompleted();
    }

    public static void testTakeUntil() {
        final Observable<Integer> observable = range.lift(RxUtils.operatorTakeUntil(new Func1<Integer, Boolean>() {

            @Override
            public Boolean call(final Integer value) {
                return value > 6;
            }
        }));
        assertThat(observable.toList().toBlocking().single().toArray()).isEqualTo(new int[] { 1, 2, 3, 4, 5, 6, 7 });
    }

    public static void testRememberLast() {
        final PublishSubject<String> rawObservable = PublishSubject.create();
        final Observable<String> observable = RxUtils.rememberLast(rawObservable, "initial");
        assertThat(observable.toBlocking().first()).isEqualTo("initial");
        assertThat(observable.toBlocking().first()).isEqualTo("initial");
        rawObservable.onNext("without subscribers");
        assertThat(observable.toBlocking().first()).isEqualTo("initial");
        final Subscription subscription = observable.subscribe();
        rawObservable.onNext("first");
        assertThat(observable.toBlocking().first()).isEqualTo("first");
        subscription.unsubscribe();
        assertThat(observable.toBlocking().first()).isEqualTo("first");
    }
}