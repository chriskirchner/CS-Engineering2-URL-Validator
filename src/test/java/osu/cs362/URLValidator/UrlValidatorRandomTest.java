package osu.cs362.URLValidator;

/**
 * Created by Chris Kirchner on 11/19/16.
 * Description: URL validator testing using random input verified against known URL validator
 * Email: kirchnch@oregonstate.edu
 * Organization: OSU
 */

import org.junit.Assert;
import org.junit.Before;

import java.nio.channels.ReadableByteChannel;
import java.util.Random;
import java.util.regex.Pattern;

import org.junit.Rule;
import org.junit.Test;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.rules.ErrorCollector;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;


/**
 * RFC1738 for valid URL
 * (only obsoleted for telent URI (4248) and gopher URI (4266)
 * URL must have a scheme and scheme part, so empty hier-part are removed
 * altough they are allowed per RFC3986
 * <scheme>:<scheme-specific-part>
 *
 */


public class UrlValidatorRandomTest {

    /** global settings **/

    //specify how many random urls to generate and test against
    int NUM_RANDOM_URLS = 1000;

    //specify scheme size
    //scheme        = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
    int MIN_SCHEME = 1;
    int MAX_SCHEME = 10;

    //specify user info size
    //userinfo      = *( unreserved / pct-encoded / sub-delims / ":" )
    int MIN_USERINFO = 0;
    int MAX_USERINFO = 10;

    //usually specify unreserved ports 1024 to 2^216, but can be higher (?)
    //port          = *DIGIT
    int MIN_PORT = 1024;
    int MAX_PORT = 65535;
    int MIN_INVALID_PORT = 65536;
    int MAX_INVALID_PORT = 2147483647;

    //IPV4 future length
    //IPvFuture     = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )
    int MIN_FUTURE_HEX = 1;
    int MAX_FUTURE_HEX = 10;
    int MIN_FUTURE = 1;
    int MAX_FUTURE = 10;

    //specify registry name size
    //reg-name      = *( unreserved / pct-encoded / sub-delims )
    int MIN_REG_NAME = 0;
    int MAX_REG_NAME = 10;

    /**
     * path          = path-abempty    ; begins with "/" or is empty
     / path-absolute   ; begins with "/" but not "//"
     / path-noscheme   ; begins with a non-colon segment
     / path-rootless   ; begins with a segment
     / path-empty      ; zero characters
     */
    //path-abempty  = *( "/" segment )
    //specify number of path levels
    int MIN_PATH_LEVELS = 0;
    int MAX_PATH_LEVELS = 10;
    //specify path size
    int MIN_PATH = 0;
    int MAX_PATH = 5;


    //specify length of each url component
    //segment       = *pchar
    int MIN_SEGMENT = 0;
    int MAX_SEGMENT = 10;

    //specify segment_nz size
    //segment-nz    = 1*pchar
    int MIN_SEGMENT_NZ = 1;
    int MAX_SEGEMENT_NZ = 10;

    //specify query size
    //query         = *( pchar / "/" / "?" )
    int MIN_QUERY = 0;
    int MAX_QUERY = 15;

    //specify fragment size
    //fragment      = *( pchar / "/" / "?" )
    int MIN_FRAG = 0;
    int MAX_FRAG = 10;

    //specify probability of uri parts showing up in random uri
    double USERINFO_P = 0.5;
    double PORT_P = 0.5;
    double QUERY_P = 0.5;
    double FRAGMENT_P = 0.2;
    double PATH_ABS_P = 0.9;

    //specify probability of uri parts being invalid
    double INVALID_OCTET_P = 0.2;
    double INVALID_AUTH_P = 0.3;
    double INVALID_FRAG_P = 0.1;
    double INVALID_QUERY_P = 0.1;
    double INVALID_SCHEME_P = 0.2;
    double INVALID_HIER_P = 0.2;
    double INVALID_USERINFO_P = 0.2;
    double INVALID_HOST_P = 0.4;
    double INVALID_PORT_P = 0.2;

