package edu.uob;

import java.io.File;
import java.io.OptionalDataException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static edu.uob.Condition.parseCondition;

public class QueryParser {
    //a method that execute each commands
    public String executeQuery(ArrayList<String> tokens, DBServer server) {
        String lastToken = tokens.get(tokens.size() - 1);
        if (!lastToken.equals(";")) {return "[ERROR] Invalid query syntax";}
        ArrayList<String> conditionTokens = new ArrayList<>();
        for (String token : tokens) {
            if (token.equalsIgnoreCase("WHERE")) {
                int indexOfWhere = tokens.indexOf(token);
                for(int i = indexOfWhere + 1; i < tokens.size()-1; i++) {
                    conditionTokens.add(tokens.get(i));
                }
                //conditionTokens = (ArrayList<String>) tokens.subList(tokens.indexOf("WHERE"), tokens.size()-1);
                System.out.println(conditionTokens);
            }
        }
        String command = tokens.get(0).toUpperCase();
        switch(command) {
            case "SELECT":
                return selectTable(tokens,server,conditionTokens);
            case "INSERT":
                return insertIntoTable(tokens,server);
            case "CREATE":
                return createObject(tokens,server);
            case "USE":
                return useDatabase(tokens,server);
            case "ALTER":
                return alterTable(tokens,server);
            case "DROP":
                return dropObject(tokens,server);
            case "JOIN":
                return joinObject(tokens,server);
            case "UPDATE":
                return updateObject(tokens,server,conditionTokens);
            case "DELETE":
                return deleteObject(tokens,server,conditionTokens);
            default:
                return "[ERROR] Unsupported command: " + command;
        }
    }

    public boolean executeCondition(ArrayList<String> conditionTokens, Row row) {
        Condition currentConditions = parseCondition(conditionTokens);
        return currentConditions.checkCondition(row);
    }

