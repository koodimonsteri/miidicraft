package utils.loaders;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ResLoader {

    public static String loadResources(String filename) throws Exception {
        String res;
        try(InputStream in = Class.forName(ResLoader.class.getName()).getResourceAsStream(filename);
            Scanner scanner = new Scanner(in, "UTF-8")) {
            res = scanner.useDelimiter("\\A").next();
        }
        return res;
    }

    public static List<String> readAllLines(String fileName) throws Exception {
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Class.forName(ResLoader.class.getName()).getResourceAsStream(fileName)))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
        }
        return list;
    }
}
