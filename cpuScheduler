import java.util.*;

class Process {
    String name;
    int arrivalTime, burstTime, priority, remainingTime;
    int waitingTime, turnaroundTime;

    Process(String name, int arrivalTime, int burstTime, int priority) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingTime = burstTime;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
    }
}

public class CPU_Schudelur {
    static Scanner input = new Scanner(System.in);

    // Helper method to input process data
    public static List<Process> inputProcesses(int numOfProcesses) {
        List<Process> processes = new ArrayList<>();
        for (int i = 0; i < numOfProcesses; i++) {
            System.out.println("Enter details for Process " + (i + 1) + ":");
            System.out.print("Name: ");
            String name = input.next();
            System.out.print("Arrival Time: ");
            int arrivalTime = input.nextInt();
            System.out.print("Burst Time: ");
            int burstTime = input.nextInt();
            System.out.print("Priority: ");
            int priority = input.nextInt();
            processes.add(new Process(name, arrivalTime, burstTime, priority));
        }
        return processes;
    }

// Priority Scheduling (Non-Preemptive)
// Shortest Job First (Non-Preemptive)
// FCAI Scheduling
   

    public static void main(String[] args) {
        System.out.print("Enter number of processes: ");
        int numberOfProcesses = input.nextInt();


        List<Process> processes = inputProcesses(numberOfProcesses);

        System.out.println("\nSelect Scheduler:");
        System.out.println("1. Priority Scheduling");
        System.out.println("2. Shortest Job First (SJF)");
        System.out.println("3. FCAI Scheduling");
        int choice = input.nextInt();

        switch (choice) {
            //case 1 -> priority
            //case 2 -> SJF
            //case 3 -> FCAI
            default -> System.out.println("Invalid choice!");
        }
    }
}
