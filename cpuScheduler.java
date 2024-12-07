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

//            //apply aging: Reduce burst time for processes waiting longer than the threshold
//            for (Process p : readyProcesses) {
//                int waitTime = startTime - p.arrivalTime;
//                if (waitTime > agingThreshold) {
//                    p.burstTime = Math.max(1, p.burstTime - 1); // Reduce burst time but ensure it remains at least 1
//                }
//            }
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
        finalProcesses.sort(Comparator.comparingInt((Process p) -> p.id));
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
        List<Integer> switchTime = new ArrayList<>();

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
            //apply aging: Reduce remaining time for processes waiting longer than the threshold
            for (Process p : readyProcesses) {
                int waitTime = startTime - p.arrivalTime - (p.burstTime - p.remainingTime);
//                if (waitTime > agingThreshold) {
//                    p.remainingTime = Math.max(1, p.remainingTime - 1); // Ensure remainingTime > 0
//                }
            }

            readyProcesses.sort(Comparator.comparingInt(p -> p.remainingTime)); //sort ready processes according to remaining time
            Process selectedProcess = readyProcesses.get(0);

            if (lastProcess != null && lastProcess != selectedProcess) {
                switchTime.add(startTime);
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
        System.out.println("switch Time : " + switchTime);

        //            // Apply aging (reduce remaining time for fairness)
//            for (Process p : processes) {
//                if (p != selectedProcess && p.arrivalTime <= startTime) {
//                    p.remainingTime = Math.max(1, p.remainingTime - 1); //make sure remainingTime > 0
//                }
//            }
    }

    public static void fcaiScheduling(List<Process> processes) {
        int n = processes.size();
        int lastArrivalTime = processes.stream().mapToInt(p -> p.arrivalTime).max().orElse(0);
        int maxBurstTime = processes.stream().mapToInt(p -> p.burstTime).max().orElse(1);
        double V1 = (double) lastArrivalTime / 10.0;
        double V2 = (double) maxBurstTime / 10.0;

        Queue<Process> readyQueue = new LinkedList<>();
        List<String> executionOrder = new ArrayList<>();
        List<Process> finalProcesses = new ArrayList<>();
        int currentTime = 0, totalWaiting = 0, totalTurnaround = 0;

        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        while (!processes.isEmpty() || !readyQueue.isEmpty()) {
            // Add newly arrived processes to the ready queue
            Iterator<Process> iterator = processes.iterator();
            while (iterator.hasNext()) {
                Process p = iterator.next();
                if (p.arrivalTime <= currentTime) {
                    readyQueue.add(p);
                    iterator.remove();
                }
            }

            // If no process is ready, move the time forward
            if (readyQueue.isEmpty()) {
                currentTime = processes.get(0).arrivalTime;
                continue;
            }

            // Calculate FCAI factors for all processes in the ready queue
            for (Process p : readyQueue) {
                p.fcaiFactor = (10 - p.priority) + Math.ceil(p.arrivalTime / V1) + Math.ceil(p.remainingTime / V2);
            }

            // Sort the ready queue by FCAI factor
            List<Process> sortedQueue = new ArrayList<>(readyQueue);
            sortedQueue.sort(Comparator.comparingDouble(p -> p.fcaiFactor));
            Process currentProcess = sortedQueue.get(0);
            readyQueue.remove(currentProcess);

            // Execute the process
            int quantum = currentProcess.quantum;
            int nonPreemptiveTime = (int) Math.ceil(0.4 * quantum);
            int executionTime = Math.min(nonPreemptiveTime, currentProcess.remainingTime);

            currentTime += executionTime;
            currentProcess.remainingTime -= executionTime;

            // Add to execution order
            if (executionOrder.isEmpty() || !executionOrder.get(executionOrder.size() - 1).equals("P" + currentProcess.id)) {
                executionOrder.add("P" + currentProcess.id);
            }

            // Handle process completion or preemption
            if (currentProcess.remainingTime > 0) {
                currentProcess.quantum += (quantum - executionTime); // Update quantum if preempted
                readyQueue.add(currentProcess); // Re-add to queue
            } else {
                // Process completed
                currentProcess.turnaroundTime = currentTime - currentProcess.arrivalTime;
                currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burstTime;
                finalProcesses.add(currentProcess);
            }

            // Debugging information
            System.out.println("\nProcess " + currentProcess.id +
                    " Remaining: " + currentProcess.remainingTime +
                    " Quantum: " + currentProcess.quantum +
                    " FCAI: " + currentProcess.fcaiFactor);
        }

        // Calculate average waiting and turnaround times
        for (Process p : finalProcesses) {
            totalWaiting += p.waitingTime;
            totalTurnaround += p.turnaroundTime;
        }

        // Output results
        System.out.println("\nExecution Order: " + executionOrder);
        System.out.println("Average Waiting Time = " + (float) totalWaiting / n);
        System.out.println("Average Turnaround Time = " + (float) totalTurnaround / n);

        // Sort final processes by ID and print details
        finalProcesses.sort(Comparator.comparingInt(p -> p.id));
        print(finalProcesses);
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

        System.out.println("Choose scheduling algorithm:");
        System.out.println("1. Non-Preemptive SJF");
        System.out.println("2. Preemptive SJF 'SRTF' ");
        System.out.println("3. FCAI ");
        System.out.println("4. Priority Scheduling");
        int contextSwitchTime = 1;
        int agingThreshold = 5;
        int choice = input.nextInt();
        switch (choice) {
            case 1:
                processes = inputProcesses(filePath);
                nonPreemptiveSJF(processes);
                break;
            case 2:
                processes = inputProcesses(filePath);
                SRTF(processes,contextSwitchTime);
                break;
            case 3:
                processes = inputProcesses(filePath);
                print(processes);
                System.out.println("before\n");
//                System.out.print("Enter aging factor for FCAI: ");
//                int agingFactor = input.nextInt();
                fcaiScheduling(processes);
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
