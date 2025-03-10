package eu.kk42.mailpreflight;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class StringAssertions {

	public static void assertStringsEqualWithDiffHighlight(String expected, String actual) {
		// default to 20 character radius
		assertStringsEqualWithDiffHighlight(expected, actual, 20);
	}

	public static void assertStringsEqualWithDiffHighlight(String expected, String actual, int contextRadius) {
		if (!expected.equals(actual)) {
			String expectedNormalized = normalizeString(expected);
			String actualNormalized = normalizeString(actual);

			int diffIndex = findFirstDifferenceIndex(expectedNormalized, actualNormalized);

			// Get surrounding context around the first difference
			String expectedContext = getContextSubstring(expectedNormalized, diffIndex, contextRadius);
			String actualContext = getContextSubstring(actualNormalized, diffIndex, contextRadius);
			int offset = 19/* adjust for the line prefix */;
			offset = (diffIndex > contextRadius) ? (diffIndex - (diffIndex-contextRadius-offset)) : (diffIndex+offset);
			String markerDistance = " ".repeat(offset);

			String diffMessage = String.format(
				"Strings differ:\n" +
				"Expected context:  %s\n" + 
				"%s^-- difference starts here\n" +
				"Actual context:    %s\n" +
				""
				, expectedContext, markerDistance, actualContext
			);

			fail(diffMessage);
		}
	}


	public static String normalizeString(String input) {
		return input
			// replace hidden characters w/their String token representations (see https://docs.oracle.com/javase/tutorial/java/data/characters.html)
			.replaceAll("\t", "\\\\t") 
			.replaceAll("\n", "\\\\n") 
			.replaceAll("\r", "\\\\r") 
			.replaceAll("\b", "\\\\b") 
			.replaceAll("\f", "\\\\f")
		;		
	}


	private static int findFirstDifferenceIndex(String expected, String actual) {
		int maxLength = Math.min(expected.length(), actual.length());
		for (int i = 0; i < maxLength; i++) {
			if (expected.charAt(i) != actual.charAt(i)) {
				return i;
			}
		}
		return maxLength < Math.max(expected.length(), actual.length()) ? maxLength : -1;
	}

	private static String getContextSubstring(String str, int index, int radius) {
		int start = Math.max(0, index - radius);
		int end = Math.min(str.length(), index + radius);
		return str.substring(start, end);
	}

	public static String readFile(String resourcePath){
		ClassLoader classLoader = StringAssertions.class.getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(resourcePath);

		assertNotNull(inputStream, "File not found");

		Scanner scanner = new Scanner(inputStream);
		String fileContent = scanner.useDelimiter("\\A").next();
		scanner.close();

		return fileContent;
	}

	public static void saveFile(String fileName, String contents){
		saveFile("target/test-output/", fileName, contents);
	}

	public static void saveFile(String folder, String fileName, String contents){
		try {
			Path outputPath = Path.of(folder, fileName);
			Files.createDirectories(outputPath.getParent());

			Files.writeString(outputPath, contents);
		} catch (IOException e) {
			fail("Could not save file! " + e.getMessage());
		}
	}
	
}
