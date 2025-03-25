package edu.uob;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
import com.alexmerz.graphviz.objects.Edge;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class EntitiesFileTests {

  // Test to make sure that the basic entities file is readable
  @Test
  void testBasicEntitiesFileIsReadable() {
      try {
//          Parser parser = new Parser();
//          FileReader reader = new FileReader("config" + File.separator + "basic-entities.dot");
//          parser.parse(reader);
//          Graph wholeDocument = parser.getGraphs().get(0);
//          ArrayList<Graph> sections = wholeDocument.getSubgraphs();
//
//          // The locations will always be in the first subgraph
//          ArrayList<Graph> locations = sections.get(0).getSubgraphs();
//          Graph firstLocation = locations.get(0);
//          Node locationDetails = firstLocation.getNodes(false).get(0);
//          // Yes, you do need to get the ID twice !
//          String locationName = locationDetails.getId().getId();
//          assertEquals("cabin", locationName, "First location should have been 'cabin'");
//
//          // The paths will always be in the second subgraph
//          ArrayList<Edge> paths = sections.get(1).getEdges();
//          Edge firstPath = paths.get(0);
//          Node fromLocation = firstPath.getSource().getNode();
//          String fromName = fromLocation.getId().getId();
//          Node toLocation = firstPath.getTarget().getNode();
//          String toName = toLocation.getId().getId();
//          assertEquals("cabin", fromName, "First path should have been from 'cabin'");
//          assertEquals("forest", toName, "First path should have been to 'forest'");

          Parser parser = new Parser();
          FileReader reader = new FileReader("config" + File.separator + "basic-entities.dot");
          parser.parse(reader);
          Graph wholeDocument = parser.getGraphs().get(0);
          HashMap<Integer,Graph> sections = new HashMap<>();
          sections.put(0,wholeDocument);

          Graph Location = sections.get(0).getSubgraphs().get(0);
          Node locationDetails = Location.getSubgraphs().get(0).getNodes(false).get(0); //first get is the number of location
          String locationName = locationDetails.getId().getId();
          assertEquals("cabin", locationName, "First location should have been 'cabin'");
          String locationDescriptionName = locationDetails.getAttributes().get("description");
          System.out.println("the location description is: " + locationDescriptionName);

          Graph firstArtefact = Location.getSubgraphs().get(0);
          Node firstArtefactDetails = firstArtefact.getSubgraphs().get(0).getNodes(false).get(1);
          String firstArtefactName = firstArtefactDetails.getId().getId();
          System.out.println("the first artefact is: " + firstArtefactName);

          Edge firstPath = sections.get(0).getSubgraphs().get(1).getEdges().get(0);
          Node fromLocation = firstPath.getSource().getNode();
          String fromName = fromLocation.getId().getId();
          Node toLocation = firstPath.getTarget().getNode();
          String toName = toLocation.getId().getId();
          assertEquals("cabin", fromName, "First path should have been from 'cabin'");
          assertEquals("forest", toName, "First path should have been to 'forest'");


      } catch (FileNotFoundException fnfe) {
          fail("FileNotFoundException was thrown when attempting to read basic entities file");
      } catch (ParseException pe) {
          fail("ParseException was thrown when attempting to read basic entities file");
      }
  }

}
