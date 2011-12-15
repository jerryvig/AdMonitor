package com.mktneutral;

import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.Robot;
import java.awt.AWTException;
import com.thoughtworks.selenium.DefaultSelenium;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import java.util.regex.Pattern;
import org.browsermob.proxy.ProxyServer;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.CapabilityType;
import org.browsermob.core.har.Har;
import org.browsermob.core.har.HarLog;
import org.browsermob.core.har.HarEntry;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class NativeClicks {
  private static WebDriverBackedSelenium selena;
  private static String winName;
  private static int fullScreen = 0;
  private static Har har;

  public static void main( String[] args ) {
    try {
     ProxyServer server = new ProxyServer(4444);
     server.start();
     Proxy proxy = server.seleniumProxy();
        //Proxy proxy = new Proxy();  
     DesiredCapabilities capabilities = new DesiredCapabilities();
     capabilities.setCapability(CapabilityType.PROXY, proxy);

     FirefoxDriver driver = new FirefoxDriver( capabilities );
     server.newHar("univision.com");
     selena = new WebDriverBackedSelenium( driver, "univision.com" );
     doClicks(  "http://www.univision.com/" );

     har = server.getHar();
     har.writeTo( new FileOutputStream( "/tmp/harfile.txt" ) );

     parseJSON();

     server.stop();
    } catch ( NullPointerException npe ) { System.out.println( "threw npe" ); npe.printStackTrace(); }
   catch ( Exception e ) { e.printStackTrace(); }
  }

  public static void doClicks( String _url ) {
    try {    
     selena.getWrappedDriver().get( _url );

     Robot bot = new Robot();
     bot.mouseMove(350,350);
     bot.delay(200);
     bot.mousePress( InputEvent.BUTTON3_MASK );
     bot.delay( 200 );
     bot.mouseRelease( InputEvent.BUTTON3_MASK );
     bot.delay( 200 );
     if ( fullScreen == 0 ) {
      selena.windowMaximize();
      bot.keyPress( java.awt.event.KeyEvent.VK_F11 );
      bot.delay( 50 );
      bot.keyRelease( java.awt.event.KeyEvent.VK_F11 );
      bot.delay( 3000 );
      bot.delay( 200 );
      fullScreen = 1;
     }
     bot.mousePress( InputEvent.BUTTON3_MASK );
     bot.delay( 200 );
     bot.mouseRelease( InputEvent.BUTTON3_MASK );
     bot.delay( 300 );

     int screenHeight = Integer.parseInt(selena.getEval( "window.outerHeight;" ).trim());

     System.out.println( screenHeight );

     int embedCount = Integer.parseInt(selena.getEval( "var embedCount = 0; var embedList = window.document.getElementsByTagName('embed'); for ( var i=0; i<embedList.length; i++ ) { embedList.item(i).setAttribute('id','embed'+i); embedCount++; } embedCount;" ).trim());
     //int embedCount = Integer.parseInt(selena.getEval( "embedCount;" ));

     System.out.println( embedCount );

     ArrayList<String> embedList = new ArrayList<String>();
     for ( int i=0; i<embedCount; i++ ) {
	 embedList.add( selena.getEval("window.document.getElementById('embed"+i+"').getAttribute('id')") );
     }

     Set<String> windows = selena.getWrappedDriver().getWindowHandles();
     Iterator iter = windows.iterator();
     winName = "";
     while (iter.hasNext()) {
	winName = (String)iter.next();
     }

     ArrayList<String> clickPoints = new ArrayList<String>();
     for ( String embedId : embedList ) {
	 clickPoints.add( selena.getEval( "var height = window.document.getElementById( '"+embedId+"' ).height; var width = window.document.getElementById('"+embedId+"' ).width; var top = window.document.getElementById( '"+embedId+"' ).offsetTop + window.screenY; var left = window.document.getElementById('"+embedId+"' ).offsetLeft + window.screenX; var leftPt = Math.floor(left+(width/2)); var topPt = Math.floor(top+(height/2)); var clickPoint = new Array( leftPt, topPt, height ); clickPoint;" ) );
     }

     int maxVerticalPos = screenHeight;
     for ( String clickPt : clickPoints ) {
      clickPt = clickPt.replaceAll(" ","");
      clickPt = clickPt.replaceAll(Pattern.quote("["),"");
      clickPt = clickPt.replaceAll(Pattern.quote("]"),"");
      String[] clickPoint = clickPt.split(",");      
      System.out.println( clickPoint[1] );
      try {
       if ( Integer.parseInt(clickPoint[1].trim()) > maxVerticalPos ) {
	   maxVerticalPos = Integer.parseInt(clickPoint[1].trim())+Integer.parseInt(clickPoint[2].trim())/2;
       }
      } catch ( NumberFormatException nfe ) { nfe.printStackTrace(); }
      catch ( ArrayIndexOutOfBoundsException nfe ) { nfe.printStackTrace(); }
     }

     System.out.println( "maxVPos = " + maxVerticalPos );

     for ( String clickPt : clickPoints ) {
      try {
	 int maxPgDownCount = 0;
	 if ( maxVerticalPos < screenHeight ) {
	     maxPgDownCount = 0;
         }
         else {
           maxPgDownCount = (new Double(Math.floor((maxVerticalPos-(screenHeight-41))/(screenHeight-41)))).intValue();
         }
         int lastPgDown = maxVerticalPos - maxPgDownCount*(screenHeight-41) - screenHeight;

        clickPt = clickPt.replaceAll(" ","");
        clickPt = clickPt.replaceAll(Pattern.quote("["),"");
        clickPt = clickPt.replaceAll(Pattern.quote("]"),"");
        String[] clickPoint = clickPt.split(",");      

        System.out.println( "maxPgDown = " + maxPgDownCount );

        int pageDownCount = 0;
        
        int verticalPos = Integer.parseInt(clickPoint[1].trim());
        for ( int i=0; i<maxPgDownCount+1; i++ ) {
	  verticalPos = (Integer.parseInt(clickPoint[1].trim()) - (screenHeight-41)*i);
          if ( verticalPos < screenHeight ) {
             break;
          }
          pageDownCount++;
        }

       System.out.println( clickPt + "," + verticalPos );

        if ( verticalPos > screenHeight ) {
	    bot.keyPress( KeyEvent.VK_END );
            bot.delay( 200 );
            bot.keyRelease( KeyEvent.VK_END );
	    verticalPos = (Integer.parseInt(clickPoint[1].trim())) - maxPgDownCount*(screenHeight-41) - lastPgDown;
            System.out.println( "lastPgDown =" + lastPgDown + "verticalPos = " + verticalPos ); 
            doClick( bot, Integer.parseInt(clickPoint[0].trim()), verticalPos );
        }
        else {
         for ( int i=0; i<pageDownCount; i++ ) {
	   bot.mouseMove(1,1);
           bot.delay(200);
           bot.mousePress( InputEvent.BUTTON1_MASK );
           bot.delay( 400 );
           bot.mouseRelease( InputEvent.BUTTON1_MASK );
           bot.delay( 200 );
           bot.keyPress( KeyEvent.VK_PAGE_DOWN );
           bot.delay( 400 );
           bot.keyRelease( KeyEvent.VK_PAGE_DOWN ); 
           System.out.println( "you executed the page downs" );         	
         }
         doClick( bot, Integer.parseInt(clickPoint[0].trim()), verticalPos );
	}
       } catch ( NumberFormatException nfe ) { nfe.printStackTrace(); }
       catch ( ArrayIndexOutOfBoundsException nfe ) { nfe.printStackTrace(); }
     }

     int iframeCount = Integer.parseInt(selena.getEval( "var iframeCount = 0; var iframeList = window.document.getElementsByTagName('iframe'); for ( var i=0; i<iframeList.length; i++ ) { iframeList.item(i).setAttribute('id','iframe'+i); iframeCount++; } iframeCount;" ).trim());
     System.out.println( iframeCount );

     ArrayList<String> iframeSrcList = new ArrayList<String>();
     for ( int i=0; i<iframeCount; i++ ) {
	 String iframeSrc = selena.getEval( "window.document.getElementById('iframe"+i+"').getAttribute('src');" );
         iframeSrcList.add( iframeSrc );
         doClicks( iframeSrc );     
     }
 
     try {
       Process p = Runtime.getRuntime().exec( "/usr/bin/killall firefox-bin" );
       BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
       String s;
       while ((s=r.readLine())!=null) {
	   System.out.println( s );
       }
       r.close();
     } catch ( IOException ioe ) { ioe.printStackTrace(); }
    } catch ( NullPointerException npe ) { npe.printStackTrace(); }
    catch ( Exception awte ) { awte.printStackTrace(); }
   }

  public static void doClick( Robot bot, int leftPos, int verticalPos ) {
    try {
      bot.mouseMove( leftPos, verticalPos );
      bot.delay(200);
      bot.keyPress( KeyEvent.VK_CONTROL );
      bot.delay( 200 );
      bot.mousePress( InputEvent.BUTTON1_MASK );
      bot.delay( 400 );
      bot.mouseRelease( InputEvent.BUTTON1_MASK );
      bot.delay( 200 );
      bot.delay( 4000 );
      bot.keyRelease( KeyEvent.VK_CONTROL );
      
      selena.selectWindow( winName );

      bot.delay( 200 );
      bot.mouseMove(1,1);
      bot.delay(200);
      bot.mousePress( InputEvent.BUTTON1_MASK );
      bot.delay( 200 );
      bot.mouseRelease( InputEvent.BUTTON1_MASK );
      bot.delay( 200 );
      bot.keyPress( KeyEvent.VK_HOME );
      bot.delay( 200 );
      bot.keyRelease( KeyEvent.VK_HOME );
      bot.delay( 1500 ); 

    } catch ( Exception awte ) { awte.printStackTrace(); }
  }

  public static void parseJSON() {
    try {
     BufferedReader r = new BufferedReader( new FileReader( "/tmp/harfile.txt" ) );
     String s;
     String jsonString = "";
     while ((s=r.readLine())!=null) {
	jsonString += s.trim();
     }
     r.close();

     JSONObject jsonObj = new JSONObject( jsonString );
     JSONObject logObj = jsonObj.getJSONObject( "log" );    
     JSONArray entriesArray = logObj.getJSONArray("entries");
     System.out.println( entriesArray.length() );
     // System.out.println( "entries string = " + logObj.toString() );
     //String[] names = JSONObject.getNames( entriesObj );
     //System.out.println( " name are " );
     //for ( String name : names ) {
     //	 System.out.println( name );
     //}
     for ( int i=0; i<entriesArray.length(); i++ ) {
       try {
	JSONObject obj = entriesArray.getJSONObject( i );
        JSONObject resp = obj.getJSONObject( "response" );
        JSONObject content = resp.getJSONObject( "content" );
       
        if ( content.getString( "mimeType" ).equals("image/png") || content.getString( "mimeType" ).equals("image/jpeg") || content.getString( "mimeType" ).equals("image/gif") || content.getString( "mimeType" ).equals("application/x-shockwave-flash")  ) {
            System.out.println(  content.getString( "mimeType" ) );
	    JSONObject req = obj.getJSONObject("request");
            System.out.println( "url = " + req.getString( "url" ) );          
        }       
       } catch ( JSONException jsone ) {
	   jsone.printStackTrace();
       }
     }

   } catch ( IOException ioe ) { ioe.printStackTrace(); }
   catch ( JSONException jsone ) { jsone.printStackTrace(); }
  }
}