    //specify octet sizes for IPv4
    int MIN_OCTET = 0;
    int MAX_OCTET = 255;
    int MIN_INVALID_OCTET = 256;
    int MAX_INVALID_OCTET = 2048;

    int MAX_INVALID_CHARS = 3;
    
    //specify character sets
    String LOWER_ALPHA = "abcdefghijklmnopqrstuvwxyz";
    String UPPER_ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String ALPHA = LOWER_ALPHA+UPPER_ALPHA;
    String DIGITS = "012345689";
    String ALPHANUM = ALPHA+DIGITS;
    String HEX = "0123456789abcdef";
    String ALL = ALPHA+DIGITS+"+-._~!$&'()*+,;=:/?#[]@";
    String SCHEME_SPECIALS = "+-.";
    String SCHEME = LOWER_ALPHA+UPPER_ALPHA+DIGITS+SCHEME_SPECIALS;
    String INVALID_SCHEME = ALL.replaceAll("["+ Pattern.quote(SCHEME)+"]", "");
    String UNRESERVED_SPECIALS = "-._~";
    String UNRESERVED = LOWER_ALPHA+UPPER_ALPHA+DIGITS+UNRESERVED_SPECIALS;
    String SUB_DELIMS = "!$&'()*+,;=";
    //Also contains PCT_ENCODED chars
    String PCHAR = UNRESERVED+SUB_DELIMS+":@";
    String FRAGMENT_SPECIALS = "/?";
    String GEN_DELIMS = ":/?#[]@";
    String RESERVED = GEN_DELIMS+SUB_DELIMS;
    String USERINFO = UNRESERVED+SUB_DELIMS+":";
    String INVALID_USERINFO = ALL.replaceAll("["+ Pattern.quote(USERINFO)+"]", "");
    String FUTURE = UNRESERVED+SUB_DELIMS+":";
    String SEGMENT_NZ_NC = UNRESERVED+SUB_DELIMS+"@";
    String QUERY_SPECIALS = "/?";
    String QUERY = PCHAR+QUERY_SPECIALS;
    String INVALID_QUERY = ALL.replaceAll("["+ Pattern.quote(QUERY)+"]", "");
    String FRAG = QUERY;
    //invalid frag has everything except what's in query
    String INVALID_FRAG = ALL.replaceAll("["+ Pattern.quote(FRAG)+"]", "");
    String REG_NAME = UNRESERVED+SUB_DELIMS;
    //try just non hex alpha chars for ipv6
    String INVALID_IPV6 = ALPHANUM.replaceAll("["+ Pattern.quote(HEX)+"]", "");
    //String INVALID_IPV6 = ALL.replaceAll("["+ Pattern.quote(HEX)+"]", "");
    String INVALID_REG_NAME = ALL.replaceAll("["+ Pattern.quote(REG_NAME)+"]", "");

    org.apache.commons.validator.routines.UrlValidator refUrlValidator;
    UrlValidator urlValidator;
    RandomStringUtils rsu;
    Random r;


    /**
     * setup: setup tests with randomness, official url validator from routines, and buggy url validator
     * @Params: allows all url options (e.g. ALLOW_ALL_SCHEMES)
     */
    @Before
    public void setup(){
        //official url validator
        refUrlValidator = new org.apache.commons.validator.routines.UrlValidator(
                org.apache.commons.validator.routines.UrlValidator.ALLOW_ALL_SCHEMES
                +org.apache.commons.validator.routines.UrlValidator.ALLOW_2_SLASHES
                +org.apache.commons.validator.routines.UrlValidator.ALLOW_LOCAL_URLS
        );
        //buggy url validtor
        urlValidator = new UrlValidator(
                UrlValidator.ALLOW_ALL_SCHEMES
                +UrlValidator.ALLOW_2_SLASHES
                +UrlValidator.ALLOW_LOCAL_URLS
        );
        //setup random seed and utilies
        rsu = new RandomStringUtils();
        long seed = System.currentTimeMillis();
        r = new Random(seed);
        System.out.printf("<<>> RANDOM SEED: %d <<>>\n", seed);
    }

