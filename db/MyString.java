package db;

/**
 * Created by kevin on 3/4/17.
 */
public class MyString extends MyValue {
    String myVal;

    public MyString() {
        isNan = false;
        isNoVal = true;
        myVal = "";
        type = "string";
    }

    public MyString(String s) {
        isNan = false;
        isNoVal = false;
        type = "string";
        myVal = s;
    }

    @Override
    public Object getVal() {
        return myVal;
    }

    @Override
    public int compareTo(MyValue a) {
        if (isNan && a.isNan) {
            return 0;
        } else if (isNan && !a.isNan) {
            return 1;
        } else if (!isNan && a.isNan) {
            return -1;
        } else {
            return myVal.compareTo((String) a.getVal());
        }
    }

    @Override
    public MyValue add(MyValue a) {
        String temp = myVal;
        temp = temp.replace("'", "");
        String atemp = (String) a.getVal();
        atemp = atemp.replace("'", "");
        String newString = "'" + temp + atemp + "'";
        return new MyString(newString);
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
        if (this.isNan && a.isNan) {
            return true;
        } else if (this.isNan || a.isNan) {
            return false;
        } else if (this.isNoVal || a.isNoVal) {
            return false;
        } else {
            return myVal.equals(a.getVal());
        }
    }
}