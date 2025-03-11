package eu.kk42.mailpreflight.processors;

import com.steadystate.css.dom.CSSRuleListImpl;
import com.steadystate.css.dom.CSSStyleDeclarationImpl;
import com.steadystate.css.dom.CSSStyleRuleImpl;
import com.steadystate.css.dom.CSSStyleSheetImpl;
import com.steadystate.css.dom.CSSValueImpl;
import com.steadystate.css.dom.RGBColorImpl;
import com.steadystate.css.format.CSSFormat;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import eu.kk42.mailpreflight.domain.CssSpecificity;
import eu.kk42.mailpreflight.domain.CssValueWithSpecificity;
import eu.kk42.mailpreflight.domain.IPreflightProcessor;
import eu.kk42.mailpreflight.domain.PreflightConfig;
import eu.kk42.mailpreflight.domain.utils.W3CNamedColor;
import eu.kk42.mailpreflight.exception.MailPreflightException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.NegativeCondition;
import org.w3c.css.sac.NegativeSelector;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.css.sac.SiblingSelector;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleRule;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Inlines css styles using Jsoup and CssParser libraries. Skips @media queries. Honours css priority rules.
 * <p>
 * Initially based on https://gist.github.com/moodysalem/69e2966834a1f79492a9 and https://stackoverflow.com/a/21757928/2270046
 *
 * @author konstantinkastanov
 * created on 2019-03-28
 */
public class CssInlinerProcessor implements IPreflightProcessor {

    private static final Logger log = LoggerFactory.getLogger(CssInlinerProcessor.class);

    private static final String STYLE = "style";
    private static final CssSpecificity STYLE_SPECIFICITY = new CssSpecificity(0, 1, 0, 0, 0);
    private static final Map<String, CssSpecificity> SPECIFICITY_CACHE = new ConcurrentHashMap<>();


