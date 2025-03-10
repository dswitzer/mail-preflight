package eu.kk42.mailpreflight.domain;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;

/**
 * @author konstantinkastanov
 * created on 2019-03-28
 */
public class PreflightConfig {
	private boolean inlineCss = true;
	private boolean useAugmentedCss = true;
	private boolean removeHtmlComments = true;
	private boolean removeCssClasses = false;
	private Boolean prettyPrint;
	private OutputSettings outputSettings;

	public boolean isInlineCss() {
		return inlineCss;
	}

	public PreflightConfig withInlineCss(boolean inlineCss) {
		this.inlineCss = inlineCss;
		return this;
	}

	public boolean isUseAugmentedCss() {
		return useAugmentedCss;
	}

	public PreflightConfig withAugmentedCss(boolean useAugmentedCss) {
		this.useAugmentedCss = useAugmentedCss;
		return this;
	}

	public boolean isRemoveHtmlComments() {
		return removeHtmlComments;
	}

	public PreflightConfig withRemoveHtmlComments(boolean removeHtmlComments) {
		this.removeHtmlComments = removeHtmlComments;
		return this;
	}

	public boolean isRemoveCssClasses() {
		return removeCssClasses;
	}

	public PreflightConfig withRemoveCssClasses(boolean removeCssClasses) {
		this.removeCssClasses = removeCssClasses;
		return this;
	}

	public boolean hasPrettyPrint() {
		return (this.prettyPrint == null) ? false : true;
	}

	public boolean isPrettyPrint() {
		return hasPrettyPrint() ? this.prettyPrint : true;
	}

	public PreflightConfig withPrettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
		return this;
	}

	public boolean hasOutputSettings() {
		return (this.outputSettings instanceof OutputSettings) ? true : false;
	}

	public PreflightConfig withOutputSettings(OutputSettings outputSettings) {
		this.outputSettings = outputSettings;
		return this;
	}

	public PreflightConfig applyOutputSettings(Document document) {
		if( hasOutputSettings() ){
			document.outputSettings(this.outputSettings);
		}
	
		// when we define a pretty print, apply it
		if( hasPrettyPrint() ){
			document.outputSettings().prettyPrint(this.prettyPrint);
		}
	
		return this;
	}
}