    /**
     * getNum
     * @param min: min random value
     * @param max: max random value
     * @return: random number between min and max, inclusive
     */
    public int getNum(int min, int max){
        return r.nextInt(max-min+1)+min;
    }

    /**
     * getHexChar
     * @return: arandom character from hex character set
     */
    public String getHexChar(){
        return rsu.random(1, 0, HEX.length(), true, true, HEX.toCharArray(), r);
    }

    /**
     * getChar
     * @param char_set: allowed character set
     * @return: arandom value from allowed character set
     */
    public String getChar(String char_set){
        return rsu.random(1, 0, char_set.length(), false, false, char_set.toCharArray(), r);

    }

    /**
     * getString
     * @param min_length: min random string length
     * @param max_length: max random string length
     * @param char_set: allowed character set
     * @return: arandom string of chars from allowed character set between min and max length
     */
    public String getString(int min_length, int max_length, String char_set){
        StringBuilder sb = new StringBuilder();
        int count = getNum(min_length, max_length);
        sb.append(
                rsu.random(count, 0, char_set.length(), false, false, char_set.toCharArray(), r)
        );
        return sb.toString();
    }

    /**
     * getInvalidString
     * @param str: string to invalidate
     * @param invalid_chars: invalid character set
     * @param max_invalid_chars: max invalid characters to replace
     * @return: an invalid string using characters from the invalid character set
     */
    public String getInvalidString(String str, String invalid_chars, int max_invalid_chars, String keep_chars){
        StringBuilder sb = new StringBuilder(str);
        //set random number of invalid characters to collapse onto string
        String tmp = str;
        int max_chars = max_invalid_chars;
        if (keep_chars != "") max_chars = tmp.replaceAll("["+Pattern.quote(keep_chars)+"]", "").length();
        int num_invalid_chars = getNum(1, max_chars);
        while (num_invalid_chars > 0){
            int index = getNum(0, str.length()-1);
            //make sure the character being replaced is not already invalid
            if (invalid_chars.indexOf(sb.charAt(index)) == -1){
                String ch = getChar(invalid_chars);
                //only replace characters you don't want to keep
                if (keep_chars.indexOf(sb.charAt(index)) == -1){
                    //replace valid with invalid char
                    sb.replace(index, index+1, ch);
                    num_invalid_chars--;
                }
            }
        }
        return sb.toString();
    }

    //overloaded method for getInvalidString
    public String getInvalidString(String str, String invalid_chars, int max_invalid_chars){
        return getInvalidString(str, invalid_chars, max_invalid_chars, "");
    }

    //nice code - http://stackoverflow.com/questions/17359834/random-boolean-with-weight-or-bias
    //return true a number of times equal to the probability
    /**
     * getBoolByProb
     * @param probability: probability of event
     * @return: atrue a number of times equal to the probability of an event
     */
    public boolean getBoolByProb(double probability){
        return (r.nextDouble() < probability);
    }

    /**
     * getBoolArrayByProb
     * @param probability: probability of event
     * @param size: size of array
     * @return: an array of booleans based on the probability of an event, with at least one true element
     */
    public boolean[] getBoolArrayByProb(double probability, int size){

        //return true for superfluous array
        boolean[] boolArray = new boolean[size];
        if (size == 1){
            boolArray[0] = true;
            return boolArray;
        }

        //add trues to array based on the probability of the event
        //force at least one true
        boolean aTrue = false;
        while (aTrue == false){
            for (int i=0; i<size; i++){
                boolean b = getBoolByProb(probability);
                boolArray[i] = b;
                if (b == true){
                    aTrue = true;
                }
            }
        }

        return boolArray;
    }


