package db;

/**
 * Created by kevin on 3/4/17.
 */
public abstract class MyValue implements Value {
    String type;
    boolean isNan;
    boolean isNoVal;

    void makeNan() {
        isNan = true;
        isNoVal = false;
    }

    String getType() {
        return type;
    }



    @Override
    public String toString() {
        if (this.isNan) {
            return "NaN";
        } else if (this.isNoVal) {
            return "NOVALUE";
        } else {
            return getVal().toString();
        }
    }

    public boolean checkType(MyValue a) {
        String thisType = this.getType();
        String aType = a.getType();
        if (thisType.equals("string")) {
            return !(aType.equals("int") || aType.equals("float"));
        }
        if (aType.equals("string")) {
            return !(thisType.equals("int") || thisType.equals("float"));
        }
        return true;
    }
}