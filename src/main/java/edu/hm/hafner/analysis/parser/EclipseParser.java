package edu.hm.hafner.analysis.parser;

import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Priority;
import edu.hm.hafner.analysis.RegexpDocumentParser;

/**
 * A parser for Eclipse compiler warnings.
 *
 * @author Ullrich Hafner
 */
public class EclipseParser extends RegexpDocumentParser {
    private static final long serialVersionUID = 425883472788422955L;
    private static final String ANT_ECLIPSE_WARNING_PATTERN = "\\[?(WARNING|ERROR)\\]?" +      // group 1 'type':
            // WARNING or ERROR in optional []
            "\\s*(?:in)?" +                  // optional " in"
            "\\s*(.*)" +                     // group 2 'filename'
            "(?:\\(at line\\s*(\\d+)\\)|" +  // either group 3 'lineNumber': at line dd
            ":\\[(\\d+)).*" +                // or group 4 'rowNumber': eg :[row,col] - col ignored
            "(?:\\r?\\n[^\\^\\n]*){1,3}" +   // 1 or 3 ignored lines (no column pointer) eg source excerpt
            "\\r?\\n(.*)" +                  // newline then group 5 (indent for column pointers)
            "([\\^]+).*" +                   // group 6 column pointers (^^^^^)
            "\\r?\\n(?:\\s*\\[.*\\]\\s*)?" + // newline then optional ignored text in [] (eg [javac])
            "(.*)";                          // group 7 'message'

    /**
     * Creates a new instance of {@link EclipseParser}.
     */
    public EclipseParser() {
        super("ejc", ANT_ECLIPSE_WARNING_PATTERN, true);
    }

    @Override
    protected Issue createWarning(final Matcher matcher) {
        String type = StringUtils.capitalize(matcher.group(1));
        Priority priority;
        if ("warning".equalsIgnoreCase(type)) {
            priority = Priority.NORMAL;
        }
        else {
            priority = Priority.HIGH;
        }

        int columnStart = StringUtils.defaultString(matcher.group(5)).length();
        int columnEnd = columnStart + matcher.group(6).length();

        Issue issue = issueBuilder().setFileName(matcher.group(2)).setLineStart(parseInt(getLine(matcher)))
                                    .setColumnStart(columnStart).setColumnEnd(columnEnd).setType(getId())
                                    .setCategory(StringUtils.EMPTY).setMessage(matcher.group(7)).setPriority(priority)
                                    .build();

        return issue;
    }

    private String getLine(final Matcher matcher) {
        String eclipse34 = matcher.group(3);
        String eclipse38 = matcher.group(4);

        return StringUtils.defaultIfEmpty(eclipse34, eclipse38);
    }
}

