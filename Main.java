package flashcards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/*
 *  This program allow you to 
 *    - Save data to use later for flashcard.
 *    - Remove saved data
 *    - Import data saved in a file (in specific format)
 *    - Export entered data to a file
 *    - Log all details of input and output
 *    - Save your wrong answer cards
 *    - Show details about cards which you answered wrong
 *    - Reset details about wrong cards
 *
 *    It can also take
 *    - file name from command line to upload data (syntax = -import filename)
 *    - file name from command line to save data on exit.  (syntax = -export filename)
 *
 *  @author   - Trapti Tiwari
 *  @email    - traptit1@yahoo.com
 *  @linkedin - https://www.linkedin.com/in/tiwari-trapti/
 */

public class Main {

    public static final Scanner readIp = new Scanner(System.in);

    // Save input data
    public static final Map<String, String> cards = new LinkedHashMap<>();

    // Save wrong card data
    public static final Map<String, Integer> wrongAnswerStats = new LinkedHashMap<>();

    // Save all input/output details
    public static final ArrayList<String> log = new ArrayList<>();

    public static boolean exportAtExit = false;

    public static String exitFileName = "";

    public static void main(String[] args) {

        //  Check if import filename is given and load data 
        //  Check if export filename is given and set parameter to save data at exit
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-import" -> importCards(args[i + 1]);
                    case "-export" -> {
                        exportAtExit = true;
                        exitFileName = args[i + 1];
                    }
                }}
        }

        //  Ask user for action
        while (true) {
            // Menu for possible actions
            System.out.println("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            log.add("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            String action = readIp.nextLine();
            log.add(action);
            
            //  Exit program 
            //  Save data to a file if -export option was given at runtime
            if (action.equals("exit")) {
                System.out.println("Bye bye!");
                log.add("Bye bye!");
                exportCards(exitFileName);
                break;
            }

            switch (action) {
                case "add" -> addCard();
                case "remove" -> removeCard();
                case "import" -> {
                    System.out.println("File name:");
                    log.add("File name:");
                    String fileName = readIp.nextLine();
                    log.add(fileName);importCards(fileName);
                }
                case "export" -> {
                    System.out.println("File name:");
                    log.add("File name:");
                    String fileName = readIp.nextLine();
                    log.add(fileName);
                    exportCards(fileName);
                }
                case "ask" -> askQuestions();
                case "log" -> logData();
                case "hardest card" -> printHardestCard();
                case "reset stats" -> resetStats();
            }

            System.out.println();
            log.add("\n");
        }
    }

    /*
     *  Reset data about wrong cards. 
     */
    private static void resetStats() {
        wrongAnswerStats.clear();
        System.out.println("Card statistics have been reset.");
        log.add("Card statistics have been reset.");
    }

    /*
     *  Print card(s) with maximum number of errors.
     */
    private static void printHardestCard() {
        int maxWrongAnswerCount = 0;

        //  Check what is the maximum number of wrong answers for card
        for (String term : wrongAnswerStats.keySet()) {
            if (wrongAnswerStats.get(term) > maxWrongAnswerCount) {
                maxWrongAnswerCount = wrongAnswerStats.get(term);
            }
        }

        //  Add all terms with max number of wrong answer
        ArrayList<String> wrongTerms = new ArrayList<>();
        for (String term : wrongAnswerStats.keySet()) {
            if (wrongAnswerStats.get(term) == maxWrongAnswerCount) {
                wrongTerms.add(term);
            }
        }

        // Print all terms with max number of wrong answer
        if (wrongTerms.size() == 0) {
            System.out.println("There are no cards with errors.");
            log.add("There are no cards with errors.");
        } else if (wrongTerms.size() == 1) {
            System.out.printf("The hardest card is \"%s\". You have %d errors answering it.\n", wrongTerms.get(0), maxWrongAnswerCount);
            log.add("The hardest card is \"%s\". You have %d errors answering it.\n".formatted(wrongTerms.get(0), maxWrongAnswerCount));
        } else {
            StringBuilder response = new StringBuilder("The hardest cards are ");

            for (String wrongTerm : wrongTerms) {
                response.append("\"%s\", ".formatted(wrongTerm));
            }
            response.replace(response.length() - 2 , response.length(), "");
            response.append(". You have %d errors answering them.\n".formatted(maxWrongAnswerCount));
            System.out.println(response);
            log.add(response.toString());
        }
    }

    /*
     *  Save log details of all input and output to a file
     */
    private static void logData() {
        System.out.println("File name:");
        log.add("File name:");
        String fileName = readIp.nextLine();
        log.add(fileName);

        File file = new File(fileName);

        try (PrintWriter writer = new PrintWriter(file)) {
            System.out.println("The log has been saved.");
            log.add("The log has been saved.");

            for (String line : log) {
                writer.println(line);
            }

        } catch (IOException e) {
            System.out.println("Can't open file");
            log.add("Can't open file");
        }

    }

    /*
     *  Ask questions from data given earlier
     *  If given answer is not correct it shows whether it was correct for another term 
     */
    private static void askQuestions() {
        
        System.out.println("How many times to ask?");
        log.add("How many times to ask?");
        int numberOfQuestions = Integer.parseInt(readIp.nextLine());
        log.add(numberOfQuestions + "");

        //  Get all the terms from saved data
        List<String> terms = cards.keySet().stream().toList();

        //  Loop through to ask question on given number of times
        for (int i = 0; i < numberOfQuestions; i++) {
            String term = terms.get(i);
            System.out.printf("Print the definition of \"%s\":\n", term);
            log.add("Print the definition of \"%s\":\n".formatted(term));
            String answer = readIp.nextLine();
            log.add(answer);

            //  Check actual answer and given answer and out accordingly
            String actualAnswer = cards.getOrDefault(terms.get(i), "");
            if (actualAnswer.equals(answer)) {
                System.out.println("Correct!");
                log.add("Correct!");
            } else {
                wrongAnswerStats.put(term, wrongAnswerStats.getOrDefault(term, 0) + 1);
                String actualTerm = checkForDefinition(answer);
                if (actualTerm.equals("")) {
                    System.out.printf("Wrong. The right answer is \"%s\".\n", actualAnswer);
                    log.add("Wrong. The right answer is \"%s\".\n".formatted(actualAnswer));
                } else {
                    System.out.printf("Wrong. The right answer is \"%s\", but your definition is correct for \"%s\".\n", actualAnswer, actualTerm);
                    log.add("Wrong. The right answer is \"%s\", but your definition is correct for \"%s\".\n".formatted(actualAnswer, actualTerm));
                }
            }
        }

    }

    /*
     *  Check if a definition already exist in data
     *  and return term for which definition exist
     *  or return empty string if definition doesn't exist
     *  @parameter  - definition (String)
     */
    private static String checkForDefinition(String definition) {
        for (String term : cards.keySet()) {
            if (cards.getOrDefault(term, "").equals(definition)) {
                return term;
            }
        }
        return "";
    }

    /*
     *  Save all input data to given file.
     *  Format => term : definition, wrong_answer_count
     *  @parameter  - filename (String)
     */
    private static void exportCards(String fileName) {

        File file = new File(fileName);

        try (PrintWriter writer = new PrintWriter(file)) {
            // save cards to file.
            for (String term : cards.keySet()) {
                writer.println(term.concat(" : ").concat(cards.get(term).concat(", " + wrongAnswerStats.getOrDefault(term, 0))));

            }

            System.out.printf("%d cards have been saved.\n", cards.size());
            log.add("%d cards have been saved.\n".formatted(cards.size()));

        } catch (IOException e) {
            System.out.println("Can't open file.");
            log.add("Can't open file.");
        }
    }

    /*
     *  Load cards data to memory from given file.
     *  @parameter  - filename (String)
     */
    private static void importCards(String fileName) {

        File file = new File(fileName);
        try (Scanner readFile = new Scanner(file)) {
            int numberOfCards = 0;

            // load cards from file.
            while (readFile.hasNextLine()) {
                String[] data = readFile.nextLine().split("[:,]");

                // if a term already in cards data, update definition otherwise add card.
                cards.put(data[0].trim(), data[1].trim());
                wrongAnswerStats.put(data[0].trim(), Integer.parseInt(data[2].trim()));
                numberOfCards++;
            }

            System.out.printf("%d cards have been loaded.\n", numberOfCards);
            log.add("%d cards have been loaded.\n".formatted(numberOfCards));

        }  catch (FileNotFoundException e) {
            System.out.println("File not found.");
            log.add("File not found.");
        }

    }

    /*
     *  Remove a particular card from data.
     *  
     */
    private static void removeCard() {
        System.out.println("Which card?");
        log.add("Which card?");
        String term = readIp.nextLine();
        log.add(term);

        String definition = cards.getOrDefault(term, "");

        if ("".equals(definition)) {
            System.out.printf("Can't remove \"%s\": there is no such card.\n", term);
            log.add("Can't remove \"%s\": there is no such card.\n".formatted(term));
        } else {
            cards.remove(term);
            System.out.println("The card has been removed.");
            log.add("The card has been removed.");
        }
    }

    /*
     *  Add a card to data.
     *  @parameter  - filename (String)
     */
    private static void addCard() {
        System.out.println("The card:");
        log.add("The card:");
        String term = readIp.nextLine();
        log.add(term);

        // If data already contains given term, don't add the term again
        if (cards.containsKey(term)) {
            System.out.printf("The card \"%s\" already exists.\n", term);
            log.add("The card \"%s\" already exists.\n".formatted(term));
        } else {
            System.out.println("The definition of the card:");
            log.add("The definition of the card:");
            String definition = readIp.nextLine();
            log.add(definition);

            //  If definition is already in data for another term, don't add definition
            if (cards.containsValue(definition)) {
                System.out.printf("The definition \"%s\" already exists.\n", definition);
                log.add("The definition \"%s\" already exists.\n".formatted(definition));
            } else {
                // If both term and definition not in data, add a new card
                cards.put(term, definition);
                System.out.printf("The pair (\"%s\":\"%s\") has been added.\n", term, definition);
                log.add("The pair (\"%s\":\"%s\") has been added.\n".formatted(term, definition));
            }
        }
    }
}
