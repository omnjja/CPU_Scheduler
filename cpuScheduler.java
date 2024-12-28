import java.io.*;
import java.util.*;
import java.util.function.Function;

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
            Iterator<Process> iterator = readyProcesses.iterator();
            while (iterator.hasNext()) {
                Process p = iterator.next();
                int waitTime = startTime - p.arrivalTime;
                if (waitTime > agingFactor) {
                    waitingQueue.add(p);
                    iterator.remove();
                }
            }
            Process selectedProcess ;
            if (readyProcesses.isEmpty() && !waitingQueue.isEmpty()) {
                selectedProcess = waitingQueue.poll();
            } else {
                selectedProcess = readyProcesses.get(0);
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
            readyProcesses.sort(Comparator.comparingInt(p -> p.remainingTime));//sort ready processes according to remaining time
             //starvation

            Iterator<Process> iterator = readyProcesses.iterator();
            while (iterator.hasNext()) {
                Process p = iterator.next();
                int waitTime = startTime - p.arrivalTime - (p.burstTime - p.remainingTime);
                if (waitTime > agingFactor) {
                    waitingQueue.add(p);
                    iterator.remove();
                }
            }
            Process selectedProcess ;
            if (readyProcesses.isEmpty() && !waitingQueue.isEmpty()) {
                selectedProcess = waitingQueue.poll();
            }
            else {
                selectedProcess = readyProcesses.get(0);
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
//        System.out.println("switch Time : " + switchTime);

    }

    public static void priorityScheduling(List<Process> processes , int contextSwitchTime ) {
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
            startTime += selectedProcess.burstTime+contextSwitchTime;

            executionOrder.add("P" + selectedProcess.id);

            finalProcesses.add(selectedProcess);
            processes.remove(selectedProcess);
        }

        print(finalProcesses);
        System.out.println("\nExecution Order: " + executionOrder);
        System.out.println("Average Waiting Time = " + (float) totalWaiting / finalProcesses.size());
        System.out.println("Average Turnaround Time = " + (float) totalTurnaround / finalProcesses.size());
    }


   public static void fcaiScheduling(List<Process> processes) {
    // Create a copy of the original processes for reference
    List<Process> originalProcesses = new ArrayList<>(processes);

    // Sort processes by arrival time to ensure correct initial order
    processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

    int currentTime = 0, totalWaiting = 0, totalTurnaround = 0;
    List<String> executionOrder = new ArrayList<>();
    List<Process> readyQueue = new LinkedList<>(); // Queue for processes ready to execute
    List<Process> finalProcesses = new ArrayList<>(); // List for completed processes

    int n = processes.size(); // Total number of processes

    // Calculate maximum values for arrival and burst times to determine scaling factors
    double lastArrivalTime = processes.stream().mapToDouble(p -> p.arrivalTime).max().orElse(0);
    double maxBurstTime = processes.stream().mapToDouble(p -> p.burstTime).max().orElse(0);
    double V1 = (double) Math.max(lastArrivalTime / 10.0, 1.0);
    double V2 = (double) Math.max(maxBurstTime / 10.0, 1.0);

    // Define a function to calculate the FCAI factor for prioritization
    Function<Process, Double> calculateFCAIFactor = process ->
            ((10 - process.priority) + Math.ceil(process.arrivalTime / V1) + Math.ceil(process.remainingTime / V2));

    // Main scheduling loop: continues until both process list and ready queue are empty
    while (!processes.isEmpty() || !readyQueue.isEmpty()) {
        // Add processes to the ready queue as they arrive
        while (!processes.isEmpty() && processes.getFirst().arrivalTime <= currentTime) {
            Process arrivingProcess = processes.removeFirst();
            arrivingProcess.fcaiFactor = calculateFCAIFactor.apply(arrivingProcess); // Update FCAI factor
            if (!readyQueue.contains(arrivingProcess)) {
                readyQueue.add(arrivingProcess);
            }
        }

        // Check if the ready queue is not empty for execution
        if (!readyQueue.isEmpty()) {
            Process current = readyQueue.removeFirst();

            // Determine the execution time (minimum of quantum or remaining time)
            int executionTime = Math.min(current.quantum, current.remainingTime);

            // Non-preemptive execution for the first 40% of the quantum
            int nonPreemptiveTime = (int) Math.ceil(current.quantum * 0.4);

            // Execute for 40% of the quantum or until the process finishes
            boolean preempted = false; // Track if the process was preempted
            int actualExecutionTime = 0;

            // Process execution loop
            while (actualExecutionTime < current.quantum && current.remainingTime > 0) {
                // Add process ID to the execution order if it’s not already the last executed
                if (executionOrder.isEmpty() || !executionOrder.getLast().equals(String.valueOf(current.id))) {
                    executionOrder.add(String.valueOf(current.id));
                }

                // Add newly arrived processes to the ready queue
                while (!processes.isEmpty() && processes.getFirst().arrivalTime <= currentTime) {
                    Process arrivingProcess = processes.removeFirst();
                    arrivingProcess.fcaiFactor = calculateFCAIFactor.apply(arrivingProcess);
                    if (!readyQueue.contains(arrivingProcess)) {
                        readyQueue.add(arrivingProcess);
                    }
                }

                // Check for preemption after non-preemptive time has passed
                if (actualExecutionTime >= nonPreemptiveTime && !readyQueue.isEmpty()) {
                    List<Process> tempQueue = new ArrayList<>(readyQueue);
                    tempQueue.sort(Comparator.comparingDouble(p -> p.fcaiFactor));
                    Process nextProcess = tempQueue.getFirst(); // Process with the lowest FCAI factor

                    if (nextProcess.fcaiFactor < current.fcaiFactor) {
                        preempted = true; // Mark process as preempted
                        current.quantum += (current.quantum - actualExecutionTime); // Update remaining quantum
                        readyQueue = tempQueue;
                        readyQueue.remove(current);
                        readyQueue.add(current); // Add current process back to the queue
                        break;
                    }
                }

                currentTime++; // Increment total time by one unit
                current.remainingTime--; // Decrease the remaining burst time
                actualExecutionTime++; // Track the execution time used
            }

            // Handle process completion
            if (current.remainingTime == 0) {
                current.turnaroundTime = currentTime - current.arrivalTime;
                current.waitingTime = current.turnaroundTime - current.burstTime;
                finalProcesses.add(current); // Add completed process to final list
                totalWaiting += current.waitingTime;
                totalTurnaround += current.turnaroundTime;

                System.out.println("\nProcess " + current.id +
                        " Remaining: " + current.remainingTime +
                        " Quantum: " + current.quantum +
                        " FCAI: " + current.fcaiFactor);

                if (current.waitingTime < 0) {
                    current.waitingTime = 0; // Reset negative waiting time to zero (edge case)
                }

            } else {
                // Handle process continuation or preemption
                if (!preempted) {
                    current.quantum += 2; // Increase quantum for continued execution
                }

                current.fcaiFactor = calculateFCAIFactor.apply(current); // Recalculate FCAI factor

                if (!readyQueue.contains(current)) {
                    readyQueue.add(current); // Re-add process to the ready queue
                }
            }
        } else {
            currentTime++; // If no processes are ready, advance time
        }
    }

    // Print final results
    System.out.println("\nExecution Order: " + executionOrder);
    System.out.println("Average Waiting Time = " + (float) totalWaiting / n);
    System.out.println("Average Turnaround Time = " + (float) totalTurnaround / n);

    finalProcesses.sort(Comparator.comparingInt(p -> p.id));
    print(finalProcesses);
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
                System.out.print("Enter aging factor for SJF: ");
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
                processes = inputProcesses(filePath);
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
