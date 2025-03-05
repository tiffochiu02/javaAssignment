package edu.uob;

import java.io.File;
import java.io.OptionalDataException;
import java.util.ArrayList;

public class QueryParser {
    //a method that execute each commands
    public String executeQuery(ArrayList<String> tokens, DBServer server) {
        String lastToken = tokens.get(tokens.size() - 1);
        if (!lastToken.equals(";")) {return "[ERROR] Invalid query syntax";}
        String command = tokens.get(0).toUpperCase();
        switch(command) {
            case "SELECT":
                return selectTable(tokens,server);
            case "INSERT":
                return insertIntoTable(tokens,server);
            case "CREATE":
                return createObject(tokens,server);
//            case "DELETE":
//                return delete(tokens,server);
            case "USE":
                return useDatabase(tokens,server);
            case "ALTER":
                return alterTable(tokens,server);
            case "DROP":
                return dropObject(tokens,server);
            default:
                return "[ERROR] Unsupported command: " + command;
        }

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
    public String selectTable(ArrayList<String> tokens, DBServer server) {
        if (server.getCurrentDatabase() == null) return "[ERROR] no database selected";
        int indexOfSelect = tokens.indexOf("SELECT");
        int indexOfFrom = tokens.indexOf("FROM");
        // int indexOfWhere = tokens.indexOf("WHERE");
        // String selectItem = tokens.get(indexOfSelect + 1);
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
        rowString.append(Table.ID_COL + "\t");
        if (selectedColumns.size() == 1 && selectedColumns.get(0).equals("*")) {
            rowString.append(selectedTable.columnNamesAsString()).append("\n"); //headers for *
            for (int iRow = 0; iRow < selectedTable.getRows().size(); iRow++) {
//                if (cond.check(row))
                rowString.append(selectedTable.getRows().get(iRow).toString()).append("\n");
            }
        } else {
            for (String selectedColumn : selectedColumns) {
                rowString.append(selectedColumn).append("\t");
            }
            rowString.append("\n");
            for (int iRow = 0; iRow < selectedTable.getRows().size(); iRow++) {
//                if (cond.check(row))
                rowString.append(selectedTable.getRows().get(iRow).toString(selectedColumns)).append("\n");
            }
        }
        return "[OK]" + "\n" + rowString;
    }

// INSERT INTO marks VALUES ('Chris', 20, FALSE);
    private String insertIntoTable(ArrayList<String> tokens, DBServer server) {
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
            return "[ERROR] Cannot drop" + tokens.get(1).toUpperCase();
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
}