    @Override
    public void process(Document document, PreflightConfig config) {

        if(!config.isInlineCss()) {
            if (config.isUseAugmentedCss()) {
                throw new MailPreflightException("Configuration is invalid. 'useAugmentedCss' is enabled but 'inlineCss' is not. Can not apply augmented css without inlining css");
            }
            return;
        }

        List<InputSource> cssSources = this.extractCssElements(document);
        Set<String> elementsThatSupportBgColorAttr = new HashSet<>(Arrays.asList("body", "table", "tr", "th", "td"));

        CSSOMParser parser = getParser(); //Is this thread safe?
        // get the formatter to use when getting the CSS information
        CSSFormat formatter = config.getCssFormatter();

        if(cssSources.isEmpty()) {
            log.warn("Could not find any css style blocks in the html, css inlining won't be used.");
            return;
        }

        Map<Element, Map<String, CssValueWithSpecificity>> allElementsStyles = new HashMap<>();

        for(InputSource cssSource : cssSources) {
            CSSStyleSheetImpl stylesheet = null;

            try {
                stylesheet = (CSSStyleSheetImpl) parser.parseStyleSheet(cssSource, null, null);
            } catch(IOException ex) {
                throw new MailPreflightException("Failed to parse css", ex);
            }
            
            CSSRuleListImpl ruleList = (CSSRuleListImpl) stylesheet.getCssRules();


            if(log.isDebugEnabled()) {
                log.debug("Found {} css rules", ruleList.getLength());
            }

            for(int ruleIndex = 0; ruleIndex < ruleList.getLength(); ruleIndex++) {
                CSSRule item = ruleList.item(ruleIndex);

                if(item instanceof CSSStyleRule) {
                    CSSStyleRuleImpl styleRule = (CSSStyleRuleImpl) item;
                    String cssSelector = styleRule.getSelectorText();

                    SelectorList cssSelectors;
                    try {
                        cssSelectors = parser.parseSelectors(new InputSource(new StringReader(cssSelector)));
                    } catch(IOException ex) {
                        throw new MailPreflightException("Failed to parse css selector: " + cssSelector, ex);
                    }


                    for(int selectorIndex = 0; selectorIndex < cssSelectors.getLength(); selectorIndex++) {
                        Selector selector = cssSelectors.item(selectorIndex);
                        Elements elements = document.select(selector.toString());
                        CSSStyleDeclarationImpl style = (CSSStyleDeclarationImpl) styleRule.getStyle();

                        if(log.isTraceEnabled()) {
                            log.trace("Found {} elements for css selector {}", elements.size(), cssSelector);
                        }

                        for(Element element : elements) {
                            Map<String, CssValueWithSpecificity> elementStyles = allElementsStyles.computeIfAbsent(element,
                                    k->stylesOf(
                                            STYLE_SPECIFICITY, element.attr(STYLE)));

                            for(int propertyIndex = 0; propertyIndex < style.getLength(); propertyIndex++) {
                                String propertyName = style.item(propertyIndex);
                                /*
                                 * In order to apply our formatting rules, we must get the CSSValue implementation
                                 * so we can apply our formatting options.
                                 * 
                                 * If we want the parsed value (which is what the original code used), we could use:
                                 * 
                                 * String propertyValue = style.getPropertyValue(propertyName);
                                 */
                                String propertyValue = ((CSSValueImpl) style.getPropertyCSSValue(propertyName)).getCssText(formatter);

                                CssValueWithSpecificity CssValueWithSpecificity = new CssValueWithSpecificity(propertyValue,
                                        calculatePropertySpecificity(selector, styleRule, propertyName));
                                this.addIfHigherPriority(elementStyles, propertyName, CssValueWithSpecificity);
                            }
                        }
                    }
                }
            }
        }

        final String AUGMENTED_CSS_PREFIX = config.getAugmentedCssPrefix();

        for(Map.Entry<Element, Map<String, CssValueWithSpecificity>> elementEntry : allElementsStyles.entrySet()) {
            Element element = elementEntry.getKey();
            StringBuilder builder = new StringBuilder();
            for(Map.Entry<String, CssValueWithSpecificity> styleEntry : elementEntry.getValue().entrySet()) {
                String propertyName = styleEntry.getKey();
                String propertyValue = styleEntry.getValue().getValue(); //TODO: add !important if necessary

                if(propertyName.startsWith(AUGMENTED_CSS_PREFIX)) {
                    if(config.isUseAugmentedCss()) {
                        String attributeName = propertyName.substring(AUGMENTED_CSS_PREFIX.length());
                        if (element.attr(attributeName).length() == 0) { //Do not override existing attributes
                            element.attr(attributeName, propertyValue);
                        }
                    }
                } else {
                    builder.append(styleEntry.getKey()).append(":").append(styleEntry.getValue().getValue()).append(";");
                }
            }
            element.attr(STYLE, builder.toString());

            /*
             * For tags that support the "bgcolor" attribute, we might need to add the attribute
             * to the element.
             */
            if( config.isTargetLegacyClients() && elementsThatSupportBgColorAttr.contains(element.tagName().toLowerCase()) ){
                String bgColor = extractHexColorFromInlineStyles(element.attr(STYLE));

                // when we detected a color, we add the color attribute
                if( (bgColor != null) && (bgColor.length() > 0 ) ){
                    element.attr("bgcolor", bgColor);
                }
            }

        }

    }

    //This method is mainly intended for testing. use calculateSelectorSpecificity(Selector) for internal use.
    public String extractHexColorFromInlineStyles(String styles) {
        CSSStyleDeclarationImpl cssStyles = new CSSStyleDeclarationImpl();
        cssStyles.setCssText(styles);

        // we need to force bgcolors to hex
        CSSFormat formatter = new CSSFormat().setRgbAsHex(true);

        // we want to search in order CSS would place precedence
        Set<String> bgProperties = new HashSet<String>(Arrays.asList("background-color", "background"));

        for( String property : bgProperties ){
            CSSValueImpl value = (CSSValueImpl) cssStyles.getPropertyCSSValue(property);
            RGBColorImpl rgbValue = findRGBColorImpl(value);

            if( rgbValue instanceof RGBColorImpl ){
                return rgbValue.getCssText(formatter);
            }
        }

        // we could find no color
        return null;
    }

    public RGBColorImpl createRGBColorImpl(String hex) {
        // when we have a "named" color, convert it to hex
        if( W3CNamedColor.isNamedColor(hex) ){
            hex = W3CNamedColor.getNamedColor(hex).asHex();
        }

        // we need to create our CSSValue so we can get the RGBColor instance
        CSSValueImpl color = new CSSValueImpl();
        color.setCssText(hex);

        return (RGBColorImpl) color.getRGBColorValue();
    }

