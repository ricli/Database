package db;

/**
 * Created by kevin on 3/6/17.
 */
public class MyColName extends MyValue{
    String myVal;
    MyColName(String s) {
        type = "colname";
        myVal = s;
    }

    @Override
    public int compareTo(MyValue a) {
        return 0;
    }

    @Override
    public MyValue add(MyValue a) throws StringException {
        return null;
    }

    @Override
    public MyValue sub(MyValue a) throws StringException {
        return null;
    }

    @Override
    public MyValue mul(MyValue a) throws StringException {
        return null;
    }

    @Override
    public MyValue div(MyValue a) throws StringException {
        return null;
    }

    @Override
    public boolean equals(MyValue a) throws StringException {
        return false;
    }

    @Override
    public Object getVal() {
        return null;
    }
}