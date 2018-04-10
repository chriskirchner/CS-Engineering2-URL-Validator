package osu.cs362.URLValidator;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.*;

public class simpleUrlTest{
	@Test public void simpleTest1(){
		boolean valid;
		
		//String[] schemes = {"http","https"};
		UrlValidator urlValidator = new UrlValidator();
		if (urlValidator.isValid("ftp://foo.bar.com/")) {
		   valid = true;
		} 
		else {
		   valid = false;
		}
		
		assertTrue("valid check", valid);
	}
	
		@Test public void simpleTest2(){
		boolean valid;
		
		String[] schemes = {"http","https"};
		UrlValidator urlValidator = new UrlValidator(schemes);
		if (urlValidator.isValid("ftp://foo.bar.com/")) {
		   valid = true;
		} 
		else {
		   valid = false;
		}
		
		assertFalse("invalid with scheme", valid);
	}
	
		@Test public void simpleTest3(){
		boolean valid;
		
		//String[] schemes = {"http","https"};
		UrlValidator urlValidator = new UrlValidator();
		if (urlValidator.isValid("http://e.bay.c.om")) {
		   valid = true;
		} 
		else {
		   valid = false;
		}
		
		assertFalse("invalid url", valid);
	}
}
