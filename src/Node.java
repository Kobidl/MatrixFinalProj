import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

public class Node<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Nullable
    private T data;
    @Nullable private Node<T> parent;

    public Node() {
        this(null);
    }

    public Node(@Nullable final T data) {
        this(data,null);
    }

    public Node(@Nullable final T data, @Nullable final Node<T> parent) {
        this.data = data;
        this.parent = parent;
    }

    @Nullable
    public T getData() {
        return data;
    }

    @NotNull
    public Node<T> setData(T data) {
        this.data = data;
        return this;
    }

    @Nullable
    public Node<T> getParent() {
        return parent;
    }

    @NotNull
    public Node<T> setParent(@Nullable final Node<T> parent) {
        this.parent = parent;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;

        Node<?> state1 = (Node<?>) o;

        return Objects.equals(data, state1.data);
    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    @Override
    public String toString() {
        return data.toString();
    }


}