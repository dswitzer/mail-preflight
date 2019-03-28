package eu.kk42.mailpreflight.domain;

/**
 * @author konstantinkastanov
 * created on 2019-03-28
 */
public class PreflightConfig {
	private boolean inlineCss = true;
	private boolean useAugmentedCss = true;
	private boolean removeHtmlComments = true;

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
}