    // CREATE TABLE marks (name, mark, pass);
    // CREATE DATABASE markbook;
    public String createObject(ArrayList<String> tokens, DBServer server) {
        String object = tokens.get(1).toUpperCase();
        switch(object) {
            case "TABLE":
                return createTable(tokens, server);
            case "DATABASE":
                return createDatabase(tokens, server);
            default:
                return "[ERROR] Unsupported command: CREATE " + object;
        }
    }
    //CREATE DATABASE markbook;
    public String createDatabase(ArrayList<String> tokens, DBServer server) {
        String dbName = tokens.get(2).toLowerCase();
        //create a File object representing the directory
        Database newDirectory = new Database(dbName, server.getStorageFolderPath());
        //if(!newDirectory.exists()) {return "[ERROR] Directory does not exist: " + newDirectory.getAbsolutePath();}
        return "[OK]" + newDirectory.getPath();
    }
    //CREATE TABLE marks (name, mark, pass);
    public String createTable(ArrayList<String> tokens, DBServer server) {
        String tableName = tokens.get(2).toLowerCase();
        Table newTable = new Table(tableName, server.getCurrentDatabase().getPath());

        for(int i = 4; i<tokens.size(); i+=2) {
            if(tokens.get(i).equals(";")){break;}
            newTable.addColumn(tokens.get(i), "");
        }
        newTable.saveTable(); //save the table after create (filewriter)
        server.getCurrentDatabase().addTable(newTable); // add the table to the current database
        return "[OK] Created table " + tableName;
    }
    //SELECT * FROM marks;
 //SELECT id,mark FROM marks WHERE pass == FALSE;
    public String selectTable(ArrayList<String> tokens, DBServer server, ArrayList<String> conditionTokens) {
        if (server.getCurrentDatabase() == null) return "[ERROR] no database selected";
        int indexOfFrom = 0;
        int indexOfSelect = 0;
        for(String token : tokens) {
            if(token.equalsIgnoreCase("FROM")) {
                indexOfFrom = tokens.indexOf(token);
            }
            if(token.equalsIgnoreCase("SELECT")) {
                indexOfSelect = tokens.indexOf(token);
            }
        }
        //int indexOfFrom = tokens.indexOf("FROM".toUpperCase());
        boolean hasConditions = !conditionTokens.isEmpty();
        String tableName = tokens.get(indexOfFrom + 1);
        Table selectedTable = server.getCurrentDatabase().getTables(tableName);
        //error detection: if table name doesn't exist
        if (selectedTable == null) {
            return "[ERROR] Table " + tableName + " not found";
        }
        StringBuilder rowString = new StringBuilder();

        ArrayList<String> selectedColumns = new ArrayList<>();
        for (int j = indexOfSelect + 1; j < indexOfFrom; j += 1) {
            String token = tokens.get(j);
            if (!token.equals(",") && !token.isEmpty()) {
                selectedColumns.add(token);
            }
        }
        //        Conditions cond PARSE
        //rowString.append(Table.ID_COL + "\t");
        if (selectedColumns.size() == 1 && selectedColumns.get(0).equals("*")) {
            rowString.append(Table.ID_COL + "\t");
            rowString.append(selectedTable.columnNamesAsString()).append("\n"); //headers for *
            for (int iRow = 0; iRow < selectedTable.getRows().size(); iRow++) {
//                if (cond.check(row))
                if(hasConditions){
                    if(executeCondition(conditionTokens, selectedTable.getRows().get(iRow))){
                        rowString.append(selectedTable.getRows().get(iRow).toString(true)).append("\n");
                    }
                } else {
                    rowString.append(selectedTable.getRows().get(iRow).toString(true)).append("\n");
                }
            }
        } else {
//            boolean requiresID = false;
            for (String selectedColumn : selectedColumns) {
//                if(selectedColumn.equalsIgnoreCase("id")){
//                    requiresID = true;
//                }
                rowString.append(selectedColumn).append("\t");
            }
            rowString.append("\n");
            for (int iRow = 0; iRow < selectedTable.getRows().size(); iRow++) {
//                if (cond.check(row))
                if(hasConditions){
                    if(executeCondition(conditionTokens, selectedTable.getRows().get(iRow))) {
                        //System.out.println("rows that meet the conditions: " + selectedTable.getRows().get(iRow).toString() + "\n");
                        rowString.append(selectedTable.getRows().get(iRow).toString(selectedColumns)).append("\n");
                        //System.out.println("selected columns: " + selectedColumns + "row " + selectedTable.getRows().get(iRow).toString(selectedColumns));
                    }
                } else {
                    rowString.append(selectedTable.getRows().get(iRow).toString(selectedColumns)).append("\n");
                }

            }
        }
        return "[OK]" + "\n" + rowString;
    }
// INSERT INTO marks VALUES ('Chris', 20, FALSE);
    private String insertIntoTable(ArrayList<String> tokens, DBServer server) {
        if(!tokens.get(1).equalsIgnoreCase("INTO") || !tokens.get(3).equalsIgnoreCase("VALUES")) {
            return "[ERROR} invalid command";
        }
        if (server.getCurrentDatabase() == null) return "[ERROR] no database selected";
        String tableName = tokens.get(2).toLowerCase();
        Table table = server.getCurrentDatabase().getTables(tableName);
        StringBuilder rowString = new StringBuilder();
        int startIndex = tokens.indexOf("(") + 1;
        int endIndex = tokens.indexOf(")");
        for (int i = startIndex; i < endIndex; i += 1) {
            String token = tokens.get(i);
            if(token.startsWith("'") && token.endsWith("'")) {token = token.substring(1, token.length()-1);}
            if (token.equals(",")) {continue;}
            rowString.append(token).append("\t");
        }
        rowString.append("\n");
        String row = rowString.toString();
        System.out.println("Row string: " + row);
        Row newRow = Row.fromString(row,table.getColumnNames(), false);
        if(newRow == null) {
            return "[ERROR] Inserted into table " + tableName + " failed";
        }
        table.addRow(newRow);
        table.saveTable();
        return "[OK]";
    }

    // "DROP " "DATABASE " [DatabaseName] | "DROP " "TABLE " [TableName]
    public String dropObject(ArrayList<String> tokens, DBServer server) {
        String object = tokens.get(1).toUpperCase();
        switch(object) {
            case "TABLE":
                return dropTable(tokens,server);
            case "DATABASE":
                return dropDatabase(tokens,server);
            default:
                return "[ERROR] Unsupported command: DROP " + object;
        }
    }
    public String dropTable(ArrayList<String> tokens, DBServer server) {
        if (server.getCurrentDatabase() == null) return "[ERROR] no database selected";
        String tableName = tokens.get(2).toLowerCase();
        File deleteFile = new File(server.getCurrentDatabase().getTables(tableName).getTablePath());
        if (!deleteFile.exists()) {
            return "[ERROR] Table " + tableName + " not found";
        }
        if (deleteFile.delete()) {
            return "[OK] Table " + tableName + " deleted";
        } else {
            return "[ERROR] Table " + tableName + " could not be deleted";
        }
    }
    public String dropDatabase(ArrayList<String> tokens, DBServer server) {
        if (server.getCurrentDatabase() == null) return "[ERROR] no database selected";
        String databaseName = tokens.get(2).toLowerCase();
        File deleteDatabaseFile = new File(server.getStorageFolderPath() + File.separator + databaseName);
        if (!deleteDatabaseFile.exists()) { return "[ERROR] Database " + databaseName + " not found"; }
        if (deleteDatabaseFile.delete()) {
            return "[OK] Database " + databaseName + " deleted";
        } else {
            return "[ERROR] Database " + databaseName + " could not be deleted";
        }
    }
   // "ALTER " "TABLE " [TableName] " " <AlterationType> " " [AttributeName]
    //<AlterationType>  ::=  "ADD" | "DROP"
    public String alterTable(ArrayList<String> tokens, DBServer server) {
        if(server.getCurrentDatabase() == null) return "[ERROR] no database selected";
        if(!tokens.get(1).equalsIgnoreCase("TABLE")){
            return "[ERROR] Invalid command" + tokens.get(1).toUpperCase();
        }
        String object = tokens.get(3).toUpperCase();
        switch(object) {
            case "ADD":
                return alterTypeAdd(tokens,server);
            case "DROP":
                return alterTypeDrop(tokens,server);
            default:
                return "[ERROR] Unsupported command: DROP " + object;
        }

    }

