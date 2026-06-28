# Ortec Finance Task List Case

This repository contains my Java solution for the Ortec Finance task list case.

The original application was a console-based task list. I extended it with task deadlines, a deadline-based overview, a `today` command, a reusable service layer, and REST endpoints.

## Implemented features

### Console application

The console application supports:

```text
show
add project <project name>
add task <project name> <task description>
check <task ID>
uncheck <task ID>
deadline <task ID> <date>
view-by-deadline
today
help
quit
```

### Deadlines

Tasks can have an optional deadline.

Deadline input in the console uses this format:

```text
dd-MM-yyyy
```

Example:

```text
deadline 1 25-11-2024
```

Tasks without a deadline are still valid. In the deadline overview, they are shown at the end under a `No deadline` block.

### Tasks due today

The command:

```text
today
```

shows the same kind of data as the `show` command, but only for tasks that have a deadline equal to the current date.

Projects without tasks due today are not printed.

Example output:

```text
secrets
    [ ] 1: Pay bills

training
    [x] 3: Submit assignment
```

If no tasks are due today, the command prints:

```text
No tasks due today.
```

The command uses the application clock, which makes the behavior testable with a fixed date in automated tests.

### Deadline overview

The command:

```text
view-by-deadline
```

shows tasks grouped by deadline. Deadline groups are sorted chronologically. Inside each deadline group, tasks are grouped by project. Tasks without a deadline are shown last.

Example output:

```text
11-11-2021:
    training:
       3: Interaction-Driven Design
13-11-2021:
    secrets:
       1: Eat more donuts.
No deadline:
    secrets:
       2: Refactor the codebase
```

## REST API

The application also exposes REST endpoints for projects and tasks.

### Create a project

```http
POST /projects
Content-Type: application/json
```

```json
{
  "name": "secrets"
}
```

### Get all projects

```http
GET /projects
```

Example response:

```json
[
  {
    "name": "secrets",
    "tasks": [
      {
        "id": 1,
        "description": "Eat more donuts.",
        "done": false,
        "deadline": null
      }
    ]
  }
]
```

### Create a task in a project

```http
POST /projects/{projectId}/tasks
Content-Type: application/json
```

In this implementation, `projectId` refers to the project name, because the original application identifies projects by name.

```json
{
  "description": "Eat more donuts."
}
```

### Set a task deadline

```http
PUT /projects/{projectId}/tasks/{taskId}?deadline=25-11-2024
```

REST request dates for setting a deadline use this format:

```text
dd-MM-yyyy
```

REST responses use the standard JSON date format:

```text
yyyy-MM-dd
```

Example response:

```json
{
  "id": 1,
  "description": "Eat more donuts.",
  "done": false,
  "deadline": "2024-11-25"
}
```

### Get projects grouped by deadline

```http
GET /projects/view_by_deadline
```

This endpoint returns tasks grouped by deadline and then by project. Tasks without a deadline are returned separately under `projectsWithoutDeadline`.

Example response:

```json
{
  "deadlineGroups": [
    {
      "deadline": "2024-11-25",
      "projects": [
        {
          "name": "secrets",
          "tasks": [
            {
              "id": 1,
              "description": "Eat more donuts.",
              "done": false,
              "deadline": "2024-11-25"
            }
          ]
        }
      ]
    }
  ],
  "projectsWithoutDeadline": [
    {
      "name": "training",
      "tasks": [
        {
          "id": 2,
          "description": "Refactor the codebase",
          "done": false,
          "deadline": null
        }
      ]
    }
  ]
}
```

## Design choices

### Core logic extracted from the console

The original console class handled both user input/output and task list logic. I extracted the core logic into `TaskListService`.

This keeps the console application focused on parsing commands and printing output, while the service owns the task list behavior:

* creating projects
* creating tasks
* checking and unchecking tasks
* setting deadlines
* finding projects with tasks due on a specific date
* grouping tasks by deadline and project

This also allows the REST controller to reuse the same core logic instead of duplicating behavior.

### Date handling

Deadlines are stored as `LocalDate` instead of strings. This makes sorting and validation safer and keeps date-related logic explicit.

The console accepts dates in `dd-MM-yyyy` format because that is the format requested in the assignment. Internally, strict parsing is used to reject invalid dates.

REST responses use the standard JSON date format `yyyy-MM-dd`.

### Testable time handling

The `today` command depends on the current date. To keep this testable, the console application uses a `Clock`. In production it uses the system clock, while tests can inject a fixed clock.

This avoids tests that depend on the real current date.

### In-memory storage

The application keeps projects and tasks in memory. This matches the original codebase and keeps the solution focused on the requested behavior instead of adding persistence complexity.

### Small commits

The implementation was split into small steps:

1. Project setup and documentation
2. Deadline support
3. Deadline console command
4. Deadline overview
5. Service refactor
6. Required REST endpoints
7. Optional REST endpoints
8. Today command
9. Deadline views grouped by project
10. Tests and documentation updates

## Testing

The solution includes tests for:

* task deadline behavior
* console command behavior
* deadline grouping and ordering
* grouping deadline views by project
* tasks due today
* service-level task list logic
* required REST endpoints
* optional REST endpoints

The tests can be run from IntelliJ using the existing JUnit configuration.

## Known limitations

* Data is stored in memory and is lost when the application stops.
* Projects are identified by name, following the original console application model.
* There is no authentication or authorization.
* REST endpoint coverage focuses on the assignment scope and selected optional endpoints.
