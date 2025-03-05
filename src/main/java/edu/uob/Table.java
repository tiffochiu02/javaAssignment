package edu.uob;

import java.io.*;
import java.util.*;

public class Table{
    final public static String ID_COL = "id";
    final private static String suffix = ".tab";
    private String tableName;
    private ArrayList<Row> rows;
    private ArrayList<Column> columns;
    private String tablePath;
    //private ArrayList<String> types;
    //private int numOfColumns;
    //private ArrayList<String> columnNames;

    public Table(String tableName, String basePath){
        this.tableName = tableName;
        this.rows = new ArrayList<>();
        this.columns = new ArrayList<>();
        this.tablePath = basePath + File.separator + tableName + suffix;
    }

    public String getTableName(){return tableName;}
    public String getTablePath(){return tablePath;}

    public ArrayList<String> getColumnNames() {
        ArrayList<String> names = new ArrayList<>();
        for (Column col : columns) {
            names.add(col.getColumnName());
        }
        return names;
    }
//    public void setColumnNames(ArrayList<String> columnNames) {
//        this.columnNames = columnNames;
//    }
    public String columnNamesAsString(){
        String columnsString = "";
        for (Column col : columns) {
            columnsString += col.getColumnName() + "\t";
        }
        return columnsString;
    }

    public long generateRowIdentifier(){  //parse through all the ids, and find the largest id, then add one to generate a new one
        long maxId = 0;
        for (Row row : this.rows) {
            long rowId = row.getPrimaryKey();
            maxId = Math.max(rowId, maxId);
        }
        return maxId + 1;
    }


    public ArrayList<Row> getRows(){return rows;}
    public ArrayList<Column> getColumns(){
        return columns;
    }

    public void addColumn(String columnName, String columnType){
        Column newCol = new Column(columnName, columnType);
        newCol.setColumnIndex(columns.size());
        columns.add(newCol);
    }
    public void removeColumn(String columnName){
        Iterator<Column> iterator = columns.iterator();
        if(iterator.hasNext()){
            Column col = iterator.next();
            if(col.getColumnName().equals(columnName)){
                iterator.remove();
            }
        }
        for(int i = 0; i < columns.size(); i++){
            columns.get(i).setColumnIndex(i);
        }
    }

    public void addRow(Row row){
        row.setPrimaryKey(generateRowIdentifier()); //set the row's primary key when creating a new row
        rows.add(row);
    }
    public void removeRow(Row row){
        rows.remove(row);
    }

    public void loadTable(){
        //open the file at a specific filePath
        try{
            File fileToOpen = new File(this.tablePath);
            FileReader fileReader = new FileReader(fileToOpen);
            BufferedReader buffReader = new BufferedReader(fileReader);
            String line = buffReader.readLine(); //take the first line of the file which is the headers
            String[] tokens = line.split("\\s+");
            for(int i = 1; i < tokens.length; i++){
                addColumn(tokens[i],"");
            }
            while (line != null) {
                //read the rest of the files and populate the rows
                line = buffReader.readLine();
                if(line == null){break;}
                Row row = Row.fromString(line,this.getColumnNames(), true); //turn each row into String
                if(row != null){this.addRow(row);} //add row to the table
                else{break;}
            }
            buffReader.close();
        }
        catch(FileNotFoundException e){
            System.out.println(e.getMessage());
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
    }
    public void saveTable() {
        try{
            FileWriter fileToSave = new FileWriter(this.tablePath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileToSave);
            bufferedWriter.write(Table.ID_COL + "\t");
            for(Column column : columns){
                bufferedWriter.write(column.getColumnName() + "\t");
            }
            bufferedWriter.newLine();
            for(Row row : rows){
                bufferedWriter.write(row.toString() + "\n");
            }
            bufferedWriter.close();
        }
        catch(FileNotFoundException e){
            System.out.println(e.getMessage());
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
    }


}
