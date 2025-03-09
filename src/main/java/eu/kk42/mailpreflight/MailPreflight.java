package eu.kk42.mailpreflight;

import eu.kk42.mailpreflight.domain.IPreflightProcessor;
import eu.kk42.mailpreflight.domain.PreflightConfig;
import eu.kk42.mailpreflight.processors.CssInlinerProcessor;
import eu.kk42.mailpreflight.processors.HtmlCommentsRemovalProcessor;
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
				new HtmlCommentsRemovalProcessor()
		);
	}

	public String preprocessEmailHtml(String html) {
		return this.preprocessEmailHtml(html, new PreflightConfig());
	}

	public String preprocessEmailHtml(String html, PreflightConfig config) {
		Document document = Jsoup.parse(html);

		for (IPreflightProcessor processor : processors) {
			processor.process(document, config);
		}

		return document.html();
	}
}
