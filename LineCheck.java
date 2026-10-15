import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.stream.Stream;

/**
 * Checks the CAT rule: no class over 250 lines of code and no method over 30,
 * blank lines and comments excluded.
 * Run from the project root:  java LineCheck.java src/main/java
 */
public class LineCheck {
    private static final int MAX_CLASS = 250;
    private static final int MAX_METHOD = 30;
    private static final double WARN_AT = 0.8;

    private record ClassStat(String file, int loc) { }
    private record MethodStat(String file, String name, int line, int loc) { }
    private record Scope(String kind, String name, int start) { }

    public static void main(String[] args) throws IOException {
        Path root = Path.of(args.length > 0 ? args[0] : "src/main/java");
        List<ClassStat> classes = new ArrayList<>();
        List<MethodStat> methods = new ArrayList<>();
        for (Path file : javaFiles(root)) analyse(file, classes, methods);
        report(classes, methods);
    }

    private static List<Path> javaFiles(Path root) throws IOException {
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(p -> p.toString().endsWith(".java")).sorted().toList();
        }
    }

    private static void analyse(Path file, List<ClassStat> classes, List<MethodStat> methods)
            throws IOException {
        List<String> clean = strip(Files.readAllLines(file));
        int[] prefix = new int[clean.size() + 1];
        for (int i = 0; i < clean.size(); i++) {
            prefix[i + 1] = prefix[i] + (clean.get(i).isBlank() ? 0 : 1);
        }
        String name = file.getFileName().toString();
        classes.add(new ClassStat(name, prefix[clean.size()]));
        findMethods(name, clean, prefix, methods);
    }

    /** Removes comments and blanks out string contents so braces inside them are ignored. */
    private static List<String> strip(List<String> raw) {
        List<String> out = new ArrayList<>();
        boolean inBlock = false;
        boolean inText = false;
        for (String line : raw) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (i < line.length()) {
                char c = line.charAt(i);
                char next = i + 1 < line.length() ? line.charAt(i + 1) : ' ';
                if (inBlock) {
                    if (c == '*' && next == '/') { inBlock = false; i += 2; } else { i++; }
                } else if (inText) {
                    if (line.startsWith("\"\"\"", i)) { inText = false; sb.append("\"\"\""); i += 3; }
                    else { sb.append('x'); i++; }
                } else if (c == '/' && next == '*') {
                    inBlock = true; i += 2;
                } else if (c == '/' && next == '/') {
                    break;
                } else if (line.startsWith("\"\"\"", i)) {
                    inText = true; sb.append("\"\"\""); i += 3;
                } else if (c == '"' || c == '\'') {
                    i = skipLiteral(line, i, c, sb);
                } else {
                    sb.append(c); i++;
                }
            }
            out.add(sb.toString());
        }
        return out;
    }

    private static int skipLiteral(String line, int start, char quote, StringBuilder sb) {
        int j = start + 1;
        while (j < line.length() && line.charAt(j) != quote) {
            if (line.charAt(j) == '\\') j++;
            j++;
        }
        sb.append(quote == '"' ? "\"\"" : "0");
        return j + 1;
    }

    private static void findMethods(String file, List<String> clean, int[] prefix,
                                    List<MethodStat> methods) {
        Deque<Scope> stack = new ArrayDeque<>();
        StringBuilder header = new StringBuilder();
        int headerStart = -1;
        for (int ln = 0; ln < clean.size(); ln++) {
            String line = clean.get(ln);
            if (isAnnotationLine(line, header)) continue;
            for (char c : line.toCharArray()) {
                if (c == '{') {
                    stack.push(classify(header.toString(), stack.peek(), headerStart < 0 ? ln : headerStart));
                    header.setLength(0);
                    headerStart = -1;
                } else if (c == '}') {
                    if (!stack.isEmpty()) closeScope(file, stack.pop(), ln, prefix, methods);
                    header.setLength(0);
                    headerStart = -1;
                } else if (c == ';') {
                    header.setLength(0);
                    headerStart = -1;
                } else {
                    header.append(c);
                    if (headerStart < 0 && c != ' ' && c != '\t') headerStart = ln;
                }
            }
            header.append(' ');
        }
    }

    private static boolean isAnnotationLine(String line, StringBuilder header) {
        String t = line.trim();
        return header.toString().isBlank() && t.startsWith("@")
                && !t.contains("{") && !t.contains("}") && !t.contains(";");
    }

    private static void closeScope(String file, Scope s, int endLine, int[] prefix,
                                   List<MethodStat> methods) {
        if (!s.kind().equals("METHOD")) return;
        int loc = prefix[endLine + 1] - prefix[s.start()];
        methods.add(new MethodStat(file, s.name(), s.start() + 1, loc));
    }

    private static Scope classify(String rawHeader, Scope parent, int start) {
        String h = rawHeader.replaceAll("\\s+", " ").trim();
        boolean hasEquals = h.contains("=");
        if (!hasEquals && h.matches(".*\\b(class|interface|enum|record)\\b.*")) {
            return new Scope("CLASS", "", start);
        }
        boolean inClass = parent != null && parent.kind().equals("CLASS");
        if (inClass && !hasEquals && h.contains("(") && !h.startsWith("new ")
                && h.matches(".*\\)\\s*(throws [\\w., ]+)?$")) {
            return new Scope("METHOD", methodName(h), start);
        }
        return new Scope("OTHER", "", start);
    }

    private static String methodName(String header) {
        String before = header.substring(0, header.indexOf('(')).trim();
        int space = before.lastIndexOf(' ');
        return before.substring(space + 1) + "()";
    }

    private static void report(List<ClassStat> classes, List<MethodStat> methods) {
        System.out.println("Dungeon Quest line check  (limits: " + MAX_CLASS + " per class, "
                + MAX_METHOD + " per method; blanks and comments not counted)\n");
        System.out.println("CLASSES, largest first");
        classes.stream().sorted(Comparator.comparingInt(ClassStat::loc).reversed()).limit(12)
                .forEach(c -> System.out.printf("  %4d  %-28s %s%n", c.loc(), c.file(),
                        status(c.loc(), MAX_CLASS)));
        System.out.println("\nMETHODS, largest first");
        methods.stream().sorted(Comparator.comparingInt(MethodStat::loc).reversed()).limit(12)
                .forEach(m -> System.out.printf("  %4d  %-28s %-24s %s%n", m.loc(), m.file(),
                        m.name(), status(m.loc(), MAX_METHOD)));
        if (classes.isEmpty()) {
            System.out.println("No .java files found. Check the folder you passed in.");
            return;
        }
        long badClasses = classes.stream().filter(c -> c.loc() > MAX_CLASS).count();
        long badMethods = methods.stream().filter(m -> m.loc() > MAX_METHOD).count();
        System.out.println("\n" + classes.size() + " files, " + methods.size() + " methods checked.");
        System.out.println(badClasses + " class(es) and " + badMethods + " method(s) over the limit.");
        System.out.println(badClasses + badMethods == 0 ? "ALL CLEAR" : "SPLIT the ones marked OVER.");
    }

    private static String status(int loc, int max) {
        if (loc > max) return "OVER (limit " + max + ")";
        return loc >= max * WARN_AT ? "close to the limit" : "ok";
    }
}