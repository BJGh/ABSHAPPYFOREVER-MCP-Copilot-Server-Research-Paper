package tokens;

import java.util.regex.Pattern;

public class Terminal {

    private final Pattern pattern;
    private final LexType type;
    private final int priority;

    public Terminal (String pattern, LexType type, int priority) {

        this.priority = priority;
        this.pattern = Pattern.compile(pattern);
        this.type = type;
    }

    public Terminal (String pattern, LexType type) {

        this(pattern, type, 0);
    }

    public boolean matches (StringBuilder string) {

        return pattern.matcher(string).matches();
    }

    public Pattern get_pattern () {

        return pattern;
    }

    public LexType get_type () {

        return type;
    }

    public int get_priority () {

        return priority;
    }

    public Terminal compare_priority (Terminal terminal) {

        if (terminal == null || this.priority >= terminal.priority)
            return this;
        else
            return terminal;
    }
}
