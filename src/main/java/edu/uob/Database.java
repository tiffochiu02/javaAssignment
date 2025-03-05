package edu.uob;

import javax.xml.xpath.XPath;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Database {
    private String name;
    private String path;
    private Map<String, Table> tables;

    public Database(String name, String basePath) {
        this.name = name.toLowerCase();
        this.path = basePath + File.separator + this.name;
        tables = new HashMap<>();

        File dbFolder = new File(path);
        if (!dbFolder.exists()) dbFolder.mkdir(); //If folder doesn't exist, create a new directory
        //otherwise, load all the existing tables
        else {
            for (File tableFile : dbFolder.listFiles()) {
                if(!tableFile.getName().toLowerCase().endsWith(".tab")) continue;
                String tableName = tableFile.getName().split("\\.")[0].toLowerCase();
                Table table = new Table(tableName, this.path);
                table.loadTable();
                this.addTable(table);

            }
        }
    }
    public String getName() {return name;}
    public String getPath() {return path;}
    public boolean hasTable(String tableName) {
        return tables.containsKey(tableName.toLowerCase());
    }
    public Table getTables(String tableName) {
        return tables.get(tableName.toLowerCase());
    }
    public void addTable(Table table) {
        tables.put(table.getTableName().toLowerCase(), table);
    }

}