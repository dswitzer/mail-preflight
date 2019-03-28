package eu.kk42.mailpreflight.domain;

import java.util.Objects;

/**
 * @author konstantinkastanov
 * created on 2019-03-28
 */
public class CssSpecificity implements Comparable<CssSpecificity>, Cloneable {
    //Variable naming is based on CSS2.2 spec: https://www.w3.org/TR/CSS22/cascade.html#specificity
    private int x; //!import
    private int a; //style is located in STYLE attribute
    private int b; //ID selector
    private int c; //classes, various attributes and pseudo classes selectors
    private int d; //Element and pseudo element selectors

    public CssSpecificity() {
    }

    public CssSpecificity(int x, int a, int b, int c, int d) {
        this.x = x;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public int getD() {
        return d;
    }

    public void setD(int d) {
        this.d = d;
    }

    public void incrementA() {
        a++;
    }

    public void incrementB() {
        b++;
    }

    public void incrementC() {
        c++;
    }

    public void incrementD() {
        d++;
    }

    public void incrementX() {
        x++;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || !(o instanceof CssSpecificity)) return false;
        CssSpecificity that = (CssSpecificity) o;
        return x == that.x &&
               a == that.a &&
               b == that.b &&
               c == that.c &&
               d == that.d;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, a, b, c, d);
    }

    @Override
    public int compareTo(CssSpecificity o) {
        if(o == this) {
            return 0;
        }
        int comp = Integer.compare(getX(), o.getX());
        if(comp != 0) {
            return comp;
        }
        comp = Integer.compare(getA(), o.getA());
        if(comp != 0) {
            return comp;
        }
        comp = Integer.compare(getB(), o.getB());
        if(comp != 0) {
            return comp;
        }
        comp = Integer.compare(getC(), o.getC());
        if(comp != 0) {
            return comp;
        }
        return Integer.compare(getD(), o.getD());
    }

    @Override
    public CssSpecificity clone() {
        return new CssSpecificity(x, a, b, c, d);
    }

    @Override
    public String toString() {
        return x + "," + a + "," + b + "," + c + "," + d;
    }
}
