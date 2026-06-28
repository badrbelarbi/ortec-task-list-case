package com.ortecfinance.tasklist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Map;

public final class TaskList implements Runnable {
    private static final String QUIT = "quit";

    private static final DateTimeFormatter DEADLINE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final TaskListService taskListService;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Clock clock;

    public static void startConsole() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        new TaskList(in, out).run();
    }

    public TaskList(BufferedReader reader, PrintWriter writer) {
        this(reader, writer, new TaskListService(), Clock.systemDefaultZone());
    }

    public TaskList(BufferedReader reader, PrintWriter writer, TaskListService taskListService) {
        this(reader, writer, taskListService, Clock.systemDefaultZone());
    }

    TaskList(BufferedReader reader, PrintWriter writer, TaskListService taskListService, Clock clock) {
        this.in = reader;
        this.out = writer;
        this.taskListService = taskListService;
        this.clock = clock;
    }

    public void run() {
        out.println("Welcome to TaskList! Type 'help' for available commands.");

        while (true) {
            out.print("> ");
            out.flush();

            String command;
            try {
                command = in.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (command.equals(QUIT)) {
                break;
            }

            execute(command);
        }
    }

    private void execute(String commandLine) {
        String[] commandRest = commandLine.split(" ", 2);
        String command = commandRest[0];

        switch (command) {
            case "show":
                show();
                break;
            case "add":
                add(commandRest[1]);
                break;
            case "check":
                check(commandRest[1]);
                break;
            case "uncheck":
                uncheck(commandRest[1]);
                break;
            case "help":
                help();
                break;
            case "deadline":
                if (commandRest.length < 2) {
                    out.println("Usage: deadline <task ID> <date>");
                } else {
                    deadline(commandRest[1]);
                }
                break;
            case "view-by-deadline":
                viewByDeadline();
                break;
            case "today":
                today();
                break;
            default:
                error(command);
                break;
        }
    }

    private void show() {
        for (Map.Entry<String, List<Task>> project : taskListService.getProjects().entrySet()) {
            out.println(project.getKey());

            for (Task task : project.getValue()) {
                out.printf(
                        "    [%c] %d: %s%n",
                        task.isDone() ? 'x' : ' ',
                        task.getId(),
                        task.getDescription()
                );
            }

            out.println();
        }
    }

    private void add(String commandLine) {
        String[] subcommandRest = commandLine.split(" ", 2);
        String subcommand = subcommandRest[0];

        if (subcommand.equals("project")) {
            addProject(subcommandRest[1]);
        } else if (subcommand.equals("task")) {
            String[] projectTask = subcommandRest[1].split(" ", 2);
            addTask(projectTask[0], projectTask[1]);
        }
    }

    private void addProject(String name) {
        taskListService.addProject(name);
    }

    private void addTask(String project, String description) {
        boolean added = taskListService.addTask(project, description);

        if (!added) {
            out.printf("Could not find a project with the name \"%s\".", project);
            out.println();
        }
    }

    private void check(String idString) {
        setDone(idString, true);
    }

    private void uncheck(String idString) {
        setDone(idString, false);
    }

    private void setDone(String idString, boolean done) {
        long id = Long.parseLong(idString);
        boolean updated = taskListService.setDone(id, done);

        if (!updated) {
            out.printf("Could not find a task with an ID of %d.", id);
            out.println();
        }
    }

    private void deadline(String commandLine) {
        String[] taskDeadline = commandLine.split(" ", 2);

        if (taskDeadline.length < 2) {
            out.println("Usage: deadline <task ID> <date>");
            return;
        }

        long id;
        try {
            id = Long.parseLong(taskDeadline[0]);
        } catch (NumberFormatException e) {
            out.printf("Invalid task ID \"%s\".", taskDeadline[0]);
            out.println();
            return;
        }

        LocalDate deadline;
        try {
            deadline = LocalDate.parse(taskDeadline[1], DEADLINE_FORMATTER);
        } catch (DateTimeParseException e) {
            out.printf("Invalid date \"%s\". Use dd-MM-yyyy.", taskDeadline[1]);
            out.println();
            return;
        }

        boolean updated = taskListService.setDeadline(id, deadline);

        if (!updated) {
            out.printf("Could not find a task with an ID of %d.", id);
            out.println();
        }
    }

    private void viewByDeadline() {
        TaskListService.DeadlineView deadlineView = taskListService.getDeadlineView();

        for (Map.Entry<LocalDate, List<Task>> deadlineGroup : deadlineView.tasksByDeadline().entrySet()) {
            printDeadlineGroup(
                    DEADLINE_FORMATTER.format(deadlineGroup.getKey()),
                    deadlineGroup.getValue()
            );
        }

        if (!deadlineView.tasksWithoutDeadline().isEmpty()) {
            printDeadlineGroup("No deadline", deadlineView.tasksWithoutDeadline());
        }
    }

    private void today() {
        LocalDate today = LocalDate.now(clock);
        List<Task> tasksDueToday = taskListService.getTasksDueOn(today);

        if (tasksDueToday.isEmpty()) {
            out.println("No tasks due today.");
            return;
        }

        printDeadlineGroup("Today", tasksDueToday);
    }

    private void printDeadlineGroup(String title, List<Task> tasks) {
        out.printf("%s:%n", title);

        for (Task task : tasks) {
            out.printf("       %d: %s%n", task.getId(), task.getDescription());
        }
    }

    private void help() {
        out.println("Commands:");
        out.println("  show");
        out.println("  add project <project name>");
        out.println("  add task <project name> <task description>");
        out.println("  check <task ID>");
        out.println("  uncheck <task ID>");
        out.println("  deadline <task ID> <date>");
        out.println("  view-by-deadline");
        out.println("  today");
        out.println();
    }

    private void error(String command) {
        out.printf("I don't know what the command \"%s\" is.", command);
        out.println();
    }
}