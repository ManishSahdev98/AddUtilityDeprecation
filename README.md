# Java Deprecation Utility

A comprehensive Java utility to automatically deprecate methods and classes across an entire project with the specific comment requirement: `/** Do not change without asking MODS Team*/`.

## Features

- **Method Deprecation**: Automatically finds and deprecates specified methods across all Java files in a project
- **Class Deprecation**: Automatically deprecates classes that only contain deprecated methods
- **Smart Detection**: Avoids re-deprecating already deprecated methods/classes
- **Flexible Matching**: Supports both method name and full method signature matching
- **Safe Updates**: Only modifies files that actually need changes
- **Comprehensive Coverage**: Processes all Java files while excluding build directories

## Requirements

- Java 11 or higher (uses `Files.readString()` and `Files.writeString()`)
- Access to the project directory you want to process

## Usage

### Basic Usage

```bash
java DeprecationUtility <project_path> <method_name>
```

### Advanced Usage with Method Signature

```bash
java DeprecationUtility <project_path> <method_name> "<method_signature>"
```

### Examples

1. **Deprecate a method by name only:**
   ```bash
   java DeprecationUtility /path/to/your/project calculateTotal
   ```

2. **Deprecate a method with specific signature:**
   ```bash
   java DeprecationUtility /path/to/your/project calculateTotal "int calculateTotal(int a, int b)"
   ```

3. **Deprecate a method in current directory:**
   ```bash
   java DeprecationUtility . processData
   ```

## How It Works

### Method Deprecation Process

1. **File Discovery**: Recursively finds all `.java` files in the specified project directory
2. **Method Search**: Searches for method declarations matching the specified name/signature
3. **Deprecation Check**: Verifies the method isn't already deprecated
4. **Annotation Addition**: Adds the required comment and `@Deprecated` annotation above the method
5. **File Update**: Writes the updated content back to the file

### Class Deprecation Process

1. **Class Analysis**: After method deprecation, analyzes all classes in the project
2. **Method Counting**: Counts non-deprecated methods in each class
3. **Auto-Deprecation**: If a class contains only deprecated methods, it's automatically deprecated
4. **Smart Detection**: Avoids deprecating already deprecated classes

## Output Format

The utility adds the following above each deprecated method/class:

```java
/** Do not change without asking MODS Team*/
@Deprecated
public void methodName() {
    // method implementation
}
```

## Safety Features

- **Backup Recommendation**: Always commit your changes or create a backup before running
- **Dry Run**: The utility shows what it's going to do before making changes
- **Conflict Avoidance**: Won't modify already deprecated methods/classes
- **Build Directory Exclusion**: Automatically skips `/target/` and `/build/` directories

## Compilation

```bash
javac DeprecationUtility.java
```

## Example Output

```
Searching for method: calculateTotal
Found 2 occurrence(s) in: /path/to/project/src/main/java/Calculator.java
Updated: /path/to/project/src/main/java/Calculator.java
Found 1 occurrence(s) in: /path/to/project/src/main/java/MathUtils.java
Updated: /path/to/project/src/main/java/MathUtils.java

Checking for classes to deprecate...
Deprecating class: OldCalculator in /path/to/project/src/main/java/OldCalculator.java
Updated class deprecation in: /path/to/project/src/main/java/OldCalculator.java
Deprecation process completed successfully!
```

## Supported Method Patterns

The utility can detect various method declaration patterns:

- `public void methodName()`
- `private int methodName(String param)`
- `protected static String methodName()`
- `methodName()` (package-private)
- Constructor methods
- Abstract methods

## Limitations

- Only processes `.java` files (not compiled `.class` files)
- Requires Java 11+ for file operations
- Method detection is based on regex patterns and may not catch all edge cases
- Assumes standard Java syntax and formatting

## Troubleshooting

### Common Issues

1. **Permission Denied**: Ensure you have read/write access to the project directory
2. **Method Not Found**: Check if the method name is spelled correctly
3. **No Changes Made**: The method might already be deprecated or not found

### Debug Mode

The utility provides detailed output about what it's doing. If you need more information, you can modify the source code to add additional logging.

## Contributing

Feel free to enhance this utility with additional features such as:

- Support for different deprecation comment formats
- Batch processing of multiple methods
- Integration with build tools (Maven, Gradle)
- Support for other JVM languages (Kotlin, Scala)

## License

This utility is provided as-is for educational and professional use.
