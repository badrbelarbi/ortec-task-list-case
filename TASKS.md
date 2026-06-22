# Task List Case - Implementation Plan

## Goal

Extend the existing task list application with task deadlines, a deadline-based overview, and a cleaner structure that separates core task list logic from the console interface.

## Priorities

### P0 - Project setup and documentation

* [x] Create public GitHub repository
* [x] Add original Java codebase
* [x] Add `.gitignore`
* [ ] Add README with solution notes
* [ ] Add this task plan

### P1 - Add deadlines to tasks

* [ ] Add a deadline field to `Task`
* [ ] Make the default deadline empty
* [ ] Parse dates in `dd-MM-yyyy` format
* [ ] Add the console command `deadline <ID> <date>`
* [ ] Add tests for setting deadlines

### P2 - Add deadline view

* [ ] Add the console command `view-by-deadline`
* [ ] Group tasks by deadline
* [ ] Sort deadline groups chronologically
* [ ] Show tasks without a deadline in a `No deadline` block at the end
* [ ] Add tests for grouping and ordering

### P3 - Refactor for multiple interfaces

* [ ] Extract core business logic from the console class
* [ ] Introduce a `TaskListService`
* [ ] Keep the console application working
* [ ] Add tests for the core service

### P4 - REST API

Required:

* [ ] `POST /projects` to create a project
* [ ] `GET /projects` to return all projects and tasks

Optional:

* [ ] `POST /projects/{projectId}/tasks`
* [ ] `PUT /projects/{projectId}/tasks/{taskId}?deadline=<date>`
* [ ] `GET /projects/view_by_deadline`

### P5 - Optional features

* [ ] Add `today` command
* [ ] Group deadline view by project
