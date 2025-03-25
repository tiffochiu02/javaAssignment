package edu.uob;

//import org.junit.jupiter.api.Test;
//import org.junit.Test;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.w3c.dom.Document;
//import static org.junit.jupiter.api.Assertions.fail;
//import static org.junit.jupiter.api.Assertions.assertEquals;
public class ActionsFileReader {
    private LinkedList<GameAction> actions;

    public ActionsFileReader() {
        actions = new LinkedList<>();
    }

    public LinkedList<GameAction> getActions() {
        return this.actions;
    }

    public void ActionsHandling(File actionsFile) {
        try{
            //File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(actionsFile);
            Element root = document.getDocumentElement();
            NodeList actions = root.getChildNodes();
            int numberOfActions = actions.getLength()/2;
            System.out.println("Number of actions: " + numberOfActions);

            setActions(actions);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setActions(NodeList actions) {
        int numberOfActions = actions.getLength();
        int indexOfActions = 0;
        for (int i = 1; i < numberOfActions; i+=2) {
            Element action = (Element)actions.item(i);
            Element triggers = (Element)action.getElementsByTagName("triggers").item(0);
            //NodeList triggers = action.getElementsByTagName("triggers");
            Element subjects = (Element)action.getElementsByTagName("subjects").item(0);
            Element consumed = (Element)action.getElementsByTagName("consumed").item(0);
            Element produced = (Element)action.getElementsByTagName("produced").item(0);
            Element narration = (Element)action.getElementsByTagName("narration").item(0);
            String narrationText = narration.getTextContent();
            GameAction newAction = new GameAction();
            setTriggers(triggers,newAction);
            setSubjects(subjects,newAction);
            setConsumed(consumed,newAction);
            setProduced(produced,newAction);
            newAction.setNarration(narrationText);
            this.actions.add(indexOfActions,newAction);
            indexOfActions++;
            System.out.println("Action " + indexOfActions + newAction.getTriggers().toString() +
                    " subjects: " + newAction.getSubjects().toString() + " consumed: " + newAction.getConsumed().toString()
                    + " produced: " + newAction.getProduced().toString() + " narration: " + newAction.getNarration().toString());
        }
    }

    public void setTriggers(Element triggers, GameAction newAction) {
        int numberOfTriggers = triggers.getElementsByTagName("keyphrase").getLength();
        System.out.println("Number of triggers: " + numberOfTriggers);
        for (int i = 0; i < numberOfTriggers; i++) {
            //Element trigger = (Element)triggers.item(i);
            String triggerPhrase = triggers.getElementsByTagName("keyphrase").item(i).getTextContent();
            System.out.println("TriggerPhrase: " + triggerPhrase);
            newAction.addTriggers(triggerPhrase,i);
        }
    }

    public void setSubjects(Element subjects, GameAction newAction) {
        int numberOfSubjects = subjects.getElementsByTagName("entity").getLength();
        for (int i = 0; i < numberOfSubjects; i++) {
            String subjectEntity = subjects.getElementsByTagName("entity").item(i).getTextContent();
            System.out.println("SubjectEntity: " + subjectEntity);
            newAction.addSubjects(subjectEntity,i);
        }
    }

    public void setConsumed(Element consumed, GameAction newAction) {
        int numberOfConsumed = consumed.getElementsByTagName("entity").getLength();
        for (int i = 0; i < numberOfConsumed; i++) {
            String consumedEntity = consumed.getElementsByTagName("entity").item(i).getTextContent();
            System.out.println("ConsumedEntity: " + consumedEntity);
            newAction.addConsumed(i,consumedEntity);
        }
    }

    public void setProduced(Element produced, GameAction newAction) {
        int numberOfProduced = produced.getElementsByTagName("entity").getLength();
        for (int i = 0; i < numberOfProduced; i++) {
            String producedEntity = produced.getElementsByTagName("entity").item(i).getTextContent();
            System.out.println("ProducedEntity: " + producedEntity);
            newAction.addProduced(i,producedEntity);
        }
    }

//    public HashMap<Integer,String> getAllTriggers(){
//        HashMap<Integer,String> triggers = new HashMap<Integer,String>();
//        for(int i = 0; i < this.actions.size(); i++){
//            triggers.putAll(this.actions.get(i).triggers);
//        }
//        return triggers;
//    }




}
