import org.openqa.selenium.firefox.FirefoxDriver;
import au.com.bytecode.opencsv.CSVReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriverBackedSelenium;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import org.openqa.selenium.server.SeleniumServer;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.io.DOMReader;
import org.dom4j.Node;
import org.dom4j.Element;

public class DLTest {
  public static void main( String[] args ) {
      try {
	  BufferedReader adUrlReader = new BufferedReader( new FileReader( "./AdUrls.csv" ) );
	  ArrayList<String> adUrlList = new ArrayList<String>();
	  String line;   

	  while ( (line = adUrlReader.readLine()) != null ) {
	      adUrlList.add( line.trim() );
	  }
	  adUrlReader.close();
      
	  int fileCounter = 0;
	  for ( String adUrl : adUrlList ) {
	      try {
		  String cmdString = "/usr/bin/wget -v --output-document=/home/diggler/Desktop/groovy_testing/data_scrapes/Admonitor/DL/file" + Integer.toString(fileCounter) + " --tries=3 " + adUrl;
		  System.out.println( cmdString );
		  Runtime rt = Runtime.getRuntime();
		  Process  p = rt.exec( cmdString );
		  p.waitFor();
		  BufferedReader r = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		  String s;
		  while ((s = r.readLine())!=null) {
		      System.out.println( s );
		  }
		  r.close();
	      } catch ( IOException ioe ) { ioe.printStackTrace(); }
	      catch ( InterruptedException ie ) { ie.printStackTrace(); }
	      fileCounter++;
	  }

      } catch ( IOException e ) { e.printStackTrace(); }
  }
}