    public String alterTypeAdd(ArrayList<String> tokens, DBServer server) {
        String tableName = tokens.get(2).toLowerCase();
        String columnName = tokens.get(4).toLowerCase();
        Table table = server.getCurrentDatabase().getTables(tableName);
        table.addColumn(columnName,"");
        table.saveTable();
        return "[OK] " + columnName + " added";

    }
    public String alterTypeDrop(ArrayList<String> tokens, DBServer server) {
        String tableName = tokens.get(2).toLowerCase();
        String columnName = tokens.get(4).toLowerCase();
        Table table = server.getCurrentDatabase().getTables(tableName);
        if(!table.getColumnNames().contains(columnName)){
            return "[ERROR] Column " + columnName + " not found";
        }
        System.out.println("reached here");
        table.removeColumn(columnName);
        table.saveTable();
        return "[OK] " + columnName + " removed";
    }

//    public String deleteDatabase(String query, DBServer server) {}

    public String useDatabase(ArrayList<String> tokens, DBServer server) {
        if(tokens.size() < 2) return "[ERROR] Invalid database"; //check whether there is a table name in the query
        String dbName = tokens.get(1).toLowerCase(); //find the name of the file that you try to find in the database
        File dbFolder = new File(server.getStorageFolderPath() + File.separator + dbName);
        if(!dbFolder.exists()) return "[ERROR] Database does not exist";
        server.setCurrentDatabase(new Database(dbName,server.getStorageFolderPath()));
        return "[OK] Database in use";
    }
//"DELETE " "FROM " [TableName] " WHERE " <Condition>
    public String deleteObject(ArrayList<String> tokens, DBServer server, ArrayList<String> conditionTokens) {
        if(!tokens.get(1).equalsIgnoreCase("FROM") || !tokens.get(3).equalsIgnoreCase("WHERE")) return "[ERROR] Invalid command";
        String tableName = tokens.get(2).toLowerCase();
        Table table = server.getCurrentDatabase().getTables(tableName);
        for(Row row : table.getRows()) {
            if(executeCondition(conditionTokens, row)) {
                table.removeRow(row);
            }
        }
        table.saveTable();
        return "[OK] deleted from Table " + tableName;
    }

