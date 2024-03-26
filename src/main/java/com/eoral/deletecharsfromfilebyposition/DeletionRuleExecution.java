package com.eoral.deletecharsfromfilebyposition;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class DeletionRuleExecution {

    public static void main(String[] args) {
        String inputFilePathStr = "C:\\Users\\212752553\\development\\repo\\from-github-com\\delete-chars-from-file-by-position\\src\\main\\resources\\infra-setup.ts.txt";
        String inputFileCharset = "UTF-8";
        String deletionRulesStr = "5:1-3";
        new DeletionRuleExecution().deleteCharsFromFile(inputFilePathStr, inputFileCharset, deletionRulesStr, null);
    }

    public void deleteCharsFromFile(
            String inputFilePathStr,
            String inputFileCharset,
            String deletionRulesStr,
            BehaviorAfterDeletionRulesExecutedForEachLine behaviorAfterDeletionRulesExecutedForEachLine) {
        deleteCharsFromFile(
                Path.of(inputFilePathStr),
                Charset.forName(inputFileCharset),
                DeletionRuleParser.parseMultiple(deletionRulesStr),
                behaviorAfterDeletionRulesExecutedForEachLine);
    }

    public void deleteCharsFromFile(
            Path inputFilePath,
            Charset inputFileCharset,
            List<DeletionRule> deletionRules,
            BehaviorAfterDeletionRulesExecutedForEachLine behaviorAfterDeletionRulesExecutedForEachLine) {

        DeletionRulesGroupedByLine deletionRulesGroupedByLine = new DeletionRulesGroupedByLine(deletionRules);
        Path tempFilePath = Utils.createTempFile();

        try (FileInputStream fis = new FileInputStream(inputFilePath.toFile());
             InputStreamReader isr = new InputStreamReader(fis, inputFileCharset);
             BufferedReader reader = new BufferedReader(isr);
             FileOutputStream fos = new FileOutputStream(tempFilePath.toFile());
             OutputStreamWriter osw = new OutputStreamWriter(fos, inputFileCharset);
             BufferedWriter writer = new BufferedWriter(osw)) {

            int lineNumber = 0;
            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                List<DeletionRule> deletionRulesOfLine = deletionRulesGroupedByLine.getByLine(lineNumber);
                String restOfTheLine = deleteCharsFromLine(line, deletionRulesOfLine, behaviorAfterDeletionRulesExecutedForEachLine);
                if (restOfTheLine != null) {
                    writer.write(restOfTheLine);
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        Utils.moveFileReplaceExisting(tempFilePath, inputFilePath);
    }

    public String deleteCharsFromLine(
            String line,
            List<DeletionRule> deletionRules,
            BehaviorAfterDeletionRulesExecutedForEachLine behaviorAfterDeletionRulesExecutedForEachLine) {
        if (deletionRules.isEmpty()) {
            return line; // return line as is
        } else {
            String lineAfterRulesApplied;
            if (containsOneRuleThatDeletesLine(deletionRules)) {
                lineAfterRulesApplied = null; // line will be deleted
            } else if (containsOneRuleThatEmptiesLine(deletionRules)) {
                lineAfterRulesApplied = ""; // line will be emptied
            } else {
                Set<Integer> columns = Utils.getColumns(1, line.length());
                if (!columns.isEmpty()) {
                    for (DeletionRule deletionRule : deletionRules) {
                        Set<Integer> columnsToBeRemoved = deletionRule.getEffectedColumns(line.length());
                        columns.removeAll(columnsToBeRemoved);
                        if (columns.isEmpty()) {
                            break;
                        }
                    }
                }
                lineAfterRulesApplied = getRemainingChars(line, columns); // return rest of the line
            }
            return applyBehaviorAfterDeletionRulesExecutedForEachLine(
                    lineAfterRulesApplied, behaviorAfterDeletionRulesExecutedForEachLine);
        }
    }

    private boolean containsOneRuleThatDeletesLine(List<DeletionRule> deletionRules) {
        for (DeletionRule deletionRule : deletionRules) {
            if (deletionRule.deletesLine()) {
                return true;
            }
        }
        return false;
    }

    private boolean containsOneRuleThatEmptiesLine(List<DeletionRule> deletionRules) {
        for (DeletionRule deletionRule : deletionRules) {
            if (deletionRule.emptiesLine()) {
                return true;
            }
        }
        return false;
    }

    private String getRemainingChars(String line, Set<Integer> remainingColumns) {
        if (remainingColumns.isEmpty()) {
            return "";
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            SortedSet<Integer> remainingColumnsSorted = new TreeSet<>(remainingColumns);
            for (Integer column : remainingColumnsSorted) {
                int beginIndex = column - 1;
                int endIndex = beginIndex + 1;
                stringBuilder.append(line, beginIndex, endIndex);
            }
            return stringBuilder.toString();
        }
    }

    private String applyBehaviorAfterDeletionRulesExecutedForEachLine(
            String lineAfterRulesApplied, BehaviorAfterDeletionRulesExecutedForEachLine behavior) {
        String result = lineAfterRulesApplied;
        if (lineAfterRulesApplied != null && behavior != null) {
            if (behavior.isDeleteLineIfBlank() && lineAfterRulesApplied.trim().length() == 0) {
                result = null;
            }
        }
        return result;
    }
}
