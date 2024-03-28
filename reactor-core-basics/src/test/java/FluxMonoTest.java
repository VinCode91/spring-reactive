import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FluxMonoTest {

    @Test
    void collectFluxElements() {
        List<Integer> elements = new ArrayList<>();

        Flux.just(1, 2, 3, 4)
                .log()
                .subscribe(elements::add);
        assertThat(elements).containsExactly(1, 2, 3, 4);
    }

    @Test
    void mapFluxItems() {
        List<Integer> elements = new ArrayList<>();

        Flux.just(1, 2, 3, 4)
                .log()
                 .map(i -> i*2)
                .subscribe(elements::add);
        assertThat(elements).containsExactly(2, 4, 6, 8);
    }

    @Test
    void combineStreams() {
        List<String> elements = new ArrayList<>();
        Flux.just(1, 2, 3, 4)
                .log()
                .map(i -> i*2)
                .zipWith(Flux.range(0, Integer.MAX_VALUE).log(),
                        (one, two) -> String.format("First Flux: %d, Second Flux: %d", one, two))
                .log()
                .subscribe(elements::add);

        assertThat(elements).containsExactly(
                "First Flux: 2, Second Flux: 0",
                "First Flux: 4, Second Flux: 1",
                "First Flux: 6, Second Flux: 2",
                "First Flux: 8, Second Flux: 3"
        );
    }

    @Test
    void backpressureTest() {
        List<Integer> elements = new ArrayList<>();
        Flux.just(1, 2, 3, 4)
                .log()
                .subscribe(new Subscriber<Integer>() {

                    private Subscription s;
                    int onNextAmount;
                    @Override
                    public void onSubscribe(Subscription s) {
                        System.out.println("Request method call - limit number of items sent by stream");
                        this.s = s;
                        s.request(2);
                    }
                    @Override
                    public void onNext(Integer integer) {
                        elements.add(integer);
                        onNextAmount++;
                        if (onNextAmount % 2 == 0) {
                            s.request(2);
                        }
                    }
                    @Override
                    public void onError(Throwable throwable) {}
                    @Override
                    public void onComplete() {}
                });
        assertThat(elements).containsExactly(1, 2, 3, 4);
    }
}
