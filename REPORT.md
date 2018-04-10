# PROJECT REPORT PART II

## TECHNIQUES (OLD TOOLS)

### Random Testing
 
Random testing of the buggy url validator from apache's commons.validator utilized the ABNF (Augmented Backus-Naur Form) metalanguage in RFC3986 Appendix A (https://www.ietf.org/rfc/rfc3986.txt; RFC2396 is obsolete) for describing valid URI syntax and grammar, to construct both valid and invalid random URI statements. The official apache url validator from the routines package acted as the model reference to produce expected values of validity for any randomly generated URI.  The code below shows such testing of the buggy url validator, "urlValidator", with random URIs intended to be valid, generated from "getURI(true)", using the routine's url validator, "refUrlValidator", to give the expected values.  An error collector is used to reveal multiple bugs without halting tests on failure.
    
```java
@Test
public void testValidURL(){
	for (int i=0; i<NUM_TESTS; i++){
		//generate random valid URI
	    String url = getURI(true);
	    collector.checkThat(
	            i+": Expected '"+url+"' to be VALID according to both reference and buggy url validators ",
	            urlValidator.isValid(url),
	            equalTo(refUrlValidator.isValid(url))
	    );
	}
}
```

Random testing also comprised of invalid URIs generated from "getURI(false)" in similar fashion, where the boolean determines the validity. To produce random URI statements, the ABNF description of valid URIs was translated into Java.  For example, to produce the "scheme" portion of a URI, the ABNF description "scheme = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )" converted to the following:

```java
public String getScheme(boolean valid){
	//get random string of at least on alpha char 
	//with subsequent aplhanumeric, "+", "-", and "." characters up to MAX_SCHEME length
    String scheme = getChar(ALPHA)+getString(MIN_SCHEME-1, MAX_SCHEME-1, SCHEME);
	//get invalid scheme using INVALID_SCHEME characters
    if (!valid && scheme.length() > 0){
        scheme = getInvalidString(scheme, INVALID_SCHEME, scheme.length());
    }
    return scheme;
}
```

For invalid URIs, invalid character sets for each URI component were produced by subtracting the set of all valid characters for a given component from the set of all URI characters (percent encoding not used).  To produce any given random character or string, the RandomStringUtils (RSU) library was used, as shown for "getString()" below.

```java
//generate a random string from "min" to "max" length with characters taken from "char_set"
public String getString(int min_length, int max_length, String char_set){
    StringBuilder sb = new StringBuilder();
    //get random length of string
    int count = getNum(min_length, max_length);
    sb.append(		
    	//produce random string with "r" as the seed
    	rsu.random(count, 0, char_set.length(), false, false, char_set.toCharArray(), r)
    );
    return sb.toString();
}
```
    
The use of random testing revealed a signficant number of errors, not only in the buggy url validator, but potentially in official routine's url validator as well (e.g., ipv4 futures or ipv6 addresses suffixed by ipv4 in the host part of the URL authority were not valid, but should be).

### Input Domain Partitioning

