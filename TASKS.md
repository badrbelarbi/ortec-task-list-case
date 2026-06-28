# Task List Case - Implementation Plan

## Goal

Extend the existing task list application with task deadlines, a deadline-based overview, and a cleaner structure that separates core task list logic from the console interface.

## Priorities

### P0 - Project setup and documentation

* [x] Create public GitHub repository
* [x] Add original Java codebase
* [x] Add `.gitignore`
* [x] Add README with solution notes
* [x] Add this task plan

### P1 - Add deadlines to tasks

* [x] Add a deadline field to `Task`
* [x] Make the default deadline empty
* [x] Parse dates in `dd-MM-yyyy` format
* [x] Add the console command `deadline <ID> <date>`
* [x] Add tests for setting deadlines

### P2 - Add deadline view

* [x] Add the console command `view-by-deadline`
* [x] Group tasks by deadline
* [x] Sort deadline groups chronologically
* [x] Show tasks without a deadline in a `No deadline` block at the end
* [x] Add tests for grouping and ordering

### P3 - Refactor for multiple interfaces

* [x] Extract core business logic from the console class
* [x] Introduce a `TaskListService`
* [x] Keep the console application working
* [x] Add tests for the core service

### P4 - REST API

Required:

* [x] `POST /projects` to create a project
* [x] `GET /projects` to return all projects and tasks

Optional:

* [ ] `POST /projects/{projectId}/tasks`
* [ ] `PUT /projects/{projectId}/tasks/{taskId}?deadline=<date>`
* [ ] `GET /projects/view_by_deadline`

### P5 - Optional features

* [ ] Add `today` command
* [ ] Group deadline view by project
