package db;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.StringJoiner;

/**
 * Created by kevin on 2/27/17.
 */
class Table {
    LinkedList<MyList> table;

    Table() {
        table = new LinkedList<>();
    }

    Table(Table t) {
        table = new LinkedList<>();
        for (int i = 0; i < t.numCols(); i += 1) {
            MyList newList = new MyList(t.getCol(i));
            table.add(newList);
        }
    }

    Table(ArrayList<String> names, ArrayList<String> types) {
        table = new LinkedList<>();
        for (int i = 0; i < names.size(); i += 1) {
            table.add(new MyList(names.get(i), types.get(i)));
        }
    }

    void addCol(MyList m) {
        table.addLast(m);

    }

    void addRow(ArrayList<MyValue> row) {
        for (int i = 0; i < row.size(); i += 1) {
            getCol(i).addLast(row.get(i));
        }
    }

    void removeRow(int toBeRem) {
        for (int i = 0; i < numCols(); i += 1) {
            getCol(i).remove(toBeRem);
        }
    }

    MyList getCol(int i) {
        return table.get(i);
    }

    MyList getCol(String s) {
        for (int i = 0; i < numCols(); i += 1) {
            if (s.equals(getCol(i).getName())) {
                return getCol(i);
            }
        }
        return null;
    }

    ArrayList<MyValue> getRow(int index) {
        ArrayList<MyValue> row = new ArrayList<>();
        for (int i = 0; i < numCols(); i += 1) {
            MyValue value = table.get(i).get(index);
            row.add(value);
        }
        return row;
    }

    ArrayList<String> getColNames() {
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < numCols(); i += 1) {
            names.add(getCol(i).getName());
        }
        return names;
    }

    ArrayList<String> getColTypes() {
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < numCols(); i += 1) {
            names.add(getCol(i).getType());
        }
        return names;
    }

    int numCols() {
        return table.size();
    }

    int colSize() {
        if (table.isEmpty()) {
            return 0;
        }
        return table.get(0).size();
    }

    ArrayList<String> sameCol(Table t) {
        ArrayList<String> shared = new ArrayList<>();
        ArrayList<String> names1 = getColNames();
        ArrayList<String> names2 = t.getColNames();
        for (String s1 : names1) {
            for (String s2 : names2) {
                if (s1.equals(s2)) {
                    shared.add(s1);
                }
            }
        }
        return shared;
    }

    ArrayList<Integer> sameColIndex(ArrayList<String> shared) {
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 0; i < shared.size(); i += 1) {
            for (int k = 0; k < numCols(); k += 1) {
                if (this.getCol(k).getName().equals(shared.get(i))) {
                    indices.add(k);
                }
            }
        }
        return indices;
    }

    ArrayList<MyValue> allButGiven(int rowIndex, ArrayList<String> shared) {
        ArrayList<MyValue> spliced = new ArrayList<>();
        for (int i = 0; i < numCols(); i += 1) {
            if (!shared.contains(this.getCol(i).getName())) {
                spliced.add(this.getCol(i).get(rowIndex));
            }
        }
        return spliced;
    }

    Table joinTable(Table t) {
        try {
            ArrayList<String> shared = this.sameCol(t);
            ArrayList<Integer> sharedIndex = this.sameColIndex(shared);
            ArrayList<Integer> sharedIndexT = t.sameColIndex(shared);
            Table joined;
            if (shared.isEmpty()) {
                ArrayList<String> names1 = new ArrayList<>(this.getColNames());
                ArrayList<String> names2 = new ArrayList<>(t.getColNames());
                ArrayList<String> types1 = new ArrayList<>(this.getColTypes());
                ArrayList<String> types2 = new ArrayList<>(t.getColTypes());
                names1.addAll(names2);
                types1.addAll(types2);
                joined = new Table(names1, types1);
                for (int i = 0; i < colSize(); i += 1) {
                    ArrayList<MyValue> row1 = getRow(i);
                    for (int k = 0; k < t.colSize(); k += 1) {
                        ArrayList<MyValue> row2 = t.getRow(k);
                        ArrayList<MyValue> copy1 = new ArrayList<>(row1);
                        copy1.addAll(row2);
                        joined.addRow(copy1);
                    }
                }
            } else {
                ArrayList<String> names1 = new ArrayList<>(this.getColNames());
                ArrayList<String> names2 = new ArrayList<>(t.getColNames());
                ArrayList<String> types1 = new ArrayList<>(this.getColTypes());
                ArrayList<String> types2 = new ArrayList<>(t.getColTypes());
                ArrayList<String> newNames = new ArrayList<>();
                ArrayList<String> newTypes = new ArrayList<>();
                newNames.addAll(shared);
                for (int i = 0; i < sharedIndex.size(); i += 1) {
                    newTypes.add(getCol(sharedIndex.get(i)).getType());
                }
                int sharedSize = sharedIndex.size() - 1;
                int row1Size = names1.size();
                int row2Size = names2.size();
                for (int i = 0; i < sharedSize + 1; i += 1) {
                    names1.remove(shared.get(i));
                    names2.remove(shared.get(i));
                }
                for (int i = sharedSize; i >= 0; i -= 1) {
                    for (int k = row1Size - 1; k >= 0; k -= 1) {
                        if (sharedIndex.get(i) == k) {
                            types1.remove(k);
                        }
                    }
                    for (int k = row2Size - 1; k >= 0; k -= 1) {
                        if (sharedIndexT.get(i) == k) {
                            types2.remove(k);
                        }
                    }
                }
                newNames.addAll(names1);
                newNames.addAll(names2);
                newTypes.addAll(types1);
                newTypes.addAll(types2);
                joined = new Table(newNames, newTypes);
                for (int x = 0; x < this.colSize(); x += 1) {
                    for (int y = 0; y < t.colSize(); y += 1) {
                        for (int z = 0; z < shared.size(); z += 1) {
                            if (!this.getCol(shared.get(z)).get(x).equals
                                    (t.getCol(shared.get(z)).get(y))) {
                                break;
                            } else {
                                if (z == shared.size() - 1) {
                                    ArrayList<MyValue> newRow = new ArrayList<>();
                                    for (int i : sharedIndex) {
                                        newRow.add(this.getCol(i).get(x));
                                    }
                                    newRow.addAll(this.allButGiven(x, shared));
                                    newRow.addAll(t.allButGiven(y, shared));
                                    joined.addRow(newRow);
                                }
                            }
                        }
                    }
                }
            }
            return joined;
        } catch (StringException s) {
            return null;
        }
    }

    @Override
    public String toString() {
        String result = "";
        StringJoiner tableJoiner = new StringJoiner(",");
        for (int i = 0; i < numCols(); i += 1) {
            tableJoiner.add(getCol(i).getName() + " " + getCol(i).getType());
        }
        result = result + tableJoiner.toString() + "\n";
        for (int i = 0; i < colSize(); i += 1) {
            StringJoiner temp = new StringJoiner(",");
            for (int k = 0; k < numCols(); k += 1) {
                temp.add(getCol(k).get(i).toString());
            }
            result = result + temp.toString() + "\n";
        }
        return result.substring(0, result.length() - 1);
    }
}