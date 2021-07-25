import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class DirectNode<T> extends Node<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Nullable
    private T data;
    @Nullable private T parent;

    public DirectNode() {
        this(null);
    }

    public DirectNode(@Nullable final T data) {
        this(data,null);
    }

    public DirectNode(@Nullable final T data, @Nullable final DirectNode<T> parent) {
        this.data = data;
        this.parent = parent == null ? null :parent.getData();
    }

    @Nullable
    public T getData() {
        return data;
    }

    @NotNull
    public DirectNode<T> setData(T data) {
        this.data = data;
        return this;
    }

    @Nullable
    public DirectNode<T> getParent() {
        return new DirectNode<>(parent);
    }

    @NotNull
    public DirectNode<T> setParent(@Nullable final DirectNode<T> parent) {
        this.parent = parent.getData();
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectNode<?> that = (DirectNode<?>) o;
        return Objects.equals(data, that.data) &&
                Objects.equals(parent, that.parent);
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