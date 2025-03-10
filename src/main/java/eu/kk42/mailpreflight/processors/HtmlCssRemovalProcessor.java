package eu.kk42.mailpreflight.processors;

import eu.kk42.mailpreflight.domain.IPreflightProcessor;
import eu.kk42.mailpreflight.domain.PreflightConfig;

import java.util.LinkedList;
import java.util.regex.Pattern;

import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

/**
 * @author dswitzer
 * created on 2025-03-10
 */
public class HtmlCssRemovalProcessor implements IPreflightProcessor {

    @Override
    public void process(Document document, PreflightConfig config) {
        if( config.isRemoveCssClasses() ){
            HtmlCssRemovalProcessor.removeStyleElements(document);
            HtmlCssRemovalProcessor.removeCLassAttributes(document);
        }
    }

    public static void removeStyleElements(Document document) {
        // remove the style elements
        document.select("style").remove();

        // remove conditional comments
        HtmlCssRemovalProcessor.removeConditionalStyleComments(document);
    }

    public static void removeCLassAttributes(Document document) {
        // Select all elements and remove the "class" attribute
        for (Element element : document.getAllElements()) {
            element.removeAttr("class");
        }        
    }

    public static void removeConditionalStyleComments(Document document) {
        /*
         * To avoid Stack Overflow issues with deeply nested documents, we avoid recursion 
         * by using a stack to process the nodes.
         */
        LinkedList<Node> stack = new LinkedList<Node>();
        stack.addAll(document.childNodes());

        // we only want comments with a <style> element
        Pattern pattern = Pattern.compile("<style[^>]*?>");

        // process our stack
        while ( !stack.isEmpty() ) {
            Node node = stack.poll();

            if ( node instanceof Comment ) {
                String comment = ((Comment) node).getData();

                if (comment.contains("[if") && comment.contains("<![endif]") && pattern.matcher(comment).find() ) {
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
}
