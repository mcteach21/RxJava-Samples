package mc.apps.rxjava;

import io.reactivex.*;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import jdk.swing.interop.SwingInterOpUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class Samples {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("***************************************");
        System.out.println("*********** RxJava Samples ************");
        System.out.println("***************************************");

        // Observables();
        // Operators();
        test();
        // schedule();
        //FlowableAndBackPressure();
        System.out.println("***************************************");
        System.out.println("***************************************");
    }

    private static void Operators() throws InterruptedException {

        String[] names = {"homer","marge", "bart", "lisa", "maggie"};
        final String family ="SIMPSON";

        Observable
                .fromArray(names)
                .map(new Function<String, String>(){
                    @Override
                    public String apply(@NonNull String name) throws Exception {
                        return name.substring(0,1).toUpperCase().concat(name.substring(1).toLowerCase()).concat(" "+family);
                    }
                }).subscribe(
                        System.out::println,
                        Throwable::printStackTrace,
                        () ->System.out.println("Done!")
                );

        System.out.println();
        // filter + map
        Observable.range(1,100)
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(@NonNull Integer n) throws Exception {
                        return n%5==0;
                    }
                })
                .map(new Function<Integer, String>(){
                    @Override
                    public String apply(@NonNull Integer n) {
                        return n.toString();
                    }
                })
                .subscribe(
                        s-> display(s),
                        throwable -> display(throwable.getLocalizedMessage()),
                        () -> System.out.println("Observable : done!")
                );

        System.out.println();
        // flatmap
        Observable
                .fromArray(names)
                .flatMap(new Function<String, Observable<String>>() {
                    @Override
                    public Observable<String> apply(@NonNull String name) throws Exception {
                        return Observable.just(name).map(n->formatted(n)+" "+family);
                    }
                    private String formatted(String name) {
                        return name.substring(0,1).toUpperCase().concat(name.substring(1).toLowerCase());
                    }
                })
                .subscribe(
                        System.out::println,
                        Throwable::printStackTrace,
                        () ->System.out.println("Done!")
                );

        System.out.println();
        // scan
        Observable
                .fromArray(names)
                .scan((n1,n2) -> n1+n2)
                .subscribe(System.out::println);



        // group by
        Observable
                .fromArray(names)
                .groupBy(s -> s.length())
//                .flatMapSingle(x -> x.toList())
                .flatMapSingle(group -> group.toList().map(e -> group.getKey() + " : " + e))
