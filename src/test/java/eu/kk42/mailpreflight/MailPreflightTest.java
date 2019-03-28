package eu.kk42.mailpreflight;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konstantinkastanov
 * created on 2019-03-29
 */
public class MailPreflightTest {
	private static final Logger log = LoggerFactory.getLogger(MailPreflightTest.class);
	private MailPreflight preflight = new MailPreflight();

	@Test
	public void preprocessEmailHtml_shouldNotFail_smokeTest() {
		//This is just a smoke test to check that no exceptions are thrown in MailPreflight plumbing. Individual processors are tested in separate test classes.
		String result = preflight.preprocessEmailHtml("<html><body><style>h1 {font-weigth: bold}</style><h1>OUTATIME</h1></body></html>");
		//log.info(result);
	}
}
