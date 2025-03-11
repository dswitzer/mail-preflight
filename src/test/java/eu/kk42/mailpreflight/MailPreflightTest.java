package eu.kk42.mailpreflight;

import org.junit.jupiter.api.Test;

import eu.kk42.mailpreflight.domain.PreflightConfig;
import eu.kk42.mailpreflight.domain.utils.W3CNamedColor;

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
		String html = "<html><body><style>h1 {font-weight: bold;}</style><h1>Hello World</h1></body></html>";

		// This is just a smoke test to check that no exceptions are thrown in MailPreflight plumbing. Individual processors are tested in separate test classes.
		String result = preflight.preprocessEmailHtml(html);
		//log.info(result);
		// System.out.println(result);
		assertTrue(result.length() > 0);
	}

	@Test
	public void preprocessEmailHtml_should_inline_css_simple() {
		String html = "<html><body><style>h1 {font-weight: bold}</style><h1>Hello World</h1></body></html>";

		String expected = "<html>\n" +
			" <head></head>\n" +
			" <body>\n" +
			"  <style>h1 {font-weight: bold}</style>\n" +
			"  <h1 style=\"font-weight:bold;\">Hello World</h1>\n" +
			" </body>\n" +
			"</html>"
		;
		String actual = preflight.preprocessEmailHtml(html);

		assertStringsEqualWithDiffHighlight(expected, actual);
	}

	@Test
	public void preprocessEmailHtml_should_target_legacy_browsers_by_default_and_use_hex_colors_with_bgcolor() {
		String html = "<html><body><style>body {background: #fff} h1 {font-weight: bold; color: #0000ff}</style><h1>Hello World</h1></body></html>";

		String expected = "<html>\n" +
			" <head></head>\n" +
			" <body style=\"background:#ffffff;\" bgcolor=\"#ffffff\">\n" +
			"  <style>body {background: #fff} h1 {font-weight: bold; color: #0000ff}</style>\n" +
			"  <h1 style=\"color:#0000ff;font-weight:bold;\">Hello World</h1>\n" +
			" </body>\n" +
			"</html>"
		;
		String actual = preflight.preprocessEmailHtml(html);

		assertStringsEqualWithDiffHighlight(expected, actual);
	}

	@Test
	public void preprocessEmailHtml_should_target_legacy_browsers_by_default_and_extract_bgcolor_from_background() {
		String html = "<html><body><style>body {background: border-box #fff} h1 {font-weight: bold; color: #0000ff}</style><h1>Hello World</h1></body></html>";

		String expected = "<html>\n" +
			" <head></head>\n" +
			" <body style=\"background:border-box #ffffff;\" bgcolor=\"#ffffff\">\n" +
			"  <style>body {background: border-box #fff} h1 {font-weight: bold; color: #0000ff}</style>\n" +
			"  <h1 style=\"color:#0000ff;font-weight:bold;\">Hello World</h1>\n" +
			" </body>\n" +
			"</html>"
		;
		String actual = preflight.preprocessEmailHtml(html);

		assertStringsEqualWithDiffHighlight(expected, actual);
	}

	@Test
	public void preprocessEmailHtml_should_extract_bgcolor_from_background_as_named_color() {
		String html = "<html><body><style>body {background: border-box white} h1 {font-weight: bold; color: blue}</style><h1>Hello World</h1></body></html>";

		String expected = "<html>\n" +
			" <head></head>\n" +
			" <body style=\"background:border-box white;\" bgcolor=\"#ffffff\">\n" +
			"  <style>body {background: border-box white} h1 {font-weight: bold; color: blue}</style>\n" +
			"  <h1 style=\"color:blue;font-weight:bold;\">Hello World</h1>\n" +
			" </body>\n" +
			"</html>"
		;
		String actual = preflight.preprocessEmailHtml(html);

		assertStringsEqualWithDiffHighlight(expected, actual);
	}

	@Test
	public void preprocessEmailHtml_should_target_legacy_browsers_by_default_and_extract_bgcolor_from_background_color() {
		String html = "<html><body><style>body {background-color: #fff} h1 {font-weight: bold; color: #0000ff}</style><h1>Hello World</h1></body></html>";

		String expected = "<html>\n" +
			" <head></head>\n" +
			" <body style=\"background-color:#ffffff;\" bgcolor=\"#ffffff\">\n" +
			"  <style>body {background-color: #fff} h1 {font-weight: bold; color: #0000ff}</style>\n" +
			"  <h1 style=\"color:#0000ff;font-weight:bold;\">Hello World</h1>\n" +
			" </body>\n" +
			"</html>"
		;
		String actual = preflight.preprocessEmailHtml(html);

		assertStringsEqualWithDiffHighlight(expected, actual);
	}

	@Test
	public void preprocessEmailHtml_should_process_rgb_colors_as_hex_for_maximum_legacy_compatibility() {
		String html = "<html><body><style>h1 {font-weight: bold; color: rgb(0, 0, 255)}</style><h1>Hello World</h1></body></html>";

		String expected = "<html>\n" +
			" <head></head>\n" +
			" <body>\n" +
			"  <style>h1 {font-weight: bold; color: rgb(0, 0, 255)}</style>\n" +
			"  <h1 style=\"color:#0000ff;font-weight:bold;\">Hello World</h1>\n" +
			" </body>\n" +
			"</html>"
		;
		String actual = preflight.preprocessEmailHtml(html);

		assertStringsEqualWithDiffHighlight(expected, actual);
	}

	@Test
	public void preprocessEmailHtml_should_convert_hex_colors_to_rgb_when_useHexColors_false() {
		String html = "<html><body><style>h1 {font-weight: bold; color: #0000ff}</style><h1>Hello World</h1></body></html>";

		PreflightConfig config = preflight.createConfig();
		config.withUseHexColors(false);

		String expected = "<html>\n" +
			" <head></head>\n" +
			" <body>\n" +
			"  <style>h1 {font-weight: bold; color: #0000ff}</style>\n" +
			"  <h1 style=\"color:rgb(0, 0, 255);font-weight:bold;\">Hello World</h1>\n" +
			" </body>\n" +
			"</html>"
		;
		String actual = preflight.preprocessEmailHtml(html, config);

		assertStringsEqualWithDiffHighlight(expected, actual);
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
	public void preprocessEmailHtml_should_process_as_xhtml() {
		String templatePath = "templates/postmark/basic/example/content.html";
		String expectedPath = "assertions/templates/postmark/basic/example_default_as_xhtml.html";

		String template = StringAssertions.readFile(templatePath);
		PreflightConfig config = preflight.createConfig();

		// create custom output settings for jSoup
		OutputSettings settings = new OutputSettings();
		settings
			.escapeMode(EscapeMode.xhtml)
			.syntax(Syntax.xml)
		;
		// update the configuration
		config.withOutputSettings(settings);
	
		String expected = StringAssertions.readFile(expectedPath);
		String actual = preflight.preprocessEmailHtml(template, config);

		StringAssertions.saveFile("target/test-output/", "preprocessEmailHtml_test_actual.html", actual);		

		assertStringsEqualWithDiffHighlight(expected, actual);
	}

	@Test
	public void preprocessEmailHtml_should_process_with_custom_augmentedCssPrefix() {
		String templatePath = "templates/postmark/basic/example/content.html";
		String expectedPath = "assertions/templates/postmark/basic/example_default_augmentedCssPrefix.html";

		String template = StringAssertions.readFile(templatePath);
		PreflightConfig config = preflight.createConfig()
			.withAugmentedCssPrefix("-premailer-")
		;
	
		String expected = StringAssertions.readFile(expectedPath);
		String actual = preflight.preprocessEmailHtml(template, config);

		assertStringsEqualWithDiffHighlight(expected, actual);
	}

	@Test
	public void preprocessEmailHtml_should_process_basic_example_without_targeting_legacy_clients() {
		String templatePath = "templates/postmark/basic/example/content.html";
		String expectedPath = "assertions/templates/postmark/basic/example_default_without_legacy_formatting.html";

		PreflightConfig config = preflight.createConfig();
		config.withTargetLegacyClients(false);

		String template = StringAssertions.readFile(templatePath);
	
		String expected = StringAssertions.readFile(expectedPath);
		String actual = preflight.preprocessEmailHtml(template, config);

		assertStringsEqualWithDiffHighlight(expected, actual);
	}

	@Test
	public void preprocessEmailHtml_should_process_basic_example_but_target_modern_browsers_with_rgb_colors_and_no_bgcolor_attribute() {
		String templatePath = "templates/postmark/basic/example/content.html";
		String expectedPath = "assertions/templates/postmark/basic/example_default_with_rgb_colors.html";

		PreflightConfig config = preflight.createConfig();
		config.withTargetLegacyClients(false);

		String template = StringAssertions.readFile(templatePath);
	
		String expected = StringAssertions.readFile(expectedPath);
		String actual = preflight.preprocessEmailHtml(template, config);

		assertStringsEqualWithDiffHighlight(expected, actual);
	}

	@Test
	public void preprocessEmailHtml_should_process_remove_classes_and_styles() {
		String templatePath = "templates/postmark/basic/example/content.html";
		String expectedPath = "assertions/templates/postmark/basic/example_classes_and_styles_purges.html";

		PreflightConfig config = preflight.createConfig();
		config.withRemoveCssClasses(true);

		String template = StringAssertions.readFile(templatePath);
	
		String expected = StringAssertions.readFile(expectedPath);
		String actual = preflight.preprocessEmailHtml(template, config);

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