    /**
     * getURI
     * @param valid: true if the random URI is valid, false otherwise
     * @return: a random URI according to the RFC3986 ABNF scheme below
     */
    //URI           = scheme ":" hier-part [ "?" query ] [ "#" fragment ]
    public String getURI(boolean valid){
        String uri = "";
        //randomly decide if optional uri parts are shown
        boolean show_fragment = getBoolByProb(FRAGMENT_P);
        boolean show_query = getBoolByProb(QUERY_P);

        boolean isInvalid = false;
        boolean invalid_frag = false;
        boolean invalid_query = false;
        boolean invalid_scheme = false;
        boolean invalid_hier = false;

        //randomly decide what uri parts will be invalidated,
        // with at least one invalid part for if uri is to be invalid
        while (!valid && !isInvalid) {
            if (show_fragment) invalid_frag = getBoolByProb(INVALID_FRAG_P);
            if (show_query) invalid_query = getBoolByProb(INVALID_QUERY_P);
            invalid_scheme = getBoolByProb(INVALID_SCHEME_P);
            invalid_hier = getBoolByProb(INVALID_HIER_P);
            if (invalid_frag || invalid_query || invalid_scheme || invalid_hier) isInvalid = true;
        }

        //build uri
        uri += getScheme(!invalid_scheme);
        uri += ":" + getHierPart(!invalid_hier);
        if (show_query) uri += "?" + getQuery(!invalid_query);
        if (show_fragment) uri += "#" + getFragment(!invalid_frag);

        return uri;
    }

    /**
     * getURI
     * @param valid: true if the random hier-part is valid, false otherwise
     * @return: a random hierarchical part according to the RFC3986 ABNF scheme below
     */

    /** RFC 3986
     / hier-part     = "//" authority path-abempty
     / path-absolute
     / path-rootless
     / path-empty
     **/

    public String getHierPart(boolean valid){
        StringBuilder hier = new StringBuilder();
        //randomly choose hier-part type
        int option = getNum(1, 4);
        switch(option){
            case 1: hier.append("//"+getAuthority(valid)+getPathAbEmpty()); break;
            /**CASE 2-4 NOT VALID FOR URL (?)**/
            case 2: hier.append(getPathAbsolute()); break;
            case 3: hier.append(getPathRootless()); break;
            case 4: hier.append(getPathEmpty()); break;
        }
        return hier.toString();
    }


    /** URI REFERENCE - NOT VALID URL (?) **/
    //URI-reference = URI / relative-ref
    public String getURIReference(){
        return "";
    }


    /** RELATIVE REF - NOT VALID URL (?) **/
    //relative-ref  = relative-part [ "?" query ] [ "#" fragment ]
    public String getRelativeRef(){
        return "";
    }

    /** RELATIVE PART - NOT VALID URL (?) **/
    /**
     / relative-part = "//" authority path-abempty
     / path-absolute
     / path-noscheme
     / path-empty
     *
     */
    public String getRelativePart(){
        return "";
    }

    /**
     * getScheme
     * @param valid: true if the random scheme is valid, false otherwise
     * @return: a random scheme according to the RFC3986 ABNF scheme below
     */
    //scheme        = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
    public String getScheme(boolean valid){
        String scheme = getChar(ALPHA)+getString(MIN_SCHEME-1, MAX_SCHEME-1, SCHEME);
        if (!valid && scheme.length() > 0){
            scheme = getInvalidString(scheme, INVALID_SCHEME, scheme.length());
        }
        return scheme;
    }

