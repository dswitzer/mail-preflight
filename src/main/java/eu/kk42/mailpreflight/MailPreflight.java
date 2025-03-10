package eu.kk42.mailpreflight;

import eu.kk42.mailpreflight.domain.IPreflightProcessor;
import eu.kk42.mailpreflight.domain.PreflightConfig;
import eu.kk42.mailpreflight.processors.CssInlinerProcessor;
import eu.kk42.mailpreflight.processors.HtmlCommentsRemovalProcessor;
import eu.kk42.mailpreflight.processors.HtmlCssRemovalProcessor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author konstantinkastanov
 * <p>
 * created on 2019-03-18
 */
public class MailPreflight {
	// private static final Logger log = LoggerFactory.getLogger(MailPreflight.class);


	private final List<IPreflightProcessor> processors;

	public MailPreflight() {
		processors = Arrays.asList(
				new CssInlinerProcessor(),
				new HtmlCommentsRemovalProcessor(),
				new HtmlCssRemovalProcessor()
		);
	}

	// allow custom processors to be added
	public MailPreflight withProcessor(IPreflightProcessor processor) {
		processors.add(processor);

		return this;
	}

	public PreflightConfig createConfig() {
		return new PreflightConfig();
	}

	public String preprocessEmailHtml(String html) {
		return this.preprocessEmailHtml(html, this.createConfig());
	}

	public String preprocessEmailHtml(PreflightConfig config, String html) {
		return this.preprocessEmailHtml(config, html);
	}

	public String preprocessEmailHtml(String html, PreflightConfig config) {
		Document document = Jsoup.parse(html);

		// apply the processors
		for (IPreflightProcessor processor : processors) {
			processor.process(document, config);
		}

		// apply the jSoup output settings which have been configured
		config.applyOutputSettings(document);

		return document.html();
	}
}
