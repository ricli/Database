package db;

import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Database {
    private static final List<String> TYPES = Arrays.asList("string", "int", "float");
    // Various common constructs, simplifies parsing.
    private static final String REST = "\\s*(.*)\\s*",
            COMMA = "\\s*,\\s*",
            AND = "\\s+and\\s+";
    // Stage 1 syntax, contains the command name.
    private static final Pattern CREATE_CMD = Pattern.compile("create table " + REST),
            LOAD_CMD = Pattern.compile("load " + REST),
            STORE_CMD = Pattern.compile("store " + REST),
            DROP_CMD = Pattern.compile("drop table " + REST),
            INSERT_CMD = Pattern.compile("insert into " + REST),
            PRINT_CMD = Pattern.compile("print " + REST),
            SELECT_CMD = Pattern.compile("select " + REST);
    // Stage 2 syntax, contains the clauses of commands.
    private static final Pattern CREATE_NEW = Pattern.compile("(\\S+)\\s+\\((\\S+\\s+\\S+\\s*"
            + "(?:,\\s*\\S+\\s+\\S+\\s*)*)\\)"),
            SELECT_CLS = Pattern.compile("([^,]+?(?:,[^,]+?)*)\\s+from\\s+"
                    + "(\\S+\\s*(?:,\\s*\\S+\\s*)*)(?:\\s+where\\s+"
                    + "([\\w\\s+\\-*/'<>=!.]+?(?:\\s+and\\s+"
                    + "[\\w\\s+\\-*/'<>=!.]+?)*))?"),
            CREATE_SEL = Pattern.compile("(\\S+)\\s+as select\\s+"
                    + SELECT_CLS.pattern()),
            INSERT_CLS = Pattern.compile("(\\S+)\\s+values\\s+(.+?"
                    + "\\s*(?:,\\s*.+?\\s*)*)"),
            COND = Pattern.compile("([A-Za-z]\\w*)\\s*(<=|>=|>|<|==|!=)\\s*"
                    + "(\\d*\\.?\\d+|'.*?'|\\w+)"),
            MATH = Pattern.compile("([A-Za-z]\\w*)\\s*(-|\\+|/|\\*|)\\s*(\\d*\\.?\\d+|'.*?'|\\w+)"),
            MATH_SECOND = Pattern.compile("(-|\\+|/|\\*|)");


    HashMap<String, Table> allData;

    public Database() {
        allData = new HashMap<>();
    }

    public String transact(String query) {
        return eval(query);
    }

    private String storeTable(String name) {
        try (PrintWriter out = new PrintWriter(name + ".tbl")) {
            if (allData.containsKey(name)) {
                out.println(allData.get(name).toString());
            } else {
                return "ERROR: Table not found";
            }
            return "";
        } catch (FileNotFoundException e) {
            return "ERROR: file not found";
        }
    }

    private String dropTable(String name) {
        if (!allData.containsKey(name)) {
            return "ERROR: Dropped table not found";
        }
        allData.remove(name);
        return "";
    }

    private String eval(String query) {
        Matcher m;
        if ((m = CREATE_CMD.matcher(query)).matches()) {
            return createTable(m.group(1));
        } else if ((m = LOAD_CMD.matcher(query)).matches()) {
            return loadTable(m.group(1));
        } else if ((m = STORE_CMD.matcher(query)).matches()) {
            return storeTable(m.group(1));
        } else if ((m = DROP_CMD.matcher(query)).matches()) {
            return dropTable(m.group(1));
        } else if ((m = INSERT_CMD.matcher(query)).matches()) {
            return insertRow(m.group(1));
        } else if ((m = PRINT_CMD.matcher(query)).matches()) {
            return printTable(m.group(1));
        } else if ((m = SELECT_CMD.matcher(query)).matches()) {
            return select(m.group(1));
        } else {
            return "ERROR: Malformed query";
        }
    }

    private String select(String expr) {
        try {
            Matcher m = SELECT_CLS.matcher(expr);
            if (!m.matches()) {
                return "ERROR: Malformed select";
            }
            return select(m.group(1), m.group(2), m.group(3)).toString();
        } catch (StringException s) {
            return "ERROR: " + s.toString();
        }
    }

    private Table select(String exprs, String tables, String conds) throws StringException {
        try {
            Table newTable;
            String[] tableSplit = removeAll(tables);
            for (int i = 0; i < tableSplit.length; i += 1) {
                if (!allData.containsKey(tableSplit[i])) {
                    throw new StringException("ERROR: Table not found");
                }
            }
            Table allJoined = joinTables(tableSplit);
            String[] exprSplit = exprs.split(",");
            for (int i = 0; i < exprSplit.length; i += 1) {
                exprSplit[i] = exprSplit[i].trim();
            }
            if (Arrays.asList(exprSplit).contains("*")) {
                if (exprSplit.length != 1) {
                    throw new StringException("ERROR: Incorrect select with *");
                } else {
                    if (conds == null) {
                        return allJoined;
                    } else {
                        return filterTable(allJoined, conds);
                    }
                }
            }
            ArrayList<String> colNames = allJoined.getColNames();
            for (int k = 0; k < exprSplit.length; k += 1) {
                String partial = exprSplit[k];
                if (partial.contains(" as ")) /* contains as */ {
                    String[] split = partial.split(" as ");
                    String splitCond = split[0].replaceAll("\\s+", "");
                    String alias = split[1].replaceAll("\\s+", "");
                    Matcher m = MATH.matcher(splitCond);
                    if (m.matches()) /* contains as and math */ {
                        allJoined = asMath(allJoined, alias, m);
                    } else /* contains as does not contain arithmetic */ {
                        String[] splitMore = removeAll(splitCond);
                        for (int i = 0; i < splitMore.length; i += 1) {
                            String colName = splitMore[i];
                            if (!colNames.contains(colName)) {
                                throw new StringException("ERROR: Column not found (as, no math)");
                            }
                        }
                        for (int i = 0; i < splitMore.length; i += 1) {
                            MyList myCol = allJoined.getCol(splitMore[i]);
                            MyList newList = new MyList(alias, myCol);
                            allJoined.addCol(newList);
                        }
                    }
                } else /* no as */ {
                    allJoined = noAs(allJoined, partial);
                }
            }
            if (conds != null) {
                String[] condSplit = conds.split(" and ");
                for (int i = 0; i < condSplit.length; i += 1) {
                    allJoined = filterTableSingle(allJoined, condSplit[i]);
                }
            }
            newTable = selectHelper(allJoined, exprs);
            return newTable;
        } catch (StringException s) {
            throw new StringException("ERROR: " + s.toString());
        }
    }

    private Table noAs(Table t, String partial) throws StringException {
        ArrayList<String> colNames = t.getColNames();
        partial = partial.replaceAll(" ", "");
        Matcher m = MATH_SECOND.matcher(partial);
        if (m.matches()) /* does not contain as contains math*/ {
            Matcher a = MATH.matcher(partial);
            String operand1 = a.group(1).replaceAll("\\s+", "");
            String operator = a.group(2).replaceAll("\\s+", "");
            String operand2 = a.group(3).replaceAll("\\s+", "");
            if (colNames.contains(operand1)) {
                MyList col1 = t.getCol(operand1);
                MyList newCol;
                MyValue op2 = loadHelper(operand2);
                if (colNames.contains(operand2)) {
                    if (col1.size() != t.getCol(operand2).size()) {
                        throw new StringException("ERROR: Column sizes differ");
                    }
                    String str = col1.newType(t.getCol(operand2));
                    newCol = new MyList(col1.getName(), str);
                    for (int i = 0; i < col1.size(); i += 1) {
                        MyList col2 = t.getCol(operand2);
                        MyValue temp = simplifier(col1.get(i), col2.get(i), operator);
                        if (temp == null) {
                            throw new StringException("ERROR: Incorrect operator");
                        }
                        newCol.add(temp);
                    }
                } else if (op2.equals("invalid") || op2.equals("string")) {
                    throw new StringException("ERROR: wrong type or not in cols");
                } else {
                    newCol = new MyList(partial, col1.getType());
                    for (int i = 0; i < col1.size(); i += 1) {
                        MyValue temp = simplifier(col1.get(i), op2, operator);
                        if (temp == null) {
                            throw new StringException("ERROR: Incorrect operator (2)");
                        }
                        newCol.add(temp);
                    }
                }
                t.addCol(newCol);
            } else {
                throw new StringException("ERROR: Column name for operand 1 not found");
            }
        }
        return t;
    }

    private Table asMath(Table a, String s, Matcher m) throws StringException {
        ArrayList<String> colNames = a.getColNames();
        String operand1 = m.group(1);
        String operator = m.group(2);
        String operand2 = m.group(3);
        if (colNames.contains(operand1)) {
            MyList col1 = a.getCol(operand1);
            MyList newCol;
            MyValue op2 = loadHelper(operand2);
            if (colNames.contains(operand2)) {
                if (col1.size() != a.getCol(operand2).size()) {
                    throw new StringException("ERROR: Column sizes differ");
                }
                String str = col1.newType(a.getCol(operand2));
                newCol = new MyList(s, str);
                for (int i = 0; i < col1.size(); i += 1) {
                    MyList col2 = a.getCol(operand2);
                    MyValue temp = simplifier(col1.get(i), col2.get(i), operator);
                    if (temp == null) {
                        throw new StringException("ERROR: Incorrect operator");
                    }
                    newCol.add(temp);
                }
            } else if (op2.equals("invalid") || op2.equals("string")) {
                throw new StringException("ERROR: wrong type or not in cols");
            } else {
                newCol = new MyList(s, col1.getType());
                for (int i = 0; i < col1.size(); i += 1) {
                    MyValue temp = simplifier(col1.get(i), op2, operator);
                    if (temp == null) {
                        throw new StringException("ERROR: Incorrect operator (2)");
                    }
                    newCol.add(temp);
                }
            }
            a.addCol(newCol);
        } else {
            throw new StringException("ERROR: Column name for operand 1 not found");
        }
        return a;
    }

    private Table selectHelper(Table t, String exprs) {
        Table newTable = new Table();
        String[] exprSplit = exprs.split(",");
        for (String partial : exprSplit) {
            Matcher m = MATH_SECOND.matcher(partial);
            if (partial.contains(" as ")) {
                String[] split = partial.split(" as ");
                String alias = split[1].replaceAll("\\s+", "");
                for (int i = 0; i < t.numCols(); i += 1) {
                    if (t.getCol(i).getName().equals(alias)) {
                        newTable.addCol(new MyList(alias, t.getCol(i)));
                    }
                }
            } else if (m.matches()) {
                partial = partial.replaceAll(" ", "");
                for (int i = 0; i < t.numCols(); i += 1) {
                    if (t.getCol(i).getName().equals(partial)) {
                        String newName = m.group(1);
                        MyList newList = new MyList(newName, t.getCol(i));
                        newTable.addCol(newList);
                    }
                }
            } else {
                partial = partial.replaceAll(" ", "");
                newTable.addCol(t.getCol(partial));
            }
        }
        return newTable;
    }

    private static String newType(String s, MyValue a) {
        if (s.equals("int") && a.getType().equals("float")) {
            return "float";
        }
        return s;
    }

    private Table filterTableSingle(Table t, String s) throws StringException {
        if (s == null) {
            return t;
        } else {
            Table copy = new Table(t);
            Matcher m = COND.matcher(s);
            if (m.matches()) {
                String operand1 = m.group(1);
                String operator = m.group(2);
                String operand2 = m.group(3);
                if (t.getColNames().contains(operand1)) {
                    MyList col1 = copy.getCol(operand1);
                    if (t.getColNames().contains(operand2)) {
                        MyList col2 = copy.getCol(operand2);
                        for (int k = 0; k < col1.size();) {
                            MyValue val1 = col1.get(k);
                            MyValue val2 = col2.get(k);
                            if (!compareSimplify(val1, val2, operator)) {
                                copy.removeRow(k);
                            } else {
                                k += 1;
                            }
                        }
                    } else {
                        MyValue op2 = loadHelper(operand2);
                        if (op2.getType().equals("invalid")) {
                            throw new StringException("ERROR: Type invalid ");
                        }
                        for (int i = 0; i < col1.size(); ) {
                            MyValue temp = col1.get(i);
                            if (!compareSimplify(temp, op2, operator)) {
                                copy.removeRow(i);
                            } else {
                                i += 1;
                            }
                        }
                    }
                } else {
                    throw new StringException("ERROR: operand 1 not in columns");
                }
            }
            return copy;
        }
    }

    private static boolean compareSimplify(MyValue a, MyValue b, String operator) {
        if (a.isNoVal || b.isNoVal) {
            return false;
        }
        switch (operator) {
            case (">="):
                return a.compareTo(b) >= 0;
            case ("<="):
                return a.compareTo(b) <= 0;
            case ("=="):
                return a.compareTo(b) == 0;
            case ("!="):
                return a.compareTo(b) != 0;
            case ("<"):
                return a.compareTo(b) < 0;
            case (">"):
                return a.compareTo(b) > 0;
            default:
                return false;
        }
    }

    private Table filterTable(Table table, String conds) throws StringException {
        String[] condSplit = conds.split(" and ");
        Table copy = new Table(table);
        for (int i = 0; i < condSplit.length; i += 1) {
            Matcher m = COND.matcher(condSplit[i]);
            if (m.matches()) {
                String operand1 = m.group(1);
                String operator = m.group(2);
                String operand2 = m.group(3);
                ArrayList<String> colNames = copy.getColNames();
                if (!colNames.contains(operand1)) {
                    throw new StringException("ERROR 1");
                }
                MyList col1 = copy.getCol(operand1);

                if (!colNames.contains(operand2)) {
                    MyValue op2 = loadHelper(operand2);
                    if (op2.getType().equals("invalid")) {
                        throw new StringException("ERROR 2");
                    }
                    for (int k = 0; k < col1.size();) {
                        MyValue temp = col1.get(k);
                        if (!compareSimplify(temp, op2, operator)) {
                            copy.removeRow(k);
                        } else {
                            k += 1;
                        }
                    }
                } else {
                    MyList col2 = copy.getCol(operand2);
                    for (int k = 0; k < col1.size();) {
                        MyValue val1 = col1.get(k);
                        MyValue val2 = col2.get(k);
                        if (!compareSimplify(val1, val2, operator)) {
                            copy.removeRow(k);
                        } else {
                            k += 1;
                        }
                    }
                }
            } else {
                throw new StringException("ERROR 3");
            }
        }
        return copy;
    }

    private static MyValue simplifier(MyValue a, MyValue b, String operator)
            throws StringException {
        if (a.checkType(b)) {
            switch (operator) {
                case ("+"):
                    return a.add(b);
                case ("-"):
                    return a.sub(b);
                case ("/"):
                    return a.div(b);
                case ("*"):
                    return a.mul(b);
                default:
                    return null;
            }
        } else {
            throw new StringException("ERROR: Types not equal");
        }
    }

    private Table joinTables(String[] tables) {
        Table temp = new Table(allData.get(tables[0]));
        for (int i = 1; i < tables.length; i += 1) {
            Table t = allData.get(tables[i]);
            temp = temp.joinTable(t);
        }
        return temp;
    }

    private String insertRow(String expr) {
        Matcher m = INSERT_CLS.matcher(expr);
        if (m.matches()) {
            return insertRowInto(m.group(1), m.group(2));
        } else {
            return "ERROR: Malformed insert";
        }

    }

    private String[] removeAll(String s) {
        return s.replaceAll("^[,\\s]+", "").split("[,\\s]+");
    }

    private String insertRowInto(String name, String data) {
        if (!allData.containsKey(name)) {
            return "ERROR: Table does not exist";
        }
        String[] dataSplit = data.split(",");
        for (int i = 0; i < dataSplit.length; i += 1) {
            dataSplit[i] = dataSplit[i].trim();
        }
        Table currTable = allData.get(name);
        ArrayList<String> colTypes = currTable.getColTypes();
        ArrayList<MyValue> toBeAdded = new ArrayList<>();
        if (currTable.numCols() != dataSplit.length) {
            return "ERROR: Mismatched row size";
        }
        for (int i = 0; i < currTable.numCols(); i += 1) {
            MyValue temp;
            if (dataSplit[i].equals("NOVALUE")) {
                String t = currTable.getCol(i).getType();
                temp = newNoValGiven(t);
            } else {
                temp = loadHelper(dataSplit[i]);
            }
            if (temp.getType().equals(currTable.getCol(i).getType())) {
                toBeAdded.add(temp);
            } else if (temp.getType().equals("invalid")) {
                return "ERROR: Malformed data entry";
            } else {
                return "ERROR: Type value does not match column";
            }
        }
        currTable.addRow(toBeAdded);
        return "";
    }

    private MyValue newNoValGiven(String s) {
        MyValue temp;
        switch (s) {
            case "string":
                temp = new MyString();
                return temp;
            case "int":
                temp = new MyInt();
                return temp;
            case "float":
                temp = new MyFloat();
                return temp;
            default:
                return new MyInvalid();

        }
    }

    private MyValue newNanGiven(String s) {
        MyValue temp;
        switch (s) {
            case "string":
                temp = new MyString();
                temp.makeNan();
                return temp;
            case "int":
                temp = new MyInt();
                temp.makeNan();
                return temp;
            case "float":
                temp = new MyFloat();
                temp.makeNan();
                return temp;
            default:
                return new MyInvalid();
        }
    }

    private String createTable(String expr) {
        Matcher m;
        if ((m = CREATE_NEW.matcher(expr)).matches()) {
            return createNewTable(m.group(1), m.group(2).split(COMMA));
        } else if ((m = CREATE_SEL.matcher(expr)).matches()) {
            return createSelectedTable(m.group(1), m.group(2), m.group(3), m.group(4));
        } else {
            return "ERROR: Malformed create";
        }
    }

    private String createNewTable(String name, String[] cols) {
        if (allData.containsKey(name)) {
            return "ERROR: Table already exists: " + name;
        }
        Table newTable;
        ArrayList<String> allCol = new ArrayList<>();
        ArrayList<String> allType = new ArrayList<>();
        for (String pair : cols) {
            String[] temp = pair.split("\\s+");
            String colName = temp[0];
            String colType = temp[1];
            if (!TYPES.contains(colType)) {
                return "ERROR: Invalid Type: " + colType;
            }
            allCol.add(colName);
            allType.add(colType);
        }
        newTable = new Table(allCol, allType);
        allData.put(name, newTable);
        return "";
    }

    private String createSelectedTable(String name, String exprs, String tables, String conds) {
        try {
            Table newTable = select(exprs, tables, conds);
            allData.put(name, newTable);
            return "";
        } catch (StringException s) {
            return "ERROR: " + s.toString();
        }
    }

    private String loadTable(String name) {
        try {
            String currDir = System.getProperty("user.dir");
            File file = new File(currDir + "/" + name + ".tbl");
            Scanner in = new Scanner(file);
            Table newTable = new Table();
            if (!in.hasNextLine()) {
                return "ERROR: Empty file";
        }
            String firstLine = in.nextLine();
            String[] splitFirstLine = firstLine.split(",|\\ ");
            ArrayList<String> newSplit = new ArrayList<>();
            /* gets rid of spaces in array and converts to arraylist */
            for (int k = 0; k < splitFirstLine.length; k += 1) {
                if (!splitFirstLine[k].equals("")) {
                    newSplit.add(splitFirstLine[k]);
                }
            }
            if (newSplit.size() % 2 != 0) {
                return "ERROR: Malformed table";
            }
            /* manipulates first col */
            for (int i = 0; i < newSplit.size() - 1; i += 2) {
                String colName = newSplit.get(i);
                String colType = newSplit.get(i + 1);
                MyList newCol = new MyList(colName, colType);
                newTable.addCol(newCol);
            }
            /* actual data */
            while (in.hasNextLine()) {
                String copy = in.nextLine();
                if (copy.trim().isEmpty()) {
                    return "ERROR: loading empty line";
                }
                String[] nextLine = copy.split(",");
                ArrayList<MyValue> toBeAdded = new ArrayList<>();
                if (nextLine.length != newTable.numCols()) {
                    return "ERROR: Inserting to big row";
                }
                for (int i = 0; i < newTable.numCols(); i += 1) {
                    String eval = nextLine[i].trim();
                    MyValue temp;
                    if (eval.equals("NOVALUE")) {
                        String t = newTable.getCol(i).getType();
                        temp = newNoValGiven(t);
                    } else if (eval.equals("NaN")) {
                        String t = newTable.getCol(i).getType();
                        temp = newNanGiven(t);
                    } else {
                        temp = loadHelper(eval);
                    }
                    if (newTable.getCol(i).getType().equals(temp.getType())) {
                        toBeAdded.add(temp);
                    } else {
                        return "ERROR: Malformed table (type)";
                    }
                }
                newTable.addRow(toBeAdded);
            }
            allData.put(name, newTable);
        } catch (FileNotFoundException e) {
            return "ERROR: FILE NOT FOUND";
        }
        return "";
    }

    private MyValue loadHelper(String s) {
        if (s.startsWith("'") && s.endsWith("'")) {
            return new MyString(s);
        } else if (s.contains(".")) {
            return new MyFloat(s);
        } else if (s.matches("^(-?)\\d+$")) {
            return new MyInt(s);
        } else if (s.matches("\\w+")) {
            return new MyColName(s);
        } else {
            return new MyInvalid();
        }
    }

    private String printTable(String name) {
        if (allData.get(name) == null) {
            return "ERROR: no such table";
        }
        return allData.get(name).toString();
    }
}
