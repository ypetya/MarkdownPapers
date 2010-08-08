/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tautua.markdownpapers.generators;

import org.tautua.markdownpapers.grammar.*;
import org.tautua.markdownpapers.grammar.util.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>HTML generator.</p>
 *
 * @author Larry Ruiz
 */
public class HtmlGenerator implements Visitor {
    private static final Map<Character, String> ESCAPED_CHARS;
    private static final String EMPTY_STRING = "";
    private static final char TAB = '\t';
    private static final char EOL = '\n';
    private static final char SPACE = ' ';
    private Appendable buffer;
    private Document document;
    private Stack<Node> markupStack = new DequeStack<Node>();

    static {
        ESCAPED_CHARS = new HashMap();
        ESCAPED_CHARS.put('&', "&amp;");
        ESCAPED_CHARS.put('<', "&lt;");
        ESCAPED_CHARS.put('>', "&gt;");
        ESCAPED_CHARS.put('\"', "&quot;");
    }

    public HtmlGenerator(Appendable buffer) {
        this.buffer = buffer;
    }

    public void visit(CharRef node) {
        append(node.getValue());
    }

    public void visit(ClosingTag node) {
        append("</");
        append(node.getName());
        append(">");
    }

    public void visit(Code node) {
        append("<pre><code>");
        visitChildrenAndAppendSeparator(node, EOL);
        append("</code></pre>");
        append(EOL);
    }

    public void visit(CodeSpan node) {
        append("<code>");
        appendAndEscape(node.getText());
        append("</code>");
    }

    public void visit(CodeText node) {
        appendAndEscape(node.getValue());
    }

    public void visit(Comment node) {
        append("<!--");
        append(node.getText());
        append("-->");
    }

    public void visit(Document node) {
        document = node;
        visitChildrenAndAppendSeparator(node, EOL);
    }

    public void visit(Emphasis node) {
        switch (node.getType()) {
            case ITALIC:
                append("<em>");
                append(node.getText());
                append("</em>");
                break;
            case BOLD:
                append("<strong>");
                append(node.getText());
                append("</strong>");
                break;
            case ITALIC_AND_BOLD:
                append("<strong><em>");
                append(node.getText());
                append("</em></strong>");
                break;
        }
    }

    public void visit(Header node) {
        String level = String.valueOf(node.getLevel());
        append("<h");
        append(level);
        append(">");
        node.childrenAccept(this);
        append("</h");
        append(level);
        append(">");
        append(EOL);
    }

    public void visit(Image node) {
        Resource resource = resolveResource(node);
        if (resource == null) {
            append("<img src=\"\" alt=\"");
            appendAndEscape(node.getText());
            append("\"/>");
        } else {
            append("<img");
            append(" src=\"");
            appendAndEscape(resource.getLocation());
            if (node.getText() != null) {
                append("\" alt=\"");
                appendAndEscape(node.getText());
            }
            if (resource.getName() != null) {
                append("\" title=\"");
                appendAndEscape(resource.getName());
            }
            append("\"/>");
        }
    }

    public void visit(InlineUrl node) {
        append("<a href=\"");
        appendAndEscape(node.getUrl());
        append("\">");
        appendAndEscape(node.getUrl());
        append("</a>");
    }

    public void visit(Item node) {
        append("<li>");
        node.childrenAccept(this);
        append("</li>");
        append(EOL);
    }

    public void visit(Line node) {
        node.childrenAccept(this);
    }

    public void visit(Link node) {
        Resource resource = resolveResource(node);
        if (resource == null) {
            if (node.isReferenced()) {
                append("[");
                node.childrenAccept(this);
                append("]");
                if (node.getReferenceName() != null) {
                    if (node.hasWhitespaceAtMiddle()) {
                        append(' ');
                    }
                    append("[");
                    append(node.getReferenceName());
                    append("]");
                }
            } else {
                append("<a href=\"\">");
                node.childrenAccept(this);
                append("</a>");
            }
        } else {
            append("<a");
            append(" href=\"");
            appendAndEscape(resource.getLocation());
            if (resource.getName() != null) {
                append("\" title=\"");
                appendAndEscape(resource.getName());
            }
            append("\">");
            node.childrenAccept(this);
            append("</a>");
        }
    }

