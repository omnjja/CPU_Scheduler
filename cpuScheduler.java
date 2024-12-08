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
    int quantum;
    double fcaiFactor;

    Process(int id, int arrivalTime, int burstTime, int priority, int quantum) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingTime = burstTime;
        this.quantum = quantum;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.fcaiFactor = 0.0;
    }
}

public class Main {
    static Scanner input = new Scanner(System.in);

    public static List<Process> inputProcesses(String filePath) {
        List<Process> processes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s+"); // Split by whitespace
                int id = Integer.parseInt(parts[0]); // PID
                int arrivalTime = Integer.parseInt(parts[1]); // Arrival Time
                int burstTime = Integer.parseInt(parts[2]); // Burst Time
                int priority = Integer.parseInt(parts[3]); // Priority
                int quantum = Integer.parseInt(parts[4]); // Quantum
                processes.add(new Process(id, arrivalTime, burstTime, priority, quantum));
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return processes;
    }

    public static void nonPreemptiveSJF(List<Process> processes, int agingFactor) {
        processes.sort(Comparator.comparingInt((Process p) -> p.arrivalTime));
        List<Process> finalProcesses = new ArrayList<>();
        List<String> executionOrder = new ArrayList<>();
        Queue<Process> waitingQueue = new LinkedList<>();

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
            //starvation
            for (Process p : readyProcesses) {
                int waitTime = startTime - p.arrivalTime ;
                if (waitTime > agingFactor) {
                    waitingQueue.add(p);
                }
            }
            Process selectedProcess ;
            readyProcesses.sort(Comparator.comparingInt(p -> p.remainingTime)); //sort ready processes according to remaining time
            if (waitingQueue.isEmpty()) {
                selectedProcess = readyProcesses.get(0);
            }
            else {
                selectedProcess = waitingQueue.poll();
            }

            selectedProcess.waitingTime = startTime - selectedProcess.arrivalTime;
            selectedProcess.turnaroundTime = selectedProcess.waitingTime + selectedProcess.burstTime;
            totalWaiting += selectedProcess.waitingTime;
            totalTurnaround += selectedProcess.turnaroundTime;
            startTime += selectedProcess.burstTime;

            executionOrder.add("P" + selectedProcess.id);

            finalProcesses.add(selectedProcess);
            processes.remove(selectedProcess);
        }
        finalProcesses.sort(Comparator.comparingInt((Process p) -> p.id));
        print(finalProcesses);
        System.out.println("\nExecution Order: " + executionOrder);
        System.out.println("Average Waiting Time = " + (float) totalWaiting / executionOrder.size());
        System.out.println("Average Turnaround Time = " + (float) totalTurnaround / executionOrder.size());
    }


    public static void SRTF(List<Process> processes, int contextSwitchTime, int agingFactor) {
        int n = processes.size();
        processes.sort(Comparator.comparingInt((Process p) -> p.arrivalTime));
        List<Process> finalProcesses = new ArrayList<>();
        List<String> executionOrder = new ArrayList<>();
        List<Integer> switchTime = new ArrayList<>();
        Queue<Process> waitingQueue = new LinkedList<>();

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
            readyProcesses.sort(Comparator.comparingInt(p -> p.remainingTime));
             //starvation
            for (Process p : readyProcesses) {
                int waitTime = startTime - p.arrivalTime - (p.burstTime - p.remainingTime);
                if (waitTime > agingFactor) {
                    waitingQueue.add(p);
                }
            }
            Process selectedProcess ;
            readyProcesses.sort(Comparator.comparingInt(p -> p.remainingTime)); //sort ready processes according to remaining time
            if (waitingQueue.isEmpty()) {
                selectedProcess = readyProcesses.get(0);
            }
            else {
                selectedProcess = waitingQueue.poll();
            }
            if (lastProcess != null && lastProcess != selectedProcess) {
                switchTime.add(startTime);
                startTime += contextSwitchTime;
            }

            //log execution order if switching to a new process
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
        System.out.println("switch Time : " + switchTime);

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
        System.out.println("P\tBT\tAT\tP\tQ\tWT\tTAT");
        for (Process p : processes) {
            System.out.println("P" + p.id + "\t" + p.burstTime + "\t" + p.arrivalTime + "\t" + p.priority + "\t" + p.quantum + "\t" + p.waitingTime + "\t" + p.turnaroundTime);
        }
    }

    public static void main(String[] args)
    {
        String filePath = "input.txt"; // Specify the file path
        List<Process> processes;
        int agingFactor;
        System.out.println("Choose scheduling algorithm:");
        System.out.println("1. Non-Preemptive SJF");
        System.out.println("2. Preemptive SJF 'SRTF' ");
        System.out.println("3. FCAI ");
        System.out.println("4. Priority Scheduling");
        int choice = input.nextInt();
        switch (choice) {
            case 1:
                System.out.print("Enter aging factor for SRTF: ");
                 agingFactor = input.nextInt();
                processes = inputProcesses(filePath);
                nonPreemptiveSJF(processes, agingFactor);
                break;
            case 2:
                System.out.print("Enter context switch time for SRTF: ");
                int contextSwitchTime = input.nextInt();
                System.out.print("Enter aging factor for SRTF: ");
                agingFactor = input.nextInt();
                processes = inputProcesses(filePath);
                SRTF(processes,contextSwitchTime,agingFactor);
                break;
            case 3:
                //fcai
                break;
            case 4:
                processes = inputProcesses(filePath);
                priorityScheduling(processes);
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }
}
