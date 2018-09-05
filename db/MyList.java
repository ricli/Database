package db;

import java.util.LinkedList;

/**
 * Created by ericl on 3/4/2017.
 */
class MyList extends LinkedList<MyValue> {
    String name;
    String type;


    MyList(String name, String type) {
        super();
        this.type = type;
        this.name = name;
    }

    MyList(String name, MyList list) {
        for (MyValue x : list) {
            this.add(x);
        }
        this.name = name;
        this.type = list.getType();
    }

    MyList(MyList list) {
        for (MyValue x : list) {
            this.add(x);
        }
        this.name = list.getName();
        this.type = list.getType();
    }

    String newType(MyList a) {
        if (a.getType().equals("float") || this.getType().equals("float")) {
            return "float";
        }
        return getType();
    }

    String getName() {
        return name;
    }

    String getType() {
        return type;
    }
}