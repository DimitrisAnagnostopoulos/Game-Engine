package xmlParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlParser {

	private static final Pattern DATA = Pattern.compile(">(.+?)<");
	private static final Pattern START_TAG = Pattern.compile("<(.+?)>");
	private static final Pattern ATTR_NAME = Pattern.compile("(.+?)=");
	private static final Pattern ATTR_VAL = Pattern.compile("\"(.+?)\"");
	private static final Pattern CLOSED = Pattern.compile("(</|/>)");

	public static XmlNode loadXmlFile(String filePath) {
		BufferedReader reader = null;
		try {
			FileReader isr = null;
			isr = new FileReader(filePath);
			reader = new BufferedReader(isr);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Can't find the XML file: " + filePath);
			System.exit(0);
			return null;
		}
		try {
			reader.readLine();
			XmlNode node = loadNode(reader);
			reader.close();
			return node;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error with XML file format for: " + filePath);
			System.exit(0);
			return null;
		}
	}

	private static XmlNode loadNode(BufferedReader reader) throws Exception {
		String line = reader.readLine().trim();
		if (line.startsWith("</")) {
			return null;
		}
		String[] startTagParts = getStartTag(line).split(" ");
		XmlNode node = new XmlNode(startTagParts[0].replace("/", ""));
		addAttributes(startTagParts, node);
		addData(line, node);
		if (CLOSED.matcher(line).find()) {
			return node;
		}
		XmlNode child = null;
		while ((child = loadNode(reader)) != null) {
			node.addChild(child);
		}
		return node;
	}

	private static void addData(String line, XmlNode node) {
		Matcher matcher = DATA.matcher(line);
		if (matcher.find()) {
			node.setData(matcher.group(1));
		}
	}

	private static void addAttributes(String[] titleParts, XmlNode node) {
		for (int i = 1; i < titleParts.length; i++) {
			if (titleParts[i].contains("=")) {
				addAttribute(titleParts[i], node);
			}
		}
	}

	private static void addAttribute(String attributeLine, XmlNode node) {
		Matcher nameMatch = ATTR_NAME.matcher(attributeLine);
		nameMatch.find();
		Matcher valMatch = ATTR_VAL.matcher(attributeLine);
		valMatch.find();
		node.addAttribute(nameMatch.group(1), valMatch.group(1));
	}

	private static String getStartTag(String line) {
		Matcher match = START_TAG.matcher(line);
		match.find();
		return match.group(1);
	}

}
