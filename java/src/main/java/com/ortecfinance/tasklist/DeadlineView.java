package com.ortecfinance.tasklist;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record DeadlineView(
        Map<LocalDate, Map<String, List<Task>>> tasksWithDeadline,
        Map<String, List<Task>> tasksWithoutDeadline
) {
}
