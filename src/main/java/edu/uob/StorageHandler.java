package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class StorageHandler{
    //String columnNames;

    public static void main(String[] args){
        try {
            String name = "databases" + File.separator + "people.tab";
            //a variable that tracks the current file path?
            //String currentFilePath = System.getProperty("user.dir") + File.separator + name;

            File fileToOpen = new File(name);
            //        if(!fileToOpen.exists()){
            //            throw new FileNotFoundException(name); //how to catch this by another part of the server?
            //        }


            FileReader fileReader = new FileReader(fileToOpen);
            BufferedReader buffReader = new BufferedReader(fileReader);
            String line = buffReader.readLine();
            String[] columns = line.split("\t");

            System.out.println(columns[1]);
            while (line != null) {
                System.out.println(line);
                line = buffReader.readLine();
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

}

//loadtable
//save - filewriter and bufferwriter
