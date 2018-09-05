package db;

/**
 * Created by kevin on 3/4/17.
 */
public class MyInt extends MyValue {
    Integer myVal;

    public MyInt() {
        isNan = false;
        isNoVal = true;
        type = "int";
        myVal = new Integer(0);
    }

    public MyInt(String s) {
        isNoVal = false;
        isNan = false;
        type = "int";
        s = s.replaceAll("\'|\"", "");
        myVal = new Integer(s);
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
        } else if (a.getType().equals("float")){
            return Float.compare((float) myVal, (float) a.getVal());
        } else {
            return Integer.compare(myVal, (int) a.getVal());
        }
    }
    public static void main(String[] args) {
        MyInt a = new MyInt("2");
        MyFloat b = new MyFloat("0.5");
        System.out.println(a.div(b));
    }

    @Override
    public MyValue add(MyValue a) {
        if (this.isNan || a.isNan) {
            MyValue temp = new MyInt();
            temp.makeNan();
            return temp;
        }
        if (a.getType().equals("float")) {
            return new MyFloat(myVal + (float) a.getVal() + "");
        }
        return new MyInt(myVal + (int) a.getVal() + "");
    }

    @Override
    public MyValue sub(MyValue a) {
        if (this.isNan || a.isNan) {
            MyValue temp = new MyInt();
            temp.makeNan();
            return temp;
        }
        if (a.getType().equals("float")) {
            return new MyFloat(myVal - (float) a.getVal() + "");
        }
        return new MyInt(myVal - (int) a.getVal() + "");
    }

    @Override
    public MyValue mul(MyValue a) {
        if (this.isNan || a.isNan) {
            MyValue temp = new MyInt();
            temp.makeNan();
            return temp;
        }
        if (a.getType().equals("float")) {
            return new MyFloat(myVal * (float) a.getVal() + "");
        }
        return new MyInt(myVal * (int) a.getVal() + "");
    }

    @Override
    public MyValue div(MyValue a) {
        if (this.isNan || a.isNan || a.getVal().equals(0)) {
            MyValue temp = new MyInt();
            temp.makeNan();
            return temp;
        }
        if (a.getType().equals("float")) {
            MyFloat val = new MyFloat(myVal / (float) a.getVal() + "");
            if ((float) val.getVal() == Float.POSITIVE_INFINITY || (float) val.getVal() == Float.POSITIVE_INFINITY) {
                MyFloat temp = new MyFloat();
                temp.makeNan();
                return temp;
            }
            return val;
        }
        return new MyInt(myVal / (int) a.getVal() + "");
    }

    @Override
    public boolean equals(MyValue a){
        if (this.isNan && a.isNan) {
            return true;
        } else if (this.isNan || a.isNan) {
            return false;
        } else if (this.isNoVal || a.isNoVal) {
            return false;
        } else if (a.getType().equals("float")){
            float temp = (float) myVal;
            return (temp == (float) a.getVal());
        } else {
            return myVal.equals(a.getVal());
        }
    }
}