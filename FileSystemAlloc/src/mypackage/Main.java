package mypackage;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Command command = new Command();
       // System.out.println("commands \n----------------------------------------\n");
        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.println("\nEnter your command:\t(Enter 0 to Terminate)");
            String com = scan.nextLine();
            com += " ";
            if(!command.ManageCommand(com)){
                command.saveToVF();
                break;
            }
        }
    }
}
