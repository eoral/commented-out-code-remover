package com.eoral.deletecharsfromfilebyposition;

import java.util.Collections;
import java.util.Set;

public class DeletionRule {

    private Integer line;
    private Integer startColumn;
    private Integer endColumn;

    public DeletionRule(Integer line, Integer startColumn, Integer endColumn) {
        ensureLineIsValid(line);
        ensureColumnsAreValid(startColumn, endColumn);
        this.line = line;
        this.startColumn = startColumn;
        this.endColumn = endColumn;
    }

    private void ensureLineIsValid(Integer line) {
        if (line == null || line < 1) {
            throw new IllegalArgumentException("Line must be greater than or equal to 1.");
        }
    }

    private void ensureColumnsAreValid(Integer startColumn, Integer endColumn) {
        if (startColumn != null && endColumn == null) {
            if (startColumn < 1) {
                throw new IllegalArgumentException("Start column must be greater than or equal to 1.");
            }
        } else if (startColumn == null && endColumn != null) {
            throw new IllegalArgumentException("When end column is specified, start column must be specified too.");
        } else if (startColumn != null && endColumn != null) {
            if (startColumn < 1) {
                throw new IllegalArgumentException("Start column must be greater than or equal to 1.");
            }
            if (endColumn < startColumn) {
                throw new IllegalArgumentException("End column must be greater than or equal to start column.");
            }
        }
    }

    public Integer getLine() {
        return line;
    }

    public Integer getStartColumn() {
        return startColumn;
    }

    public Integer getEndColumn() {
        return endColumn;
    }

    public boolean deletesLine() {
        return startColumn == null && endColumn == null;
    }

    public boolean emptiesLine() {
        return startColumn != null && startColumn == 1 && endColumn == null;
    }

    public Set<Integer> getEffectedColumns(int lineLength) {
        if (startColumn == null && endColumn == null) {
            throw new IllegalStateException("This method cannot be invoked when start column and end column are null.");
        } else {
            if (startColumn > lineLength) {
                return Collections.emptySet();
            } else {
                int actualEndColumn = endColumn == null ? lineLength : Integer.min(lineLength, endColumn);
                return Utils.getColumns(startColumn, actualEndColumn);
            }
        }
    }
}