    public RGBColorImpl findRGBColorImpl(CSSValueImpl value) {
        // if our value is null, there is nothing to find
        if( value == null ) return null;

        Object lexValue = (Object) value.getValue();

        // some CSS properties (like background) might return many values, so we need to find the color
        if( lexValue instanceof ArrayList<?>){
            lexValue = findRGBColorImpl((ArrayList<?>) lexValue);
        }

        if( lexValue instanceof RGBColorImpl ){
            return (RGBColorImpl) lexValue;

        // check if we have a "named" color (aliceblue, rebeccapurple, white, black, etc)
        } else if( W3CNamedColor.isNamedColor(((CSSValueImpl)lexValue).getCssText()) ){
            return createRGBColorImpl(((CSSValueImpl)lexValue).getCssText());
        }

        return null;
    }

    public RGBColorImpl findRGBColorImpl(ArrayList<?> values) {
        for( Object item : (ArrayList<?>) values ){
            if( item instanceof CSSValueImpl ){
                CSSValueImpl itemValue = (CSSValueImpl) item;

                if( itemValue.getPrimitiveType() ==  CSSValueImpl.CSS_RGBCOLOR ){
                    return (RGBColorImpl) itemValue.getRGBColorValue();

                // check if we have a "named" color (aliceblue, rebeccapurple, white, black, etc)
                } else if( W3CNamedColor.isNamedColor(itemValue.getCssText()) ){
                    return createRGBColorImpl(itemValue.getCssText());
                }
            }
        }

        // if we cannot find a match, return null
        return (RGBColorImpl) null;
    }


    //This method is mainly intended for testing. use calculateSelectorSpecificity(Selector) for internal use.
    public CssSpecificity calculateSelectorSpecificity(String selector) {
        if(SPECIFICITY_CACHE.containsKey(selector)) {
            return SPECIFICITY_CACHE.get(selector);
        }

        return this.calculateSelectorSpecificity(this.parseSingleSelector(selector));
    }


    private void addIfHigherPriority(Map<String, CssValueWithSpecificity> currentProperties,
                                     String propertyName, CssValueWithSpecificity newValue) {
        CssValueWithSpecificity oldValue = currentProperties.get(propertyName);

        if(oldValue == null) {
            currentProperties.put(propertyName, newValue);
        } else {

            int compare = oldValue.getSpecificity().compareTo(newValue.getSpecificity());
            if(compare <= 0) {
                currentProperties.put(propertyName, newValue);
            }
        }
    }


    public CssSpecificity calculateSelectorSpecificity(Selector selector) {
        String selectorStr = selector.toString();

        if(SPECIFICITY_CACHE.containsKey(selectorStr)) {
            return SPECIFICITY_CACHE.get(selectorStr);
        }

        CssSpecificity specificity = selectorsSpecificityInternal(new CssSpecificity(), selector);

        SPECIFICITY_CACHE.put(selectorStr, specificity);
        return specificity;
    }

    private CssSpecificity selectorsSpecificityInternal(CssSpecificity specificity, Selector selector) {

        if(selector instanceof DescendantSelector) {
            DescendantSelector descSelector = (DescendantSelector) selector;
            specificity = selectorsSpecificityInternal(specificity, descSelector.getAncestorSelector());
            specificity = selectorsSpecificityInternal(specificity, descSelector.getSimpleSelector());
        } else if(selector instanceof SiblingSelector) {
            SiblingSelector siblSelector = (SiblingSelector) selector;
            specificity = selectorsSpecificityInternal(specificity, siblSelector.getSelector());
            specificity = selectorsSpecificityInternal(specificity, siblSelector.getSiblingSelector());
        } else if(selector instanceof NegativeSelector) {
            NegativeSelector negSelector = (NegativeSelector) selector;
            specificity = selectorsSpecificityInternal(specificity, negSelector.getSimpleSelector());
        } else if(selector instanceof ConditionalSelector) {
            ConditionalSelector condSelector = (ConditionalSelector) selector;
            specificity = selectorsSpecificityInternal(specificity, condSelector.getSimpleSelector());
            specificity = selectorConditionSpecificity(specificity, condSelector.getCondition());
        } else if(selector instanceof ElementSelector) {
            ElementSelector elemSelector = (ElementSelector) selector;
            String selectorText = elemSelector.toString();
            if(selectorText.length() > 0 && !"*".equals(selectorText)) {
                specificity.incrementD();
            }
        }
        return specificity;
    }

