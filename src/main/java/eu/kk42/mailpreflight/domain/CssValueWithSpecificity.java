package eu.kk42.mailpreflight.domain;

/**
 * @author konstantinkastanov
 * created on 2019-03-28
 */
public class CssValueWithSpecificity {
    private String value;
    private CssSpecificity specificity;

    public CssValueWithSpecificity(String value, CssSpecificity specificity) {
        this.value = value;
        this.specificity = specificity;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public CssSpecificity getSpecificity() {
        return specificity;
    }

    public void setSpecificity(CssSpecificity specificity) {
        this.specificity = specificity;
    }
}
