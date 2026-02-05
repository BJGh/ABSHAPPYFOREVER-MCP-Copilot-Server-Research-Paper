import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class TextDriver {

    private String source;

    TextDriver (String pathname) {

        String line;
        source = new String();

        try {

            File file = new File(pathname);
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);

            line = reader.readLine();

            while (line != null) {

                line += '\n';
                source += line;
                line = reader.readLine();
            }

        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public String get_source () {

        return source;
    }
}
