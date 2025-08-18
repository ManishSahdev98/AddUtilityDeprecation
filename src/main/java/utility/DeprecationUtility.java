package utility;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class DeprecationUtility {

    private static final String DEPRECATION_COMMENT = "/** Do not change without asking Sahdev Team*/";
    private static final String DEPRECATION_ANNOTATION = "@Deprecated";

    private final Path projectRoot;
    private final List<Path> javaFiles;
    private final Map<String, List<MethodInfo>> methodOccurrences;

    public DeprecationUtility(String projectPath) throws IOException {
        this.projectRoot = Paths.get(projectPath);
        this.javaFiles = findJavaFiles(projectRoot);
        this.methodOccurrences = new HashMap<>();
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java utility.DeprecationUtility <project_path> <method_name> [method_signature]");
            System.out.println("Example: java utility.DeprecationUtility /path/to/project calculateTotal");
            System.out.println("Example: java utility.DeprecationUtility /path/to/project calculateTotal \"int calculateTotal(int a, int b)\"");
            System.exit(1);
        }

        String projectPath = args[0];
        String methodName = args[1];

        String methodSignature = args.length > 2 ? args[2] : null;

        try {
            DeprecationUtility utility = new DeprecationUtility(projectPath);
            utility.deprecateMethod(methodName, methodSignature);
            utility.deprecateEmptyClasses();
            System.out.println("Deprecation process completed successfully!");
        } catch (Exception e) {
            System.err.println("Error during deprecation: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private List<Path> findJavaFiles(Path root) throws IOException {
        List<Path> files = new ArrayList<>();
        Files.walk(root)
            .filter(path -> path.toString().endsWith(".java"))
            .filter(path -> !path.toString().contains("/target/") && !path.toString().contains("/build/"))
            .forEach(files::add);
        return files;
    }

    public void deprecateMethod(String methodName, String methodSignature) throws IOException {
        System.out.println("Searching for method: " + methodName);

        for (Path javaFile : javaFiles) {
            String content = Files.readString(javaFile);
            String originalContent = content;


            List<MethodInfo> methods = findMethodDeclarations(content, methodName, methodSignature);

            if (!methods.isEmpty()) {
                System.out.println("Found " + methods.size() + " occurrence(s) in: " + javaFile);

                methods.sort((a, b) -> Integer.compare(b.lineNumber, a.lineNumber));

                for (MethodInfo method : methods) {
                    content = deprecateMethodInContent(content, method);
                }


                if (!content.equals(originalContent)) {
                    Files.writeString(javaFile, content);
                    System.out.println("Updated: " + javaFile);
                }
            }
        }
    }

    private List<MethodInfo> findMethodDeclarations(String content, String methodName, String methodSignature) {
        List<MethodInfo> methods = new ArrayList<>();
        String[] lines = content.split("\n");

        String methodPattern;

        if (methodSignature != null) {
            methodPattern = Pattern.quote(methodSignature);
        } else {
            methodPattern = "\\s*\\w+\\s+\\w+\\s*" + Pattern.quote(methodName) + "\\s*\\(";
        }

        Pattern pattern = Pattern.compile(methodPattern);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (pattern.matcher(line).find()) {
                // Check if method is not already deprecated
                if (!isMethodDeprecated(content, i)) {
                    methods.add(new MethodInfo(i, line, methodName));
                }
            }
        }
        return methods;
    }

    private boolean isMethodDeprecated(String content, int lineNumber) {
        String[] lines = content.split("\n");

        for (int i = Math.max(0, lineNumber - 3); i < lineNumber; i++) {
            if (i < lines.length && lines[i].trim().contains("@Deprecated")) {
                return true;
            }
        }
        return false;
    }

    private String deprecateMethodInContent(String content, MethodInfo method) {
        String[] lines = content.split("\n");

        int methodLine = method.lineNumber;
        int insertLine = methodLine;

        while (insertLine > 0 && (lines[insertLine - 1].trim().startsWith("@") ||
                                 lines[insertLine - 1].trim().startsWith("/**") ||
                                 lines[insertLine - 1].trim().startsWith("*") ||
                                 lines[insertLine - 1].trim().isEmpty())) {
            insertLine--;
        }

        StringBuilder newContent = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            if (i == insertLine) {
                newContent.append("\n").append(DEPRECATION_COMMENT).append("\n");
                newContent.append(DEPRECATION_ANNOTATION);
            }
            newContent.append(lines[i]);
            if (i < lines.length - 1) {
                newContent.append("\n");
            }
        }
        return newContent.toString();
    }

    public void deprecateEmptyClasses() throws IOException {
        System.out.println("\nChecking for classes to deprecate...");
        for (Path javaFile : javaFiles) {
            String content = Files.readString(javaFile);
            String originalContent = content;

            List<ClassInfo> classes = findClassDeclarations(content);

            for (ClassInfo classInfo : classes) {
                if (shouldDeprecateClass(content, classInfo)) {
                    System.out.println("Deprecating class: " + classInfo.className + " in " + javaFile);
                    content = deprecateClassInContent(content, classInfo);
                }
            }

            if (!content.equals(originalContent)) {
                Files.writeString(javaFile, content);
                System.out.println("Updated class deprecation in: " + javaFile);
            }
        }
    }

    private List<ClassInfo> findClassDeclarations(String content) {
        List<ClassInfo> classes = new ArrayList<>();
        String[] lines = content.split("\n");

        Pattern classPattern = Pattern.compile("\\s*(public\\s+)?(abstract\\s+)?(final\\s+)?class\\s+(\\w+)");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Matcher matcher = classPattern.matcher(line);
            if (matcher.find()) {
                String className = matcher.group(4);
                classes.add(new ClassInfo(i, className));
            }
        }
        return classes;
    }

    private boolean shouldDeprecateClass(String content, ClassInfo classInfo) {
        if (isClassDeprecated(content, classInfo.lineNumber)) {
            return false;
        }

        int nonDeprecatedMethods = countNonDeprecatedMethods(content, classInfo);
        return nonDeprecatedMethods == 0;
    }

    private boolean isClassDeprecated(String content, int lineNumber) {
        String[] lines = content.split("\n");

        for (int i = Math.max(0, lineNumber - 3); i < lineNumber; i++) {
            if (i < lines.length && lines[i].trim().contains("@Deprecated")) {
                return true;
            }
        }
        return false;
    }

    private int countNonDeprecatedMethods(String content, ClassInfo classInfo) {
        String[] lines = content.split("\n");
        int count = 0;

        int classEnd = findClassEnd(lines, classInfo.lineNumber);

        for (int i = classInfo.lineNumber; i < classEnd; i++) {
            if (i < lines.length && isMethodDeclaration(lines[i])) {
                if (!isMethodDeprecatedInRange(content, i, classEnd)) {
                    count++;
                }
            }
        }
        return count;
    }

    private int findClassEnd(String[] lines, int startLine) {
        int braceCount = 0;
        boolean inClass = false;

        for (int i = startLine; i < lines.length; i++) {
            String line = lines[i];

            if (line.contains("{")) {
                if (!inClass) {
                    inClass = true;
                }
                braceCount++;
            }

            if (line.contains("}")) {
                braceCount--;
                if (inClass && braceCount == 0) {
                    return i;
                }
            }
        }

        return lines.length;
    }

    /**
     * Find classes that should be deprecated (only contain deprecated methods)
     */
    private List<ClassInfo> findClassesToDeprecate() throws IOException {
        List<ClassInfo> classesToDeprecate = new ArrayList<>();

        for (Path javaFile : javaFiles) {
            String content = Files.readString(javaFile);
            List<ClassInfo> classes = findClassDeclarations(content);

            for (ClassInfo classInfo : classes) {
                if (shouldDeprecateClass(content, classInfo)) {
                    classesToDeprecate.add(classInfo);
                }
            }
        }
        return classesToDeprecate;
    }

    /**
     * Check if a line contains a method declaration
     */
    private boolean isMethodDeclaration(String line) {
        String trimmed = line.trim();
        return trimmed.matches(".*\\w+\\s+\\w+\\s*\\(.*\\)\\s*\\{?\\s*$") ||
               trimmed.matches(".*\\w+\\s*\\(.*\\)\\s*\\{?\\s*$");
    }

    /**
     * Check if method is deprecated within a range
     */
    private boolean isMethodDeprecatedInRange(String content, int methodLine, int endLine) {
        String[] lines = content.split("\n");

        for (int i = Math.max(0, methodLine - 3); i < methodLine && i < endLine; i++) {
            if (i < lines.length && lines[i].trim().contains("@Deprecated")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Add deprecation annotation to a class
     */
    private String deprecateClassInContent(String content, ClassInfo classInfo) {
        String[] lines = content.split("\n");

        int insertLine = classInfo.lineNumber;

        // Find the right place to insert
        while (insertLine > 0 && (lines[insertLine - 1].trim().startsWith("@") ||
                                 lines[insertLine - 1].trim().startsWith("/**") ||
                                 lines[insertLine - 1].trim().startsWith("*") ||
                                 lines[insertLine - 1].trim().isEmpty())) {
            insertLine--;
        }

        // Create new content with deprecation
        StringBuilder newContent = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            if (i == insertLine) {
                newContent.append("\n").append(DEPRECATION_COMMENT).append("\n");
                newContent.append(DEPRECATION_ANNOTATION).append("\n");
            }
            newContent.append(lines[i]);
            if (i < lines.length - 1) {
                newContent.append("\n");
            }
        }

        return newContent.toString();
    }

    /**
     * Inner class to store method information
     */
    private static class MethodInfo {
        final int lineNumber;
        final String lineContent;
        final String methodName;

        MethodInfo(int lineNumber, String lineContent, String methodName) {
            this.lineNumber = lineNumber;
            this.lineContent = lineContent;
            this.methodName = methodName;
        }
    }

    /**
     * Inner class to store class information
     */
    private static class ClassInfo {
        final int lineNumber;
        final String className;

        ClassInfo(int lineNumber, String className) {
            this.lineNumber = lineNumber;
            this.className = className;
        }
    }


    public WebServer.DeprecationResult deprecateMethodWithResult(String methodName, String methodSignature) throws IOException {
        StringBuilder details = new StringBuilder();
        int filesUpdated = 0;
        int methodsDeprecated = 0;
        int classesDeprecated = 0;

        if(methodName !=null && !methodName.trim().isEmpty()) {
            details.append("Starting deprecation process for method: ").append(methodName).append("\n");
        }

        if (methodSignature != null && !methodSignature.trim().isEmpty()) {
            details.append("Method signature: ").append(methodSignature).append("\n");
        }
        details.append("Project path: ").append(projectRoot).append("\n\n");

        // Deprecate methods
        for (Path javaFile : javaFiles) {
            String content = Files.readString(javaFile);
            String originalContent = content;

            // Find method declarations
            List<MethodInfo> methods = findMethodDeclarations(content, methodName, methodSignature);

            if (!methods.isEmpty()) {
                details.append("Found ").append(methods.size()).append(" occurrence(s) in: ").append(javaFile).append("\n");

                // Sort methods by line number (descending) to avoid offset issues
                methods.sort((a, b) -> Integer.compare(b.lineNumber, a.lineNumber));

                for (MethodInfo method : methods) {
                    content = deprecateMethodInContent(content, method);
                    methodsDeprecated++;
                }

                // Write back to file if content changed
                if (!content.equals(originalContent)) {
                    Files.writeString(javaFile, content);
                    details.append("Updated: ").append(javaFile).append("\n");
                    filesUpdated++;
                }
            }
        }

        details.append("\n");

        // Deprecate empty classes
        details.append("Checking for classes that can be deprecated...\n");
        List<ClassInfo> classesToDeprecate = findClassesToDeprecate();

        for (ClassInfo classInfo : classesToDeprecate) {
            details.append("Deprecating class: ").append(classInfo.className).append(" (only contains deprecated methods)\n");
            classesDeprecated++;
        }

        // Apply class deprecations
        for (Path javaFile : javaFiles) {
            String content = Files.readString(javaFile);
            String originalContent = content;

            for (ClassInfo classInfo : classesToDeprecate) {
                if (javaFile.toString().contains(classInfo.className + ".java")) {
                    content = deprecateClassInContent(content, classInfo);
                    details.append("Updated class: ").append(javaFile).append("\n");
                    if (content.equals(originalContent)) {
                        filesUpdated++;
                    }
                }
            }

            // Write back to file if content changed
            if (!content.equals(originalContent)) {
                Files.writeString(javaFile, content);
            }
        }

        details.append("\nDeprecation process completed successfully!\n");
        details.append("Total files updated: ").append(filesUpdated).append("\n");
        details.append("Total methods deprecated: ").append(methodsDeprecated).append("\n");
        details.append("Total classes deprecated: ").append(classesDeprecated).append("\n");

        return new WebServer.DeprecationResult(true, filesUpdated, methodsDeprecated, classesDeprecated, details.toString(), null);
    }
}