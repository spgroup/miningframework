package org.dspace.sort;

import org.dspace.text.filter.DecomposeDiactritics;
import org.dspace.text.filter.LowerCaseAndTrim;
import org.dspace.text.filter.StripDiacritics;
import org.dspace.text.filter.TextFilter;

public class OrderFormatText extends AbstractTextFilterOFD {

    {
        filters = new TextFilter[] { new DecomposeDiactritics(), new StripDiacritics(), new LowerCaseAndTrim() };
    }
}
