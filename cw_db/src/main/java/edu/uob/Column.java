package edu.uob;

import java.util.ArrayList;

public class Column {
    private String columnName;
    private String columnType;
    private Integer columnIndex;
    private boolean isPrimaryKey;

    public Column(String columnName, String ColumnType) {
        this.columnName = columnName.toLowerCase();
        this.columnType = "";
        this.columnIndex = null;
        this.isPrimaryKey = false;

    }

    public String getColumnName() {return columnName;}
    public void setColumnName(String columnName) {
        this.columnName = columnName.toLowerCase();
    }
    public String getColumnType() {return columnType;}
    public Integer getColumnIndex(String columnName) {return columnIndex;}
    public void setColumnIndex(int columnIndex) {this.columnIndex = columnIndex;}
    public boolean isPrimaryKey() {return isPrimaryKey;}
//    public String toString(Column column) {}
//    public Column fromString(String column) {}
}
