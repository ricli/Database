package db;

/**
 * Created by kevin on 3/4/17.
 */
public interface Value {
    int compareTo(MyValue a);

    String toString();

    MyValue add(MyValue a) throws StringException;

    MyValue sub(MyValue a) throws StringException;

    MyValue mul(MyValue a) throws StringException;

    MyValue div(MyValue a) throws StringException;

    boolean equals(MyValue a) throws StringException;

    Object getVal();
}