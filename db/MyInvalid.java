package db;

/**
 * Created by kevin on 3/5/17.
 */
public class MyInvalid extends MyValue {

    public MyInvalid() {
        type = "invalid";
    }

    public String toString() {
        return "shouldn't be returned";
    }
    @Override
    public int compareTo(MyValue a) {
        return 0;
    }

    @Override
    public MyValue add(MyValue a) {
        return null;
    }

    @Override
    public MyValue sub(MyValue a) {
        return null;
    }

    @Override
    public MyValue mul(MyValue a) {
        return null;
    }

    @Override
    public MyValue div(MyValue a) {
        return null;
    }

    @Override
    public boolean equals(MyValue a) {
        return false;
    }

    @Override
    public Object getVal() {
        return null;
    }
}