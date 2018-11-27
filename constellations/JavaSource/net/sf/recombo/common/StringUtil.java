
package net.sf.recombo.common;
import java.util.Map;
import java.util.Vector;

import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.apache.oro.text.perl.Perl5Util;

/**
 * String manipulation utilities.
 * 
 * @author Devendra Tewari
 */
public class StringUtil {

    /**
     * Method for splitting a text string using Perl 5 style regular expressions. 
     * For example, to split the text <pre>"a; b; c"</pre> using ";" use 
     * <pre>split ("a; b; c", "/;/")</pre> To separate the text <pre>"8-12,15,18"</pre>
     * using "," and "-" use <pre>split ("8-12,15,18", "/[,-]/")</pre>
     * 
     * @param text
     *            Text to split.
     * @param regex
     *            Perl 5 style regular expression.
     * @return Array of String.
     */
    public static String[] split(String text, String regex) throws MalformedPerl5PatternException {
        Vector list = new Vector();
        Perl5Util util = new Perl5Util();
        String[] result;

        util.split(list, regex, text, Perl5Util.SPLIT_ALL);

        result = new String[list.size()];
        list.copyInto(result);
        return result;
    }

    /**
     * Substitute special characters in a String by their corresponding HTML equivalents.
     * 
     * @param str Text string.
     * @return    A new text string after substitution.
     */
    public static String formatToHTML(String str){
        StringBuffer retorno = new StringBuffer();        
        int tamanho = str.length();
        
        for(int i = 0; i < tamanho; i++){
            char c = str.charAt(i);
            
            switch (c) {
            	case '&':
                    retorno.append("&amp;");
            		break;
            	case '<':
                    retorno.append("&lt;");
            		break;
            	case '>':
                    retorno.append("&gt;");
            		break;
            	case '"':
                    retorno.append("&quot");
            		break;
            	case '\'':
                    retorno.append("&#039;");
            		break;
            	default:
                    retorno.append(c);
            }
        }        
        
        return retorno.toString();
    }    

    /**
     * Interpolate a text string with the given arguments. The arguments that appear 
     * in the text should have the syntax ${nome_parametro}. An argument in the text
     * can be a list of values. For example, the code: 
     * <pre>
     * Map map = new HashMap();
     * map.put("test0", "abc0"); 
     * map.put("test1", "abc1"); 
     * map.put("test2","abc2"); 
     * System.out.println(interpolate("The list is ${test}", map)); 
     * </pre>
     * will print the following to the output:
     * <pre> 
     *   The list is : abc0, abc1, abc2.
     * </pre>
     * 
     * @param Some text with arguments
     * @param arguments Argument map
     * @return Interpolated text
     */
    public static String interpolate(String text, Map arguments) {
        return interpolate(text, arguments, false);
    }

    /**
     * Interpolate a text string with the given arguments. The arguments that appear 
     * in the text should have the syntax ${nome_parametro}. An argument in the text
     * can be a list of values. For example, the code: 
     * <pre> 
     *   Map map = new HashMap();
     *   map.put("test0", "abc0"); 
     *   map.put("test1", "abc1"); 
     *   map.put("test2", "abc2"); 
     *   System.out.println(interpolate("The list is ${test}", map, true));
     * </pre> 
     * will print the following to the output:
     * <pre> 
     *   The list is
     *   <ul>
     *     <li>abc0</li>
     *     <li>abc1</li>
     *     <li>abc2</li>
     *   </ul>
     * </pre>
     * 
     * @param Some text with arguments
     * @param arguments Argument map
     * @param html
     *            Set to true to generate html output.
     * @return Interpolated text
     */
    public static String interpolate(String text, Map arguments,
            boolean html) {
        StringBuffer result = new StringBuffer();

        int posTextStart = 0;
        int posArgStart = text.indexOf("${");

        if (posArgStart == -1) {
            // No arguments found, copy the entire text.
            result.append(text);
        } else {
            while (posArgStart != -1) {
                // Copy the text before the argument
                result.append(text.substring(posTextStart,
                        posArgStart));

                // Discover the name of the argument
                int posArgEnd = text.indexOf("}", posArgStart);

                if (posArgEnd == -1) {
                    posTextStart = text.length();
                    result.append("<MISSING CLOSING BRACE>");
                    break;
                }

                String argument = text.substring(posArgStart + 2,
                        posArgEnd);

                // Discover the value of the argument and concatenate to result
                Object valor = arguments.get(argument);

                if (valor != null) {
                    result.append(valor);
                } else {
                    // argument has a list of values
                    int i = 0;
                    valor = arguments.get(argument + i);
                    while (valor != null) {
                        if (i == 0) {
                            if (html)
                                result.append("<ul>");
                            else
                                result.append(": ");
                        } else {
                            if (!html) result.append(", ");
                        }
                        if (html) result.append("<li>");
                        result.append(valor);
                        if (html) result.append("</li>");
                        i++;
                        valor = arguments.get(argument + i);
                    }
                    if (html)
                        result.append("</ul>");
                    else
                        result.append(".");
                    if (i == 0) result.append("<" + argument + "=NULL>");
                }

                // Prepare to resolve the next argument
                posTextStart = posArgEnd + 1;
                posArgStart = text.indexOf("${", posTextStart);
            }

            // Concatenate the remaining text
            result.append(text.substring(posTextStart));
        }

        return result.toString();
    }

    /**
     * Check to see if the specified text is empty. This means the
     * reference to String is not null and the String contains
     * at least one non whitespace character. 
     * 
     * @return boolean True if the text is empty.
     */

    public static boolean isEmpty(String text) {
        boolean retVal = false;
        if (text == null || text.trim().length() == 0) {
			retVal = true;
		}
        return retVal;
    }
    
    /**
	 * Substitute single quote and double quote in text with appropriate escape
	 * sequences valid in scripting languages like javascript and beanshell. 
	 * Thus a single quote is substituted in the text by \\' and a double quote by \\".
	 * 
	 * @param text Text string
	 */
    public static String escapeQuote(String text) {
		return text.replaceAll("'", "\\\\'").replaceAll("\"", "\\\\\"");
	}
    
    public static void main(String[] args) {
    }
}
