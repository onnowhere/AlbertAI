import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;
import java.util.List;
import java.io.*;

/**
 * Albert AI Machine Learning Chat Bot
 * 
 * @author zhaom2017
 * @version 1.0 10-19-2015
 */

public class Albert
{
    /**
     * Returns current message number in log
     */
    public static int getAInum(String fileName)
    {
        String[] logtext = readFileWords(fileName);
        int ainumcounter = 0;
        //Counts the number of lines in file
        for (String line : logtext)
        {
            ainumcounter = ainumcounter + 1;
        }
        //Every 4 lines is a new set of log message/replies thus divide by 4
        ainumcounter = (ainumcounter/4)+1;
        return (ainumcounter);
    }
    
    /**
     * Returns individual words in file
     */
    public static String[] readFileWords(String fileName)
    {
        String line = null;
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = 
                new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);
            
            String str;
            //Create array of words
            List<String> list = new ArrayList<String>();
            while((str = bufferedReader.readLine()) != null){
                String[] words = str.split(" ");
                for (String word : words)
                {
                    list.add(word);
                }
            }
            
            // Always close files.
            bufferedReader.close();  

            String[] stringArr = list.toArray(new String[0]);
            return stringArr;
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + fileName + "'");   
        }
        return null;
    }
    
    /**
     * Returns individual lines in file
     */
    public static String[] readFileLines(String fileName)
    {
        String line = null;
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = 
                new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);
            
            String str;
            //Create array of lines
            List<String> list = new ArrayList<String>();
            while((str = bufferedReader.readLine()) != null){
                list.add(str);
            }
            
            // Always close files.
            bufferedReader.close();

            String[] stringArr = list.toArray(new String[0]);
            return stringArr;
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + fileName + "'");   
        }
        return null;
    }
    
    /**
     * Write onto a file
     */
    public static void writeFile(String fileName, String text)
    {
        try {
            // Assume default encoding.
            FileWriter fileWriter =
                new FileWriter(fileName, true);

            // Always wrap FileWriter in BufferedWriter.
            BufferedWriter bufferedWriter =
                new BufferedWriter(fileWriter);

            // Note that write() does not automatically
            // append a newline character.
            bufferedWriter.write(text);

            // Always close files.
            bufferedWriter.close();
        }
        catch(IOException ex) {
            System.out.println(
                "Error writing to file '"
                + fileName + "'");
        }
    }
    
    /**
     * Add word to the dictionary
     */
    public static void addDictionary(String input, String fileName)
    {
        for (String word : input.split(" "))
            {
                //Normalize characters
                word = Normalizer.normalize(word, Normalizer.Form.NFD);
                //Remove all non alphanumeric
                word = word.replaceAll("[^A-Za-z0-9]", "");
                String[] terms = readFileWords(fileName);
                boolean addword = true;
                //Determine if word exists in dictionary already and if not add to dictionary
                for (String termword : terms)
                {
                    if (termword.equals(word))
                    {
                        addword = false;
                    }
                }
                if (addword == true)
                {
                    writeFile(fileName, word.toLowerCase()+System.getProperty("line.separator"));
                }
            }
    }
    
    /**
     * Returns the closest response from the log
     * Finds the user input and scans for an archived ai input that
     * most closely corresponds to the user input by expressing
     * both inputs as multidimension vectors and finds the
     * cosine similarity between them
     */
    
    //The closer cosSim is to 1, the more similar the response
    public static String getAnswerSim(String input, String fileName)
    {
        input = input.toLowerCase();
        //Clear input if input ends with invalid string used for log checking
        if (input.length()>13 && input.substring(input.length()-13).equals("END>uxoyQ9R9/"))
        {
            input = "";
        }
        Integer[] v1 = vectorwords(input, "Dictionary.txt");
        String[] lines = readFileLines(fileName);
        List<String> list = new ArrayList<String>();
        String line2 = "none";
        String line3 = "none";
        double similarity = 0.0;
        int nearestlength = 10000000;
        int nearlength = 0;
        for (String line : lines)
        {
            //Determine cosine similarity only when conditions for finding user reply are correct
            line = line.substring(2);
            Integer[] v2 = vectorwords(line3, "Dictionary.txt");
            double cosSim = cosineSim(v1,v2);
            boolean useline = true;
            nearlength = Math.abs(input.length()-line3.length());
            if (line3.length()>13 && line3.substring(line3.length()-13).equals("END>uxoyQ9R9/"))
            {
                //empty -- does not do action if string ends with invalid string
            }
            else if (line2.length()>7 && line2.substring(0,7).equals("erreply"))//usermessage-'us'
            {
                //new section
                //finds the length of the string and sees how closely
                //it matches to the length of strings in the log
                //and takes the closest distance and sets other
                //Finds only the closest string lengths
                if (nearlength < nearestlength)
                {
                    nearestlength = nearlength;
                    list.clear();
                    similarity = 0;
                }
                if (nearlength > nearestlength+3)
                {
                    useline = false;
                }
                //runs action if a userreply is found in the correct line
                //Finds only the outputs that are closest to a similarity of 1.0 (or exactly same)
                if (useline == true && cosSim == similarity)
                {
                    //Add to list if line's cosSim is same as current similarity value
                    list.add(line);
                }
                else if (useline == true && cosSim > similarity)
                {
                    //Clear list if a string is found that is more similar
                    similarity = cosSim;
                    list.clear();
                    list.add(line);
                }
            }
            //line, line2, line3 example in log:
            //first line (not recorded): aimessage1:
            //line3: hello
            //line2: userreply1:
            //line: hi
            line3 = line2;
            line2 = line;
        }
        //If rare case of empty string return the same input as given in
        if (list.isEmpty())
        {
            list.add(input);
        }
        //Make array of all outputs
        String[] outputs = list.toArray(new String[0]);
        int outputCount = 0;
        //Choose random output out of chosen outputs
        for (String output : outputs)
        {
             outputCount = outputCount+1;
        }
        int randomNum = randInt(1,outputCount)-1;
        String output = outputs[randomNum];
        return output;
    }
    
    /**
     * Finds the cosine similiarity between two vectors
     */
    public static double cosineSim(Integer[] v1, Integer[] v2)
    {
        double m1 = magnitude(v1);
        double m2 = magnitude(v2);
        double dot = dotproduct(v1, v2);
        //Formula for cosine similarity: (x dot y)/(mag x * mag y)
        double cosineSim = dot/(m1*m2);
        //Round to get rid of bad decimals caused by square root in magnitude (i.e. 0.999999993 goes to 1)
        cosineSim = Math.round(cosineSim*10000.0)/10000.0;
        return cosineSim;
    }
    
    /**
     * Finds the dot product of two vectors
     */
    public static double dotproduct(Integer[] v1, Integer[] v2)
    {
        int vlen1 = v1.length;
        double sum = 0.0;
        for (int i = 0; i < vlen1; i++) {
            sum += v1[i] * v2[i];
        }
        return sum;
    }
    
    /**
     * Finds the magnitude of a vector
     */
    public static double magnitude(Integer[] vector)
    {
        double magnitude = 0.0;
        for (int value : vector)
        {
            magnitude = magnitude+Math.pow((double)value, 2);
        }
        magnitude = Math.sqrt(magnitude);
        return magnitude;
    }
    
    /**
     * Returns a random integer within a range
     */
    public static int randInt(int min, int max)
    {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
    
    /**
     * Turns a line into a vector
     * Vector values correspond to the number of times each word in the dictionary in order
     * appear in the input
     *
     * i.e.:
     * Dictionary: hello how there are you
     * Input: hello there hello
     * Vector: [ 2, 0, 1, 0, 0]
    */
    public static Integer[] vectorwords(String input, String fileName)
    {
        //Next line usually reads dictionary
        String[] words = readFileWords(fileName);
        List<Integer> list = new ArrayList<Integer>();
        for(String term : words)
        {
            //Finds the number of times a word in the file appears in the input
            //and adds it into the vector array
            int termcount = 0;
            for (String word : input.split(" "))
            {
                if (word.equals(term))
                {
                    termcount = termcount+1;
                }
            }
            list.add(termcount);
        }
        Integer[] vector = list.toArray(new Integer[0]);
        return vector;
    }
    
    /**
     * Main message input and display
     */
    public static void main (String[] args)
    {
        String dictionary = "Dictionary.txt";
        String log = "Log.txt";
        String text;
        boolean repeat = true;
        //Set up input reader
        Scanner in = new Scanner(System.in);
        //Initial information
        System.out.println("#-- Albert AI version 1.0");
        System.out.println("::Hello");
        String aiInput = "Hello";
        while(repeat = true)
        {
            //Get current message number in log
            //Used for convenience in reading log only currently
            int ainum = getAInum(log);
            
            System.out.print(">> ");
            String userInput = in.nextLine();
            
            //Add user input words to dictionary if words are unknown
            addDictionary(userInput, dictionary);
            
            //Makes the text to add to the log
            String userInputtext = ">>"+userInput+System.getProperty("line.separator");
            String aiInputtext = "::"+aiInput+System.getProperty("line.separator");
            //Writes the determined text to the log
            //Writes the current ai message and user input in response to the ai message
            //Same messages as in the past will be written again, thus when
            //picking a response, the responses that appear most (and so are 'good'
            //reponses commonly used) will be more likely to be chosen
            writeFile(log, "aimessage"+ainum+": "+System.getProperty("line.separator"));
            writeFile(log, aiInputtext.toLowerCase());
            writeFile(log, "userreply"+ainum+": "+System.getProperty("line.separator"));
            writeFile(log, userInputtext.toLowerCase());
            
            //getAnswerSim finds the closest statement based of
            //similarity in the multidimension vectors of the input
            //vs the ai message in the log sentences
            aiInput = getAnswerSim(userInput, log);
            //Print response
            System.out.println("::"+aiInput);
        }
    }
}