Input Domain Partitioning served as an excellent way to sniff out bugs and pinpoint the input domains for which the URL validator code failed. We implemented 5 different input domain tests including a host partition (the www.google.com part of https://www.google.com/), a scheme partition (the http part of http://www.google.com), a top level domain partition (the .com in www.google.com), a query partition (the q=domain in https://www.google.com/search?q=domain), and a port partition (the :8080 in portquiz.net:8080/). There are many more ways we could break URLs down according to the RFC documentation but we felt these 5 represent several of the most commonly used and important parts of a URL. 

Similar to random testing, we implemented an error collector to avoid exit on test failure. 

For each test partition we implemented an array of result pairs containing the test string (URL) with only the partition type varying from pair to pair and result expect from the URLValidator. 
Ex. 

	String start = "http://";
	ResultPair[] testHosts = {
           new ResultPair(start + "www.google.c", false),
            new ResultPair(start + "www.goog;le.com", false),
            new ResultPair(start + "w.google.com", false),
            new ResultPair(start + "wWw.gOoGlE.cOm", true),
        }

We then iterated through each of the pairs and tested the actual result against the expected result and failed on any inconsistencies
Ex.    

	for(int i = 0; i < testHosts.length; i++){
            ResultPair currentPair = testHosts[i];
            result = urlVal.isValid(currentPair.item);
            collector.checkThat(
                "Expected scheme to be " + currentPair.valid + " for "+currentPair.item, result, equalTo(currentPair.valid));
        }

Although simple, this test certainly helped to reveal a number of bugs in the validator code. 

## TECHNIQUES (NEW TOOLS)

### Mutation Testing
One of the new tools that we decided to use is mutation testing. Mutation is a test coverage tool.  Similar to line coverage, it allows testers to evaluate the quality of their test suite.   

A major limitation of mutation testing is that it only works for passing tests.  This makes sense from the rationale behind mutation testing, because if a failed test was mutated it would be impossible to determine if the test was killing the mutation or simply continuing to fail the test as it had before the mutation.  However, this made it problematic when evaluating our test coverage, since our tests had numerous failed test cases.  
In order to utilize mutation testing in our project, we removed the test cases that failed from our test suite.  For input domain partitioning, we removed the specific urls that caused test failure and ran pitest on the remaining passing tests.  Since random testing is random (and thus there aren't specific cases that can be written out) we ran a random test of 1000 valid urls and 1000 invalid urls.  Each of the url test cases that passed was then stored in a file.  We then ran pitest on the static (but randomly generated) passing test cases.  This meant that the results of mutation coverage were underrepresenting the actual coverage of our tests, but allowed us to get some idea of our test coverage.  Using input domain partitioning resulted in 52% of mutants being killed (full report in mutateIDP/mutateIDPResults.html) while random testing resulted in 44% kill rate (mutateRandom/mutateRandomResults.html).

Using pitest for our mutation coverage allowed us to get a direct comparison between mutation coverage and line coverage, a topic we explored in more depth in this class. Instead of checking whether a line of code is run in the course of the test suite, we can examine if the test responds accordingly when the program itself is mutated, as well as "line coverage" of the mutations.  This allowed us to see specific lines of the source code that were not being tested for correctly (or were tested for but were removed because they produced an error). 

### Static Analysis
Another testing technique we utilized is static analysis. We utilized a program called FindBugs that checks predefined rules on syntax, best practices, correctness, performance, security, dodgy code, and even malicious code vulnerability. Findbugs managed to show us some possible bugs with internalization and code that it condsiders "dodgy". 

Since static analysis also entails debugging code by examiming code without executing the actual program, a weakness that we ran accross is that high programming and domain knowledge will be needed by whoever is doing static analysis. This became apparent when Java and Regular Expressions needed to be analyzed. Also, if using tools such as FindBugs(Netbeans and Eclipse have similar analysis tools), the results are only as good as the predefined rules in the program.

## BUG REPORTS
For Product: org.apache.commons.validator.UrlValidator, Validator 1.1 ($Revision: 1739358 $)

#### Title: 1000-65535 port numbers in URL authority should be valid (Random and Static Analysis)

Class: minor bug  
Is it reproducible: (Yes) / Occasionally / One Time / No

##### Description
Port numbers between 1000-65535 are incorrectly deemed invalid by the UrlValidator.isValid() function. The source code is incorrect for port number validation.

##### Steps to Produce/Reproduce
1. Instantiate UrlValidator object with ALLOW_ALL_SCHEMES, ALLOW_2_SLASHES, and ALLOW_LOCAL_URLS flags
2. Test url validator with "VyPn+eGC://242.50.112.92:59062/" => INVALID
3. Test url validator with typical port numbers, such as "VyPn+eGC://242.50.112.92:80/" => VALID 

##### Expected Results
The url should be valid for port 59062.

##### Actual Results
The url is considered invalid.

##### Test Case that Fails
UrlValidatorRandomTest.java: 8th test case for valid URIs with random seed "1480911251289"

Static Analysis: The regular expression for matching port numbers is "\d{1,3}", which will only validate 1-3 digit ports (line 160 of UrlValidator.java)

##### Justification for Passing Test Case
The port number of the url should be valid per the official org.apache.commons.validator.routines.UrlValidator. Port numbers less than 65535 are allowed by the TCP protocol as well as RFC3986's ABNF description of port, where "port = *DIGIT".  Since the scheme is not reserved, port numbers corresponding to that scheme are not reserved either.

##### Attachments
	<<>> RANDOM SEED: 1480911251289 <<>>
	java.lang.AssertionError: 8: Expected 'VyPn+eGC://242.50.112.92:59062/' to be VALID according to both reference and buggy url validators 
	Expected: <true>
		 but: was <false>
-

#### Title:  Local URLs without TLD should be valid

Class: minor bug  
Is it reproducible: (Yes) / Occasionally / One Time / No

##### Description
Local URLs without top-level domains (TLDs) are incorrectly considered invalid by the UrlValidator.isValid() function

##### Steps to Produce/Reproduce
1. Instantiate UrlValidator object with ALLOW_ALL_SCHEMES, ALLOW_2_SLASHES, and ALLOW_LOCAL_URLS flags
2. Test url validator with "ZyK6C5b3P://h/v~jVlS//N(F/gk:PLon/HW/in/=/C:/" => INVALID
3. Test url validator with a typical TLD, such as "ZyK6C5b3P://h.com/v~jVlS//N(F/gk:PLon/HW/in/=/C:/" => VALID

##### Expected Results
The URL without a TLD should be valid.

##### Actual Results
The URL without a TLD is invalid.

##### Test Case that Fails
UrlValidatorRandomTest.java: 479th test case for VALID URIs with random seed "1480914105335"

##### Justification for Passing Test Case
The url without a TLD is valid per the official org.apache.commons.validator.routines.UrlValidator. TLDs are not required per RFC3986's ABNF for the host (host = IP-literal / IPv4address / reg-name) and the registry name (reg-name = *( unreserved / pct-encoded / sub-delims). While URIs intended to have global scope do require fully-qualified domain names, the host operating is allowed to interpret the host without a specified domain for the arbitrary scheme "ZyK6C5b3P" with the "ALLOW_LOCAL_URLS" flag set.

##### Attachments
	<<>> RANDOM SEED: 1480914105335 <<>>
	java.lang.AssertionError: 479: Expected 'ZyK6C5b3P://h/v~jVlS//N(F/gk:PLon/HW/in/=/C:/' to be VALID according to both reference and buggy url validators 
	Expected: <true>
		 but: was <false>
-

#### Title:  Out-of-bounds decimal octet in host IPv4 address of URL should be disallowed (Random Testing, Static Analysis, and IDP Testing)
Class: major bug  
Is it reproducible: (Yes) / Occasionally / One Time / No

##### Description
URLs containing dotted decimal octets in the IPv4 host addresses greater than 255 are incorrectly considered valid by the UrlValidator.isValid() function.

##### Steps to Produce/Reproduce
1. Instantiate UrlValidator object with ALLOW_ALL_SCHEMES, ALLOW_2_SLASHES, and ALLOW_LOCAL_URLS flags
2. Test url validator with "Ln6sU://151.1207.232.59/9hZ3B/wRCC5-9/v-zf1j52E" => VALID
Or http://500.500.500.500 => VALID

##### Expected Results
The url with out-of-bounds decimal octect should be invalid

##### Actual Results
The url with out-of-bounds decimal octect of "1207" is valid

##### Test Case that Fails
UrlValidatorRandomTest.java: 512th test case for INVALID URIs with random seed "1480917445350"
UrlValidatorIDPTest.java: UrlValidatorIDPTest

Static Analysis: The regular expression for matching ipv4 octects is "\d{1,3}", which will incorrectly validate digits from 255-999 (line 160 of UrlValidator.java). However, since digits greater than 3 are still allowed, failing code must be present upstream.

##### Justification for Passing Test Case
The url without an invalid decimal octect is correctly invalid per the official org.apache.commons.validator.routines.UrlValidator. Further, the RFC3986's ABNF definition of decimal octect only allows values up to 255. It is literally impossible to have an binary octet greater than 255.

##### Attachments
	<<>> RANDOM SEED: 1480917445350 <<>>
	java.lang.AssertionError: 512: Expected 'Ln6sU://151.1207.232.59/9hZ3B/wRCC5-9/v-zf1j52E' to be INVALID according to both reference and buggy url validators
	Expected: <false>
		 but: was <true>

	<<IDP Test Results>>
	testHostPartition(osu.cs362.URLValidator.UrlValidatorIDPTest): Expected scheme to be false for http://500.500.500.500
	
-

#### Title:  Many top level domains are not listed in validating domain(Static Analysis)
Class: major bug  
Is it reproducible: (Yes) / Occasionally / One Time / No

##### Description
The list for validating top level domains are not comprehensive and the list differs from IANA. This will cause some domains to be invalid when they should be valid.  

##### Steps to Produce/Reproduce
1. Test URL: www.pusan.co.kr. This needs to be validated by another method, as static analysis does not run the actual program.

##### Expected Results
The url is valid and should pass the URL validator

##### Actual Results
The URL is not validated by the URL validator

##### Test Case that Fails
This is found by static analysis and comparing the list of TLDS to the IANA list.

##### Justification for Passing Test Case
The list of top level domains should include all domains specified by IANA. Becuase many domains are missing from the buggy implementation, many valid URL's using the omitted domains will not be validated by the buggy implementation.

-

#### Title:  IPV6 adresses are not validated (Static Analysis)
Class: major bug  
Is it reproducible: (Yes) / Occasionally / One Time / No

##### Description
On examination of code, did not find an implementation of IPV6 addresses. 

##### Steps to Produce/Reproduce
1. Test URL: http://[2001:4860:0:2001::68]/. This needs to be validated by another method, as static analysis does not run the actual program.

##### Expected Results
The url is valid and should pass the URL validator per rfc3986. 
    
##### Actual Results
The URL will not be validated by the Buggy URL validator

##### Test Case that Fails
This is found by static analysis and examining code 

##### Justification for Passing Test Case
According to the rfc, IP literals are valid per these rules:  

    host        = IP-literal / IPv4address / reg-name  
    IP-literal = "[" ( IPv6address / IPvFuture  ) "]"


#### Title:  Conversion to lower or upper case flagged by FindBugs program (Static Analysis)
Class: minor bug  
Is it reproducible: Yes / (Occasionally) / One Time / No

##### Description
FindBugs has flagged that the conversion methods to change case can be an issue. Recommended to use the localized version of method.

##### Steps to Produce/Reproduce
Code will need to be run and validated if URL Validator validates an address according to regional settings.

##### Expected Results
String is changed to correct uppercase or lowercase character.
    
##### Actual Results
String is not changed to correct uppercase or lowercase character.

##### Test Case that Fails
This is found by static analysis and examining code 

##### Justification for Passing Test Case
A String is being converted to upper or lowercase, using the platform's default encoding. This may result in improper conversions when used with international characters.

##### Attachments
![Figure1](https://github.com/OSU-CS362-F16/f16-project-smalleyt/blob/master/StaticAnalysis/FindBug.JPG)

-

### Title:  .us TLD not accepted
Class: major bug
Is it reproducible: (Yes) / Occasionally / One Time / No

##### Description
.us TLD is not accepted 

##### Steps to Produce/Reproduce
1. Test URL with IDP test file: http://www.google.us

##### Expected Results
The URL http://www.google.us should be valid

##### Actual Results
The URL is evaluated as invalid 

##### Test Case that Fails
testTLDPartition

##### Justification for Passing Test Case
.us is a valid Top Level Domain 

##### Attachments
	testTLDPartition(osu.cs362.URLValidator.UrlValidatorIDPTest): Expected scheme to be true for http://www.google.us
-

### Title: Basic query string rejected
Class: major bug
Is it reproducible: (Yes) / Occasionally / One Time / No

##### Description
Basic query string provided in conjuction with google.com host url does not return valid result

##### Steps to Produce/Reproduce
1. Test URL with IDP test file: http://www.google.com/search?q=google
2. Test URL with IDP test file: http://www.google.com/search?q=randomtexthere&oq=morerandomtext
3. Test URL with IDP test file: http://www.google.com/search?query=stuff

##### Expected Results
The URL http://www.google.com/search?q=google is and should return as valid

##### Actual Results
The URL is evaluated as invalid 

##### Test Case that Fails
testQueryPartition

##### Justification for Passing Test Case
search?q=google is, for example, the exact query used by google to conduct searches (keyword search here is "google"). This should be valid.

##### Attachments
	testQueryPartition(osu.cs362.URLValidator.UrlValidatorIDPTest): Expected scheme to be true for http://www.google.com/search?q=google
  
-

### Title: Invalid schemes accepted and validated by code
Class: major bug
Is it reproducible: (Yes) / Occasionally / One Time / No

##### Description
Basic URL with htp scheme is accepted 

##### Steps to Produce/Reproduce
1. Test URL with IDP test file: htp://www.google.com

##### Expected Results
The URL htp://www.google.com should not be valid

##### Actual Results
The URL is evaluated as valid 

##### Test Case that Fails
UrlValidatorIDPTest{

##### Justification for Passing Test Case
The test case should fail on htp://www.google.com because htp is not a valid scheme (there are only so many protocols accepted and htp is not one)

##### Attachments
testSchemePartition(osu.cs362.URLValidator.UrlValidatorIDPTest): Expected scheme to be false for htp://www.google.com
