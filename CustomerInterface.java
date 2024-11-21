/**
 * CustomerInterface.java
 * @author Minh Long Hang
 * CIS 22C, Applied Lab 3
 */
import java.io.*;
import java.util.Scanner;


public class CustomerInterface {
    public static void main(String[] args) {
        try {
            BST<MutualFundAccount> accountsByValue = new BST<>();
            BST<MutualFundAccount> accountsByName = new BST<>();
            LinkedList<MutualFund> funds = new LinkedList<>();
            
            // Read mutual funds from file
            File file = new File("mutual_funds.txt");
            Scanner input = new Scanner(file);
            while(input.hasNextLine()) {
                String name = input.nextLine();
                String ticker = input.nextLine();
                double sharePrice = Double.parseDouble(input.nextLine());
                funds.addLast(new MutualFund(name, ticker, sharePrice));
            }
            input.close();
            
            Scanner scanner = new Scanner(System.in);
            String choice;
            
            System.out.println("Welcome to Mutual Fund InvestorTrack (TM)!\n");
            
            do {
                System.out.println("Please select from the following options:\n");
                System.out.println("A. Purchase a Fund");
                System.out.println("B. Sell a Fund");
                System.out.println("C. Display Your Current Funds");
                System.out.println("X. Exit\n");
                System.out.print("Enter your choice: ");
                
                // Check if there is a next token to read
                if (!scanner.hasNextLine()) {
                    System.out.println("No input available. Exiting.");
                    break;
                }
                
                choice = scanner.next().toUpperCase();
                System.out.println(); 
                
                switch(choice) {
                    case "A":
                        purchaseFund(funds, accountsByName, accountsByValue, scanner);
                        break;
                        
                    case "B":
                        sellFund(accountsByName, accountsByValue, scanner);
                        break;
                        
                    case "C":
                        displayFunds(accountsByName, accountsByValue, scanner);
                        break;
                        
                    case "X":
                        System.out.println("Goodbye!");
                        break;
                        
                    default:
                        System.out.println("Invalid menu option. Please enter A-C or X to exit.\n");
                }
                
            } while(!choice.equals("X"));
            
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: The file 'mutual_funds.txt' was not found.");
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
    
    private static void purchaseFund(LinkedList<MutualFund> funds, 
                                  BST<MutualFundAccount> accountsByName, 
                                  BST<MutualFundAccount> accountsByValue, 
                                  Scanner scanner) {
    displayFundOptions(funds);
    System.out.print("Enter your choice: (1-" + funds.getLength() + "): ");
    int fundChoice = Integer.parseInt(scanner.next());
    
    if (fundChoice >= 1 && fundChoice <= funds.getLength()) {
        System.out.print("\nEnter the number of shares to purchase: ");
        double shares = Double.parseDouble(scanner.next());
        
        // Find the selected fund
        funds.positionIterator();
        for (int i = 1; i < fundChoice; i++) {
            funds.advanceIterator();
        }
        MutualFund selectedFund = funds.getIterator();

        // Check if account already exists
        MutualFundAccount tempAccount = new MutualFundAccount(selectedFund);
        MutualFundAccount existingAccount = accountsByName.search(tempAccount, new NameComparator());

        if (existingAccount != null) {
            // Update existing account shares
            existingAccount.updateShares(shares);
        } else {
            // Create new account if it does not exist
            MutualFundAccount newAccount = new MutualFundAccount(selectedFund, shares);
            accountsByName.insert(newAccount, new NameComparator());
            accountsByValue.insert(newAccount, new ValueComparator());
        }
    } else {
        System.out.println("Invalid choice!\n");
        }
    }

    private static void sellFund(BST<MutualFundAccount> accountsByName, 
                              BST<MutualFundAccount> accountsByValue, 
                              Scanner scanner) {
    if (accountsByName.isEmpty()) {
        System.out.println("You don't have any funds to sell at this time.\n");
        return;
    }
    
    System.out.print("You own the following mutual funds:");
    System.out.print(accountsByName.inOrderString());

    System.out.print("Enter the name of the fund to sell: ");
    scanner.nextLine(); // Consume newline
    String fundName = scanner.nextLine();

    System.out.print("Enter the number of shares to sell or \"all\" to sell everything: ");
    String sellAmount = scanner.next();

    MutualFundAccount temp = new MutualFundAccount(new MutualFund(fundName));
    MutualFundAccount account = accountsByName.search(temp, new NameComparator());

    if (account == null) {
        System.out.println("No account found for the fund: " + fundName + "\n");
        return;
    }

    double sharesToSell;
    if (sellAmount.equalsIgnoreCase("all")) {
        sharesToSell = account.getNumShares();
    } else {
        try {
            sharesToSell = Double.parseDouble(sellAmount);
            if (sharesToSell <= 0) {
                System.out.println("Invalid amount! Must be greater than 0.\n");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter a valid number or \"all\".\n");
            return;
        }
    }

    if (sharesToSell > account.getNumShares()) {
        System.out.println("You cannot sell more shares than you own.\n");
        return;
    } else {
        // Update the number of shares
        account.updateShares(-sharesToSell);
        
        // Check if shares are now zero
        if (account.getNumShares() == 0) {
            accountsByName.remove(account, new NameComparator());
            accountsByValue.remove(account, new ValueComparator());
        } else {
            // Remove the account from both trees
            accountsByName.remove(account, new NameComparator());
            accountsByValue.remove(account, new ValueComparator());

            // Re-insert the updated account to both trees
            accountsByName.insert(account, new NameComparator());
            accountsByValue.insert(account, new ValueComparator());
        }
    }
}
    private static void displayFunds(BST<MutualFundAccount> accountsByName, BST<MutualFundAccount> accountsByValue, Scanner scanner) {
        if (accountsByName.isEmpty()) {
            System.out.println("You don't have any funds to display at this time.\n");
        } else {
            System.out.println("View Your Mutual Funds By:\n");
            System.out.println("1. Name");
            System.out.println("2. Value\n");
            System.out.print("Enter your choice (1 or 2): ");
            String displayChoice = scanner.next();
            
            if (displayChoice.equals("1")) {
                System.out.print(accountsByName.inOrderString()); // Ascending by name
            } else if (displayChoice.equals("2")) {
                System.out.print(accountsByValue.inOrderString()); // Ascending by value
            } else {
                System.out.println("Invalid Choice!\n");
            }
        }
    }
    
    private static void displayFundOptions(LinkedList<MutualFund> funds) {
        System.out.println("Please select from the options below:\n");
        int count = 1;
        funds.positionIterator();
        
        while (!funds.offEnd()) {
            MutualFund fund = funds.getIterator();
            // Assuming MutualFund has getName(), getTicker(), and getSharePrice() methods
            System.out.printf("%d. %s\n%s\nShare Price: $%.2f\n", 
                              count, fund.getFundName(), fund.getTicker(), fund.getPricePerShare());
            count++;
            funds.advanceIterator();
        }
    }
}