    /**
     * getAuthority
     * @param valid: true if the random authority is valid, false otherwise
     * @return: a random authority according to the RFC3986 ABNF scheme below
     */
    //authority     = [ userinfo "@" ] host [ ":" port ]
    public String getAuthority(boolean valid){
        String authority = "";
        //randomly decide if optional authority parts are shown
        boolean show_userinfo = getBoolByProb(USERINFO_P);
        boolean show_port = getBoolByProb(PORT_P);

        boolean isInvalid = false;
        boolean invalid_userinfo = false;
        boolean invalid_port = false;
        boolean invalid_host = false;

        //randomly decide what uri parts will be invalidated,
        // with at least one invalid part for if authority is to be invalid
        while (!valid && !isInvalid){
            if (show_userinfo) invalid_userinfo = getBoolByProb(INVALID_USERINFO_P);
            if (show_port) invalid_port = getBoolByProb(INVALID_PORT_P);
            invalid_host = getBoolByProb(INVALID_HOST_P);
            if (invalid_userinfo || invalid_port || invalid_host) isInvalid = true;
        }

        //build authority
        if (show_userinfo) authority += getUserInfo(!invalid_userinfo)+"@";
        authority += getHost(!invalid_host);
        if (show_port) authority += ":"+getPort(!invalid_port);

        return authority;
    }

    /**
     * getUserInfo
     * @param valid: true if the random user info is valid, false otherwise
     * @return: a random user info part according to the RFC3986 ABNF scheme below
     */
    /** ! CAN LATER ADD PCT_ENCODED **/
    //userinfo      = *( unreserved / pct-encoded / sub-delims / ":" )
    public String getUserInfo(boolean valid){
        String userInfo = "";
        userInfo = getString(MIN_USERINFO, MAX_USERINFO, USERINFO);
        if(!valid && userInfo.length() > 0) {
            userInfo = getInvalidString(userInfo, INVALID_USERINFO, userInfo.length());
        }
        return userInfo;
    }

    /**
     * getHost
     * @param valid: true if the random host is valid, false otherwise
     * @return: a random host according to the RFC3986 ABNF scheme below
     */
    /** ! CAN LATER TO ADD PCT_ENCODED **/
    //host = IP-literal / IPv4address / reg-name
    public String getHost(boolean valid){
        String host = "";

        //randomly choose host type with equal probabilities
        int option = getNum(1, 3);
        switch(option){
            case 1: host = getIPLiteral(valid); break;
            case 2: host = getIPv4Address(valid); break;
            case 3: host = getRegName(valid); break;
        }

        return host;
    }

    /**
     * getPort
     * @param valid: true if the random port is valid, false otherwise
     * @return: a random port according to the RFC3986 ABNF scheme below AND TCP PROTOCOL
     */
    //port          = *DIGIT
    public int getPort(boolean valid){
        int port = 0;
        if (valid) port = getNum(MIN_PORT, MAX_PORT);
        else port = getNum(MIN_INVALID_PORT, MAX_INVALID_PORT);
        return port;
    }

    /**
     * getIPLiteral
     * @param valid: true if the random ip literal is valid, false otherwise
     * @return: a random ip literal according to the RFC3986 ABNF scheme below
     */
    //IP-literal = "[" ( IPv6address / IPvFuture ) "]"
    public String getIPLiteral(boolean valid){
        int option = getNum(1,2);
        String IPLiteral = "[";
        switch(option){
            case 1: IPLiteral += getIPv6Address(valid); break;
            /**IP FUTURES APPEARS TO BE ALREADY INVALID BY REFERENCE URL INVALIDATOR (?) **/
            case 2: IPLiteral += getIPvFuture(); break;
        }
        IPLiteral += "]";
        return IPLiteral;
    }

    /**
     * getIPvFuture
     * @return: a random ipv4 future according to the RFC3986 ABNF scheme below
     */
    /** IPv4 FUTURES APPEAR TO BE INVALID PER OFFICIAL URL VALIDATOR (?) **/
    //IPvFuture     = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )
    public String getIPvFuture(){
        StringBuilder future = new StringBuilder();
        future.append("v");
        int count = getNum(MIN_FUTURE_HEX, MAX_FUTURE_HEX);
        for (int i=0; i<count; i++){
            future.append(getHexChar());
        }
        future.append(".");
        count = getNum(MIN_FUTURE, MAX_FUTURE);
        future.append(
                rsu.random(count, 0, FUTURE.length(),
                        true, true, FUTURE.toCharArray(), r)
        );
        return future.toString();
    }

