package eu.kk42.mailpreflight.domain;

import org.jsoup.nodes.Document;

/**
 * @author konstantinkastanov
 * created on 2019-03-28
 */
public interface IPreflightProcessor {
    void process(Document document, PreflightConfig config);
}
