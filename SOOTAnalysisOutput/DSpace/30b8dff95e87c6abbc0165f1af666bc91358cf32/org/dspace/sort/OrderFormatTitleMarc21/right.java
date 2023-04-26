package org.dspace.sort;

import org.dspace.text.filter.DecomposeDiactritics;
import org.dspace.text.filter.LowerCaseAndTrim;
import org.dspace.text.filter.MARC21InitialArticleWord;
import org.dspace.text.filter.StripDiacritics;
import org.dspace.text.filter.StripLeadingNonAlphaNum;
import org.dspace.text.filter.TextFilter;

public class OrderFormatTitleMarc21 extends AbstractTextFilterOFD {

    {
        filters = new TextFilter[] { new MARC21InitialArticleWord(), new DecomposeDiactritics(), new StripDiacritics(), new StripLeadingNonAlphaNum(), new LowerCaseAndTrim() };
    }
}
