package osu.cs362.URLValidator;


import junit.framework.TestCase;
import static org.junit.Assert.*;
import org.junit.Test;
import junit.framework.TestCase;
import java.util.*;

/*

IDP Test
Partitions- The following is a list of the partitions we've broken the URLs into for testing with
accompanying definitions from RFC 1738 (pub. 1996) & 3986 (pub. 2005)

URL Basic Format-
       <scheme>:<scheme-specific-part>



 1. Hostname or entire (The fully qualified domain name of a network host, or its IP
        address as a set of four decimal digit groups separated by
        ".")
 2. Scheme/Protocol (Scheme names consist of a sequence of characters. The lower case
   letters "a"--"z", digits, and the characters plus ("+"), period
   ("."), and hyphen ("-") are allowed.)
 3. Top level domain 
 4. Queries
 5. Port

 */

public class UrlValidatorIDPTest{

	/*
	Partition 1 Test: Test the host partition
	 A Hostname is the www.google.com part of https://www.google.com/ 
	*/
	@Test
    public void testHostPartition(){
    	//Let user know which test is being implemented
    	System.out.println("--HOST PARTITION TEST--");
    	//Create an instance of the UrlValidator itself 
    	UrlValidator urlVal = new UrlValidator(null, null, UrlValidator.ALLOW_ALL_SCHEMES);

    	//Create strings to append to beginning host partition
    	String start = "http://";

    	//Set result to false by default
    	boolean result = false; 

    	//Create Array of VALID + INVALID ResultPair type objects with host partition cases to test
    	//Denote whether or not they are valid in the contructors
    	//Since it is impossible to list all possible domain host names, I'm just going to list 10 of
    	//varying compositions
    	ResultPair[] testHosts = {
    		new ResultPair(start + "www.google.com", true),
            new ResultPair("http://216.58.195.238", true),
            //new ResultPair("http://500.500.500.500", false),
    		new ResultPair(start + "www.google.co", true),
    		new ResultPair(start + "www.google.c", false),
    		new ResultPair(start + "www.goog;le.com", false),
    		//new ResultPair(start + "w.google.com", false),
    		new ResultPair(start + "wWw.gOoGlE.cOm", true),
    		new ResultPair(start + "www..com", false),
    		new ResultPair(start + "www.google", false),
    		new ResultPair(start + "www.oregonstate.edu", true),
    		new ResultPair(start + "www.oregonstate.eedu", false)
    	};

    	//Finally, test schemes
    	for(int i = 0; i < testHosts.length; i++){
    		ResultPair currentPair = testHosts[i];
    		result = urlVal.isValid(currentPair.item);

				assertEquals(currentPair.valid, result);
    	}

    }

    /*
	Partition 2 Test: Test the scheme/protocol partition
	 A scheme/protocol is the http part of http://www.google.com
	*/
	@Test
    public void testSchemePartition(){
	    //Let user know which test is being implemented
	    System.out.println("--SCHEME PARTITION TEST--");
	    //Create an instance of the UrlValidator itself 
	    UrlValidator urlVal = new UrlValidator(null, null, UrlValidator.ALLOW_ALL_SCHEMES);

        //Create strings to append to beginning host partition
        String end = "://www.google.com";
        String email = ":smalleyt@oregonstate.edu";
        //Set result to false by default
        boolean result = false; 

        //Create Array of VALID + INVALID ResultPair type objects with host partition cases to test
        //Denote whether or not they are valid in the contructors
        //
        ResultPair[] testSchemes = {
            new ResultPair("http" + end, true),
            //new ResultPair("htp" + end, false),
            new ResultPair("8008" + end, false),
            new ResultPair("ftp" + end, true),
            new ResultPair("https" + end, true),
            new ResultPair("http:" + end, false),
            //new ResultPair("mailto" + email, true),
            new ResultPair("mail" + email, false)
        };

        /*Finally, test schemes*/
        for(int i = 0; i < testSchemes.length; i++){
            ResultPair currentPair = testSchemes[i];
            result = urlVal.isValid(currentPair.item);

				assertEquals(currentPair.valid, result);
        }

    }

