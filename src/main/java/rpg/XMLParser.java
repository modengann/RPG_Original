package rpg;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Write a description of class XMLParser here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class XMLParser {
    private String xml;
    private ParseListener p;
    private ArrayList parsers;
    
    public static interface ParseListener {
        public void startElement(String name, HashMap attributes);
        public void readText(String text);
        public void endElement(String name);
    }
    
    private void trimSpace() {
        while(xml.length() > 0 && xml.substring(0, 1).equals(" ")) xml = xml.substring(1);
    }
    
    private void parseEndElement() {
        int i = xml.indexOf(">");
        p.endElement(xml.substring(0, i).toLowerCase());
        xml = xml.substring(i + 1);
    }
    
    private String next() {
        if(xml.length() == 0) return null;
        String c = xml.substring(0, 1);
        xml = xml.substring(1);
        return c;
    }
    
    private void parseStartElement() {
        String name = "", attribute = "", value = "";
        int mode = 0;
        // 0 = parse name, 1 = look for attribute, 2 = parse attribute name
        // 3 = parse attribute, 4 = in quotes, 5 = escaped, 6 = ending, -1 = close, -2 = end
        HashMap attributes = new HashMap();
        
        while(mode >= 0) {
            String c = next();
            
            if(c == null) break;
            
            if(mode == 0) {
                if(c.equals(" ")) mode = 1;
                else if(c.equals("/")) mode = 6;
                else if(c.equals(">")) mode = -1;
                else name += c;
            } else if(mode == 1) {
                if(c.equals("/")) mode = 6;
                else if(c.equals(">")) mode = -1;
                else if(!c.equals(" ")) {
                    mode = 2;
                    attribute = c;
                    value = "";
                }
            } else if(mode == 2) {
                if(c.equals("=")) mode = 3;
                else if(!c.equals(" ")) attribute += c;
            } else if(mode == 3) {
                if(c.equals("/")) {
                    mode = 6;
                    attributes.put(attribute.toLowerCase(), value);
                } else if(c.equals(">")) {
                    mode = -1;
                    attributes.put(attribute.toLowerCase(), value);
                } else if(c.equals("\"")) mode = 4;
                else if(c.equals(" ")) {
                    mode = 1;
                    attributes.put(attribute.toLowerCase(), value);
                } else value += mode;
            } else if(mode == 4) {
                if(c.equals("\\")) mode = 5;
                else if(c.equals("\"")) mode = 3;
                else value += c;
            } else if(mode == 5) {
                if(c.equals("n")) value += "\n";
                else if(c.equals("t")) value += "\t";
                else value += c;
                mode = 4;
            } else if(mode == 6) {
                if(c.equals(">")) mode = -2;
            }
        }
        p.startElement(name.toLowerCase(), attributes);
        if(mode == -2) p.endElement(name);
        else parse();
    }
    
    /**
     * Start using a new parse listener, but remember what the old one was so that
     * we can later go back to it with popParser().
     * @param parser new parser to use
     */
    public void pushParser(ParseListener parser) {
        parsers.add(p);
        p = parser;
    }
    
    /**
     * Go back to the previous parse listener.
     */
    public void popParser() {
        if(!parsers.isEmpty()) p = (ParseListener)parsers.remove(parsers.size() - 1);
    }
    
    /**
     * Parse the given string by making callbacks to the ParseListener this parser
     * was constructed with.
     */
    public void parse() {
        // Read the text up to the next element. If that element is an ending tag,
        // return. Otherwise, parse that element and then keep reading.
        while(true) {
            int i = xml.indexOf("<");
            if(i == -1) {
                if(xml.length() != 0) p.readText(xml);
                return;
            } else {
                if(i != 0) p.readText(xml.substring(0, i));
                xml = xml.substring(i + 1);
                
                trimSpace();
                if(xml.substring(0, 1).equals("/")) {
                    xml = xml.substring(1);
                    parseEndElement();
                    return;
                } else {
                    parseStartElement();
                }
            }
        }
    }
    
    /**
     * Create a new parser to parse the given xml string, with the given parse listener
     * @param xml string containing xml to parse
     * @param listener ParseListener that will be notified as parsing occurs
     */
    public XMLParser(String xml, ParseListener listener) {
        this.xml = xml;
        this.p = listener;
        parsers = new ArrayList();
    }
}
