package com.eoral.deletecharsfromfilebyposition;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeletionRulesGroupedByLine {

    private Map<Integer, List<DeletionRule>> map;

    public DeletionRulesGroupedByLine(List<DeletionRule> deletionRules) {
        map = deletionRules.stream().collect(Collectors.groupingBy(DeletionRule::getLine));
    }

    public List<DeletionRule> getByLine(int line) {
        List<DeletionRule> list = map.get(Integer.valueOf(line));
        if (list == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(list);
        }
    }
}
