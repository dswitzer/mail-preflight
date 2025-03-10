package eu.kk42.mailpreflight.processors;

import eu.kk42.mailpreflight.domain.IPreflightProcessor;
import eu.kk42.mailpreflight.domain.PreflightConfig;

import java.util.LinkedList;

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
        HtmlCommentsRemovalProcessor.removeComments(document);
    }

    public static void removeComments(Document document) {
        /*
         * To avoid Stack Overflow issues with deeply nested documents, we avoid recursion 
         * by using a stack to process the nodes.
         */
        LinkedList<Node> stack = new LinkedList<Node>();
        stack.addAll(document.childNodes());

        // process our stack
        while ( !stack.isEmpty() ) {
            Node node = stack.poll();

            if ( node instanceof Comment ) {
                if (!isConditionalComment((Comment) node)) {
                    node.remove();
                    node = null;
                }
            }

            // when we have not remove the node, we must process its children
            if( node != null ){
                stack.addAll(node.childNodes());
            }
        }

    }


    private static boolean isConditionalComment(Comment comment) {
        return comment.getData().startsWith("[if");
    }
}
