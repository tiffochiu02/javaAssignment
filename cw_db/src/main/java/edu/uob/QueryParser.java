package edu.uob;

import org.w3c.dom.Node;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static edu.uob.Condition.parseCondition;

public class QueryParser {
    //a method that execute each commands
    public String executeQuery(ArrayList<String> tokens, DBServer server) throws IOException, NoSuchElementException {
        String lastToken = tokens.get(tokens.size() - 1);
        if (!lastToken.equals(";")) {
            throw new IOException();
        }
        ArrayList<String> conditionTokens = new ArrayList<>();
        for (String token : tokens) {
            if (token.equalsIgnoreCase("WHERE")) {
                int indexOfWhere = tokens.indexOf(token);
                for(int i = indexOfWhere + 1; i < tokens.size()-1; i++) {
                    conditionTokens.add(tokens.get(i));
                }
                if (conditionTokens.isEmpty()) {
                    throw new IOException();
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

    public boolean executeCondition(ArrayList<String> conditionTokens, Row row) throws IOException, NoSuchElementException {
        Condition currentConditions = parseCondition(conditionTokens);
        return currentConditions.checkCondition(row);
    }

    // CREATE TABLE marks (name, mark, pass);
    // CREATE DATABASE markbook;
    public String createObject(ArrayList<String> tokens, DBServer server) throws IOException {
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
    public String createDatabase(ArrayList<String> tokens, DBServer server) throws IOException {

        String dbName = tokens.get(2).toLowerCase();
        File allDatabases = new File(server.getStorageFolderPath());
        for(File file : Objects.requireNonNull(allDatabases.listFiles())) {
            if(file.getName().equals(dbName)) {throw new IOException();}
        }
        //create a File object representing the directory
        Database newDirectory = new Database(dbName, server.getStorageFolderPath());
        //if(!newDirectory.exists()) {return "[ERROR] Directory does not exist: " + newDirectory.getAbsolutePath();}
        return "[OK]" + newDirectory.getPath();
    }
    //CREATE TABLE marks (name, mark, pass);
    public String createTable(ArrayList<String> tokens, DBServer server) throws IOException {
        if (server.getCurrentDatabase() == null) return "[ERROR] no database selected";
        ArrayList<String> columnStrings = new ArrayList<>();
        for (int i = 3; i < tokens.size() - 1; i++) {
            columnStrings.add(tokens.get(i));
        }
        System.out.println("columnStrings: " + columnStrings);
        int numOfTokens = columnStrings.size();
        if(numOfTokens == 1 || numOfTokens == 2) {throw new IOException();}
        else if(numOfTokens > 2){
            if(!columnStrings.get(0).equals("(") || !columnStrings.get(columnStrings.size()-1).equals(")")){
                throw new IOException();
            }
            for(int i = 5; i < tokens.size(); i+=2) {
                if(tokens.get(i).equals(")")) {break;}
                if(!tokens.get(i).equals(",")) {
                    throw new IOException();
                }
            }
        }
        String tableName = tokens.get(2).toLowerCase();
        File currentDirectory = new File(server.getCurrentDatabase().getPath());
        for(File file : Objects.requireNonNull(currentDirectory.listFiles())) {
            if(file.getName().equals(tableName + Table.suffix)) {
                System.out.println("Retrieved name: " + file.getName() + " from command: " + tableName);
                throw new IOException("[ERROR] Duplicate table name: " + tableName);
            }
        }
        Table newTable = new Table(tableName, server.getCurrentDatabase().getPath());

        if(numOfTokens > 0) {
            for (int i = 4; i < tokens.size(); i += 2) {
                if (tokens.get(i).equals(";")) {
                    break;
                }
                if (tokens.get(i).equals(",")||!NodeCheck.isAttributeName(tokens.get(i))) {
                    throw new IOException();
                }
                for (String ColumnName : newTable.getColumnNames()) {
                    if (tokens.get(i).equals(ColumnName)) {
                        throw new IOException();
                    }
                }
                newTable.addColumn(tokens.get(i), "");
            }
        }
        newTable.saveTable(); //save the table after create (filewriter)
        server.getCurrentDatabase().addTable(newTable); // add the table to the current database
        return "[OK] Created table " + tableName;
    }

    //SELECT * FROM marks;
    //SELECT id,mark FROM marks WHERE pass == FALSE;
    public String selectTable (ArrayList<String> tokens, DBServer server, ArrayList<String> conditionTokens) throws IOException, NoSuchElementException {
        if (server.getCurrentDatabase() == null) return "[ERROR] no database selected";
        int indexOfFrom = 0;
        int indexOfSelect = 0;
        for(String token : tokens) {
            if(token.equalsIgnoreCase("FROM")) {
                indexOfFrom = tokens.indexOf(token);
            }
        }
        boolean hasConditions = !conditionTokens.isEmpty();
        if(hasConditions) {
            if(!tokens.get(indexOfFrom + 2).equalsIgnoreCase("WHERE")){
                throw new IOException();
            }
        } else{
            System.out.println("Hereeeee No conditions detected");
            if(tokens.size() != indexOfFrom + 3) throw new IOException();
        }
        String tableName = tokens.get(indexOfFrom + 1).toLowerCase();
        System.out.println("hereeeee table name: " + tableName);
        Table selectedTable = server.getCurrentDatabase().getTables(tableName);
        //error detection: if table name doesn't exist
        if (selectedTable == null) {
            return "[ERROR] Table " + tableName + " not found";
        }
        StringBuilder rowString = new StringBuilder();

        ArrayList<String> selectedColumns = new ArrayList<>();
//        for (int j = indexOfSelect + 1; j < indexOfFrom; j += 1) {
//            String token = tokens.get(j);
//            if (!token.equals(",") && !token.isEmpty()) {
//                selectedColumns.add(token);
//            }
//        }
        for (int j = indexOfSelect + 1; j < indexOfFrom; j += 2) {
            String token = tokens.get(j);
            if(token.equalsIgnoreCase(tokens.get(indexOfFrom + 1))) { break; }
            if (token.equals(",")) {
                throw new IOException();
            }
            selectedColumns.add(token);
        }
        System.out.println("selectedColumns: " + selectedColumns);
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
            boolean allSelectedColExist = false;
            String notExistingColumn = "";
            for(String column : selectedColumns) { //from command
                for(String retrievedColName : selectedTable.getColumnNames()) {
                    if(retrievedColName.equalsIgnoreCase(column) || column.equalsIgnoreCase(Table.ID_COL)) {
                        allSelectedColExist = true;
                        notExistingColumn = column;
                        break;
                    }
                }
            }
            System.out.println("selected table's column names: " + selectedTable.getColumnNames());
            if(!allSelectedColExist) {return "[ERROR] Column " + notExistingColumn + " not found";}
            for (String selectedColumn : selectedColumns) {
                rowString.append(selectedColumn).append("\t");
            }
            rowString.append("\n");
            for (int iRow = 0; iRow < selectedTable.getRows().size(); iRow++) {
//                if (cond.check(row))
                if(hasConditions){
                    if(executeCondition(conditionTokens, selectedTable.getRows().get(iRow))) {
                        System.out.println("rows that meet the conditions: " + selectedTable.getRows().get(iRow).toString(true) + "\n");
                        rowString.append(selectedTable.getRows().get(iRow).toString(selectedColumns)).append("\n");
                    }
                } else {
                    rowString.append(selectedTable.getRows().get(iRow).toString(selectedColumns)).append("\n");
                }
            }
        }
        return "[OK]" + "\n" + rowString;
    }
    // INSERT INTO marks VALUES ('Chris', 20, FALSE);
    private String insertIntoTable(ArrayList<String> tokens, DBServer server) throws IOException, NoSuchElementException {
        if(!tokens.get(1).equalsIgnoreCase("INTO") || !tokens.get(3).equalsIgnoreCase("VALUES")) {
            throw new IOException();
        }
        if (server.getCurrentDatabase() == null) return "[ERROR] no database selected";
        if(!tokens.get(4).equals("(") || !tokens.get(tokens.indexOf(";")-1).equals(")")) {
            throw new IOException();
        }

        String tableName = tokens.get(2).toLowerCase();
        Table table = server.getCurrentDatabase().getTables(tableName);
        //error detection: if table name doesn't exist
        if (table == null) {
            return "[ERROR] Table " + tableName + " not found";
        }

        StringBuilder rowString = new StringBuilder();
        int startIndex = tokens.indexOf("(") + 1;
        int endIndex = tokens.indexOf(")");
        if(endIndex == startIndex) {throw new IOException();}
        ArrayList<String> columnStrings = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i += 1) {
            String token = tokens.get(i);
            if (token.equals(",")) {continue;}
            if(!NodeCheck.isValue(token)){throw new IOException();}
            if(token.startsWith("'") && token.endsWith("'")) {
                token = token.substring(1, token.length()-1);
                if(!NodeCheck.isStringLiteral(token)) {throw new IOException();}
            }
            rowString.append(token).append("\t");
            columnStrings.add(token);
        }
        if(columnStrings.size() != table.getColumnNames().size()) {
            throw new IOException();
        }
        rowString.append("\n");
        String row = rowString.toString();
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
        Path deleteDatabasePath = deleteDatabaseFile.toPath();
        for (File file: (Objects.requireNonNull(deleteDatabasePath.toFile().listFiles()))) {
            file.delete();
        }
        deleteDatabaseFile.delete();
        if (deleteDatabaseFile.exists()) {
            return "[ERROR] Database " + databaseName + " not found";
        }
        return "[OK] Database " + databaseName + " deleted";
    }
    // "ALTER " "TABLE " [TableName] " " <AlterationType> " " [AttributeName]
    //<AlterationType>  ::=  "ADD" | "DROP"
    public String alterTable(ArrayList<String> tokens, DBServer server) throws IOException{
        if(server.getCurrentDatabase() == null) return "[ERROR] no database selected";
        if(!tokens.get(1).equalsIgnoreCase("TABLE")) throw new IOException();
        if(tokens.size()!=6) throw new IOException();
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

    public String alterTypeAdd(ArrayList<String> tokens, DBServer server) throws IOException {
        String tableName = tokens.get(2).toLowerCase();
        String columnName = tokens.get(4);
        if(columnName.equalsIgnoreCase(Table.ID_COL)) throw new NoSuchElementException();
        Table table = server.getCurrentDatabase().getTables(tableName);
        for(String colName: table.getColumnNames()) {
            if(colName.equalsIgnoreCase(columnName)) throw new IOException();
        }
        table.addColumn(columnName,"");
        table.saveTable();
        return "[OK] " + columnName + " added";

    }
    public String alterTypeDrop(ArrayList<String> tokens, DBServer server) throws IOException, NoSuchElementException{ //try drop id
        String tableName = tokens.get(2).toLowerCase();
        String columnName = tokens.get(4);
        Table table = server.getCurrentDatabase().getTables(tableName);
        boolean found = false;
        for(String colName: table.getColumnNames()) {
            if(colName.equalsIgnoreCase(columnName)) {
                found = true;
                columnName = colName;
            }
        }
        if(!found) throw new IOException();
        System.out.println("reached here");
        table.removeColumn(columnName);
        table.saveTable();
        return "[OK] " + columnName + " removed";
    }


    public String useDatabase(ArrayList<String> tokens, DBServer server) {
        if(tokens.size() < 2) return "[ERROR] Invalid database"; //check whether there is a table name in the query
        String dbName = tokens.get(1).toLowerCase(); //find the name of the file that you try to find in the database
        File dbFolder = new File(server.getStorageFolderPath() + File.separator + dbName);
        if(!dbFolder.exists()) return "[ERROR] Database does not exist";
        server.setCurrentDatabase(new Database(dbName,server.getStorageFolderPath()));
        return "[OK] Database in use";
    }
    //"DELETE " "FROM " [TableName] " WHERE " <Condition>
    public String deleteObject(ArrayList<String> tokens, DBServer server, ArrayList<String> conditionTokens) throws IOException  {
        if(!tokens.get(1).equalsIgnoreCase("FROM") || !tokens.get(3).equalsIgnoreCase("WHERE")) return "[ERROR] Invalid command";
        String tableName = tokens.get(2).toLowerCase();
        Table table = server.getCurrentDatabase().getTables(tableName);
        Iterator<Row> iterator = table.getRows().iterator();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            if (executeCondition(conditionTokens, row)) {
                iterator.remove(); // safe removal
            }
        }
        int id = 1;
        for(Row row : table.getRows()){
            row.setPrimaryKey(id);
            id++;
        }
        table.saveTable();
        return "[OK] deleted from Table " + tableName;
    }

    //"UPDATE " [TableName] " SET " <NameValueList> " WHERE " <Condition>
    // UPDATE people SET age = 42 WHERE name == 'Bob';
    //<NameValueList>::=  <NameValuePair> | <NameValuePair> "," <NameValueList>
    //<NameValuePair>::=  [AttributeName] "=" [Value]
    //UPDATE marks SET mark = 40, PASS = TRUE, rank > 5 WHERE age < 40;
    public String updateObject(ArrayList<String> tokens, DBServer server, ArrayList<String> conditionTokens) throws IOException {
        String tableName = tokens.get(1).toLowerCase();
        Table table = server.getCurrentDatabase().getTables(tableName);
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
        String attributeName1 = tokens.get(5);
        String attributeName2 = tokens.get(7);
        StringBuilder rowString = new StringBuilder();

        rowString.append(Table.ID_COL).append("\t");
        for(int i = 0; i < table1.getColumns().size(); i++){
            if(!table1.getColumns().get(i).getColumnName().equalsIgnoreCase(attributeName1)) {
                rowString.append(table1Name).append(".").append(table1.getColumns().get(i).getColumnName()).append("\t");
            }
        }
        for(int i = 0; i < table2.getColumns().size(); i++){
            if(!table2.getColumns().get(i).getColumnName().equalsIgnoreCase(attributeName2)) {
                rowString.append(table2Name).append(".").append(table2.getColumns().get(i).getColumnName()).append("\t");
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
                    rowItem1 = String.valueOf(table1.getRows().get(iRow).getValue(attributeName1)); //here!
                }
                if(attributeName2.equalsIgnoreCase(Table.ID_COL)){
                    rowItem2 = String.valueOf(table2.getRows().get(jRow).getPrimaryKey());
                } else{
                    rowItem2 = String.valueOf(table2.getRows().get(jRow).getValue(attributeName2));
                }
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
