package com.flacofitness.app.model.dto;

import java.util.ArrayList;
import java.util.List;

public class OperationsAutomationResult {

    private final List<String> movements = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    public void addMovement(String label, int count) {
        movements.add(label + ": " + count);
    }

    public void addError(String module, String message) {
        errors.add(module + ": " + message);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> getMovements() {
        return movements;
    }

    public List<String> getErrors() {
        return errors;
    }

    public String toHumanSummary() {
        String ok = movements.isEmpty()
                ? "No habia cambios pendientes."
                : String.join(", ", movements) + ".";
        if (errors.isEmpty()) {
            return ok;
        }
        return ok + " Avisos: " + String.join("; ", errors);
    }
}
