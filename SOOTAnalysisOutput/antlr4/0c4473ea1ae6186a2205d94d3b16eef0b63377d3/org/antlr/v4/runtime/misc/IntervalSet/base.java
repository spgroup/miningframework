package org.antlr.v4.runtime.misc;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class IntervalSet implements IntSet {

    public static final IntervalSet COMPLETE_CHAR_SET = IntervalSet.of(Lexer.MIN_CHAR_VALUE, Lexer.MAX_CHAR_VALUE);

    static {
        COMPLETE_CHAR_SET.setReadonly(true);
    }

    public static final IntervalSet EMPTY_SET = new IntervalSet();

    static {
        EMPTY_SET.setReadonly(true);
    }

    protected List<Interval> intervals;

    protected boolean readonly;

    public IntervalSet(List<Interval> intervals) {
        this.intervals = intervals;
    }

    public IntervalSet(IntervalSet set) {
        this();
        addAll(set);
    }

    public IntervalSet(int... els) {
        if (els == null) {
            intervals = new ArrayList<Interval>(2);
        } else {
            intervals = new ArrayList<Interval>(els.length);
            for (int e : els) add(e);
        }
    }

    public static IntervalSet of(int a) {
        IntervalSet s = new IntervalSet();
        s.add(a);
        return s;
    }

    public static IntervalSet of(int a, int b) {
        IntervalSet s = new IntervalSet();
        s.add(a, b);
        return s;
    }

    public void clear() {
        if (readonly)
            throw new IllegalStateException("can't alter readonly IntervalSet");
        intervals.clear();
    }

    @Override
    public void add(int el) {
        if (readonly)
            throw new IllegalStateException("can't alter readonly IntervalSet");
        add(el, el);
    }

    public void add(int a, int b) {
        add(Interval.of(a, b));
    }

    protected void add(Interval addition) {
        if (readonly)
            throw new IllegalStateException("can't alter readonly IntervalSet");
        if (addition.b < addition.a) {
            return;
        }
        for (ListIterator<Interval> iter = intervals.listIterator(); iter.hasNext(); ) {
            Interval r = iter.next();
            if (addition.equals(r)) {
                return;
            }
            if (addition.adjacent(r) || !addition.disjoint(r)) {
                Interval bigger = addition.union(r);
                iter.set(bigger);
                while (iter.hasNext()) {
                    Interval next = iter.next();
                    if (!bigger.adjacent(next) && bigger.disjoint(next)) {
                        break;
                    }
                    iter.remove();
                    iter.previous();
                    iter.set(bigger.union(next));
                    iter.next();
                }
                return;
            }
            if (addition.startsBeforeDisjoint(r)) {
                iter.previous();
                iter.add(addition);
                return;
            }
        }
        intervals.add(addition);
    }

    public static IntervalSet or(IntervalSet[] sets) {
        IntervalSet r = new IntervalSet();
        for (IntervalSet s : sets) r.addAll(s);
        return r;
    }

    @Override
    public IntervalSet addAll(IntSet set) {
        if (set == null) {
            return this;
        }
        if (set instanceof IntervalSet) {
            IntervalSet other = (IntervalSet) set;
            int n = other.intervals.size();
            for (int i = 0; i < n; i++) {
                Interval I = other.intervals.get(i);
                this.add(I.a, I.b);
            }
        } else {
            for (int value : set.toList()) {
                add(value);
            }
        }
        return this;
    }

    public IntervalSet complement(int minElement, int maxElement) {
        return this.complement(IntervalSet.of(minElement, maxElement));
    }

    @Override
    public IntervalSet complement(IntSet vocabulary) {
        if (vocabulary == null || vocabulary.isNil()) {
            return null;
        }
        IntervalSet vocabularyIS;
        if (vocabulary instanceof IntervalSet) {
            vocabularyIS = (IntervalSet) vocabulary;
        } else {
            vocabularyIS = new IntervalSet();
            vocabularyIS.addAll(vocabulary);
        }
        return vocabularyIS.subtract(this);
    }

    @Override
    public IntervalSet subtract(IntSet a) {
        if (a == null || a.isNil()) {
            return new IntervalSet(this);
        }
        if (a instanceof IntervalSet) {
            return subtract(this, (IntervalSet) a);
        }
        IntervalSet other = new IntervalSet();
        other.addAll(a);
        return subtract(this, other);
    }

    public static IntervalSet subtract(IntervalSet left, IntervalSet right) {
        if (left == null || left.isNil()) {
            return new IntervalSet();
        }
        IntervalSet result = new IntervalSet(left);
        if (right == null || right.isNil()) {
            return result;
        }
        int resultI = 0;
        int rightI = 0;
        while (resultI < result.intervals.size() && rightI < right.intervals.size()) {
            Interval resultInterval = result.intervals.get(resultI);
            Interval rightInterval = right.intervals.get(rightI);
            if (rightInterval.b < resultInterval.a) {
                rightI++;
                continue;
            }
            if (rightInterval.a > resultInterval.b) {
                resultI++;
                continue;
            }
            Interval beforeCurrent = null;
            Interval afterCurrent = null;
            if (rightInterval.a > resultInterval.a) {
                beforeCurrent = new Interval(resultInterval.a, rightInterval.a - 1);
            }
            if (rightInterval.b < resultInterval.b) {
                afterCurrent = new Interval(rightInterval.b + 1, resultInterval.b);
            }
            if (beforeCurrent != null) {
                if (afterCurrent != null) {
                    result.intervals.set(resultI, beforeCurrent);
                    result.intervals.add(resultI + 1, afterCurrent);
                    resultI++;
                    rightI++;
                    continue;
                } else {
                    result.intervals.set(resultI, beforeCurrent);
                    resultI++;
                    continue;
                }
            } else {
                if (afterCurrent != null) {
                    result.intervals.set(resultI, afterCurrent);
                    rightI++;
                    continue;
                } else {
                    result.intervals.remove(resultI);
                    continue;
                }
            }
        }
        return result;
    }

    @Override
    public IntervalSet or(IntSet a) {
        IntervalSet o = new IntervalSet();
        o.addAll(this);
        o.addAll(a);
        return o;
    }

    @Override
    public IntervalSet and(IntSet other) {
        if (other == null) {
            return null;
        }
        List<Interval> myIntervals = this.intervals;
        List<Interval> theirIntervals = ((IntervalSet) other).intervals;
        IntervalSet intersection = null;
        int mySize = myIntervals.size();
        int theirSize = theirIntervals.size();
        int i = 0;
        int j = 0;
        while (i < mySize && j < theirSize) {
            Interval mine = myIntervals.get(i);
            Interval theirs = theirIntervals.get(j);
            if (mine.startsBeforeDisjoint(theirs)) {
                i++;
            } else if (theirs.startsBeforeDisjoint(mine)) {
                j++;
            } else if (mine.properlyContains(theirs)) {
                if (intersection == null) {
                    intersection = new IntervalSet();
                }
                intersection.add(mine.intersection(theirs));
                j++;
            } else if (theirs.properlyContains(mine)) {
                if (intersection == null) {
                    intersection = new IntervalSet();
                }
                intersection.add(mine.intersection(theirs));
                i++;
            } else if (!mine.disjoint(theirs)) {
                if (intersection == null) {
                    intersection = new IntervalSet();
                }
                intersection.add(mine.intersection(theirs));
                if (mine.startsAfterNonDisjoint(theirs)) {
                    j++;
                } else if (theirs.startsAfterNonDisjoint(mine)) {
                    i++;
                }
            }
        }
        if (intersection == null) {
            return new IntervalSet();
        }
        return intersection;
    }

    @Override
    public boolean contains(int el) {
        int n = intervals.size();
        for (int i = 0; i < n; i++) {
            Interval I = intervals.get(i);
            int a = I.a;
            int b = I.b;
            if (el < a) {
                break;
            }
            if (el >= a && el <= b) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNil() {
        return intervals == null || intervals.isEmpty();
    }

    public int getMaxElement() {
        if (isNil()) {
            throw new RuntimeException("set is empty");
        }
        Interval last = intervals.get(intervals.size() - 1);
        return last.b;
    }

    public int getMinElement() {
        if (isNil()) {
            throw new RuntimeException("set is empty");
        }
        return intervals.get(0).a;
    }

    public List<Interval> getIntervals() {
        return intervals;
    }

    @Override
    public int hashCode() {
        int hash = MurmurHash.initialize();
        for (Interval I : intervals) {
            hash = MurmurHash.update(hash, I.a);
            hash = MurmurHash.update(hash, I.b);
        }
        hash = MurmurHash.finish(hash, intervals.size() * 2);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof IntervalSet)) {
            return false;
        }
        IntervalSet other = (IntervalSet) obj;
        return this.intervals.equals(other.intervals);
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean elemAreChar) {
        StringBuilder buf = new StringBuilder();
        if (this.intervals == null || this.intervals.isEmpty()) {
            return "{}";
        }
        if (this.size() > 1) {
            buf.append("{");
        }
        Iterator<Interval> iter = this.intervals.iterator();
        while (iter.hasNext()) {
            Interval I = iter.next();
            int a = I.a;
            int b = I.b;
            if (a == b) {
                if (a == Token.EOF)
                    buf.append("<EOF>");
                else if (elemAreChar)
                    buf.append("'").appendCodePoint(a).append("'");
                else
                    buf.append(a);
            } else {
                if (elemAreChar)
                    buf.append("'").appendCodePoint(a).append("'..'").appendCodePoint(b).append("'");
                else
                    buf.append(a).append("..").append(b);
            }
            if (iter.hasNext()) {
                buf.append(", ");
            }
        }
        if (this.size() > 1) {
            buf.append("}");
        }
        return buf.toString();
    }

    @Deprecated
    public String toString(String[] tokenNames) {
        return toString(VocabularyImpl.fromTokenNames(tokenNames));
    }

    public String toString(Vocabulary vocabulary) {
        StringBuilder buf = new StringBuilder();
        if (this.intervals == null || this.intervals.isEmpty()) {
            return "{}";
        }
        if (this.size() > 1) {
            buf.append("{");
        }
        Iterator<Interval> iter = this.intervals.iterator();
        while (iter.hasNext()) {
            Interval I = iter.next();
            int a = I.a;
            int b = I.b;
            if (a == b) {
                buf.append(elementName(vocabulary, a));
            } else {
                for (int i = a; i <= b; i++) {
                    if (i > a)
                        buf.append(", ");
                    buf.append(elementName(vocabulary, i));
                }
            }
            if (iter.hasNext()) {
                buf.append(", ");
            }
        }
        if (this.size() > 1) {
            buf.append("}");
        }
        return buf.toString();
    }

    @Deprecated
    protected String elementName(String[] tokenNames, int a) {
        return elementName(VocabularyImpl.fromTokenNames(tokenNames), a);
    }

    protected String elementName(Vocabulary vocabulary, int a) {
        if (a == Token.EOF) {
            return "<EOF>";
        } else if (a == Token.EPSILON) {
            return "<EPSILON>";
        } else {
            return vocabulary.getDisplayName(a);
        }
    }

    @Override
    public int size() {
        int n = 0;
        int numIntervals = intervals.size();
        if (numIntervals == 1) {
            Interval firstInterval = this.intervals.get(0);
            return firstInterval.b - firstInterval.a + 1;
        }
        for (int i = 0; i < numIntervals; i++) {
            Interval I = intervals.get(i);
            n += (I.b - I.a + 1);
        }
        return n;
    }

    public IntegerList toIntegerList() {
        IntegerList values = new IntegerList(size());
        int n = intervals.size();
        for (int i = 0; i < n; i++) {
            Interval I = intervals.get(i);
            int a = I.a;
            int b = I.b;
            for (int v = a; v <= b; v++) {
                values.add(v);
            }
        }
        return values;
    }

    @Override
    public List<Integer> toList() {
        List<Integer> values = new ArrayList<Integer>();
        int n = intervals.size();
        for (int i = 0; i < n; i++) {
            Interval I = intervals.get(i);
            int a = I.a;
            int b = I.b;
            for (int v = a; v <= b; v++) {
                values.add(v);
            }
        }
        return values;
    }

    public Set<Integer> toSet() {
        Set<Integer> s = new HashSet<Integer>();
        for (Interval I : intervals) {
            int a = I.a;
            int b = I.b;
            for (int v = a; v <= b; v++) {
                s.add(v);
            }
        }
        return s;
    }

    public int get(int i) {
        int n = intervals.size();
        int index = 0;
        for (int j = 0; j < n; j++) {
            Interval I = intervals.get(j);
            int a = I.a;
            int b = I.b;
            for (int v = a; v <= b; v++) {
                if (index == i) {
                    return v;
                }
                index++;
            }
        }
        return -1;
    }

    public int[] toArray() {
        return toIntegerList().toArray();
    }

    @Override
    public void remove(int el) {
        if (readonly)
            throw new IllegalStateException("can't alter readonly IntervalSet");
        int n = intervals.size();
        for (int i = 0; i < n; i++) {
            Interval I = intervals.get(i);
            int a = I.a;
            int b = I.b;
            if (el < a) {
                break;
            }
            if (el == a && el == b) {
                intervals.remove(i);
                break;
            }
            if (el == a) {
                I.a++;
                break;
            }
            if (el == b) {
                I.b--;
                break;
            }
            if (el > a && el < b) {
                int oldb = I.b;
                I.b = el - 1;
                add(el + 1, oldb);
            }
        }
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        if (this.readonly && !readonly)
            throw new IllegalStateException("can't alter readonly IntervalSet");
        this.readonly = readonly;
    }
}
