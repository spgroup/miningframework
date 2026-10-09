// Example of the slides "Suitable entry point algorithms for static analysis in semantic conflict detection tools":
// left (blue) modifies line 41 and right (red) lines 23 and 38. build.jar is this file compiled with
// javac --release 8 -g -d classes Text.java && jar cf build.jar -C classes .
public class Text {
    int fixes, comments;
    String text;

    public static void main(String[] args) {
        Text t = new Text();
        t.text = args.length > 0 ? args[0] : "the_the_ _dog_dog";
        t.generateReport();
    }

    void generateReport() {
        countDupWords();
        countComments();
        countDupWhiteSpace();
    }

    void countDupWords() {
        String[] words = text.split("_");
        for (int i = 1; i < words.length; i++) {
            if (words[i].equals(words[i - 1])) fixes++; // right
        }
    }

    void countComments() {
        comments = text.split("//").length - 1;
    }

    void countDupWhiteSpace() {
        int count = 0;
        for (int i = 1; i < text.length(); i++) {
            if (text.charAt(i) == ' ' && text.charAt(i - 1) == ' ') {
                count++;
            }
        }
        fixes++; // right
        count++;

        fixes = count; // left
    }
}
