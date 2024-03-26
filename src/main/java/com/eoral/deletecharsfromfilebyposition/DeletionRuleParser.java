package com.eoral.deletecharsfromfilebyposition;

import java.util.ArrayList;
import java.util.List;

public class DeletionRuleParser {

    private DeletionRuleParser() {
    }

    public static List<DeletionRule> parseMultiple(String str) {
        List<DeletionRule> deletionRules = new ArrayList<>();
        String[] parts = str.split(",");
        for (String part : parts) {
            deletionRules.add(parseSingle(part));
        }
        return deletionRules;
    }

    public static DeletionRule parseSingle(String str) {
        if (str.matches("^[1-9][0-9]*$")) { // line
            int line = Integer.parseInt(str);
            return new DeletionRule(line, null, null);
        } else if (str.matches("^[1-9][0-9]*:[1-9][0-9]*-$")) { // line:startColumn-
            String[] parts = str.split(":|-");
            int line = Integer.parseInt(parts[0]);
            int startColumn = Integer.parseInt(parts[1]);
            return new DeletionRule(line, startColumn, null);
        } else if (str.matches("^[1-9][0-9]*:[1-9][0-9]*-[1-9][0-9]*$")) { // line:startColumn-endColumn
            String[] parts = str.split(":|-");
            int line = Integer.parseInt(parts[0]);
            int startColumn = Integer.parseInt(parts[1]);
            int endColumn = Integer.parseInt(parts[2]);
            return new DeletionRule(line, startColumn, endColumn);
        } else {
            throw new IllegalArgumentException("Invalid deletion rule: " + str);
        }
    }
}
