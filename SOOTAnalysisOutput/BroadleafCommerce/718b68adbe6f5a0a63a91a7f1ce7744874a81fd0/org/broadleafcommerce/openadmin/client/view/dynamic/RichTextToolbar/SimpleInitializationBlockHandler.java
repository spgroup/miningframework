package org.broadleafcommerce.openadmin.client.view.dynamic;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.RichTextArea.Formatter;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.smartgwt.client.widgets.form.ColorPicker;
import com.smartgwt.client.widgets.form.events.ColorSelectedEvent;
import com.smartgwt.client.widgets.form.events.ColorSelectedHandler;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class RichTextToolbar extends Composite {

    public static enum DisplayType {

        DETAILED, BASIC
    }

    private static final String HTTP_STATIC_ICONS_GIF = GWT.getModuleBaseURL() + "admin/images/icons.gif";

    private static final String CSS_ROOT_NAME = "RichTextToolbar";

    public final static LinkedHashMap<String, RichTextArea.FontSize> GUI_FONT_SIZES = new LinkedHashMap<String, RichTextArea.FontSize>();

    static {
        GUI_FONT_SIZES.put(RichTextArea.FontSize.XX_SMALL.toString() + " (8 pt)", RichTextArea.FontSize.XX_SMALL);
        GUI_FONT_SIZES.put(RichTextArea.FontSize.X_SMALL.toString() + " (10 pt)", RichTextArea.FontSize.X_SMALL);
        GUI_FONT_SIZES.put(RichTextArea.FontSize.SMALL.toString() + " (12 pt)", RichTextArea.FontSize.SMALL);
        GUI_FONT_SIZES.put(RichTextArea.FontSize.MEDIUM.toString() + " (14 pt)", RichTextArea.FontSize.MEDIUM);
        GUI_FONT_SIZES.put(RichTextArea.FontSize.LARGE.toString() + " (18 pt)", RichTextArea.FontSize.LARGE);
        GUI_FONT_SIZES.put(RichTextArea.FontSize.X_LARGE.toString() + " (24 pt)", RichTextArea.FontSize.X_LARGE);
        GUI_FONT_SIZES.put(RichTextArea.FontSize.XX_LARGE.toString() + " (36 pt)", RichTextArea.FontSize.XX_LARGE);
    }

    public final static HashMap<String, String> GUI_FONTLIST = new HashMap<String, String>();

    static {
        GUI_FONTLIST.put("Helvetica", "Helvetica");
        GUI_FONTLIST.put("Times New Roman", "Times New Roman");
        GUI_FONTLIST.put("Arial", "Arial");
        GUI_FONTLIST.put("Courier New", "Courier New");
        GUI_FONTLIST.put("Georgia", "Georgia");
        GUI_FONTLIST.put("Trebuchet", "Trebuchet");
        GUI_FONTLIST.put("Verdana", "Verdana");
    }

    private static final String HTML_STYLE_CLOSE_SPAN = "</span>";

    private static final String HTML_STYLE_CLOSE_FONT = "</font>";

    private static final String HTML_STYLE_CLOSE_DIV = "</div>";

    private static final String HTML_STYLE_OPEN_BOLD = "<span style=\"font-weight: bold;\">";

    private static final String HTML_STYLE_OPEN_ITALIC = "<span style=\"font-weight: italic;\">";

    private static final String HTML_STYLE_OPEN_UNDERLINE = "<span style=\"font-weight: underline;\">";

    private static final String HTML_STYLE_OPEN_LINETHROUGH = "<span style=\"font-weight: line-through;\">";

    private static final String HTML_STYLE_OPEN_ALIGNLEFT = "<div style=\"text-align: left;\">";

    private static final String HTML_STYLE_OPEN_ALIGNCENTER = "<div style=\"text-align: center;\">";

    private static final String HTML_STYLE_OPEN_ALIGNRIGHT = "<div style=\"text-align: right;\">";

    private static final String HTML_STYLE_OPEN_INDENTRIGHT = "<div style=\"margin-left: 40px;\">";

    private static final String HTML_STYLE_OPEN_SUBSCRIPT = "<sub>";

    private static final String HTML_STYLE_CLOSE_SUBSCRIPT = "</sub>";

    private static final String HTML_STYLE_OPEN_SUPERSCRIPT = "<sup>";

    private static final String HTML_STYLE_CLOSE_SUPERSCRIPT = "</sup>";

    private static final String HTML_STYLE_OPEN_ORDERLIST = "<ol><li>";

    private static final String HTML_STYLE_CLOSE_ORDERLIST = "</ol></li>";

    private static final String HTML_STYLE_OPEN_UNORDERLIST = "<ul><li>";

    private static final String HTML_STYLE_CLOSE_UNORDERLIST = "</ul></li>";

    private static final String HTML_STYLE_HLINE = "<hr style=\"width: 100%; height: 2px;\">";

    private static final String GUI_DIALOG_INSERTURL = "Enter a link URL:";

    private static final String GUI_DIALOG_IMAGEURL = "Enter an image URL:";

    private static final String GUI_LISTNAME_FONT_SIZES = "Font Sizes";

    private static final String GUI_LISTNAME_FONTS = "Fonts";

    private static final String GUI_HOVERTEXT_SWITCHVIEW = "Switch View HTML/Source";

    private static final String GUI_HOVERTEXT_REMOVEFORMAT = "Remove Formatting";

    private static final String GUI_HOVERTEXT_IMAGE = "Insert Image";

    private static final String GUI_HOVERTEXT_HLINE = "Insert Horizontal Line";

    private static final String GUI_HOVERTEXT_BREAKLINK = "Break Link";

    private static final String GUI_HOVERTEXT_LINK = "Generate Link";

    private static final String GUI_HOVERTEXT_ASSET = "Insert Asset";

    private static final String GUI_HOVERTEXT_IDENTLEFT = "Ident Left";

    private static final String GUI_HOVERTEXT_IDENTRIGHT = "Ident Right";

    private static final String GUI_HOVERTEXT_UNORDERLIST = "Unordered List";

    private static final String GUI_HOVERTEXT_ORDERLIST = "Ordered List";

    private static final String GUI_HOVERTEXT_ALIGNRIGHT = "Align Right";

    private static final String GUI_HOVERTEXT_ALIGNCENTER = "Align Center";

    private static final String GUI_HOVERTEXT_ALIGNLEFT = "Align Left";

    private static final String GUI_HOVERTEXT_SUPERSCRIPT = "Superscript";

    private static final String GUI_HOVERTEXT_SUBSCRIPT = "Subscript";

    private static final String GUI_HOVERTEXT_STROKE = "Stroke";

    private static final String GUI_HOVERTEXT_FOREGROUND = "Foreground Color";

    private static final String GUI_HOVERTEXT_BACKGROUND = "Background Color";

    private static final String GUI_HOVERTEXT_UNDERLINE = "Underline";

    private static final String GUI_HOVERTEXT_ITALIC = "Italic";

    private static final String GUI_HOVERTEXT_BOLD = "Bold";

    private final VerticalPanel outer;

    private final HorizontalPanel topPanel;

    private final HorizontalPanel bottomPanel;

    private final RichTextArea styleText;

    private final Formatter styleTextFormatter;

    private final EventHandler evHandler;

    private ToggleButton bold;

    private ToggleButton italic;

    private ToggleButton underline;

    private ToggleButton stroke;

    private PushButton foreground;

    private PushButton background;

    private ToggleButton subscript;

    private ToggleButton superscript;

    private PushButton alignleft;

    private PushButton alignmiddle;

    private PushButton alignright;

    private PushButton orderlist;

    private PushButton unorderlist;

    private PushButton indentleft;

    private PushButton indentright;

    private PushButton generatelink;

    private PushButton breaklink;

    private PushButton insertline;

    private PushButton insertimage;

    private PushButton removeformatting;

    private ToggleButton texthtml;

    private ListBox fontlist;

    private ListBox fontSizeList;

    private Button assetslink;

    private Command saveCommand;

    public RichTextToolbar(RichTextArea richtext, DisplayType displayType) {
        outer = new VerticalPanel();
        topPanel = new HorizontalPanel();
        bottomPanel = new HorizontalPanel();
        topPanel.setStyleName(CSS_ROOT_NAME);
        bottomPanel.setStyleName(CSS_ROOT_NAME);
        styleText = richtext;
        styleTextFormatter = styleText.getFormatter();
        topPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
        bottomPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
        outer.add(topPanel);
        outer.add(bottomPanel);
        outer.setWidth("100%");
        outer.setStyleName(CSS_ROOT_NAME);
        initWidget(outer);
        evHandler = new EventHandler();
        styleText.addKeyUpHandler(evHandler);
        styleText.addClickHandler(evHandler);
        buildTools();
    }

    private class EventHandler implements ClickHandler, KeyUpHandler, ChangeHandler {

        @Override
        public void onClick(ClickEvent event) {
            if (event.getSource().equals(bold)) {
                if (isHTMLMode()) {
                    changeHtmlStyle(HTML_STYLE_OPEN_BOLD, HTML_STYLE_CLOSE_SPAN);
                } else {
                    styleTextFormatter.toggleBold();
                }
            } else if (event.getSource().equals(italic)) {
                if (isHTMLMode()) {
                    changeHtmlStyle(HTML_STYLE_OPEN_ITALIC, HTML_STYLE_CLOSE_SPAN);
                } else {
                    styleTextFormatter.toggleItalic();
                }
            } else if (event.getSource().equals(underline)) {
                if (isHTMLMode()) {
                    changeHtmlStyle(HTML_STYLE_OPEN_UNDERLINE, HTML_STYLE_CLOSE_SPAN);
                } else {
                    styleTextFormatter.toggleUnderline();
                }
            } else if (event.getSource().equals(stroke)) {
                if (isHTMLMode()) {
                    changeHtmlStyle(HTML_STYLE_OPEN_LINETHROUGH, HTML_STYLE_CLOSE_SPAN);
                } else {
                    styleTextFormatter.toggleStrikethrough();
                }
            } else if (event.getSource().equals(subscript)) {
                if (isHTMLMode()) {
                    changeHtmlStyle(HTML_STYLE_OPEN_SUBSCRIPT, HTML_STYLE_CLOSE_SUBSCRIPT);
                } else {
                    styleTextFormatter.toggleSubscript();
                }
            } else if (event.getSource().equals(superscript)) {
                if (isHTMLMode()) {
                    changeHtmlStyle(HTML_STYLE_OPEN_SUPERSCRIPT, HTML_STYLE_CLOSE_SUPERSCRIPT);
                } else {
                    styleTextFormatter.toggleSuperscript();
                }
            } else if (event.getSource().equals(alignleft)) {
                if (isHTMLMode()) {
                    changeHtmlStyle(HTML_STYLE_OPEN_ALIGNLEFT, HTML_STYLE_CLOSE_DIV);
                } else {
                    styleTextFormatter.setJustification(RichTextArea.Justification.LEFT);
                }
            } else if (event.getSource().equals(alignmiddle)) {
                if (isHTMLMode()) {
                    changeHtmlStyle(HTML_STYLE_OPEN_ALIGNCENTER, HTML_STYLE_CLOSE_DIV);
                } else {
                    styleTextFormatter.setJustification(RichTextArea.Justification.CENTER);
                }
            } else if (event.getSource().equals(alignright)) {
                if (isHTMLMode()) {
                    changeHtmlStyle(HTML_STYLE_OPEN_ALIGNRIGHT, HTML_STYLE_CLOSE_DIV);
                } else {
                    styleTextFormatter.setJustification(RichTextArea.Justification.RIGHT);
                }
            } else if (event.getSource().equals(orderlist)) {
                if (isHTMLMode()) {
                    changeHtmlStyle(HTML_STYLE_OPEN_ORDERLIST, HTML_STYLE_CLOSE_ORDERLIST);
                } else {
                    styleTextFormatter.insertOrderedList();
                }
            } else if (event.getSource().equals(unorderlist)) {
                if (isHTMLMode()) {
                    changeHtmlStyle(HTML_STYLE_OPEN_UNORDERLIST, HTML_STYLE_CLOSE_UNORDERLIST);
                } else {
                    styleTextFormatter.insertUnorderedList();
                }
            } else if (event.getSource().equals(indentright)) {
                if (isHTMLMode()) {
                    changeHtmlStyle(HTML_STYLE_OPEN_INDENTRIGHT, HTML_STYLE_CLOSE_DIV);
                } else {
                    styleTextFormatter.rightIndent();
                }
            } else if (event.getSource().equals(indentleft)) {
                if (isHTMLMode()) {
                } else {
                    styleTextFormatter.leftIndent();
                }
            } else if (event.getSource().equals(generatelink)) {
                String url = Window.prompt(GUI_DIALOG_INSERTURL, "http://");
                if (url != null) {
                    if (isHTMLMode()) {
                        changeHtmlStyle("<a href=\"" + url + "\">", "</a>");
                    } else {
                        styleTextFormatter.createLink(url);
                    }
                }
            } else if (event.getSource().equals(assetslink)) {
            } else if (event.getSource().equals(breaklink)) {
                if (isHTMLMode()) {
                } else {
                    styleTextFormatter.removeLink();
                }
            } else if (event.getSource().equals(insertimage)) {
                String url = Window.prompt(GUI_DIALOG_IMAGEURL, "http://");
                if (url != null) {
                    if (isHTMLMode()) {
                        changeHtmlStyle("<img src=\"" + url + "\">", "");
                    } else {
                        styleTextFormatter.insertImage(url);
                    }
                }
            } else if (event.getSource().equals(insertline)) {
                if (isHTMLMode()) {
                    changeHtmlStyle(HTML_STYLE_HLINE, "");
                } else {
                    styleTextFormatter.insertHorizontalRule();
                }
            } else if (event.getSource().equals(removeformatting)) {
                if (isHTMLMode()) {
                } else {
                    styleTextFormatter.removeFormat();
                }
            } else if (event.getSource().equals(foreground)) {
                ColorPicker picker = new ColorPicker();
                picker.addColorSelectedHandler(new ColorSelectedHandler() {

                    @Override
                    public void onColorSelected(ColorSelectedEvent event) {
                        if (isHTMLMode()) {
                            changeHtmlStyle("<span style=\"color: " + event.getColor() + ";\">", HTML_STYLE_CLOSE_SPAN);
                        } else {
                            styleTextFormatter.setForeColor(event.getColor());
                        }
                    }
                });
                picker.show();
            } else if (event.getSource().equals(background)) {
                ColorPicker picker = new ColorPicker();
                picker.addColorSelectedHandler(new ColorSelectedHandler() {

                    @Override
                    public void onColorSelected(ColorSelectedEvent event) {
                        if (isHTMLMode()) {
                            changeHtmlStyle("<span style=\"background: " + event.getColor() + ";\">", HTML_STYLE_CLOSE_SPAN);
                        } else {
                            styleTextFormatter.setBackColor(event.getColor());
                        }
                    }
                });
                picker.show();
            } else if (event.getSource().equals(texthtml)) {
                if (texthtml.isDown()) {
                    styleText.setText(styleText.getHTML());
                } else {
                    styleText.setHTML(styleText.getText());
                }
            } else if (event.getSource().equals(styleText)) {
            }
            updateStatus();
        }

        @Override
        public void onKeyUp(KeyUpEvent event) {
            updateStatus();
        }

        @Override
        public void onChange(ChangeEvent event) {
            if (event.getSource().equals(fontlist)) {
                if (isHTMLMode()) {
                    changeHtmlStyle("<span style=\"font-family: " + fontlist.getValue(fontlist.getSelectedIndex()) + ";\">", HTML_STYLE_CLOSE_SPAN);
                } else {
                    styleTextFormatter.setFontName(fontlist.getValue(fontlist.getSelectedIndex()));
                }
            } else if (event.getSource().equals(fontSizeList)) {
                if (isHTMLMode()) {
                    changeHtmlStyle("<font size=\"" + GUI_FONT_SIZES.get(fontSizeList.getValue(fontSizeList.getSelectedIndex())).toString() + "\">", HTML_STYLE_CLOSE_FONT);
                } else {
                    styleTextFormatter.setFontSize(GUI_FONT_SIZES.get(fontSizeList.getValue(fontSizeList.getSelectedIndex())));
                }
            }
        }
    }

    public static native JsArrayString getSelection(Element elem);

    private void changeHtmlStyle(String startTag, String stopTag) {
        JsArrayString tx = getSelection(styleText.getElement());
        String txbuffer = styleText.getText();
        Integer startpos = Integer.parseInt(tx.get(1));
        String selectedText = tx.get(0);
        styleText.setText(txbuffer.substring(0, startpos) + startTag + selectedText + stopTag + txbuffer.substring(startpos + selectedText.length()));
    }

    private Boolean isHTMLMode() {
        return texthtml.isDown();
    }

    private void updateStatus() {
        if (styleTextFormatter != null) {
            bold.setDown(styleTextFormatter.isBold());
            italic.setDown(styleTextFormatter.isItalic());
            underline.setDown(styleTextFormatter.isUnderlined());
            subscript.setDown(styleTextFormatter.isSubscript());
            superscript.setDown(styleTextFormatter.isSuperscript());
            stroke.setDown(styleTextFormatter.isStrikethrough());
        }
        if (isHTMLMode()) {
            removeformatting.setEnabled(false);
            indentleft.setEnabled(false);
            breaklink.setEnabled(false);
        } else {
            removeformatting.setEnabled(true);
            indentleft.setEnabled(true);
            breaklink.setEnabled(true);
        }
    }

    private void buildTools() {
        topPanel.add(bold = createToggleButton(HTTP_STATIC_ICONS_GIF, 0, 0, 20, 20, GUI_HOVERTEXT_BOLD));
        topPanel.add(italic = createToggleButton(HTTP_STATIC_ICONS_GIF, 0, 60, 20, 20, GUI_HOVERTEXT_ITALIC));
        topPanel.add(underline = createToggleButton(HTTP_STATIC_ICONS_GIF, 0, 140, 20, 20, GUI_HOVERTEXT_UNDERLINE));
        topPanel.add(stroke = createToggleButton(HTTP_STATIC_ICONS_GIF, 0, 120, 20, 20, GUI_HOVERTEXT_STROKE));
        topPanel.add(foreground = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 719, 20, 20, GUI_HOVERTEXT_FOREGROUND));
        topPanel.add(background = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 761, 20, 20, GUI_HOVERTEXT_BACKGROUND));
        topPanel.add(new HTML("&nbsp;"));
        topPanel.add(subscript = createToggleButton(HTTP_STATIC_ICONS_GIF, 0, 600, 20, 20, GUI_HOVERTEXT_SUBSCRIPT));
        topPanel.add(superscript = createToggleButton(HTTP_STATIC_ICONS_GIF, 0, 620, 20, 20, GUI_HOVERTEXT_SUPERSCRIPT));
        topPanel.add(new HTML("&nbsp;"));
        topPanel.add(alignleft = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 460, 20, 20, GUI_HOVERTEXT_ALIGNLEFT));
        topPanel.add(alignmiddle = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 420, 20, 20, GUI_HOVERTEXT_ALIGNCENTER));
        topPanel.add(alignright = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 480, 20, 20, GUI_HOVERTEXT_ALIGNRIGHT));
        topPanel.add(new HTML("&nbsp;"));
        topPanel.add(orderlist = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 80, 20, 20, GUI_HOVERTEXT_ORDERLIST));
        topPanel.add(unorderlist = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 20, 20, 20, GUI_HOVERTEXT_UNORDERLIST));
        topPanel.add(indentright = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 400, 20, 20, GUI_HOVERTEXT_IDENTRIGHT));
        topPanel.add(indentleft = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 540, 20, 20, GUI_HOVERTEXT_IDENTLEFT));
        topPanel.add(new HTML("&nbsp;"));
        topPanel.add(generatelink = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 500, 20, 20, GUI_HOVERTEXT_LINK));
        topPanel.add(breaklink = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 640, 20, 20, GUI_HOVERTEXT_BREAKLINK));
        topPanel.add(new HTML("&nbsp;"));
        topPanel.add(insertline = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 360, 20, 20, GUI_HOVERTEXT_HLINE));
        topPanel.add(insertimage = createPushButton(HTTP_STATIC_ICONS_GIF, 0, 380, 20, 20, GUI_HOVERTEXT_IMAGE));
        topPanel.add(new HTML("&nbsp;"));
        topPanel.add(removeformatting = createPushButton(HTTP_STATIC_ICONS_GIF, 20, 460, 20, 20, GUI_HOVERTEXT_REMOVEFORMAT));
        topPanel.add(new HTML("&nbsp;"));
        topPanel.add(texthtml = createToggleButton(HTTP_STATIC_ICONS_GIF, 0, 260, 20, 20, GUI_HOVERTEXT_SWITCHVIEW));
        bottomPanel.add(fontlist = createFontList());
        bottomPanel.add(new HTML("&nbsp;"));
        bottomPanel.add(fontSizeList = createFontSizeList());
        bottomPanel.add(assetslink = createTextPushButton(GUI_HOVERTEXT_ASSET, 0, 380, 100, 20, GUI_HOVERTEXT_ASSET));
    }

    private ToggleButton createToggleButton(String url, Integer top, Integer left, Integer width, Integer height, String tip) {
        Image extract = new Image(url, left, top, width, height);
        ToggleButton tb = new ToggleButton(extract);
        tb.setHeight(height + "px");
        tb.setWidth(width + "px");
        tb.addClickHandler(evHandler);
        if (tip != null) {
            tb.setTitle(tip);
        }
        return tb;
    }

    private PushButton createPushButton(String url, Integer top, Integer left, Integer width, Integer height, String tip) {
        Image extract = new Image(url, left, top, width, height);
        PushButton tb = new PushButton(extract);
        tb.setHeight(height + "px");
        tb.setWidth(width + "px");
        tb.addClickHandler(evHandler);
        if (tip != null) {
            tb.setTitle(tip);
        }
        return tb;
    }

    private Button createTextPushButton(String url, Integer top, Integer left, Integer width, Integer height, String tip) {
        Image extract = new Image(url, left, top, width, height);
        Button tb = new Button(url);
        tb.setHeight(height + "px");
        tb.setWidth(width + "px");
        tb.addClickHandler(evHandler);
        if (tip != null) {
            tb.setTitle(tip);
        }
        return tb;
    }

    private ListBox createFontList() {
        ListBox mylistBox = new ListBox();
        mylistBox.addChangeHandler(evHandler);
        mylistBox.setVisibleItemCount(1);
        mylistBox.addItem(GUI_LISTNAME_FONTS);
        for (String name : GUI_FONTLIST.keySet()) {
            mylistBox.addItem(name, GUI_FONTLIST.get(name));
        }
        return mylistBox;
    }

    private ListBox createFontSizeList() {
        ListBox mylistBox = new ListBox();
        mylistBox.addChangeHandler(evHandler);
        mylistBox.setVisibleItemCount(1);
        mylistBox.addItem(GUI_LISTNAME_FONT_SIZES);
        for (String name : GUI_FONT_SIZES.keySet()) {
            mylistBox.addItem(name);
        }
        return mylistBox;
    }

    public void insertAsset(String fileExtension, String name, String staticAssetFullUrl) {
        String richContent;
        if (fileExtension.equals("gif") || fileExtension.equals("jpg") || fileExtension.equals("png") || fileExtension.equals("jpeg")) {
            richContent = "<img title='" + name + "' src='" + staticAssetFullUrl + "' alt='" + name + "'/>";
            if (isHTMLMode()) {
                changeHtmlStyle(richContent, "");
            } else {
                styleTextFormatter.insertHTML(richContent);
            }
        } else {
            richContent = "<a href='" + staticAssetFullUrl + "'>" + name + "</a>";
            if (isHTMLMode()) {
                changeHtmlStyle("<a href=\"" + staticAssetFullUrl + "\">", "</a>");
            } else {
                styleTextFormatter.createLink(richContent);
            }
        }
    }

    public void addAssetHandler(final Command command) {
        assetslink.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                command.execute();
            }
        });
    }

    public String getHTML() {
        if (texthtml.isDown()) {
            return styleText.getText();
        } else {
            return styleText.getHTML();
        }
    }

    public void setHTML(String htmlString) {
        if (texthtml.isDown()) {
            styleText.setText(htmlString);
        } else {
            styleText.setHTML(htmlString);
        }
    }

    public void showAssetButton(boolean visible) {
        assetslink.setVisible(visible);
    }
}