//                        group.reduce("", (x,y) -> "".equals(x) ? y : x + ", " + y))
//                      .map(e -> group.getKey() + ": " + e))
                .subscribe(System.out::println);

        // buffer
        Observable
                .range(1,10)
                .buffer(3)
                .subscribe(System.out::println);


        // create
        Observable observable = Observable.create((ObservableOnSubscribe) observableEmitter -> {
            for (int i = 0; i < 10 ; i++) {
                observableEmitter.onNext(i + " : Hello from RxJava!");
                Thread.sleep(300);
            }
            observableEmitter.onComplete();
        });

        observable.subscribe(System.out::println);


        // just
        Observable<Integer> observable1 = Observable.just(1, 2, 3, 4, 5);
        Observable<String> observable2 = Observable.just("Java", "Kotlin");

        observable1.subscribe(System.out::println);
        observable2.subscribe(System.out::println);


        // from
        Observable
                .fromArray(names)
                .map(String::toUpperCase)
                .subscribe(System.out::println);

        System.out.println();
        Observable
                .fromCallable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        Random random = new Random();
                        return random.ints(69, 123)
                                .limit(25)
                                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                                .toString();
                    }
                })
                .subscribe(System.out::println);

        //range
        List<String> languages = Arrays.asList("Java", "Python", "CSharp", "Kotlin");
        Observable
                .range(0,languages.size())
                .subscribe(
                        i -> System.out.print(languages.get(i) + (i==languages.size()-1?".":" | "))
                );

        //defer
        Observable<String> observable_default = DeferTest.getObservable();
        Observable<String>  deferObservable   =  DeferTest.getDeferObservable();

        DeferTest.setValue("new_value");
        System.out.println();

        observable_default.subscribe(v-> System.out.println("Default Observable value : "+v));
        deferObservable.subscribe(v-> System.out.println("Defer Observable value : "+v));

        // interval
        CountDownLatch countDownLatch = new CountDownLatch(1);
        /**
         * countDownLatch : met en attente autres threads jusqu'à fin traitement observer
         * compte à rebours = 0
         */
        Observable
                .interval(1, TimeUnit.SECONDS)      //
                .subscribe(
                        value -> System.out.println("onNext -> " + value),
                        Throwable::printStackTrace,
                        () -> {
                            countDownLatch.countDown();      // décremente compte à rebours
                        }
                );

        // attente (5s) avant fin observable
        countDownLatch.await(5, TimeUnit.SECONDS);
        System.out.println("Done!");


        // repeat
        Observable.just(1, 2, 3)
                .repeat(2)
                .subscribe(
                        value -> System.out.println("onNext -> " + value),
                        Throwable::printStackTrace,
                        () -> System.out.println("Done!")
                );

        // timer
        System.out.println("message dans 10 secondes..");
        Observable
                .timer(10, TimeUnit.SECONDS)
                .blockingSubscribe( v -> System.out.println("Merci d'avoir patienté! :)") );

        System.out.println("Done!");

        System.out.println("empty..");
        Observable
                .empty()
                .subscribe(
                        v -> System.out.println("ne s'affiche pas!"),
                        Throwable::printStackTrace,
                        () -> System.out.println("Done!")
                );

        System.out.println("never..");
        Observable
                .never()
                .subscribe(
                        v -> System.out.println("ne s'affiche pas!"),
                        Throwable::printStackTrace,
                        () -> System.out.println("..ne s'affiche pas non plus!")
                );

        System.out.println("throw (error)..");
        Observable
                .error(new IOException())
                .subscribe(
                        v -> System.out.println("ne s'affiche pas!"),
                        Throwable::printStackTrace,
                        () -> System.out.println("..ne s'affiche pas non plus!")
                );

    }

    private static class DeferTest{
        static String value="Default";

        public static Observable<String> getObservable(){
            System.out.println("create observable - current value = "+value);
            return Observable.just(value);
        }
        public static Observable<String> getDeferObservable(){
            System.out.println("create Defer observable - current value = "+value);
            return Observable.defer((Callable<ObservableSource<String>>) () -> Observable.just(value));
        }

        public static void setValue(String _value) {
            System.out.println("update value : "+value+" => "+_value);
            value = _value;
        }
    }
    private static void test() throws InterruptedException {
        System.out.println();
        //List<String> languages_list = Arrays.asList("Java", "Python", "CSharp", "Kotlin");

        String[] languages_array = {"Java", "Python", "CSharp", "Kotlin"};
        List<String> languages_list = Arrays.asList(languages_array);

        Observable
                //.just("Java", "Python", "CSharp", "Kotlin")
                .fromArray(languages_list)        // Observable<List<String>>
                .flatMapIterable(l->l)            // Observable<String>
//                .fromArray(languages_array)         // Observable<String>
                .collect(StringBuilder::new, (stringBuilder, value) -> stringBuilder.append(value+"|"))
//                .subscribe(System.out::println);
        .subscribe(new SingleObserver<StringBuilder>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
                System.out.println(Thread.currentThread().getName() + "(" + Thread.currentThread().getId() + ")");
            }

            @Override
            public void onSuccess(@NonNull StringBuilder sb) {
                System.out.println(Thread.currentThread().getName() + "(" + Thread.currentThread().getId() + ")");
                System.out.println(sb);
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
                System.out.println(Thread.currentThread().getName() + "(" + Thread.currentThread().getId() + ")");
            }
        });

        System.out.println();
    }

    private static void schedule(){
        String[] languages_array = {"Java", "Python", "CSharp", "Kotlin"};
        List<String> languages_list = Arrays.asList(languages_array);

        Observable.just("Java", "Python")
                .subscribeOn(Schedulers.newThread())
                .doOnSubscribe(s ->
                        System.out.println("doOnSubscribe - Thread : " +Thread.currentThread().getName())
                )
                .observeOn(Schedulers.newThread())
                .doOnNext(s ->
                        System.out.println("doOnNext - Thread : " +Thread.currentThread().getName())
                )
                .observeOn(Schedulers.newThread())
                .map(String::toUpperCase)
                .doOnNext(s ->
                        System.out.println("doOnNext (map) - Thread : " +Thread.currentThread().getName())
                )
                .observeOn(Schedulers.newThread())
                .subscribe(s ->
                        System.out.println("subscribe - Thread : " +Thread.currentThread().getName())
                );

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Observable.just(1,2,3)
                .subscribeOn(Schedulers.from(executor))
                .doOnNext(s ->
                        System.out.println("Observable 1 - doOnNext  - Thread : " +Thread.currentThread().getName())
                )
                .map(integer -> {
                    Thread.sleep(60);
                    return integer;
                })
                .subscribe(s-> System.out.println("Observable 1 - subscribe - Thread1 : " +Thread.currentThread().getName() +" => "+s));
        Observable.just(4,5,6)
                .subscribeOn(Schedulers.from(executor))
                .doOnNext(s ->
                        System.out.println("Observable 2 - doOnNext  - Thread : " +Thread.currentThread().getName())
                )
                .map(integer -> {
                    Thread.sleep(50);
                    return integer;
                })
                .observeOn(Schedulers.newThread())
                .subscribe(s-> System.out.println("Observable 2 - subscribe - Thread2 : " +Thread.currentThread().getName() +" => "+s));
        Observable.just(7,8)
                .subscribeOn(Schedulers.from(executor))
                .doOnNext(s ->
                     System.out.println("Observable 3 - doOnNext  - Thread : " +Thread.currentThread().getName())
                )
                .map(integer -> {
                    Thread.sleep(10);
                    return integer;
                })
                .subscribe(s-> System.out.println("Observable 3 - subscribe - Thread3 : " +Thread.currentThread().getName()+" => "+s));

        Observable
                .fromArray(languages_list)
                .subscribeOn(Schedulers.newThread())
                .flatMapIterable(l->l)
                .observeOn(Schedulers.newThread())
                .collect(StringBuilder::new, StringBuilder::append)
                .observeOn(Schedulers.newThread())
                .subscribe(new SingleObserver<>() {

                    public void onSubscribe(@NonNull Disposable disposable) {
                        System.out.println("onSubscribe : " + Thread.currentThread().getName() + "(" + Thread.currentThread().getId() + ")");
                    }

                    public void onSuccess(@NonNull StringBuilder sb) {
                        System.out.println("onSuccess : " + Thread.currentThread().getName() + "(" + Thread.currentThread().getId() + ")");
                        System.out.println(sb);
                    }
                    public void onError(@NonNull Throwable throwable) {
                        System.out.println(Thread.currentThread().getName() + "(" + Thread.currentThread().getId() + ")");
                    }
                });

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) { }

    }
    private static String formatted(String name) {
        return name.substring(0,1).toUpperCase().concat(name.substring(1).toLowerCase());
    }
    private static void Observables(){

        String[] letters = {"a", "b", "c", "d", "e"};
        final StringBuilder result = new StringBuilder();

        Observable<String> observable = Observable.fromArray(letters);
        observable
                .map(String::toUpperCase)
                .subscribe(
                i -> {
                    result.append(i);
                    try {
                        Thread.sleep(200);
                        System.out.println(result.toString());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                },  									// OnNext
                Throwable::printStackTrace, 			// OnError
                () -> result.append("_Completed")       //OnCompleted
        );
        System.out.println("result = "+ result);

        Observable<String> observable2 = Observable.just("hello","from","RxJava");
        observable2
                .map(s->s.toUpperCase())
                .subscribe(
                        s-> display(s),
                        Throwable::printStackTrace,             // OnError
                        () -> System.out.println("Observable : I've finished!")
                );

//        final String[] result = {""};
//        Observer<String> observer_1 = new Observer<>() {
//            @Override
//            public void onSubscribe(@NonNull Disposable disposable) {
//                //result[0] +="subscribe!";
//            }
//
//            @Override
//            public void onNext(@NonNull String s) {
//                result[0] +=s+" ";
//            }
//
//            @Override
//            public void onError(@NonNull Throwable throwable) {
//                throwable.printStackTrace();
//            }
//
//            @Override
//            public void onComplete() {
//                System.out.println(result[0]);
//                System.out.println("Observable : I've finished!");
//            }
//        };
//        observable
//                .map(s->s.toLowerCase())
//                .subscribe(observer_1);
//

    }


    private static void FlowableAndBackPressure() throws InterruptedException {
        PublishSubject<Integer> observable = PublishSubject.create();
        observable
//                .toFlowable(BackpressureStrategy.MISSING)
                .toFlowable(BackpressureStrategy.BUFFER)
                .observeOn(Schedulers.computation())
                .subscribe(
                        s -> {
                            System.out.println("Observer   - handle : "+s+" - Thread : "+Thread.currentThread().getName());
                            Thread.sleep(s%2==0?20:10);
                        },
                        Throwable::printStackTrace
                );

        for (int i = 0; i < 1_000_000; i++) {
            observable.onNext(i);
            System.out.println("Observable - publish : "+i+" - Thread : "+Thread.currentThread().getName());
            Thread.sleep(10);
        }

    }
    private static void display(String s) {
        try {
            System.out.print("processing..");
            Thread.sleep(200*s.length());
            System.out.println(s);
        } catch (InterruptedException e) {}
    }
}
