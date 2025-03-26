package edu.uob;

import java.util.Collection;
import java.util.HashMap;

public class GameAction {
    private HashMap<Integer,String> triggers;
    private String narration;
    private HashMap<Integer,String> subjects;
    private HashMap<Integer, String> consumed;
    private HashMap<Integer,String> produced;

    public GameAction(){
        this.triggers = new HashMap<>();
        this.narration = "";
        this.subjects = new HashMap<>();
        this.consumed = new HashMap<>();
        this.produced = new HashMap<>();
    }
    public void setNarration(String narration){
        this.narration = narration;
    }
    public void addTriggers(String trigger, int key){
        this.triggers.put(key,trigger);
    }
    public void addSubjects(String subject, int key){
        this.subjects.put(key,subject);
    }
    public void addConsumed(int key, String value){
        this.consumed.put(key,value);
    }
    public void addProduced(int key, String value){
        this.getProduced().put(key,value);
    }
    public HashMap<Integer,String> getTriggers(){
        return this.triggers;
    }

    public HashMap<Integer,String> getSubjects() { return this.subjects; }
    public HashMap<Integer,String> getConsumed() { return this.consumed; }
    public HashMap<Integer,String> getProduced() { return this.produced; }
    public String getNarration() {
        return this.narration;
    }

}
