# Ortec Finance Task List Case

This repository contains my Java solution for the Ortec Finance task list case.

## Assignment summary

The existing application supports creating projects, adding tasks, checking and unchecking tasks, and viewing tasks grouped by project.

The goal of this assignment is to extend the application with deadlines, add a deadline-based view, and refactor the codebase so that the core logic is easier to test and can be reused by multiple interfaces such as the console application and REST APIs.

## Planned implementation

The work is split into small, meaningful steps:

1. Add documentation and a short implementation plan.
2. Add deadline support to tasks.
3. Add the `deadline <ID> <date>` console command.
4. Add the `view-by-deadline` console command.
5. Refactor core task list logic away from the console layer.
6. Add or improve tests for the new behavior.
7. Add REST endpoints if time allows.

## Date format

Deadlines use the following format:

```text
dd-MM-yyyy
```

Example:

```text
deadline 1 25-11-2024
```

## Commands

Existing commands:

```text
show
add project <project name>
add task <project name> <task description>
check <task ID>
uncheck <task ID>
help
quit
```

New planned commands:

```text
deadline <task ID> <date>
view-by-deadline
```

Optional command if time allows:

```text
today
```

## Notes

The main focus is to keep the existing functionality working, add tests for the new behavior, and make the code easier to maintain without over-engineering the solution.
