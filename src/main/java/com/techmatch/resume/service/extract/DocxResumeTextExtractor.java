package com.techmatch.resume.service.extract;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

@Component
public class DocxResumeTextExtractor implements ResumeTextExtractor {

    @Override
    public boolean supports(String extension) {
        return "docx".equalsIgnoreCase(extension);
    }

    @Override
    public String extract(byte[] content) {
        Exception poiException = null;
        try {
            String text = extractWithPoi(content);
            if (StringUtils.hasText(text)) {
                return text;
            }
        } catch (Exception exception) {
            poiException = exception;
        }

        try {
            String xmlText = extractFromXml(content);
            if (StringUtils.hasText(xmlText)) {
                return xmlText;
            }
        } catch (Exception xmlException) {
            if (poiException != null) {
                xmlException.addSuppressed(poiException);
            }
            throw new IllegalStateException("Failed to extract DOCX text", xmlException);
        }

        if (poiException != null) {
            throw new IllegalStateException("Failed to extract DOCX text", poiException);
        }
        throw new IllegalStateException("Failed to extract DOCX text: no readable text found");
    }

    private String extractWithPoi(byte[] content) throws Exception {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(content));
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return normalizeText(extractor.getText());
        }
    }

    private String extractFromXml(byte[] content) throws Exception {
        Set<String> fragments = new LinkedHashSet<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(content))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory() || !entry.getName().startsWith("word/") || !entry.getName().endsWith(".xml")) {
                    continue;
                }
                collectTextNodes(readEntry(zipInputStream), fragments);
            }
        }

        String text = String.join("\n", fragments);
        return normalizeText(text);
    }

    private byte[] readEntry(InputStream inputStream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        inputStream.transferTo(outputStream);
        return outputStream.toByteArray();
    }

    private void collectTextNodes(byte[] xmlContent, Set<String> fragments) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xmlContent));
        NodeList nodes = document.getElementsByTagNameNS("*", "t");
        for (int index = 0; index < nodes.getLength(); index++) {
            String value = normalizeText(nodes.item(index).getTextContent());
            if (StringUtils.hasText(value)) {
                fragments.add(value);
            }
        }

        NodeList paragraphNodes = document.getElementsByTagNameNS("*", "p");
        for (int index = 0; index < paragraphNodes.getLength(); index++) {
            String paragraphText = extractParagraphText(paragraphNodes.item(index));
            if (StringUtils.hasText(paragraphText)) {
                fragments.add(paragraphText);
            }
        }
    }

    private String extractParagraphText(Node paragraphNode) {
        NodeList childNodes = paragraphNode.getChildNodes();
        StringBuilder builder = new StringBuilder();
        appendNodeText(childNodes, builder);
        return normalizeText(builder.toString());
    }

    private void appendNodeText(NodeList nodeList, StringBuilder builder) {
        for (int index = 0; index < nodeList.getLength(); index++) {
            Node node = nodeList.item(index);
            String localName = node.getLocalName();
            if ("t".equals(localName)) {
                builder.append(node.getTextContent());
            } else if ("tab".equals(localName)) {
                builder.append('\t');
            } else if ("br".equals(localName) || "cr".equals(localName)) {
                builder.append('\n');
            }

            NodeList children = node.getChildNodes();
            if (children != null && children.getLength() > 0) {
                appendNodeText(children, builder);
            }
        }
    }

    private String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        return text.replace('\u00A0', ' ').trim();
    }
}
