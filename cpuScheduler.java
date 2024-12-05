import java.io.*;
import java.util.*;

class Process {
    int id;
    int burstTime;
    int arrivalTime;
    int priority;
    int remainingTime;
    int waitingTime;
    int turnaroundTime;

    Process(int id, int arrivalTime, int burstTime, int priority) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingTime = burstTime;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
    }
}

public class Main {
    static Scanner input = new Scanner(System.in);

    public static List<Process> inputProcesses() {
        System.out.println("Enter number of processes:");
        int numOfProcesses = input.nextInt();
        List<Process> processes = new ArrayList<>();
        for (int i = 0; i < numOfProcesses; i++) {
            System.out.println("Enter details for Process " + (i + 1) + ":");
            System.out.print("Process ID: ");
            int id = input.nextInt();
            System.out.print("Arrival Time: ");
            int arrivalTime = input.nextInt();
            System.out.print("Burst Time: ");
            int burstTime = input.nextInt();
            System.out.print("Priority: ");
            int priority = input.nextInt();
            processes.add(new Process(id, arrivalTime, burstTime, priority));
        }
        return processes;
    }

    public static void nonPreemptiveSJF(List<Process> processes) {
        processes.sort(Comparator.comparingInt((Process p) -> p.arrivalTime));
        List<Process> finalProcesses = new ArrayList<>();
        List<String> executionOrder = new ArrayList<>();

        int startTime = 0, totalWaiting = 0, totalTurnaround = 0;
        while (!processes.isEmpty()) {
            List<Process> readyProcesses = new ArrayList<>();
            for (Process p : processes) {   //put the arrived processes in ready list
                if (p.arrivalTime <= startTime) {
                    readyProcesses.add(p);
                }
            }
            if (readyProcesses.isEmpty()) {     //if there is no pro assign start time to first arrived pro
                startTime = processes.get(0).arrivalTime;
                continue;
            }

            readyProcesses.sort(Comparator.comparingInt(p -> p.burstTime)); //sort ready processes by burst time
            Process selectedProcess = readyProcesses.get(0);

            selectedProcess.waitingTime = startTime - selectedProcess.arrivalTime;
            selectedProcess.turnaroundTime = selectedProcess.waitingTime + selectedProcess.burstTime;
            totalWaiting += selectedProcess.waitingTime;
            totalTurnaround += selectedProcess.turnaroundTime;
            startTime += selectedProcess.burstTime;

            executionOrder.add("P" + selectedProcess.id);

            finalProcesses.add(selectedProcess);
            processes.remove(selectedProcess);
        }
        print(finalProcesses);
        System.out.println("\nExecution Order: " + executionOrder);
        System.out.println("Average Waiting Time = " + (float) totalWaiting / executionOrder.size());
        System.out.println("Average Turnaround Time = " + (float) totalTurnaround / executionOrder.size());
    }


    public static void SRTF(List<Process> processes, int contextSwitchTime) {
        int n = processes.size();
        processes.sort(Comparator.comparingInt((Process p) -> p.arrivalTime));
        List<Process> finalProcesses = new ArrayList<>();
        List<String> executionOrder = new ArrayList<>();

        int startTime = 0, totalWaiting = 0, totalTurnaround = 0;
        Process lastProcess = null;
        while (!processes.isEmpty()) {
            List<Process> readyProcesses = new ArrayList<>();
            for (Process p : processes) {   //put the arrived processes in ready list
                if (p.arrivalTime <= startTime) {
                    readyProcesses.add(p);
                }
            }
            if (readyProcesses.isEmpty()) {     //if there is no pro assign start time to first arrived pro
                startTime = processes.get(0).arrivalTime;
                continue;
            }

            readyProcesses.sort(Comparator.comparingInt(p -> p.remainingTime)); //sort ready processes according to remaining time
            Process selectedProcess = readyProcesses.get(0);

            if (lastProcess != null && lastProcess != selectedProcess) {
                startTime += contextSwitchTime;
            }

            // Log execution order if switching to a new process
            if (executionOrder.isEmpty() || !executionOrder.get(executionOrder.size() - 1).equals("P" + selectedProcess.id)) {
                executionOrder.add("P" + selectedProcess.id);
            }

            selectedProcess.remainingTime--;
            startTime++;

            if (selectedProcess.remainingTime == 0) {
                selectedProcess.turnaroundTime = startTime - selectedProcess.arrivalTime;
                selectedProcess.waitingTime = selectedProcess.turnaroundTime - selectedProcess.burstTime;

                totalWaiting += selectedProcess.waitingTime;
                totalTurnaround += selectedProcess.turnaroundTime;

                finalProcesses.add(selectedProcess);
                processes.remove(selectedProcess);
            }
            lastProcess = selectedProcess;
        }
        finalProcesses.sort(Comparator.comparingInt((Process p) -> p.id));
        print(finalProcesses);
        System.out.println("\nExecution Order: " + executionOrder);
        System.out.println("Average Waiting Time = " + (float) totalWaiting / n);
        System.out.println("Average Turnaround Time = " + (float) totalTurnaround / n);

        //            // Apply aging (reduce remaining time for fairness)
//            for (Process p : processes) {
//                if (p != selectedProcess && p.arrivalTime <= startTime) {
//                    p.remainingTime = Math.max(1, p.remainingTime - 1); //make sure remainingTime > 0
//                }
//            }
    }