    /**
     * getIPv6Address
     * @param valid: true if the random ipv6 address is valid, false otherwise
     * @return: a random ipv6 address according to the RFC3986 ABNF scheme below
     */
    /**
     *
     * IPv6address = 6( h16 ":" ) ls32
     / "::" 5( h16 ":" ) ls32
     / [ h16 ] "::" 4( h16 ":" ) ls32
     / [ *1( h16 ":" ) h16 ] "::" 3( h16 ":" ) ls32
     / [ *2( h16 ":" ) h16 ] "::" 2( h16 ":" ) ls32
     / [ *3( h16 ":" ) h16 ] "::" h16 ":" ls32
     / [ *4( h16 ":" ) h16 ] "::" ls32
     / [ *5( h16 ":" ) h16 ] "::" h16
     / [ *6( h16 ":" ) h16 ] "::"
     */
    public String getIPv6Address(boolean valid){
        String ipv6 = "::";
        //randomly decide ipv6 structure with equal probabilities
        while (ipv6.equals("::")){
            int option = getNum(1, 9);
            switch (option){
                case 1: ipv6 = getH16s(6,6)+":"+getLS32(); break;
                case 2: ipv6 = "::"+getH16s(5,5)+":"+getLS32(); break;
                case 3: ipv6 = getH16s(0,1)+"::"+getH16s(4,4)+":"+getLS32(); break;
                case 4: ipv6 = getH16s(0,2)+"::"+getH16s(3,3)+":"+getLS32(); break;
                case 5: ipv6 = getH16s(0,3)+"::"+getH16s(2,2)+":"+getLS32(); break;
                case 6: ipv6 = getH16s(0,4)+"::"+getH16s(1,1)+":"+getLS32(); break;
                case 7: ipv6 = getH16s(0,5)+"::"+getLS32(); break;
                case 8: ipv6 = getH16s(0,6)+"::"+getH16(); break;
                case 9: ipv6 = getH16s(0,7)+"::"; break;
            }
        }
        /** ! CAN LATER ADD LENGTHS BEYOND H16 **/
        if (!valid) ipv6 = getInvalidString(ipv6, INVALID_IPV6, ipv6.length(), ":");
        return ipv6;
    }


    /**
     * getH16
     * @return: a random h16 ipv6 address part according to the RFC3986 ABNF scheme below
     */
    //h16           = 1*4HEXDIG
    public String getH16(){
        String h16 = "";
        int count = getNum(1, 4);
        for (int i=0; i<count; i++){
            h16 += getHexChar();
        }
        return h16;
    }

    /** multiple h16s stringed together **/
    /**
     * getH16s
     * @param min: min number of H16s
     * @param max: max number of H16s
     * @return: a random string of H16 parts glued by ":"s
     */
    public String getH16s(int min, int max){
        String h16s = "";
        //randomly decide length
        int count = getNum(min, max);
        if (count > 1){
            h16s = getH16();
        }
        for (int i=0; i<count-1; i++){
            h16s += ":"+getH16();
        }
        return h16s;
    }

    /**
     * getLS32
     * @return: a random LS32 ipv6 address part according to the RFC3986 ABNF scheme below
     */
    //ls32          = ( h16 ":" h16 ) / IPv4address
    public String getLS32(){
        String ls32 = "";
        //randomly choose between H16s or IPv4 address
        int option = getNum(1,2);
        switch (option){
            case 1: ls32 = getH16()+":"+getH16(); break;
            case 2: ls32 = getIPv4Address(true); break;
        }
        return ls32;
    }

