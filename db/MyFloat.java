package db;

/**
 * Created by kevin on 3/4/17.
 */
public class MyFloat extends MyValue {
    Float myVal;

    public MyFloat() {
        isNan = false;
        isNoVal = true;
        myVal = new Float(0);
        type = "float";
    }

    public MyFloat(String s) {
        isNan = false;
        isNoVal = false;
        type = "float";
        s = s.replaceAll("\'|\"", "");
        myVal = new Float(s);
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
        } else if (a.getType().equals("int")){
            return Float.compare(myVal, new Float(a.getVal().toString()));
        } else {
            return Float.compare(myVal, new Float(a.getVal().toString()));
        }
    }

    @Override
    public MyValue add(MyValue a) throws StringException{
        if (this.isNan || a.isNan) {
            MyValue temp = new MyFloat();
            temp.makeNan();
            return temp;
        }
        return new MyFloat(myVal + new Float(a.getVal().toString()) + "");
    }

    @Override
    public MyValue sub(MyValue a) throws StringException{
        if (this.isNan || a.isNan) {
            MyValue temp = new MyFloat();
            temp.makeNan();
            return temp;
        }
        return new MyFloat(myVal - new Float(a.getVal().toString()) + "");
    }

    @Override
    public MyValue mul(MyValue a) throws StringException{
        if (this.isNan || a.isNan) {
            MyValue temp = new MyFloat();
            temp.makeNan();
            return temp;
        }
        return new MyFloat(myVal * new Float(a.getVal().toString()) + "");
    }

    @Override
    public MyValue div(MyValue a) throws StringException{
        if (this.isNan || a.isNan || a.isNoVal) {
            MyValue temp = new MyFloat();
            temp.makeNan();
            return temp;
        }
        MyFloat val = new MyFloat(myVal / new Float(a.getVal().toString()) + "");
        if ((float) val.getVal() == Float.POSITIVE_INFINITY || (float) val.getVal() == Float.POSITIVE_INFINITY) {
            MyFloat temp = new MyFloat();
            temp.makeNan();
            return temp;
        }
        return val;
    }

    @Override
    public boolean equals(MyValue a) {
        if (this.isNan && a.isNan) {
            return true;
        } else if (this.isNan || a.isNan) {
            return false;
        } else if (this.isNoVal || a.isNoVal) {
            return false;
        } else if (a.getType().equals("int")){
            MyFloat temp = new MyFloat(a.getVal().toString());
            return myVal.equals(temp.getVal());
        } else {
            return myVal.equals(a.getVal());
        }
    }

    @Override
    public String toString() {
        if (this.isNan) {
            return "NaN";
        } else if (this.isNoVal) {
            return "NOVALUE";
        } else {
            return String.format("%.3f", getVal());
        }
    }
}