    //"UPDATE " [TableName] " SET " <NameValueList> " WHERE " <Condition>
    // UPDATE people SET age = 42 WHERE name == 'Bob';
    //<NameValueList>::=  <NameValuePair> | <NameValuePair> "," <NameValueList>
    //<NameValuePair>::=  [AttributeName] "=" [Value]
    //UPDATE marks SET mark = 40, PASS = TRUE, rank > 5 WHERE age < 40;
    public String updateObject(ArrayList<String> tokens, DBServer server, ArrayList<String> conditionTokens) {
        String tableName = tokens.get(1).toLowerCase();
        Table table = server.getCurrentDatabase().getTables(tableName);
        //int indexOfWhere = tokens.indexOf("WHERE");
        int indexOfSet = 0;
        int indexOfWhere = 0;
        // the arraylist of the name value list

        ArrayList<String> wholeList = new ArrayList<>();
        for(String token : tokens){
            if (token.equalsIgnoreCase("SET")) {
                indexOfSet = tokens.indexOf(token);
            }
            if (token.equalsIgnoreCase("WHERE")) {
                indexOfWhere = tokens.indexOf(token);
            }
        }
        for(int i = indexOfSet + 1; i < indexOfWhere; i++){
            wholeList.add(tokens.get(i));
        }

        ArrayList<ArrayList> nameValueList = new ArrayList<>(); //an arraylist that stores all the subLists

        int start = 0;
        boolean hasMultipleValues = false;
        for(int i = start; i < wholeList.size(); i++) {
            if(wholeList.get(i).equals(",")){
                ArrayList<String> subList = new ArrayList<>(wholeList.subList(start,i));
                nameValueList.add(subList);
                start = i + 1;
                hasMultipleValues = true;
            }
        }
        if(!hasMultipleValues){nameValueList.add(wholeList);}
        //int numOfPairs = nameValueList.size();
        System.out.println("reached here! nameValueList: " + nameValueList);
        for(ArrayList pair : nameValueList){   //error handling!!
            String attributeName = pair.get(0).toString().toLowerCase();
            String value = pair.get(2).toString();
            if(value.startsWith("'") && value.endsWith("'")){
                value = value.substring(1,value.length()-1);
            }
            System.out.println("reached here! compared value: " + value);
            for(int iRow = 0; iRow < table.getRows().size(); iRow++){
                if(executeCondition(conditionTokens,table.getRows().get(iRow))){
                    table.getRows().get(iRow).setValue(attributeName, value);
                }
            }
        }
        table.saveTable();
        return "[OK] updated in Table " + tableName;
    }//<Join> ::=  "JOIN " [TableName] " AND " [TableName] " ON " [AttributeName] " AND " [AttributeName]
    //JOIN coursework AND marks ON submission AND id;
    //If submission in table coursework equals to id in table marks, join the two tables and both of their columns and the rows that match
    public String joinObject(ArrayList<String> tokens, DBServer server) {
        if(server.getCurrentDatabase() == null) return "[ERROR] no database selected";
        if(!tokens.get(2).equalsIgnoreCase("AND") || !tokens.get(4).equalsIgnoreCase("ON")
         || !tokens.get(6).equalsIgnoreCase("AND")) {
            return "[ERROR] Invalid command";
        }
        String table1Name = tokens.get(1).toLowerCase();
        String table2Name = tokens.get(3).toLowerCase();
        Table table1 = server.getCurrentDatabase().getTables(table1Name);
        Table table2 = server.getCurrentDatabase().getTables(table2Name);
        String attributeName1 = tokens.get(5).toLowerCase();
        String attributeName2 = tokens.get(7).toLowerCase();
        StringBuilder rowString = new StringBuilder();

        rowString.append(Table.ID_COL).append("\t");
        for(int i = 0; i < table1.getColumns().size(); i++){
            if(!Objects.equals(table1.getColumns().get(i).getColumnName(), attributeName1)) {
                rowString.append(table1.getColumns().get(i).getColumnName()).append("\t");
            }
        }
        for(int i = 0; i < table2.getColumns().size(); i++){
            if(!Objects.equals(table2.getColumns().get(i).getColumnName(), attributeName2)) {
                rowString.append(table2.getColumns().get(i).getColumnName()).append("\t");
            }
        }
        rowString.append("\n");

        int newId = 0;
        for(int iRow = 0; iRow < table1.getRows().size(); iRow++){
            for(int jRow = 0; jRow < table2.getRows().size(); jRow++){
                String rowItem1;
                String rowItem2;
                if(attributeName1.equalsIgnoreCase(Table.ID_COL)){
                    rowItem1 = String.valueOf(table1.getRows().get(iRow).getPrimaryKey());
                } else{
                    rowItem1 = String.valueOf(table1.getRows().get(iRow).getValue(attributeName1));
                }
                System.out.println("reached here! rowItem1: " + rowItem1);
                if(attributeName2.equalsIgnoreCase(Table.ID_COL)){
                    rowItem2 = String.valueOf(table2.getRows().get(jRow).getPrimaryKey());
                } else{
                    rowItem2 = String.valueOf(table2.getRows().get(jRow).getValue(attributeName2));
                }
                System.out.println("reached here! rowItem2: " + rowItem2);
                if(rowItem1.equals(rowItem2)){
                    newId++;
                    rowString.append(newId).append("\t");
                    for(String colName: table1.getColumnNames()){
                        String getValue1 = String.valueOf(table1.getRows().get(iRow).getValue(colName));
                        if(!getValue1.equalsIgnoreCase(rowItem1)){
                            rowString.append(getValue1).append("\t");
                        }
                    }
                    for(String colName: table2.getColumnNames()){
                        String getValue2 = String.valueOf(table2.getRows().get(jRow).getValue(colName));
                        if(!getValue2.equalsIgnoreCase(rowItem2)){
                            rowString.append(getValue2).append("\t");
                        }
                    }
                    rowString.append("\n");
                }
            }
        }
        return "[OK]" + "\n" + rowString;
    }
}