    /*
	Partition 3 Test: Test the top level domain
	 A TLD or top level domain is the .com in www.google.com
	*/
	@Test
    public void testTLDPartition(){
    	//Let user know which test is being implemented
    	System.out.println("--TLD PARTITION TEST--");
    	//Create an instance of the UrlValidator itself 
    	UrlValidator urlVal = new UrlValidator(null, null, UrlValidator.ALLOW_ALL_SCHEMES);
        String start = "http://www.google.";
        boolean result = false; 

        //Create Array of VALID + INVALID ResultPair type objects with host partition cases to test
        //Denote whether or not they are valid in the contructors
        ResultPair[] testTLD = {
            new ResultPair(start + "com", true),
            new ResultPair(start + "org", true),
            new ResultPair(start + "gov", true),
            new ResultPair(start + "edu", true),
            //new ResultPair(start + "us", true),
            //new ResultPair(start + "randomone", true),
            //new ResultPair(start + "custom", true),
            new ResultPair(start + "$9daiav&", false),
            new ResultPair(start + "//8080", false),
            new ResultPair(start + "net", true)
        };

        /*Finally, test schemes*/
        for(int i = 0; i < testTLD.length; i++){
            ResultPair currentPair = testTLD[i];
            result = urlVal.isValid(currentPair.item);

				assertEquals(currentPair.valid, result);
        }

    }


	/*
	Partition 4 Test: Test queries
	A queries is the q=domain in https://www.google.com/search?q=domain
	*/	
	@Test
    public void testQueryPartition(){
    	//Let user know which test is being implemented
    	System.out.println("--QUERY PARTITION TEST--");
    	//Create an instance of the UrlValidator itself 
    	UrlValidator urlVal = new UrlValidator(null, null, UrlValidator.ALLOW_ALL_SCHEMES);
        String start = "http://www.google.com/search";
        boolean result = false; 

        //Create Array of VALID + INVALID ResultPair type objects with host partition cases to test
        //Denote whether or not they are valid in the contructors
        ResultPair[] testQueries = {
            //new ResultPair(start + "?q=google", true),
            //new ResultPair(start + "?qstuff", true),
            //new ResultPair(start + "?q=randomtexthere&oq=morerandomtext", true),
            //new ResultPair(start + "?query=stuff", true),
            new ResultPair(start + "?query=$$$!@(&?*", false),
            new ResultPair(start + "?query= ", false)
        };

                /*Finally, test schemes*/
        for(int i = 0; i < testQueries.length; i++){
            ResultPair currentPair = testQueries[i];
            result = urlVal.isValid(currentPair.item);

				assertEquals(currentPair.valid, result);
        }

    }

    /*
    Partition 5: Test the port partition
	A port is the :8080 in portquiz.net:8080/
	*/
	//@Test
    public void testPortPartition(){
    	//Let user know which test is being implemented
    	System.out.println("--PORT PARTITION TEST--");
    	//Create an instance of the UrlValidator itself 
    	UrlValidator urlVal = new UrlValidator(null, null, UrlValidator.ALLOW_ALL_SCHEMES);
        String start = "http://www.google.com:";
        boolean result = false; 

        //Create Array of VALID + INVALID ResultPair type objects with host partition cases to test
        //Denote whether or not they are valid in the contructors
        ResultPair[] testPort = {
            new ResultPair(start + "8080", true),
            new ResultPair(start + "23456789", false),
            new ResultPair(start + "thisisnotanumber", false),
            new ResultPair(start + "8?x&3", false),
            new ResultPair(start + "1a2b3c", false)
        };

        /*Finally, test schemes*/
        for(int i = 0; i < testPort.length; i++){
            ResultPair currentPair = testPort[i];
            result = urlVal.isValid(currentPair.item);

				assertEquals(currentPair.valid, result);
        }

    }






}
