package assembler;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.*;

/**
 * Assembler
 * Trevor Loula
 * Instructor: Dr. Fang
 * Class: EGCP 3210 Computer Architecture
 * 
 * @author Trevor Loula
 */
public class Assembler {
    
    static private ArrayList<Pointer> Pointers = new ArrayList<Pointer>();                     // List containing all the pointers
    static private ArrayList<Instruction> Instructions = new ArrayList<Instruction>();         // List containing all the instructions
    static private ArrayList<MemoryContent> MemoryContents = new ArrayList<MemoryContent>();   // List containing all the memory addresses and their contents, will later be sorted
    
    // Save end of line separator for future use
    static String eol = System.getProperty("line.separator");
    
    /**
    * First pass reads input file into two arrays, one containing all the pointers and the other containing all the instructions
    *
    * @param fileString Input string containing file to be assembled
    * @throws AssemblerException
    */
    public static void PassOne (String fileString){
        String address = null;     // Based of any ORG instructions and then automatically incremented
        String label = null;       // First three characters of the line preceding comma
        String instruction = null; // Position 5-7 in the line
        String reference = null;   // If a memory reference instruction, position 9-11
        String indirect;           // Position 13
        boolean isORG = false;
        
        // Check for null fileString
        if ("".equals(fileString)){
            throw new AssemblerException("Null Input");
        }
        
        try (Scanner file = new Scanner(fileString)) {
            while (file.hasNextLine()) {
                String nextLine = file.nextLine();
                // Check if the line contains a symbol
                boolean isPointer = false;
                if (nextLine.indexOf(',') > 0){
                    isPointer = true;
                }
                nextLine = nextLine.replace(",", "");
                int stop = 3;
                // Cut off comments from the line
                if(nextLine.indexOf('/') > 0){
                    stop = nextLine.indexOf('/');
                } else {
                    stop = nextLine.length();
                }
                try (Scanner ScannerLine = new Scanner(nextLine.substring(0, stop))){
                    label = null;
                    instruction = null;
                    reference = null;
                    indirect = null;
                    
                    // If line declares a pointer, save pointer
                    if(isPointer){
                        label = ScannerLine.next("[A-Z0-9]+");
                    }
                    // Save all instructions
                    if(ScannerLine.hasNext("[A-Z0-9]+")){
                        instruction = ScannerLine.next("[A-Z0-9]+");
                    }
                    if(ScannerLine.hasNext("[A-Z0-9-]+")){
                        reference = ScannerLine.next("[A-Z0-9-]+");
                    }
                    if(ScannerLine.hasNext("[A-Z0-9]+")){
                        indirect = ScannerLine.next("[A-Z0-9]+");
                    }
                } catch (java.util.InputMismatchException e) {
                    throw new AssemblerException(e.getMessage());
                }
                // Get starting address
                if ("ORG".equals(instruction)){
                    // Initialize address
                    address = reference;
                    isORG = true;
                } else {
                    // If the address hasn't been initialized, throw exception
                    if (address == null){
                        throw new AssemblerException("Must Specify an ORG");
                    }
                    if (!isORG){
                        // Increment the address
                        int DecAddress = Integer.parseInt(address, 16);
                        DecAddress++;
                        address = Integer.toHexString(DecAddress);
                    }
                    isORG = false;
                    // Convert all decimals to hex
                    if("DEC".equals(instruction)){
                        // If the decimal is negative, remove extra ffff's to make reference four bits
                        boolean isNegative = false;
                        if ("-".equals(reference.substring(0, 1))){
                            isNegative = true;
                        }
                        reference = Integer.toHexString(Integer.parseInt(reference));
                        if (isNegative){
                            reference = reference.substring(reference.length()-4, reference.length());
                        }
                    }
                    // Ensure that any numbers are four bits  
                    if (IsNumber(reference)){
                        while(reference.length() < 4){
                            reference = "0" + reference;
                        }
                    }
                    // If instruction is end, break loop
                    if ("END".equals(instruction)){
                        break;
                    }
                    // If label has been initialized, then the line refers to a Pointer
                    if (label != null){
                        // Add a new Pointer to the list
                        Pointers.add(new Pointer(address, label));
                    }
                    // If instruction is not a number, save it for later
                    if ((!"DEC".equals(instruction)) && (!"HEX".equals(instruction))) {
                        if ((instruction != null) || (reference != null)){
                            Instructions.add(new Instruction(address, instruction, reference, indirect));
                        // Throw an error for an invalid line, but ignore blank lines
                        } else if (!"".equals(nextLine) && (nextLine.trim().length() > 0)){
                            throw new AssemblerException("Invalid Line: '" + nextLine + "'");
                        }
                    }
                    // If it's just a value, put it directly in memory
                    if (("DEC".equals(instruction)) || ("HEX".equals(instruction))){
                        if (reference != null){
                            MemoryContents.add(new MemoryContent(address, reference));
                        // Throw an error for an invalid line, but ignore blank lines
                        } else if ((!"".equals(nextLine)) && (nextLine.trim().length() > 0)){
                            throw new AssemblerException("Invalid Line: '" + nextLine + "'");
                        }
                    }
                }
            }
        } catch (java.util.InputMismatchException e) {
            throw new AssemblerException(e.getMessage());
        }
    }
    