    /**
     * getIPv4Address
     * @param valid: true if the random ipv4 address is valid, false otherwise
     * @return: a random ipv4 address according to the RFC3986 ABNF scheme below
     */
    //IPv4address = dec-octet "." dec-octet "." dec-octet "." dec-octet
    public String getIPv4Address(boolean valid){
        String ipv4 = "";
        if (valid){
            ipv4 = getDecOctet(true)+"."+getDecOctet(true)
                    +"."+getDecOctet(true)+"."+getDecOctet(true);
        }
        else {
            //randomly choose ipv4 parts that are invalid
            boolean[] valids = getBoolArrayByProb(INVALID_OCTET_P, 4);
            ipv4 = getDecOctet(!valids[0])+"."+getDecOctet(!valids[1])
                    +"."+getDecOctet(!valids[2])+"."+getDecOctet(!valids[3]);
        }
        return ipv4;
    }

    /**
     * getDecOctet
     * @param valid: true if the random decimal octet  is valid, false otherwise
     * @return: a random ipv4 address according to the RFC3986 ABNF scheme below
     */
    /**
     * dec-octet = DIGIT ; 0-9
     / %x31-39 DIGIT ; 10-99
     / "1" 2DIGIT ; 100-199
     / "2" %x30-34 DIGIT ; 200-249
     / "25" %x30-35 ; 250-255
     */
    public String getDecOctet(boolean valid){
        int dec = 0;
        if (valid){
            dec = getNum(MIN_OCTET, MAX_OCTET);
        }
        else {
            dec = getNum(MIN_INVALID_OCTET, MAX_INVALID_OCTET);
        }
        return Integer.toString(dec);
    }

    /** ! CAN LATER ADD PCT_ENCODED CHARS **/
    /**
     * getRegName
     * @param valid: true if the random registered name is valid, false otherwise
     * @return: a random registered name according to the RFC3986 ABNF scheme below
     */
    //reg-name = *( unreserved / pct-encoded / sub-delims )
    public String getRegName(boolean valid){
        String regName = getString(MIN_REG_NAME, MAX_REG_NAME, REG_NAME);
        if (!valid && regName.length() > 0) regName = getInvalidString(regName, INVALID_REG_NAME, regName.length());
        return regName;
    }

    /**
     * path          = path-abempty    ; begins with "/" or is empty
     / path-absolute   ; begins with "/" but not "//"
     / path-noscheme   ; begins with a non-colon segment
     / path-rootless   ; begins with a segment
     / path-empty      ; zero characters
     */
//    public String getPath(){
//        String path = "";
//        int option = getNum(1, 5);
//        switch (option){
//            case 1: path = getPathAbEmpty(); break;
//            case 2: path = getPathAbsolute(); break;
//            case 3: path = getPathNoScheme(); break;
//            case 4: path = getPathRootless(); break;
//            case 5: path = getPathEmpty(); break;
//        }
//        return path;
//    }

    /**
     * getPathAbEmpty
     * @return: a random absolute or empty path
     */
    //path-abempty  = *( "/" segment )
    public String getPathAbEmpty(){
        StringBuilder path = new StringBuilder();
        //randomly decide number of folders
        int count = getNum(0, MAX_PATH_LEVELS);
        for (int i=0; i<count; i++){
            path.append(
                    "/"+getSegment()
            );
        }
        return path.toString();
    }

    /**
     * getPathAbsolute
     * @return: a random absolute path
     */
    //path-absolute = "/" [ segment-nz *( "/" segment ) ]
    public String getPathAbsolute(){
        StringBuilder path = new StringBuilder();
        path.append("/");
        boolean path_rootless = getBoolByProb(PATH_ABS_P);
        if (path_rootless){
            path.append(
                    getPathRootless()
            );
        }
        return path.toString();
    }