    public static void fcaiScheduling(List<Process> processes, int agingFactor) {
    // Sort processes initially by arrival time
    processes.sort(Comparator.comparingInt((Process p) -> p.arrivalTime));
    List<Process> finalProcesses = new ArrayList<>();
    List<String> executionOrder = new ArrayList<>();

    int startTime = 0, totalWaiting = 0, totalTurnaround = 0;

    while (!processes.isEmpty()) {
        // List of processes that are ready
        List<Process> readyProcesses = new ArrayList<>();
        for (Process p : processes) {
            if (p.arrivalTime <= startTime) {
                readyProcesses.add(p);
            }
        }

        // If no process is ready, move to the next arrival time
        if (readyProcesses.isEmpty()) {
            startTime = processes.get(0).arrivalTime;
            continue;
        }

        // Apply aging: Increase the priority of waiting processes
        for (Process p : readyProcesses) {
            p.priority -= agingFactor; // Lower number = higher priority
        }

        // Sort ready processes by dynamic priority, then by arrival time
        readyProcesses.sort(Comparator.comparingInt((Process p) -> p.priority)
                .thenComparingInt(p -> p.arrivalTime));

        // Select the process with the highest adjusted priority
        Process selectedProcess = readyProcesses.get(0);

        // Execute the selected process
        selectedProcess.waitingTime = startTime - selectedProcess.arrivalTime;
        selectedProcess.turnaroundTime = selectedProcess.waitingTime + selectedProcess.burstTime;
        totalWaiting += selectedProcess.waitingTime;
        totalTurnaround += selectedProcess.turnaroundTime;
        startTime += selectedProcess.burstTime;

        // Log execution order
        executionOrder.add("P" + selectedProcess.id);

        // Add the executed process to the final list and remove it from the original list
        finalProcesses.add(selectedProcess);
        processes.remove(selectedProcess);
    }

    // Print the results
    print(finalProcesses);
    System.out.println("\nExecution Order: " + executionOrder);
    System.out.println("Average Waiting Time = " + (float) totalWaiting / finalProcesses.size());
    System.out.println("Average Turnaround Time = " + (float) totalTurnaround / finalProcesses.size());
}


public static void priorityScheduling(List<Process> processes) {
    processes.sort(Comparator.comparingInt((Process p) -> p.arrivalTime));
    List<Process> finalProcesses = new ArrayList<>();
    List<String> executionOrder = new ArrayList<>();

    int startTime = 0, totalWaiting = 0, totalTurnaround = 0;
    while (!processes.isEmpty()) {
        List<Process> readyProcesses = new ArrayList<>();
        for (Process p : processes) {   // Add arrived processes to the ready list
            if (p.arrivalTime <= startTime) {
                readyProcesses.add(p);
            }
        }

        if (readyProcesses.isEmpty()) { // No ready process, advance time to next arrival
            startTime = processes.get(0).arrivalTime;
            continue;
        }

        // Sort ready processes by priority (lower priority value = higher priority)
        readyProcesses.sort(Comparator.comparingInt(p -> p.priority));
        Process selectedProcess = readyProcesses.get(0);

        selectedProcess.waitingTime = startTime - selectedProcess.arrivalTime;
        selectedProcess.turnaroundTime = selectedProcess.waitingTime + selectedProcess.burstTime;
        totalWaiting += selectedProcess.waitingTime;
        totalTurnaround += selectedProcess.turnaroundTime;
        startTime += selectedProcess.burstTime;

        executionOrder.add("P" + selectedProcess.id);

        finalProcesses.add(selectedProcess);
        processes.remove(selectedProcess);
    }

    print(finalProcesses);
    System.out.println("\nExecution Order: " + executionOrder);
    System.out.println("Average Waiting Time = " + (float) totalWaiting / finalProcesses.size());
    System.out.println("Average Turnaround Time = " + (float) totalTurnaround / finalProcesses.size());
}



    public static void print(List<Process> processes) {
        System.out.println("P\tBT\tAT\tWT\tTAT");
        for (Process p : processes) {
            System.out.println("P" + p.id + "\t" + p.burstTime + "\t" + p.arrivalTime + "\t" + p.waitingTime + "\t" + p.turnaroundTime);
        }
    }

    public static void main(String[] args)
    {
        List<Process> processes;
        System.out.println("Choose scheduling algorithm:");
        System.out.println("1. Non-Preemptive SJF");
        System.out.println("2. Preemptive SJF");
        System.out.println("3. Preemptive SJF 'SRTF'");
        System.out.println("4. FCAI ");
        System.out.println("5. Priority Scheduling");
        int contextSwitchTime = 1;
        int choice = input.nextInt();
        switch (choice) {
            case 1:
                //
                break;
            case 2:
                processes = inputProcesses();
                nonPreemptiveSJF(processes);
                break;
            case 3:
                processes = inputProcesses();
                SRTF(processes,contextSwitchTime);
                break;
            case 4:
                processes = inputProcesses();
                System.out.print("Enter aging factor for FCAI: ");
                int agingFactor = input.nextInt();
                fcaiScheduling(processes, agingFactor);
                break;
            case 5:
               processes = inputProcesses();
               priorityScheduling(processes);
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }
}
