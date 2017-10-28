package com.cas.circuit.config.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

public class CompMannualBuilder {
	public static void main(String[] args) throws FileNotFoundException {
		readAllCompCfg("通用元器件列表", "src/com/cas/circuit/config/components");
		readAllCompCfg("FUNUC专用元器件列表", "src/com/cas/circuit/config/components/fanuc");
		readAllCompCfg("Mitsubishi元器件列表", "src/com/cas/circuit/config/components/mitsubishi");
		readAllCompCfg("Panasonic专用元器件列表", "src/com/cas/circuit/config/components/panasonic");
		readAllCompCfg("YL_335B专用元器件列表", "src/com/cas/circuit/config/components/pl");
		readAllCompCfg("机器人专用元器件列表", "src/com/cas/circuit/config/components/robot");
	}

	private static void readAllCompCfg(String type, String path) throws FileNotFoundException {

		Map<String, List<String[]>> data = new HashMap<String, List<String[]>>();

		File dir = new File(path);
		File[] fileList = dir.listFiles();
		for (File file : fileList) {
			if (file.isFile() && file.getPath().endsWith(".xml")) {
				data.put(file.getName(), readXml(file));
			}
		}
//		System.out.println("CompMannualBuilder.readAllCompCfg()" + data);

		builder(type, data);
	}

	private static void builder(String filename, Map<String, List<String[]>> data) throws FileNotFoundException {
		File file = new File(filename);
//		1、【接触器】Accontactor.xml
//		型号：
//		【交流接触器】：LC1-D09M7C|LC1-D09M7C
		int count = 0;
		PrintWriter out = new PrintWriter(file);
		for (Entry<String, List<String[]>> entry : data.entrySet()) {
			count++;

			out.println(count + "、" + entry.getKey());
//			out.println("型号：");

			List<String[]> models = entry.getValue();
			for (String[] info : models) {
				out.println("【型号：" + info[0] + "】：[" + info[1] + "]");
			}

			out.println("###########################");
			out.println();
		}
		out.flush();
		out.close();
	}

	private static List<String[]> readXml(File file) throws FileNotFoundException {
		List<String[]> listData = new ArrayList<String[]>();
		SAXReader saxReader = createNonvalidatXmlReader();
		Document document = null;
		System.out.println("CompMannualBuilder.readXml()" + file);
		try {
			document = saxReader.read(new FileInputStream(file));
		} catch (Exception e) {
			System.err.println("ParserFactory.Parser.initItem() " + e.getMessage() + "\r\n " + file);
			return listData;
		}

		Element root = document.getRootElement();

		Iterator compIter = root.elementIterator("ElecCompDef");
		while (compIter.hasNext()) {
			String[] data = new String[2];
			Element element = (Element) compIter.next();
			data[0] = element.attributeValue("model");
			data[1] = element.attributeValue("name");
//			data[2] = element.attributeValue("mdlRef");

			listData.add(data);
		}

		return listData;
	}

	private static SAXReader createNonvalidatXmlReader() {
		SAXReader saxReader = new SAXReader(false);
		try {
			saxReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (SAXException e) {
			e.printStackTrace();
		}

		return saxReader;
	}
}