    /**
     * getPathNoScheme
     * @return: a non-zero non-colon path followed by an absolute or empty path according to the RFC3986 ABNF scheme below
     */
    //path-noscheme = segment-nz-nc *( "/" segment )
    public String getPathNoScheme(){
        return getSegmentNZNC()+getPathAbEmpty();
    }

    /**
     * getPathRootless
     * @return: a random rootless path according to the RFC3986 ABNF scheme below
     */
    //path-rootless = segment-nz *( "/" segment )
    public String getPathRootless(){
        return getSegmentNZ()+getPathAbEmpty();
    }

    /**
     * getPathEmpty
     * @return: returns an empty path according to the RFC3986 ABNF scheme below
     */
    //path-empty    = 0<pchar>
    public String getPathEmpty(){
        return "";
    }

    /**
     * getSegment
     * @return: a path segment made of pchar according to the RFC3986 ABNF scheme below
     */
    //segment       = *pchar
    public String getSegment(){
        return getString(MIN_SEGMENT, MAX_SEGMENT, PCHAR);
    }

    /**
     *
     * getSegmentNZ
     * @return: a random non-zero segment according to the RFC3986 ABNF scheme below
     */
    //segment-nz    = 1*pchar
    public String getSegmentNZ(){
        return getString(MIN_SEGMENT_NZ, MAX_SEGEMENT_NZ, PCHAR);
    }

    /**
     * getSegmentNZNC
     * @return: a random non-zero non-colon segment according to the RFC3986 ABNF scheme below
     */
    /** 
     * segment-nz-nc = 1*( unreserved / pct-encoded / sub-delims / "@" )
     * ; non-zero-length segment without any colon ":"
     **/
    public String getSegmentNZNC(){
        return getString(MIN_SEGMENT_NZ, MAX_SEGEMENT_NZ, SEGMENT_NZ_NC);
    }

    /** ! CAN LATER ADD PCT_ENCODED **/
    /**
     * getQuery
     * @param valid: true if the random ipv4 address is valid, false otherwise
     * @return: a random query according to the RFC3986 ABNF scheme below
     */
    //query         = *( pchar / "/" / "?" )
    public String getQuery(boolean valid){
        String query = getString(MIN_QUERY, MAX_QUERY, QUERY);
        if (!valid && query.length() > 0){
            query = getInvalidString(query, INVALID_QUERY, query.length());
        }
        return query;
    }

    /**
     * getFragment
     * @param valid: true if the random ipv4 address is valid, false otherwise
     * @return: a random fragment according to the RFC3986 ABNF scheme below
     */
    //fragment      = *( pchar / "/" / "?" )
    //same as query
    public String getFragment(boolean valid){
        String frag = getString(MIN_FRAG, MAX_FRAG, FRAG);
        if (!valid && frag.length() > 0) {
            frag = getInvalidString(frag, INVALID_FRAG, frag.length());
        }
        return frag;
    }

    /** TESTING CODE **/

    //citation - http://stackoverflow.com/questions/5147187/ignore-assertion-failure-in-a-testcase-junit
    //setup ErrorCollector to run many tests at once with failing on one
    @Rule
    public ErrorCollector collector = new ErrorCollector();

    //test urls intended to be valid
    @Test
    public void testValidURL(){
        for (int i=0; i<1000; i++){
            String url = getURI(true);
            collector.checkThat(
                    i+": Expected '"+url+"' to be VALID according to both reference and buggy url validators ",
                    urlValidator.isValid(url),
                    equalTo(refUrlValidator.isValid(url))
            );
        }
    }

    //test urls intended be invalid
    @Test
    public void testInvalidURL(){
        for (int i=0; i<1000; i++){
            String url = getURI(false);
            collector.checkThat(
                    i+": Expected '"+url+"' to be INVALID according to both reference and buggy url validators",
                    urlValidator.isValid(url),
                    equalTo(refUrlValidator.isValid(url))
            );
        }
    }

}