    private CssSpecificity selectorConditionSpecificity(CssSpecificity specificity, Condition condition) {

        if(condition instanceof NegativeCondition) {
            NegativeCondition negCondition = (NegativeCondition) condition;
            specificity = selectorConditionSpecificity(specificity, negCondition.getCondition());
        } else if(condition instanceof CombinatorCondition) {
            CombinatorCondition comCondition = (CombinatorCondition) condition;
            specificity = selectorConditionSpecificity(specificity, comCondition.getFirstCondition());
            specificity = selectorConditionSpecificity(specificity, comCondition.getSecondCondition());
        } else if(condition instanceof AttributeCondition) {
            AttributeCondition attrCondition = (AttributeCondition) condition;
            switch(attrCondition.getConditionType()) {
                case Condition.SAC_ID_CONDITION:
                    specificity.incrementB();
                    break;
                case Condition.SAC_PSEUDO_CLASS_CONDITION:
                    String value = attrCondition.getValue();
                    if(value.startsWith("not(")) { //This is a workaround. CssParser is parsing 'not' conditions as pseudoclasses (which is kinda correct, but not what we want)
                        String notSelector = value.substring("not(".length(), value.length() - 1);
                        this.selectorsSpecificityInternal(specificity, parseSingleSelector(notSelector));
                    } else {
                        specificity.incrementC();
                    }
                    break;
                default:
                    specificity.incrementC();
                    break;
            }
        } else {
            specificity.incrementC(); //Not sure if that is correct.
        }

        return specificity;
    }

    private CssSpecificity calculatePropertySpecificity(Selector selector, CSSStyleRule styleRule, String propertyName) {
        CssSpecificity selectorSpecificity = this.calculateSelectorSpecificity(selector);
        String priorityKeyword = styleRule.getStyle().getPropertyPriority(propertyName);

        if(priorityKeyword == null || priorityKeyword.length() == 0) {
            return selectorSpecificity;
        } else {
            CssSpecificity propertySpecificity = selectorSpecificity.clone();
            propertySpecificity.incrementX();
            return propertySpecificity;
        }
    }


    private static HashMap<String, CssValueWithSpecificity> stylesOf(CssSpecificity specificity, String properties) {
        HashMap<String, CssValueWithSpecificity> vp = new HashMap<>();
        if(properties == null || properties.trim().length() == 0) {
            return vp;
        }
        String[] props = properties.split(";");
        if(props.length > 0) {
            for(String p : props) {
                String[] pcs = p.split(":", 2);
                if(pcs.length != 2) {
                    continue;
                }
                String name = pcs[0].trim();
                String value = pcs[1].trim();
                CssValueWithSpecificity vwp = new CssValueWithSpecificity(value, specificity);
                vp.put(name, vwp);
            }
        }
        return vp;
    }


    private Selector parseSingleSelector(String selector) {
        CSSOMParser parser = getParser();

        SelectorList cssSelectors;
        try {
            cssSelectors = parser.parseSelectors(new InputSource(new StringReader(selector)));
        } catch(IOException ex) {
            throw new MailPreflightException("Failed to parse css selector: " + selector, ex);
        }

        if(cssSelectors.getLength() > 1) {
            throw new MailPreflightException("Can't calculate specificity for multiple selectors: " + selector);
        } else if(cssSelectors.getLength() == 0) {
            throw new MailPreflightException("Can't calculate specificity for empty selector: " + selector);
        } else {
            return cssSelectors.item(0);
        }
    }


    protected List<InputSource> extractCssElements(Document document) {
        Elements styleTags = document.select(STYLE);
        return styleTags.stream()
                        .map(style->style.getAllElements().get(0).data().trim())
                        .map(styleStr->new InputSource(new StringReader(styleStr)))
                        .collect(Collectors.toList());
    }

    protected CSSOMParser getParser() {
        return new CSSOMParser(new SACParserCSS3());
    }
}
