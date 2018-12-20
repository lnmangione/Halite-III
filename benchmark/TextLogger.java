package benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class TextLogger {

    public static void writeLine(String fileName, String line){
        File file = new File(fileName);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(file, true));
        } catch (Exception e) {
            e.printStackTrace();
        }
        writer.append(line + "\n");
        writer.close();
    }
}