package eu.kk42.mailpreflight.domain;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;

import com.steadystate.css.format.CSSFormat;

/**
 * @author konstantinkastanov
 * created on 2019-03-28
 */
public class PreflightConfig {
	private boolean inlineCss = true;
	private boolean useAugmentedCss = true;
	private String augmentedCssPrefix = "-mailpreflight-";
	private boolean removeHtmlComments = true;
	private boolean removeCssClasses = false;
	// for Jsoup processing
	private Boolean prettyPrint;
	private OutputSettings outputSettings;
	// for CSSOMParser processing
	private boolean targetLegacyClients = true;
	private Boolean useHexColors;
	private CSSFormat cssFormatter;

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

	public String getAugmentedCssPrefix() {
		return augmentedCssPrefix;
	}

	public PreflightConfig withAugmentedCssPrefix(String augmentedCssPrefix) {
		this.augmentedCssPrefix = augmentedCssPrefix;
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

	// for Jsoup processing
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

	// for CSSOMParser processing
	public boolean isTargetLegacyClients() {
		return this.targetLegacyClients;
	}

	public PreflightConfig withTargetLegacyClients(boolean targetLegacyClients) {
		this.targetLegacyClients = targetLegacyClients;

		/*
		 * When targeting legacy clients, we need to use hex colors.
		 * 
		 * If we have not specifically defined a whether or not we want to use
		 * hex colors, then we should mirror the setting to the hex colors because
		 * the two options are tightly coupled.
		 */

		// when targeting legacy clients, we need to use hex colors (but we should mirror the setting when not d)
		if( this.targetLegacyClients || !hasUseHexColors() ){
			this.useHexColors = this.targetLegacyClients;
		}
		
		return this;
	}

	public boolean hasUseHexColors() {
		return (this.useHexColors == null) ? false : true;
	}

	public boolean isUseHexColors() {
		return hasUseHexColors() ? this.useHexColors : true;
	}

	public PreflightConfig withUseHexColors(boolean useHexColors) {
		this.useHexColors = useHexColors;
		return this;
	}

	public boolean hasCssFormatter() {
		return (this.cssFormatter instanceof CSSFormat) ? true : false;
	}

	public PreflightConfig withCssFormatter(CSSFormat cssFormatter) {
		this.cssFormatter = cssFormatter;
		return this;
	}

	public CSSFormat getCssFormatter() {
		CSSFormat formatter = hasCssFormatter() ? this.cssFormatter : new CSSFormat();

		/*
		 * We want to apply our hex setting whenever explicitly set
		 * or if the user has not supplied a formatter, we apply the
		 * default formatting rule.
		 */
		if( !hasCssFormatter() || hasUseHexColors() ){
			formatter.setRgbAsHex(isUseHexColors());
		}
	
		return formatter;
	}
}
