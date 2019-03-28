package eu.kk42.mailpreflight.processors;

import eu.kk42.mailpreflight.domain.IPreflightProcessor;
import eu.kk42.mailpreflight.domain.PreflightConfig;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

/**
 * @author konstantinkastanov
 * created on 2019-03-28
 */
public class HtmlCommentsRemovalProcessor implements IPreflightProcessor {

    @Override
    public void process(Document document, PreflightConfig config) {
        this.removeComments(document);
    }

    protected void removeComments(Node node) {
        for (int i = 0; i < node.childNodeSize();) {
            Node child = node.childNode(i);
            if (child instanceof Comment) {
                if(!isConditionalComment((Comment) child)) {
                    child.remove();
                } else {
                    i++;
                }
            } else {
                removeComments(child);
                i++;
            }
        }
    }

    private boolean isConditionalComment(Comment comment) {
        return comment.getData().startsWith("[if");
    }
}
