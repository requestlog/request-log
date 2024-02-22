package io.github.requestlog.core.support.tuples;

import lombok.Getter;

import java.util.Objects;


@Getter
public final class Tuple2<T1, T2> {

    private final T1 t1;
    private final T2 t2;

    public Tuple2(T1 t1, T2 t2) {
        this.t1 = Objects.requireNonNull(t1, "t1 cannot be null");
        this.t2 = Objects.requireNonNull(t2, "t2 cannot be null");
    }


    public static <T1, T2> Tuple2<T1, T2> of(T1 t1, T2 t2) {
        return new Tuple2<>(t1, t2);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Tuple2) {
            Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) obj;
            return this.t1.equals(tuple2.t1) && this.t2.equals(tuple2.t2);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (String.valueOf(t1) + t2).hashCode();
    }

}
