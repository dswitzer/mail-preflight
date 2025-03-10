package eu.kk42.mailpreflight;

import org.junit.jupiter.api.Test;

import eu.kk42.mailpreflight.domain.PreflightConfig;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Entities.EscapeMode;

import static org.junit.jupiter.api.Assertions.*;
import static eu.kk42.mailpreflight.StringAssertions.assertStringsEqualWithDiffHighlight;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;


/**
 * @author konstantinkastanov
 * created on 2019-03-29
 */
public class MailPreflightTest {
	// private static final Logger log = LoggerFactory.getLogger(MailPreflightTest.class);
	private MailPreflight preflight = new MailPreflight();


	@Test
	public void preprocessEmailHtml_shouldNotFail_smokeTest() {
		// This is just a smoke test to check that no exceptions are thrown in MailPreflight plumbing. Individual processors are tested in separate test classes.
		String result = preflight.preprocessEmailHtml("<html><body><style>h1 {font-weight: bold}</style><h1>Hello World</h1></body></html>");
		//log.info(result);
		// System.out.println(result);
		assertTrue(result.length() > 0);
	}

	@Test
	public void preprocessEmailHtml_should_inline_css_simple() {
		String result = preflight.preprocessEmailHtml("<html><body><style>h1 {font-weight: bold}</style><h1>Hello World</h1></body></html>");
		assertStringsEqualWithDiffHighlight("<html>\n" + //
						" <head></head>\n" + //
						" <body>\n" + //
						"  <style>h1 {font-weight: bold}</style>\n" + //
						"  <h1 style=\"font-weight:bold;\">Hello World</h1>\n" + //
						" </body>\n" + //
						"</html>", result);
	}

	@Test
	public void preprocessEmailHtml_should_process_basic_example() {
		String templatePath = "templates/postmark/basic/example/content.html";
		String expectedPath = "assertions/templates/postmark/basic/example_default.html";

		String template = StringAssertions.readFile(templatePath);
	
		String expected = StringAssertions.readFile(expectedPath);
		String actual = preflight.preprocessEmailHtml(template);

		assertStringsEqualWithDiffHighlight(expected, actual);
	}

	@Test
	public void preprocessEmailHtml_should_process_remove_classes_and_styles() {
		String templatePath = "templates/postmark/basic/example/content.html";
		String expectedPath = "assertions/templates/postmark/basic/example_classes_and_styles_purges.html";

		String template = StringAssertions.readFile(templatePath);
	
		String expected = StringAssertions.readFile(expectedPath);
		String actual = preflight.preprocessEmailHtml(template, preflight.createConfig().withRemoveCssClasses(true));

		StringAssertions.saveFile("target/test-output/", "preprocessEmailHtml_test_actual.html", actual);

		assertStringsEqualWithDiffHighlight(expected, actual);
	}

	@Test
	public void preprocessEmailHtml_should_process_with_pretty_print_disabled() {
		String templatePath = "templates/postmark/basic/example/content.html";
		String expectedPath = "assertions/templates/postmark/basic/example_pretty_print_disabled.html";

		String template = StringAssertions.readFile(templatePath);
	
		String expected = StringAssertions.readFile(expectedPath);
		// create a config with pretty print disabled
		String actual = preflight.preprocessEmailHtml(template, preflight.createConfig().withPrettyPrint(false));

		assertStringsEqualWithDiffHighlight(expected, actual);
	}

	@Test
	public void preprocessEmailHtml_should_process_with_custom_output_settings() {
		String templatePath = "templates/postmark/basic/example/content.html";
		String expectedPath = "assertions/templates/postmark/basic/example_custom_output_settings.html";

		String template = StringAssertions.readFile(templatePath);
		PreflightConfig config = preflight.createConfig();

		// create custom output settings for jSoup
		OutputSettings settings = new OutputSettings();
		settings
			.escapeMode(EscapeMode.base)
			.syntax(Syntax.html)
			.prettyPrint(true)
			.indentAmount(0)
		;
		// update the configuration
		config.withOutputSettings(settings);
	
		String expected = StringAssertions.readFile(expectedPath);
		String actual = preflight.preprocessEmailHtml(template, config);

		assertStringsEqualWithDiffHighlight(expected, actual);
	}
}
