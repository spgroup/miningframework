package org.dspace.sort;

import org.dspace.text.filter.DecomposeDiactritics;
import org.dspace.text.filter.LowerCaseAndTrim;
import org.dspace.text.filter.StandardInitialArticleWord;
import org.dspace.text.filter.TextFilter;

public class OrderFormatTitle extends AbstractTextFilterOFD {

    {
        filters = new TextFilter[] { new StandardInitialArticleWord(), new DecomposeDiactritics(), new LowerCaseAndTrim() };
    }
}