    public void visit(LinkRef node) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void visit(List node) {
        if (node.isOrdered()) {
            append("<ol>");
            append(EOL);
            node.childrenAccept(this);
            append("</ol>");
        } else {
            append("<ul>");
            append(EOL);
            node.childrenAccept(this);
            append("</ul>");
        }
        append(EOL);
    }

    public void visit(OpeningTag node) {
        append("<");
        append(node.getName());
        for(TagAttr attr : node.getAttributes()) {
            append(" ");
            append(attr.getName());
            append("=\"");
            append(attr.getValue());
            append("\"");
        }
        append(">");
    }

    public void visit(Paragraph node) {
        if (containsHR(node)) {
            visitChildrenAndAppendSeparator(node, EOL);
        } else if (isMarkup(node)) {
            switchToMarkup(node);
            visitChildrenAndAppendSeparator(node, EOL);
        } else {
            Node parent = node.jjtGetParent();
            if(parent instanceof Item) {
                if (!((Item)parent).isLoose()) {
                    visitChildrenAndAppendSeparator(node, EOL);
                    return;
                }
            }
            append("<p>");
            visitChildrenAndAppendSeparator(node, EOL);
            append("</p>");
            append(EOL);
        }
    }

    public void visit(Ruler node) {
        append("<hr/>");
        append(EOL);
    }

    public void visit(Quote node) {
        append("<blockquote>");
        append(EOL);
        node.childrenAccept(this);
        append("</blockquote>");
        append(EOL);
    }

    public void visit(SimpleNode node) {
        throw new IllegalArgumentException("can not process this element");
    }

    public void visit(EmptyTag node) {
        append("<");
        append(node.getName());
        for (TagAttr attr : node.getAttributes()) {
            append(" ");
            append(attr.getName());
            append("=\"");
            append(attr.getValue());
            append("\"");
        }
        append("/>");
    }

    public void visit(Text node) {
        appendAndEscape(node.getValue());
    }

    Resource resolveResource(Image image) {
        if (image.getResource() == null) {
            LinkRef linkRef;
            if (image.getRefId() == null) {
                linkRef = document.getLinkRef(image.getText());
            } else {
                linkRef = document.getLinkRef(image.getRefId());
            }
            return linkRef != null ? linkRef.getResource() : null;
        }

        return image.getResource();
    }

    Resource resolveResource(Link link) {
        if (link.isReferenced()) {
            LinkRef linkRef = null;
            if (link.getReferenceName() == null || link.getReferenceName().equals(EMPTY_STRING)) {
                linkRef = document.getLinkRef(link.getText());
            } else {
                linkRef = document.getLinkRef(link.getReferenceName());
            }
            return linkRef != null ? linkRef.getResource() : null;
        }

        return link.getResource();
    }

    void visitChildrenAndAppendSeparator(Node node, char separator){
        int count = node.jjtGetNumChildren();
        for(int i = 0; i < count; i++) {
            node.jjtGetChild(i).accept(this);
            if(i < count - 1) {
                append(separator);
            }
        }
    }

    void appendAndEscape(String val) {
        for(char c : val.toCharArray()) {
            String escape = ESCAPED_CHARS.get(c);
            if (escape != null) {
                append(escape);
            } else {
                append(c);
            }
        }
    }

    void append(String val) {
        try {
            buffer.append(val);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void append(char val) {
        try {
            buffer.append(val);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void switchToMarkup(Paragraph node){
        markupStack.push(node.jjtGetChild(0).jjtGetChild(0));
    }

    private boolean isMarkup(Paragraph node) {
        Node grandson = node.jjtGetChild(0).jjtGetChild(0);
        return grandson instanceof OpeningTag && ((OpeningTag)grandson).isBalanced();
    }

    private boolean containsHR(Paragraph node) {
        Node grandson = node.jjtGetChild(0).jjtGetChild(0);
        if (grandson instanceof Tag && ((Tag)grandson).getName().equalsIgnoreCase("hr")) {
            if (node.jjtGetNumChildren() > 1) {
                return false;
            } else if (node.jjtGetChild(0).jjtGetNumChildren() == 1) {
                return true;
            } else {
                for (int i = 1; i < node.jjtGetChild(0).jjtGetNumChildren(); i++) {
                    Node sibling = node.jjtGetChild(0).jjtGetChild(i);
                    if (!(sibling instanceof Text && ((Text)sibling).isWhitespace())) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean isOnMarkupBlock() {
        return markupStack.size() > 0;
    }
}