    /**
    * Uses the two ArrayLists - Pointers and Instructions - to create build the memory contents
    * Stores final memory contents into arraylist sorted by address
    *
    * @throws AssemblerException
    */
    public static void PassTwo (){
        // Process each instruction
        for(Instruction Instruction : Instructions) {
            // Ignore ORG, these are already handled in pass one
            if ("ORG".equals(Instruction.getInstruction())){
                continue;
            }
            // Merges the instructions opcode (5xxx) with the memory address it effects, and then adds the final command to the Memory contents pqueue
            switch (Instruction.getInstruction()){
                case "AND":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), mergeOpcode("0", "8", Instruction.getReference(), Instruction.getIndirect())));
                    break;
                case "ADD":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), mergeOpcode("1", "9", Instruction.getReference(), Instruction.getIndirect())));
                    break;
                case "LDA":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), mergeOpcode("2", "A", Instruction.getReference(), Instruction.getIndirect())));
                    break;
                case "STA":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), mergeOpcode("3", "B", Instruction.getReference(), Instruction.getIndirect())));
                    break;
                case "BUN":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), mergeOpcode("4", "C", Instruction.getReference(), Instruction.getIndirect())));
                    break;
                case "BSA":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), mergeOpcode("5", "D", Instruction.getReference(), Instruction.getIndirect())));
                    break;
                case "ISZ":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), mergeOpcode("6", "E", Instruction.getReference(), Instruction.getIndirect())));
                    break;
                case "CLA":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), "7800"));
                    break;
                case "CLE":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), "7400"));
                    break;
                case "CMA":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), "7200"));
                    break;
                case "CME":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), "7100"));
                    break;
                case "CIR":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), "7080"));
                    break;
                case "CIL":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), "7040"));
                    break;
                case "INC":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), "7020"));
                    break;
                case "SPA":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), "7010"));
                    break;
                case "SNA":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), "7008"));
                    break;
                case "SZA":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), "7004"));
                    break;
                case "SZE":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), "7002"));
                    break;
                case "HLT":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), "7001"));
                    break;
                case "INP":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), "F800"));
                    break;
                case "OUT":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), "F400"));
                    break;
                case "SKI":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), "F200"));
                    break;
                case "SKO":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), "F100"));
                    break;
                case "ION":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), "F080"));
                    break;
                case "IOF":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), "F040"));
                    break;
                /*case "DEC":
                case "HEX":
                    MemoryContents.add(new MemoryContent(Instruction.getAddress(), Instruction.getReference()));
                    break;*/
                case "END":
                    break;
                default:
                    throw new AssemblerException("Invalid Instruction: '" + Instruction.getInstruction() + "'");
            }
        }
    }
    
    /**
    * Runs pass one and pass 2 and then returns the memory contents
    *
    * @param file string containing file to be assembled
    */
    public static void Assemble (String file){
        // Clear static variables from possible previous runs
        Pointers.clear();
        Instructions.clear();
        MemoryContents.clear();
        
        PassOne(file);  // Run first pass
        PassTwo();      // Run second pass
        
        // Sort memory array by memory address
        Collections.sort(MemoryContents, MemoryContent.MemoryComparator);  
    }
    /**
    * Saves memory contents and pointers to individual text files
    * Intentionally a separate function so that the main Assemble() function
    * can still complete even if writing to files throws an exception
    */
    public static void saveFiles(){
        // Save assembled code to file
        try {
            PrintWriter out = new PrintWriter("memory.txt");
            out.println(getCode());
            out.close();
        } catch (FileNotFoundException e){
            throw new AssemblerException("Error writing to file: memory.txt");
        }
        // Save symbols to file
        try {
            PrintWriter out = new PrintWriter("symbols.txt");
            out.println(getPointers());
            out.close();
        } catch (FileNotFoundException e){
            throw new AssemblerException("Error writing to file: symbols.txt");
        }
    }
    
    public static String getCode(){
        // Print out memory contents to string
        String AssembledCode = "";
        for(MemoryContent MemoryContent : MemoryContents){
            AssembledCode += MemoryContent.getAddress() + ": " + MemoryContent.getContents() + eol;
        }
        return AssembledCode;
    }
     
    public static String getPointers(){
        // Print out pointers to string
        String PointerList = "";
        for(Pointer Pointer : Pointers){
            PointerList += Pointer.getLabel() + ": " + Pointer.getAddress() + eol;
        }
        return PointerList;
    }
    
    /**
    * Takes in a code like like 5xxx and replaces xxx with the pointers value
    *
    * @param code The instruction code for direct memory referencing (0xxx)
    * @param ICode The instruction code for indirect memory referencing (8xxx)
    * @param reference The pointer being referenced
    * @return int containing the memory contents
    * @throws AssemblerException
    */
    public static String mergeOpcode(String code, String ICode, String reference, String indirect){        
        // Find the pointer in the list of Pointers and get the address
        for(Pointer Pointer : Pointers){
            if(Pointer.getLabel().equals(reference)){
                if("I".equals(indirect)){
                    return ICode + Pointer.getAddress();
                } else {
                    return code + Pointer.getAddress();
                }
            }
        }
        throw new AssemblerException("Invalid Pointer: '" + reference + "'");
    }
    
    /**
    * Reads the input file to a string
    *
    * @param filename Name of the file to open and read
    * @return inputFile String containing the entire file to be assembled
    * @throws AssemblerException
    */
    public static String ReadFile (String filename) {
        String line = null;
        String inputFile = "";
        
        try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                inputFile += line + eol;
            }
            bufferedReader.close();
        }
        catch(FileNotFoundException e) {
            throw new AssemblerException("Unable To Open File: '" + filename + "'");              
        } catch(IOException e) {
            throw new AssemblerException("Error Reading File: '" + filename + "'");            
        }
        return inputFile;
    }
    
    /**
    * Checks if an inputted string contains a number
    *
    * @param str String to be analyzed
    * @return true if string is a number
    */
    public static boolean IsNumber (String str){
        try {
            Integer.parseInt(str, 16);
        } catch (NumberFormatException e){
            return false;
        }
        return true;
    }
    /**
    * Returns about program string
    *
    * @return about program string
    */
    public static String about(){
        String about = ""
                + "Author: Trevor Loula" + eol
                + "Instructor: Dr. Fang" + eol
                + "Class: EGCP 3210 Computer Architecture" + eol
                + "Assembler Project" + eol
                + "Created in  Apache NetBeans IDE 10.0 using Java 11.0.1" + eol + eol
                
                + "--------------------Instructions--------------------" + eol
                
                + "Choose File Button:" + eol
                + "Opens a file chooser to select file, " + eol
                + "then automatically fills the filename field, loads the file," + eol
                + "and assembles the program." + eol + eol
                
                + "Filename Input:" + eol
                + "Area to manually enter filename of program." + eol
                + "Can enter name of file in root directory or a file path." + eol + eol
                
                + "Load File Button:" + eol
                + "Loads the file specified by the filename input textbox" + eol
                + "into the loaded file textarea." + eol + eol
                
                + "Assemble Program Button:" + eol
                + "Assembles the program currently loaded into the Loaded File textarea." + eol
                + "Creates memory.txt and symbols.txt files." + eol + eol
                
                + "Exit Button: Closes the Java Assembler application.";
        return about;
    }
}